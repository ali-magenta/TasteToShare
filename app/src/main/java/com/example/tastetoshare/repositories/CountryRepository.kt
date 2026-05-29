package com.example.tastetoshare.repositories;
import com.example.tastetoshare.data.entities.Country;
import kotlinx.coroutines.flow.Flow;

interface CountryRepository {
    fun getCountryById(countryId: String): Flow<Country>
    fun getCountryByName(name: String): Flow<Country?>
    fun getCountries(): Flow<List<Country>>

    suspend fun addOrOverrideCountry(country :Country)
    suspend fun deleteCountry(countryId : String)
}
