package com.prg.absolutecinema.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class Spettacolo(
    val id: Int,
    val cinema: Cinema,
    val tariffa: String,
    val prezzo: Double,
    val sala: String,
    val dataSpettacolo: LocalDate,
    val oraSpettacolo: String
) {

    //Restituisce la data e l'ora dello spettacolo nel formato illustrato:
    //es. 13 settembre • 17:30
    fun getDataAndOra(): String {
        val data = LocalDate.parse(
            dataSpettacolo.toString(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        )
        val nomeDelMese = data.month.getDisplayName(
            java.time.format.TextStyle.FULL, Locale("it")
        )
        return "${data.dayOfMonth} $nomeDelMese • ${oraSpettacolo.take(5)}"
    }

    //Restituisce la data e l'ora dello spettacolo nel formato illustrato:
    //es. 13/09/2024
    fun getDataFormatted(): String {
        val formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return dataSpettacolo.format(formatterData)
    }
}

class Cinema(val nome: String, val citta: String, val via: String)