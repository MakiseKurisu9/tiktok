<template>
  <v-container fluid class="user-profile-container">
    <!-- Header Section with User Info -->
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
                    {{ userInfo.username || "Unknown User" }}
                  </h2>
                  <p class="text-body-1 text-grey-darken-1 mb-3">
                    {{ userInfo.userDescription || "No description available" }}
                  </p>
                  <v-row class="stats-row">
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ followersCount }}</span>
                        <span class="stat-label">Followers</span>
                      </div>
                    </v-col>
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ followingCount }}</span>
                        <span class="stat-label">Following</span>
                      </div>
                    </v-col>
                    <v-col cols="auto">
                      <div class="stat-item">
                        <span class="stat-number">{{ favouritesCount }}</span>
                        <span class="stat-label">Favourites</span>
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
                  @click="editProfileDialog = true"
                >
                  Edit Profile
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
            <v-tab value="history">
              <v-icon left>mdi-history</v-icon>
              Watch History
            </v-tab>
            <v-tab value="followers">
              <v-icon left>mdi-account-group</v-icon>
              Followers
            </v-tab>
            <v-tab value="following">
              <v-icon left>mdi-account-plus</v-icon>
              Following
            </v-tab>
            <v-tab value="favourites">
              <v-icon left>mdi-heart</v-icon>
              Favourites
            </v-tab>
          </v-tabs>

          <v-card-text class="pa-6">
            <v-tabs-window v-model="activeTab">
              <!-- Watch History Tab -->
              <v-tabs-window-item value="history">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">Video Watch History</h3>
                  <v-row>
                    <v-col
                      v-for="video in watchHistory"
                      :key="video.id"
                      cols="12"
                      md="6"
                      lg="4"
                    >
                      <v-card class="video-card" hover>
                        <v-img :src="video.thumbnail" height="200" cover>
                          <v-chip
                            class="ma-2"
                            color="black"
                            text-color="white"
                            size="small"
                          >
                            {{ video.duration }}
                          </v-chip>
                        </v-img>
                        <v-card-title class="text-subtitle-1">
                          {{ video.title }}
                        </v-card-title>
                        <v-card-subtitle>
                          Watched {{ formatDate(video.watchedAt) }}
                        </v-card-subtitle>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div
                    v-if="watchHistory.length === 0"
                    class="text-center py-8"
                  >
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-history</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">
                      No watch history yet
                    </p>
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Followers Tab -->
              <v-tabs-window-item value="followers">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">Followers ({{ followersCount }})</h3>
                  <v-row>
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
                          <h4 class="text-h6 mb-1">{{ follower.nickName }}</h4>
                          <p class="text-body-2 text-grey-darken-1">
                            {{ follower.userDescription || "No description" }}
                          </p>
                        </v-card-text>
                        <v-card-actions class="justify-center">
                          <v-btn
                            color="primary"
                            variant="outlined"
                            size="small"
                            @click="viewProfile(follower.id)"
                          >
                            View Profile
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div v-if="followers.length === 0" class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-account-group</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">
                      No followers yet
                    </p>
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Following Tab -->
              <v-tabs-window-item value="following">
                <div class="tab-content">
                  <h3 class="text-h5 mb-4">Following ({{ followingCount }})</h3>
                  <v-row>
                    <v-col
                      v-for="following in followingList"
                      :key="following.id"
                      cols="12"
                      sm="6"
                      md="4"
                      lg="3"
                    >
                      <v-card class="user-card" hover>
                        <v-card-text class="text-center pa-4">
                          <v-avatar size="80" class="mb-3">
                            <v-img
                              :src="following.avatarSource || defaultAvatar"
                            />
                          </v-avatar>
                          <h4 class="text-h6 mb-1">{{ following.nickName }}</h4>
                          <p class="text-body-2 text-grey-darken-1">
                            {{ following.userDescription || "No description" }}
                          </p>
                        </v-card-text>
                        <v-card-actions class="justify-center">
                          <v-btn
                            color="error"
                            variant="outlined"
                            size="small"
                            @click="unfollowUser(following.id)"
                          >
                            Unfollow
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div
                    v-if="followingList.length === 0"
                    class="text-center py-8"
                  >
                    <v-icon size="64" color="grey-lighten-1"
                      >mdi-account-plus</v-icon
                    >
                    <p class="text-h6 text-grey-darken-1 mt-4">
                      Not following anyone yet
                    </p>
                  </div>
                </div>
              </v-tabs-window-item>

              <!-- Favourites Tab -->
              <v-tabs-window-item value="favourites">
                <div class="tab-content">
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3 class="text-h5">Favourites ({{ favouritesCount }})</h3>
                    <v-btn
                      color="primary"
                      prepend-icon="mdi-plus"
                      @click="createFavouriteDialog = true"
                    >
                      New Favourite
                    </v-btn>
                  </div>
                  <v-row>
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
                                <v-list-item-title>Edit</v-list-item-title>
                              </v-list-item>
                              <v-list-item
                                @click="deleteFavouriteConfirm(favourite.id)"
                              >
                                <v-list-item-title>Delete</v-list-item-title>
                              </v-list-item>
                            </v-list>
                          </v-menu>
                        </v-card-title>
                        <v-card-text>
                          <p class="text-body-2 text-grey-darken-1 mb-2">
                            {{ favourite.description || "No description" }}
                          </p>
                          <v-chip
                            size="small"
                            color="primary"
                            variant="outlined"
                          >
                            {{ favourite.videoCount || 0 }} videos
                          </v-chip>
                        </v-card-text>
                        <v-card-actions>
                          <v-btn
                            color="primary"
                            variant="text"
                            @click="viewFavourite(favourite.id)"
                          >
                            View Videos
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-col>
                  </v-row>
                  <div v-if="favourites.length === 0" class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1">mdi-heart</v-icon>
                    <p class="text-h6 text-grey-darken-1 mt-4">
                      No favourites created yet
                    </p>
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
        <v-card-title>Edit Profile</v-card-title>
        <v-card-text>
          <v-form ref="editForm">
            <v-text-field
              v-model="editedUser.nickName"
              label="Nickname"
              :rules="[(v) => !!v || 'Nickname is required']"
              required
            />
            <v-textarea
              v-model="editedUser.userDescription"
              label="Description"
              rows="3"
            />
            <v-select
              v-model="editedUser.sex"
              :items="sexOptions"
              label="Gender"
              item-title="text"
              item-value="value"
            />
            <v-file-input
              v-model="avatarFile"
              label="Profile Picture"
              accept="image/*"
              prepend-icon="mdi-camera"
              @change="handleFileUpload"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" variant="text" @click="editProfileDialog = false">
            Cancel
          </v-btn>
          <v-btn color="primary" @click="saveProfile"> Save </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Create Favourite Dialog -->
    <v-dialog v-model="createFavouriteDialog" max-width="500px">
      <v-card>
        <v-card-title>Create New Favourite</v-card-title>
        <v-card-text>
          <v-form ref="favouriteForm">
            <v-text-field
              v-model="newFavourite.name"
              label="Favourite Name"
              :rules="[(v) => !!v || 'Name is required']"
              required
            />
            <v-textarea
              v-model="newFavourite.description"
              label="Description"
              rows="3"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            color="grey"
            variant="text"
            @click="createFavouriteDialog = false"
          >
            Cancel
          </v-btn>
          <v-btn color="primary" @click="createFavourite"> Create </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading" class="align-center justify-center">
      <v-progress-circular color="primary" indeterminate size="64" />
    </v-overlay>

    <!-- Snackbar for notifications -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000">
      {{ snackbar.message }}
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="snackbar.show = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import {
  getCustomerFavourite,
  getFavouriteById,
  addOrUpdateFavourite,
  deleteFavourite,
  uploadAvatar,
  getUserInfoByUserId,
  updateUserInfo,
  getFollow,
  getFollowers,
  followUser,
} from "@/api/customer";
import { useUserStore } from "@/pinia/user";

// Reactive data
const loading = ref(false);
const activeTab = ref("history");
const editProfileDialog = ref(false);
const createFavouriteDialog = ref(false);
const avatarFile = ref(null);

// User data
const userInfo = ref({
  username: "",
  avatarSource: "",
  sex: "",
  userDescription: "",
});

const editedUser = ref({
  nickName: "",
  avatarSource: "",
  sex: "",
  userDescription: "",
});

// Lists data
const followers = ref([]);
const followingList = ref([]);
const favourites = ref([]);
const watchHistory = ref([]);

// New favourite form
const newFavourite = reactive({
  name: "",
  description: "",
});

// Computed properties
const followersCount = computed(() => followers.value.length);
const followingCount = computed(() => followingList.value.length);
const favouritesCount = computed(() => favourites.value.length);

// Constants
const defaultAvatar = new URL("@/assets/Logo.png", import.meta.url).href;
const sexOptions = [
  { text: "Male", value: "M" },
  { text: "Female", value: "F" },
  { text: "Prefer not to say", value: "O" },
];

// Snackbar
const snackbar = reactive({
  show: false,
  message: "",
  color: "success",
});

// Methods
const showMessage = (message, color = "success") => {
  snackbar.message = message;
  snackbar.color = color;
  snackbar.show = true;
};

const loadUserInfo = async (userId) => {
  try {
    loading.value = true;
    console.log("now is load userInfo" + userId);
    const response = await getUserInfoByUserId(userId);
    console.log(response.data);

    userInfo.value = response.data;
    editedUser.value = { ...response.data };
  } catch (error) {
    console.error("Error loading user info:", error);
    showMessage("Error loading user information", "error");
  } finally {
    loading.value = false;
  }
};

const loadFollowers = async () => {
  try {
    const response = await getFollowers(1, 50);
    followers.value = response.data.records || [];
  } catch (error) {
    console.error("Error loading followers:", error);
    showMessage("Error loading followers", "error");
  }
};

const loadFollowing = async () => {
  try {
    const response = await getFollow(1, 50);
    followingList.value = response.data.records || [];
  } catch (error) {
    console.error("Error loading following:", error);
    showMessage("Error loading following list", "error");
  }
};

const loadFavourites = async () => {
  try {
    const response = await getCustomerFavourite();
    favourites.value = response.data || [];
  } catch (error) {
    console.error("Error loading favourites:", error);
    showMessage("Error loading favourites", "error");
  }
};

const loadWatchHistory = async () => {
  // Mock data for watch history since no API provided
  watchHistory.value = [
    {
      id: 1,
      title: "Sample Video 1",
      thumbnail: "https://via.placeholder.com/300x200",
      duration: "10:30",
      watchedAt: new Date("2024-01-15"),
    },
    {
      id: 2,
      title: "Sample Video 2",
      thumbnail: "https://via.placeholder.com/300x200",
      duration: "15:45",
      watchedAt: new Date("2024-01-14"),
    },
  ];
};

const handleFileUpload = async (event) => {
  if (!avatarFile.value) return;

  try {
    loading.value = true;
    const response = await uploadAvatar(avatarFile.value[0]);
    editedUser.value.avatarSource = response.data.url;
    showMessage("Avatar uploaded successfully");
  } catch (error) {
    console.error("Error uploading avatar:", error);
    showMessage("Error uploading avatar", "error");
  } finally {
    loading.value = false;
  }
};

const saveProfile = async () => {
  try {
    loading.value = true;
    await updateUserInfo(editedUser.value);
    userInfo.value = { ...editedUser.value };
    editProfileDialog.value = false;
    showMessage("Profile updated successfully");
  } catch (error) {
    console.error("Error updating profile:", error);
    showMessage("Error updating profile", "error");
  } finally {
    loading.value = false;
  }
};

const createFavourite = async () => {
  try {
    loading.value = true;
    await addOrUpdateFavourite(newFavourite);
    await loadFavourites();
    createFavouriteDialog.value = false;
    newFavourite.name = "";
    newFavourite.description = "";
    showMessage("Favourite created successfully");
  } catch (error) {
    console.error("Error creating favourite:", error);
    showMessage("Error creating favourite", "error");
  } finally {
    loading.value = false;
  }
};

const editFavourite = (favourite) => {
  // Implementation for editing favourite
  console.log("Edit favourite:", favourite);
};

const deleteFavouriteConfirm = async (favouriteId) => {
  if (confirm("Are you sure you want to delete this favourite?")) {
    try {
      loading.value = true;
      await deleteFavourite(favouriteId);
      await loadFavourites();
      showMessage("Favourite deleted successfully");
    } catch (error) {
      console.error("Error deleting favourite:", error);
      showMessage("Error deleting favourite", "error");
    } finally {
      loading.value = false;
    }
  }
};

const unfollowUser = async (userId) => {
  try {
    loading.value = true;
    await followUser(userId, false);
    await loadFollowing();
    showMessage("User unfollowed successfully");
  } catch (error) {
    console.error("Error unfollowing user:", error);
    showMessage("Error unfollowing user", "error");
  } finally {
    loading.value = false;
  }
};

const viewProfile = (userId) => {
  // Implementation for viewing user profile
  console.log("View profile:", userId);
};

const viewFavourite = (favouriteId) => {
  // Implementation for viewing favourite videos
  console.log("View favourite:", favouriteId);
};

const formatDate = (date) => {
  return new Date(date).toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

// Lifecycle
onMounted(() => {
  // Load current user info (you might need to get userId from route or store)
  // loadUserInfo(currentUserId)
  const userStore = useUserStore();
  console.log(userStore);
  console.log(userStore.userId);
  loadUserInfo(userStore.userId);
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
  border: 1px solid rgba(255, 255, 255, 0.2);
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
  transition: all 0.3s ease;
  border-radius: 12px;
}

.video-card:hover,
.user-card:hover,
.favourite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
}

.video-card .v-img {
  border-radius: 12px 12px 0 0;
}

.user-card .v-avatar {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.favourite-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.v-tabs {
  border-radius: 12px 12px 0 0;
}

.v-tabs-window {
  border-radius: 0 0 12px 12px;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* Responsive design */
@media (max-width: 768px) {
  .user-profile-container {
    padding: 12px;
  }

  .user-details {
    padding-left: 0;
    margin-top: 16px;
  }

  .stats-row {
    justify-content: center;
  }

  .stat-item {
    margin: 0 16px;
  }
}
</style>
