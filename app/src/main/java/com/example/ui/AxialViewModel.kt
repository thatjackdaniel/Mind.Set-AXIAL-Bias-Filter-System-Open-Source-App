package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AxialAnalysisResult
import com.example.data.AxialRepository
import com.example.data.AxialSignalEntity
import com.example.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AxialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AxialRepository(application)

    private val _inputSignal = MutableStateFlow("")
    val inputSignal: StateFlow<String> = _inputSignal.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<AxialAnalysisResult?>(null)
    val analysisResult: StateFlow<AxialAnalysisResult?> = _analysisResult.asStateFlow()

    private val _selectedSignalEntity = MutableStateFlow<AxialSignalEntity?>(null)
    val selectedSignalEntity: StateFlow<AxialSignalEntity?> = _selectedSignalEntity.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _allHistory = MutableStateFlow<List<AxialSignalEntity>>(emptyList())
    val allHistory: StateFlow<List<AxialSignalEntity>> = _allHistory.asStateFlow()

    // Tabs: "analyze" (Input screen) or "history" (Database Ledger) or "details" (Protocol display)
    private val _currentTab = MutableStateFlow("analyze")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allSignals.collectLatest { signals ->
                _allHistory.value = signals
            }
        }
    }

    fun setInputSignal(text: String) {
        _inputSignal.value = text
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun selectHistorySignal(entity: AxialSignalEntity) {
        _selectedSignalEntity.value = entity
        _inputSignal.value = entity.originalText
        try {
            val result = RetrofitClient.resultAdapter.fromJson(entity.jsonResult)
            _analysisResult.value = result
            _currentTab.value = "details"
        } catch (e: Exception) {
            _errorMessage.value = "Failed to parse historical record: ${e.localizedMessage}"
        }
    }

    fun setTab(tabName: String) {
        _currentTab.value = tabName
    }

    fun analyzeCurrentSignal() {
        val text = _inputSignal.value.trim()
        if (text.isBlank()) {
            _errorMessage.value = "Input text stream is empty. Please paste text to enforce boundaries."
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            _analysisResult.value = null
            _selectedSignalEntity.value = null

            val result = repository.analyzeText(text)
            result.fold(
                onSuccess = { analysis ->
                    _analysisResult.value = analysis
                    
                    // Generate a descriptive title
                    val titleText = if (analysis.isIdle) {
                        "Idle: " + (if (text.length > 30) text.take(30) + "..." else text)
                    } else if (analysis.reconstruction.originalNarrativeSummary.isNotEmpty()) {
                        val str = analysis.reconstruction.originalNarrativeSummary
                        if (str.length > 40) str.take(40) + "..." else str
                    } else {
                        "Signal " + (if (text.length > 30) text.take(30) + "..." else text)
                    }
                    
                    try {
                        val jsonStr = RetrofitClient.resultAdapter.toJson(analysis)
                        repository.saveSignal(titleText, text, jsonStr)
                    } catch (e: Exception) {
                        // Database writing errors do not fail the core display
                    }
                    
                    _currentTab.value = "details"
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Core pipeline connection malfunction."
                }
            )
            _isAnalyzing.value = false
        }
    }

    fun deleteHistoricalSignal(id: Int) {
        viewModelScope.launch {
            repository.deleteSignal(id)
            if (_selectedSignalEntity.value?.id == id) {
                _selectedSignalEntity.value = null
                _analysisResult.value = null
                _currentTab.value = "analyze"
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllSignals()
            _selectedSignalEntity.value = null
            _analysisResult.value = null
            _currentTab.value = "analyze"
        }
    }
}
