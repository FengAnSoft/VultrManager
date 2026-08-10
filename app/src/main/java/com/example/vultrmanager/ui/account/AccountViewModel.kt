package com.example.vultrmanager.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vultrmanager.data.VultrAuthException
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.remote.model.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val isLoading: Boolean = false,
    val account: Account? = null,
    val error: String? = null,
    /** True when the request failed because no (valid) API key is configured. */
    val needsApiKey: Boolean = false
)

class AccountViewModel(private val repository: VultrRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun loadAccount() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getAccount()
                .onSuccess { account ->
                    _uiState.update {
                        it.copy(isLoading = false, account = account, needsApiKey = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载失败",
                            needsApiKey = e is VultrAuthException
                        )
                    }
                }
        }
    }
}
