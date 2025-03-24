package com.prg.absolutecinema.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.prg.absolutecinema.data.Film
import com.prg.absolutecinema.R
import com.prg.absolutecinema.databinding.FragmentDettagliFilmBinding
import com.prg.absolutecinema.retrofit.UserAPI.Companion.BASE_URL
import com.prg.absolutecinema.viewmodels.FilmViewModel
import java.time.LocalDate

class DettagliFilmFragment : Fragment(R.layout.fragment_dettagli_film) {

    private lateinit var binding: FragmentDettagliFilmBinding

    private val viewModel: FilmViewModel by activityViewModels<FilmViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDettagliFilmBinding.inflate(inflater, container, false)

        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.requestListaSpettacoliFilm()

        val film = viewModel.filmSelezionato.value
        film?.let { fillFragment(it) }

        binding.visualizzaSpettacoliBtn.visibility = if(film?.stato == "In programmazione") {
            View.VISIBLE
        }
        else {
            View.GONE
        }
    }

    //Configura i listeners dei Button del Fragment
    private fun setupListeners() {
        binding.trailerBtn.setOnClickListener {
            openTrailerFilm()
        }

        binding.visualizzaSpettacoliBtn.setOnClickListener {
            findNavController().navigate(R.id.action_dettagliFilmFragment_to_spettacoliFilmFragment)
        }
    }

    //Riempie le TextView e le ImageView del Fragment
    @SuppressLint("SetTextI18n")
    private fun fillFragment(film: Film) {
        binding.titolo.text = film.titolo
        binding.annoEtaDurataGenere.text = "${film.annoDiUscita} • ${film.etaMinima} • " +
                                           "${film.durata}m • ${film.genere}"
        binding.regia.text = film.regia
        binding.cast.text = film.cast
        binding.lingua.text = film.lingua
        binding.trama.text = film.trama

        Glide.with(this)
            .load("$BASE_URL/static/img/${film.banner}")
            .into(binding.banner)

        Glide.with(this)
            .load("$BASE_URL/static/img/${film.locandina}")
            .into(binding.locandinaDettagli)

        binding.programmazione.text = getProgrammazioneFilm(film)
        binding.tariffe.text = getTariffeFilm(film)
    }

    //Compone il campo di testo della programmazione del film
    private fun getProgrammazioneFilm(film: Film): String {
        return with(StringBuilder()) {
            if(film.stato == "In programmazione") {
                if(film.dataUltimoSpettacolo == LocalDate.now()) {
                    append("Disponibile fino ad oggi.")
                }
                else {
                    append("Disponibile fino al ${film.dataUltimoSpettacoloFormatted}.")
                }
            }
            else {
                append("Disponibile a partire dal ${film.dataPrimoSpettacoloFormatted} ")
                append("fino al ${film.dataUltimoSpettacoloFormatted}.")
            }
            toString()
        }
    }

    //Compone il campo di testo delle tariffe del film
    private fun getTariffeFilm(film: Film): String {
        if(film.listaTariffe.isEmpty()) {
            return "Nessuna tariffa disponibile."
        }

        return with(StringBuilder()) {
            for(tariffa in film.listaTariffe) {
                append("- ${tariffa.nome}")

                if(tariffa != film.listaTariffe.last()) {
                    append("\n")
                }
            }
            toString()
        }
    }

    //Apre il trailer del film su YouTube
    private fun openTrailerFilm() {
        viewModel.filmSelezionato.value?.trailer.let {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
            }
            catch(e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(), getString(R.string.msg_errore), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}