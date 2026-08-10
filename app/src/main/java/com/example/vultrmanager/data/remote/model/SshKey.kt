package com.example.vultrmanager.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * An SSH key registered on the Vultr account, from GET /ssh-keys -> { ssh_keys: [...] }.
 * See: https://www.vultr.com/api/#operation/get-ssh-keys
 */
data class SshKey(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("date_created") val dateCreated: String? = null
)

data class SshKeysResponse(
    @SerializedName("ssh_keys") val sshKeys: List<SshKey>
)
