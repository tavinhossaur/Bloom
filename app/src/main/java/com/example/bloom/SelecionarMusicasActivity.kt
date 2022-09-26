package com.example.bloom

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.MainActivity.Companion.escuro
import com.example.bloom.databinding.ActivitySelecionarMusicasBinding

// Classe da activity
class SelecionarMusicasActivity : AppCompatActivity() {

    private lateinit var musicaAdapter: MusicaAdapter // Variável que leva a classe MusicAdapter

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivitySelecionarMusicasBinding // binding é a variável do ViewBinding para ligar as views ao código
        var escuroSelect : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
    }

    // Método chamado quando o aplicativo/activity é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BloomNoActionBar)
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivitySelecionarMusicasBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivitySelecionarMusicasBinding (activity_selecionar_musicas.xml)
        setContentView(binding.root)

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.slcMusicaRv.setHasFixedSize(true)
        // Para este caso específico, o tamanho de itens no cache precisa ser
        // grande por conta do indicador de seleção de músicas do app
        binding.slcMusicaRv.setItemViewCacheSize(5000)
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.slcMusicaRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.slcMusicaRv.layoutManager = LinearLayoutManager(this@SelecionarMusicasActivity)
        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@SelecionarMusicasActivity, MainActivity.listaMusicaMain, activitySelecionar = true)
        // Setando o Adapter para este RecyclerView
        binding.slcMusicaRv.adapter = musicaAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.slcMusicaRv.isMotionEventSplittingEnabled = false

        binding.btnVoltarSlc.setOnClickListener {finish()}

        if (MainActivity.listaMusicaMain.size < 1){
            binding.slcMusicaRv.visibility = View.GONE
            binding.avisoSelecionar.visibility = View.VISIBLE
        }else{
            binding.slcMusicaRv.visibility = View.VISIBLE
            binding.avisoSelecionar.visibility = View.GONE
        }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            escuroSelect = true
            binding.btnVoltarSlc.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
        }else {escuroSelect = false}

        val pesquisaView = binding.pesquisaViewSlc
        // Hint da pesquisa
        pesquisaView.queryHint = "Procure por título, artista, álbum..."

        // Para pesquisas de músicas
        binding.pesquisaViewSlc.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            // O texto é sempre enviado, com ou sem a confirmação de pesquisa do usuário
            // Dessa forma, a lista atualiza na hora com base na pesquisa do usuário
            override fun onQueryTextSubmit(query: String?): Boolean = true

            // Quando o texto muda
            override fun onQueryTextChange(textoPesquisa: String?): Boolean {
                // Cria a lista de músicas da pesquisa
                MainActivity.listaMusicaPesquisa = ArrayList()
                // Se o texto da pesquisa não for nulo
                if (textoPesquisa != null){
                    // Passa o texto dela para caixa baixa para pode encontrar a música de forma mais fácil
                    val pesquisa = textoPesquisa.lowercase()
                    // Para cada música na lista de músicas da tela principal
                    for (musica in MainActivity.listaMusicaMain)
                    // Se o título, artista ou álbum da música, em caixa baixa conter o texto da pesquisa
                        if (musica.titulo.lowercase().contains(pesquisa) || musica.artista.lowercase().contains(pesquisa) || musica.album.lowercase().contains(pesquisa)){
                            // Então adicione essa música a lista de pesquisa
                            MainActivity.listaMusicaPesquisa.add(musica)
                            // Defina pesquisando como true
                            MainActivity.pesquisando = true
                            // E atualize a lista de músicas pesquisadas
                            musicaAdapter.atualizarLista(MainActivity.listaMusicaPesquisa)
                        }
                }
                return true
            }
        })
    }
}