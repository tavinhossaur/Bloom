package com.example.bloom

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.FavoritosViewLayoutBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import java.io.File

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class FavoritosAdapter(private val context: Context, private var listaFavoritos: ArrayList<Musica>) : RecyclerView.Adapter<FavoritosAdapter.Holder>() {

    class Holder(binding: FavoritosViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicafavView    // Título da música
        val artista = binding.artistaMusicafavView  // Artista da música
        val imagem = binding.imgMusicafavView       // Imagem da música
        val duracao = binding.tempoMusicafavView    // Duração da música
        val btnExtra = binding.btnExtrafavView      // Botão de opções extras
        val btnFav = binding.favBtn                 // Botão de favorito

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

        holder.btnFav.setOnClickListener {
            checarFavoritos(listaFavoritos[posicao].id)
            // Se a música já estiver favoritada
            if (PlayerActivity.favoritado) {
                // Então defina a variável favoritado para false
                PlayerActivity.favoritado = false
                // Mude o ícone para o coração vazio de desfavoritado das views do miniplayer e da notificação
                // apenas se elas estiverem sendo visíveis porque o serviço de música não está nulo
                if (PlayerActivity.musicaService != null) {
                    MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
                    setBtnsNotify()
                }
                holder.btnFav.setImageResource(R.drawable.ic_round_favorite_border_24)
                Handler(Looper.getMainLooper()).postDelayed({
                    // Remove a música da lista de favoritos
                    listaFavoritos.remove(listaFavoritos[posicao])
                    // E notifica que ela foi removida
                    notifyItemRemoved(posicao)
                    // Verifica se essa exclusão fez com que ficassem 0 itens na lista de favoritos
                    if (itemCount == 0) {
                        FavoritosActivity.binding.favoritosRv.visibility = View.INVISIBLE
                        FavoritosActivity.binding.avisoFavoritas.visibility = View.VISIBLE
                        FavoritosActivity.binding.fabFavRandom.visibility = View.INVISIBLE
                    }
                }, 300)
            }
        }

        // Quando o usuário clicar no botão de opções extras
        holder.btnExtra.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            holder.btnExtra.isEnabled = false
            OptionsSheet().show(context) {
                title("${listaFavoritos[posicao].titulo} | ${listaFavoritos[posicao].artista}")
                titleColorRes(R.color.white)
                // Altera o botão de fechar o dialogo
                closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
                // Marca como falso as múltiplas opções
                multipleChoices(false)
                // Mantém as cores dos ícones
                preventIconTint(true)
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { holder.btnExtra.isEnabled = true }
                with(
                    Option(R.drawable.ic_round_share_24, "Compartilhar"),
                    Option(R.drawable.ic_round_delete_forever_24, "Excluir"),
                    Option(R.drawable.ic_round_info_24, "Detalhes")
                )
                onPositive { index: Int, _: Option ->
                    when(index){
                        0 -> {
                            // Cria a intent para o compartilhamento
                            val compartIntent = Intent()
                            // Define a ação que será feita na intent (ACTION_SEND), ("enviar")
                            compartIntent.action = Intent.ACTION_SEND
                            // Define o tipo do conteúdo que será enviado, no caso, audio
                            compartIntent.type = "audio/*"
                            // Junto da intent, com putExtra, estão indo os dados da música com base no caminho da música selecionada
                            compartIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaFavoritos[posicao].caminho))
                            // Inicia a intent chamando o método "createChooser" que mostra um BottomSheetDialog com opções de compartilhamento (Bluetooth, Whatsapp, Drive)
                            // E um título que aparece no BottomSheetDialog
                            startActivity(Intent.createChooser(compartIntent, "Selecione como você vai compartilhar a música"))
                        }
                        1 -> {
                            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                            val permSheet = InfoSheet().build(requireContext()) {
                                // Estilo do sheet (AlertDialog)
                                style(SheetStyle.DIALOG)
                                // Título do AlertDialog
                                title("Deseja mesmo excluir a música?")
                                // Cor do título
                                titleColorRes(R.color.purple1)
                                // Mensagem do AlertDialog
                                content("Excluir a música \"${listaFavoritos[posicao].titulo}\" de ${listaFavoritos[posicao].artista}?\n\nAtenção: se a música que você estiver tentando excluir não for apagada, você precisará apaga-la manualmente no armazenamento do dispositivo.")
                                // Botão positivo que exclui a música em questão
                                positiveButtonColorRes(R.color.purple1)
                                onPositive("Sim, excluir") {
                                    // Criando o objeto "musica" com base nos dados da música que foi selecionada
                                    val musica = Musica(listaFavoritos[posicao].id, listaFavoritos[posicao].titulo, listaFavoritos[posicao].artista, listaFavoritos[posicao].album, listaFavoritos[posicao].duracao, listaFavoritos[posicao].imagemUri, listaFavoritos[posicao].caminho)
                                    // Criando o objeto "arquivo" que leva o objeto "musica" e o seu caminho (url do arquivo no armazenamento do dispositivo)
                                    val arquivo = File(listaFavoritos[posicao].caminho)
                                    // Exclui a música do armazenamento do dispositivo
                                    arquivo.delete()
                                    // Remove a música da lista de favoritos
                                    // (consequentemente será removida da lista principal por ter sido excluída do armazenamento)
                                    listaFavoritos.remove(musica)
                                    // E notifica que ela foi removida
                                    notifyItemRemoved(posicao)
                                }
                                // Botão negativo que apenas fecha o diálogo
                                negativeButtonColorRes(R.color.grey3)
                                onNegative {
                                    dismiss()
                                }
                            }
                            // Mostra o AlertDialog
                            permSheet.show()
                        }
                        // Detalhes da música
                        2 -> {
                            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                            val detalhesSheet = InfoSheet().build(requireContext()) {
                                // Estilo do sheet (AlertDialog)
                                style(SheetStyle.DIALOG)
                                // Mensagem do AlertDialog
                                content("Título: ${listaFavoritos[posicao].titulo}" +
                                        "\nArtista: ${listaFavoritos[posicao].artista}" +
                                        "\nAlbum: ${listaFavoritos[posicao].album}" +
                                        "\nDuração ${formatarDuracao(listaFavoritos[posicao].duracao)}" +
                                        "\n\nDiretório: ${listaFavoritos[posicao].caminho}")
                                // Esconde os ambos os botões
                                displayButtons(false)
                            }
                            // Mostra o AlertDialog
                            detalhesSheet.show()
                        }
                    }
                }
            }
        }
    }

    // Retorna a quantidade total das músicas na lista de músicas
    override fun getItemCount(): Int {
        return listaFavoritos.size
    }
}