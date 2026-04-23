<template>
  <v-app>
    <!-- App Bar -->
    <v-app-bar :elevation="0" color="surface" height="64" class="app-bar">
      <v-app-bar-nav-icon
        v-if="mobile"
        @click="drawer = !drawer"
        class="mr-2"
      />

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

      <div class="d-flex align-center">
        <v-btn
          v-if="mobile"
          icon
          variant="text"
          @click="openSearch"
          class="mr-2"
        >
          <v-icon>mdi-magnify</v-icon>
        </v-btn>

        <v-btn variant="text" class="mr-2" @click="showUploadDialog = true">
          <v-icon class="mr-1">mdi-plus</v-icon>
          <span v-if="!mobile">上传</span>
        </v-btn>

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

        <Auth />
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
      <div class="user-section pa-4">
        <div class="d-flex align-center">
          <v-avatar size="48" class="mr-3">
            <v-img :src="userAvatar" alt="用户头像" />
          </v-avatar>
          <div>
            <h3
              class="text-subtitle-1 font-weight-medium text-truncate"
              style="max-width: 130px"
            >
              {{ userName || "游客" }}
            </h3>
            <p class="text-caption text-medium-emphasis">欢迎回来</p>
          </div>
        </div>
      </div>

      <v-divider />

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
            <v-chip size="small" :color="item.badge.color" variant="flat">{{
              item.badge.text
            }}</v-chip>
          </template>
        </v-list-item>
      </v-list>

      <v-divider class="my-2" />

      <v-list nav>
        <v-list-subheader class="text-medium-emphasis font-weight-medium"
          >视频分类</v-list-subheader
        >
        <v-list-item
          v-for="type in videoTypes"
          :key="type.id"
          :to="`/videoType/${type.id}`"
          :title="type.name"
          :prepend-icon="type.icon || 'mdi-folder-outline'"
          rounded="xl"
          class="nav-item"
        />
      </v-list>

      <template v-slot:append>
        <div class="pa-4">
          <v-divider class="mb-4" />
          <div class="text-center">
            <p class="text-caption text-medium-emphasis mb-2">
              © 2024 TikTok Clone
            </p>
          </div>
        </div>
      </template>
    </v-navigation-drawer>

    <!-- Main content -->
    <v-main class="main-content">
      <div class="content-wrapper">
        <router-view v-slot="{ Component, route: r }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="r.path" />
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
          <v-btn icon @click="showSearch = false"
            ><v-icon>mdi-close</v-icon></v-btn
          >
        </v-toolbar>

        <v-divider />

        <v-card-text class="pa-0">
          <!-- Search History from API -->
          <div v-if="searchHistory.length > 0" class="pa-4">
            <div class="d-flex align-center justify-space-between mb-3">
              <h3 class="text-subtitle-2 font-weight-medium">搜索历史</h3>
              <v-btn variant="text" size="small" @click="clearAllSearchHistory"
                >清除</v-btn
              >
            </div>
            <v-chip-group column>
              <v-chip
                v-for="term in searchHistory"
                :key="term"
                variant="outlined"
                size="small"
                closable
                @click="
                  searchQuery = term;
                  performSearch();
                "
                @click:close.stop="deleteOneHistory(term)"
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
            <HotList elevation="0" @select="onHotSelect" />
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- Upload Dialog -->
    <v-dialog v-model="showUploadDialog" max-width="600" persistent>
      <v-card>
        <v-card-title>上传视频</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="uploadForm.title"
            label="视频标题"
            variant="outlined"
            class="mb-3"
          />
          <v-textarea
            v-model="uploadForm.description"
            label="视频简介"
            variant="outlined"
            rows="3"
            class="mb-3"
          />
          <v-text-field
            v-model="uploadForm.type"
            label="视频分类 (如：搞笑,音乐)"
            variant="outlined"
            class="mb-3"
          />
          <v-text-field
            v-model="uploadForm.imgSource"
            label="封面图 URL（可选）"
            variant="outlined"
            class="mb-3"
          />
          <v-file-input
            v-model="uploadFile"
            label="选择视频文件"
            accept="video/*"
            prepend-icon="mdi-video"
            variant="outlined"
            :loading="uploadingFile"
          />
          <v-progress-linear
            v-if="uploadingFile"
            indeterminate
            color="primary"
            class="mt-2"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            @click="showUploadDialog = false"
            :disabled="uploadingFile || uploadSubmitting"
            >取消</v-btn
          >
          <v-btn
            color="primary"
            @click="submitUpload"
            :loading="uploadSubmitting"
            :disabled="uploadingFile"
            >发布</v-btn
          >
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
            @click="notification.read = true"
          >
            <v-list-item-title class="text-wrap">{{
              notification.title
            }}</v-list-item-title>
            <v-list-item-subtitle class="text-wrap">{{
              notification.message
            }}</v-list-item-subtitle>
          </v-list-item>
          <div v-if="notifications.length === 0" class="text-center pa-8">
            <v-icon size="48" color="grey-lighten-2" class="mb-2"
              >mdi-bell-outline</v-icon
            >
            <p class="text-body-2 text-medium-emphasis">暂无通知</p>
          </div>
        </v-list>
      </v-card>
    </v-menu>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" :timeout="3000" color="success">{{
      snackMsg
    }}</v-snackbar>
  </v-app>
</template>

<script setup>
import { ref, computed, onMounted, watch, reactive } from "vue";
import { useDisplay } from "vuetify";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/pinia/user";
import { getAllTypes, getSearchVideoHistory, deleteSearchHistory } from "@/api";
import { addOrUpdateVideo } from "@/api/video";
import request from "@/utils/request";
import Auth from "@/components/Auth.vue";
import HotList from "@/components/HotList.vue";

const route = useRoute();
const router = useRouter();
const { mobile } = useDisplay();

const drawer = ref(!mobile.value);
const showSearch = ref(false);
const showUploadDialog = ref(false);
const showNotifications = ref(false);
const searchQuery = ref("");
const searchInputRef = ref(null);
const snackbar = ref(false);
const snackMsg = ref("");

// User data
const userStore = useUserStore();
const userName = computed(() => userStore.nickname || "游客");
const defaultAvatar = new URL("@/assets/default-avatar.png", import.meta.url)
  .href;
const userAvatar = computed(() => userStore.avatarSource || defaultAvatar); // ← add this

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
  { title: "订阅管理", icon: "mdi-cog", to: "/subscribe", value: "subscribe" },
]);

// Video types from API
const videoTypes = ref([]);

// Search history from API
const searchHistory = ref([]);

// Notifications (local)
const notifications = ref([]);
const unreadNotifications = computed(
  () => notifications.value.filter((n) => !n.read).length,
);

// Upload form
const uploadForm = ref({ title: "", description: "", type: "", imgSource: "" });
const uploadFile = ref(null);
const uploadingFile = ref(false);
const uploadSubmitting = ref(false);

// Methods
function isActiveRoute(to) {
  return route.path === to || route.path.startsWith(to + "/");
}

function openSearch() {
  showSearch.value = true;
  loadSearchHistory();
  setTimeout(() => {
    if (searchInputRef.value) searchInputRef.value.focus();
  }, 100);
}

function performSearch() {
  if (!searchQuery.value || !searchQuery.value.trim()) return;
  router.push(`/search?q=${encodeURIComponent(searchQuery.value)}`);
  showSearch.value = false;
}

function clearSearch() {
  searchQuery.value = "";
}

function onHotSelect(item) {
  searchQuery.value = item.title || item.videoTitle || "";
  performSearch();
}

async function loadSearchHistory() {
  try {
    const res = await getSearchVideoHistory();
    const data = res.data;
    if (Array.isArray(data)) {
      searchHistory.value = data;
    } else {
      searchHistory.value = [];
    }
  } catch (e) {
    console.error("Failed to load search history:", e);
  }
}

async function deleteOneHistory(term) {
  try {
    await deleteSearchHistory(term);
    searchHistory.value = searchHistory.value.filter((t) => t !== term);
  } catch (e) {
    console.error(e);
  }
}

async function clearAllSearchHistory() {
  for (const term of [...searchHistory.value]) {
    try {
      await deleteSearchHistory(term);
    } catch (e) {
      /* skip */
    }
  }
  searchHistory.value = [];
}

function markAllAsRead() {
  notifications.value.forEach((n) => (n.read = true));
}

async function submitUpload() {
  if (!uploadForm.value.title) {
    alert("请填写视频标题");
    return;
  }

  // v-file-input in Vuetify 3 binds a single File object (or null)
  const file =
    uploadFile.value instanceof File
      ? uploadFile.value
      : Array.isArray(uploadFile.value) && uploadFile.value.length > 0
        ? uploadFile.value[0]
        : null;

  if (!file) {
    alert("请选择视频文件");
    return;
  }

  const videoCategory =
    uploadForm.value.type?.split(",")[0]?.trim() || "general";

  uploadingFile.value = true;
  let videoUrl = "";
  try {
    const fd = new FormData();
    fd.append("file", file);
    fd.append("type", "video");
    fd.append("category", videoCategory);

    const uploadRes = await request.post("/upload", fd, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    videoUrl = uploadRes?.data?.data;
  } catch (e) {
    alert("视频上传失败");
    uploadingFile.value = false;
    return;
  } finally {
    uploadingFile.value = false;
  }

  // ... rest of submit logic

  uploadSubmitting.value = true;
  try {
    await addOrUpdateVideo({
      title: uploadForm.value.title,
      description: uploadForm.value.description,
      source: videoUrl,
      imgSource: uploadForm.value.imgSource,
      type: uploadForm.value.type,
    });
    snackMsg.value = "发布成功";
    snackbar.value = true;
    showUploadDialog.value = false;
    uploadForm.value = { title: "", description: "", type: "", imgSource: "" };
    uploadFile.value = null;
  } catch (e) {
    console.log("upload failure occur");
    console.log(e);

    alert("发布失败");
  } finally {
    uploadSubmitting.value = false;
  }
}

watch(mobile, (v) => {
  drawer.value = !v;
});

onMounted(async () => {
  try {
    const res = await getAllTypes();
    videoTypes.value = res.data || [];
  } catch (e) {
    console.error(e);
  }
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
.notification-item {
  border-bottom: 1px solid rgba(var(--v-border-color), 0.08);
  padding: 12px 16px;
}
.notification-unread {
  background: rgba(var(--v-theme-primary), 0.04);
  border-left: 3px solid rgb(var(--v-theme-primary));
}
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
</style>
