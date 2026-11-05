
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OpportunitiesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMessage: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_opportunities)

        recyclerView = findViewById(R.id.recyclerOpportunities)
        progressBar = findViewById(R.id.progressBar)
        txtMessage = findViewById(R.id.txtMessage)

        recyclerView.layoutManager = LinearLayoutManager(this)

        database = AppDatabase.getDatabase(applicationContext)

        // Initialize Retrofit and the authentication interceptor.
        ApiClient.initialize(applicationContext)

        loadOpportunities()
    }

    private fun loadOpportunities() {

        progressBar.visibility = View.VISIBLE
        txtMessage.visibility = View.GONE

        lifecycleScope.launch {

            val cachedOpportunities = withContext(Dispatchers.IO) {
                database.opportunityDao().getAllOpportunities()
            }

            if (isFinishing || isDestroyed) return@launch

            if (cachedOpportunities.isNotEmpty()) {
                displayOpportunities(cachedOpportunities)

                txtMessage.text =
                    "Showing saved opportunities. Checking for updates..."

                txtMessage.visibility = View.VISIBLE
            }

            ApiClient.apiService.getOpportunities()
                .enqueue(object : Callback<List<Opportunity>> {

                    override fun onResponse(
                        call: Call<List<Opportunity>>,
                        response: Response<List<Opportunity>>
                    ) {

                        if (response.isSuccessful) {

                            val opportunities = response.body().orEmpty()

                            lifecycleScope.launch {

                                val savedOpportunities =
                                    withContext(Dispatchers.IO) {

                                        val dao = database.opportunityDao()

                                        dao.clearAll()

                                        if (opportunities.isNotEmpty()) {

                                            val entities =
                                                opportunities.map {

                                                    OpportunityEntity(
                                                        id = it.id,
                                                        title = it.title,
                                                        provider = it.provider,
                                                        deadline = it.deadline
                                                    )
                                                }

                                            dao.insertOpportunities(entities)
                                        }

                                        dao.getAllOpportunities()
                                    }

                                if (isFinishing || isDestroyed) {
                                    return@launch
                                }

                                progressBar.visibility = View.GONE

                                if (savedOpportunities.isEmpty()) {

                                    txtMessage.text =
                                        "No opportunities available."

                                    txtMessage.visibility = View.VISIBLE

                                } else {

                                    displayOpportunities(savedOpportunities)
                                    txtMessage.visibility = View.GONE
                                }

                                Log.d(
                                    "OPPORTUNITIES",
                                    "Saved ${savedOpportunities.size} opportunities locally."
                                )
                            }

                        } else {

                            progressBar.visibility = View.GONE

                            Log.e(
                                "OPPORTUNITIES",
                                "Server error: ${response.code()}"
                            )

                            showCachedDataOrError(
                                "Unable to refresh opportunities. Server error: ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Opportunity>>,
                        t: Throwable
                    ) {

                        progressBar.visibility = View.GONE

                        Log.e(
                            "OPPORTUNITIES",
                            "Network failure: ${t.message}",
                            t
                        )

                        showCachedDataOrError(
                            "Internet unavailable. Showing saved opportunities."
                        )
                    }
                })
        }
    }

    private fun displayOpportunities(
        opportunities: List<OpportunityEntity>
    ) {

        recyclerView.adapter = OpportunityAdapter(opportunities) { opportunity ->

            saveOpportunity(opportunity)
        }
    }

    private fun saveOpportunity(
        opportunity: OpportunityEntity
    ) {

        val request = SaveOpportunityRequest(
            opportunityId = opportunity.id
        )

        ApiClient.apiService.saveOpportunity(request)
            .enqueue(object : Callback<SaveOpportunityResponse> {

                override fun onResponse(
                    call: Call<SaveOpportunityResponse>,
                    response: Response<SaveOpportunityResponse>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            this@OpportunitiesActivity,
                            "Opportunity saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(
                            "SAVED_OPPORTUNITY",
                            "Saved opportunity ID: ${opportunity.id}"
                        )

                    } else {

                        val message = when (response.code()) {
                            409 -> "This opportunity is already saved."
                            401 -> "Your session has expired. Please log in again."
                            else -> "Unable to save opportunity. Error: ${response.code()}"
                        }

                        Toast.makeText(
                            this@OpportunitiesActivity,
                            message,
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e(
                            "SAVED_OPPORTUNITY",
                            "Save failed: ${response.code()}"
                        )
                    }
                }

                override fun onFailure(
                    call: Call<SaveOpportunityResponse>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@OpportunitiesActivity,
                        "Network error. Could not save opportunity.",
                        Toast.LENGTH_LONG
                    ).show()

                    Log.e(
                        "SAVED_OPPORTUNITY",
                        "Network error: ${t.message}",
                        t
                    )
                }
            })
    }

    private fun showCachedDataOrError(message: String) {

        lifecycleScope.launch {

            val cachedOpportunities = withContext(Dispatchers.IO) {
                database.opportunityDao().getAllOpportunities()
            }

            if (isFinishing || isDestroyed) return@launch

            if (cachedOpportunities.isNotEmpty()) {

                displayOpportunities(cachedOpportunities)

                txtMessage.text = message
                txtMessage.visibility = View.VISIBLE

            } else {

                txtMessage.text =
                    "No saved opportunities available. Connect to the internet and try again."

                txtMessage.visibility = View.VISIBLE
            }
        }
    }
}
