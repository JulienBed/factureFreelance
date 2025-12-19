import api from './api'
import type { Client, CreateClientRequest } from '@/types'

export const clientService = {
  async getClients(search?: string): Promise<Client[]> {
    const params = search ? { search } : {}
    const response = await api.get('/clients', { params })
    return response.data
  },

  async getClient(id: number): Promise<Client> {
    const response = await api.get(`/clients/${id}`)
    return response.data
  },

  async createClient(data: CreateClientRequest): Promise<Client> {
    const response = await api.post('/clients', data)
    return response.data
  },

  async updateClient(id: number, data: CreateClientRequest): Promise<Client> {
    const response = await api.put(`/clients/${id}`, data)
    return response.data
  },

  async deleteClient(id: number): Promise<void> {
    await api.delete(`/clients/${id}`)
  }
}
