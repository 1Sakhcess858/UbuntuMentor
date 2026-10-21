#!/usr/bin/env kotlin

package com.example.ubuntumentor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

// Data Models
data class AuthRequest(val name: String? = null, val email: String, val password: String)
data class AuthResponse(val message: String, val userId: Int?, val name: String?)
data class Opportunity(val id: Int, val title: String, val provider: String, val deadline: String)

// API Interface
interface ApiService {
    @POST("api/auth/register")
    fun registerUser(@Body request: AuthRequest): Call<AuthResponse>

    @POST("api/auth/login")
    fun loginUser(@Body request: AuthRequest): Call<AuthResponse>

    @GET("api/opportunities")
    fun getOpportunities(): Call<List<Opportunity>>
}

// Retrofit Object
object ApiClient {
    // 10.0.2.2 is how the Android Emulator talks to your PC's localhost
    private const val BASE_URL = "http://10.0.2.2:3000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}