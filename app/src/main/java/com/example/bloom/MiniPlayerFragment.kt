package com.example.bloom

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bloom.databinding.FragmentMiniPlayerBinding

// A classe do miniplayer é um Fragment, uma porção ou parte de uma interface de usuário
class MiniPlayerFragment : Fragment() {
    // Declaração de um objeto/classe estática
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentMiniPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código
    }
    // Criação da view do miniplayer
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMp = inflater.inflate(R.layout.fragment_mini_player, container, false)
        // Inicialização do binding
        binding = FragmentMiniPlayerBinding.bind(viewMp)
        return viewMp
    }
}