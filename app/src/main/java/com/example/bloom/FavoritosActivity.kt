package com.example.bloom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.example.bloom.databinding.ActivityFavoritosBinding
import com.example.bloom.databinding.ActivityMainBinding
import com.example.bloom.databinding.ActivityPlayerBinding
import kotlinx.android.synthetic.main.activity_favoritos.*

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding : ActivityFavoritosBinding   // Variável usada para ligar os componentes da tela

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityFavoritosBinding (activity_favoritos.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarFav.setOnClickListener {finish()}
        // Abrir a gaveta lateral de opções (Drawer)
        binding.btnMenuFav.setOnClickListener {setDrawer()}
    }

    // Método para abrir e fechar o DrawerLayout
    private fun setDrawer() {
        binding.drawerLayoutFav.openDrawer(GravityCompat.START)
    }
}