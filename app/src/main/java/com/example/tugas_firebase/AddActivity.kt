package com.example.tugas_firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.tugas_firebase.databinding.AddFormBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: AddFormBinding
    private var updateId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val budgetId = intent.getStringExtra("budgets_id")
        val budgetNominal = intent.getStringExtra("budgets_nominal")
        val budgetDescription = intent.getStringExtra("budgets_description")
        val budgetDate = intent.getStringExtra("budget_date")
        val actionType = intent.getStringExtra("action_type")

        updateId = intent.getStringExtra("budget_id") ?: ""
        binding.txtTitle.setText(budgetNominal ?: "")
        binding.txtDesc.setText(budgetDescription ?: "")
        binding.txtDate.setText(budgetDate ?: "")

        with(binding){
            if (actionType == "add") {
                btnAdd.setOnClickListener {
                    val nominal = txtTitle.text.toString()
                    val description = txtDesc.text.toString()
                    val date = txtDate.text.toString()
                    val newBudget = Budget(
                       nominal = nominal, date = date,
                        description = description
                    )
                    addBudget(newBudget)
                }
            }else if (actionType == "update") {
                btnUpdate.setOnClickListener {
                    val nominal = txtTitle.text.toString()
                    val description = txtDesc.text.toString()
                    val date = txtDate.text.toString()
                    val budgetToUpdate =
                       Budget(nominal = nominal, date = date, description = description)

                    if (updateId.isNotEmpty()) {
                        updateBudget(budgetToUpdate)
                    } else {
                        addBudget(budgetToUpdate)
                    }
                }
            }
        }
    }
    private fun addBudget(budget: Budget) {
       budgetCollectionRef
            .add(budget)
            .addOnSuccessListener { documentReference ->
                val createdComplaintId = documentReference.id
                budget.id = createdComplaintId
                documentReference.set(budget)
                    .addOnSuccessListener {
                        val intent = Intent(this@AddActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        Log.d("formactivity", "Error updating document", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error adding document", exception)
            }
    }

    private fun updateBudget(budget: Budget) {
        budgetCollectionRef.document(updateId).update(
            "nominal", budget.nominal,
            "description", budget.description,
            "date", budget.date
        )

            .addOnSuccessListener {
                val intent = Intent(this@AddActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("formactivity", "Error updating document", exception)
            }
    }
}
