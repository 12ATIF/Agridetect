package com.dicoding.capstone.dermaface.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.capstone.dermaface.repository.ArticleRepository
import com.dicoding.capstone.dermaface.repository.DetailHistoryRepository
import com.dicoding.capstone.dermaface.repository.HistoryRepository
import com.dicoding.capstone.dermaface.repository.LocalHistoryRepository
import com.dicoding.capstone.dermaface.repository.LocalResultRepository
import com.dicoding.capstone.dermaface.repository.ScanRepository
import com.dicoding.capstone.dermaface.repository.SplashRepository
import com.dicoding.capstone.dermaface.repository.UserRepository

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val context: Context? = null,
    private val userRepository: UserRepository? = null,
    private val articleRepository: ArticleRepository? = null,
    private val scanRepository: ScanRepository? = null,
    private val localResultRepository: LocalResultRepository? = null,
    private val localHistoryRepository: LocalHistoryRepository? = null,
    private val historyRepository: HistoryRepository? = null,
    private val detailHistoryRepository: DetailHistoryRepository? = null,
    private val splashRepository: SplashRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                userRepository?.let { UserViewModel(it) }
                    ?: error("UserRepository not provided")
            }
            modelClass.isAssignableFrom(ArticleViewModel::class.java) -> {
                articleRepository?.let { ArticleViewModel(it) }
                    ?: error("ArticleRepository not provided")
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                userRepository?.let { LoginViewModel(it) }
                    ?: error("UserRepository not provided")
            }
            modelClass.isAssignableFrom(ScanViewModel::class.java) -> {
                scanRepository?.let { ScanViewModel(it) }
                    ?: context?.let { ScanViewModel(ScanRepository(it)) }
                    ?: error("ScanRepository not provided")
            }
            modelClass.isAssignableFrom(ResultViewModel::class.java) -> {
                localResultRepository?.let { ResultViewModel(it) }
                    ?: context?.let { ResultViewModel(LocalResultRepository(it)) }
                    ?: error("LocalResultRepository not provided")
            }
            modelClass.isAssignableFrom(LocalHistoryViewModel::class.java) -> {
                localHistoryRepository?.let { LocalHistoryViewModel(it) }
                    ?: context?.let { LocalHistoryViewModel(LocalHistoryRepository(it)) }
                    ?: error("LocalHistoryRepository not provided")
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                localHistoryRepository?.let { HistoryViewModel(it) }
                    ?: context?.let { HistoryViewModel(LocalHistoryRepository(it)) }
                    ?: error("LocalHistoryRepository not provided")
            }
            modelClass.isAssignableFrom(DetailHistoryViewModel::class.java) -> {
                detailHistoryRepository?.let { DetailHistoryViewModel(it) }
                    ?: error("DetailHistoryRepository not provided")
            }
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                splashRepository?.let { SplashViewModel(it) }
                    ?: error("SplashRepository not provided")
            }
            else -> error("Unknown ViewModel class")
        } as T
    }
}