
package com.example.ubuntumentor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApplicationsActivity : AppCompatActivity() {

    private lateinit var recyclerApplications: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMessage: TextView
    private lateinit var adapter: ApplicationAdapter

    private val applications = mutableListOf<Application>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)

        if (!SessionManager(this).isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        ApiClient.initialize(applicationContext)

        recyclerApplications =
            findViewById(R.id.recyclerApplications)

        progressBar = findViewById(R.id.progressApplications)
        txtMessage = findViewById(R.id.txtApplicationsMessage)

        recyclerApplications.layoutManager =
            LinearLayoutManager(this)

        adapter = ApplicationAdapter(
            applications,
            onStatusChanged = { application, status ->
                updateApplication(
                    application,
                    UpdateApplicationRequest(status = status)
                )
            },
            onNotesSaved = { application, notes ->
                updateApplication(
                    application,
                    UpdateApplicationRequest(notes = notes)
                )
            },
            onDelete = { application ->
                deleteApplication(application)
            }
        )

        recyclerApplications.adapter = adapter

        findViewById<Button>(R.id.btnAddApplication)
            .setOnClickListener {
                showOpportunityPicker()
            }

        loadApplications()
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            loadApplications()
        }
    }

    private fun loadApplications() {
        progressBar.visibility = View.VISIBLE
        txtMessage.visibility = View.GONE

        ApiClient.apiService.getApplications()
            .enqueue(object : Callback<ApplicationsResponse> {

                override fun onResponse(
                    call: Call<ApplicationsResponse>,
                    response: Response<ApplicationsResponse>
                ) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        applications.clear()
                        applications.addAll(
                            response.body()!!.applications
                        )
                        adapter.notifyDataSetChanged()

                        if (applications.isEmpty()) {
                            txtMessage.text =
                                "You are not tracking any applications yet. Tap Add Application to begin."
                            txtMessage.visibility = View.VISIBLE
                        } else {
                            txtMessage.visibility = View.GONE
                        }
                    } else {
                        showMessage(
                            "Could not load applications. Please try again."
                        )
                    }
                }

                override fun onFailure(
                    call: Call<ApplicationsResponse>,
                    t: Throwable
                ) {
                    progressBar.visibility = View.GONE
                    showMessage(
                        "Network error. Check your connection and try again."
                    )
                }
            })
    }

    private fun showOpportunityPicker() {
        progressBar.visibility = View.VISIBLE

        ApiClient.apiService.getOpportunities()
            .enqueue(object : Callback<List<Opportunity>> {

                override fun onResponse(
                    call: Call<List<Opportunity>>,
                    response: Response<List<Opportunity>>
                ) {
                    progressBar.visibility = View.GONE

                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(
                            this@ApplicationsActivity,
                            "Could not load opportunities",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    val available = response.body()!!.filter { opportunity ->
                        applications.none {
                            it.opportunityId == opportunity.id
                        }
                    }

                    if (available.isEmpty()) {
                        Toast.makeText(
                            this@ApplicationsActivity,
                            "All available opportunities are already being tracked",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val labels = available.map {
                        "${it.title} - ${it.provider}"
                    }.toTypedArray()

                    AlertDialog.Builder(this@ApplicationsActivity)
                        .setTitle("Choose an opportunity")
                        .setItems(labels) { _, index ->
                            createApplication(available[index])
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                override fun onFailure(
                    call: Call<List<Opportunity>>,
                    t: Throwable
                ) {
                    progressBar.visibility = View.GONE

                    Toast.makeText(
                        this@ApplicationsActivity,
                        "Network error loading opportunities",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun createApplication(opportunity: Opportunity) {
        progressBar.visibility = View.VISIBLE

        ApiClient.apiService.createApplication(
            ApplicationRequest(opportunity.id)
        ).enqueue(object : Callback<ApplicationResponse> {

            override fun onResponse(
                call: Call<ApplicationResponse>,
                response: Response<ApplicationResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ApplicationsActivity,
                        "Application tracking started",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadApplications()
                } else {
                    Toast.makeText(
                        this@ApplicationsActivity,
                        "Could not add application",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<ApplicationResponse>,
                t: Throwable
            ) {
                progressBar.visibility = View.GONE

                Toast.makeText(
                    this@ApplicationsActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateApplication(
        application: Application,
        request: UpdateApplicationRequest
    ) {
        ApiClient.apiService.updateApplication(
            application.opportunityId,
            request
        ).enqueue(object : Callback<ApplicationResponse> {

            override fun onResponse(
                call: Call<ApplicationResponse>,
                response: Response<ApplicationResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ApplicationsActivity,
                        "Application updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadApplications()
                } else {
                    Toast.makeText(
                        this@ApplicationsActivity,
                        "Could not update application",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<ApplicationResponse>,
                t: Throwable
            ) {
                Toast.makeText(
                    this@ApplicationsActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun deleteApplication(application: Application) {
        AlertDialog.Builder(this)
            .setTitle("Remove application?")
            .setMessage(
                "Remove ${application.title} from your tracking list?"
            )
            .setPositiveButton("Remove") { _, _ ->

                ApiClient.apiService.deleteApplication(
                    application.opportunityId
                ).enqueue(object : Callback<BasicResponse> {

                    override fun onResponse(
                        call: Call<BasicResponse>,
                        response: Response<BasicResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@ApplicationsActivity,
                                "Application removed",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadApplications()
                        } else {
                            Toast.makeText(
                                this@ApplicationsActivity,
                                "Could not remove application",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(
                        call: Call<BasicResponse>,
                        t: Throwable
                    ) {
                        Toast.makeText(
                            this@ApplicationsActivity,
                            "Network error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMessage(message: String) {
        txtMessage.text = message
        txtMessage.visibility = View.VISIBLE
    }
}
