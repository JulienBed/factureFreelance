import api from './api'
import type { Invoice, CreateInvoiceRequest, InvoiceStatus } from '@/types'

export const invoiceService = {
  async getInvoices(status?: InvoiceStatus): Promise<Invoice[]> {
    const params = status ? { status } : {}
    const response = await api.get('/invoices', { params })
    return response.data
  },

  async getInvoice(id: number): Promise<Invoice> {
    const response = await api.get(`/invoices/${id}`)
    return response.data
  },

  async createInvoice(data: CreateInvoiceRequest): Promise<Invoice> {
    const response = await api.post('/invoices', data)
    return response.data
  },

  async updateInvoice(id: number, data: CreateInvoiceRequest): Promise<Invoice> {
    const response = await api.put(`/invoices/${id}`, data)
    return response.data
  },

  async updateInvoiceStatus(id: number, status: InvoiceStatus): Promise<Invoice> {
    const response = await api.put(`/invoices/${id}/status`, null, { params: { status } })
    return response.data
  },

  async deleteInvoice(id: number): Promise<void> {
    await api.delete(`/invoices/${id}`)
  }
}
