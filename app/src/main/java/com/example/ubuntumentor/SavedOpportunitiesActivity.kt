
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedOpportunitiesActivity : AppCompatActivity() {

    private lateinit var recyclerSaved: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_opportunities)

        val sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.initialize(applicationContext)

        recyclerSaved = findViewById(R.id.recyclerSavedOpportunities)
        progressBar = findViewById(R.id.progressSaved)
        txtMessage = findViewById(R.id.txtSavedMessage)

        recyclerSaved.layoutManager = LinearLayoutManager(this)

        loadSavedOpportunities()
    }

    private fun loadSavedOpportunities() {

        progressBar.visibility = View.VISIBLE
        txtMessage.visibility = View.GONE

        ApiClient.apiService.getSavedOpportunities()
            .enqueue(object : Callback<SavedOpportunitiesResponse> {

                override fun onResponse(
                    call: Call<SavedOpportunitiesResponse>,
                    response: Response<SavedOpportunitiesResponse>
                ) {

                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {

                        val opportunities =
                            response.body()!!.opportunities

                        if (opportunities.isEmpty()) {

                            recyclerSaved.adapter =
                                SavedOpportunityAdapter(emptyList()) {}

                            txtMessage.text =
                                "You haven't saved any opportunities yet."

                            txtMessage.visibility = View.VISIBLE

                        } else {

                            txtMessage.visibility = View.GONE

                            recyclerSaved.adapter =
                                SavedOpportunityAdapter(opportunities) { opportunity ->
                                    deleteSavedOpportunity(opportunity)
                                }
                        }

                    } else {

                        txtMessage.text =
                            "Could not load saved opportunities. Please try again."

                        txtMessage.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(
                    call: Call<SavedOpportunitiesResponse>,
                    t: Throwable
                ) {

                    progressBar.visibility = View.GONE

                    txtMessage.text =
                        "Network error. Check your internet connection and try again."

                    txtMessage.visibility = View.VISIBLE
                }
            })
    }

    private fun deleteSavedOpportunity(
        opportunity: SavedOpportunity
    ) {

        ApiClient.apiService.deleteSavedOpportunity(
            opportunity.opportunityId
        ).enqueue(object : Callback<BasicResponse> {

            override fun onResponse(
                call: Call<BasicResponse>,
                response: Response<BasicResponse>
            ) {

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@SavedOpportunitiesActivity,
                        "Opportunity removed successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadSavedOpportunities()

                } else {

                    Toast.makeText(
                        this@SavedOpportunitiesActivity,
                        "Could not remove opportunity",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<BasicResponse>,
                t: Throwable
            ) {

                Toast.makeText(
                    this@SavedOpportunitiesActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
