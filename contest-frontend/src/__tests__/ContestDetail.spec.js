import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createTestingPinia } from '@pinia/testing'

vi.mock('../api/contest', () => ({
  getContestById: vi.fn().mockResolvedValue({
    data: {
      id: 1,
      name: '测试竞赛',
      status: 1,
      contestType: 2,
      category: '理工类',
      level: '校级',
      organizer: '计算机学院',
      description: '<p>竞赛详情</p>',
      contestTime: '2026-06-15T09:00:00',
      registerEndTime: '2026-06-01T23:59:59',
      location: '线上',
      currentCount: 10,
    },
  }),
}))

vi.mock('../api/registration', () => ({
  registerPersonal: vi.fn(),
  registerTeam: vi.fn(),
  approveRegistration: vi.fn(),
  rejectRegistration: vi.fn(),
  cancelRegistration: vi.fn(),
  pageRegistrationByUser: vi.fn().mockResolvedValue({ data: { records: [] } }),
  pageRegistrationByContest: vi.fn().mockResolvedValue({ data: { records: [] } }),
}))

vi.mock('../api/team', () => ({
  listUserTeams: vi.fn().mockResolvedValue({
    data: [
      { id: 1, teamName: '团队A' },
      { id: 2, teamName: '团队B' },
    ],
  }),
  getTeamById: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '1' } }),
  useRouter: () => ({ push: vi.fn() }),
}))

import ContestDetail from '../views/contest/ContestDetail.vue'

describe('ContestDetail.vue', () => {
  let wrapper

  beforeEach(async () => {
    wrapper = mount(ContestDetail, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: { user: { user: { id: 1, name: 'test' }, token: 'test' } },
          }),
          ElementPlus,
        ],
        stubs: { NavBar: true, 'router-link': { template: '<a><slot /></a>' } },
      },
    })
    await flushPromises()
  })

  it('renders contest name', () => {
    expect(wrapper.text()).toContain('测试竞赛')
  })

  it('shows team registration section', async () => {
    expect(wrapper.find('.team-info').exists()).toBe(true)
  })

  it('shows team registration button', () => {
    expect(wrapper.text()).toContain('以团队报名')
  })

  it('shows create team link', () => {
    expect(wrapper.text()).toContain('创建新团队')
  })
})
