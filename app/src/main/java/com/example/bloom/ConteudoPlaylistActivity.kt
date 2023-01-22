package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.ActivityConteudoPlaylistBinding
import com.google.gson.GsonBuilder
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText
import java.io.IOException


class ConteudoPlaylistActivity : AppCompatActivity() {

    companion object {
        var posPlaylistAtual : Int = -1 // Posição da playlist selecionada
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityConteudoPlaylistBinding // binding é a variável do ViewBinding para ligar as views ao código
        @SuppressLint("StaticFieldLeak")
        lateinit var musicaAdapter : MusicaAdapter // Variável que leva a classe MusicAdapter
        var escuroContPl : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
        var init = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
        super.onCreate(savedInstanceState)
        init = true
        // Inicialização do binding
        binding = ActivityConteudoPlaylistBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityConteudoPlaylistBinding (activity_conteudo_playlist.xml)
        setContentView(binding.root)

        // Recebe a intent feita ao clicar no card de uma playlist
        // para indentificar qual playlist essa activity deve mostrar o conteúdo
        posPlaylistAtual = intent.extras?.get("Playlist") as Int

        // Chama o método para checar se alguma música apagada pelo usuário estava presente na playlist
        try {
            PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist = checarMusicasApagadas(PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist)
        }catch (E: Exception){return}

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.musicasPlaylistRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.musicasPlaylistRv.setItemViewCacheSize(13)
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.musicasPlaylistRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.musicasPlaylistRv.layoutManager = LinearLayoutManager(this@ConteudoPlaylistActivity)
        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this, PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist, conteudoPlaylist = true)
        // Setando o Adapter para este RecyclerView
        binding.musicasPlaylistRv.adapter = musicaAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.musicasPlaylistRv.isMotionEventSplittingEnabled = false

        // Ao clicar no botão voltar, encerra a activity
        binding.btnVoltarCpl.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.btnVoltarCpl.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            finish()
        }

        // Ao clicar no botão de imagem na imagem da playlist, chama a função para fazer a alteração da mesma
        binding.cardImgCpl.setOnLongClickListener {
            // Cria o popup menu
            val contexto: Context = ContextThemeWrapper(this, R.style.PopupMenuStyle)
            val popup = PopupMenu(contexto, binding.playlistImgCpl, Gravity.CENTER)
            popup.setForceShowIcon(true)
            // Infla o menu do card
            popup.inflate(R.menu.img_menu)
            // Adicionando o listener das opções do menu
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Editar playlist
                    R.id.alterar_img -> {
                        selecionarImagem()
                        true
                    }
                    // Remover imagem
                    R.id.remover_img -> {
                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                        InfoSheet().show(this) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Título do AlertDialog
                            title("Restaurar padrão")
                            // Mensagem do AlertDialog
                            content("Deseja restaurar para a imagem padrão?")
                            // Botão positivo que exclui a playlist em questão
                            positiveButtonColorRes(R.color.purple1)
                            // onClose { binding.btnImagemPlaylist.isEnabled = true }
                            onPositive("Sim, restaurar") {
                                // Se houver uma imagem selecionada pelo usuário na playlist
                                if (PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri != ""){
                                    // Passa um valor em branco para a imagem da playlist atual
                                    PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri = ""
                                }
                                Toast.makeText(this@ConteudoPlaylistActivity, "Padrão restaurado", Toast.LENGTH_SHORT).show()

                                // Caso haja músicas na playlist, muda a imagem para a capa da primeira música do álbum, caso contrário volta para a imagem placeholder
                                Glide.with(this)
                                    // Carrega a posição da música com base no index e a uri da sua imagem
                                    // "getOrNull" é utilizado para caso o index não exista.
                                    .load(PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist.getOrNull(0)?.imagemUri)
                                    // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                                    // junto do método centerCrop() para ajustar a imagem dentro da view
                                    .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
                                    // Alvo da aplicação da imagem
                                    .into(binding.playlistImgCpl)
                            }
                            // Cor do botão negativo
                            negativeButtonColorRes(R.color.grey3)
                        }
                        true
                    }
                    else -> false
                }
            }
            // Mostra o menu popup
            popup.show()
            true
        }

        // Quando clicado no botão para adicionar músicas, leva o usuário para tela de seleção de músicas
        binding.btnAddMusicas.setOnClickListener{
            startActivity(Intent(this, SelecionarMusicasActivity::class.java))
        }

        // Quando for clicado no FAB para randomizar as músicas da playlist
        binding.fabRandomCpl.setOnClickListener {
            val favIntent = Intent(this@ConteudoPlaylistActivity, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            favIntent.putExtra("indicador", 0)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe da MainActivity (String)
            favIntent.putExtra("classe", "ConteudoPlaylistRandom")
            startActivity(favIntent)
        }

        // Quando clicado no botão de opções extra mostra um popup menu
        binding.btnExtraCpl.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.btnExtraCpl.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_grow_fade_in_from_bottom))
            // Previne que o usuário crie dois menus ao dar dois cliques rápidos
            binding.btnExtraCpl.isEnabled = false
            // Cria o popup menu
            val contexto: Context = ContextThemeWrapper(this, R.style.PopupMenuStyle)
            val popup = PopupMenu(contexto, binding.btnExtraCpl, Gravity.CENTER)
            popup.setForceShowIcon(true)
            // Infla o menu do card
            popup.inflate(R.menu.playlist_menu)
            // Torna o objeto clicável novamente quando o diálogo for fechado
            popup.setOnDismissListener { binding.btnExtraCpl.isEnabled = true }
            // Adicionando o listener das opções do menu
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Adicionar músicas
                    R.id.add_musicas -> {
                        // Leva o usuário para tela de seleção de músicas
                        startActivity(Intent(this, SelecionarMusicasActivity::class.java))
                        true
                    }
                    // Editar playlist
                    R.id.edit_playlist -> {
                        // Cria e mostra a InputSheet
                        InputSheet().show(this) {
                            // Estilo do sheet (BottomSheet)
                            style(SheetStyle.BOTTOM_SHEET)
                            // Título do BottomSheetDialog
                            title("Editar a playlist")
                            // Mostra o botão de fechar o BottomSheetDialog
                            displayCloseButton(true)
                            // Conteúdo da sheet (Edit Texts)
                            with(InputEditText("nome_playlist") {
                                required(true)
                                drawable(R.drawable.ic_round_folder_24)
                                label("Insira o novo nome da playlist")
                                hint("Músicas para tomar banho...")
                            })
                            // Cor do botão "confirmar"
                            positiveButtonColorRes(R.color.purple1)
                            // Botão confirmar do BottomSheet
                            onPositive("Editar") { result ->
                                val nomePlaylist = result.getString("nome_playlist").toString()
                                // Muda o nome da playlist
                                PlaylistsActivity.playlists.modelo[posPlaylistAtual].nome = nomePlaylist
                                binding.nomePlaylistCpl.text = nomePlaylist
                            }
                            // Cor do botão negativo
                            negativeButtonColorRes(R.color.grey3)
                        }
                        true
                    }
                    // Limpar playlist
                    R.id.clear_playlist -> {
                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                        val permSheet = InfoSheet().build(this) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Título do AlertDialog
                            title("Deseja mesmo limpar a playlist?")
                            // Mensagem do AlertDialog
                            content("Remover todas as músicas da playlist ${PlaylistsActivity.playlists.modelo[posPlaylistAtual].nome}?")
                            // Botão positivo que exclui a playlist em questão
                            positiveButtonColorRes(R.color.purple1)
                            onPositive("Sim, limpar") {
                                // Se houver uma ou mais músicas na playlist
                                if (PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist.size >= 1){
                                    // Então limpe as músicas presentes nela
                                    PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist.clear()

                                    if (PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri.isEmpty()) {
                                        binding.playlistImgCpl.setImageResource(R.drawable.bloom_lotus_icon_grey)
                                    }

                                    // E mostre um toast confirmando para o usuário que a playlist foi limpa
                                    Toast.makeText(context, "Playlist limpa", Toast.LENGTH_SHORT).show()

                                    binding.fabRandomCpl.visibility = View.INVISIBLE
                                    binding.musicasPlaylistRv.visibility = View.INVISIBLE
                                    binding.avisoCpl.visibility = View.VISIBLE
                                    binding.btnAddMusicas.visibility = View.VISIBLE

                                // Caso contrário (não houver nenhuma música para apagar)
                                }else{
                                    // Apenas mostre um toast
                                    Toast.makeText(context, "A playlist já está limpa", Toast.LENGTH_SHORT).show()
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
        }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            escuroContPl = true
            binding.btnVoltarCpl.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.btnExtraCpl.setImageResource(R.drawable.ic_baseline_more_vert_white_24)
            binding.nomePlaylistCpl.setTextColor(ContextCompat.getColor(this, R.color.grey2))
            binding.cardImgCpl.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.btnAddMusicas.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.fabRandomCpl.setBackgroundColor(ContextCompat.getColor(this, R.color.black3))
        }else {escuroContPl = false }
    }

    // Método que cria uma intent para fazer a requisição de uma ação de selecionar uma imagem do armazenamento do dispositivo
    private fun selecionarImagem() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        imagensIntent.launch(i)
    }

    // Intent que será lançada no método
    private var imagensIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        // Se a intent funcionou
        if (result.resultCode == RESULT_OK) {
            // Retorna a imagem para o objeto data
            val data = result.data
            // Verifica se os dados não são nulos, ou seja, se a imagem foi mesmo selecionada
            if (data != null && data.data != null) {
                // Adiciona a uri da imagem aos dados
                val uriImagem: Uri? = data.data
                try {
                    // Passa a uri para a imagem da playlist atual
                    PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri = uriImagem.toString()
                    // E define a imagem com base na mesma uri
                    binding.playlistImgCpl.setImageURI(uriImagem)
                    Toast.makeText(this, "Imagem alterada com sucesso", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    // No caso de erro mostra a exception no toast abaixo
                    Toast.makeText(this, "Erro: $e", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        binding.nomePlaylistCpl.text = PlaylistsActivity.playlists.modelo[posPlaylistAtual].nome
        binding.nomeCriadorCpl.text = "por ${PlaylistsActivity.playlists.modelo[posPlaylistAtual].criador}"

        musicaAdapter = MusicaAdapter(this, PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist, conteudoPlaylist = true)
        binding.musicasPlaylistRv.adapter = musicaAdapter

        // Se o usuário já escolheu uma imagem
        if (PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri == ""){
            // Utilizando Glide, Procura na lista de músicas a posição da música em específico
            // e retorna sua imagem de álbum no lugar da ImageView da mesma
            Glide.with(this)
                // Carrega a posição da primeira música e a uri da sua imagem
                .load(PlaylistsActivity.playlists.modelo[posPlaylistAtual].playlist.getOrNull(0)?.imagemUri)
                // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
                // Alvo da aplicação da imagem
                .into(binding.playlistImgCpl)
        }else{
            Glide.with(this)
                // Carrega a uri da imagem selecionada pelo usuário
                .load(PlaylistsActivity.playlists.modelo[posPlaylistAtual].imagemPlaylistUri)
                // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
                .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
                // Alvo da aplicação da imagem
                .into(binding.playlistImgCpl)
        }

        if (musicaAdapter.itemCount > 0){
            binding.fabRandomCpl.visibility = View.VISIBLE
            binding.musicasPlaylistRv.visibility = View.VISIBLE
            binding.avisoCpl.visibility = View.INVISIBLE
            binding.btnAddMusicas.visibility = View.INVISIBLE
        }

        musicaAdapter.notifyDataSetChanged()

        // SharedPreferences, para salvar a lista de playlists do usuário
        val editor = getSharedPreferences("Favoritos", MODE_PRIVATE).edit()
        val jsonStringPlaylists = GsonBuilder().create().toJson(PlaylistsActivity.playlists)
        editor.putString("Lista de playlists", jsonStringPlaylists)
        editor.apply()
    }
}