import api from './request'

export interface ProductQuery {
  pageNum?: number
  pageSize?: number
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface Product {
  id: number
  userId: number
  title: string
  description: string
  categoryId: number
  price: number
  originalPrice?: number
  images: string[]
  location: string
  status: number
  viewCount: number
  createTime: string
}

export interface Category {
  id: number
  name: string
  parentId: number
  sortOrder: number
}

export function getProducts(params: ProductQuery): Promise<PageResult<Product>> {
  return api.get('/products', { params }).then((res: any) => res.data)
}

export function getProductById(id: number): Promise<any> {
  return api.get(`/products/${id}`).then((res: any) => res.data)
}

export function getCategories(): Promise<Category[]> {
  return api.get('/products/categories').then((res: any) => res.data || [])
}

export function searchProducts(params: ProductQuery): Promise<PageResult<Product>> {
  return api.get('/products/search', { params }).then((res: any) => res.data)
}

export function getUploadUrl(filename: string): Promise<any> {
  return api.get('/products/upload-url', { params: { filename } }).then((res: any) => res.data || res)
}

export function createProduct(data: any): Promise<any> {
  return api.post('/products', data).then((res: any) => res.data || res)
}

export function updateProduct(id: number, data: any): Promise<any> {
  return api.put(`/products/${id}`, data)
}

export function deleteProduct(id: number): Promise<any> {
  return api.delete(`/products/${id}`)
}