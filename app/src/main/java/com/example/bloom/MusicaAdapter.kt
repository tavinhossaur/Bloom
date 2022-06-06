package com.example.bloom

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom.databinding.MusicItemLayoutBinding

class MusicaAdapter(private val context: Context, private val listaMusicas: ArrayList<String>)  : RecyclerView.Adapter<MusicaAdapter.Holder>() {
    class Holder(binding: MusicItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicaView    // Título da música
        val artista = binding.artistaMusicaView  // Artista da música
        val imagem = binding.imgMusicaView       // Imagem da música
        val duracao = binding.tempoMusicaView    // Duração da música
    }

    // Um ViewHolder descreve uma exibição de itens e metadados sobre seu lugar dentro do RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(MusicItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.titulo.text = listaMusicas[position]
    }

    override fun getItemCount(): Int {
        return listaMusicas.size
    }
}