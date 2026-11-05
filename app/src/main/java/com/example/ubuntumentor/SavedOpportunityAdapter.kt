
package com.example.ubuntumentor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SavedOpportunityAdapter(
    private val opportunities: List<SavedOpportunity>,
    private val onDeleteClick: (SavedOpportunity) -> Unit
) : RecyclerView.Adapter<SavedOpportunityAdapter.SavedViewHolder>() {

    class SavedViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val title: TextView =
            itemView.findViewById(R.id.txtSavedTitle)

        val provider: TextView =
            itemView.findViewById(R.id.txtSavedProvider)

        val deadline: TextView =
            itemView.findViewById(R.id.txtSavedDeadline)

        val deleteButton: Button =
            itemView.findViewById(R.id.btnRemoveSaved)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SavedViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_opportunity, parent, false)

        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SavedViewHolder,
        position: Int
    ) {

        val opportunity = opportunities[position]

        holder.title.text = opportunity.title
        holder.provider.text = "Provider: ${opportunity.provider}"
        holder.deadline.text = "Deadline: ${opportunity.deadline}"

        holder.deleteButton.setOnClickListener {
            onDeleteClick(opportunity)
        }
    }

    override fun getItemCount(): Int {
        return opportunities.size
    }
}
