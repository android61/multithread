package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder().url("${BASE_URL}/api/slow/posts").build()

        requestAndParsePosts(request, callback)
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts").build()

        requestAndParsePost(request, callback)
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        val request: Request =
            Request.Builder().delete().url("${BASE_URL}/api/slow/posts/$id").build()
        request(request, callback)
    }

    override fun likeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().post(gson.toJson(id).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes").build()
        requestAndParsePost(request, callback)
    }

    override fun unlikeByIdAsync(id: Long, callback: PostRepository.Callback<Post>) {
        val request: Request = Request.Builder().delete(gson.toJson(id).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts/$id/likes").build()
        requestAndParsePost(request, callback)
    }

    private fun requestAndParsePost(request: Request, callback: PostRepository.Callback<Post>) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(body, Post::class.java))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    private fun requestAndParsePosts(
        request: Request, callback: PostRepository.Callback<List<Post>>
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    callback.onSuccess(gson.fromJson(body, typeToken.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    private fun request(request: Request, callback: PostRepository.Callback<Unit>) {
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                callback.onSuccess(Unit)
                response.close()
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }
}