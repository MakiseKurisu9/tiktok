<template>
  <v-app>
    <!-- App Bar -->
    <v-app-bar :elevation="0" color="surface" height="64" class="app-bar">
      <!-- Mobile menu toggle -->
      <v-app-bar-nav-icon
        v-if="mobile"
        @click="drawer = !drawer"
        class="mr-2"
      />

      <!-- Logo/Brand -->
      <div class="d-flex align-center">
        <v-icon size="32" color="primary" class="mr-2">mdi-video</v-icon>
        <h1 class="text-h5 font-weight-bold brand-text">TikTok</h1>
      </div>

      <v-spacer />

      <!-- Desktop Search -->
      <div v-if="!mobile" class="search-container mx-4">
        <v-text-field
          v-model="searchQuery"
          density="comfortable"
          variant="outlined"
          placeholder="搜索视频..."
          prepend-inner-icon="mdi-magnify"
          hide-details
          single-line
          clearable
          rounded
          class="search-field"
          @click:prepend-inner="performSearch"
          @keyup.enter="performSearch"
          @click="openSearch"
        />
      </div>

      <v-spacer />

      <!-- Action buttons -->
      <div class="d-flex align-center">
        <!-- Mobile search button -->
        <v-btn
          v-if="mobile"
          icon
          variant="text"
          @click="openSearch"
          class="mr-2"
        >
          <v-icon>mdi-magnify</v-icon>
        </v-btn>

        <!-- Upload button -->
        <v-btn variant="text" class="mr-2" @click="showUploadDialog = true">
          <v-icon class="mr-1">mdi-plus</v-icon>
          <span v-if="!mobile">上传</span>
        </v-btn>

        <!-- Notifications -->
        <v-btn
          icon
          variant="text"
          class="mr-2"
          @click="showNotifications = !showNotifications"
        >
          <v-badge
            v-if="unreadNotifications > 0"
            :content="unreadNotifications"
            color="error"
            floating
          >
            <v-icon>mdi-bell-outline</v-icon>
          </v-badge>
          <v-icon v-else>mdi-bell-outline</v-icon>
        </v-btn>

        <!-- User menu -->
        <auth />
      </div>
    </v-app-bar>

    <!-- Navigation Drawer -->
    <v-navigation-drawer
      v-model="drawer"
      :permanent="!mobile"
      :temporary="mobile"
      width="280"
      class="navigation-drawer"
    >
      <!-- User profile section -->
      <div class="user-section pa-4">
        <div class="d-flex align-center">
          <v-avatar size="48" class="mr-3">
            <v-img :src="userAvatar" alt="用户头像" />
          </v-avatar>
          <div>
            <h3 class="text-subtitle-1 font-weight-medium">{{ userName }}</h3>
            <p class="text-caption text-medium-emphasis">欢迎回来</p>
          </div>
        </div>
      </div>

      <v-divider />

      <!-- Main navigation -->
      <v-list nav class="py-2">
        <v-list-item
          v-for="item in mainNavItems"
          :key="item.title"
          :to="item.to"
          :prepend-icon="item.icon"
          :title="item.title"
          :value="item.value"
          class="nav-item"
          rounded="xl"
          :class="{ 'nav-item-active': isActiveRoute(item.to) }"
        >
          <template v-if="item.badge" v-slot:append>
            <v-chip size="small" :color="item.badge.color" variant="flat">
              {{ item.badge.text }}
            </v-chip>
          </template>
        </v-list-item>
      </v-list>

      <v-divider class="my-2" />

      <!-- Video categories -->
      <v-list nav>
        <v-list-subheader class="text-medium-emphasis font-weight-medium">
          视频分类
        </v-list-subheader>

        <v-list-item
          v-for="type in videoTypes"
          :key="type.id"
          :to="`/videoType/${type.id}`"
          :title="type.name"
          :prepend-icon="type.icon || 'mdi-folder-outline'"
          rounded="xl"
          class="nav-item"
        >
          <template v-if="type.count" v-slot:append>
            <span class="text-caption text-medium-emphasis">{{
              type.count
            }}</span>
          </template>
        </v-list-item>
      </v-list>

      <!-- Footer section -->
      <template v-slot:append>
        <div class="pa-4">
          <v-divider class="mb-4" />
          <div class="text-center">
            <p class="text-caption text-medium-emphasis mb-2">
              © 2024 TikTok Clone
            </p>
            <div class="d-flex justify-center">
              <v-btn
                v-for="social in socialLinks"
                :key="social.name"
                :icon="social.icon"
                :href="social.url"
                variant="text"
                size="small"
                class="mx-1"
                target="_blank"
              />
            </div>
          </div>
        </div>
      </template>
    </v-navigation-drawer>

    <!-- Main content -->
    <v-main class="main-content">
      <div class="content-wrapper">
        <router-view v-slot="{ Component, route }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>
    </v-main>

    <!-- Search Dialog -->
    <v-dialog
      v-model="showSearch"
      max-width="600"
      :fullscreen="mobile"
      transition="dialog-bottom-transition"
    >
      <v-card class="search-dialog">
        <v-toolbar color="surface" flat>
          <v-text-field
            ref="searchInputRef"
            v-model="searchQuery"
            autofocus
            density="comfortable"
            variant="plain"
            placeholder="搜索视频、用户或话题..."
            prepend-inner-icon="mdi-magnify"
            hide-details
            single-line
            clearable
            @keyup.enter="performSearch"
            @click:clear="clearSearch"
            class="search-dialog-input"
          />
          <v-btn icon @click="showSearch = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-divider />

        <v-card-text class="pa-0">
          <!-- Search History -->
          <div v-if="searchHistory.length > 0" class="pa-4">
            <div class="d-flex align-center justify-space-between mb-3">
              <h3 class="text-subtitle-2 font-weight-medium">搜索历史</h3>
              <v-btn variant="text" size="small" @click="clearSearchHistory">
                清除
              </v-btn>
            </div>
            <v-chip-group column>
              <v-chip
                v-for="term in searchHistory"
                :key="term"
                variant="outlined"
                size="small"
                @click="
                  searchQuery = term;
                  performSearch();
                "
                class="mr-2 mb-2"
              >
                {{ term }}
              </v-chip>
            </v-chip-group>
          </div>

          <v-divider v-if="searchHistory.length > 0" />

          <!-- Hot searches -->
          <div class="pa-4">
            <h3 class="text-subtitle-2 font-weight-medium mb-3">
              热门搜索排行榜
            </h3>
            <hotList elevation="0" />
          </div>

          <!-- Search Suggestions -->
          <div v-if="searchQuery && searchSuggestions.length > 0" class="pa-0">
            <v-divider />
            <v-list>
              <v-list-item
                v-for="(suggestion, index) in searchSuggestions"
                :key="index"
                @click="
                  searchQuery = suggestion;
                  performSearch();
                "
                class="suggestion-item"
              >
                <template v-slot:prepend>
                  <v-icon size="20" class="mr-3">mdi-magnify</v-icon>
                </template>
                <v-list-item-title>{{ suggestion }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Upload Dialog -->
    <v-dialog v-model="showUploadDialog" max-width="500">
      <v-card>
        <v-card-title>上传视频</v-card-title>
        <v-card-text>
          <div class="text-center py-8">
            <v-icon size="64" color="primary" class="mb-4"
              >mdi-cloud-upload</v-icon
            >
            <p class="text-h6 mb-2">选择要上传的视频</p>
            <p class="text-body-2 text-medium-emphasis">
              支持 MP4, MOV, AVI 格式，最大 100MB
            </p>
            <v-btn color="primary" class="mt-4" @click="selectFile">
              选择文件
            </v-btn>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showUploadDialog = false">取消</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Notifications Menu -->
    <v-menu
      v-model="showNotifications"
      :close-on-content-click="false"
      location="bottom end"
      width="350"
    >
      <template v-slot:activator="{ props }">
        <div v-bind="props"></div>
      </template>

      <v-card>
        <v-toolbar color="surface" flat density="compact">
          <v-toolbar-title class="text-subtitle-1">通知</v-toolbar-title>
          <v-spacer />
          <v-btn
            icon="mdi-check-all"
            variant="text"
            size="small"
            @click="markAllAsRead"
          />
        </v-toolbar>

        <v-divider />

        <v-list class="pa-0" max-height="400" style="overflow-y: auto">
          <v-list-item
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ 'notification-unread': !notification.read }"
            @click="markAsRead(notification.id)"
          >
            <template v-slot:prepend>
              <v-avatar size="40" class="mr-3">
                <v-img :src="notification.avatar" />
              </v-avatar>
            </template>

            <v-list-item-title class="text-wrap">
              {{ notification.title }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-wrap">
              {{ notification.message }}
            </v-list-item-subtitle>

            <template v-slot:append>
              <div class="text-caption text-medium-emphasis">
                {{ formatTime(notification.time) }}
              </div>
            </template>
          </v-list-item>

          <div v-if="notifications.length === 0" class="text-center pa-8">
            <v-icon size="48" color="grey-lighten-2" class="mb-2">
              mdi-bell-outline
            </v-icon>
            <p class="text-body-2 text-medium-emphasis">暂无通知</p>
          </div>
        </v-list>
      </v-card>
    </v-menu>

    <!-- Loading overlay for page transitions -->
    <v-overlay v-model="pageLoading" class="align-center justify-center">
      <v-progress-circular indeterminate size="64" color="primary" />
    </v-overlay>
  </v-app>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useDisplay } from "vuetify";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/pinia/user";

const route = useRoute();
const router = useRouter();
// Composables
const { mobile, tablet } = useDisplay();

// Reactive data
const drawer = ref(!mobile.value);
const showSearch = ref(false);
const showUploadDialog = ref(false);
const showNotifications = ref(false);
const pageLoading = ref(false);
const searchQuery = ref("");
const searchInputRef = ref(null);

// User data
let userName = ref("");
const defaultAvatar = new URL("@/assets/Logo.png", import.meta.url).href;
const userAvatar = ref(defaultAvatar);

// Navigation items
const mainNavItems = ref([
  {
    title: "热门视频",
    icon: "mdi-fire",
    to: "/index/hotVideos",
    value: "hot",
    badge: { text: "HOT", color: "error" },
  },
  {
    title: "推荐视频",
    icon: "mdi-thumb-up",
    to: "/index/pushedVideos",
    value: "recommended",
  },
  {
    title: "关注视频",
    icon: "mdi-heart",
    to: "/following",
    value: "following",
  },
  {
    title: "个人中心",
    icon: "mdi-account",
    to: "/customer/profile",
    value: "profile",
  },
  {
    title: "我的视频",
    icon: "mdi-video-box",
    to: "/my-videos",
    value: "my-videos",
  },
]);

// Video types
let videoTypes = ref([
  { id: 1, name: "搞笑", tag: "mdi-emoticon-happy", count: 1234 },
  { id: 2, name: "音乐", tag: "mdi-music", count: 856 },
  { id: 3, name: "舞蹈", tag: "mdi-dance-ballroom", count: 672 },
  { id: 4, name: "美食", tag: "mdi-food", count: 543 },
  { id: 5, name: "旅行", tag: "mdi-airplane", count: 421 },
  { id: 6, name: "宠物", tag: "mdi-paw", count: 389 },
  { id: 7, name: "科技", tag: "mdi-laptop", count: 298 },
  { id: 8, name: "运动", tag: "mdi-dumbbell", count: 267 },
]);

import { getAllTypes } from "@/api";
const apiGetAllTypes = async () => {
  const res = await getAllTypes();
  videoTypes.value = res.data;
};

// Social links
const socialLinks = ref([
  { name: "GitHub", icon: "mdi-github", url: "https://github.com" },
  { name: "Twitter", icon: "mdi-twitter", url: "https://twitter.com" },
  { name: "Instagram", icon: "mdi-instagram", url: "https://instagram.com" },
]);

// Search functionality
const searchHistory = ref(["世界杯", "法拉利", "可爱宠物", "科技前沿"]);
const searchSuggestions = ref([]);

// Notifications
const notifications = ref([
  {
    id: 1,
    title: "新的点赞",
    message: "用户123点赞了你的视频",
    avatar: "/api/placeholder/40/40",
    time: new Date(Date.now() - 300000), // 5 minutes ago
    read: false,
  },
  {
    id: 2,
    title: "新的关注者",
    message: "用户456开始关注你",
    avatar: "/api/placeholder/40/40",
    time: new Date(Date.now() - 3600000), // 1 hour ago
    read: false,
  },
  {
    id: 3,
    title: "评论回复",
    message: "有人回复了你的评论",
    avatar: "/api/placeholder/40/40",
    time: new Date(Date.now() - 7200000), // 2 hours ago
    read: true,
  },
]);

// Computed properties
const unreadNotifications = computed(() => {
  return notifications.value.filter((n) => !n.read).length;
});

// Methods
function isActiveRoute(to) {
  return route.path === to || route.path.startsWith(to + "/");
}

function openSearch() {
  showSearch.value = true;
  // Focus search input after dialog opens
  setTimeout(() => {
    if (searchInputRef.value) {
      searchInputRef.value.focus();
    }
  }, 100);
}

function performSearch() {
  if (!searchQuery.value.trim()) return;

  // Add to search history
  if (!searchHistory.value.includes(searchQuery.value)) {
    searchHistory.value.unshift(searchQuery.value);
    // Keep only last 10 searches
    if (searchHistory.value.length > 10) {
      searchHistory.value.pop();
    }
  }

  // Navigate to search results
  router.push(`/search?q=${encodeURIComponent(searchQuery.value)}`);
  showSearch.value = false;
}

function clearSearch() {
  searchQuery.value = "";
  searchSuggestions.value = [];
}

function clearSearchHistory() {
  searchHistory.value = [];
}

function selectFile() {
  // Mock file selection
  const input = document.createElement("input");
  input.type = "file";
  input.accept = "video/*";
  input.onchange = (e) => {
    const file = e.target.files[0];
    if (file) {
      console.log("Selected file:", file.name);
      // Handle file upload logic here
      showUploadDialog.value = false;
    }
  };
  input.click();
}

function markAsRead(notificationId) {
  const notification = notifications.value.find((n) => n.id === notificationId);
  if (notification) {
    notification.read = true;
  }
}

function markAllAsRead() {
  notifications.value.forEach((n) => (n.read = true));
}

function formatTime(time) {
  const now = new Date();
  const diff = now - new Date(time);
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return "刚刚";
}

// Mock search suggestions
function updateSearchSuggestions(query) {
  if (!query) {
    searchSuggestions.value = [];
    return;
  }

  const suggestions = [
    "热门舞蹈挑战",
    "搞笑宠物视频",
    "美食制作教程",
    "旅行风景视频",
    "音乐翻唱作品",
    "健身运动指导",
    "科技产品测评",
    "手工制作教程",
  ].filter((s) => s.includes(query));

  searchSuggestions.value = suggestions.slice(0, 5);
}

// Watchers
watch(searchQuery, (newValue) => {
  updateSearchSuggestions(newValue);
});

watch(mobile, (newValue) => {
  drawer.value = !newValue;
});

// Navigation transition effects
watch(
  () => route.path,
  () => {
    pageLoading.value = true;
    setTimeout(() => {
      pageLoading.value = false;
    }, 300);
  }
);

// Lifecycle
onMounted(() => {
  // Initialize any required data
  const userStore = useUserStore();
  userName = userStore.nickname;
  console.log(userName.value);

  apiGetAllTypes();
  console.log("Layout component mounted");
});
</script>

<style scoped>
.app-bar {
  border-bottom: 1px solid rgba(var(--v-border-color), 0.12);
  backdrop-filter: blur(10px);
}

.brand-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.search-container {
  max-width: 400px;
  width: 100%;
}

.search-field {
  transition: all 0.3s ease;
}

.search-field:focus-within {
  transform: scale(1.02);
}

.navigation-drawer {
  border-right: 1px solid rgba(var(--v-border-color), 0.12);
}

.user-section {
  background: linear-gradient(
    135deg,
    rgba(var(--v-theme-primary), 0.1),
    rgba(var(--v-theme-secondary), 0.1)
  );
  border-radius: 12px;
  margin: 16px;
}

.nav-item {
  margin: 4px 12px;
  transition: all 0.2s ease;
}

.nav-item:hover {
  transform: translateX(4px);
}

.nav-item-active {
  background: rgba(var(--v-theme-primary), 0.12);
  color: rgb(var(--v-theme-primary));
}

.main-content {
  width: 1500px;
  padding-left: 20px;

  background: linear-gradient(
    135deg,
    rgba(var(--v-theme-surface), 1) 0%,
    rgba(var(--v-theme-background), 1) 100%
  );
  min-height: 100vh;
}

.content-wrapper {
  min-height: calc(100vh - 64px);
}

.search-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.search-dialog-input {
  padding-left: 16px;
}

.suggestion-item {
  transition: background-color 0.2s ease;
}

.suggestion-item:hover {
  background: rgba(var(--v-theme-primary), 0.08);
}

.notification-item {
  border-bottom: 1px solid rgba(var(--v-border-color), 0.08);
  padding: 12px 16px;
}

.notification-unread {
  background: rgba(var(--v-theme-primary), 0.04);
  border-left: 3px solid rgb(var(--v-theme-primary));
}

/* Page transitions */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Mobile optimizations */
@media (max-width: 600px) {
  .user-section {
    margin: 12px;
    padding: 16px;
  }

  .nav-item {
    margin: 2px 8px;
  }

  .search-dialog {
    border-radius: 0;
  }
}

/* Dark theme adjustments */
.v-theme--dark .app-bar {
  background: rgba(var(--v-theme-surface), 0.8) !important;
}

.v-theme--dark .navigation-drawer {
  background: rgba(var(--v-theme-surface), 0.95);
}

/* Scrollbar styling */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: rgba(var(--v-border-color), 0.3);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-border-color), 0.5);
}

/* Loading animations */
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.loading {
  animation: pulse 2s infinite;
}

/* Smooth hover effects */
.v-btn {
  transition: all 0.2s ease;
}

.v-btn:hover {
  transform: translateY(-1px);
}

.v-card {
  transition: all 0.3s ease;
}

/* Focus styles for accessibility */
.v-btn:focus-visible,
.v-list-item:focus-visible {
  outline: 2px solid rgb(var(--v-theme-primary));
  outline-offset: 2px;
}
</style>
