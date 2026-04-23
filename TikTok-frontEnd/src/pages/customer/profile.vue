<template>
  <v-container fluid class="user-profile-container">
    <!-- Header Section -->
    <v-row class="mb-6">
      <v-col cols="12">
        <v-card class="user-info-card" elevation="4">
          <v-card-text class="pa-6">
            <v-row align="center">
              <v-col cols="auto">
                <v-avatar size="120" class="user-avatar">
                  <v-img :src="userInfo.avatarSource || defaultAvatar" />
                </v-avatar>
              </v-col>
              <v-col>
                <div class="user-details">
                  <h2 class="text-h4 font-weight-bold text-grey-darken-1 mb-2">
                    {{ userInfo.nickname || "未知用户" }}
                  </h2>
                  <p class="text-body-1 text-grey-darken-1 mb-3">
                    {{ userInfo.userDescription || "No description available" }}
                  </p>
                  <v-row class="stats-row">
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ followersCount }}</span>
                        <span class="stat-label">粉丝</span>
                      </div>
                    </v-col>
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ followingCount }}</span>
                        <span class="stat-label">关注</span>
                      </div>
                    </v-col>
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ favouritesCount }}</span>
                        <span class="stat-label">收藏夹</span>
                      </div>
                    </v-col>
                  </v-row>
                </div>
              </v-col>
              <v-col cols="auto">
                <v-btn
                  color="primary"
                  variant="outlined"
                  prepend-icon="mdi-pencil"
                  @click="openEditProfile"
                >
                  编辑资料
                </v-btn>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Tabs Section -->
    <v-row>
      <v-col cols="12">
        <v-card elevation="2">
          <v-tabs
            v-model="activeTab"
            bg-color="primary"
            color="white"
            align-tabs="center"
            show-arrows
          >
            <v-tab value="history"
              ><v-icon left>mdi-history</v-icon> 观看历史</v-tab
            >
            <v-tab value="followers"
              ><v-icon left>mdi-account-group</v-icon> 粉丝</v-tab
            >
            <v-tab value="following"
              ><v-icon left>mdi-account-plus</v-icon> 关注</v-tab
            >
            <v-tab value="favourites"
              ><v-icon left>mdi-heart</v-icon> 收藏夹</v-tab
            >
          </v-tabs>

          <v-card-text class="pa-6">
            <v-tabs-window v-model="activeTab">
              <!-- Watch History Tab -->
              <v-tabs-window-item value="history">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">观看历史</h3>
                  <v-row v-if="watchHistory.length > 0">
                    <v-col
                      v-for="video in watchHistory"
                      :key="video.id"
                      cols="12"
                      md="6"
                      lg="4"
                    >
                      <v-card
                        class="video-card"
                        hover
                        @click="openPlayer(video)"
                      >
                        <v-img
                          :src="video.imgSource || defaultCover"
                          height="200"
                          cover
                        >
                          <template v-slot:error>
                            <div
                              class="d-flex align-center justify-center fill-height"
                            >
                              <v-icon size="48" color="grey-lighten-2"
                                >mdi-image-broken</v-icon
                              >
                            </div>
                          </template>
                        </v-img>
                        <v-card-title class="text-subtitle-1">{{
                          video.title || "无标题"
                        }}</v-card-title>
                        <v-card-subtitle>
                          <v-icon size="14">mdi-eye</v-icon>
                          {{ video.views || 0 }}
                          <v-icon size="14" class="ml-2">mdi-heart</v-icon>
                          {{ video.likes || 0 }}
                        </v-card-subtitle>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div v-else class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-history</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">暂无观看记录</p>
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Followers Tab -->
              <v-tabs-window-item value="followers">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">粉丝 ({{ followersCount }})</h3>
                  <v-row v-if="followers.length > 0">
                    <v-col
                      v-for="follower in followers"
                      :key="follower.id"
                      cols="12"
                      sm="6"
                      md="4"
                      lg="3"
                    >
                      <v-card class="user-card" hover>
                        <v-card-text class="text-center pa-4">
                          <v-avatar size="80" class="mb-3">
                            <v-img
                              :src="follower.avatarSource || defaultAvatar"
                            />
                          </v-avatar>
                          <h4 class="text-h6 mb-1">
                            {{ follower.nickname || "用户" }}
                          </h4>
                          <p class="text-body-2 text-grey-darken-1">
                            {{ follower.userDescription || "暂无简介" }}
                          </p>
                        </v-card-text>
                        <v-card-actions class="justify-center">
                          <v-btn
                            :color="
                              followingSet.has(follower.id)
                                ? 'error'
                                : 'primary'
                            "
                            variant="outlined"
                            size="small"
                            @click="doFollow(follower.id)"
                          >
                            {{
                              followingSet.has(follower.id)
                                ? "取消关注"
                                : "回关"
                            }}
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div v-else class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-account-group</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">暂无粉丝</p>
                  </div>
                  <div v-if="followersHasMore" class="text-center mt-4">
                    <v-btn
                      variant="outlined"
                      @click="loadMoreFollowers"
                      :loading="followersLoadingMore"
                      >加载更多</v-btn
                    >
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Following Tab -->
              <v-tabs-window-item value="following">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">关注 ({{ followingCount }})</h3>
                  <v-row v-if="followingList.length > 0">
                    <v-col
                      v-for="f in followingList"
                      :key="f.id"
                      cols="12"
                      sm="6"
                      md="4"
                      lg="3"
                    >
                      <v-card class="user-card" hover>
                        <v-card-text class="text-center pa-4">
                          <v-avatar size="80" class="mb-3">
                            <v-img :src="f.avatarSource || defaultAvatar" />
                          </v-avatar>
                          <h4 class="text-h6 mb-1">
                            {{ f.nickname || "用户" }}
                          </h4>
                          <p class="text-body-2 text-grey-darken-1">
                            {{ f.userDescription || "暂无简介" }}
                          </p>
                        </v-card-text>
                        <v-card-actions class="justify-center">
                          <v-btn
                            color="error"
                            variant="outlined"
                            size="small"
                            @click="doFollow(f.id)"
                          >
                            取消关注
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div v-else class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-account-plus</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">
                      暂未关注任何人
                    </p>
                  </div>
                  <div v-if="followingHasMore" class="text-center mt-4">
                    <v-btn
                      variant="outlined"
                      @click="loadMoreFollowing"
                      :loading="followingLoadingMore"
                      >加载更多</v-btn
                    >
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Favourites Tab -->
              <v-tabs-window-item value="favourites">
                <div class="tab-content">
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3 class="text-h5">收藏夹 ({{ favouritesCount }})</h3>
                    <v-btn
                      color="primary"
                      prepend-icon="mdi-plus"
                      @click="createFavouriteDialog = true"
                    >
                      新建收藏夹
                    </v-btn>
                  </div>

                  <!-- Favourite folders -->
                  <div v-if="!viewingFavouriteId">
                    <v-row v-if="favourites.length > 0">
                      <v-col
                        v-for="favourite in favourites"
                        :key="favourite.id"
                        cols="12"
                        md="6"
                        lg="4"
                      >
                        <v-card class="favourite-card" hover>
                          <v-card-title
                            class="d-flex justify-space-between align-center"
                          >
                            <span>{{ favourite.name }}</span>
                            <v-menu>
                              <template v-slot:activator="{ props }">
                                <v-btn
                                  icon="mdi-dots-vertical"
                                  size="small"
                                  variant="text"
                                  v-bind="props"
                                />
                              </template>
                              <v-list>
                                <v-list-item @click="editFavourite(favourite)">
                                  <v-list-item-title>编辑</v-list-item-title>
                                </v-list-item>
                                <v-list-item
                                  @click="deleteFavouriteConfirm(favourite.id)"
                                >
                                  <v-list-item-title>删除</v-list-item-title>
                                </v-list-item>
                              </v-list>
                            </v-menu>
                          </v-card-title>
                          <v-card-text>
                            <p class="text-body-2 text-grey-darken-1 mb-2">
                              {{ favourite.description || "暂无描述" }}
                            </p>
                            <v-chip
                              size="small"
                              color="primary"
                              variant="outlined"
                            >
                              {{ favourite.videoCount || 0 }} 个视频
                            </v-chip>
                          </v-card-text>
                          <v-card-actions>
                            <v-btn
                              color="primary"
                              variant="text"
                              @click="viewFavourite(favourite.id)"
                              >查看视频</v-btn
                            >
                          </v-card-actions>
                        </v-card>
                      </v-col>
                    </v-row>
                    <div v-else class="text-center py-8">
                      <v-icon size="64" color="grey-lighten-1"
                        >mdi-heart</v-icon
                      >
                      <p class="text-h6 text-grey-darken-1 mt-4">暂无收藏夹</p>
                    </div>
                  </div>

                  <!-- Favourite videos view -->
                  <div v-else>
                    <v-btn
                      variant="text"
                      prepend-icon="mdi-arrow-left"
                      class="mb-4"
                      @click="viewingFavouriteId = null"
                    >
                      返回收藏夹列表
                    </v-btn>
                    <v-row v-if="favouriteVideos.length > 0">
                      <v-col
                        v-for="video in favouriteVideos"
                        :key="video.id"
                        cols="12"
                        md="6"
                        lg="4"
                      >
                        <v-card
                          class="video-card"
                          hover
                          @click="openPlayer(video)"
                        >
                          <v-img
                            :src="video.imgSource || defaultCover"
                            height="200"
                            cover
                          />
                          <v-card-title class="text-subtitle-1">{{
                            video.title || "无标题"
                          }}</v-card-title>
                        </v-card>
                      </v-col>
                    </v-row>
                    <div v-else class="text-center py-8">
                      <p class="text-body-1 text-medium-emphasis">
                        该收藏夹暂无视频
                      </p>
                    </div>
                  </div>
                </div>
              </v-tabs-window-item>
            </v-tabs-window>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Edit Profile Dialog -->
    <v-dialog v-model="editProfileDialog" max-width="600px">
      <v-card>
        <v-card-title>编辑资料</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="editedUser.nickname"
            label="昵称"
            :rules="[(v) => !!v || '昵称不能为空']"
            required
          />
          <v-textarea
            v-model="editedUser.userDescription"
            label="个人简介"
            rows="3"
          />
          <v-select
            v-model="editedUser.sex"
            :items="sexOptions"
            label="性别"
            item-title="text"
            item-value="value"
          />
          <v-file-input
            v-model="avatarFile"
            label="头像"
            accept="image/*"
            prepend-icon="mdi-camera"
            @change="handleFileUpload"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" variant="text" @click="editProfileDialog = false"
            >取消</v-btn
          >
          <v-btn color="primary" @click="saveProfile" :loading="savingProfile"
            >保存</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Create/Edit Favourite Dialog -->
    <v-dialog v-model="createFavouriteDialog" max-width="500px">
      <v-card>
        <v-card-title>{{
          editingFavourite ? "编辑收藏夹" : "新建收藏夹"
        }}</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="newFavourite.name"
            label="收藏夹名称"
            :rules="[(v) => !!v || '名称不能为空']"
            required
          />
          <v-textarea
            v-model="newFavourite.description"
            label="描述"
            rows="3"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey"
            variant="text"
            @click="createFavouriteDialog = false"
            >取消</v-btn
          >
          <v-btn color="primary" @click="saveFavourite">{{
            editingFavourite ? "保存" : "创建"
          }}</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <VideoPlayerDialog v-model="showPlayer" :video="currentVideoForPlayer" />

    <v-overlay v-model="loading" class="align-center justify-center">
      <v-progress-circular color="primary" indeterminate size="64" />
    </v-overlay>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import {
  getCustomerFavourite,
  addOrUpdateFavourite,
  deleteFavourite,
  uploadAvatar,
  getUserInfoByUserId,
  updateUserInfo,
  getFollow,
  getFollowers,
  followUser,
} from "@/api/customer";
import { getVideoHistory, getVideoInFavouriteTable } from "@/api/video";
import { useUserStore } from "@/pinia/user";
import VideoPlayerDialog from "@/components/VideoPlayerDialog.vue";

const loading = ref(false);
const activeTab = ref("history");
const editProfileDialog = ref(false);
const createFavouriteDialog = ref(false);
const editingFavourite = ref(false);
const avatarFile = ref(null);
const savingProfile = ref(false);
const showPlayer = ref(false);
const currentVideoForPlayer = ref(null);
const followingSet = computed(
  () => new Set(followingList.value.map((f) => f.id)),
);

const defaultAvatar = new URL("@/assets/default-avatar.png", import.meta.url)
  .href;
const defaultCover = new URL("@/assets/video-cover.png", import.meta.url).href;

const userInfo = ref({
  username: "",
  nickname: "",
  avatarSource: "",
  sex: "",
  userDescription: "",
});
const editedUser = ref({
  nickname: "",
  avatarSource: "",
  sex: "",
  userDescription: "",
});

const followers = ref([]);
const followingList = ref([]);
const favourites = ref([]);
const watchHistory = ref([]);
const favouriteVideos = ref([]);
const viewingFavouriteId = ref(null);

// Pagination
const followersPage = ref(1);
const followersHasMore = ref(false);
const followersLoadingMore = ref(false);
const followingPage = ref(1);
const followingHasMore = ref(false);
const followingLoadingMore = ref(false);

const newFavourite = reactive({ id: null, name: "", description: "" });

const followersCount = computed(() => followers.value.length);
const followingCount = computed(() => followingList.value.length);
const favouritesCount = computed(() => favourites.value.length);

const sexOptions = [
  { text: "男", value: "M" },
  { text: "女", value: "F" },
  { text: "不愿透露", value: "O" },
];

const snackbar = reactive({ show: false, message: "", color: "success" });
const showMessage = (message, color = "success") => {
  snackbar.message = message;
  snackbar.color = color;
  snackbar.show = true;
};

function openPlayer(video) {
  currentVideoForPlayer.value = { ...video };
  showPlayer.value = true;
}

const loadUserInfo = async (userId) => {
  try {
    loading.value = true;
    const response = await getUserInfoByUserId(userId);
    userInfo.value = response.data;
    editedUser.value = { ...response.data };
  } catch (error) {
    console.error("Error loading user info:", error);
    showMessage("加载用户信息失败", "error");
  } finally {
    loading.value = false;
  }
};

const loadFollowers = async (reset = true) => {
  try {
    if (reset) followersPage.value = 1;
    const response = await getFollowers(followersPage.value, 20);
    const records = response.data?.items || response.data || [];
    if (reset) followers.value = records;
    else followers.value.push(...records);
    followersHasMore.value = records.length >= 20;
  } catch (error) {
    console.error("Error loading followers:", error);
  }
};

const loadMoreFollowers = async () => {
  followersPage.value++;
  followersLoadingMore.value = true;
  await loadFollowers(false);
  followersLoadingMore.value = false;
};

const loadFollowing = async (reset = true) => {
  try {
    if (reset) followingPage.value = 1;
    const response = await getFollow(followingPage.value, 20);
    const records = response.data?.items || response.data || [];
    if (reset) followingList.value = records;
    else followingList.value.push(...records);
    followingHasMore.value = records.length >= 20;
  } catch (error) {
    console.error("Error loading following:", error);
  }
};

const loadMoreFollowing = async () => {
  followingPage.value++;
  followingLoadingMore.value = true;
  await loadFollowing(false);
  followingLoadingMore.value = false;
};

const loadFavourites = async () => {
  try {
    const response = await getCustomerFavourite();
    favourites.value = response.data || [];
  } catch (error) {
    console.error("Error loading favourites:", error);
  }
};

const loadWatchHistory = async () => {
  try {
    const response = await getVideoHistory();
    const data = response.data;
    if (Array.isArray(data)) watchHistory.value = data;
    else if (data?.records) watchHistory.value = data.records;
    else if (data?.list) watchHistory.value = data.list;
    else watchHistory.value = [];
  } catch (error) {
    console.error("Error loading watch history:", error);
    watchHistory.value = [];
  }
};

const viewFavourite = async (favouriteId) => {
  viewingFavouriteId.value = favouriteId;
  try {
    const response = await getVideoInFavouriteTable(favouriteId);
    const data = response.data;
    if (Array.isArray(data)) favouriteVideos.value = data;
    else if (data?.records) favouriteVideos.value = data.records;
    else if (data?.list) favouriteVideos.value = data.list;
    else favouriteVideos.value = [];
  } catch (error) {
    console.error("Error loading favourite videos:", error);
    favouriteVideos.value = [];
  }
};

const handleFileUpload = async () => {
  // Vuetify v-file-input v-model can be a File, an array of Files, or null
  let file = avatarFile.value;
  if (Array.isArray(file)) file = file[0];
  if (!file) return;
  try {
    loading.value = true;
    const response = await uploadAvatar(file);
    editedUser.value.avatarSource = response.data;
    showMessage("头像上传成功");
  } catch (error) {
    console.error("Error uploading avatar:", error);
    showMessage("头像上传失败", "error");
  } finally {
    loading.value = false;
  }
};

const openEditProfile = () => {
  editedUser.value = {
    nickname: userInfo.value.nickname || "",
    avatarSource: userInfo.value.avatarSource || "",
    sex: userInfo.value.sex || "",
    userDescription: userInfo.value.userDescription || "",
  };
  editProfileDialog.value = true;
};

const saveProfile = async () => {
  if (!editedUser.value.nickname || !editedUser.value.nickname.trim()) {
    showMessage("昵称不能为空", "error");
    return;
  }
  savingProfile.value = true;
  try {
    await updateUserInfo(editedUser.value);
    userInfo.value = { ...userInfo.value, ...editedUser.value };

    editProfileDialog.value = false;
    showMessage("资料更新成功");
  } catch (error) {
    console.error("Error updating profile:", error);
    showMessage("更新失败", "error");
  } finally {
    savingProfile.value = false;
  }
};

const editFavourite = (favourite) => {
  editingFavourite.value = true;
  newFavourite.id = favourite.id;
  newFavourite.name = favourite.name;
  newFavourite.description = favourite.description || "";
  createFavouriteDialog.value = true;
};

const saveFavourite = async () => {
  if (!newFavourite.name) {
    showMessage("请填写收藏夹名称", "error");
    return;
  }
  try {
    loading.value = true;
    const formData = new FormData();
    if (newFavourite.id) formData.append("id", newFavourite.id);
    formData.append("name", newFavourite.name);
    formData.append("description", newFavourite.description);
    await addOrUpdateFavourite(formData);
    await loadFavourites();
    createFavouriteDialog.value = false;
    newFavourite.id = null;
    newFavourite.name = "";
    newFavourite.description = "";
    editingFavourite.value = false;
    showMessage(editingFavourite.value ? "收藏夹更新成功" : "收藏夹创建成功");
  } catch (error) {
    console.error("Error saving favourite:", error);
    showMessage("操作失败", "error");
  } finally {
    loading.value = false;
  }
};

const deleteFavouriteConfirm = async (favouriteId) => {
  if (confirm("确定要删除这个收藏夹吗？")) {
    try {
      loading.value = true;
      await deleteFavourite(favouriteId);
      await loadFavourites();
      showMessage("收藏夹已删除");
    } catch (error) {
      showMessage("删除失败", "error");
    } finally {
      loading.value = false;
    }
  }
};

const doFollow = async (userId, isFollow) => {
  const isCurrentlyFollowing = followingSet.value.has(userId);
  try {
    await followUser(userId, !isCurrentlyFollowing);
    await loadFollowing();
    await loadFollowers();
    showMessage(isCurrentlyFollowing ? "已取消关注" : "已关注");
  } catch (error) {
    showMessage("操作失败", "error");
  }
};

onMounted(() => {
  const userStore = useUserStore();
  if (userStore.userId) {
    loadUserInfo(userStore.userId);
  }
  loadFollowers();
  loadFollowing();
  loadFavourites();
  loadWatchHistory();
});
</script>

<style scoped>
.user-profile-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}
.user-info-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
}
.user-avatar {
  border: 4px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}
.user-details {
  padding-left: 24px;
}
.stats-row {
  margin-top: 16px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 32px;
}
.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #1976d2;
}
.stat-label {
  font-size: 14px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.tab-content {
  min-height: 400px;
}
.video-card,
.user-card,
.favourite-card {
  background: rgba(255, 255, 255, 0.08) !important;
  transition: all 0.3s ease;
  border-radius: 12px;
}
.video-card:hover,
.user-card:hover,
.favourite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}
.favourite-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
}
.v-tabs {
  border-radius: 12px 12px 0 0;
}
@media (max-width: 768px) {
  .user-profile-container {
    padding: 12px;
  }
  .user-details {
    padding-left: 0;
    margin-top: 16px;
  }
  .stat-item {
    margin: 0 16px;
  }
}
</style>
