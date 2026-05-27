import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import { createTestingPinia } from '@pinia/testing'

vi.mock('../api/team', () => ({
  getTeamById: vi.fn().mockResolvedValue({
    data: {
      id: 1,
      teamName: '测试团队',
      teamNo: 'T20260501001',
      status: 0,
      memberCount: 1,
      leaderId: 1,
      inviteCode: '',
    },
  }),
  listTeamMembers: vi.fn().mockResolvedValue({ data: [] }),
  listPendingMembers: vi.fn().mockResolvedValue({ data: [] }),
  generateInviteCode: vi.fn(),
  approveMember: vi.fn(),
  rejectMember: vi.fn(),
  removeMember: vi.fn(),
  dissolveTeam: vi.fn(),
  submitTeamReview: vi.fn(),
  joinByInviteCode: vi.fn(),
  leaveTeam: vi.fn(),
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '1' } }),
  useRouter: () => ({ push: vi.fn() }),
}))

import TeamDetail from '../views/team/TeamDetail.vue'

function flushPromises() {
  return new Promise(resolve => setTimeout(resolve, 50))
}

describe('TeamDetail.vue', () => {
  let wrapper

  beforeEach(async () => {
    wrapper = mount(TeamDetail, {
      global: {
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            initialState: { user: { user: { id: 1, name: 'test' }, token: 'test' } },
          }),
          ElementPlus,
        ],
        stubs: { NavBar: true, 'router-link': true },
      },
    })
    await flushPromises()
  })

  it('renders team name', () => {
    expect(wrapper.text()).toContain('测试团队')
  })

  it('does not show contest name', () => {
    expect(wrapper.text()).not.toContain('未知竞赛')
  })

  it('shows team number', () => {
    expect(wrapper.text()).toContain('T20260501001')
  })

  it('shows team status', () => {
    expect(wrapper.text()).toContain('组建中')
  })

  it('shows submit review button for leader', () => {
    expect(wrapper.text()).toContain('提交报名审核')
  })
})
