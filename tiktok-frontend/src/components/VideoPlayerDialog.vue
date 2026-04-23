<template>
  <v-dialog v-model="visible" max-width="900" scrollable class="video-dialog">
    <v-card class="video-player-card">
      <v-toolbar color="surface" flat density="compact">
        <v-toolbar-title class="text-subtitle-1">{{
          video?.title || "播放视频"
        }}</v-toolbar-title>
        <v-spacer />
        <v-btn icon @click="close"><v-icon>mdi-close</v-icon></v-btn>
      </v-toolbar>

      <div class="video-container">
        <video
          v-if="video?.source"
          ref="playerRef"
          :src="video.source"
          controls
          autoplay
          class="video-player"
          @canplay="onCanPlay"
          @error="onError"
          @loadstart="loading = true"
          @timeupdate="onTimeUpdate"
          @ended="onVideoEnded"
        />
        <div
          v-if="loading"
          class="video-loading-overlay d-flex align-center justify-center"
        >
          <v-progress-circular indeterminate color="white" size="48" />
        </div>
        <div
          v-if="error"
          class="video-error-overlay d-flex flex-column align-center justify-center"
        >
          <v-icon size="48" color="white" class="mb-2">mdi-alert-circle</v-icon>
          <p class="text-white mb-3">视频加载失败</p>
          <v-btn color="primary" @click="retry">重试</v-btn>
        </div>
      </div>

      <div class="creator-bar d-flex align-center pa-3 ga-2">
        <v-avatar size="40" class="mr-1">
          <v-img
            :src="creatorAvatar || defaultAvatar"
            :alt="video?.publisherName"
          >
            <template v-slot:error>
              <v-icon size="24" color="grey-lighten-2"
                >mdi-account-circle</v-icon
              >
            </template>
          </v-img>
        </v-avatar>
        <div class="flex-grow-1">
          <div class="text-subtitle-2 font-weight-medium">
            {{ video?.publisherName || "未知创作者" }}
          </div>
          <div class="text-caption text-medium-emphasis">创作者</div>
        </div>
        <v-btn
          v-if="!isSelf"
          :color="isFollowing ? 'grey' : 'primary'"
          :variant="isFollowing ? 'outlined' : 'flat'"
          size="small"
          rounded
          :loading="followLoading"
          @click="handleFollow"
          prepend-icon="mdi-account-plus"
        >
          {{ isFollowing ? "已关注" : "关注" }}
        </v-btn>
        <v-chip v-else size="small" variant="outlined" color="grey">
          <v-icon start size="14">mdi-account</v-icon>本人
        </v-chip>
      </div>

      <v-divider />

      <div class="d-flex align-center pa-3 ga-2">
        <v-btn
          variant="text"
          :color="liked ? 'red' : ''"
          @click="handleLike"
          prepend-icon="mdi-heart"
        >
          {{ formatCount(video?.likes || 0) }}
        </v-btn>
        <v-btn
          variant="text"
          prepend-icon="mdi-star"
          @click="showFavDialog = true"
          >收藏</v-btn
        >
        <v-btn variant="text" prepend-icon="mdi-share" @click="handleShare"
          >分享</v-btn
        >
        <v-spacer />
        <span class="text-caption text-medium-emphasis"
          >{{ formatCount(video?.views || 0) }} 次播放</span
        >
      </div>

      <v-divider />

      <v-card-text class="pa-4" style="max-height: 400px; overflow-y: auto">
        <h4 class="text-subtitle-1 font-weight-medium mb-3">评论</h4>
        <div class="d-flex align-center mb-4 ga-2">
          <v-text-field
            v-model="newComment"
            density="compact"
            variant="outlined"
            placeholder="写下你的评论..."
            hide-details
            @keyup.enter="postComment"
          />
          <v-btn
            color="primary"
            :disabled="!newComment.trim()"
            @click="postComment"
            :loading="postingComment"
            >发送</v-btn
          >
        </div>

        <div v-if="commentsLoading" class="text-center py-4">
          <v-progress-circular indeterminate size="32" />
        </div>
        <div v-else>
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item mb-3"
          >
            <div class="d-flex">
              <v-avatar size="36" class="mr-3">
                <v-img
                  :src="comment.commentersDTO?.avatarSource || defaultAvatar"
                />
              </v-avatar>
              <div class="flex-grow-1">
                <div class="d-flex align-center">
                  <span class="text-subtitle-2 font-weight-medium mr-2">{{
                    comment.commentersDTO?.nickname || "匿名用户"
                  }}</span>
                  <span class="text-caption text-medium-emphasis">{{
                    formatTime(comment.createTime)
                  }}</span>
                  <v-spacer />
                  <v-btn
                    icon
                    size="x-small"
                    variant="text"
                    @click="likeCommentHandler(comment)"
                  >
                    <v-icon size="16" :color="comment.isLiked ? 'red' : 'grey'"
                      >mdi-thumb-up</v-icon
                    >
                  </v-btn>
                  <span class="text-caption ml-1">{{
                    comment.likesCount || 0
                  }}</span>
                </div>
                <p class="text-body-2 mt-1">{{ comment.content }}</p>
                <v-btn
                  variant="text"
                  size="x-small"
                  class="mt-1"
                  @click="replyTo = comment"
                  >回复</v-btn
                >

                <div
                  v-if="replyTo?.id === comment.id"
                  class="d-flex align-center mt-2 ga-2"
                >
                  <v-text-field
                    v-model="replyContent"
                    density="compact"
                    variant="outlined"
                    :placeholder="`回复 ${comment.commentersDTO?.nickname}...`"
                    hide-details
                    @keyup.enter="postReply(comment)"
                  />
                  <v-btn
                    size="small"
                    color="primary"
                    @click="postReply(comment)"
                    >回复</v-btn
                  >
                  <v-btn size="small" variant="text" @click="replyTo = null"
                    >取消</v-btn
                  >
                </div>

                <div
                  v-if="comment.subComments && comment.subComments.length > 0"
                  class="ml-4 mt-2"
                >
                  <div
                    v-for="sub in comment.subComments"
                    :key="sub.id"
                    class="d-flex mb-2"
                  >
                    <v-avatar size="28" class="mr-2">
                      <v-img
                        :src="sub.commentersDTO?.avatarSource || defaultAvatar"
                      />
                    </v-avatar>
                    <div>
                      <span class="text-caption font-weight-medium">{{
                        sub.commentersDTO?.nickname || "匿名用户"
                      }}</span>
                      <p class="text-body-2">{{ sub.content }}</p>
                    </div>
                  </div>
                </div>
                <v-btn
                  v-if="
                    comment.childCount > 0 &&
                    (!comment.subComments || comment.subComments.length === 0)
                  "
                  variant="text"
                  size="x-small"
                  color="primary"
                  @click="loadSubComments(comment)"
                >
                  查看 {{ comment.childCount }} 条回复
                </v-btn>
              </div>
            </div>
          </div>

          <div v-if="comments.length === 0" class="text-center py-4">
            <v-icon size="48" color="grey-lighten-2"
              >mdi-comment-outline</v-icon
            >
            <p class="text-body-2 text-medium-emphasis mt-2">
              暂无评论，快来抢沙发
            </p>
          </div>
          <div v-if="hasMoreComments" class="text-center mt-2">
            <v-btn
              variant="text"
              size="small"
              @click="loadMoreComments"
              :loading="loadingMoreComments"
              >加载更多评论</v-btn
            >
          </div>
        </div>
      </v-card-text>
    </v-card>

    <v-dialog v-model="showFavDialog" max-width="400">
      <v-card>
        <v-card-title>添加到收藏夹</v-card-title>
        <v-card-text>
          <v-list v-if="favourites.length > 0">
            <v-list-item
              v-for="fav in favourites"
              :key="fav.id"
              :title="fav.name"
              @click="addToFavourite(fav.id)"
            >
              <template v-slot:prepend
                ><v-icon>mdi-folder-heart</v-icon></template
              >
            </v-list-item>
          </v-list>
          <p v-else class="text-body-2 text-medium-emphasis">
            暂无收藏夹，请先创建
          </p>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showFavDialog = false">关闭</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-dialog>
</template>

<script setup>
import { ref, watch, computed } from "vue";
import {
  starVideo,
  addVideoIntoHistory,
  addVideoIntoFavouriteTable,
  commentOrAnswerComment,
  getCommentByVideoId,
  likeComment,
  getSubCommentsByRootId,
  incrementViews,
  isVideoLiked,
} from "@/api/video";
import { shareVideo as apiShareVideo } from "@/api";
import {
  getCustomerFavourite,
  updateUserModel,
  getUserInfoByUserId,
  followUser,
  getFollow,
} from "@/api/customer";
import { useUserStore } from "@/pinia/user";

const props = defineProps({
  modelValue: Boolean,
  video: Object,
});

// FIX 1: removed double comma in emits
const emit = defineEmits([
  "update:modelValue",
  "liked",
  "favourited",
  "viewed",
]);

const defaultAvatar = new URL("@/assets/default-avatar.png", import.meta.url)
  .href;
const visible = ref(false);
const playerRef = ref(null);
const loading = ref(false);
const error = ref(false);
const liked = ref(false);
const creatorAvatar = ref(null);
const isFollowing = ref(false);
const followLoading = ref(false);
const userStore = useUserStore();

const isSelf = computed(
  () =>
    userStore.userId != null &&
    props.video?.publisherId != null &&
    Number(userStore.userId) === Number(props.video.publisherId),
);

const comments = ref([]);
const commentsLoading = ref(false);
const newComment = ref("");
const postingComment = ref(false);
const replyTo = ref(null);
const replyContent = ref("");
const commentPage = ref(1);
const hasMoreComments = ref(false);
const loadingMoreComments = ref(false);
const showFavDialog = ref(false);
const favourites = ref([]);
const modelUpdated = ref(false);

watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
  },
);
watch(visible, (v) => {
  emit("update:modelValue", v);
});

watch(
  () => props.video,

  async (v) => {
    modelUpdated.value = false;
    creatorAvatar.value = null;
    isFollowing.value = false;

    if (v && v.id) {
      // FIX 2: emit "viewed" after incrementing so parent updates views count on card
      incrementViews(v.id)
        .then(() => {
          emit("viewed", v.id);
        })
        .catch(() => {});
      isVideoLiked(v.id)
        .then((res) => {
          liked.value = res.data ?? false;
        })
        .catch(() => {
          liked.value = v.isLiked || false;
        });

      commentPage.value = 1;
      comments.value = [];
      await loadComments();

      if (v.publisherId) {
        try {
          const res = await getUserInfoByUserId(v.publisherId);
          creatorAvatar.value = res.data?.avatarSource || null;
        } catch {
          creatorAvatar.value = null;
        }

        if (!isSelf.value) {
          try {
            const followRes = await getFollow(1, 100);
            const followList =
              followRes.data?.items ||
              followRes.data?.records ||
              followRes.data ||
              [];
            isFollowing.value = Array.isArray(followList)
              ? followList.some((u) => u.id === v.publisherId)
              : false;
          } catch {
            isFollowing.value = false;
          }
        }
      }

      setTimeout(() => {
        if (visible.value && v.id) addVideoIntoHistory(v.id).catch(() => {});
      }, 3000);
    }
  },
);

watch(showFavDialog, async (v) => {
  if (v) {
    try {
      const res = await getCustomerFavourite();
      favourites.value = res.data || [];
    } catch (e) {
      console.error(e);
    }
  }
});

function onCanPlay() {
  loading.value = false;
  error.value = false;
}
function onError() {
  loading.value = false;
  error.value = true;
}
function retry() {
  error.value = false;
  loading.value = true;
  if (playerRef.value) playerRef.value.load();
}
function close() {
  visible.value = false;
  if (playerRef.value) playerRef.value.pause();
}

function onTimeUpdate() {
  if (!playerRef.value || !props.video?.videoTypeId || modelUpdated.value)
    return;
  const { currentTime, duration } = playerRef.value;
  if (!duration) return;
  const watchRatio = currentTime / duration;
  if (watchRatio >= 0.3) {
    modelUpdated.value = true;
    updateUserModel({
      typeId: props.video.videoTypeId,
      score: Math.min(watchRatio * 10, 10),
    }).catch(() => {});
  }
}

function onVideoEnded() {
  if (!props.video?.videoTypeId || modelUpdated.value) return;
  modelUpdated.value = true;
  updateUserModel({ typeId: props.video.videoTypeId, score: 10 }).catch(
    () => {},
  );
}

// FIX 3: mutate props.video.likes directly since video is a reference not a copy
async function handleLike() {
  if (!props.video) return;
  try {
    await starVideo(props.video.id);
    liked.value = !liked.value;
    props.video.likes = (props.video.likes || 0) + (liked.value ? 1 : -1);
    props.video.isLiked = liked.value;
    emit("liked", props.video.id, liked.value);
  } catch (e) {
    console.error(e);
  }
}

async function handleShare() {
  if (!props.video) return;
  try {
    await apiShareVideo(props.video.id);
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(window.location.href);
      alert("链接已复制到剪贴板");
    }
  } catch (e) {
    console.error(e);
  }
}

async function handleFollow() {
  if (!props.video?.publisherId || isSelf.value) return;
  followLoading.value = true;
  try {
    const newState = !isFollowing.value;
    await followUser(props.video.publisherId, newState);
    isFollowing.value = newState;
  } catch (e) {
    console.error(e);
  } finally {
    followLoading.value = false;
  }
}

// FIX 4: mutate props.video.favourites directly + emit
async function addToFavourite(favId) {
  try {
    await addVideoIntoFavouriteTable(favId, props.video.id);
    props.video.favourites = (props.video.favourites || 0) + 1;
    showFavDialog.value = false;
    alert("已添加到收藏夹");
    emit("favourited", props.video.id);
  } catch (e) {
    console.error(e);
    alert("添加失败");
  }
}

async function loadComments() {
  if (!props.video?.id) return;
  commentsLoading.value = true;
  try {
    const res = await getCommentByVideoId(
      props.video.id,
      commentPage.value,
      10,
    );
    const data = res.data;
    const records =
      data?.items ||
      data?.records ||
      data?.list ||
      (Array.isArray(data) ? data : []);
    comments.value = records;
    hasMoreComments.value = records.length >= 10;
  } catch (e) {
    console.error(e);
  } finally {
    commentsLoading.value = false;
  }
}

async function loadMoreComments() {
  commentPage.value++;
  loadingMoreComments.value = true;
  try {
    const res = await getCommentByVideoId(
      props.video.id,
      commentPage.value,
      10,
    );
    const data = res.data;
    const records =
      data?.items ||
      data?.records ||
      data?.list ||
      (Array.isArray(data) ? data : []);
    comments.value.push(...records);
    hasMoreComments.value = records.length >= 10;
  } catch (e) {
    console.error(e);
  } finally {
    loadingMoreComments.value = false;
  }
}

async function postComment() {
  if (!newComment.value.trim() || !props.video?.id) return;
  postingComment.value = true;
  try {
    await commentOrAnswerComment(props.video.id, newComment.value);
    newComment.value = "";
    commentPage.value = 1;
    await loadComments();
  } catch (e) {
    console.error(e);
  } finally {
    postingComment.value = false;
  }
}

async function postReply(comment) {
  if (!replyContent.value.trim()) return;
  try {
    await commentOrAnswerComment(
      props.video.id,
      replyContent.value,
      comment.id,
    );
    replyContent.value = "";
    replyTo.value = null;
    commentPage.value = 1;
    await loadComments();
  } catch (e) {
    console.error(e);
  }
}

async function likeCommentHandler(comment) {
  try {
    await likeComment(comment.id);
    comment.isLiked = !comment.isLiked;
    // FIX 5: was mutating comment.likes, should be comment.likesCount
    comment.likesCount = (comment.likesCount || 0) + (comment.isLiked ? 1 : -1);
  } catch (e) {
    console.error(e);
  }
}

async function loadSubComments(comment) {
  try {
    const res = await getSubCommentsByRootId(comment.id, 1, 20);
    const data = res.data;
    comment.subComments =
      data?.items ||
      data?.records ||
      data?.list ||
      (Array.isArray(data) ? data : []);
  } catch (e) {
    console.error(e);
  }
}

function formatTime(time) {
  if (!time) return "";
  const diff = Date.now() - new Date(time).getTime();
  const days = Math.floor(diff / 86400000);
  const hrs = Math.floor(diff / 3600000);
  const mins = Math.floor(diff / 60000);
  if (days > 0) return `${days}天前`;
  if (hrs > 0) return `${hrs}小时前`;
  if (mins > 0) return `${mins}分钟前`;
  return "刚刚";
}

function formatCount(c) {
  if (!c) return "0";
  if (c >= 10000) return `${(c / 10000).toFixed(1)}万`;
  if (c >= 1000) return `${(c / 1000).toFixed(1)}k`;
  return c.toString();
}
</script>

<style scoped>
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
  max-height: 60vh;
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
.comment-item {
  border-bottom: 1px solid rgba(var(--v-border-color), 0.08);
  padding-bottom: 12px;
}
.ga-2 {
  gap: 8px;
}
.creator-bar {
  background: rgba(var(--v-theme-surface-variant), 0.3);
}
</style>
