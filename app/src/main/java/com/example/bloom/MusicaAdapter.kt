package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.MusicViewLayoutBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import java.io.File

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class MusicaAdapter(private val context: Context, private var listaMusicas: ArrayList<Musica>, private val conteudoPlaylist : Boolean = false, private val activitySelecionar : Boolean = false, private val filaReproducao : Boolean = false) : RecyclerView.Adapter<MusicaAdapter.Holder>() {
    class Holder(binding: MusicViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicaView    // Título da música
        val artista = binding.artistaMusicaView  // Artista da música
        val imagem = binding.imgMusicaView       // Imagem da música
        val duracao = binding.tempoMusicaView    // Duração da música
        val botao = binding.btnExtraView         // Botão de opções extras

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

        // Quando o usuário clicar no botão de opções extras
        holder.botao.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            holder.botao.isEnabled = false
            checarFavoritos(listaMusicas[posicao].id)
            OptionsSheet().show(context) {
                title("${listaMusicas[posicao].titulo} | ${listaMusicas[posicao].artista}")
                titleColorRes(R.color.white)
                // Altera o botão de fechar o dialogo
                closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
                // Marca como falso as múltiplas opções
                multipleChoices(false)
                // Mantém as cores dos ícones
                preventIconTint(true)
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { holder.botao.isEnabled = true }
                // Se não estiver favoritado
                if (!PlayerActivity.favoritado) {
                    // Mostra as opções com a opção de favoritar
                    with(
                        Option(R.drawable.ic_round_favorite_border_menu_24, "Favoritar"),
                        Option(R.drawable.ic_round_share_24, "Compartilhar"),
                        Option(R.drawable.ic_round_delete_forever_24, "Excluir"),
                        Option(R.drawable.ic_round_info_24, "Detalhes")
                    )
                // Caso contrário (estiver favoritado)
                }else{
                    // Mostra as opções com a opção de desfavoritar
                    with(
                        Option(R.drawable.ic_round_favorite_menu_24, "Desfavoritar"),
                        Option(R.drawable.ic_round_share_24, "Compartilhar"),
                        Option(R.drawable.ic_round_delete_forever_24, "Excluir"),
                        Option(R.drawable.ic_round_info_24, "Detalhes")
                    )
                }
                // Quando clicado em uma opção
                onPositive { index: Int, _: Option ->
                    // Verifica qual foi clicada
                    when(index){
                        // Favoritar / Desfavoritar
                        0 -> {
                            PlayerActivity.favIndex = checarFavoritos(listaMusicas[posicao].id)
                            // Se a música já estiver favoritada
                            if (PlayerActivity.favoritado){
                                // Então defina a variável favoritado para false
                                PlayerActivity.favoritado = false
                                // Mude o ícone para o coração vazio de desfavoritado das views do miniplayer e da notificação
                                // apenas se elas estiverem sendo visíveis porque o serviço de música não está nulo
                                if (PlayerActivity.musicaService != null){
                                    MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
                                    setBtnsNotify()
                                }
                                // E remova a música da lista de favoritos utilizando o indicador favIndex
                                FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
                                // Caso contrário (música desfavoritada)
                            }else{
                                // Então defina a variável favoritado para true
                                PlayerActivity.favoritado = true
                                // Mude o ícone para o coração cheio de favoritado das views do miniplayer e da notificação
                                // apenas se elas estiverem sendo visíveis porque o serviço de música não está nulo
                                if (PlayerActivity.musicaService != null){
                                    MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_miniplayer_24)
                                    setBtnsNotify()
                                }
                                // E adicione a música atual a lista de favoritos
                                FavoritosActivity.listaFavoritos.add(listaMusicas[posicao])
                            }
                        }
                        // Compartilhar
                        1 -> {
                            // Cria a intent para o compartilhamento
                            val compartIntent = Intent()
                            // Define a ação que será feita na intent (ACTION_SEND), ("enviar")
                            compartIntent.action = Intent.ACTION_SEND
                            // Define o tipo do conteúdo que será enviado, no caso, audio
                            compartIntent.type = "audio/*"
                            // Junto da intent, com putExtra, estão indo os dados da música com base no caminho da música selecionada
                            compartIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaMusicas[posicao].caminho))
                            // Inicia a intent chamando o método "createChooser" que mostra um BottomSheetDialog com opções de compartilhamento (Bluetooth, Whatsapp, Drive)
                            // E um título que aparece no BottomSheetDialog
                            startActivity(Intent.createChooser(compartIntent, "Selecione como você vai compartilhar a música"))
                        }
                        // Excluir
                        2 -> {
                            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                            val permSheet = InfoSheet().build(requireContext()) {
                                // Estilo do sheet (AlertDialog)
                                style(SheetStyle.DIALOG)
                                // Título do AlertDialog
                                title("Deseja mesmo excluir a música?")
                                // Cor do título
                                titleColorRes(R.color.purple1)
                                // Mensagem do AlertDialog
                                content("Excluir a música \"${listaMusicas[posicao].titulo}\" de ${listaMusicas[posicao].artista}?\n\nAtenção: se a música que você estiver tentando excluir não for apagada, você precisará apaga-la manualmente no armazenamento do dispositivo.")
                                // Botão positivo que exclui a música em questão
                                positiveButtonColorRes(R.color.purple1)
                                onPositive("Sim, excluir") {
                                    // Criando o objeto "musica" com base nos dados da música que foi selecionada
                                    val musica = Musica(listaMusicas[posicao].id, listaMusicas[posicao].titulo, listaMusicas[posicao].artista, listaMusicas[posicao].album, listaMusicas[posicao].duracao, listaMusicas[posicao].imagemUri, listaMusicas[posicao].caminho)
                                    // Criando o objeto "arquivo" que leva o objeto "musica" e o seu caminho (url do arquivo no armazenamento do dispositivo)
                                    val arquivo = File(listaMusicas[posicao].caminho)
                                    // Exclui a música do armazenamento do dispositivo
                                    arquivo.delete()
                                    // Remove a música da lista
                                    listaMusicas.remove(musica)
                                    // Notifica que ela foi removida
                                    notifyItemRemoved(posicao)
                                    // E atualiza a lista de músicas
                                    atualizarLista(MainActivity.listaMusicaMain)
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
                        // Mostrar detalhes
                        3 -> {
                            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                            val detalhesSheet = InfoSheet().build(requireContext()) {
                                // Estilo do sheet (AlertDialog)
                                style(SheetStyle.DIALOG)
                                // Mensagem do AlertDialog
                                content("Título: ${listaMusicas[posicao].titulo}" +
                                        "\nArtista: ${listaMusicas[posicao].artista}" +
                                        "\nAlbum: ${listaMusicas[posicao].album}" +
                                        "\nDuração ${formatarDuracao(listaMusicas[posicao].duracao)}" +
                                        "\n\nDiretório: ${listaMusicas[posicao].caminho}")
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

        // Método para adição de músicas na playlist
        fun adicionarMusica(musica : Musica) : Boolean{
            // Pra cada música clicada
            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.forEachIndexed { index, musicas ->
                // Verifica se ela já foi selecionada
                if (musica.id == musicas.id){
                    // Se já tiver sido selecionada, remova da lista
                    // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                    val falseSheet = InfoSheet().build(context) {
                        // Estilo do sheet (AlertDialog)
                        style(SheetStyle.DIALOG)
                        // Título do AlertDialog
                        title("Essa música já foi adicionada")
                        // Cor do título
                        titleColorRes(R.color.purple1)
                        // Mensagem do AlertDialog
                        content("Deseja remove-lá?")
                        // Impede que o usuário clique fora do diálogo para fechá-lo
                        cancelableOutside(false)
                        // Botão positivo que remove a música selecionada da playlist
                        positiveButtonColorRes(R.color.purple1)
                        onPositive("Sim, remover") {
                            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.removeAt(index)
                            holder.botao.visibility = View.INVISIBLE
                            // Torna o objeto clicável novamente
                            holder.root.isEnabled = true
                        }
                        // Botão negativo que apenas fecha o diálogo
                        negativeButtonColorRes(R.color.grey3)
                        onNegative {
                            dismiss()
                            // Torna o objeto clicável novamente
                            holder.root.isEnabled = true
                        }
                    }
                    // Mostra o AlertDialog
                    falseSheet.show()
                    return false
                }
            }
            // Se não foi selecionada, adicione ela a lista
            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
            val addSheet = InfoSheet().build(context) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Título do AlertDialog
                title("Adicionar música")
                // Cor do título
                titleColorRes(R.color.purple1)
                // Mensagem do AlertDialog
                content("Deseja adicionar a música selecionada?")
                // Impede que o usuário clique fora do diálogo para fechá-lo
                cancelableOutside(false)
                // Botão positivo que remove a música selecionada da playlist
                positiveButtonColorRes(R.color.purple1)
                onPositive("Sim, adicionar") {
                    PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.add(musica)
                    holder.botao.visibility = View.VISIBLE
                    // Torna o objeto clicável novamente
                    holder.root.isEnabled = true
                }
                // Botão negativo que apenas fecha o diálogo
                negativeButtonColorRes(R.color.grey3)
                onNegative {
                    dismiss()
                    // Torna o objeto clicável novamente
                    holder.root.isEnabled = true
                }
            }
            // Mostra o AlertDialog
            addSheet.show()
            return true
        }

        // Em algumas telas, as views das músicas terão comportamentos específicos
        when{
            // Na tela do conteudoPlaylist
            conteudoPlaylist -> {
                holder.root.setOnClickListener {
                    irParaMusica("ConteudoPlaylist", posicao)
                }
                holder.botao.setOnClickListener {
                    // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                    holder.botao.isEnabled = false
                    OptionsSheet().show(context) {
                        title("${listaMusicas[posicao].titulo} | ${listaMusicas[posicao].artista}")
                        titleColorRes(R.color.white)
                        // Altera o botão de fechar o dialogo
                        closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
                        // Marca como falso as múltiplas opções
                        multipleChoices(false)
                        // Mantém as cores dos ícones
                        preventIconTint(true)
                        // Torna o objeto clicável novamente
                        onClose { holder.botao.isEnabled = true }
                        // Se a música em questão não estiver favoritada
                        if (!PlayerActivity.favoritado) {
                            // Então mostre as opções com a opção de favoritar a música
                            with(
                                Option(R.drawable.ic_round_favorite_border_menu_24, "Favoritar"),
                                Option(R.drawable.ic_round_share_24, "Compartilhar"),
                                Option(R.drawable.ic_round_remove_24, "Remover"),
                                Option(R.drawable.ic_round_delete_forever_24, "Excluir")
                            )
                        // Caso contrário (não favoritada)
                        }else{
                            // Então mostre as opções com a opção de desfavoritar a música
                            with(
                                Option(R.drawable.ic_round_favorite_menu_24, "Desfavoritar"),
                                Option(R.drawable.ic_round_share_24, "Compartilhar"),
                                Option(R.drawable.ic_round_remove_24, "Remover"),
                                Option(R.drawable.ic_round_delete_forever_24, "Excluir")
                            )
                        }
                        onPositive { index: Int, _: Option ->
                            when(index){
                                // Favoritar/Desfavoritar música
                                0 -> {
                                    PlayerActivity.favIndex = checarFavoritos(listaMusicas[posicao].id)
                                    // Se a música já estiver favoritada
                                    if (PlayerActivity.favoritado){
                                        // Então defina a variável favoritado para false
                                        PlayerActivity.favoritado = false
                                        // Mude o ícone para o coração vazio de desfavoritado das views do miniplayer e da notificação
                                        // apenas se elas estiverem sendo visíveis porque o serviço de música não está nulo
                                        if (PlayerActivity.musicaService != null){
                                            MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
                                            setBtnsNotify()
                                        }
                                        // E remova a música da lista de favoritos utilizando o indicador favIndex
                                        FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
                                        // Caso contrário (música desfavoritada)
                                    }else{
                                        // Então defina a variável favoritado para true
                                        PlayerActivity.favoritado = true
                                        // Mude o ícone para o coração cheio de favoritado das views do miniplayer e da notificação
                                        // apenas se elas estiverem sendo visíveis porque o serviço de música não está nulo
                                        if (PlayerActivity.musicaService != null){
                                            MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_miniplayer_24)
                                            setBtnsNotify()
                                        }
                                        // E adicione a música atual a lista de favoritos
                                        FavoritosActivity.listaFavoritos.add(listaMusicas[posicao])
                                    }
                                }
                                // Compartilhar música
                                1 -> {
                                    // Cria a intent para o compartilhamento
                                    val compartIntent = Intent()
                                    // Define a ação que será feita na intent (ACTION_SEND), ("enviar")
                                    compartIntent.action = Intent.ACTION_SEND
                                    // Define o tipo do conteúdo que será enviado, no caso, audio
                                    compartIntent.type = "audio/*"
                                    // Junto da intent, com putExtra, estão indo os dados da música com base no caminho da música selecionada
                                    compartIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(listaMusicas[posicao].caminho))
                                    // Inicia a intent chamando o método "createChooser" que mostra um BottomSheetDialog com opções de compartilhamento (Bluetooth, Whatsapp, Drive)
                                    // E um título que aparece no BottomSheetDialog
                                    startActivity(Intent.createChooser(compartIntent, "Selecione como você vai compartilhar a música"))
                                }
                                // Remover música da playlist
                                2 -> {
                                    // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                                    val permSheet = InfoSheet().build(requireContext()) {
                                        // Estilo do sheet (AlertDialog)
                                        style(SheetStyle.DIALOG)
                                        // Título do AlertDialog
                                        title("Deseja mesmo remover a música?")
                                        // Cor do título
                                        titleColorRes(R.color.purple1)
                                        // Mensagem do AlertDialog
                                        content("Remover a música \"${listaMusicas[posicao].titulo}\" da playlist \"${PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].nome}\"?")
                                        // Botão positivo que remove a música em questão
                                        positiveButtonColorRes(R.color.purple1)
                                        onPositive("Sim, remover") {
                                            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.removeAt(posicao)
                                            notifyItemRemoved(posicao)
                                            atualizarPlaylists()
                                            if (PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.size < 1){
                                                ConteudoPlaylistActivity.binding.playlistImgCpl.setImageResource(R.drawable.bloom_lotus_icon_grey)
                                                ConteudoPlaylistActivity.binding.fabRandomCpl.visibility = View.INVISIBLE
                                                ConteudoPlaylistActivity.binding.musicasPlaylistRv.visibility = View.INVISIBLE
                                                ConteudoPlaylistActivity.binding.avisoCpl.visibility = View.VISIBLE
                                                ConteudoPlaylistActivity.binding.btnAddMusicas.visibility = View.VISIBLE
                                            }
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
                                // Excluir música
                                3 -> {
                                    // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                                    val permSheet = InfoSheet().build(requireContext()) {
                                        // Estilo do sheet (AlertDialog)
                                        style(SheetStyle.DIALOG)
                                        // Título do AlertDialog
                                        title("Deseja mesmo excluir a música?")
                                        // Cor do título
                                        titleColorRes(R.color.purple1)
                                        // Mensagem do AlertDialog
                                        content("Excluir a música \"${listaMusicas[posicao].titulo}\" de ${listaMusicas[posicao].artista}?\n\nAtenção: se a música que você estiver tentando excluir não for apagada, você precisará apaga-la manualmente no armazenamento do dispositivo.")
                                        // Botão positivo que exclui a música em questão
                                        positiveButtonColorRes(R.color.purple1)
                                        onPositive("Sim, excluir") {
                                            // Criando o objeto "musica" com base nos dados da música que foi selecionada
                                            val musica = Musica(listaMusicas[posicao].id, listaMusicas[posicao].titulo, listaMusicas[posicao].artista, listaMusicas[posicao].album, listaMusicas[posicao].duracao, listaMusicas[posicao].imagemUri, listaMusicas[posicao].caminho)
                                            // Criando o objeto "arquivo" que leva o objeto "musica" e o seu caminho (url do arquivo no armazenamento do dispositivo)
                                            val arquivo = File(listaMusicas[posicao].caminho)
                                            // Exclui a música do armazenamento do dispositivo
                                            arquivo.delete()
                                            // Remove a música da lista
                                            listaMusicas.remove(musica)
                                            // Notifica que ela foi removida
                                            notifyItemRemoved(posicao)
                                            // E atualiza a lista
                                            atualizarPlaylists()
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
                            }
                        }
                    }
                }
            }
            // Na tela de selecionar músicas
            activitySelecionar -> {
                // Esconde a duração da música que não tem utilidade nessa tela
                holder.duracao.visibility = View.INVISIBLE
                // Esconde o ícone de selecionado inicialmente
                holder.botao.visibility = View.INVISIBLE
                // Muda o ícone para um ícone de seleção
                holder.botao.setImageResource(R.drawable.ic_round_check_circle_24)
                // Define o botão como não clicável
                holder.botao.isClickable = false
                // Quando a música for clicada
                holder.root.setOnClickListener{
                    // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                    holder.root.isEnabled = false
                    adicionarMusica(listaMusicas[posicao])
                }
            }
            // No card da fila de reprodução
            filaReproducao -> {
                // Impede que o usuário clique em uma das músicas
                holder.root.isEnabled = false
                // Muda o ícone para um ícone de remoção
                holder.botao.setImageResource(R.drawable.ic_round_remove_24)
                // Quando for clicado no botão de opções extras
                holder.botao.setOnClickListener {
                    // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                    holder.botao.isEnabled = false
                    // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                    val permSheet = InfoSheet().build(context) {
                        // Estilo do sheet (AlertDialog)
                        style(SheetStyle.DIALOG)
                        // Título do AlertDialog
                        title("Deseja mesmo remover a música?")
                        // Cor do título
                        titleColorRes(R.color.purple1)
                        // Mensagem do AlertDialog
                        content("Remover a música \"${listaMusicas[posicao].titulo}\" da fila de reprodução atual?")
                        // Torna o objeto clicável novamente
                        onClose { holder.botao.isEnabled = true }
                        // Botão positivo que remove a música em questão
                        positiveButtonColorRes(R.color.purple1)
                        onPositive("Sim, remover") {
                            // Remove a música da lista
                            PlayerActivity.filaMusica.removeAt(posicao)
                            // Notifica que ela foi removida
                            notifyItemRemoved(posicao)
                            // E atualiza a lista de reprodução
                            atualizarLista(PlayerActivity.filaMusica)
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
            }
            // Em qualquer outra situação
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
    fun atualizarLista(lista : ArrayList<Musica>){
        listaMusicas = ArrayList()
        listaMusicas.addAll(lista)
        notifyDataSetChanged()
    }

    // Método para atualizar a lista de playlists
    @SuppressLint("NotifyDataSetChanged")
    fun atualizarPlaylists(){
        listaMusicas = ArrayList()
        listaMusicas = PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist
        notifyDataSetChanged()
    }
}