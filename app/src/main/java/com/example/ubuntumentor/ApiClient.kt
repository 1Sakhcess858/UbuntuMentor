
package com.example.ubuntumentor

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// ======================================
// AUTHENTICATION MODELS
// ======================================

data class AuthRequest(
    val name: String? = null,
    val email: String,
    val password: String
)

data class GoogleAuthRequest(
    val idToken: String
)

data class AuthResponse(
    val message: String,
    val userId: Int?,
    val name: String?,
    val token: String? = null
)

data class UserProfile(
    val message: String,
    val userId: Int,
    val name: String,
    val email: String
)

// ======================================
// OPPORTUNITY MODELS
// ======================================

data class Opportunity(
    val id: Int,
    val title: String,
    val provider: String,
    val deadline: String
)

data class SaveOpportunityRequest(
    val opportunityId: Int
)

data class SavedOpportunity(
    val opportunityId: Int,
    val title: String,
    val provider: String,
    val deadline: String,
    val createdAt: String?
)

data class SaveOpportunityResponse(
    val message: String,
    val opportunity: SavedOpportunity?
)

data class SavedOpportunitiesResponse(
    val message: String,
    val opportunities: List<SavedOpportunity>
)

// ======================================
// APPLICATION TRACKING MODELS
// ======================================

data class ApplicationRequest(
    val opportunityId: Int
)

data class UpdateApplicationRequest(
    val status: String? = null,
    val notes: String? = null
)

data class Application(
    val opportunityId: Int,
    val title: String,
    val provider: String,
    val deadline: String,
    val status: String,
    val notes: String?,
    val appliedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class ApplicationResponse(
    val message: String,
    val application: Application?
)

data class ApplicationsResponse(
    val message: String,
    val applications: List<Application>
)

// ======================================
// GENERAL RESPONSE
// ======================================

data class BasicResponse(
    val message: String
)

// ======================================
// API SERVICE
// ======================================

interface ApiService {

    // AUTHENTICATION

    @POST("api/auth/register")
    fun registerUser(
        @Body request: AuthRequest
    ): Call<AuthResponse>

    @POST("api/auth/login")
    fun loginUser(
        @Body request: AuthRequest
    ): Call<AuthResponse>

    @POST("api/auth/google")
    fun googleLogin(
        @Body request: GoogleAuthRequest
    ): Call<AuthResponse>

    @GET("api/auth/me")
    fun getCurrentUser(): Call<UserProfile>

    // OPPORTUNITIES

    @GET("api/opportunities")
    fun getOpportunities(): Call<List<Opportunity>>

    // SAVED OPPORTUNITIES

    @POST("api/saved-opportunities")
    fun saveOpportunity(
        @Body request: SaveOpportunityRequest
    ): Call<SaveOpportunityResponse>

    @GET("api/saved-opportunities")
    fun getSavedOpportunities(): Call<SavedOpportunitiesResponse>

    @DELETE("api/saved-opportunities/{opportunityId}")
    fun deleteSavedOpportunity(
        @Path("opportunityId") opportunityId: Int
    ): Call<BasicResponse>

    // APPLICATION TRACKING

    @POST("api/applications")
    fun createApplication(
        @Body request: ApplicationRequest
    ): Call<ApplicationResponse>

    @GET("api/applications")
    fun getApplications(): Call<ApplicationsResponse>

    @PATCH("api/applications/{opportunityId}")
    fun updateApplication(
        @Path("opportunityId") opportunityId: Int,
        @Body request: UpdateApplicationRequest
    ): Call<ApplicationResponse>

    @DELETE("api/applications/{opportunityId}")
    fun deleteApplication(
        @Path("opportunityId") opportunityId: Int
    ): Call<BasicResponse>
}

// ======================================
// API CLIENT
// ======================================

object ApiClient {

    private const val BASE_URL = "http://192.168.1.101:3000/"

    lateinit var apiService: ApiService
        private set

    fun initialize(context: Context) {

        val client = OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor(context.applicationContext)
            )
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)
    }
}
