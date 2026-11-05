
package com.example.ubuntumentor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ApplicationAdapter(
    private val applications: List<Application>,
    private val onStatusChanged: (Application, String) -> Unit,
    private val onNotesSaved: (Application, String) -> Unit,
    private val onDelete: (Application) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder>() {

    private val statuses = listOf(
        "Interested",
        "Applied",
        "Interview",
        "Accepted",
        "Unsuccessful"
    )

    class ApplicationViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val title: TextView =
            view.findViewById(R.id.txtApplicationTitle)

        val provider: TextView =
            view.findViewById(R.id.txtApplicationProvider)

        val deadline: TextView =
            view.findViewById(R.id.txtApplicationDeadline)

        val spinner: Spinner =
            view.findViewById(R.id.spinnerApplicationStatus)

        val notes: EditText =
            view.findViewById(R.id.editApplicationNotes)

        val saveNotes: Button =
            view.findViewById(R.id.btnSaveApplicationNotes)

        val delete: Button =
            view.findViewById(R.id.btnDeleteApplication)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application, parent, false)

        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ApplicationViewHolder,
        position: Int
    ) {
        val application = applications[position]

        holder.title.text = application.title
        holder.provider.text = "Provider: ${application.provider}"
        holder.deadline.text = "Deadline: ${application.deadline}"

        val spinnerAdapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            statuses
        )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        holder.spinner.adapter = spinnerAdapter

        val selectedPosition =
            statuses.indexOf(application.status).coerceAtLeast(0)

        holder.spinner.setSelection(selectedPosition, false)

        holder.spinner.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) = Unit

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedStatus = statuses[position]

                    if (selectedStatus != application.status) {
                        onStatusChanged(application, selectedStatus)
                    }
                }
            }

        holder.notes.setText(application.notes.orEmpty())

        holder.saveNotes.setOnClickListener {
            onNotesSaved(
                application,
                holder.notes.text.toString()
            )
        }

        holder.delete.setOnClickListener {
            onDelete(application)
        }
    }

    override fun getItemCount(): Int = applications.size
}
