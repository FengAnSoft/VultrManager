package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Vultr account + billing info, from GET /account -> { account: {...} }.
 * See: https://www.vultr.com/api/#operation/get-account
 *
 * Note: `balance` is the account balance; Vultr reports it as a negative number
 * when there is available credit (e.g. -5.00 means $5.00 available).
 */
data class Account(
    @SerializedName("balance") val balance: Double? = null,
    @SerializedName("pending_charges") val pendingCharges: Double? = null,
    @SerializedName("last_payment_date") val lastPaymentDate: String? = null,
    @SerializedName("last_payment_amount") val lastPaymentAmount: Double? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("acls") val acls: List<String>? = null
)

data class AccountResponse(
    @SerializedName("account") val account: Account
)
