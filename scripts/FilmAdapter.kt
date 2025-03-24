package com.prg.absolutecinema.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prg.absolutecinema.databinding.ViewFilmBinding
import com.bumptech.glide.Glide
import com.prg.absolutecinema.data.Film
import com.prg.absolutecinema.retrofit.UserAPI.Companion.BASE_URL

interface OnFilmClickListener {
    fun onFilmClick(film: Film)
}

class FilmAdapter(
    private val clickListener: OnFilmClickListener
) : RecyclerView.Adapter<FilmAdapter.ViewHolder>() {

    private val listaFilm: MutableList<Film> = mutableListOf()

    class ViewHolder(val binding: ViewFilmBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewFilmBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val viewHolder = ViewHolder(binding)
        setupListeners(viewHolder)

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val film = listaFilm[position]

        Glide.with(holder.binding.locandina.context)
            .load("$BASE_URL/static/img/${film.locandina}")
            .into(holder.binding.locandina)

        holder.binding.titolo.text = film.titolo
        if(film.listaTariffe.any { it.nome == "Evento" }) {
            holder.binding.tagEventoTxt.visibility = View.VISIBLE
        }
        else {
            holder.binding.tagEventoTxt.visibility = View.GONE
        }
    }

    override fun getItemCount() = listaFilm.size

    //Configura i listeners del ViewHolder
    private fun setupListeners(viewHolder: ViewHolder) {
        viewHolder.binding.filmCardView.setOnClickListener {
            val film = listaFilm[viewHolder.adapterPosition]
            clickListener.onFilmClick(film)
        }
    }

    //Aggiorna la lista dei film visualizzati nel Fragment
    fun updateListaFilm(listaFilm: List<Film>) {
        this.listaFilm.clear()
        this.listaFilm.addAll(listaFilm)
        notifyDataSetChanged()
    }
}