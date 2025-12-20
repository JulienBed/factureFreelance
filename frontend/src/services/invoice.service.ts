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
  },

  async downloadPdf(id: number, invoiceNumber: string): Promise<void> {
    const response = await api.get(`/invoices/${id}/pdf`, {
      responseType: 'blob'
    })

    // Create a blob URL and trigger download
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${invoiceNumber}_Factur-X.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}
