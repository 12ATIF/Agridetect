package com.dicoding.capstone.dermaface.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
	@field:SerializedName("-ChiliInfo2025")
	val chiliInfo2025: List<ChiliInfoItem?>? = null
)

data class ChiliInfoItem(
	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("link")
	val link: String? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("desc")
	val desc: String? = null
)