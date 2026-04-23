import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      component: () => import("@/pages/Login.vue"),
    },
    {
      path: "/findPassword",
      component: () => import("@/pages/FindPassword.vue"),
    },
    {
      path: "/",
      redirect: "/index/hotVideos",
      component: () => import("@/components/AppLayout.vue"),
      children: [
        { path: "index/hotVideos", component: () => import("@/pages/hotVideo/HotVideo.vue") },
        { path: "videoType/:id", component: () => import("@/pages/videoType/VideoType.vue"), props: true },
        { path: "customer/profile", component: () => import("@/pages/customer/profile.vue") },
        { path: "index/pushedVideos", component: () => import("@/pages/video/PushVideo.vue") },
        { path: "following", component: () => import("@/pages/following/FollowingFeed.vue") },
        { path: "search", component: () => import("@/pages/search/SearchResult.vue") },
        { path: "my-videos", component: () => import("@/pages/myVideos/MyVideos.vue") },
        { path: "subscribe", component: () => import("@/pages/subscribe/Subscribe.vue") },
      ],
    },
  ],
});

export default router;
