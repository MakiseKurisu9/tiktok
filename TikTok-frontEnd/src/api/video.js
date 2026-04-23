// src/api/video.js
import request from "@/utils/request";

// 获取收藏夹中的所有视频
export function getVideoInFavouriteTable(favouriteTableId) {
  return request.get(`/video/favourite/${favouriteTableId}`);
}

// 将视频添加到收藏夹
export function addVideoIntoFavouriteTable(favouriteTableId, videoId) {
  return request.post(`/video/favourite/${favouriteTableId}/${videoId}`);
}

export const isVideoLiked = (videoId) =>
  request.get(`/video/isLiked/${videoId}`);

// 添加视频播放记录（用户刷视频时调用）
export function addVideoIntoHistory(videoId) {
  return request.post(`/video/history/${videoId}`);
}
// 增加视频浏览次数
export const incrementViews = (videoId) =>
  request.post(`/video/view/${videoId}`);

// 获取视频播放记录
export function getVideoHistory() {
  return request.get("/video/history");
}

// 收藏（点赞）视频
export function starVideo(videoId) {
  return request.post(`/video/star/${videoId}`);
}

// 删除视频
export function deleteVideo(videoId) {
  return request.delete(`/video/delete/${videoId}`);
}

// 新增或更新视频信息
export function addOrUpdateVideo(video) {
  return request.post("/video", video);
}

// 获取用户所管理的视频（分页）
export function listVideos(page = 1, limit = 10) {
  return request.get("/video", {
    params: { page, limit },
  });
}

// 获取推送视频流（关注）
export function feedPush(lastId) {
  return request.get("/video/follow/feed", {
    params: { lastId },
  });
}

// 发布评论或回复评论
export function commentOrAnswerComment(videoId, content, parentId = null) {
  const params = { videoId, content };
  if (parentId !== null) {
    params.parentId = parentId;
  }
  return request.post("/video/comment", null, { params });
}

// 获取某视频下的评论（分页）
export function getCommentByVideoId(videoId, page, limit) {
  return request.get("/video/index/comment/by/video", {
    params: { videoId, page, limit },
  });
}

// 点赞评论
export function likeComment(commentId) {
  return request.post(`/video/comment/like/${commentId}`);
}

// 删除评论
export function deleteComment(commentId) {
  return request.delete(`/video/comment/${commentId}`);
}

// 获取子评论（通过根评论 ID）
export function getSubCommentsByRootId(rootId, page = 1, limit = 5) {
  return request.get("/video/index/comment/by/rootComment", {
    params: { rootId, page, limit },
  });
}
