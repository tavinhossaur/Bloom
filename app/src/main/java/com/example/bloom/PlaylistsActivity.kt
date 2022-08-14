package com.example.bloom

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom.databinding.ActivityPlaylistsBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var playlistsAdapter: PlaylistsAdapter // variável que leva a classe PlaylistsAdapter

    companion object{
        var playlists : ModeloPlaylist = ModeloPlaylist()
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPlaylistsBinding // binding é a variável do ViewBinding para ligar as views ao código
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityPlaylistsBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlaylistsBinding (activity_playlists.xml)
        setContentView(binding.root)

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.playlistsRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.playlistsRv.setItemViewCacheSize(13)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.playlistsRv.layoutManager = GridLayoutManager(this@PlaylistsActivity, 2)

        // Criando uma variável do Adapter com o contexto (tela) e a lista de playlists que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        playlistsAdapter = PlaylistsAdapter(this, playlists.modelo)
        // Setando o Adapter para este RecyclerView
        binding.playlistsRv.adapter = playlistsAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.playlistsRv.isMotionEventSplittingEnabled = false

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarPl.setOnClickListener {finish()}

        binding.fabCriarPl.setOnClickListener {criarPlaylist()}

        // Caso não tenha nenhuma música favoritada ainda
        if (playlists.modelo.isNotEmpty()){
            binding.avisoPlaylists.visibility = View.GONE
            binding.playlistsRv.visibility = View.VISIBLE
        }else{
            binding.avisoPlaylists.visibility = View.VISIBLE
            binding.playlistsRv.visibility = View.GONE
        }
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_BloomNoActionBar)
        setTheme(R.style.Theme_BloomNoActionBar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }

    // Método para poder chamar o BottomSheetDialog ao botão do timer ser clicado
    private fun criarPlaylist(){
        // Cria e mostra a InputSheet
        // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
        binding.fabCriarPl.isEnabled = false
        InputSheet().show(this@PlaylistsActivity) {
            // Estilo do sheet (BottomSheet)
            style(SheetStyle.BOTTOM_SHEET)
            // Altera o botão de fechar o dialogo
            closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
            // Título do BottomSheetDialog
            title("Criar uma playlist")
            // Cor do título
            titleColorRes(R.color.purple1)
            // Conteúdo da sheet (Edit Texts)
            with(InputEditText("nome_playlist") {
                required(true)
                drawable(R.drawable.ic_round_folder_24)
                label("Insira o nome da playlist")
                hint("Músicas para viagem...")
            })
            with(InputEditText("nome_criador") {
                required(false)
                drawable(R.drawable.ic_round_person_24)
                hint("Nome do criador (Opcional)")
            })
            // Torna o objeto clicável novamente quando o diálogo for fechado
            onClose { binding.fabCriarPl.isEnabled = true }
            // Cor do botão "confirmar"
            positiveButtonColorRes(R.color.purple1)
            // Botão confirmar do BottomSheet
            onPositive("Criar") { result ->
                val nomePlaylist = result.getString("nome_playlist").toString()
                val nomeCriador = result.getString("nome_criador").toString()
                adicionarPlaylist(nomePlaylist, nomeCriador)
            }

            // Cor do botão "cancelar"
            negativeButtonColorRes(R.color.grey3)
            // Botão cancelar do BottomSheet
            onNegative { dismiss() }
        }
    }

    private fun adicionarPlaylist(nome : String, criador : String){
        var playlistExiste = false
        for (i in playlists.modelo){
            if (nome == i.nome){
                playlistExiste = true
                break
            }
        }
        if (playlistExiste) {
            Toast.makeText(this, "Já existe uma playlist com este nome", Toast.LENGTH_SHORT).show()
        }else{
            val tempPlaylist = Playlist()
            tempPlaylist.nome = nome
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.criador = criador
            playlists.modelo.add(tempPlaylist)
            playlistsAdapter.atualizarLista()

            binding.playlistsRv.visibility = View.VISIBLE
            binding.avisoPlaylists.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playlistsAdapter.notifyDataSetChanged()
    }
}