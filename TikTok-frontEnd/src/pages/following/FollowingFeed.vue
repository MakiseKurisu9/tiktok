<template>
  <v-container class="px-4 py-6">
    <div class="mb-8">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">关注动态</h1>
      <p class="text-body-1 text-medium-emphasis">
        查看你关注的人发布的最新视频
      </p>
    </div>

    <div v-if="loading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
      <p class="text-body-2 text-medium-emphasis mt-4">正在加载关注视频...</p>
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
      <v-icon size="64" color="grey-lighten-2">mdi-heart-outline</v-icon>
      <p class="text-h6 text-medium-emphasis mt-4">暂无关注视频</p>
      <p class="text-body-2 text-medium-emphasis">去关注一些创作者吧</p>
    </div>

    <div v-if="hasMore" class="text-center mt-4">
      <v-btn variant="outlined" @click="loadMore" :loading="loadingMore"
        >加载更多</v-btn
      >
    </div>

    <VideoPlayerDialog
      v-model="showPlayer"
      :video="currentVideo"
      @viewed="onViewed"
      @favourited="onFavourited"
    />
  </v-container>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { feedPush, starVideo, isVideoLiked } from "@/api/video";
import VideoCard from "@/components/VideoCard.vue";
import VideoPlayerDialog from "@/components/VideoPlayerDialog.vue";

const videos = ref([]);
const loading = ref(true);
const loadingMore = ref(false);
const hasMore = ref(true);
const lastId = ref(null);
const showPlayer = ref(false);
const currentVideo = ref(null);

function currentTimestampId() {
  return BigInt(Date.now()) << BigInt(22);
}

async function loadData(initial = false) {
  if (initial) {
    loading.value = true;
    lastId.value = currentTimestampId().toString();
  } else {
    loadingMore.value = true;
  }
  try {
    const res = await feedPush(lastId.value);
    const data = res.data;
    let records = [];
    let minTime = null;
    if (data && data.list) {
      records = data.list;
      minTime = data.minTime;
    } else if (Array.isArray(data)) {
      records = data;
    }
    // FIX: don't override real values with 0 — only set isLiked default
    const processed = records.map((v) => ({
      ...v,
      isLiked: v.isLiked || false,
    }));
    // ← Add this block right here
    const likeChecks = await Promise.all(
      processed.map((v) =>
        isVideoLiked(v.id)
          .then((res) => ({ id: v.id, isLiked: res.data }))
          .catch(() => ({ id: v.id, isLiked: false })),
      ),
    );
    const likedMap = Object.fromEntries(
      likeChecks.map((x) => [x.id, x.isLiked]),
    );
    processed.forEach((v) => {
      v.isLiked = likedMap[v.id] ?? false;
    });
    if (initial) videos.value = processed;
    else videos.value.push(...processed);
    if (minTime) lastId.value = minTime.toString();
    hasMore.value = processed.length >= 5;
  } catch (e) {
    console.error(e);
    hasMore.value = false;
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

function loadMore() {
  loadData(false);
}

// FIX: pass reference directly, no spread — so dialog mutations reflect on card
function openPlayer(video) {
  currentVideo.value = video;
  showPlayer.value = true;
}

async function toggleLike(video) {
  try {
    await starVideo(video.id);
    video.isLiked = !video.isLiked;
    video.likes = (video.likes || 0) + (video.isLiked ? 1 : -1);
  } catch (e) {
    console.error(e);
  }
}

// FIX: handle viewed event from dialog to update views count on card
function onViewed(videoId) {
  const v = videos.value.find((x) => x.id === videoId);
  if (v) v.views = (v.views || 0) + 1;
}

// FIX: handle favourited event from dialog to update favourites count on card
function onFavourited(videoId) {
  const v = videos.value.find((x) => x.id === videoId);
  if (v) v.favourites = (v.favourites || 0) + 1;
}

onMounted(() => loadData(true));
</script>

<style scoped>
.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
