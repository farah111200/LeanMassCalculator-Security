package com.example.leanmass.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.leanmass.databinding.ActivityHistoryBinding
import com.example.leanmass.databinding.ItemLbmRecordBinding
import com.example.leanmass.model.LbmRecord
import com.example.leanmass.utils.LbmCalculator
import com.example.leanmass.viewmodel.AuthViewModel
import com.example.leanmass.viewmodel.LbmViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val lbmViewModel: LbmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val userId = authViewModel.currentUserId() ?: run {
            finish()
            return
        }

        val adapter = LbmAdapter { record ->
            lbmViewModel.deleteRecord(record)
            Toast.makeText(this, "Enregistrement supprimé", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lbmViewModel.getRecords(userId).observe(this) { records ->
            adapter.submitList(records)
            binding.tvEmpty.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

class LbmAdapter(
    private val onDelete: (LbmRecord) -> Unit
) : RecyclerView.Adapter<LbmAdapter.ViewHolder>() {

    private var items: List<LbmRecord> = emptyList()

    fun submitList(list: List<LbmRecord>) {
        items = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemLbmRecordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLbmRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        with(holder.binding) {
            tvDate.text = record.date
            tvLbm.text = String.format("LBM : %.2f kg", record.lbm)
            tvDetails.text = "${if (record.gender == "M") "Homme" else "Femme"} • ${record.weight}kg • ${record.height}cm"

            val isSat = LbmCalculator.isSatisfactory(record.gender, record.lbm)
            ivStatus.setImageResource(
                if (isSat) android.R.drawable.checkbox_on_background
                else android.R.drawable.ic_dialog_alert
            )

            btnDelete.setOnClickListener { onDelete(record) }
        }
    }

    override fun getItemCount() = items.size
}
