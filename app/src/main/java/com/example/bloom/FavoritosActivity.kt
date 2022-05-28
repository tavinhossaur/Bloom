package com.example.bloom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.example.bloom.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_favoritos.*

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding   // Variável usada para ligar os componentes da tela

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        setTheme(R.style.temaClaroNav)

        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        btn_menu_fav.setOnClickListener{setDrawer()}
    }

    // Método para abrir e fechar o DrawerLayout
    private fun setDrawer() {
        drawer_layout_fav.openDrawer(GravityCompat.START)
    }
}