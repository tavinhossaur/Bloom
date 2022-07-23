package com.example.bloom

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.bloom.databinding.ActivityPlaylistsBinding

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlaylistsBinding // binding é a variável do ViewBinding para ligar as views ao código

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityPlaylistsBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlaylistsBinding (activity_playlists.xml)
        setContentView(binding.root)

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarPl.setOnClickListener {finish()}
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_BloomNoActionBar)
        setTheme(R.style.Theme_BloomNoActionBar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }
}