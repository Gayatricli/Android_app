package com.example.stressease.SOS

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stressease.Api.CrisisContact
import com.example.stressease.Api.RetrofitClient
import com.example.stressease.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SOS : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SOSAdapter
    private lateinit var spinnerCountry: Spinner
    private lateinit var btnFetchContacts: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos)

        recyclerView = findViewById(R.id.rvSOSContacts)
        spinnerCountry = findViewById(R.id.spinnerCountry)
        btnFetchContacts = findViewById(R.id.btnFetchContacts)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SOSAdapter()
        recyclerView.adapter = adapter

        // Example dropdown countries
        val countries = listOf("Select Country", "India", "USA", "UK", "Canada", "Australia")
        val spinnerAdapter = ArrayAdapter(this, R.layout.spinner_item_country ,countries)
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_drop_down)
        spinnerCountry.adapter = spinnerAdapter

        btnFetchContacts.setOnClickListener {
            val selectedCountry = spinnerCountry.selectedItem.toString()

            if (selectedCountry == "Select Country") {
                Toast.makeText(this, "Please select a country", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("SOS", "Selected country = $selectedCountry")
                fetchCrisisContacts(selectedCountry)
            }
        }
    }

    private fun fetchCrisisContacts(country: String) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val token = prefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("SOS", "Sending request to Flask with country=$country")
                val response = RetrofitClient.api.getCrisisContacts("Bearer $token", country)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {

                        val contacts: List<CrisisContact> = response.body()?.resources?.crisisHotlines ?: emptyList()

                        Log.d("SOS", "Received ${contacts.size} contacts from Flask")

                        if (contacts.isNotEmpty()) {
                            recyclerView.visibility = View.VISIBLE
                            adapter.setData(contacts)
                        } else {
                            adapter.setData(emptyList())
                            Toast.makeText(this@SOS, "No contacts found for $country", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(
                            this@SOS,
                            "Failed: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("SOS", "Flask error: ${response.errorBody()?.string()}")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SOS", "Exception sending request: ${e.message}")
                    Toast.makeText(this@SOS, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}





