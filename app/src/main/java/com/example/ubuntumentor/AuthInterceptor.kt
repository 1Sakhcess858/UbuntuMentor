package com.example.ubuntumentor

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {

    private val appContext = context.applicationContext

    override fun intercept(chain: Interceptor.Chain): Response {

        val preferences = appContext.getSharedPreferences(
            "UbuntuMentorSession",
            Context.MODE_PRIVATE
        )

        val token = preferences.getString("authToken", null)

        val originalRequest = chain.request()

        val newRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}