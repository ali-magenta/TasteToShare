package com.example.tastetoshare.repositories

import android.util.Log
import androidx.core.net.toUri
import com.example.tastetoshare.data.FirebaseCollections
import com.example.tastetoshare.data.entities.Country
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.collections.map
import kotlin.collections.plus

class FirebaseCountryRepository(
    private val firestore: FirebaseFirestore,
): CountryRepository {

    private val countryCollection = firestore.collection(FirebaseCollections.COUNTRIES)

    suspend fun initializeData(countries: List<Country>) {
        val existing = countryCollection.limit(1).get().await()
        if (existing.isEmpty) {
            firestore.runBatch { batch ->
                countries.forEach { country ->
                    val docRef = countryCollection.document(country.countryId)
                    batch.set(docRef, country)
                }
            }.await()
        }
    }


    override fun getCountries(): Flow<List<Country>> {
        return countryCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Country::class.java)
            }
    }


    override fun getCountryById(countryId: String): Flow<Country> {
        return countryCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Country::class.java)
            }.map { countries ->  countries.find { it.countryId == countryId }?:  Country()  }
    }

    override fun getCountryByName(name: String): Flow<Country?> {
        return countryCollection.snapshots()
            .map { snapshot ->
                snapshot.toObjects(Country::class.java)
            }.map { countries ->  countries.find { it.name == name }}
    }


    override suspend fun addOrOverrideCountry(country: Country) {
        try {
            val ref = countryCollection.document(country.countryId)
            firestore.runTransaction { transaction ->
                transaction.set(ref, country)
            }.await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error addOrOverrideCountry", e)
        }
    }

    override suspend fun deleteCountry(countryId: String) {
        countryCollection.document(countryId)
            .delete()
            .addOnSuccessListener { Log.d("delete", "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w("delete", "Error deleting document", e) }

    }
}



