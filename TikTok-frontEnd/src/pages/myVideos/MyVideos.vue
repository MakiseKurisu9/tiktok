<template>
  <v-container class="px-4 py-6">
    <div class="d-flex justify-space-between align-center mb-8">
      <div>
        <h1 class="text-h4 font-weight-bold mb-2 gradient-text">我的视频</h1>
        <p class="text-body-1 text-medium-emphasis">管理你发布的所有视频</p>
      </div>
      <v-btn color="primary" prepend-icon="mdi-plus" @click="openUpload">
        发布视频
      </v-btn>
    </div>

    <div v-if="loading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
    </div>

    <v-row v-else-if="videos.length > 0">
      <v-col
        v-for="video in videos"
        :key="video.id"
        cols="12"
        sm="6"
        md="4"
        lg="3"
      >
        <v-card class="video-manage-card" elevation="2">
          <v-img :src="video.imgSource || defaultCover" height="180" cover>
            <v-chip
              class="ma-2"
              :color="video.status === 1 ? 'success' : 'warning'"
              size="small"
            >
              已发布
            </v-chip>
          </v-img>
          <v-card-title class="text-subtitle-1">{{
            video.title || "无标题"
          }}</v-card-title>
          <v-card-subtitle>
            <span class="mr-3"
              ><v-icon size="14">mdi-eye</v-icon> {{ video.views || 0 }}</span
            >
            <span class="mr-3"
              ><v-icon size="14">mdi-heart</v-icon> {{ video.likes || 0 }}</span
            >
            <span
              ><v-icon size="14">mdi-comment</v-icon>
              {{ video.comments || 0 }}</span
            >
          </v-card-subtitle>
          <v-card-actions>
            <v-btn
              size="small"
              variant="text"
              color="primary"
              @click="editVideo(video)"
            >
              <v-icon class="mr-1">mdi-pencil</v-icon>编辑
            </v-btn>
            <v-spacer />
            <v-btn
              size="small"
              variant="text"
              color="error"
              @click="confirmDelete(video)"
            >
              <v-icon class="mr-1">mdi-delete</v-icon>删除
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

    <div v-else class="text-center py-16">
      <v-icon size="64" color="grey-lighten-2">mdi-video-off</v-icon>
      <p class="text-h6 text-medium-emphasis mt-4">暂无视频</p>
      <v-btn color="primary" class="mt-4" @click="openUpload"
        >发布第一个视频</v-btn
      >
    </div>

    <div v-if="hasMore" class="text-center mt-4">
      <v-btn variant="outlined" @click="loadMore" :loading="loadingMore"
        >加载更多</v-btn
      >
    </div>

    <!-- Upload / Edit Dialog -->
    <v-dialog v-model="showDialog" max-width="600" persistent>
      <v-card>
        <v-card-title>{{ isEditing ? "编辑视频" : "发布视频" }}</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="form.title"
            label="视频标题"
            variant="outlined"
            class="mb-3"
          />
          <v-textarea
            v-model="form.description"
            label="视频简介"
            variant="outlined"
            rows="3"
            class="mb-3"
          />
          <v-text-field
            v-model="form.type"
            label="视频分类 (如：搞笑,音乐)"
            variant="outlined"
            class="mb-3"
          />
          <v-text-field
            v-model="form.imgSource"
            label="封面图 URL（可选）"
            variant="outlined"
            class="mb-3"
          />

          <div v-if="!isEditing" class="mb-3">
            <p class="text-subtitle-2 mb-2">上传视频文件</p>
            <v-file-input
              v-model="videoFile"
              label="选择视频文件"
              accept="video/*"
              prepend-icon="mdi-video"
              variant="outlined"
              :loading="uploading"
            />
            <v-progress-linear
              v-if="uploading"
              indeterminate
              color="primary"
              class="mt-2"
            />
          </div>

          <v-text-field
            v-model="form.source"
            label="视频 URL"
            variant="outlined"
            :disabled="uploading"
            hint="上传后自动填入，也可手动输入"
            persistent-hint
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            @click="showDialog = false"
            :disabled="uploading || submitting"
            >取消</v-btn
          >
          <v-btn
            color="primary"
            @click="submitVideo"
            :loading="submitting"
            :disabled="uploading"
          >
            {{ isEditing ? "保存" : "发布" }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete confirm -->
    <v-dialog v-model="showDeleteConfirm" max-width="400">
      <v-card>
        <v-card-title>确认删除</v-card-title>
        <v-card-text>确定要删除这个视频吗？此操作不可撤销。</v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="showDeleteConfirm = false">取消</v-btn>
          <v-btn color="error" @click="doDelete" :loading="deleting"
            >删除</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snack" :color="snackColor" :timeout="3000">{{
      snackMsg
    }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { listVideos, addOrUpdateVideo, deleteVideo } from "@/api/video";
import request from "@/utils/request";

const defaultCover = new URL("@/assets/video-cover.png", import.meta.url).href;

const videos = ref([]);
const loading = ref(true);
const loadingMore = ref(false);
const page = ref(1);
const hasMore = ref(false);

const showDialog = ref(false);
const isEditing = ref(false);
const uploading = ref(false);
const submitting = ref(false);
const videoFile = ref(null);

const showDeleteConfirm = ref(false);
const deleting = ref(false);
const deleteTarget = ref(null);

const snack = ref(false);
const snackMsg = ref("");
const snackColor = ref("success");

const form = reactive({
  id: null,
  title: "",
  description: "",
  source: "",
  imgSource: "",
  type: "",
});

function showMessage(msg, color = "success") {
  snackMsg.value = msg;
  snackColor.value = color;
  snack.value = true;
}

async function loadVideos(reset = true) {
  if (reset) {
    page.value = 1;
    loading.value = true;
  } else {
    loadingMore.value = true;
  }
  try {
    const res = await listVideos(page.value, 12);
    const data = res.data;
    const records =
      data?.records ||
      data?.list ||
      data?.items ||
      (Array.isArray(data) ? data : []);
    if (reset) videos.value = records;
    else videos.value.push(...records);
    hasMore.value = records.length >= 12;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
    loadingMore.value = false;
  }
}

function loadMore() {
  page.value++;
  loadVideos(false);
}

function openUpload() {
  isEditing.value = false;
  Object.assign(form, {
    id: null,
    title: "",
    description: "",
    source: "",
    imgSource: "",
    type: "",
  });
  videoFile.value = null;
  showDialog.value = true;
}

function editVideo(video) {
  isEditing.value = true;
  Object.assign(form, {
    id: video.id,
    title: video.title || "",
    description: video.description || "",
    source: video.source || "",
    imgSource: video.imgSource || "",
    type: video.type || "",
  });
  showDialog.value = true;
}

async function submitVideo() {
  if (!form.title) {
    showMessage("请填写视频标题", "error");
    return;
  }

  // Fix: Vuetify 3 v-file-input binds a single File, not an array
  const file =
    videoFile.value instanceof File
      ? videoFile.value
      : Array.isArray(videoFile.value) && videoFile.value.length > 0
        ? videoFile.value[0]
        : null;

  if (file) {
    uploading.value = true;
    try {
      const fd = new FormData();
      fd.append("file", file);
      fd.append("type", "video");
      const uploadRes = await request.post("/upload", fd, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const url = uploadRes?.data;

      if (!url) throw new Error("服务器未返回视频地址");
      form.source = url;
    } catch (e) {
      showMessage("视频上传失败: " + (e.message || ""), "error");
      uploading.value = false;
      return;
    }
    uploading.value = false;
  }

  if (!form.source && !isEditing.value) {
    showMessage("请上传视频或填写视频URL", "error");
    return;
  }

  submitting.value = true;
  try {
    await addOrUpdateVideo({
      id: form.id,
      title: form.title,
      description: form.description,
      source: form.source,
      imgSource: form.imgSource,
      type: form.type,
    });
    showMessage(isEditing.value ? "保存成功" : "发布成功");
    showDialog.value = false;
    await loadVideos();
  } catch (e) {
    showMessage("操作失败", "error");
  } finally {
    submitting.value = false;
  }
}

function confirmDelete(video) {
  deleteTarget.value = video;
  showDeleteConfirm.value = true;
}

async function doDelete() {
  deleting.value = true;
  try {
    await deleteVideo(deleteTarget.value.id);
    showMessage("删除成功");
    showDeleteConfirm.value = false;
    await loadVideos();
  } catch (e) {
    showMessage("删除失败", "error");
  } finally {
    deleting.value = false;
  }
}

onMounted(() => loadVideos());
</script>

<style scoped>
.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.video-manage-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}
.video-manage-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}
</style>
