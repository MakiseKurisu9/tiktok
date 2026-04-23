<template>
  <v-container class="px-4 py-6">
    <div class="mb-8">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">推荐视频</h1>
      <p class="text-body-1 text-medium-emphasis">看看你感兴趣的内容</p>
    </div>

    <div v-if="initialLoading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
      <p class="text-body-2 text-medium-emphasis mt-4">正在加载推荐视频...</p>
    </div>

    <v-row v-else class="video-grid">
      <v-col v-for="video in pushedVideos" :key="video.id" cols="12" sm="6" md="4" lg="3" class="video-col">
        <VideoCard :video="video" @play="openPlayer" @toggle-like="toggleLike" />
      </v-col>
    </v-row>

    <v-card v-if="!initialLoading && pushedVideos.length === 0" class="text-center py-16 my-8" variant="outlined">
      <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-video-off-outline</v-icon>
      <h3 class="text-h6 mb-2">暂无推荐视频</h3>
      <p class="text-body-2 text-medium-emphasis mb-4">稍后再来看看，或许会有新的精彩内容</p>
      <v-btn color="primary" variant="outlined" @click="refreshVideos">刷新</v-btn>
    </v-card>

    <div v-if="pushedVideos.length > 0" class="text-center py-8">
      <v-btn color="primary" variant="outlined" @click="refreshVideos" :loading="initialLoading">换一批</v-btn>
    </div>

    <VideoPlayerDialog v-model="showPlayer" :video="currentVideo" @liked="onLiked" />

    <v-snackbar v-model="errorSnackbar" color="error" timeout="4000" location="top">{{ errorMessage }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { getPushVideos } from "@/api";
import { starVideo } from "@/api/video";
import { ref, onMounted } from "vue";
import VideoCard from "@/components/VideoCard.vue";
import VideoPlayerDialog from "@/components/VideoPlayerDialog.vue";

const pushedVideos = ref([]);
const initialLoading = ref(true);
const errorSnackbar = ref(false);
const errorMessage = ref("");
const showPlayer = ref(false);
const currentVideo = ref(null);

function openPlayer(video) {
  currentVideo.value = { ...video };
  showPlayer.value = true;
}

async function toggleLike(video) {
  try {
    await starVideo(video.id);
    video.isLiked = !video.isLiked;
    video.likes = Math.max(0, (video.likes || 0) + (video.isLiked ? 1 : -1));
  } catch (e) { console.error(e); }
}

function onLiked(videoId, isLiked) {
  const v = pushedVideos.value.find((x) => x.id === videoId);
  if (v) { v.isLiked = isLiked; v.likes += isLiked ? 1 : -1; }
}

async function loadVideos() {
  initialLoading.value = true;
  try {
    const response = await getPushVideos();
    if (!response || !response.data) throw new Error("Invalid response");

    let newVideos = [];
    const d = response.data;
    if (Array.isArray(d)) newVideos = d;
    else if (d.items) newVideos = d.items;
    else if (d.list) newVideos = d.list;
    else if (d.records) newVideos = d.records;

    pushedVideos.value = newVideos.map((v) => ({
      ...v, isLiked: v.isLiked || false, isFavourited: v.isFavourited || false,
      likes: v.likes || 0, views: v.views || 0, favourites: v.favourites || 0,
      shares: v.shares || 0, comments: v.comments || 0,
    }));
  } catch (error) {
    console.error("加载推荐视频失败", error);
    errorMessage.value = "加载视频失败，请稍后重试";
    errorSnackbar.value = true;
  } finally {
    initialLoading.value = false;
  }
}

async function refreshVideos() {
  pushedVideos.value = [];
  await loadVideos();
}

onMounted(() => loadVideos());
</script>

<style scoped>
.gradient-text { background: linear-gradient(45deg, #ff6b6b, #4ecdc4); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.video-grid { margin: 0 -8px; }
.video-col { padding: 8px; }
</style>
