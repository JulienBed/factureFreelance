import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Invoice, CreateInvoiceRequest, InvoiceStatus } from '@/types'
import { invoiceService } from '@/services/invoice.service'

export const useInvoiceStore = defineStore('invoices', () => {
  const invoices = ref<Invoice[]>([])
  const currentInvoice = ref<Invoice | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const fetchInvoices = async (status?: InvoiceStatus) => {
    loading.value = true
    error.value = null
    try {
      invoices.value = await invoiceService.getInvoices(status)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to fetch invoices'
      throw e
    } finally {
      loading.value = false
    }
  }

  const fetchInvoice = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      currentInvoice.value = await invoiceService.getInvoice(id)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to fetch invoice'
      throw e
    } finally {
      loading.value = false
    }
  }

  const createInvoice = async (data: CreateInvoiceRequest) => {
    loading.value = true
    error.value = null
    try {
      const newInvoice = await invoiceService.createInvoice(data)
      invoices.value.push(newInvoice)
      return newInvoice
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to create invoice'
      throw e
    } finally {
      loading.value = false
    }
  }

  const updateInvoice = async (id: number, data: CreateInvoiceRequest) => {
    loading.value = true
    error.value = null
    try {
      const updatedInvoice = await invoiceService.updateInvoice(id, data)
      const index = invoices.value.findIndex((i) => i.id === id)
      if (index !== -1) {
        invoices.value[index] = updatedInvoice
      }
      return updatedInvoice
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to update invoice'
      throw e
    } finally {
      loading.value = false
    }
  }

  const updateInvoiceStatus = async (id: number, status: InvoiceStatus) => {
    loading.value = true
    error.value = null
    try {
      const updatedInvoice = await invoiceService.updateInvoiceStatus(id, status)
      const index = invoices.value.findIndex((i) => i.id === id)
      if (index !== -1) {
        invoices.value[index] = updatedInvoice
      }
      return updatedInvoice
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to update invoice status'
      throw e
    } finally {
      loading.value = false
    }
  }

  const deleteInvoice = async (id: number) => {
    loading.value = true
    error.value = null
    try {
      await invoiceService.deleteInvoice(id)
      invoices.value = invoices.value.filter((i) => i.id !== id)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to delete invoice'
      throw e
    } finally {
      loading.value = false
    }
  }

  const downloadPdf = async (id: number, invoiceNumber: string) => {
    loading.value = true
    error.value = null
    try {
      await invoiceService.downloadPdf(id, invoiceNumber)
    } catch (e: any) {
      error.value = e.response?.data?.message || 'Failed to download PDF'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    invoices,
    currentInvoice,
    loading,
    error,
    fetchInvoices,
    fetchInvoice,
    createInvoice,
    updateInvoice,
    updateInvoiceStatus,
    deleteInvoice,
    downloadPdf
  }
})
