package com.demo.stateflowandroid.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Repo(
    val name: String,
    val owner: RepoOwner,
    @Json(name = "stargazers_count") val stars: Int
)

inline class MinStarCount(val value: Int)

val NoMinStarCount = MinStarCount(0)
