package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.PlaylistViewLayoutBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class PlaylistsAdapter(private val context: Context, private var listaPlaylists: ArrayList<Playlist>) : RecyclerView.Adapter<PlaylistsAdapter.Holder>() {

    class Holder(binding: PlaylistViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val nome = binding.nomePlaylist    // Título da música
        val criador = binding.criadorPlaylist  // Artista da música
        val imagem = binding.imgPlaylist       // Imagem da música

        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a PlaylistViewLayoutBinding (playlist_view_layout)
        val root = binding.root
    }

    // Um ViewHolder descreve uma exibição de itens e metadados sobre seu lugar dentro do RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(PlaylistViewLayoutBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    // Carregado toda vez que um elemento é adicionado na lista
    override fun onBindViewHolder(holder: Holder, posicao: Int) {
        // Procura na lista de músicas a posição da música em específico e retorna seu título no lugar da caixa de texto da mesma
        holder.nome.text = listaPlaylists[posicao].nome
        holder.criador.text = "Criado por: ${listaPlaylists[posicao].criador}"

        // Quando o usuário clicar no card, é enviado para a tela de conteúdo da playlist
        holder.root.setOnClickListener{
            val playlistIntent = Intent(context, ConteudoPlaylistActivity::class.java)
            playlistIntent.putExtra("Playlist", posicao)
            startActivity(context, playlistIntent, null)
        }

        // Ajuste de cores para o modo escuro do Android
        if (PlaylistsActivity.escuroPl){
            holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black6))
        }

        // Se a playlist não estiver vazia
        if (PlaylistsActivity.playlists.modelo[posicao].playlist.isNotEmpty()){
            // Utilizando Glide, Procura na lista de músicas a posição da música em específico
            // e retorna sua imagem de álbum no lugar da ImageView da mesma
            Glide.with(context)
                // Carrega a posição da música e a uri da sua imagem
                .load(PlaylistsActivity.playlists.modelo[posicao].playlist[0].imagemUri)
                // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                // junto do método centerCrop() para ajustar a imagem dentro da view
                .apply(RequestOptions().placeholder(R.drawable.placeholder_bloom_grey).centerCrop())
                // Alvo da aplicação da imagem
                .into(holder.imagem)
        // Caso a playlist esteja vazia
        }else{
            Glide.with(context)
                // Carrega a posição da imagem placeholder
                .load(R.drawable.placeholder_bloom_grey)
                // Faz a aplicação da imagem com o método centerCrop() para ajustar a imagem dentro da view
                .apply(RequestOptions().centerCrop())
                // Alvo da aplicação da imagem
                .into(holder.imagem)
        }

        // Quando o usuário clicar e segurar no card da playlist, um menu será aberto
        holder.root.setOnLongClickListener {
            // Cria o popup menu
            val contexto: Context = ContextThemeWrapper(context, R.style.PopupMenuStyle)
            val popup = PopupMenu(contexto, holder.root, Gravity.CENTER)
            popup.setForceShowIcon(true)
            // Infla o menu do card
            popup.inflate(R.menu.card_menu)
            // Adicionando o listener das opções do menu
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Editar playlist
                    R.id.edit_playlist -> {
                        // Cria e mostra a InputSheet
                        // Lista de nomes de playlists para hint da input no BottomSheetDialog
                        val nomespl = arrayOf("Músicas para viagem...", "Músicas para banho...", "Músicas para relaxar...", "Músicas para estudar...", "Músicas para liberar a raiva...")
                        InputSheet().show(context) {
                            // Estilo do sheet (BottomSheet)
                            style(SheetStyle.BOTTOM_SHEET)
                            // Título do BottomSheetDialog
                            title("Editar a playlist")
                            // Conteúdo da sheet (Edit Texts)
                            with(InputEditText("nome_playlist") {
                                required(true)
                                drawable(R.drawable.ic_round_folder_24)
                                label("Insira o novo nome da playlist")
                                hint(nomespl.random())
                            })
                            // Cor do botão "confirmar"
                            positiveButtonColorRes(R.color.purple1)
                            // Botão confirmar do BottomSheet
                            onPositive("Editar") { result ->
                                val nomePlaylist = result.getString("nome_playlist").toString()
                                // Muda o nome da playlist
                                PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].nome = nomePlaylist
                                holder.nome.text = nomePlaylist
                            }
                            // Cor do botão negativo
                            negativeButtonColorRes(R.color.grey3)
                        }
                        true
                    }
                    // Quando clicado na opção pra excluir a playlist, mostra um alertDialog perguntando se o usuário
                    // deseja mesmo excluí-la.
                    R.id.delete_playlist -> {
                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                        val permSheet = InfoSheet().build(context) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Título do AlertDialog
                            title("Deseja mesmo excluir a playlist?")
                            // Mensagem do AlertDialog
                            content("Excluir a playlist \"${listaPlaylists[posicao].nome}\"?")
                            // Botão positivo que exclui a playlist em questão
                            positiveButtonColorRes(R.color.purple1)
                            onPositive("Sim, excluir") {
                                // Remove a playlist da lista
                                PlaylistsActivity.playlists.modelo.removeAt(posicao)
                                // Atualiza a lista de playlists
                                atualizarLista()
                                // Notifica que a playlist foi removida
                                notifyItemRemoved(posicao)
                                // Verifica se essa exclusão fez com que ficassem 0 itens na lista de playlists
                                if (itemCount == 0) {
                                    PlaylistsActivity.binding.playlistsRv.visibility = View.GONE
                                    PlaylistsActivity.binding.avisoPlaylists.visibility = View.VISIBLE
                                }
                            }
                            // Cor do botão negativo
                            negativeButtonColorRes(R.color.grey3)
                        }
                        // Mostra o AlertDialog
                        permSheet.show()
                        true
                    }
                    else -> false
                }
            }
            // Mostra o menu popup
            popup.show()
            true
        }
    }

    // Retorna a quantidade total das músicas na lista de músicas
    override fun getItemCount(): Int {
        return listaPlaylists.size
    }

    // Método para atualizar a lista de músicas
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarLista(){
        listaPlaylists = ArrayList()
        listaPlaylists.addAll(PlaylistsActivity.playlists.modelo)
        notifyDataSetChanged()
    }
}