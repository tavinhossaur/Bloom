package com.example.bloom

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.contextaware.ContextAwareHelper
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bloom.databinding.MusicItemLayoutBinding

class MusicaAdapter(private val context: Context, private val listaMusicas: ArrayList<Musica>)  : RecyclerView.Adapter<MusicaAdapter.Holder>() {
    class Holder(binding: MusicItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicaView    // Título da música
        val artista = binding.artistaMusicaView  // Artista da música
        val imagem = binding.imgMusicaView       // Imagem da música
        val duracao = binding.tempoMusicaView    // Duração da música

        val root = binding.root //
    }

    // Um ViewHolder descreve uma exibição de itens e metadados sobre seu lugar dentro do RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(MusicItemLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // Procura na lista de músicas a posição da música em específico e retorna seu título no lugar da caixa de texto da mesma
        holder.titulo.text = listaMusicas[position].titulo
        // Procura na lista de músicas a posição da música em específico e retorna seu artista no lugar da caixa de texto da mesma
        holder.artista.text = listaMusicas[position].artista
        // Procura na lista de músicas a posição da música em específico e retorna sua duração no lugar da caixa de texto da mesma
        // e também faz a formatação da duração da músicas
        holder.duracao.text = formatarDuracao(listaMusicas[position].duracao)

        // Quando clicado na view da música no RecyclerView, o usuário é levado para o player
        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    // Retorna a quantidade total das músicas na lista de músicas
    override fun getItemCount(): Int {
        return listaMusicas.size
    }
}