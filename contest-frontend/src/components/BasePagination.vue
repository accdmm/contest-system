<template>
  <div v-if="total > 0" class="pagination-wrap">
    <el-pagination
      background
      :layout="layout"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      :small="small"
      @current-change="$emit('change', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, required: true },
  pageSize: { type: Number, default: 10 },
  currentPage: { type: Number, default: 1 },
  layout: { type: String, default: 'prev, pager, next' },
  small: { type: Boolean, default: false },
})

defineEmits(['change'])
</script>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
