package com.example.bloom

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.MainActivity.Companion.listaMusicaMain
import com.example.bloom.MainActivity.Companion.listaMusicaPesquisa
import com.example.bloom.MainActivity.Companion.pesquisando
import com.example.bloom.databinding.ActivityPesquisarMusicasBinding

// Classe da activity
class PesquisarMusicasActivity : AppCompatActivity() {

    private lateinit var musicaAdapter: MusicaAdapter // Variável que leva a classe MusicAdapter

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPesquisarMusicasBinding // binding é a variável do ViewBinding para ligar as views ao código
        var escuroPesquisar : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
    }

    // Método chamado quando o aplicativo/activity é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_BloomPesquisar)
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPesquisarMusicasBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivitySelecionarMusicasBinding (activity_selecionar_musicas.xml)
        setContentView(binding.root)

        // Define o estilo customizado da action bar
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar_layout)
        // Elevação 0 na actionBar
        supportActionBar?.elevation = 0F

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.pesqMusicaRv.setHasFixedSize(true)
        // Para este caso específico, o tamanho de itens no cache precisa ser
        // grande por conta do indicador de seleção de músicas do app
        binding.pesqMusicaRv.setItemViewCacheSize(10)
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.pesqMusicaRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.pesqMusicaRv.layoutManager = LinearLayoutManager(this@PesquisarMusicasActivity)
        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@PesquisarMusicasActivity, listaMusicaMain, activityPesquisar = true)
        // Setando o Adapter para este RecyclerView
        binding.pesqMusicaRv.adapter = musicaAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.pesqMusicaRv.isMotionEventSplittingEnabled = false

        musicaAdapter.atualizarLista(listaMusicaPesquisa)

        binding.voltarBtn.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.voltarBtn.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            finish()
        }

        if (listaMusicaMain.size < 1){
            binding.pesqMusicaRv.visibility = View.GONE
            binding.avisoPesquisa.visibility = View.VISIBLE
        }else{
            binding.pesqMusicaRv.visibility = View.VISIBLE
            binding.avisoPesquisa.visibility = View.GONE
        }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            escuroPesquisar = true
            binding.voltarImg.setColorFilter(ContextCompat.getColor(this, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.voltarText.setTextColor(ContextCompat.getColor(this, R.color.white))
        }else { escuroPesquisar = false}
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
                    // Evita um bug que pode ocorrer ao usuário procurar uma música com espaços em branco na pesquisa
                    pesquisa = pesquisa.trim()
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
}