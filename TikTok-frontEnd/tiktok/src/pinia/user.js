import { defineStore } from "pinia";
import { ref } from "vue";

export const useUserStore = defineStore(
  "user",
  () => {
    const userId = ref(null);
    const nickname = ref("");
    const email = ref("");

    const setUser = (user) => {
      userId.value = user.id;
      nickname.value = user.nickname;
      email.value = user.email;
    };

    const removeUser = () => {
      userId.value = null;
      nickname.value = "";
      email.value = "";
    };

    return {
      userId,
      nickname,
      email,
      setUser,
      removeUser,
    };
  },
  {
    persist: {
      storage: localStorage, // 可选用 sessionStorage
      paths: ["userId", "nickname", "email"],
    },
  }
);
