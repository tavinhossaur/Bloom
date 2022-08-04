package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.MusicViewLayoutBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class MusicaAdapter(private val context: Context, private var listaMusicas: ArrayList<Musica>, private val conteudoPlaylist : Boolean = false, private val activitySelecionar : Boolean = false) : RecyclerView.Adapter<MusicaAdapter.Holder>() {
    class Holder(binding: MusicViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicaView    // Título da música
        val artista = binding.artistaMusicaView  // Artista da música
        val imagem = binding.imgMusicaView       // Imagem da música
        val duracao = binding.tempoMusicaView    // Duração da música
        val selecionado = binding.btnExtraView        // Botão de opções extras

        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a MusicViewLayoutBinding (music_view_layout)
        val root = binding.root
    }

    // Um ViewHolder descreve uma exibição de itens e metadados sobre seu lugar dentro do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(MusicViewLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: Holder, posicao: Int) {
        // Procura na lista de músicas a posição da música em específico e retorna seu título no lugar da caixa de texto da mesma
        holder.titulo.text = listaMusicas[posicao].titulo
        // Procura na lista de músicas a posição da música em específico e retorna seu artista no lugar da caixa de texto da mesma
        holder.artista.text = "${listaMusicas[posicao].artista} | ${listaMusicas[posicao].album}"
        // Procura na lista de músicas a posição da música em específico e retorna sua duração no lugar da caixa de texto da mesma
        // e também faz a formatação da duração da músicas
        holder.duracao.text = formatarDuracao(listaMusicas[posicao].duracao)

        // Utilizando Glide, Procura na lista de músicas a posição da música em específico
        // e retorna sua imagem de álbum no lugar da ImageView da mesma
        Glide.with(context)
            // Carrega a posição da música e a uri da sua imagem
            .load(listaMusicas[posicao].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(holder.imagem)

        when{
            conteudoPlaylist -> {
                holder.root.setOnClickListener {
                    irParaMusica("ConteudoPlaylist", posicao)
                }
            }
            activitySelecionar -> {
                holder.selecionado.visibility = View.INVISIBLE
                holder.selecionado.setImageResource(R.drawable.ic_round_check_circle_24)
                holder.root.setOnClickListener{
                    if (adicionarMusica(listaMusicas[posicao])){
                        holder.selecionado.visibility = View.VISIBLE
                    }else{
                        holder.selecionado.visibility = View.INVISIBLE
                    }
                }
            }
            else -> {
                // Quando clicado na view da música no RecyclerView, o usuário é levado para o player
                holder.root.setOnClickListener {
                    when{
                        // Quando pesquisando for true, chame o método irParaMusica e passe a referência: "Pesquisa", e a posição da música
                        MainActivity.pesquisando -> irParaMusica("Pesquisa", posicao)
                        // Quando a música que for selecionada já estiver tocando, chame o método irParaMusica e passe a referência: "MiniPlayer", e a posição da música no Player
                        listaMusicas[posicao].id == PlayerActivity.musicaAtual -> irParaMusica("MiniPlayer", PlayerActivity.posMusica)
                        // Em qualquer outro caso, chame o método irParaMusica e passe a referência: "Adapter" e a posição da música
                        else -> irParaMusica("Adapter", posicao)
                    }
                }
            }
        }
    }

    // Retorna a quantidade total das músicas na lista de músicas
    override fun getItemCount(): Int {
        return listaMusicas.size
    }

    // Método para levar o usuário a música
    private fun irParaMusica(referencia : String, pos : Int){
        val adapterIntent = Intent(context, PlayerActivity::class.java)
        // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
        adapterIntent.putExtra("indicador", pos)
        // Quando o usuário é levado a tela do player, também é enviado a string da classe que fez a intent
        adapterIntent.putExtra("classe", referencia)
        startActivity(context, adapterIntent, null)
    }

    // Método para atualizar a lista de músicas
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(listaPesquisa : ArrayList<Musica>){
        listaMusicas = ArrayList()
        listaMusicas.addAll(listaPesquisa)
        notifyDataSetChanged()
    }

    // Método para atualizar a lista de playlists
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarPlaylists(){
        listaMusicas = ArrayList()
        listaMusicas = PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist
        notifyDataSetChanged()
    }

    fun adicionarMusica(musica : Musica) : Boolean{
        PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.forEachIndexed { index, musicas ->
            if (musica.id == musicas.id){
                PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.removeAt(index)
                return false
            }
        }
        PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.add(musica)
        return true
    }
}