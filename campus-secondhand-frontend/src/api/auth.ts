import api from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
}

export interface LoginResponse {
  code: number
  message: string
  data: {
    token: string
    user: {
      id: number
      username: string
      nickname: string
      avatarUrl?: string
      creditScore: number
    }
  }
}

export function login(data: LoginRequest): Promise<LoginResponse> {
  return api.post('/auth/login', data)
}

export function register(data: RegisterRequest): Promise<any> {
  return api.post('/auth/register', data)
}

export function logout(): Promise<any> {
  return api.post('/auth/logout')
}

export function getUserInfo(): Promise<any> {
  return api.get('/user/info')
}

export function updateUser(data: any): Promise<any> {
  return api.put('/user/update', data)
}