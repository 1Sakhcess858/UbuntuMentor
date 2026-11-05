
package com.example.ubuntumentor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OpportunityAdapter(
    private val opportunities: List<OpportunityEntity>,
    private val onSaveClick: (OpportunityEntity) -> Unit
) : RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder>() {

    class OpportunityViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView =
            itemView.findViewById(R.id.txtTitle)

        val provider: TextView =
            itemView.findViewById(R.id.txtProvider)

        val deadline: TextView =
            itemView.findViewById(R.id.txtDeadline)

        val saveButton: Button =
            itemView.findViewById(R.id.btnSaveOpportunity)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OpportunityViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_opportunity, parent, false)

        return OpportunityViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: OpportunityViewHolder,
        position: Int
    ) {

        val opportunity = opportunities[position]

        holder.title.text = opportunity.title
        holder.provider.text = "Provider: ${opportunity.provider}"
        holder.deadline.text = "Deadline: ${opportunity.deadline}"

        holder.saveButton.isEnabled = true
        holder.saveButton.text = "Save Opportunity"

        holder.saveButton.setOnClickListener {
            onSaveClick(opportunity)
        }
    }

    override fun getItemCount(): Int {
        return opportunities.size
    }
}
