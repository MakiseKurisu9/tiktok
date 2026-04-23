<template>
  <v-container class="px-4 py-6">
    <div class="mb-8">
      <h1 class="text-h4 font-weight-bold mb-2 gradient-text">订阅管理</h1>
      <p class="text-body-1 text-medium-emphasis">
        选择你感兴趣的视频分类，获取个性化推荐
      </p>
    </div>

    <div v-if="loading" class="text-center py-16">
      <v-progress-circular indeterminate color="primary" size="48" width="4" />
    </div>

    <v-card v-else elevation="2" class="pa-6">
      <h3 class="text-h6 mb-4">选择你感兴趣的分类</h3>
      <v-chip-group v-model="selectedTypes" column multiple>
        <v-chip
          v-for="type in allTypes"
          :key="type.id"
          :value="type.id"
          filter
          variant="outlined"
          size="large"
          class="ma-1"
        >
          {{ type.name }}
        </v-chip>
      </v-chip-group>

      <div class="mt-6">
        <v-btn
          color="primary"
          size="large"
          @click="saveSubscriptions"
          :loading="saving"
        >
          保存订阅
        </v-btn>
      </div>
    </v-card>

    <v-snackbar v-model="snack" :color="snackColor" :timeout="3000">{{
      snackMsg
    }}</v-snackbar>
  </v-container>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getAllTypes } from "@/api";
import { getSubscribe, subscribeVideoTypes } from "@/api/customer";

const allTypes = ref([]);
const selectedTypes = ref([]);
const loading = ref(true);
const saving = ref(false);
const snack = ref(false);
const snackMsg = ref("");
const snackColor = ref("success");

function showMessage(msg, color = "success") {
  snackMsg.value = msg;
  snackColor.value = color;
  snack.value = true;
}

onMounted(async () => {
  try {
    const [typesRes, subRes] = await Promise.all([
      getAllTypes(),
      getSubscribe(),
    ]);
    allTypes.value = typesRes.data || [];
    const subscribed = subRes.data || [];
    // subscribed could be array of type objects or strings
    selectedTypes.value = subscribed.map((s) => s.name || s.typeName || s);
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
});

async function saveSubscriptions() {
  saving.value = true;
  try {
    const typesStr = selectedTypes.value.join(",");
    await subscribeVideoTypes(typesStr);
    showMessage("订阅保存成功");
  } catch (e) {
    showMessage("保存失败", "error");
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.gradient-text {
  background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
