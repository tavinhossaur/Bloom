package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.FavoritosViewLayoutBinding

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class FavoritosAdapter(private val context: Context, private var listaFavoritos: ArrayList<Musica>) : RecyclerView.Adapter<FavoritosAdapter.Holder>() {
    class Holder(binding: FavoritosViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicafavView    // Título da música
        val artista = binding.artistaMusicafavView  // Artista da música
        val imagem = binding.imgMusicafavView       // Imagem da música
        val duracao = binding.tempoMusicafavView    // Duração da música

        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a FavoritosViewLayoutBinding (favoritos_view_layout)
        val root = binding.root
    }

    // Um ViewHolder descreve uma exibição de itens e metadados sobre seu lugar dentro do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(FavoritosViewLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: Holder, posicao: Int) {
        // Procura na lista de músicas a posição da música em específico e retorna seu título no lugar da caixa de texto da mesma
        holder.titulo.text = listaFavoritos[posicao].titulo
        // Procura na lista de músicas a posição da música em específico e retorna seu artista no lugar da caixa de texto da mesma
        holder.artista.text = listaFavoritos[posicao].artista
        // Procura na lista de músicas a posição da música em específico e retorna sua duração no lugar da caixa de texto da mesma
        // e também faz a formatação da duração da músicas
        holder.duracao.text = formatarDuracao(listaFavoritos[posicao].duracao)

        // Utilizando Glide, Procura na lista de músicas a posição da música em específico
        // e retorna sua imagem de álbum no lugar da ImageView da mesma
        Glide.with(context)
            // Carrega a posição da música e a uri da sua imagem
            .load(listaFavoritos[posicao].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(holder.imagem)

        // Quando clicado na view da música no RecyclerView, o usuário é levado para o player
        holder.root.setOnClickListener {
            val adapterIntent = Intent(context, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            adapterIntent.putExtra("indicador", posicao)
            // Quando o usuário é levado a tela do player, também é enviado a string da classe que fez a intent
            adapterIntent.putExtra("classe", "Favoritos")
            ContextCompat.startActivity(context, adapterIntent, null)
        }
    }

    // Retorna a quantidade total das músicas na lista de músicas
    override fun getItemCount(): Int {
        return listaFavoritos.size
    }

    // Método para atualizar a lista de músicas
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(listaPesquisa : ArrayList<Musica>){
        listaFavoritos = ArrayList()
        listaFavoritos.addAll(listaPesquisa)
        notifyDataSetChanged()
    }
}