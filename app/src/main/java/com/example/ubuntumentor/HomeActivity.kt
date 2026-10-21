package com.example.ubuntumentor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // NEW: Go to settings button
        val btnGoToSettings = findViewById<Button>(R.id.btnGoToSettings)
        btnGoToSettings.setOnClickListener {
            startActivity(android.content.Intent(this, SettingsActivity::class.java))
        }

        // Fetch API data
        val recycler = findViewById<RecyclerView>(R.id.recyclerOpportunities)
        recycler.layoutManager = LinearLayoutManager(this)

        ApiClient.apiService.getOpportunities().enqueue(object : Callback<List<Opportunity>> {
            override fun onResponse(call: Call<List<Opportunity>>, response: Response<List<Opportunity>>) {
                if (response.isSuccessful && response.body() != null) {
                    recycler.adapter = OpportunityAdapter(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<Opportunity>>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Failed to load data: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}

// Adapter for the list (Keep this at the bottom)
class OpportunityAdapter(private val opportunities: List<Opportunity>) : RecyclerView.Adapter<OpportunityAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(android.R.id.text1)
        val tvProvider: TextView = view.findViewById(android.R.id.text2)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val opp = opportunities[position]
        holder.tvTitle.text = opp.title
        holder.tvProvider.text = "${opp.provider} | Deadline: ${opp.deadline}"
    }
    override fun getItemCount() = opportunities.size
}