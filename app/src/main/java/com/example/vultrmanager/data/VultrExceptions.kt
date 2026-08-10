package com.example.vultrmanager.data

/** The API key is missing / invalid. The UI should prompt the user to set it. */
class VultrAuthException(message: String) : Exception(message)

/** A non-2xx response from the Vultr API. [code] is the HTTP status code. */
class VultrApiException(message: String, val code: Int = -1) : Exception(message)

/** Network-level failure (no connection, timeout, DNS, etc.). */
class VultrNetworkException(message: String) : Exception(message)
