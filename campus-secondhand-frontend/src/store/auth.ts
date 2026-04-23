import { defineStore } from 'pinia'
import { login, register, logout, getUserInfo } from '../api/auth'
import { getToken, setToken, clearToken, setUser } from '../utils/token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: null as any,
    isLoggedIn: !!getToken()
  }),

  actions: {
    async loginAction(username: string, password: string) {
      const res: any = await login({ username, password })
      if (res.code === 200 && res.data) {
        this.token = res.data.token
        this.user = res.data.user
        this.isLoggedIn = true
        setToken(res.data.token)
        setUser(res.data.user)
      }
      return res
    },

    async registerAction(username: string, password: string, nickname: string) {
      const res: any = await register({ username, password, nickname })
      return res
    },

    async logoutAction() {
      await logout()
      this.token = null
      this.user = null
      this.isLoggedIn = false
      clearToken()
    },

    async fetchUser() {
      if (!this.token) return
      try {
        const res: any = await getUserInfo()
        if (res.code === 200) {
          this.user = res.data
          setUser(res.data)
        }
      } catch (e) {
        this.logoutAction()
      }
    }
  }
})