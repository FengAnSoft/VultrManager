package com.example.vultrmanager.ui.instances

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vultrmanager.data.VultrAuthException
import com.example.vultrmanager.data.VultrRepository
import com.example.vultrmanager.data.remote.model.Instance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Ordering choices for the instance list. */
enum class InstanceSortOrder {
    NAME_ASC, NAME_DESC, STATUS, CREATED
}

data class InstanceListUiState(
    val isLoading: Boolean = false,
    val instances: List<Instance> = emptyList(),
    /** Free-text query matched against name / IP / region. */
    val searchQuery: String = "",
    /** Status category filter; null means "show all". See [statusCategory]. */
    val statusFilter: String? = null,
    val sortOrder: InstanceSortOrder = InstanceSortOrder.NAME_ASC,
    val error: String? = null,
    /** True when the request failed because no (valid) API key is configured. */
    val needsApiKey: Boolean = false
)

class InstanceListViewModel(private val repository: VultrRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(InstanceListUiState())
    val uiState: StateFlow<InstanceListUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onStatusFilterChange(category: String?) {
        _uiState.update { it.copy(statusFilter = category) }
    }

    fun onSortOrderChange(order: InstanceSortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun loadInstances() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.listInstances()
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            instances = list,
                            error = null,
                            needsApiKey = false
                        )
                    }
                }
                .onFailure { e ->
                    val needsKey = e is VultrAuthException
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载失败",
                            needsApiKey = needsKey
                        )
                    }
                }
        }
    }
}
