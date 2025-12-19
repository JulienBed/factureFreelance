import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Client, CreateClientRequest } from '@/types'
import { clientService } from '@/services/client.service'

export const useClientStore = defineStore('clients', () => {
  const clients = ref<Client[]>([])
  const currentClient = ref<Client | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchClients = async (search?: string) => {
    loading.value = true
    error.value = null
    try {
      clients.value = await clientService.getClients(search)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to fetch clients'
      throw e
    } finally {
      loading.value = false
    }
  }

  const fetchClient = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      currentClient.value = await clientService.getClient(id)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to fetch client'
      throw e
    } finally {
      loading.value = false
    }
  }

  const createClient = async (data: CreateClientRequest) => {
    loading.value = true
    error.value = null
    try {
      const newClient = await clientService.createClient(data)
      clients.value.push(newClient)
      return newClient
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to create client'
      throw e
    } finally {
      loading.value = false
    }
  }

  const updateClient = async (id: number, data: CreateClientRequest) => {
    loading.value = true
    error.value = null
    try {
      const updatedClient = await clientService.updateClient(id, data)
      const index = clients.value.findIndex((c) => c.id === id)
      if (index !== -1) {
        clients.value[index] = updatedClient
      }
      return updatedClient
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to update client'
      throw e
    } finally {
      loading.value = false
    }
  }

  const deleteClient = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      await clientService.deleteClient(id)
      clients.value = clients.value.filter((c) => c.id !== id)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to delete client'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    clients,
    currentClient,
    loading,
    error,
    fetchClients,
    fetchClient,
    createClient,
    updateClient,
    deleteClient
  }
})
