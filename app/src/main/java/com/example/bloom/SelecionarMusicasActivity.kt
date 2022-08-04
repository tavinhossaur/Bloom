package com.example.bloom

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivitySelecionarMusicasBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet

class SelecionarMusicasActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySelecionarMusicasBinding
    private lateinit var musicaAdapter: MusicaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivitySelecionarMusicasBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityConteudoPlaylistBinding (activity_conteudo_playlist.xml)
        setContentView(binding.root)

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.slcMusicaRv.setHasFixedSize(true)
        // Para este caso específico, o tamanho do cache precisa ser grande por conta do indicador de seleção
        // de músicas do app
        binding.slcMusicaRv.setItemViewCacheSize(5000)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.slcMusicaRv.layoutManager = LinearLayoutManager(this@SelecionarMusicasActivity)
        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@SelecionarMusicasActivity, MainActivity.listaMusicaMain, activitySelecionar = true)
        // Setando o Adapter para este RecyclerView
        binding.slcMusicaRv.adapter = musicaAdapter

        binding.btnVoltarSlc.setOnClickListener {finish()}

        binding.btnInfo.setOnClickListener {
            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
            val permSheet = InfoSheet().build(this) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Título do AlertDialog
                title("Adicionando músicas")
                // Cor do título
                titleColorRes(R.color.purple1)
                // Mensagem do AlertDialog
                content("Para adicionar as músicas, basta clicar em uma delas e ela será automáticamente adicionada. Caso você deseje remover a música adicionada, basta clicar novamente na mesma.")

                // Botão positivo que exclui a playlist em questão
                positiveButtonColorRes(R.color.purple1)
                onPositive("Entendido") {
                    dismiss()
                }
            }
            // Mostra o AlertDialog
            permSheet.show()
        }

        val pesquisaView = binding.pesquisaViewSlc
        pesquisaView.queryHint = "Procure por título, artista, álbum..."
        pesquisaView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text).setHintTextColor(ContextCompat.getColor(this@SelecionarMusicasActivity, R.color.grey3))

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
                            musicaAdapter.atualizarLista(listaPesquisa = MainActivity.listaMusicaPesquisa)
                        }
                }
                return true
            }
        })
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
}