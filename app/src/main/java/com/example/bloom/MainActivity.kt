package com.example.bloom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_mini_player.*
import java.io.File
import java.net.URI
import java.util.logging.Level.parse

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding // Variável usada para ligar os componentes da tela
    private lateinit var musicaAdapter : MusicaAdapter // Variável que leva o Adapter

    // Funciona como uma classe static do Java
    companion object{
        lateinit var ListaMusicaMain : ArrayList<Musica> // Lista de músicas da tela principal
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarLayout()

        // Abrir a tela de playlists
        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this, PlaylistsActivity::class.java))
            closeSearch()
        }

        // Abrir a tela de favoritos
        binding.favoritosBtn.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
            closeSearch()
        }

        // Randomizar as músicas
        binding.randomBtn.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java))
            Toast.makeText(this, "Músicas randomizadas!", Toast.LENGTH_SHORT).show()
            closeSearch()
        }

        // Abrir a gaveta lateral de opções (Drawer)
        binding.btnMenuMain.setOnClickListener{setDrawer()}

        // Abrir a barra de pesquisa de músicas
        binding.btnSearch.setOnClickListener{openSearch()}

        /* Abrir a tela do player clicando no miniplayer
         binding.miniPlayerMain.setOnClickListener{
             startActivity(Intent(this, PlayerActivity::class.java))
             closeSearch()}
        */

        /*  binding.navLayout.setNavigationItemSelectedListener {
              when(it.itemId){
                  R.id.item_musicas ->
                  R.id.item_albuns ->
                  R.id.item_artistas ->
                  R.id.item_favoritos ->
                  R.id.item_playlists ->
                  R.id.item_config ->
                  R.id.item_equalizador ->
                  R.id.item_sobre ->
              }
              true
          }
          */
    }

    // Método que faz a checa se foram concedidas as permissões que o app precisa
    private fun checarPermissoes(){
        // Se o aplicativo ainda não tiver a permissão concedida, fara a requisição da mesma, caso contrário, nada acontece e a pessoa pode utilizar o aplicativo normalmente
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(applicationContext, PermissaoActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 500)
        }
    }

    // Método para abrir e fechar o DrawerLayout
    private fun setDrawer() {
        binding.drawerLayoutMain.openDrawer(GravityCompat.START)
        closeSearch()
    }

    private fun closeSearch(){
        binding.tituloMain.isVisible = true
        binding.searchBar.isGone = true
    }

    // Função que é passada para o botão search, para que quando ele seja clicado, abra a EditText de pesquisa, e esconda a TextView "Todas as músicas"
    private fun openSearch() {
        binding.searchBar.requestFocus()
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        binding.tituloMain.isGone = true
        binding.searchBar.isVisible = true
    }

    // Método para chamar tudo que será a inicialização do layout da tela inicial
    private fun iniciarLayout(){
        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Checagem de permissões quando o usuário entrar na tela principal
        checarPermissoes()
        setTheme(R.style.temaClaroNav)

        // Lista de músicas
        ListaMusicaMain = procurarMusicas()

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.musicasRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.musicasRv.setItemViewCacheSize(13)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.musicasRv.layoutManager = LinearLayoutManager(this@MainActivity)

        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@MainActivity, ListaMusicaMain)
        // Setando o Adapter para este RecyclerView
        binding.musicasRv.adapter = musicaAdapter
    }

    // Método que faz a procura de músicas pelos arquivos do celular
    @SuppressLint("Recycle", "Range")
    private fun procurarMusicas(): ArrayList<Musica>{
        // Lista temporária de músicas
        val tempLista = ArrayList<Musica>()
        // Condições para a seleção das músicas dos arquivos
        val selectMusica = MediaStore.Audio.Media.IS_MUSIC + ">= 0"
        // Array de dados que serão retornados dos arquivos
        val dadosMusica = arrayOf(
            MediaStore.Audio.Media._ID, // ID da música
            MediaStore.Audio.Media.TITLE, // Título da música
            MediaStore.Audio.Media.ARTIST, // Artista da música
            MediaStore.Audio.Media.ALBUM, // Álbum da música
            MediaStore.Audio.Media.DURATION, // Duração da música
            MediaStore.Audio.Media.DATE_ADDED, // Data de quando a música foi adicionada ao aplicativo
            MediaStore.Audio.Media.DATA) // Caminho da música

        // Cursor é o mecanismo que faz a busca e seleciona as músicas e as organiza com base nas condições passadas nos parâmetros
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, dadosMusica, selectMusica, null, MediaStore.Audio.Media.TITLE + " ASC", null)

        // If que, se houverem arquivos de áudios, o cursor começará a passar por todos eles um por um,
        // retornando seus dados e os adicionando ao modelo do array da música (Musica.kt),
        // e então as adicionando para a lista de músicas temporarias
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) // Cursor procura e adiciona o ID da música
                    val tituloC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) // Cursor procura e adiciona o título da música
                    val artistaC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) // Cursor procura e adiciona o artista da música
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) // Cursor procura e adiciona o álbuns da música
                    val duracaoC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) // Cursor procura e adiciona o duração da música
                    val caminhoC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) // Cursor procura e adiciona o caminho da música

                    // Passando os dados retornados da música para o modelo do array da música (Musica.kt)
                    val musica = Musica(id = idC, titulo = tituloC, artista = artistaC, album = albumC, duracao = duracaoC, caminho = caminhoC)
                    // Passando o caminho da música para uma constante que o identifica como um arquivo
                    val arquivo = File(musica.caminho)
                    // Se o arquivo existir, ele será adicionado para a lista de músicas,
                    // se ele foi excluído, não aparecerá mais na lista quando o aplicativo for iniciado novamente
                    if (arquivo.exists())
                        tempLista.add(musica)

                } while (cursor.moveToNext())
                cursor.close() // Termina o cursor, para que ele não fique executando o loop de pesquisa infinitamente
        }
        return tempLista // Retorna a lista de músicas para o ArrayList<Musica>
    }

}