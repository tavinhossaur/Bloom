package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.options.Option
import com.maxkeppeler.sheets.options.OptionsSheet
import java.io.File

// Classe da activity
class MainActivity : AppCompatActivity() {

    // Declaração de objetos/classes estáticas para poder utilizar
    companion object{
        lateinit var listaMusicaMain : ArrayList<Musica>     // Lista de músicas da tela principal
        lateinit var listaMusicaPesquisa : ArrayList<Musica> // Lista de músicas que aparecerá na pesquisa
        @SuppressLint("StaticFieldLeak")
        lateinit var musicaAdapter : MusicaAdapter // Variável que leva a classe MusicAdapter
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityMainBinding           // binding é a variável do ViewBinding para ligar as views ao código
        var pesquisando : Boolean = false                    // Variável para indentificar se o usuário está fazendo uma pesquisa de músicas
        var ordem : Int = 0                                  // Variável que leva um valor para indicar qual ordem a lista de músicas está ordenada
        val listaOrdens = arrayOf(
            // Lista de ordenações
            // para a organização alfabética, está sendo utilizado o código em SQL "COLLATE NOCASE ASC", sendo COLLATE a cláusula que
            // define uma ordenação, essa ordenação recebe como argumentos o "NOCASE" que torna a ordenação case-insensitive e "ASC" seria
            // "ascendente" ou seja, alfabéticamente de A a Z.
            // Por nome de artista com "COLLATE NOCASO ASC"
            MediaStore.Audio.Media.ARTIST + " COLLATE NOCASE ASC",
            // Por título da música com "COLLATE NOCASO ASC"
            MediaStore.Audio.Media.DISPLAY_NAME + " COLLATE NOCASE ASC",
            // Pela data adicionada de forma decrescente, ou seja, o mais recente primeiro
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            // E pela duração também de forma decrescente, a música mais longa música primeiro
            MediaStore.Audio.Media.DURATION + " DESC",
        )
        var selectMusica : String  = "" // Variável de seleção da música no armazenamento
        var escuro : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
    }

    // Método chamado quando o aplicativo/activity é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
        super.onCreate(savedInstanceState)

        // Método da api da Google para gerar uma Splash Screen
        installSplashScreen()

        // Define o estilo customizado da action bar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar_layout)
        // Elevação 0 na actionBar
        supportActionBar?.elevation = 0F

        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityMainBinding (activity_main.xml)
        setContentView(binding.root)

        // Checagem de permissões quando o usuário entrar na tela principal
        if(checarPermissoes()){
            iniciarLayout()

            // Para retornar os dados do usuário das músicas favoritas
            FavoritosActivity.listaFavoritos = ArrayList()
            val editor= getSharedPreferences("Favoritos", MODE_PRIVATE)
            val jsonStringFavoritos = editor.getString("Lista de favoritos", null)
            val typeTokenFavoritos = object : TypeToken<ArrayList<Musica>>(){}.type

            if (jsonStringFavoritos != null){
                val dataFavoritos : ArrayList<Musica> = GsonBuilder().create().fromJson(jsonStringFavoritos, typeTokenFavoritos)
                FavoritosActivity.listaFavoritos.addAll(dataFavoritos)
            }

            // Para retornar os dados do usuário das playlists criadas
            PlaylistsActivity.playlists = ModeloPlaylist()
            val jsonStringPlaylists = editor.getString("Lista de playlists", null)

            if (jsonStringPlaylists != null){
                val dataPlaylists : ModeloPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylists, ModeloPlaylist::class.java)
                PlaylistsActivity.playlists = dataPlaylists
            }
        }

        // Ao clicar no switch da configuração 2, chama o método para ativar ou desligar o modo escuro
        // binding.modoEscuroBtn.setOnClickListener{ modoEscuro() }

        // Abrir tela de configurações
        binding.configsBtn.setOnClickListener {
            startActivity(Intent(this, ConfiguracoesActivity::class.java))
        }

        // Abrir a tela de favoritos
        binding.favoritosBtn.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        // Abrir a tela de playlists
        binding.playlistsBtn.setOnClickListener {
            startActivity(Intent(this, PlaylistsActivity::class.java))
        }

        // Randomizar as músicas
        binding.randomBtn.setOnClickListener {
            // A lista de músicas precisa conter pelo menos uma música para funcionar
            if (listaMusicaMain.size < 1){
                // Criação de um toast para informar o usuário de que ele não possui músicas suficientes para utilizar a funcionalidade
                Toast.makeText(this, "Você não possui músicas!", Toast.LENGTH_SHORT).show()
            }else{
            // Se houver uma ou mais músicas, leve o usuário para o player com os dados para randomizá-la
            val mainIntent = Intent(this@MainActivity, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            mainIntent.putExtra("indicador", 0)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe da MainActivity (String)
            mainIntent.putExtra("classe", "Main")
            startActivity(mainIntent)
            }
        }

        binding.filtroBtn.setOnClickListener {
            binding.filtroBtn.isEnabled = false
            var ordemAtual: Int
            OptionsSheet().show(this) {
                // Estilo do sheet (BottomSheet)
                style(SheetStyle.BOTTOM_SHEET)
                // Título do BottomSheetDialog
                title("Ordenar músicas")
                // Marca como falso as múltiplas opções
                multipleChoices(false)
                // Mantém as cores dos ícones
                preventIconTint(true)
                // Cor do título
                titleColorRes(R.color.purple1)
                // Conteúdo da sheet (Radio Buttons)
                with(
                    Option(R.drawable.ic_round_person_24, "Por artista"),
                    Option(R.drawable.ic_round_library_music_24, "Por título"),
                    Option(R.drawable.ic_round_calendar_today_24, "Por data adicionada"),
                    Option(R.drawable.ic_round_access_time_filled_24, "Por duração")
                )
                // Cor do botão "confirmar"
                positiveButtonColorRes(R.color.purple1)
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { binding.filtroBtn.isEnabled = true }
                // Botão confirmar do BottomSheet
                onPositive { index: Int, _: Option ->
                    // Quando o index (selecionado)
                    when(index){
                        // For "0" (Por artista)
                        0 -> {
                            // Então a ordem atual será o valor 0 do array da lista de ordens
                            ordemAtual = 0

                            val editor = getSharedPreferences("ORDEM", MODE_PRIVATE).edit()
                            editor.putInt("ordemSet", ordemAtual)
                            editor.apply()
                        }
                        1 -> {
                            ordemAtual = 1

                            val editor = getSharedPreferences("ORDEM", MODE_PRIVATE).edit()
                            editor.putInt("ordemSet", ordemAtual)
                            editor.apply()
                        }
                        2 -> {
                            ordemAtual = 2

                            val editor = getSharedPreferences("ORDEM", MODE_PRIVATE).edit()
                            editor.putInt("ordemSet", ordemAtual)
                            editor.apply()
                        }
                        3 -> {
                            ordemAtual = 3

                            val editor = getSharedPreferences("ORDEM", MODE_PRIVATE).edit()
                            editor.putInt("ordemSet", ordemAtual)
                            editor.apply()
                        }
                    }
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
                // Cor do botão negativo
                negativeButtonColorRes(R.color.grey3)
            }
        }
    }

    // Método que faz a checa se foram concedidas as permissões que o app precisa
    private fun checarPermissoes() : Boolean{
        // Se o aplicativo ainda não tiver a permissão concedida, fara a requisição da mesma, caso contrário, nada acontece e a pessoa pode utilizar o aplicativo normalmente
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(applicationContext, PermissaoActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 500)
            return false
        }
        return true
    }

    // Método para chamar tudo que será a inicialização do layout da tela inicial
    private fun iniciarLayout(){
        pesquisando = false
        // Lista de músicas
        val ordemEditor = getSharedPreferences("ORDEM", MODE_PRIVATE)
        // Retorna a ordem da lista selecionada pelo usuário
        ordem = ordemEditor.getInt("ordemSet", 0)
        // E procura as músicas
        listaMusicaMain = procurarMusicas()
        // Se a quantidade de músicas for maior ou igual a 1
        if(listaMusicaMain.size >= 1){
            // Mostra a lista e esconde o aviso
            binding.avisoMusicas.visibility = View.GONE
            binding.musicasRv.visibility = View.VISIBLE
        // Caso contrário (não há músicas)
        }else{
            // Esconde a lista e mostra o aviso de que não há músicas
            binding.avisoMusicas.visibility = View.VISIBLE
            binding.musicasRv.visibility = View.GONE
        }
        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.musicasRv.setHasFixedSize(true)
        // Para este caso específico, o tamanho de itens no cache precisa ser
        // grande por conta do indicador de música atual tocando
        binding.musicasRv.setItemViewCacheSize(5000)
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.musicasRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.musicasRv.layoutManager = LinearLayoutManager(this@MainActivity)

        // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@MainActivity, listaMusicaMain)
        // Setando o Adapter para este RecyclerView
        binding.musicasRv.adapter = musicaAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.musicasRv.isMotionEventSplittingEnabled = false

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            escuro = true
            binding.filtroImg.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
        }else{
            escuro = false
        }
    }

    // Método que faz a procura de músicas pelos arquivos do celular
    @SuppressLint("Recycle", "Range")
    private fun procurarMusicas(): ArrayList<Musica> {
        // Lista temporária de músicas
        val lista = ArrayList<Musica>()
        // Condições para a seleção das músicas dos arquivos "!= 0" (diferente de 0) significa que o cursor procurará apenas músicas
        // e não ringtones do android " AND " título não igual a "AUD%" ou seja, o título da música não pode ser igual a
        // AUD + zero ou outros caracteres.
        // Se o switch1 das configurações for true, então os áudios do WhatsApp não vão ser selecionados
        // Array de dados que serão retornados dos arquivos
        val dadosMusica = arrayOf(
            MediaStore.Audio.Media._ID, // ID da música
            MediaStore.Audio.Media.TITLE, // Título da música
            MediaStore.Audio.Media.ARTIST, // Artista da música
            MediaStore.Audio.Media.ALBUM, // Álbum da música
            MediaStore.Audio.Media.DURATION, // Duração da música
            MediaStore.Audio.Media.ALBUM_ID, // ID do álbum (imagem)
            MediaStore.Audio.Media.DATA
        ) // Caminho da música

        // Cursor é o mecanismo que faz a busca e seleciona as músicas e as organiza com base nas condições passadas nos parâmetros
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            dadosMusica,
            selectMusica,
            null,
            listaOrdens[ordem],
            null
        )

        // If que, se houverem arquivos de áudios, o cursor começará a passar por todos eles um por um,
        // retornando seus dados e os adicionando ao modelo do array da música (Musica.kt),
        // e então as adicionando para a lista de músicas temporarias
        if (cursor != null) {
            if (cursor.moveToFirst())
                do {
                    val idC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)) // Cursor procura e adiciona o ID da música
                    val tituloC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)) // Cursor procura e adiciona o título da música
                    val artistaC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)) // Cursor procura e adiciona o artista da música
                    val albumC =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)) // Cursor procura e adiciona o álbum da música
                    val duracaoC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) // Cursor procura e adiciona o duração da música
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString() // Cursor procura e adiciona o id do álbum da música
                    val uri =
                        Uri.parse("content://media/external/audio/albumart") // Link onde ficará as imagens dos álbuns que o cursor deve retornar
                    val imagemUriC = Uri.withAppendedPath(uri, albumIdC).toString() // A imagemUri é a junção do id com o link da imagem
                    val caminhoC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)) // Cursor procura e adiciona o caminho da música

                    // Passando os dados retornados da música para a classe Musica
                    val musica = Musica(
                        id = idC,
                        titulo = tituloC,
                        artista = artistaC,
                        album = albumC,
                        duracao = duracaoC,
                        caminho = caminhoC,
                        imagemUri = imagemUriC
                    )
                    // Passando o caminho da música para uma constante que o identifica como um arquivo
                    val arquivo = File(musica.caminho)
                    // Se o arquivo existir, ele será adicionado para a lista de músicas,
                    // se ele foi excluído, não aparecerá mais na lista quando o aplicativo for iniciado novamente
                    if (arquivo.exists())
                        lista.add(musica)

                } while (cursor.moveToNext())
            cursor.close() // Termina o cursor, para que ele não fique executando o loop de pesquisa infinitamente
            }
        return lista // Retorna a lista de músicas para o ArrayList<Musica>
    }

    // Método para mostrar (inflar) a pesquisa na action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pesquisa_menu, menu)
        val pesquisaView = menu.findItem(R.id.pesquisa_view)?.actionView as SearchView

        // Texto da hint
        pesquisaView.queryHint = "Procure por título, artista, álbum..."

        // Fica lendo o que o usuário está digitando na barra de pesquisa
        pesquisaView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            // O texto é sempre enviado, com ou sem a confirmação de pesquisa do usuário
            // Dessa forma, a lista atualiza na hora com base na pesquisa do usuário
            override fun onQueryTextSubmit(query: String?): Boolean = true

            // Quando o texto muda
            override fun onQueryTextChange(textoPesquisa: String?): Boolean {
                // Cria a lista de músicas da pesquisa
                listaMusicaPesquisa = ArrayList()
                // Se o texto da pesquisa não for nulo
                if (textoPesquisa != null){
                    // Passa o texto dela para caixa baixa para pode encontrar a música de forma mais fácil
                    var pesquisa = textoPesquisa.lowercase()
                    // Evita um bug que pode ocorrer ao usuário procurar uma música com caracteres especiais na pesquisa
                    pesquisa = pesquisa.replace(" ", "")
                    // Para cada música na lista de músicas da tela principal
                    for (musica in listaMusicaMain)
                        // Se o título, artista ou álbum da música, em caixa baixa conter o texto da pesquisa
                        if (musica.titulo.lowercase().contains(pesquisa) || musica.artista.lowercase().contains(pesquisa) || musica.album.lowercase().contains(pesquisa)){
                            // Então adicione essa música a lista de pesquisa
                            listaMusicaPesquisa.add(musica)
                            // Defina pesquisando como true
                            pesquisando = true
                            // E atualize a lista de músicas pesquisadas
                            musicaAdapter.atualizarLista(listaMusicaPesquisa)
                        }
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    // Método onDestroy, para encerrar o processo do aplicativo
    override fun onDestroy() {
        super.onDestroy()
        // Se a música não estiver tocando e a reprodução de músicas for nula
        if (!PlayerActivity.tocando && PlayerActivity.musicaService != null){
            // Então chame o método para encerrar o processo inteiro do app.
            encerrarProcesso()
        }
    }

    // Método onResume, para quando o usuário volta a activity
    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        // Se o usuário já tiver dado a permissão pro aplicativo, então pode retornar as preferencias salvas
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // SharedPreferences, para salvar a lista de favoritos do usuário
            val editor = getSharedPreferences("Favoritos", MODE_PRIVATE).edit()
            val jsonStringFavoritos = GsonBuilder().create().toJson(FavoritosActivity.listaFavoritos)
            editor.putString("Lista de favoritos", jsonStringFavoritos)
            // SharedPreferences, para salvar a lista de playlists do usuário
            val jsonStringPlaylists = GsonBuilder().create().toJson(PlaylistsActivity.playlists)
            editor.putString("Lista de playlists", jsonStringPlaylists)
            editor.apply()
            // SharedPreferences, para retornar a ordenação da lista de músicas do usuário
            val ordemEditor = getSharedPreferences("ORDEM", MODE_PRIVATE)
            val valorOrdem = ordemEditor.getInt("ordemSet", 0)
            // Se o valor da ordem atual for diferente do valor selecionado
            if (ordem != valorOrdem) {
                // Então iguala o valor
                ordem = valorOrdem
                // E atualiza a lista de músicas procurando e armazenando elas com a nova ordem
                listaMusicaMain = procurarMusicas()
                musicaAdapter.atualizarLista(listaMusicaMain)
            }
            // SharedPreferences, para retornar as definições do usuário
            val switchEditor = getSharedPreferences("SWITCH1", MODE_PRIVATE)
            val switchAud = switchEditor.getBoolean("switchAud", ConfiguracoesActivity.switch1)
            // Se o valor do switch for true
            if (switchAud) {
                // Então os áudios do WhatsApp serão ignorados
                selectMusica = MediaStore.Audio.Media.IS_MUSIC + " != 0" + " AND " + MediaStore.Audio.Media.TITLE + " NOT  LIKE  'AUD%'"
                // E atualiza a lista de músicas procurando novamente por elas com os parâmetros de seleção acima
                listaMusicaMain = procurarMusicas()
                musicaAdapter.atualizarLista(listaMusicaMain)
                // Caso contrário (valor false para o switch)
            } else {
                // Então a seleção será de todos os áudios encontrados
                selectMusica = MediaStore.Audio.Media.IS_MUSIC + " != 0"
                // E atualiza a lista de músicas procurando novamente por elas com os parâmetros de seleção acima
                listaMusicaMain = procurarMusicas()
                musicaAdapter.atualizarLista(listaMusicaMain)
            }
            // Se o usuário abriu a barra de pesquisa e saiu da tela, quando ele voltar a barra de pesquisa estará fechada
            supportActionBar?.collapseActionView()
        }
        // Quando retornado a tela, e o serviço de música não for nulo, então torne o Miniplayer visível.
        if (PlayerActivity.musicaService != null) {
            binding.miniPlayer.visibility = View.VISIBLE
        }
    }
}