package com.example.tugas_firebase


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.example.tugas_firebase.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private val budgetCollectionRef = firestore.collection("budgets")
    private lateinit var binding: ActivityMainBinding
    private val budgetListLiveData: MutableLiveData<List<Budget>> by lazy {
        MutableLiveData<List<Budget>>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            fabAdd.setOnClickListener {
                val intent = Intent(this@MainActivity, AddActivity::class.java)
                intent.putExtra("action_type", "add")
                startActivity(intent)
            }

            listView.setOnItemClickListener { adapterView, _, i, _ ->
                val budgetString = adapterView.adapter.getItem(i) as String
                val budget = parseStringToBudget(budgetString)

                val intent = Intent(this@MainActivity, AddActivity::class.java).apply {
                    putExtra("action_type", "update")
                    putExtra("budgets_id", budget.id)
                    putExtra("budgets_nominal", budget.nominal)
                    putExtra("budgets_description", budget.description)
                    putExtra("budgets_date", budget.date)
                }
                startActivity(intent)

                finish()
            }

            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, _, i, _ ->
                val item = adapterView.adapter.getItem(i) as Budget
                deleteBudget(item)
                true
            }
        }

        observeBudget()
        getAllBudget()
    }

    private fun getAllBudget() {
        observeBudgetChanges()
    }

    private fun observeBudget() {
        budgetListLiveData.observe(this) { budgets ->
            val budgetStrings = budgets.map {
                "Nominal: ${it.nominal}, Deskripsi: ${it.description}, Tanggal: ${it.date}"
            }
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                budgetStrings.toMutableList()
            )
            binding.listView.adapter = adapter
        }
    }

    private fun observeBudgetChanges() {
        budgetCollectionRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Log.d("MainActivity", "Error listening for budget changes: ", error)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val budgets = snapshots.toObjects(Budget::class.java)
                budgetListLiveData.postValue(budgets)
            }
        }
    }

    private fun parseStringToBudget(budgetString: String): Budget {
        val parts = budgetString.split(", ")
        val nominal = parts[0].substringAfter("Nominal: ")
        val description = parts[1].substringAfter("Deskripsi: ")
        val date = parts[2].substringAfter("Tanggal: ")

        return Budget(nominal, date, description)
    }

    private fun deleteBudget(budget: Budget) {
        if (budget.id.isEmpty()) {
            Log.d("MainActivity", "Error deleting: budget ID is empty!")
            return
        }

        budgetCollectionRef.document(budget.id).delete()
            .addOnFailureListener {
                Log.d("MainActivity", "Error deleting budget: ", it)
            }
    }
}
