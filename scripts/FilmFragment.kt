package com.prg.absolutecinema.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.prg.absolutecinema.viewmodels.FilmViewModel
import com.prg.absolutecinema.adapters.FragmentPageAdapter
import com.prg.absolutecinema.R
import com.prg.absolutecinema.databinding.FragmentFilmBinding
import com.prg.absolutecinema.databinding.PopupSimpleBinding

class FilmFragment : Fragment(R.layout.fragment_film) {

    private lateinit var binding: FragmentFilmBinding

    private val viewModel: FilmViewModel by activityViewModels<FilmViewModel>()

    private var filmInProgrammazioneListUpdated: Boolean = false
    private var filmInArrivoListUpdated: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilmBinding.inflate(inflater, container, false)

        setupViewPager()
        setupListeners()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Mostra il Popup di conferma dopo aver acquistato dei biglietti
        if(viewModel.isBackFromCarrello.value == true) {
            showNoticePopup()
            viewModel.setIsBackFromCarrelloValue(false)
        }
    }

    //Imposta il FragmentPageAdapter per il caricamento dei Fragment
    //nel ViewPager2
    private fun setupViewPager() {
        binding.viewPager.adapter = FragmentPageAdapter(
            childFragmentManager,
            lifecycle
        )
    }

    //Configura i listeners degli elementi del Fragment
    private fun setupListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let { binding.viewPager.currentItem = tab.position }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabReselected(tab: TabLayout.Tab?) { }
        })

        //Sincronizza il TabLayout con lo stato del ViewPager2
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        //Definisce il comportamento dello SwipeRefreshLayout quando l'utente
        //richiede un aggiornamento tramite uno swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener {
            filmInProgrammazioneListUpdated = false
            filmInArrivoListUpdated = false

            viewModel.requestListeFilm("In programmazione")
            viewModel.requestListeFilm("In arrivo")
        }
    }

    private fun setupObservers() {
        viewModel.filmInProgrammazioneList.observe(viewLifecycleOwner) { _ ->
            filmInProgrammazioneListUpdated = true
            checkIfRefreshIsCompleted()
        }

        viewModel.filmInArrivoList.observe(viewLifecycleOwner) { _ ->
            filmInArrivoListUpdated = true
            checkIfRefreshIsCompleted()
        }
    }

    private fun checkIfRefreshIsCompleted() {
        if(filmInProgrammazioneListUpdated && filmInArrivoListUpdated) {
            binding.swipeRefreshLayout.isRefreshing = false
            filmInProgrammazioneListUpdated = false
            filmInArrivoListUpdated = false
        }
    }

    //Mostra a schermo il pop-up di conferma dell'acquisto dei biglietti
    private fun showNoticePopup() {
        val popupBinding = PopupSimpleBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(popupBinding.root)
        val popup = builder.create()

        setupSimplePopupListeners(popupBinding, popup)
        bindSimplePopup(popupBinding)
        popup.show()

        //Rimuove i margini dell'AlertDialog
        popup.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Imposta la larghezza del popup per coprire tutta la larghezza della finestra
        popup.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    //Configura i listeners dei Button del Popup
    private fun setupSimplePopupListeners(popupBinding: PopupSimpleBinding, popup: AlertDialog) {
        popupBinding.okBtn.setOnClickListener {
            popup.dismiss()
        }
    }

    //Riempie il contenuto del Popup
    private fun bindSimplePopup(popupBinding: PopupSimpleBinding) {
        popupBinding.titoloTxt.text = getString(R.string.title_acquisto_effettuato)
        popupBinding.messaggioTxt.text = getString(R.string.msg_acquisto_effettuato)
        popupBinding.icon.setImageResource(R.drawable.ic_check)
    }
}