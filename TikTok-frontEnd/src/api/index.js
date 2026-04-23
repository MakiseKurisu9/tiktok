// @/api/video.js
import request from "@/utils/request";

// 根据视频类型获取视频列表
export function getVideosByTypeId(typeId, page, limit) {
  return request.get(`/index/video/type/${typeId}`, {
    params: {
      page: page,
      limit: limit,
    },
  });
}

// 获取所有视频分类
export function getAllTypes() {
  return request.get("/index/types");
}

// 获取指定视频信息
export function getVideo(videoId) {
  return request.get(`/index/video/${videoId}`);
}

// 搜索视频
export function searchVideo({ page = 1, limit = 10, searchName }) {
  return request.get("/index/search", {
    params: { page, limit, searchName },
  });
}

// 获取搜索历史
export function getSearchVideoHistory() {
  return request.get("/index/search/history");
}

// 删除搜索历史项
export function deleteSearchHistory(searchName) {
  return request.post("/index/search/history/delete", null, {
    params: { searchName },
  });
}

// 分享视频（增加分享数 + 返回链接）
export function shareVideo(videoId) {
  return request.post(`/index/share/${videoId}`);
}

// 获取某用户发布的视频
export function getUserPublishVideos({ page = 1, limit = 10, userId }) {
  return request.get("/index/video/user", {
    params: { page, limit, userId },
  });
}

// 获取热榜排行
export function getHotRank() {
  return request.get("/index/video/hot/rank");
}

// 获取热播视频
export function getHotVideo(page, limit) {
  return request.get("/index/video/hot", {
    params: {
      page: page,
      limit: limit,
    },
  });
}

// 获取相似视频（根据类型）
export function getSimilarVideoByType(videoId, typeNames) {
  return request.get("/index/video/similar", {
    params: { videoId, typeNames },
  });
}

// 获取兴趣推荐视频
export function getPushVideos() {
  return request.get("/index/pushVideos");
}