<template>
  <section class="hero anim-fade" :style="heroStyle">
    <div class="hero-bg">
      <div class="hero-shape hero-shape--1"></div>
      <div class="hero-shape hero-shape--2"></div>
      <div class="hero-shape hero-shape--3"></div>
      <div class="hero-stars"></div>
    </div>
    <div class="container hero-inner">
      <slot>
        <div class="hero-content">
          <span v-if="badge" class="hero-badge">{{ badge }}</span>
          <h1 class="hero-title">{{ title }}</h1>
          <p v-if="subtitle" class="hero-subtitle">{{ subtitle }}</p>
          <div v-if="showActions" class="hero-actions">
            <el-button
              v-if="primaryAction"
              type="primary"
              size="large"
              round
              @click="handleAction(primaryAction)"
            >{{ primaryText }}</el-button>
            <el-button
              v-if="secondaryAction"
              size="large"
              round
              @click="handleAction(secondaryAction)"
            >{{ secondaryText }}</el-button>
          </div>
        </div>
      </slot>
      <div v-if="showDecoration" class="hero-decoration anim-slide-right anim-delay-2">
        <div class="hero-decoration-ring"></div>
        <div class="hero-decoration-dots"></div>
        <div class="orbit orbit--1"><span class="orbit-dot"></span></div>
        <div class="orbit orbit--2"><span class="orbit-dot"></span></div>
        <div class="orbit orbit--3"><span class="orbit-dot"></span></div>
        <div class="orbit orbit--4"><span class="orbit-dot"></span></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  badge: { type: String, default: '' },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  showActions: { type: Boolean, default: false },
  showDecoration: { type: Boolean, default: true },
  primaryText: { type: String, default: '' },
  primaryAction: { type: [String, Function], default: null },
  secondaryText: { type: String, default: '' },
  secondaryAction: { type: [String, Function], default: null },
  heroStyle: { type: Object, default: null },
})

const router = useRouter()

function handleAction(action) {
  if (typeof action === 'string') {
    router.push(action)
  } else if (typeof action === 'function') {
    action()
  }
}
</script>

<style>
.hero {
  position: relative;
  padding: 100px 0 80px;
  overflow: hidden;
  background: linear-gradient(160deg, #0a1018 0%, #12102a 40%, #0a1018 100%);
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.04'/%3E%3C/svg%3E");
  opacity: 0.4;
  pointer-events: none;
  z-index: 1;
}

.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 40%, rgba(168,85,247,0.12) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 60%, rgba(201,168,76,0.10) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 20%, rgba(232,93,74,0.08) 0%, transparent 40%);
  animation: nebula-drift 20s ease-in-out infinite alternate;
  pointer-events: none;
  z-index: 0;
}

.hero > * {
  position: relative;
  z-index: 2;
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
}

.hero-shape--1 {
  width: 600px;
  height: 600px;
  background: var(--c-gold);
  top: -200px;
  right: -100px;
  opacity: 0.04;
}

.hero-shape--2 {
  width: 500px;
  height: 500px;
  border: 1px solid rgba(201, 168, 76, 0.08);
  top: 50%;
  left: -200px;
  transform: translateY(-50%);
  background: none;
}

.hero-shape--3 {
  width: 200px;
  height: 200px;
  background: var(--c-accent);
  bottom: 20%;
  right: 30%;
  opacity: 0.06;
}

.hero-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
}

.hero-content {
  flex: 1;
  max-width: 640px;
}

.hero-badge {
  display: inline-block;
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--c-accent);
  background: rgba(232, 93, 74, 0.12);
  padding: 6px 16px;
  border-radius: 20px;
  margin-bottom: 20px;
}

.hero-title {
  font-family: 'DM Serif Display', serif;
  font-size: 3.5rem;
  color: #fff;
  line-height: 1.15;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.65);
  line-height: 1.7;
  margin-bottom: 32px;
  max-width: 520px;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.hero-actions .el-button {
  padding: 12px 32px;
  font-size: 0.95rem;
}

.hero-actions .el-button--primary {
  background: var(--c-accent) !important;
}

.hero-actions .el-button--primary:hover {
  background: var(--c-accent-light) !important;
}

.hero-actions .el-button:not(.el-button--primary) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
  border: 1px solid rgba(255, 255, 255, 0.15) !important;
}

.hero-actions .el-button:not(.el-button--primary):hover {
  background: rgba(255, 255, 255, 0.14) !important;
}

.hero-decoration {
  flex-shrink: 0;
  position: relative;
  width: 340px;
  height: 340px;
}

.hero-decoration::before {
  content: '';
  position: absolute;
  top: -60px;
  left: -60px;
  width: 460px;
  height: 460px;
  border: 1.5px solid rgba(168, 85, 247, 0.12);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 35s linear infinite;
}

.hero-decoration-ring {
  width: 340px;
  height: 340px;
  border: 1.5px solid rgba(201, 168, 76, 0.2);
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
  transform-origin: center;
  animation: orbit-spin 25s linear infinite reverse;
}

.hero-decoration-ring::before {
  content: '';
  position: absolute;
  inset: 40px;
  border: 1.5px dashed rgba(201, 168, 76, 0.15);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 20s linear infinite;
}

.hero-decoration-ring::after {
  content: '';
  position: absolute;
  inset: 80px;
  background: rgba(168, 85, 247, 0.08);
  border-radius: 50%;
  transform-origin: center;
  animation: orbit-spin 15s linear infinite reverse;
}

.hero-decoration-dots {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: var(--c-gold);
  border-radius: 50%;
  box-shadow:
    24px 24px 0 rgba(201, 168, 76, 0.25),
    -24px -24px 0 rgba(201, 168, 76, 0.25),
    24px -24px 0 rgba(201, 168, 76, 0.15),
    -24px 24px 0 rgba(201, 168, 76, 0.15),
    0 -36px 0 rgba(255, 255, 255, 0.08),
    0 36px 0 rgba(255, 255, 255, 0.08);
}

/* === Orbiting Nodes === */
.orbit {
  position: absolute;
  inset: 0;
  pointer-events: none;
  transform-origin: center;
}

.orbit-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 7px;
  height: 7px;
  margin-left: -3.5px;
  border-radius: 50%;
  animation: orbit-pulse 4s ease-in-out infinite;
}

.orbit--1 { animation: orbit-spin 35s linear infinite; }
.orbit--1 .orbit-dot { margin-top: calc(-230px - 3.5px); background: rgba(168,85,247,0.7); box-shadow: 0 0 8px rgba(168,85,247,0.4); }

.orbit--2 { animation: orbit-spin 25s linear infinite reverse; animation-delay: -6s; }
.orbit--2 .orbit-dot { margin-top: calc(-170px - 3.5px); background: rgba(201,168,76,0.7); box-shadow: 0 0 8px rgba(201,168,76,0.4); }

.orbit--3 { animation: orbit-spin 20s linear infinite; animation-delay: -5s; }
.orbit--3 .orbit-dot { margin-top: calc(-130px - 3.5px); background: rgba(201,168,76,0.6); box-shadow: 0 0 6px rgba(201,168,76,0.3); }

.orbit--4 { animation: orbit-spin 15s linear infinite reverse; animation-delay: -4s; }
.orbit--4 .orbit-dot { margin-top: calc(-90px - 3.5px); background: rgba(168,85,247,0.6); box-shadow: 0 0 6px rgba(168,85,247,0.3); }

@keyframes orbit-pulse {
  0%, 100% { opacity: 0.5; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* === Stars === */
.hero-stars {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-stars::before {
  content: '';
  position: absolute;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #fff;
  animation: star-twinkle 4s ease-in-out infinite;
  box-shadow:
    80px 60px 0 0 rgba(255,255,255,0.6),
    200px 30px 0 0 rgba(255,255,255,0.5),
    350px 80px 0 0 rgba(201,168,76,0.5),
    500px 45px 0 0 rgba(255,255,255,0.7),
    650px 100px 0 0 rgba(200,168,255,0.5),
    100px 150px 0 0 rgba(255,255,255,0.4),
    300px 180px 0 0 rgba(201,168,76,0.4),
    550px 200px 0 0 rgba(255,255,255,0.6),
    700px 160px 0 0 rgba(200,168,255,0.4),
    150px 250px 0 0 rgba(255,255,255,0.5),
    400px 280px 0 0 rgba(201,168,76,0.4),
    600px 300px 0 0 rgba(255,255,255,0.5),
    50px 320px 0 0 rgba(200,168,255,0.4),
    250px 350px 0 0 rgba(255,255,255,0.6),
    500px 380px 0 0 rgba(201,168,76,0.4),
    680px 340px 0 0 rgba(255,255,255,0.5),
    180px 420px 0 0 rgba(200,168,255,0.4),
    450px 450px 0 0 rgba(255,255,255,0.5);
}

.hero-stars::after {
  content: '';
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(201, 168, 76, 0.6);
  animation: star-twinkle 7s ease-in-out infinite 1s;
  box-shadow:
    150px 100px 0 0,
    400px 50px 0 0,
    600px 250px 0 0,
    80px 400px 0 0,
    500px 420px 0 0;
}

/* ===== Animations ===== */
@keyframes nebula-drift {
  0% { transform: scale(1) translate(0, 0); opacity: 0.6; }
  50% { transform: scale(1.05) translate(-1%, 1%); opacity: 1; }
  100% { transform: scale(1) translate(1%, -1%); opacity: 0.6; }
}

@keyframes orbit-spin {
  to { transform: rotate(360deg); }
}

@keyframes star-twinkle {
  0%, 100% { opacity: 0.2; }
  40% { opacity: 1; }
  70% { opacity: 0.2; }
}

@media (max-width: 1024px) {
  .hero-title {
    font-size: 2.5rem;
  }
  .hero-decoration {
    display: none;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: 60px 0 60px;
  }
  .hero-title {
    font-size: 2rem;
  }
  .hero-actions {
    flex-direction: column;
  }
}
</style>
