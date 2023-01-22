package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.bloom.databinding.ActivityPlaylistsBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputCheckBox
import com.maxkeppeler.sheets.input.type.InputEditText

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var playlistsAdapter: PlaylistsAdapter // variável que leva a classe PlaylistsAdapter

    companion object{
        var playlists : ModeloPlaylist = ModeloPlaylist() // Lista de playlists
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPlaylistsBinding // binding é a variável do ViewBinding para ligar as views ao código
        var escuroPl : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
        lateinit var nomeCriador : String
        lateinit var nomePlaylist : String
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
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
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.playlistsRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.playlistsRv.layoutManager = GridLayoutManager(this@PlaylistsActivity, 2)

        // Passando ao adapter o contexto (tela) e a lista de playlists que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        playlistsAdapter = PlaylistsAdapter(this, playlists.modelo)
        // Setando o Adapter para este RecyclerView
        binding.playlistsRv.adapter = playlistsAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.playlistsRv.isMotionEventSplittingEnabled = false

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarPl.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.btnVoltarPl.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            finish()
        }

        // Ao clicar no botão de criar playlists, chama o método para criar uma playlist
        binding.fabCriarPl.setOnClickListener {criarPlaylist()}

        // Caso tenha pelo menos uma playlist criada
        if (playlists.modelo.isNotEmpty()){
            // Esconde o aviso da playlist
            binding.avisoPlaylists.visibility = View.GONE
            // E mostra a lista de playlists
            binding.playlistsRv.visibility = View.VISIBLE
        }else{
            binding.avisoPlaylists.visibility = View.VISIBLE
            binding.playlistsRv.visibility = View.GONE
        }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            escuroPl = true
            binding.btnVoltarPl.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.tituloActivityPlaylists.setTextColor(ContextCompat.getColor(this, R.color.grey2))
            binding.fabCriarPl.setBackgroundColor(ContextCompat.getColor(this, R.color.black3))
        }else { escuroPl = false}
    }

    // Método para poder chamar o BottomSheetDialog ao botão do timer ser clicado
    private fun criarPlaylist(){
        // Cria e mostra a InputSheet
        // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
        binding.fabCriarPl.isEnabled = false

        // Lista de nomes de criador para hint da input no BottomSheetDialog
        val nomescr = arrayOf("David Bowie...", "Freddie Mercury...", "Michael Jackson...", "Elton John...", "Pat Benatar...", "Whitney Houston...", "Bonnie Tyler...", "Cindy Lauper...")
        // Lista de nomes de playlists para hint da input no BottomSheetDialog
        val nomespl = arrayOf("Músicas para viagem...", "Músicas para banho...", "Músicas para relaxar...", "Músicas para estudar...", "Músicas para liberar a raiva...")

        InputSheet().show(this@PlaylistsActivity) {
            // Estilo do sheet (BottomSheet)
            style(SheetStyle.BOTTOM_SHEET)
            // Título do BottomSheetDialog
            title("Criar uma playlist")
            // Conteúdo da sheet (Edit Texts)
            with(InputEditText("nome_playlist") {
                required(true)
                drawable(R.drawable.ic_round_folder_24)
                label("Insira o nome da playlist")
                hint(nomespl.random())
            })
            with(InputEditText("nome_criador") {
                label("Insira seu nome")
                required(true)
                drawable(R.drawable.ic_round_person_24)
                hint(nomescr.random())
            })
            with(InputCheckBox("selecionar_imagem") { // Read value later by index or custom key from bundle
                text("Deseja selecionar uma imagem para playlist?")
                // ... more options
            })
            // Torna o objeto clicável novamente quando o diálogo for fechado
            onClose { binding.fabCriarPl.isEnabled = true }
            // Cor do botão "confirmar"
            positiveButtonColorRes(R.color.purple1)
            // Botão confirmar do BottomSheet
            onPositive("Criar") { result ->
                // Retorna o valor string da input "nome_playlist"
                nomePlaylist = result.getString("nome_playlist").toString()
                // Retorna o valor string da input "nome_criador"
                nomeCriador = result.getString("nome_criador").toString()

                if (result.getBoolean("selecionar_imagem")){
                    val i = Intent()
                    i.type = "image/*"
                    i.action = Intent.ACTION_GET_CONTENT
                    launchSomeActivity.launch(i)
                }else{
                    // Passa todas para o método adicionar playlist
                    adicionarPlaylist(nomePlaylist, nomeCriador, "")
                }
            }
            // Cor do botão negativo
            negativeButtonColorRes(R.color.grey3)
        }
    }

    private val launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            // do your operation from here....
            if (data != null && data.data != null) {
                val selectedImageUri: Uri? = data.data

                // Passa todas para o método adicionar playlist
                adicionarPlaylist(nomePlaylist, nomeCriador, selectedImageUri.toString())

                // SharedPreferences, para salvar o caminho da imagem escolhida
                val editor = getSharedPreferences("Imagem", MODE_PRIVATE).edit()
                editor.putString("imagepath", selectedImageUri.toString())
                editor.apply()
            }
        }
    }

    // Método para adicionar playlists, que recebe o nome da mesma e do criador
    private fun adicionarPlaylist(nome: String, criador: String, imagem: String){
        // Variável para identificar se a playlist já existe, por padrão tem valor false
        var playlistExiste = false
        // Loop para verificar se já há alguma de mesmo nome na lista de playlists
        for (i in playlists.modelo){
            // Se o nome inserido for igual ao nome de alguma playlist
            if (nome == i.nome){
                // O valor da variável de indentificação passa a ser true
                playlistExiste = true
                // E o loop encerra
                break
            }
        }
        // Portanto, se a playlist existir
        if (playlistExiste) {
            // Mostra um toast dizendo que a playlist já existe
            Toast.makeText(this, "Já existe uma playlist com este nome", Toast.LENGTH_SHORT).show()
        // Caso contrário (playlist não existe)
        }else{
            // Então cria um objeto da classe Playlist
            val novaPlaylist = Playlist()
            // Define o nome da playlist
            novaPlaylist.nome = nome
            // Define o nome do criador da playlist
            novaPlaylist.criador = criador
            // Define a imagem da playlist se o usuário desejar
            novaPlaylist.imagemPlaylistUri = imagem
            // Define a lista de músicas dessa playlist como um ArrayList
            novaPlaylist.playlist = ArrayList()
            // Adiciona a nova playlist para lista de playlists
            playlists.modelo.add(novaPlaylist)
            // E atualiza a lista de playlists
            playlistsAdapter.atualizarLista()
            // Muda a visibilidade dos itens quando houver ao menos uma playlist na lista
            // (a verificação disso não fica aqui)
            binding.playlistsRv.visibility = View.VISIBLE
            binding.avisoPlaylists.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    // Quando retornar a esta activity, verifica se a lista de playlists foi alterada
    override fun onResume() {
        super.onResume()
        playlistsAdapter.notifyDataSetChanged()
        playlistsAdapter.atualizarLista()
    }
}