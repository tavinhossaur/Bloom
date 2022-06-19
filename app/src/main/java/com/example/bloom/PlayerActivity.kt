package com.example.bloom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bloom.databinding.ActivityMainBinding
import com.example.bloom.databinding.ActivityPermissaoBinding
import com.example.bloom.databinding.ActivityPlayerBinding
import kotlinx.android.synthetic.main.activity_player.*

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlayerBinding (activity_player.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)
    }
}