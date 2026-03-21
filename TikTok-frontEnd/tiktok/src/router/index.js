import { createRouter, createWebHistory } from "vue-router";
import Login from "@/pages/Login.vue";
import FindPassword from "@/pages/FindPassword.vue";
import AppLayout from "@/components/AppLayout.vue";
import HotVideo from "@/pages/hotVideo/HotVideo.vue";
import VideoType from "@/pages/videoType/VideoType.vue";
import profile from "@/pages/customer/profile.vue";
import PushVideo from "@/pages/video/PushVideo.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      component: Login,
    },
    {
      path: "/findPassword",
      component: FindPassword,
    },
    {
      path: "/",
      redirect: "/index/hotVideos",
      component: AppLayout,
      children: [
        { path: "index/hotVideos", component: HotVideo },
        { path: "videoType/:id", component: VideoType, props: true },
        { path: "customer/profile", component: profile },
        { path: "index/pushedVideos", component: PushVideo },
      ],
    },
  ],
});

export default router;
