<template>
  <v-container class="px-4 py-6">
    <div class="mb-6">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">搜索结果</h1>
      <p class="text-body-1 text-medium-emphasis">
        "{{ route.query.q }}" 的搜索结果
      </p>
    </div>

    <div v-if="loading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
      <p class="text-body-2 text-medium-emphasis mt-4">正在搜索...</p>
    </div>

    <v-row v-else-if="videos.length > 0" class="video-grid">
      <v-col
        v-for="video in videos"
        :key="video.id"
        cols="12"
        sm="6"
        md="4"
        lg="3"
      >
        <VideoCard
          :video="video"
          @play="openPlayer"
          @toggle-like="toggleLike"
        />
      </v-col>
    </v-row>

    <div v-else class="text-center py-16">
      <v-icon size="64" color="grey-lighten-2">mdi-magnify</v-icon>
      <p class="text-h6 text-medium-emphasis mt-4">未找到相关视频</p>
      <p class="text-body-2 text-medium-emphasis">试试其他关键词吧</p>
    </div>

    <div v-if="hasMore" class="text-center mt-4">
      <v-btn variant="outlined" @click="loadMore" :loading="loadingMore"
        >加载更多</v-btn
      >
    </div>

    <VideoPlayerDialog
      v-model="showPlayer"
      :video="currentVideo"
      @liked="onLiked"
    />
  </v-container>
</template>

<script setup>
import { ref, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import { searchVideo } from "@/api";
import { starVideo } from "@/api/video";
import VideoCard from "@/components/VideoCard.vue";
import VideoPlayerDialog from "@/components/VideoPlayerDialog.vue";

const route = useRoute();
const videos = ref([]);
const loading = ref(false);
const loadingMore = ref(false);
const page = ref(1);
const hasMore = ref(false);
const showPlayer = ref(false);
const currentVideo = ref(null);

async function search(reset = true) {
  const q = route.query.q;
  if (!q) return;
  if (reset) {
    page.value = 1;
    videos.value = [];
    loading.value = true;
  } else {
    loadingMore.value = true;
  }
  try {
    const res = await searchVideo({
      page: page.value,
      limit: 12,
      searchName: q,
    });
    const data = res.data;
    const records =
      data?.records ||
      data?.list ||
      data?.items ||
      (Array.isArray(data) ? data : []);
    const processed = records.map((v) => ({
      ...v,
      isLiked: false,
      likes: v.likes || 0,
      views: v.views || 0,
      favourites: v.favourites || 0,
      shares: v.shares || 0,
    }));
    if (reset) videos.value = processed;
    else videos.value.push(...processed);
    hasMore.value = processed.length >= 12;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

function loadMore() {
  page.value++;
  search(false);
}

function openPlayer(video) {
  currentVideo.value = { ...video };
  showPlayer.value = true;
}

async function toggleLike(video) {
  try {
    await starVideo(video.id);
    video.isLiked = !video.isLiked;
    video.likes += video.isLiked ? 1 : -1;
  } catch (e) {
    console.error(e);
  }
}

function onLiked(videoId, isLiked) {
  const v = videos.value.find((x) => x.id === videoId);
  if (v) {
    v.isLiked = isLiked;
    v.likes += isLiked ? 1 : -1;
  }
}

watch(
  () => route.query.q,
  () => search(),
);
onMounted(() => search());
</script>

<style scoped>
.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
