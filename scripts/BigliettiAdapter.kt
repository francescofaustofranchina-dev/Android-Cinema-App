package com.prg.absolutecinema.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.prg.absolutecinema.data.Biglietto
import com.prg.absolutecinema.databinding.ViewBigliettoBinding
import com.prg.absolutecinema.retrofit.UserAPI.Companion.BASE_URL
import java.time.format.DateTimeFormatter

class BigliettiAdapter : RecyclerView.Adapter<BigliettiAdapter.ViewHolder>() {

    private val listaBiglietti: MutableList<Biglietto> = mutableListOf()

    class ViewHolder(val binding: ViewBigliettoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewBigliettoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val biglietto = listaBiglietti[position]

        Glide.with(holder.binding.locandinaImg.context)
            .load("$BASE_URL/static/img/${biglietto.locandina}")
            .into(holder.binding.locandinaImg)

        holder.binding.titoloFilmTxt.text = biglietto.titoloFilm
        holder.binding.cinemaTxt.text = "${biglietto.nomeCinema} • ${biglietto.sala}"
        holder.binding.salaPostoTxt.text = "Posto: ${biglietto.fila}${biglietto.numeroPosto}"
        holder.binding.dataOraTxt.text = "${biglietto.dataSpettacolo
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))} • ${biglietto.oraSpettacolo}"
        val prezzo = String.format("%.2f€",biglietto.prezzo).replace(".", ",")
        holder.binding.tariffaTxt.text = "Tariffa ${biglietto.tariffa}: $prezzo"
    }

    override fun getItemCount(): Int = listaBiglietti.size

    //Aggiorna il contenuto della RecyclerView contenente
    //i biglietti acquistati dall'utente
    fun updateListaBiglietti(listaBiglietti: List<Biglietto>) {
        this.listaBiglietti.clear()
        this.listaBiglietti.addAll(listaBiglietti)
        notifyDataSetChanged()
    }
}