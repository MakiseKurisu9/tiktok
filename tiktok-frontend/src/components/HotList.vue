<template>
  <v-list density="compact" class="hot-list">
    <v-list-item
      v-for="(item, index) in hotList"
      :key="item.videoId || index"
      class="hot-item"
      @click="$emit('select', item)"
    >
      <template v-slot:prepend>
        <span class="rank-number mr-3" :class="{ 'top-rank': index < 3 }">
          {{ index + 1 }}
        </span>
      </template>
      <v-list-item-title class="text-body-2">
        {{ item.title || item.videoTitle || `热搜 #${index + 1}` }}
      </v-list-item-title>
      <template v-slot:append>
        <v-chip size="small" variant="text" color="grey">
          {{ formatHot(item.hotScore || item.score || 0) }}热力指数
        </v-chip>
      </template>
    </v-list-item>
    <div v-if="hotList.length === 0" class="text-center pa-4">
      <p class="text-body-2 text-medium-emphasis">暂无热搜数据</p>
    </div>
  </v-list>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getHotRank } from "@/api";

defineEmits(["select"]);

const hotList = ref([]);

function formatHot(score) {
  if (score >= 10000) return `${(score / 10000).toFixed(1)}万`;
  if (score >= 1000) return `${(score / 1000).toFixed(1)}k`;
  return score.toString();
}

onMounted(async () => {
  try {
    const res = await getHotRank();
    hotList.value = res.data || [];
  } catch (e) {
    console.error("Failed to load hot rank:", e);
  }
});
</script>

<style scoped>
.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: bold;
  color: grey;
}
.top-rank {
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  color: white;
  border-radius: 6px;
}
</style>
