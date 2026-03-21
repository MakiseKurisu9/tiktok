<template>
  <v-container class="px-4 py-6">
    <!-- Header Section -->
    <div class="mb-8">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">热门视频</h1>
      <p class="text-body-1 text-medium-emphasis">发现最受欢迎的精彩内容</p>
    </div>

    <!-- Loading State for Initial Load -->
    <div v-if="initialLoading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
      <p class="text-body-2 text-medium-emphasis mt-4">正在加载热门视频...</p>
    </div>

    <!-- Video Grid -->
    <v-row v-else class="video-grid">
      <v-col
        v-for="video in hotVideos"
        :key="video.id"
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xl="3"
        class="video-col"
      >
        <v-card
          class="video-card mx-auto"
          elevation="0"
          @click="playVideo(video)"
          :ripple="false"
        >
          <!-- Video Thumbnail with Overlay -->
          <div class="thumbnail-container">
            <v-img
              :src="video.imgSource || defaultCover"
              height="200"
              cover
              class="thumbnail-image"
              :aspect-ratio="16 / 9"
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-progress-circular indeterminate color="primary" />
                </div>
              </template>
              <template v-slot:error>
                <div class="d-flex align-center justify-center fill-height">
                  <v-icon size="48" color="grey-lighten-2"
                    >mdi-image-broken</v-icon
                  >
                </div>
              </template>
            </v-img>

            <!-- Play Button Overlay -->
            <div class="play-overlay">
              <v-btn
                icon
                size="large"
                color="white"
                class="play-button"
                elevation="2"
              >
                <v-icon size="32">mdi-play</v-icon>
              </v-btn>
            </div>

            <!-- Duration Badge -->
            <div v-if="video.duration" class="duration-badge">
              <v-chip size="small" color="rgba(0,0,0,0.7)" text-color="white">
                {{ formatDuration(video.duration) }}
              </v-chip>
            </div>

            <!-- View Count Badge -->
            <div class="views-badge">
              <v-chip size="small" color="rgba(0,0,0,0.7)" text-color="white">
                <v-icon size="16" class="mr-1">mdi-eye</v-icon>
                {{ formatViews(video.views) }}
              </v-chip>
            </div>
          </div>

          <!-- Card Content -->
          <v-card-text class="px-4 py-3">
            <h3 class="video-title text-subtitle-1 font-weight-medium mb-2">
              {{ video.title || "Untitled Video" }}
            </h3>

            <div
              class="d-flex align-center text-caption text-medium-emphasis mb-2"
            >
              <v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>
              {{ formatTime(video.createTime) }}
            </div>

            <!-- Video Type Badge -->
            <div v-if="video.type" class="mb-3">
              <v-chip size="small" variant="outlined" color="primary">
                {{ formatVideoType(video.type) }}
              </v-chip>
            </div>

            <!-- Stats Row -->
            <div class="d-flex align-center justify-space-between">
              <div
                class="d-flex align-center gap-3 text-caption text-medium-emphasis"
              >
                <span class="d-flex align-center">
                  <v-icon size="14" class="mr-1">mdi-heart</v-icon>
                  {{ formatCount(video.likes) }}
                </span>
                <span class="d-flex align-center">
                  <v-icon size="14" class="mr-1">mdi-star</v-icon>
                  {{ formatCount(video.favourites) }}
                </span>
                <span class="d-flex align-center">
                  <v-icon size="14" class="mr-1">mdi-share</v-icon>
                  {{ formatCount(video.shares) }}
                </span>
              </div>

              <v-btn
                icon
                size="small"
                variant="text"
                @click.stop="toggleLike(video)"
              >
                <v-icon size="18" :color="video.isLiked ? 'red' : 'grey'">
                  {{ video.isLiked ? "mdi-heart" : "mdi-heart-outline" }}
                </v-icon>
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Loading State for More Content -->
    <div class="text-center my-8" v-if="loadingMore">
      <v-progress-circular indeterminate color="primary" size="40" width="3" />
      <p class="text-body-2 text-medium-emphasis mt-3">加载更多视频...</p>
    </div>

    <!-- Empty State -->
    <v-card
      v-if="!loadingMore && hotVideos.length === 0 && !initialLoading"
      class="text-center py-16 my-8"
      variant="outlined"
    >
      <v-icon size="64" color="grey-lighten-1" class="mb-4">
        mdi-video-off-outline
      </v-icon>
      <h3 class="text-h6 mb-2">暂无热门视频</h3>
      <p class="text-body-2 text-medium-emphasis mb-4">
        稍后再来看看，或许会有新的精彩内容
      </p>
      <v-btn color="primary" variant="outlined" @click="refreshVideos">
        刷新
      </v-btn>
    </v-card>

    <!-- End of Content Indicator -->
    <div v-if="!hasMore && hotVideos.length > 0" class="text-center py-8">
      <v-divider class="mb-4" />
      <p class="text-body-2 text-medium-emphasis">
        已加载全部内容 ({{ hotVideos.length }} 个视频)
      </p>
    </div>

    <!-- Error State -->
    <v-snackbar
      v-model="errorSnackbar"
      color="error"
      timeout="4000"
      location="top"
    >
      {{ errorMessage }}
      <template v-slot:actions>
        <v-btn variant="text" @click="errorSnackbar = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>

    <!-- Video Player Dialog -->
    <v-dialog v-model="dialog" max-width="1200" persistent class="video-dialog">
      <v-card class="video-player-card">
        <v-card-title
          class="d-flex align-center justify-space-between px-6 py-4"
        >
          <h3 class="text-h6">{{ currentVideo?.title || "Untitled Video" }}</h3>
          <v-btn icon variant="text" @click="closeVideo">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-divider />

        <div class="video-container">
          <video
            v-if="playingVideo"
            ref="videoPlayer"
            :src="playingVideo"
            controls
            autoplay
            preload="metadata"
            @loadstart="onVideoLoadStart"
            @canplay="onVideoCanPlay"
            @error="onVideoError"
            class="video-player"
          />

          <!-- Video Loading State -->
          <div
            v-if="videoLoading"
            class="video-loading-overlay d-flex align-center justify-center"
          >
            <div class="text-center">
              <v-progress-circular
                indeterminate
                color="white"
                size="48"
                width="4"
              />
              <p class="text-white mt-3">视频加载中...</p>
            </div>
          </div>

          <!-- Video Error State -->
          <div
            v-if="videoError"
            class="video-error-overlay d-flex align-center justify-center"
          >
            <div class="text-center">
              <v-icon size="64" color="white" class="mb-4"
                >mdi-alert-circle</v-icon
              >
              <p class="text-white mb-4">视频加载失败</p>
              <v-btn color="white" variant="outlined" @click="retryVideo">
                重试
              </v-btn>
            </div>
          </div>
        </div>

        <!-- Video Info -->
        <v-card-text class="px-6 py-4">
          <div class="d-flex align-center justify-space-between mb-3">
            <div class="d-flex align-center">
              <v-avatar size="32" class="mr-3">
                <v-img :src="defaultAvatar" />
              </v-avatar>
              <div>
                <p class="text-subtitle-2 mb-0">视频发布者</p>
                <p class="text-caption text-medium-emphasis">
                  {{ formatTime(currentVideo?.createTime) }}
                </p>
              </div>
            </div>

            <div class="d-flex align-center gap-2">
              <v-btn
                variant="text"
                size="small"
                @click="toggleLike(currentVideo)"
              >
                <v-icon
                  class="mr-1"
                  :color="currentVideo?.isLiked ? 'red' : ''"
                >
                  {{
                    currentVideo?.isLiked ? "mdi-heart" : "mdi-heart-outline"
                  }}
                </v-icon>
                {{ formatCount(currentVideo?.likes) }}
              </v-btn>

              <v-btn variant="text" size="small" @click="shareVideo">
                <v-icon class="mr-1">mdi-share-variant</v-icon>
                分享
              </v-btn>

              <v-btn
                variant="text"
                size="small"
                @click="toggleFavourite(currentVideo)"
              >
                <v-icon
                  class="mr-1"
                  :color="currentVideo?.isFavourited ? 'orange' : ''"
                >
                  {{
                    currentVideo?.isFavourited ? "mdi-star" : "mdi-star-outline"
                  }}
                </v-icon>
                收藏
              </v-btn>
            </div>
          </div>

          <!-- Video Stats -->
          <div
            class="d-flex align-center gap-4 mb-3 text-caption text-medium-emphasis"
          >
            <span class="d-flex align-center">
              <v-icon size="16" class="mr-1">mdi-eye</v-icon>
              {{ formatCount(currentVideo?.views) }} 观看
            </span>
            <span class="d-flex align-center">
              <v-icon size="16" class="mr-1">mdi-heart</v-icon>
              {{ formatCount(currentVideo?.likes) }} 点赞
            </span>
            <span class="d-flex align-center">
              <v-icon size="16" class="mr-1">mdi-star</v-icon>
              {{ formatCount(currentVideo?.favourites) }} 收藏
            </span>
            <span class="d-flex align-center">
              <v-icon size="16" class="mr-1">mdi-share</v-icon>
              {{ formatCount(currentVideo?.shares) }} 分享
            </span>
          </div>

          <p class="text-body-2 text-medium-emphasis">
            {{ currentVideo?.description || "暂无描述" }}
          </p>

          <!-- Video Type -->
          <div v-if="currentVideo?.type" class="mt-3">
            <v-chip size="small" variant="outlined" color="primary">
              {{ formatVideoType(currentVideo.type) }}
            </v-chip>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Infinite Scroll Trigger -->
    <div
      ref="infiniteScrollTrigger"
      class="infinite-scroll-trigger"
      style="height: 10px"
    />
  </v-container>
</template>

<script setup>
import { getHotVideo } from "@/api";
import { ref, onMounted, onUnmounted, nextTick, computed } from "vue";
import { useDisplay } from "vuetify";

// Responsive breakpoints
const { mobile, tablet } = useDisplay();

// Video data
const hotVideos = ref([]);
const page = ref(1);
const pageSize = ref(8);
const loadingMore = ref(false);
const initialLoading = ref(true);
const hasMore = ref(true);
const total = ref(0);

// Error handling
const errorSnackbar = ref(false);
const errorMessage = ref("");

// Assets
const defaultCover = new URL("@/assets/original.png", import.meta.url).href;
const defaultAvatar = new URL("@/assets/Logo.png", import.meta.url).href;

// Video player
const dialog = ref(false);
const playingVideo = ref("");
const currentVideo = ref(null);
const videoPlayer = ref(null);
const videoLoading = ref(false);
const videoError = ref(false);

// Infinite scroll
const infiniteScrollTrigger = ref(null);
let observer = null;

// Computed properties
const gridCols = computed(() => {
  if (mobile.value) return 1;
  if (tablet.value) return 2;
  return 6; // Default for larger screens
});

// Video playback functions
function playVideo(video) {
  if (!video.source) {
    showError("视频源不可用");
    return;
  }

  currentVideo.value = { ...video };
  playingVideo.value = video.source;
  dialog.value = true;
  videoLoading.value = true;
  videoError.value = false;
}

function closeVideo() {
  dialog.value = false;
  playingVideo.value = "";
  currentVideo.value = null;
  videoLoading.value = false;
  videoError.value = false;

  // Stop video playback
  if (videoPlayer.value) {
    videoPlayer.value.pause();
    videoPlayer.value.currentTime = 0;
  }
}

function onVideoLoadStart() {
  videoLoading.value = true;
  videoError.value = false;
}

function onVideoCanPlay() {
  videoLoading.value = false;
  videoError.value = false;
}

function onVideoError(event) {
  videoLoading.value = false;
  videoError.value = true;
  console.error("Video playback error:", event);
}

function retryVideo() {
  if (currentVideo.value?.source) {
    videoError.value = false;
    videoLoading.value = true;
    playingVideo.value = "";
    nextTick(() => {
      playingVideo.value = currentVideo.value.source;
    });
  }
}

// Utility functions
function formatTime(time) {
  if (!time) return "未知时间";

  const date = new Date(time);
  const now = new Date();
  const diff = now - date;

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return "刚刚";
}

function formatDuration(seconds) {
  if (!seconds || seconds <= 0) return "00:00";
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, "0")}:${secs
    .toString()
    .padStart(2, "0")}`;
}

function formatViews(views) {
  return formatCount(views);
}

function formatCount(count) {
  if (!count || count === 0) return "0";
  if (count >= 10000) return `${(count / 10000).toFixed(1)}万`;
  if (count >= 1000) return `${(count / 1000).toFixed(1)}k`;
  return count.toString();
}

function formatVideoType(type) {
  const typeMap = {
    football: "足球",
    car: "扒车",
    pet: "宠物",
    科技: "tech",
    动漫: "anime",
    歌曲: "song",
  };
  return typeMap[type] || type;
}

// Interaction functions
function toggleLike(video) {
  if (!video) return;

  const wasLiked = video.isLiked;
  video.isLiked = !wasLiked;
  video.likes = Math.max(0, (video.likes || 0) + (video.isLiked ? 1 : -1));

  // Here you would typically call an API to update the like status
  // updateVideoLike(video.id, video.isLiked);
}

function toggleFavourite(video) {
  if (!video) return;

  const wasFavourited = video.isFavourited;
  video.isFavourited = !wasFavourited;
  video.favourites = Math.max(
    0,
    (video.favourites || 0) + (video.isFavourited ? 1 : -1)
  );

  // Here you would typically call an API to update the favourite status
  // updateVideoFavourite(video.id, video.isFavourited);
}

function shareVideo() {
  if (navigator.share && currentVideo.value) {
    navigator
      .share({
        title: currentVideo.value.title,
        text: "看看这个精彩视频！",
        url: window.location.href,
      })
      .catch(console.error);
  } else {
    // Fallback: copy to clipboard
    navigator.clipboard
      .writeText(window.location.href)
      .then(() => {
        showError("链接已复制到剪贴板", "success");
      })
      .catch(() => {
        showError("分享失败");
      });
  }
}

// Error handling
function showError(message, type = "error") {
  errorMessage.value = message;
  errorSnackbar.value = true;

  if (type === "error") {
    console.error(message);
  }
}

// Data loading
async function loadMore() {
  if (loadingMore.value || !hasMore.value) return;

  loadingMore.value = true;

  try {
    console.log("当前页码:" + page.value + "当前limit:" + pageSize.value);
    const response = await getHotVideo(page.value, pageSize.value);

    console.log(response.data);

    if (!response || !response.data) {
      throw new Error("Invalid response from server");
    }

    // Handle different response structures
    let newVideos = [];
    let totalCount = 0;

    if (Array.isArray(response.data)) {
      // Direct array response
      newVideos = response.data;
      totalCount = response.total || response.data.length;
    } else if (response.data.items && Array.isArray(response.data.items)) {
      // Paginated response with items
      newVideos = response.data.items;
      totalCount = response.data.total || response.data.items.length;
    } else if (response.data.list && Array.isArray(response.data.list)) {
      // Alternative structure
      newVideos = response.data.list;
      totalCount = response.data.total || response.data.list.length;
    } else {
      throw new Error("Unexpected response structure");
    }

    // Process new videos
    const processedVideos = newVideos.map((video) => ({
      ...video,
      isLiked: video.isLiked || false,
      isFavourited: video.isFavourited || false,
      likes: video.likes || 0,
      views: video.views || 0,
      favourites: video.favourites || 0,
      shares: video.shares || 0,
      comments: video.comments || 0,
    }));

    if (page.value === 1) {
      hotVideos.value = processedVideos;
    } else {
      hotVideos.value = [...hotVideos.value, ...processedVideos];
    }

    // Check if there are more videos to load
    if (
      processedVideos.length < pageSize.value ||
      hotVideos.value.length >= totalCount
    ) {
      hasMore.value = false;
    } else {
      page.value++;
    }

    total.value = totalCount;
    console.log("本次获取视频数:", processedVideos.length);
    console.log("当前已加载:", hotVideos.value.length);
    console.log("总数:", totalCount);

    console.log(
      `Loaded ${processedVideos.length} videos, total: ${hotVideos.value.length}`
    );
    console.log(hotVideos.value);
  } catch (error) {
    console.error("加载热门视频失败", error);
    showError("加载视频失败，请稍后重试");
    hasMore.value = false;
  } finally {
    loadingMore.value = false;
    initialLoading.value = false;
  }
}

async function refreshVideos() {
  page.value = 1;
  hasMore.value = true;
  hotVideos.value = [];
  initialLoading.value = true;
  await loadMore();
}

// Intersection Observer for infinite scroll
function setupInfiniteScroll() {
  if (!infiniteScrollTrigger.value) return;

  observer = new IntersectionObserver(
    (entries) => {
      if (
        entries[0].isIntersecting &&
        !loadingMore.value &&
        hasMore.value &&
        !initialLoading.value
      ) {
        loadMore();
      }
    },
    {
      rootMargin: "100px",
    }
  );

  observer.observe(infiniteScrollTrigger.value);
}

// Lifecycle
onMounted(async () => {
  await loadMore();

  nextTick(() => {
    setupInfiniteScroll();
  });
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.video-grid {
  margin: 0 -8px;
}

.video-col {
  padding: 8px;
}

.video-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid rgba(var(--v-border-color), 0.12);
  height: 100%;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.thumbnail-container {
  overflow: hidden;
}

.thumbnail-image {
  transition: transform 0.3s ease;
}

.video-card:hover .thumbnail-image {
  transform: scale(1.05);
}

.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.video-card:hover .play-overlay {
  opacity: 1;
}

.play-button {
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
}

.duration-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

.views-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.video-title {
  display: -webkit-box;

  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  height: 2.8em;
}

.video-dialog {
  z-index: 2100;
}

.video-player-card {
  border-radius: 12px;
  overflow: hidden;
}

.video-container {
  position: relative;
  background: #000;
}

.video-player {
  width: 100%;
  height: auto;
  max-height: 70vh;
  display: block;
}

.video-loading-overlay,
.video-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 10;
  min-height: 300px;
}

.infinite-scroll-trigger {
  visibility: hidden;
}

/* Mobile optimizations */
@media (max-width: 600px) {
  .video-card {
    border-radius: 12px;
  }

  .thumbnail-image {
    height: 160px !important;
  }

  .video-title {
    height: 4.2em;
  }

  .video-player {
    max-height: 50vh;
  }
}

/* Smooth animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.video-card {
  animation: fadeInUp 0.6s ease forwards;
}

.video-card:nth-child(even) {
  animation-delay: 0.1s;
}

.video-card:nth-child(3n) {
  animation-delay: 0.2s;
}

/* Gap utility for better spacing */
.gap-2 {
  gap: 8px;
}

.gap-3 {
  gap: 12px;
}

.gap-4 {
  gap: 16px;
}
</style>
