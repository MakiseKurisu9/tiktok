<template>
  <div>
    <v-menu
      v-if="isLoggedIn"
      location="bottom end"
      :close-on-content-click="true"
    >
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-avatar size="36">
            <v-img :src="userAvatar" />
            <!-- ← was defaultAvatar -->
          </v-avatar>
        </v-btn>
      </template>
      <v-list min-width="200">
        <v-list-item>
          <v-list-item-title class="font-weight-medium">{{
            userStore.nickname || "用户"
          }}</v-list-item-title>
          <v-list-item-subtitle>{{ userStore.email }}</v-list-item-subtitle>
        </v-list-item>
        <v-divider />
        <v-list-item
          prepend-icon="mdi-account"
          title="个人中心"
          @click="goProfile"
        />
        <v-list-item
          prepend-icon="mdi-video-box"
          title="我的视频"
          @click="goMyVideos"
        />
        <v-list-item
          prepend-icon="mdi-cog"
          title="订阅管理"
          @click="goSubscribe"
        />
        <v-divider />
        <v-list-item
          prepend-icon="mdi-logout"
          title="退出登录"
          @click="logout"
        />
      </v-list>
    </v-menu>
    <v-btn v-else variant="outlined" color="primary" @click="goLogin">
      登录
    </v-btn>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useTokenStore } from "@/pinia/token";
import { useUserStore } from "@/pinia/user";

const router = useRouter();
const tokenStore = useTokenStore();
const userStore = useUserStore();

const defaultAvatar = new URL("@/assets/default-avatar.png", import.meta.url)
  .href;
const userAvatar = computed(() => userStore.avatarSource || defaultAvatar); // ← add this

const isLoggedIn = computed(() => !!tokenStore.token);

function goLogin() {
  router.push("/login");
}
function goProfile() {
  router.push("/customer/profile");
}
function goMyVideos() {
  router.push("/my-videos");
}
function goSubscribe() {
  router.push("/subscribe");
}

function logout() {
  tokenStore.removeToken();
  userStore.removeUser();
  router.push("/login");
}
</script>
