package com.example.bloom

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bloom.databinding.ActivityMainBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding   // Variável usada para ligar os componentes da tela

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        setTheme(R.style.Theme_AppCompat_temaClaro)

        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)
    }
}