import axios from 'axios'
import { getToken, clearToken } from '../utils/token'
import router from '../router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      clearToken()
      router.push('/login')
    }
    return Promise.reject(error.response?.data || error)
  }
)

export default api