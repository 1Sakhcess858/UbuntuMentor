
package com.example.ubuntumentor

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ApiClient.initialize(applicationContext)

        db = AppDatabase.getDatabase(this)

        // Get and save Firebase notification token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val token = task.result
                    val file = File(filesDir, "token.txt")
                    file.writeText(token)

                    Toast.makeText(
                        this,
                        "Token saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        this,
                        "Failed to get notification token",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // Open Settings
        val btnGoToSettings =
            findViewById<Button>(R.id.btnGoToSettings)

        btnGoToSettings.setOnClickListener {
            startActivity(
                android.content.Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        // Set up RecyclerView
        val recycler =
            findViewById<RecyclerView>(R.id.recyclerOpportunities)

        recycler.layoutManager = LinearLayoutManager(this)

        // Load opportunities from API
        loadOpportunities(recycler)
    }

    private fun loadOpportunities(recycler: RecyclerView) {

        ApiClient.apiService.getOpportunities()
            .enqueue(object : Callback<List<Opportunity>> {

                override fun onResponse(
                    call: Call<List<Opportunity>>,
                    response: Response<List<Opportunity>>
                ) {

                    if (response.isSuccessful && response.body() != null) {

                        val apiData = response.body()!!

                        lifecycleScope.launch {

                            // Update Room only when API returns data
                            if (apiData.isNotEmpty()) {

                                val entities = apiData.map { opportunity ->
                                    OpportunityEntity(
                                        id = opportunity.id,
                                        title = opportunity.title,
                                        provider = opportunity.provider,
                                        deadline = opportunity.deadline
                                    )
                                }

                                db.opportunityDao().clearAll()
                                db.opportunityDao()
                                    .insertOpportunities(entities)
                            }

                            // Display saved Room records
                            val savedData =
                                db.opportunityDao().getAllOpportunities()

                            recycler.adapter = OpportunityAdapter(savedData) {
                                opportunity -> saveOpportunity(opportunity)
                            }
                        }

                    } else {
                        loadOfflineData(recycler)
                    }
                }

                override fun onFailure(
                    call: Call<List<Opportunity>>,
                    t: Throwable
                ) {

                    Toast.makeText(
                        this@HomeActivity,
                        "Offline Mode: Loading saved data",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadOfflineData(recycler)
                }
            })
    }

    private fun loadOfflineData(recycler: RecyclerView) {

        lifecycleScope.launch {

            val savedData =
                db.opportunityDao().getAllOpportunities()

            recycler.adapter = OpportunityAdapter(savedData) {
                opportunity -> saveOpportunity(opportunity)
            }

            if (savedData.isEmpty()) {
                Toast.makeText(
                    this@HomeActivity,
                    "No saved opportunities available offline",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveOpportunity(opportunity: OpportunityEntity) {

        ApiClient.apiService.saveOpportunity(
            SaveOpportunityRequest(opportunity.id)
        ).enqueue(object : Callback<SaveOpportunityResponse> {

            override fun onResponse(
                call: Call<SaveOpportunityResponse>,
                response: Response<SaveOpportunityResponse>
            ) {

                if (response.isSuccessful) {

                    Toast.makeText(
                        this@HomeActivity,
                        "Opportunity saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {

                    Toast.makeText(
                        this@HomeActivity,
                        "Could not save opportunity. It may already be saved.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<SaveOpportunityResponse>,
                t: Throwable
            ) {

                Toast.makeText(
                    this@HomeActivity,
                    "Network error. Could not save opportunity.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}
