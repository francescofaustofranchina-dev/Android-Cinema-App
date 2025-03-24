package com.prg.absolutecinema.activities

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.prg.absolutecinema.R
import com.prg.absolutecinema.application.MyApplication
import com.prg.absolutecinema.databinding.ActivityNavigationBinding
import com.prg.absolutecinema.databinding.PopupStandardBinding
import com.prg.absolutecinema.viewmodels.AbbonamentiViewModel
import com.prg.absolutecinema.viewmodels.FilmViewModel
import com.prg.absolutecinema.viewmodels.ProfiloViewModel

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding

    private val profiloViewModel: ProfiloViewModel by viewModels {
        ProfiloViewModel.factory(
            (applicationContext as MyApplication).sharedPreferences
        )
    }
    private lateinit var filmviewModel: FilmViewModel
    private val abbonamentiViewModel: AbbonamentiViewModel by viewModels {
        AbbonamentiViewModel.factory(
            (applicationContext as MyApplication).sharedPreferences
        )
    }

    private lateinit var navController: NavController

    private var actionBar: ActionBar? = null
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationBinding.inflate(layoutInflater)

        profiloViewModel.getUserPaymentMethod()
        abbonamentiViewModel.getAbbonamento()
        filmviewModel = ViewModelProvider(this)[FilmViewModel::class.java]
        filmviewModel.requestListeFilm("In programmazione")
        filmviewModel.requestListeFilm("In arrivo")

        val view = binding.root
        setContentView(view)

        setupActionBar()
        setupNavController()
        setupDrawerLayout()
        setupListeners()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(drawerToggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }

    //Imposta l'ActionBar della DraweActivity
    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragmentContainer.id) as NavHostFragment
        navController = navHostFragment.navController
    }

    //Configura il DrawerLayout laterale
    private fun setupDrawerLayout() {
        drawerLayout = binding.drawerLayout
        drawerToggle = ActionBarDrawerToggle(this, drawerLayout, binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    //Configura i listeners degli elementi della UI
    private fun setupListeners() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true

            when(menuItem.itemId) {
                R.id.menu_profilo -> navController.navigate(R.id.action_filmFragment_to_profiloFragment)
                R.id.menu_miei_biglietti -> navController.navigate(R.id.action_filmFragment_to_iMieiBigliettiFragment)
                R.id.menu_abbonamenti -> navController.navigate(R.id.action_filmFragment_to_abbonamentiFragment)
                R.id.menu_assistenza_cliente -> navController.navigate(R.id.action_filmFragment_to_assistenzaClienteFragment)
                R.id.menu_logout -> showPopupLogout()
            }

            drawerLayout.closeDrawers()
            true
        }

        //Apporta modifiche all'ActionBar quando cambia la schermata
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val title = destination.label
            if(title != null) {
                val spannableTitle = SpannableString(title)
                spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0,
                    title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                val actionBarTitle = binding.toolbarTitle
                actionBarTitle.text = spannableTitle

                if(destination.id == R.id.filmFragment) {
                    showBackButton(false)
                    binding.navigationView.setCheckedItem(R.id.menu_film)
                }
                else {
                    showBackButton(true)
                }
            }
        }
    }

    //Mostra schermo il pop-up di conferma per effettuare il logout
    private fun showPopupLogout() {
        val popupBinding = PopupStandardBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(popupBinding.root)
        val popup = builder.create()

        setupStandardPopupListeners(popupBinding, popup)
        bindStandardPopup(popupBinding)
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
    private fun setupStandardPopupListeners(popupBinding: PopupStandardBinding, popup: AlertDialog) {
        popupBinding.confermaBtn.setOnClickListener {
            logout()
            popup.dismiss()
        }

        popupBinding.annullaBtn.setOnClickListener {
            popup.dismiss()
        }

        popup.setOnDismissListener {
            binding.navigationView.setCheckedItem(R.id.menu_film)
        }
    }

    //Riempie il contenuto del Popup
    private fun bindStandardPopup(popupBinding: PopupStandardBinding) {
        popupBinding.titoloTxt.text = getString(R.string.title_sei_sicuro)
        popupBinding.icon.setImageResource(R.drawable.ic_advice)
        popupBinding.messaggioTxt.text = getString(R.string.msg_logout)
    }

    //Effettua il logout dall'applicazione
    private fun logout() {
        profiloViewModel.removeUserLocalData()
        endUserSession()
    }

    //Naviga nel MainActivity
    private fun endUserSession() {
        val options = ActivityOptions.makeCustomAnimation(
            this, R.anim.slide_out_down, R.anim.slide_in_down
        )

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent, options.toBundle())
    }

    //Rende visibile o nasconde il tasto indietro nell'ActionBar
    private fun showBackButton(show: Boolean) {
        if(show) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawerToggle.isDrawerIndicatorEnabled = false
            actionBar?.setDisplayShowTitleEnabled(false)
            actionBar?.setDisplayHomeAsUpEnabled(true)
            drawerToggle.setToolbarNavigationClickListener {
                navController.popBackStack()
            }
        }
        else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            actionBar?.setDisplayHomeAsUpEnabled(false)
            drawerToggle.isDrawerIndicatorEnabled = true
            drawerToggle.toolbarNavigationClickListener = null
        }
    }
}