export interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  companyName?: string
  siret?: string
  phone?: string
}

export interface Client {
  id: number
  companyName: string
  contactName?: string
  email: string
  phone?: string
  addressStreet?: string
  addressCity?: string
  addressPostalCode?: string
  addressCountry?: string
  siret?: string
  tvaNumber?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

export interface InvoiceItem {
  id?: number
  description: string
  quantity: number
  unitPrice: number
  taxRate: number
  amount?: number
}

export enum InvoiceStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

export interface Invoice {
  id: number
  clientId: number
  clientName: string
  invoiceNumber: string
  status: InvoiceStatus
  issueDate: string
  dueDate: string
  paidDate?: string
  subtotal: number
  taxRate: number
  taxAmount: number
  total: number
  currency: string
  notes?: string
  paymentTerms?: string
  items: InvoiceItem[]
  createdAt: string
  updatedAt: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface VerifyOtpRequest {
  email: string
  otp: string
}

export interface CreateClientRequest {
  companyName: string
  contactName?: string
  email: string
  phone?: string
  addressStreet?: string
  addressCity?: string
  addressPostalCode?: string
  addressCountry?: string
  siret?: string
  tvaNumber?: string
  notes?: string
}

export interface CreateInvoiceRequest {
  clientId: number
  issueDate: string
  dueDate: string
  status?: InvoiceStatus
  taxRate?: number
  notes?: string
  paymentTerms?: string
  items: InvoiceItem[]
}
