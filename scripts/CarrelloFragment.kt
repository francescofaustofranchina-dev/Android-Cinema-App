package com.prg.absolutecinema.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.prg.absolutecinema.R
import com.prg.absolutecinema.adapters.PreventivoBigliettiAdapter
import com.prg.absolutecinema.application.MyApplication
import com.prg.absolutecinema.databinding.FragmentCarrelloBinding
import com.prg.absolutecinema.retrofit.UserAPI
import com.prg.absolutecinema.viewmodels.AbbonamentiViewModel
import com.prg.absolutecinema.viewmodels.FilmViewModel
import com.prg.absolutecinema.viewmodels.ProfiloViewModel

class CarrelloFragment : Fragment(R.layout.fragment_carrello) {

    private lateinit var binding: FragmentCarrelloBinding

    private val filmViewModel: FilmViewModel by activityViewModels<FilmViewModel>()
    private val profiloViewModel: ProfiloViewModel by activityViewModels {
        ProfiloViewModel.factory(
            (requireContext().applicationContext as MyApplication).sharedPreferences
        )
    }
    private val abbonamentiViewModel: AbbonamentiViewModel by activityViewModels {
        AbbonamentiViewModel.factory(
            (requireContext().applicationContext as MyApplication).sharedPreferences
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCarrelloBinding.inflate(inflater, container, false)

        filmViewModel.setNumTicketToUse(0)
        profiloViewModel.getUserPaymentMethod()
        abbonamentiViewModel.getAbbonamento()

        setupListeners()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillFragment()
    }

    //Configura i listeners dei Button del Fragment
    private fun setupListeners() {
        binding.registraCartaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_carrelloFragment_to_registrazioneMetodoPagamentoFragment)
        }

        binding.acquistaBtn.setOnClickListener {
            val email = profiloViewModel.email.value!!
            val numTicketsRimanenti = abbonamentiViewModel.abbonamentoAttivo.value?.ticketRimanenti ?: 0

            filmViewModel.acquistaBiglietti(email, numTicketsRimanenti)
                .observe(viewLifecycleOwner) { isSuccessfull ->
                    if(isSuccessfull) {
                        val numTicketsUsati = filmViewModel.numTicketSelezionato.value ?: 0
                        abbonamentiViewModel.updateNumTicketsAbbonamento(numTicketsUsati)
                        filmViewModel.setIsBackFromCarrelloValue(true)
                        findNavController().navigate(R.id.action_carrelloFragment_to_filmFragment)
                    }
                    else {
                        Toast.makeText(
                            context, getString(R.string.msg_errore), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        binding.ticketsSpinner.setOnItemClickListener { parent, _, position, _ ->
            val selectedNumber = parent.getItemAtPosition(position) as Int
            filmViewModel.setNumTicketToUse(selectedNumber)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        filmViewModel.numTicketSelezionato.observe(viewLifecycleOwner) {
            binding.totaleTxt.text = String.format("Totale: %.2f€", filmViewModel.getCostoTotaleBiglietti())
                .replace(".", ",")
        }

        profiloViewModel.numeroCarta.observe(viewLifecycleOwner) { numeroCarta ->
            if(numeroCarta != null) {
                binding.tableLayout.let {
                    val secondRow = it.getChildAt(1) as TableRow

                    val numeroCartaTxt = secondRow.getChildAt(0) as TextView
                    val proprietarioCartaTxt = secondRow.getChildAt(1) as TextView
                    val scadenzaCartaTxt = secondRow.getChildAt(2) as TextView

                    numeroCartaTxt.text = "Finisce con ${profiloViewModel.numeroCarta.value}"
                    proprietarioCartaTxt.text = profiloViewModel.proprietarioCarta.value.toString()
                    scadenzaCartaTxt.text = profiloViewModel.scadenzaCarta.value.toString()

                    binding.tableLayout.visibility = View.VISIBLE
                }
                binding.noMetodoPagamentoTxt.visibility = View.GONE
                binding.registraCartaBtn.visibility = View.GONE
                binding.acquistaBtn.visibility = View.VISIBLE
            }
            else {
                binding.tableLayout.visibility = View.GONE
                binding.noMetodoPagamentoTxt.visibility = View.VISIBLE
                binding.registraCartaBtn.visibility = View.VISIBLE
                binding.acquistaBtn.visibility = View.GONE
            }
        }

        abbonamentiViewModel.abbonamentoAttivo.observe(viewLifecycleOwner) { abbonamentoAttivo ->
            if(abbonamentoAttivo != null) {
                setupTicketSpinner()
                binding.infoAbbonamentoTxt.text = "Hai ancora a disposizione ${abbonamentoAttivo.ticketRimanenti} " +
                        "ticket da utilizzare per i tuoi biglietti. Indica quanti ticket utilizzare:"

                binding.abbonamentoTxt.visibility = View.VISIBLE
                binding.infoAbbonamentoTxt.visibility = View.VISIBLE
                binding.ticketsDropdown.visibility = View.VISIBLE
            }
            else {
                binding.abbonamentoTxt.visibility = View.GONE
                binding.infoAbbonamentoTxt.visibility = View.GONE
                binding.ticketsDropdown.visibility = View.GONE
            }
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun fillFragment() {
        filmViewModel.filmSelezionato.value?.let {
            Glide.with(this)
                .load("${UserAPI.BASE_URL}/static/img/${it.locandina}")
                .into(binding.locandinaImg)

            binding.titoloFilmTxt.text = it.titolo
        }

        filmViewModel.spettacoloSelezionato.value?.let {
            binding.nomeCinemaAndSalaTxt.text = "${it.cinema.nome} • ${it.sala}"
            binding.dataAndOraSpettacoloTxt.text = it.getDataAndOra()
            binding.tariffaSpettacoloTxt.text = "Tariffa ${it.tariffa}"

            binding.recyclerView.apply {
                adapter = PreventivoBigliettiAdapter(
                    filmViewModel.postiSelezionati.value!!,
                    "${String.format("%.2f", it.prezzo)}€"
                )
                layoutManager = LinearLayoutManager(context)
            }
        }
    }

    //Gestisce la creazione dello spinner contenente il numero di ticket
    //utilizzabili dall'utente per acquistare i biglietti
    private fun setupTicketSpinner() {
        //Sono gli item dello spinner. Ciascun item indica un numero di
        //ticket da utilizzare
        val opzioniDisponibili: Array<Int>
        val totaleItems =
            if(filmViewModel.postiSelezionati.value!!.size >
                abbonamentiViewModel.abbonamentoAttivo.value!!.ticketRimanenti){
                abbonamentiViewModel.abbonamentoAttivo.value!!.ticketRimanenti
            }
            else {
                filmViewModel.postiSelezionati.value!!.size
            }

        opzioniDisponibili = Array(totaleItems + 1) { it }
        val ticketAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            opzioniDisponibili
        )
        ticketAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.ticketsSpinner.setAdapter(ticketAdapter)
    }
}