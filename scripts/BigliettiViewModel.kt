package com.prg.absolutecinema.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.prg.absolutecinema.data.Biglietto
import com.prg.absolutecinema.retrofit.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BigliettiViewModel(private val sharedPreferences: SharedPreferences) : ViewModel() {

    private val _listaBiglietti = MutableLiveData<MutableList<Biglietto>>(mutableListOf())
    val listaBiglietti: LiveData<MutableList<Biglietto>>
       get() = _listaBiglietti

    fun requestListaBiglietti() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val email = sharedPreferences.getString("email", null) ?: ""
                val response = Client.retrofit.getBiglietti(email)
                val responseBody = response.body()

                if(response.isSuccessful && responseBody != null) {
                    _listaBiglietti.postValue(parseJsonToBiglietto(responseBody))
                }
            }
            catch(e: Exception) { }
        }
    }

    private fun parseJsonToBiglietto(jsonString: JsonArray): MutableList<Biglietto> {
        val result: MutableList<Biglietto> = mutableListOf()

        for (jsonElement in jsonString) {
            val biglietto = jsonElement.asJsonObject

            val locandina = biglietto.get("locandina").asString
            val titoloFilm = biglietto.get("titoloFilm").asString
            val fila = biglietto.get("fila").asString[0]
            val numeroPosto = biglietto.get("numeroPosto").asInt
            val nomeCinema = biglietto.get("nomeCinema").asString
            val sala = biglietto.get("sala").asString
            val dataSpettacolo = LocalDate.parse(
                biglietto.get("dataSpettacolo").asString,
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )
            val oraSpettacolo = biglietto.get("oraSpettacolo").asString.take(5)
            val tariffa = biglietto.get("tariffa").asString
            val prezzo = biglietto.get("prezzo").asDouble

            result.add(Biglietto(
                locandina, titoloFilm, fila, numeroPosto, nomeCinema,
                sala, dataSpettacolo, oraSpettacolo, tariffa, prezzo
            ))
        }
        return result
    }

    companion object {
        //Crea un'istanza di ViewModel.Factory, che consente di definire
        //come creare il ViewModel
        fun factory(sharedPreferences: SharedPreferences) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if(modelClass.isAssignableFrom(BigliettiViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return BigliettiViewModel(sharedPreferences) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}