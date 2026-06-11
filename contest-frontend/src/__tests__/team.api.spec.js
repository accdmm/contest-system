import { describe, it, expect, vi, beforeEach } from 'vitest'

const mockRequest = {
  post: vi.fn(),
  get: vi.fn(),
  put: vi.fn(),
  delete: vi.fn(),
}

vi.mock('../api/request', () => ({
  default: mockRequest,
}))

const {
  createTeam,
  generateInviteCode,
  joinByInviteCode,
  approveMember,
  rejectMember,
  removeMember,
  dissolveTeam,
  submitTeamReview,
  getTeamById,
  listTeamMembers,
  listPendingMembers,
  getTeamsByLeader,
  leaveTeam,
  pageTeams,
  adminApproveTeam,
  adminRejectTeam,
  listUserTeams,
} = await import('../api/team')

describe('team API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('createTeam calls POST /team', () => {
    createTeam({ userId: 1, teamName: 'test' })
    expect(mockRequest.post).toHaveBeenCalledWith('/team', { userId: 1, teamName: 'test' })
  })

  it('generateInviteCode calls POST /team/{id}/invite', () => {
    generateInviteCode(1, 1)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/invite', { userId: 1 })
  })

  it('joinByInviteCode calls POST /team/join', () => {
    joinByInviteCode({ userId: 1, inviteCode: 'ABC' })
    expect(mockRequest.post).toHaveBeenCalledWith('/team/join', { userId: 1, inviteCode: 'ABC' })
  })

  it('approveMember calls POST', () => {
    approveMember(1, 2, 3)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/members/3/approve', null, { params: { userId: 2 } })
  })

  it('rejectMember calls POST', () => {
    rejectMember(1, 2, 3)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/members/3/reject', null, { params: { userId: 2 } })
  })

  it('removeMember calls POST', () => {
    removeMember(1, 2, 3)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/members/3', null, { params: { userId: 2 } })
  })

  it('dissolveTeam calls POST', () => {
    dissolveTeam(1, 2)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/dissolve', null, { params: { userId: 2 } })
  })

  it('submitTeamReview calls POST', () => {
    submitTeamReview(1, 2)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/submit', null, { params: { userId: 2 } })
  })

  it('getTeamById calls GET', () => {
    getTeamById(1)
    expect(mockRequest.get).toHaveBeenCalledWith('/team/1/detail')
  })

  it('listTeamMembers calls GET', () => {
    listTeamMembers(1)
    expect(mockRequest.get).toHaveBeenCalledWith('/team/1/members')
  })

  it('listPendingMembers calls GET', () => {
    listPendingMembers(1)
    expect(mockRequest.get).toHaveBeenCalledWith('/team/1/pending')
  })

  it('getTeamsByLeader calls GET /team/leader with userId only', () => {
    getTeamsByLeader(1)
    expect(mockRequest.get).toHaveBeenCalledWith('/team/leader', { params: { userId: 1 } })
  })

  it('leaveTeam calls POST', () => {
    leaveTeam(1, 2)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/leave', null, { params: { userId: 2 } })
  })

  it('pageTeams calls GET', () => {
    pageTeams({ page: 1, size: 10 })
    expect(mockRequest.get).toHaveBeenCalledWith('/team/page', { params: { page: 1, size: 10 } })
  })

  it('adminApproveTeam calls POST', () => {
    adminApproveTeam(1)
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/admin-approve')
  })

  it('adminRejectTeam calls POST', () => {
    adminRejectTeam(1, 'bad')
    expect(mockRequest.post).toHaveBeenCalledWith('/team/1/admin-reject', { reason: 'bad' })
  })

  it('listUserTeams calls GET', () => {
    listUserTeams(1)
    expect(mockRequest.get).toHaveBeenCalledWith('/team/user/1')
  })
})
