// @/api/customer.js
import request from "@/utils/request";

// 获取当前用户收藏夹
export function getCustomerFavourite() {
  return request.get("/customer/favourites");
}

// 获取指定收藏夹信息
export function getFavouriteById(favouriteId) {
  return request.get(`/customer/favourites/${favouriteId}`);
}

// 新增或修改收藏夹
export function addOrUpdateFavourite(favouriteDTO) {
  return request.post("/customer/favourites", favouriteDTO);
}

// 删除收藏夹
export function deleteFavourite(favouriteId) {
  return request.delete(`/customer/favourites/${favouriteId}`);
}

// 订阅分类
export function subscribeVideoTypes(types) {
  return request.post("/customer/subscribe", null, {
    params: { types },
  });
}

// 获取用户订阅的分类
export function getSubscribe() {
  return request.get("/customer/subscribe");
}

// 上传用户头像
export function uploadAvatar(file) {
  const formData = new FormData();
  formData.append("file", file);
  return request.post("/customer/upload/avatar", formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
}

// 根据用户ID获取用户信息
export function getUserInfoByUserId(userId) {
  return request.get(`/customer/getInfo/${userId}`);
}

// 修改用户信息
export function updateUserInfo({
  nickname,
  avatarSource,
  sex,
  userDescription,
}) {
  return request.put("/customer", null, {
    params: {
      nickname,
      avatarSource,
      sex,
      userDescription,
    },
  });
}

// 获取我关注的用户（我是粉丝）
export function getFollow(page = 1, limit = 10) {
  return request.get("/customer/follow", {
    params: { page, limit },
  });
}

// 获取我的粉丝
export function getFollowers(page = 1, limit = 10) {
  return request.get("/customer/followers", {
    params: { page, limit },
  });
}

// 关注或取关用户
export function followUser(followUserId, isFollow) {
  return request.post(`/customer/${followUserId}/${isFollow}`);
}

// 更新用户兴趣模型
export function updateUserModel(userModelDTO) {
  return request.post("/customer/updateUserModel", userModelDTO);
}
