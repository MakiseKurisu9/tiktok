<template>
  <v-card
    class="video-card mx-auto"
    elevation="0"
    @click="$emit('play', video)"
    :ripple="false"
  >
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
            <v-icon size="48" color="grey-lighten-2">mdi-image-broken</v-icon>
          </div>
        </template>
      </v-img>
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
      <div v-if="video.duration" class="duration-badge">
        <v-chip size="small" color="rgba(0,0,0,0.7)" text-color="white">
          {{ formatDuration(video.duration) }}
        </v-chip>
      </div>
      <div class="views-badge">
        <v-chip size="small" color="rgba(0,0,0,0.7)" text-color="white">
          <v-icon size="16" class="mr-1">mdi-eye</v-icon>
          {{ formatCount(video.views) }}
        </v-chip>
      </div>
    </div>

    <v-card-text class="px-4 py-3">
      <h3 class="video-title text-subtitle-1 font-weight-medium">
        {{ video.title || "Untitled Video" }}
      </h3>

      <!-- Creator info below title -->
      <div class="d-flex align-center mb-1 creator-row" mt-0 @click.stop>
        <v-avatar size="28" class="mr-2">
          <v-img
            :src="creatorAvatar || defaultAvatar"
            :alt="video.publisherName"
          >
            <template v-slot:error>
              <v-icon size="20" color="grey-lighten-2"
                >mdi-account-circle</v-icon
              >
            </template>
          </v-img>
        </v-avatar>
        <span
          class="text-caption font-weight-medium text-medium-emphasis creator-name"
        >
          {{ video.publisherName || "未知创作者" }}
        </span>
      </div>

      <div class="d-flex align-center text-caption text-medium-emphasis mb-2">
        <v-icon size="14" class="mr-1">mdi-clock-outline</v-icon>
        {{ formatTime(video.createTime) }}
      </div>
      <div v-if="video.type" class="mb-3">
        <v-chip size="small" variant="outlined" color="primary">{{
          video.type
        }}</v-chip>
      </div>
      <div class="d-flex align-center justify-space-between">
        <div
          class="d-flex align-center gap-3 text-caption text-medium-emphasis"
        >
          <span class="d-flex align-center">
            <v-icon size="14" class="mr-1">mdi-heart</v-icon
            >{{ formatCount(video.likes) }}
          </span>
          <span class="d-flex align-center">
            <v-icon size="14" class="mr-1">mdi-star</v-icon
            >{{ formatCount(video.favourites) }}
          </span>
          <span class="d-flex align-center">
            <v-icon size="14" class="mr-1">mdi-share</v-icon
            >{{ formatCount(video.shares) }}
          </span>
        </div>
        <v-btn
          icon
          size="small"
          variant="text"
          @click.stop="$emit('toggleLike', video)"
        >
          <v-icon size="18" :color="video.isLiked ? 'red' : 'grey'">
            {{ video.isLiked ? "mdi-heart" : "mdi-heart-outline" }}
          </v-icon>
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup>
import { ref, watch } from "vue";
import { getUserInfoByUserId } from "@/api/customer";

const props = defineProps({ video: { type: Object, required: true } });
defineEmits(["play", "toggleLike"]);

const defaultCover = new URL("@/assets/video-cover.png", import.meta.url).href;
const defaultAvatar = new URL("@/assets/default-avatar.png", import.meta.url)
  .href;

const creatorAvatar = ref(null);

watch(
  () => props.video?.publisherId,
  async (id) => {
    if (!id) return;
    try {
      const res = await getUserInfoByUserId(id);
      creatorAvatar.value = res.data?.avatarSource || null;
    } catch {
      creatorAvatar.value = null;
    }
  },
  { immediate: true },
);

function formatTime(time) {
  if (!time) return "未知时间";
  const diff = Date.now() - new Date(time).getTime();
  const days = Math.floor(diff / 86400000);
  const hrs = Math.floor(diff / 3600000);
  const mins = Math.floor(diff / 60000);
  if (days > 0) return `${days}天前`;
  if (hrs > 0) return `${hrs}小时前`;
  if (mins > 0) return `${mins}分钟前`;
  return "刚刚";
}

function formatDuration(s) {
  if (!s || s <= 0) return "00:00";
  return `${Math.floor(s / 60)
    .toString()
    .padStart(2, "0")}:${Math.floor(s % 60)
    .toString()
    .padStart(2, "0")}`;
}

function formatCount(c) {
  if (!c || c === 0) return "0";
  if (c >= 10000) return `${(c / 10000).toFixed(1)}万`;
  if (c >= 1000) return `${(c / 1000).toFixed(1)}k`;
  return c.toString();
}
</script>

<style scoped>
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
  position: relative;
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
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  height: 2.8em;
}
.gap-3 {
  gap: 12px;
}
.creator-row {
  cursor: default;
}
.creator-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
