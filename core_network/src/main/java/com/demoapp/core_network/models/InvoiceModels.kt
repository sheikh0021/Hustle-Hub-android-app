package com.demoapp.core_network.models

data class InvoiceCreateResponse(
    val error: Boolean,
    val message: String,
    val success: Boolean,
    val data: InvoiceData?
)

data class InvoiceData(
    val id: Int,
    val task: Int,
    val task_title: String,
    val worker: Int,
    val worker_name: String,
    val invoice_number: String,
    val amount: String,
    val status: String,
    val created_at: String,
    val paid_at: String?
)


