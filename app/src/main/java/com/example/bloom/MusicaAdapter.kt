package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.MusicViewLayoutBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import java.io.File

// Classe do Adapter que liga a lista de músicas aos itens do RecyclerView
class MusicaAdapter(private val context: Context, private var listaMusicas: ArrayList<Musica>, private val activityPesquisar : Boolean = false, private val conteudoPlaylist : Boolean = false, private val activitySelecionar : Boolean = false, private val filaReproducao : Boolean = false) : RecyclerView.Adapter<MusicaAdapter.Holder>() {
    class Holder(binding: MusicViewLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        val titulo = binding.tituloMusicaView    // Título da música
        val artista = binding.artistaMusicaView  // Artista da música
        val imagem = binding.imgMusicaView       // Imagem da música
        val play = binding.playAnim              // Indicador da música atual reproduzindo
        val duracao = binding.tempoMusicaView    // Duração da música
        val botao = binding.btnExtraView         // Botão de opções extras

        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a MusicViewLayoutBinding (music_view_layout)
        val root = binding.viewMusica
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
            .apply(RequestOptions().placeholder(R.drawable.placeholder_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(holder.imagem)

        // Ajuste de cores para o modo escuro do Android
        if (MainActivity.escuro){
            // Muda a cor do ícone para branco
            holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            // Muda a cor do título para uma cor mais clara
            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
            // Cor do background
            holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black1))
        }else{
            // Muda a cor do ícone preto
            holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            // Muda a cor do título para uma cor mais escura
            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
            // Cor do background
            holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
        }

        // Quando o usuário clicar no botão de opções extras
        holder.botao.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            holder.botao.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            holder.botao.isEnabled = false
            checarFavoritos(listaMusicas[posicao].id)
            OptionsSheet().show(context) {
                title("${listaMusicas[posicao].titulo} | ${listaMusicas[posicao].artista}")
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
                        // Favoritar/Desfavoritar música
                        0 -> {
                            PlayerActivity.favIndex = checarFavoritos(listaMusicas[posicao].id)
                            // Se a música já estiver favoritada
                            if (PlayerActivity.favoritado){
                                // Então defina a variável favoritado para false
                                PlayerActivity.favoritado = false
                                // Mude o ícone para o coração vazio de desfavoritado das views do miniplayer e da notificação
                                // apenas se elas estiverem sendo visíveis e a música selecionada for a mesma tocando
                                if (PlayerActivity.musicaService != null && listaMusicas[posicao].id == PlayerActivity.musicaAtual){
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
                                // apenas se elas estiverem sendo visíveis e a música selecionada for a mesma tocando
                                if (PlayerActivity.musicaService != null && listaMusicas[posicao].id == PlayerActivity.musicaAtual){
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
                        // Excluir música
                        2 -> {
                            // Se a música que o usuário está tentando excluir for a mesma que estiver reproduzindo
                            // Mostra um toast que não é possível fazer a exclusão
                            if(listaMusicas[posicao].id == PlayerActivity.musicaAtual) {
                                Toast.makeText(context, "Não é possível excluir a música reproduzindo", Toast.LENGTH_SHORT).show()
                            }else {
                                // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                                val permSheet = InfoSheet().build(requireContext()) {
                                    // Estilo do sheet (AlertDialog)
                                    style(SheetStyle.DIALOG)
                                    // Título do AlertDialog
                                    title("Deseja mesmo excluir a música?")
                                    // Mensagem do AlertDialog
                                    content("Excluir a música \"${listaMusicas[posicao].titulo}\" de ${listaMusicas[posicao].artista}?\n\nAtenção: se a música que você deseja excluir estiver localizada no armazenamento externo, você deverá excluí-la manualmente.")
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
                                        MainActivity.listaMusicaMain.remove(musica)
                                        // Notifica que ela foi removida
                                        notifyItemRemoved(posicao)
                                        // E atualiza a lista principal
                                        atualizarLista(MainActivity.listaMusicaMain)
                                    }
                                    // Cor do botão negativo
                                    negativeButtonColorRes(R.color.grey3)
                                }
                                // Mostra o AlertDialog
                                permSheet.show()
                            }
                        }
                        // Mostrar detalhes da música
                        3 -> {
                            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                            val detalhesSheet = InfoSheet().build(requireContext()) {
                                // Estilo do sheet (AlertDialog)
                                style(SheetStyle.DIALOG)
                                // Mensagem do AlertDialog
                                content("Título: ${listaMusicas[posicao].titulo}" +
                                        "\nArtista(s): ${listaMusicas[posicao].artista}" +
                                        "\nÁlbum: ${listaMusicas[posicao].album}" +
                                        "\nDuração: ${formatarDuracao(listaMusicas[posicao].duracao)}" +
                                        "\n\nDiretório: ${listaMusicas[posicao].caminho}")
                                // Esconde os ambos os botões
                                displayButtons(false)
                            }
                            // Mostra o AlertDialog
                            detalhesSheet.show()
                        }
                    }
                }
                // Cor do botão negativo
                negativeButtonColorRes(R.color.grey3)
            }
        }

        // Método para adição de músicas na playlist
        fun adicionarMusica(musica : Musica) : Boolean{
            // Pra cada música clicada
            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.forEachIndexed { index, musicas ->
                // Verifica se ela já foi adicionada
                if (musica.id == musicas.id){
                    // Se já tiver sido adicionada, remova da lista
                    // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                    val falseSheet = InfoSheet().build(context) {
                        // Estilo do sheet (AlertDialog)
                        style(SheetStyle.DIALOG)
                        // Título do AlertDialog
                        title("Essa música já foi adicionada")
                        // Mensagem do AlertDialog
                        content("Deseja remove-lá?")
                        // Torna o objeto clicável novamente quando o diálogo for fechado
                        onClose { holder.root.isEnabled = true }
                        // Botão positivo que remove a música selecionada da playlist
                        positiveButtonColorRes(R.color.purple1)
                        onPositive("Sim, remover") {
                            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.removeAt(index)
                            holder.botao.visibility = View.INVISIBLE
                            // Torna o objeto clicável novamente
                            holder.root.isEnabled = true
                        }
                        // Cor do botão negativo
                        negativeButtonColorRes(R.color.grey3)
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
                // Mensagem do AlertDialog
                content("Deseja adicionar a música selecionada?")
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { holder.root.isEnabled = true }
                // Botão positivo que remove a música selecionada da playlist
                positiveButtonColorRes(R.color.purple1)
                onPositive("Sim, adicionar") {
                    PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.add(musica)
                    holder.botao.visibility = View.VISIBLE
                    // Torna o objeto clicável novamente
                    holder.root.isEnabled = true
                }
                // Cor do botão negativo
                negativeButtonColorRes(R.color.grey3)
            }
            // Mostra o AlertDialog
            addSheet.show()
            return true
        }

        // Em algumas telas, as views das músicas terão comportamentos específicos
        when{
            // Na tela do conteudoPlaylist
            conteudoPlaylist -> {
                if (ConteudoPlaylistActivity.escuroContPl){
                    // Muda a cor do ícone branco
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais clara
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black1))
                }else{
                    // Muda a cor do ícone para branco
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais clara
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                // Se estiver reproduzindo músicas
                if (PlayerActivity.musicaService != null) {
                    // Verifica qual música está tocando e procura o id igual a ela na lista
                    if (PlayerActivity.musicaAtual == listaMusicas[posicao].id) {
                        // Muda a cor do título
                        holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.purple1))
                        // E torna visível a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.VISIBLE
                        // Aplica uma animação pra troca de música
                        holder.titulo.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
                    }else{
                        // Muda a cor do título com base no modo escuro
                        if (ConteudoPlaylistActivity.escuroContPl){
                            // Cor clara do texto
                            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
                        }else{
                            // Cor escura do texto
                            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                        }
                        // E remove a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.GONE
                    }
                }

                holder.root.setOnClickListener {
                    irParaMusica("ConteudoPlaylist", posicao)
                }

                holder.botao.setOnClickListener {
                    // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                    holder.botao.isEnabled = false
                    // Muda a animação do botão ao ser clicado
                    holder.botao.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
                    OptionsSheet().show(context) {
                        // Estilo da sheet (BottomSheetDialog)
                        style(SheetStyle.BOTTOM_SHEET)
                        // Título da sheet
                        title("${listaMusicas[posicao].titulo} | ${listaMusicas[posicao].artista}")
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
                                        // apenas se elas estiverem sendo visíveis e a música selecionada for a mesma tocando
                                        if (PlayerActivity.musicaService != null && listaMusicas[posicao].id == PlayerActivity.musicaAtual){
                                            setBtnsNotify()
                                        }
                                        // E remova a música da lista de favoritos utilizando o indicador favIndex
                                        FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
                                        // Caso contrário (música desfavoritada)
                                    }else{
                                        // Então defina a variável favoritado para true
                                        PlayerActivity.favoritado = true
                                        // Mude o ícone para o coração cheio de favoritado das views do miniplayer e da notificação
                                        // apenas se elas estiverem sendo visíveis e a música selecionada for a mesma tocando
                                        if (PlayerActivity.musicaService != null && listaMusicas[posicao].id == PlayerActivity.musicaAtual){
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
                                        // Mensagem do AlertDialog
                                        content("Remover a música \"${listaMusicas[posicao].titulo}\" da playlist \"${PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].nome}\"?")
                                        // Botão positivo que remove a música em questão
                                        positiveButtonColorRes(R.color.purple1)
                                        onPositive("Sim, remover") {
                                            PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.removeAt(posicao)
                                            notifyItemRemoved(posicao)
                                            atualizarPlaylists()
                                            // Se a playlist ficar com menos de uma música, esconde e mostra as views abaixo
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
                                    }
                                    // Mostra o AlertDialog
                                    permSheet.show()
                                }
                                // Excluir música
                                3 -> {
                                    // Se a música que o usuário está tentando excluir for a mesma que estiver reproduzindo
                                    // Mostra um toast que não é possível fazer a exclusão
                                    if(listaMusicas[posicao].id == PlayerActivity.musicaAtual) {
                                        Toast.makeText(context, "Não é possível excluir a música reproduzindo", Toast.LENGTH_SHORT).show()
                                    }else {
                                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                                        val permSheet = InfoSheet().build(requireContext()) {
                                            // Estilo do sheet (AlertDialog)
                                            style(SheetStyle.DIALOG)
                                            // Título do AlertDialog
                                            title("Deseja mesmo excluir a música?")
                                            // Mensagem do AlertDialog
                                            content("Excluir a música \"${listaMusicas[posicao].titulo}\" de ${listaMusicas[posicao].artista}?\n\nAtenção: se a música que você deseja excluir estiver localizada no armazenamento externo, você deverá excluí-la manualmente.")
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
                                                // E atualiza a lista da playlist e a principal
                                                atualizarPlaylists()
                                                atualizarLista(MainActivity.listaMusicaMain)

                                                // Se a playlist ficar com menos de uma música, esconde e mostra as views abaixo
                                                if (PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist.size < 1){
                                                    ConteudoPlaylistActivity.binding.playlistImgCpl.setImageResource(R.drawable.bloom_lotus_icon_grey)
                                                    ConteudoPlaylistActivity.binding.fabRandomCpl.visibility = View.INVISIBLE
                                                    ConteudoPlaylistActivity.binding.musicasPlaylistRv.visibility = View.INVISIBLE
                                                    ConteudoPlaylistActivity.binding.avisoCpl.visibility = View.VISIBLE
                                                    ConteudoPlaylistActivity.binding.btnAddMusicas.visibility = View.VISIBLE
                                                }
                                            }
                                            // Cor do botão negativo
                                            negativeButtonColorRes(R.color.grey3)
                                        }
                                        // Mostra o AlertDialog
                                        permSheet.show()
                                    }
                                }
                            }
                        }
                        // Cor do botão negativo
                        negativeButtonColorRes(R.color.grey3)
                    }
                }
            }
            // Na tela de selecionar músicas
            activitySelecionar -> {
                holder.botao.setImageResource(R.drawable.ic_round_check_circle_24)
                if (SelecionarMusicasActivity.escuroSelect){
                    // Muda a cor do ícone para branco
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais clara
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black1))
                }else{
                    // Muda a cor do ícone para preto
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais escura
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }

                // Esconde a duração da música que não tem utilidade nessa tela
                holder.duracao.visibility = View.INVISIBLE
                // Esconde o ícone de selecionado inicialmente
                holder.botao.visibility = View.INVISIBLE
                // Define o botão como não clicável
                holder.botao.isClickable = false
                // Quando a música for clicada
                holder.root.setOnClickListener{
                    // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                    holder.root.isEnabled = false
                    // E chama o método para adicionar a música selecionada
                    adicionarMusica(listaMusicas[posicao])
                }
            }
            // No card da fila de reprodução
            filaReproducao -> {
                // Impede que o usuário clique em uma das músicas
                holder.root.isEnabled = false
                // Impede que o usuário clique em um dos botões das músicas
                holder.botao.isEnabled = false
                holder.botao.setImageResource(R.drawable.ic_round_queue_music_24)

                // Cores padrão para esta tela
                holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))

                // Se estiver reproduzindo músicas
                if (PlayerActivity.musicaService != null) {
                    // Verifica qual música está tocando e procura o id igual a ela na lista
                    if (PlayerActivity.musicaAtual == listaMusicas[posicao].id) {
                        // Muda a cor do título
                        holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.purple1))
                        // E torna visível a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.VISIBLE
                        // Aplica uma animação pra troca de música
                        holder.titulo.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
                    }else{
                        // A cor do título permanece a padrão para a tela
                        // E remove a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.GONE
                    }
                }
            }
            // Na tela de pesquisar música
            activityPesquisar -> {
                if (PesquisarMusicasActivity.escuroPesquisar){
                    // Muda a cor do ícone para branco
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais clara
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black1))
                }else{
                    // Muda a cor do ícone para preto
                    holder.botao.setColorFilter(ContextCompat.getColor(context, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
                    // Muda a cor do título para uma cor mais escura
                    holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                    // Cor do background
                    holder.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                }
                // A operação padrão a ser executada quando a música for clicada
                holder.root.setOnClickListener { irParaMusica("Pesquisa", posicao) }
            }
            // Em qualquer outra situação
            else -> {
                // Se estiver reproduzindo músicas
                if (PlayerActivity.musicaService != null) {
                    // Verifica qual música está tocando e procura o id igual a ela na lista
                    if (PlayerActivity.musicaAtual == listaMusicas[posicao].id) {
                        // Muda a cor do título
                        holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.purple1))
                        // E torna visível a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.VISIBLE
                        // Aplica uma animação pra troca de música
                        holder.titulo.startAnimation(AnimationUtils.loadAnimation(context, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
                    }else{
                        // Muda a cor do título com base no modo escuro
                        if (MainActivity.escuro){
                            // Cor clara do texto
                            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.grey2))
                        }else{
                            // Cor escura do texto
                            holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.black2))
                        }
                        // E remove a imagem de sinalização que a música está tocando
                        holder.play.visibility = View.GONE
                    }
                }
                // Quando clicado na view da música no RecyclerView, o usuário é levado para o player
                holder.root.setOnClickListener {
                    if (listaMusicas[posicao].id == PlayerActivity.musicaAtual) {
                        // Quando a música que for selecionada já estiver tocando, chame o método irParaMusica e passe a referência: "MiniPlayer", e a posição da música no Player
                        irParaMusica("MiniPlayer", PlayerActivity.posMusica)
                        // Em qualquer outro caso, chame o método irParaMusica e passe a referência: "Adapter" e a posição da música
                    }else { irParaMusica("Adapter", posicao) }
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