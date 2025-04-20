package com.dicoding.capstone.dermaface.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.capstone.dermaface.BuildConfig
import com.dicoding.capstone.dermaface.data.model.ChiliInfoItem
import com.dicoding.capstone.dermaface.data.model.NewsResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.*
import java.io.IOException

class ArticleRepository {
    private val TAG = "ArticleRepository"
    private val databaseUrl = BuildConfig.BASE_URL

    fun fetchArticles(): LiveData<List<ChiliInfoItem>> {
        val articlesLiveData = MutableLiveData<List<ChiliInfoItem>>()
        val client = OkHttpClient()
        val request = Request.Builder().url(databaseUrl).build()

        Log.d(TAG, "Fetching articles from: $databaseUrl")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error fetching articles: ${e.message}")
                articlesLiveData.postValue(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    try {
                        val jsonResponse = responseBody.string()
                        Log.d(TAG, "Response received: $jsonResponse")

                        val newsResponse: NewsResponse = Gson().fromJson(jsonResponse, NewsResponse::class.java)
                        val articles = newsResponse.chiliInfo2025?.filterNotNull() ?: emptyList()
                        Log.d(TAG, "Parsed ${articles.size} articles")

                        articlesLiveData.postValue(articles)
                    } catch (e: JsonSyntaxException) {
                        Log.e(TAG, "JSON parsing error: ${e.message}")
                        articlesLiveData.postValue(emptyList())
                    }
                } ?: run {
                    Log.e(TAG, "Response body is null")
                    articlesLiveData.postValue(emptyList())
                }
            }
        })

        return articlesLiveData
    }
}