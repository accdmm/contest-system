<template>
  <div>
    <NavBar />

    <el-carousel height="400px" v-if="banners.length">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <a v-if="b.linkUrl" :href="b.linkUrl" target="_blank">
          <img :src="b.imageUrl" class="banner-img" />
        </a>
        <img v-else :src="b.imageUrl" class="banner-img" />
      </el-carousel-item>
    </el-carousel>

    <div class="container">
      <h2>热门竞赛</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="c in hotContests" :key="c.id">
          <ContestCard :contest="c" />
        </el-col>
      </el-row>

      <h2>最新竞赛</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="c in latestContests" :key="c.id">
          <ContestCard :contest="c" />
        </el-col>
      </el-row>

      <h2>公告</h2>
      <el-timeline>
        <el-timeline-item v-for="a in announcements" :key="a.id" :timestamp="a.publishTime || a.createTime">
          {{ a.title }}
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import NavBar from '../../components/NavBar.vue'
import ContestCard from '../../components/ContestCard.vue'
import { listBanners, listAnnouncements } from '../../api/cms'
import { getHotContests, getLatestContests } from '../../api/contest'

const banners = ref([])
const hotContests = ref([])
const latestContests = ref([])
const announcements = ref([])

onMounted(async () => {
  try {
    const [bRes, aRes, hotRes, latestRes] = await Promise.all([
      listBanners(),
      listAnnouncements(),
      getHotContests(4),
      getLatestContests(4)
    ])
    banners.value = bRes.data || []
    announcements.value = aRes.data || []
    hotContests.value = hotRes.data || []
    latestContests.value = latestRes.data || []
  } catch (e) {
    // ignore
  }
})
</script>

<style scoped>
.banner-img { width: 100%; height: 400px; object-fit: cover; }
.container { max-width: 1200px; margin: 20px auto; padding: 0 20px; }
.container h2 { margin: 30px 0 15px; }
</style>
