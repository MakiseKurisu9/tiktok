<template>
  <v-container class="px-4 py-6">
    <div class="mb-8">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">
        {{ typeName || '分类视频' }}
      </h1>
      <p class="text-body-1 text-medium-emphasis">发现感兴趣的精彩内容</p>
    </div>

    <div v-if="initialLoading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
      <p class="text-body-2 text-medium-emphasis mt-4">正在加载视频...</p>
    </div>

    <v-row v-else class="video-grid">
      <v-col v-for="video in typeVideos" :key="video.id" cols="12" sm="6" md="4" lg="3" class="video-col">
        <VideoCard :video="video" @play="openPlayer" @toggle-like="toggleLike" />
      </v-col>
    </v-row>

    <div class="text-center my-8" v-if="loadingMore">
      <v-progress-circular indeterminate color="primary" size="40" width="3" />
      <p class="text-body-2 text-medium-emphasis mt-3">加载更多视频...</p>
    </div>

    <v-card v-if="!loadingMore && typeVideos.length === 0 && !initialLoading" class="text-center py-16 my-8" variant="outlined">
      <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-video-off-outline</v-icon>
      <h3 class="text-h6 mb-2">该分类下暂无视频</h3>
      <v-btn color="primary" variant="outlined" @click="refreshVideos">刷新</v-btn>
    </v-card>

    <div v-if="!hasMore && typeVideos.length > 0" class="text-center py-8">
      <v-divider class="mb-4" />
      <p class="text-body-2 text-medium-emphasis">已加载全部内容 ({{ typeVideos.length }} 个视频)</p>
    </div>

    <VideoPlayerDialog v-model="showPlayer" :video="currentVideo" @liked="onLiked" />

    <div ref="infiniteScrollTrigger" style="height: 10px; visibility: hidden;" />

    <v-snackbar v-model="errorSnackbar" color="error" timeout="4000" location="top">{{ errorMessage }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from "vue";
import { getVideosByTypeId } from "@/api";
import { starVideo } from "@/api/video";
import VideoCard from "@/components/VideoCard.vue";
import VideoPlayerDialog from "@/components/VideoPlayerDialog.vue";

const props = defineProps(["id"]);

const typeVideos = ref([]);
const typeName = ref("");
const page = ref(1);
const pageSize = ref(8);
const loadingMore = ref(false);
const initialLoading = ref(true);
const hasMore = ref(true);
const errorSnackbar = ref(false);
const errorMessage = ref("");
const showPlayer = ref(false);
const currentVideo = ref(null);
const infiniteScrollTrigger = ref(null);
let observer = null;

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
  const v = typeVideos.value.find((x) => x.id === videoId);
  if (v) { v.isLiked = isLiked; v.likes += isLiked ? 1 : -1; }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return;
  loadingMore.value = true;
  try {
    const response = await getVideosByTypeId(props.id, page.value, pageSize.value);
    if (!response || !response.data) throw new Error("Invalid response");

    let newVideos = [];
    let totalCount = 0;
    const d = response.data;
    if (Array.isArray(d)) { newVideos = d; totalCount = d.length; }
    else if (d.items) { newVideos = d.items; totalCount = d.total || d.items.length; }
    else if (d.list) { newVideos = d.list; totalCount = d.total || d.list.length; }
    else if (d.records) { newVideos = d.records; totalCount = d.total || d.records.length; }

    const processed = newVideos.map((v) => ({
      ...v, isLiked: v.isLiked || false, isFavourited: v.isFavourited || false,
      likes: v.likes || 0, views: v.views || 0, favourites: v.favourites || 0,
      shares: v.shares || 0, comments: v.comments || 0,
    }));

    if (processed.length > 0 && !typeName.value) {
      typeName.value = processed[0].type || "分类视频";
    }

    if (page.value === 1) typeVideos.value = processed;
    else typeVideos.value.push(...processed);

    if (processed.length < pageSize.value || typeVideos.value.length >= totalCount) hasMore.value = false;
    else page.value++;
  } catch (error) {
    console.error("加载分类视频失败", error);
    errorMessage.value = "加载视频失败，请稍后重试";
    errorSnackbar.value = true;
    hasMore.value = false;
  } finally {
    loadingMore.value = false;
    initialLoading.value = false;
  }
}

async function refreshVideos() {
  page.value = 1; hasMore.value = true; typeVideos.value = []; initialLoading.value = true;
  await loadMore();
}

function setupInfiniteScroll() {
  if (!infiniteScrollTrigger.value) return;
  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !loadingMore.value && hasMore.value && !initialLoading.value) loadMore();
  }, { rootMargin: "100px" });
  observer.observe(infiniteScrollTrigger.value);
}

// Watch for route param changes (switching categories)
watch(() => props.id, () => {
  typeName.value = "";
  refreshVideos();
});

onMounted(async () => {
  await loadMore();
  nextTick(() => setupInfiniteScroll());
});
onUnmounted(() => { if (observer) observer.disconnect(); });
</script>

<style scoped>
.gradient-text { background: linear-gradient(45deg, #ff6b6b, #4ecdc4); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
.video-grid { margin: 0 -8px; }
.video-col { padding: 8px; }
</style>
