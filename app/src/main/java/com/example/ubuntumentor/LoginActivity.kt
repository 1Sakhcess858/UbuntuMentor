
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var sessionManager: SessionManager

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ApiClient.initialize(this)

        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            openDashboard()
            return
        }

        // GOOGLE SIGN-IN CONFIGURATION
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(
                "316104145736-cn78r0r03rgnrhvigv4o5qa2cuq5tg65.apps.googleusercontent.com"
            )
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleSignIn =
            findViewById<SignInButton>(R.id.btnGoogleSignIn)
        val tvGoToRegister =
            findViewById<TextView>(R.id.tvGoToRegister)

        // OPEN REGISTER SCREEN
        tvGoToRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }

        // EMAIL AND PASSWORD LOGIN
        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val request = AuthRequest(
                email = email,
                password = password
            )

            ApiClient.apiService.loginUser(request)
                .enqueue(object : Callback<AuthResponse> {

                    override fun onResponse(
                        call: Call<AuthResponse>,
                        response: Response<AuthResponse>
                    ) {

                        if (!response.isSuccessful) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Invalid credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        val user = response.body()
                        val token = user?.token

                        if (token.isNullOrBlank()) {
                            Toast.makeText(
                                this@LoginActivity,
                                "No authentication token received",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        sessionManager.saveSession(
                            userId = user.userId?.toString() ?: "",
                            name = user.name ?: email,
                            authType = "API",
                            token = token
                        )

                        verifyBackendSession()
                    }

                    override fun onFailure(
                        call: Call<AuthResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Network Error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        // GOOGLE SIGN-IN BUTTON
        btnGoogleSignIn.setOnClickListener {

            // Clear previous Google selection
            googleSignInClient.signOut().addOnCompleteListener {

                val signInIntent = googleSignInClient.signInIntent

                startActivityForResult(
                    signInIntent,
                    RC_SIGN_IN
                )
            }
        }
    }

    // HANDLE GOOGLE SIGN-IN RESULT
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                val googleIdToken = account.idToken

                if (googleIdToken.isNullOrBlank()) {
                    Toast.makeText(
                        this,
                        "Google did not return an ID token. Check OAuth configuration.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val credential = GoogleAuthProvider.getCredential(
                    googleIdToken,
                    null
                )

                // FIREBASE AUTHENTICATION
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { authTask ->

                        if (authTask.isSuccessful) {

                            val firebaseUser = auth.currentUser

                            if (firebaseUser == null) {
                                Toast.makeText(
                                    this,
                                    "Firebase user not found",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@addOnCompleteListener
                            }

                            // GET FIREBASE ID TOKEN
                            firebaseUser.getIdToken(true)
                                .addOnSuccessListener { tokenResult ->

                                    val firebaseIdToken =
                                        tokenResult.token

                                    if (firebaseIdToken.isNullOrBlank()) {
                                        Toast.makeText(
                                            this,
                                            "Could not get Firebase ID token",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@addOnSuccessListener
                                    }

                                    // SEND FIREBASE TOKEN TO NODE.JS BACKEND
                                    val request = GoogleAuthRequest(
                                        idToken = firebaseIdToken
                                    )

                                    ApiClient.apiService.googleLogin(request)
                                        .enqueue(object : Callback<AuthResponse> {

                                            override fun onResponse(
                                                call: Call<AuthResponse>,
                                                response: Response<AuthResponse>
                                            ) {

                                                if (!response.isSuccessful) {
                                                    Toast.makeText(
                                                        this@LoginActivity,
                                                        "Backend Google login failed: ${response.code()}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return
                                                }

                                                val user = response.body()
                                                val backendToken = user?.token

                                                if (backendToken.isNullOrBlank()) {
                                                    Toast.makeText(
                                                        this@LoginActivity,
                                                        "Backend did not return a JWT",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return
                                                }

                                                // SAVE BACKEND JWT
                                                sessionManager.saveSession(
                                                    userId = user.userId?.toString() ?: "",
                                                    name = user.name
                                                        ?: firebaseUser.displayName
                                                        ?: "Google User",
                                                    authType = "GOOGLE",
                                                    token = backendToken
                                                )

                                                // VERIFY BACKEND JWT
                                                verifyBackendSession()
                                            }

                                            override fun onFailure(
                                                call: Call<AuthResponse>,
                                                t: Throwable
                                            ) {
                                                Toast.makeText(
                                                    this@LoginActivity,
                                                    "Backend network error: ${t.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        })
                                }
                                .addOnFailureListener { error ->

                                    Toast.makeText(
                                        this,
                                        "Firebase token error: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                        } else {

                            val errorMessage =
                                authTask.exception?.message
                                    ?: "Unknown Firebase authentication error"

                            Toast.makeText(
                                this@LoginActivity,
                                "Firebase Authentication Failed: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

            } catch (e: ApiException) {

                Toast.makeText(
                    this,
                    "Google Sign-In Failed: ${e.statusCode} - ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // VERIFY BACKEND JWT
    private fun verifyBackendSession() {

        ApiClient.apiService.getCurrentUser()
            .enqueue(object : Callback<UserProfile> {

                override fun onResponse(
                    call: Call<UserProfile>,
                    response: Response<UserProfile>
                ) {

                    if (response.isSuccessful) {

                        val profile = response.body()

                        Toast.makeText(
                            this@LoginActivity,
                            "JWT VERIFIED: ${profile?.name}",
                            Toast.LENGTH_LONG
                        ).show()

                        openDashboard()

                    } else {

                        sessionManager.logout()

                        Toast.makeText(
                            this@LoginActivity,
                            "JWT verification failed: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<UserProfile>,
                    t: Throwable
                ) {

                    sessionManager.logout()

                    Toast.makeText(
                        this@LoginActivity,
                        "JWT network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // OPEN DASHBOARD
    private fun openDashboard() {

        startActivity(
            Intent(this, DashboardActivity::class.java)
        )

        finish()
    }
}
