package com.demoapp.feature_jobs.presentation.models

import java.util.Date

data class InvoiceData(
    val id: String,
    val jobId: String,
    val jobTitle: String,
    val clientName: String,
    val clientEmail: String,
    val clientAddress: String,
    val serviceDescription: String,
    val amount: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val completionDate: Date,
    val invoiceDate: Date = Date(),
    val notes: String = ""
)

enum class InvoiceStatus {
    PENDING,
    SENT,
    PAID,
    OVERDUE
}
