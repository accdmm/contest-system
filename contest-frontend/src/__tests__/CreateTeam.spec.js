import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createTestingPinia } from '@pinia/testing'
import { ElMessage } from 'element-plus'

vi.mock('../api/team', () => ({
  createTeam: vi.fn().mockResolvedValue({ data: { id: 1 } }),
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
}))

vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: { success: vi.fn(), warning: vi.fn() },
  }
})

import CreateTeam from '../views/team/CreateTeam.vue'

describe('CreateTeam.vue', () => {
  let wrapper

  beforeEach(() => {
    wrapper = mount(CreateTeam, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn }), ElementPlus],
        stubs: { NavBar: true },
      },
    })
  })

  it('renders the create team form', () => {
    expect(wrapper.text()).toContain('创建团队')
    expect(wrapper.text()).toContain('团队名称')
  })

  it('does not show contest selection', () => {
    expect(wrapper.text()).not.toContain('选择竞赛')
  })

  it('shows warning when submitting with empty name', async () => {
    const btn = wrapper.find('.submit-btn')
    await btn.trigger('click')
    expect(ElMessage.warning).toHaveBeenCalledWith('请输入团队名称')
  })

  it('renders submit button', () => {
    const btn = wrapper.find('.submit-btn')
    expect(btn.exists()).toBe(true)
    expect(btn.text()).toContain('创建团队')
  })
})
