package com.example.bloom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.example.bloom.databinding.ActivityMainBinding
import com.example.bloom.databinding.ActivityPermissaoBinding
import com.example.bloom.databinding.ActivityPlaylistsBinding
import kotlinx.android.synthetic.main.activity_playlists.*

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlaylistsBinding // binding é a variável do ViewBinding para ligar as views ao código

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPlaylistsBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlaylistsBinding (activity_playlists.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)

        binding.btnMenuPl.setOnClickListener{setDrawer()}
    }

    // Método para abrir e fechar o DrawerLayout
    private fun setDrawer() {
       binding.drawerLayoutPl.openDrawer(GravityCompat.START)
    }
}