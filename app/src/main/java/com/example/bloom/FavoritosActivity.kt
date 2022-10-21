package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityFavoritosBinding

class FavoritosActivity : AppCompatActivity() {

    private lateinit var favoritosAdapter : FavoritosAdapter // Variável que leva a classe FavoritosAdapter

    companion object{
        var listaFavoritos : ArrayList<Musica> = ArrayList() // Lista de músicas favoritadas
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityFavoritosBinding // binding é a variável do ViewBinding para ligar as views ao código
        var escuroFav : Boolean = false // Variável para definir se o modo escuro está ligado ou desligado
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityFavoritosBinding (activity_favoritos.xml)
        setContentView(binding.root)

        // Faz a checagem de músicas apagadas que poderiam estar favoritadas
        checarMusicasApagadas(listaFavoritos)

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.favoritosRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.favoritosRv.setItemViewCacheSize(13)
        // Desativa o nested scrolling para a rolagem ser mais suave
        binding.favoritosRv.isNestedScrollingEnabled = false
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.favoritosRv.layoutManager = LinearLayoutManager(this@FavoritosActivity)

        // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        favoritosAdapter = FavoritosAdapter(this@FavoritosActivity, listaFavoritos)
        // Setando o Adapter para este RecyclerView
        binding.favoritosRv.adapter = favoritosAdapter
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.favoritosRv.isMotionEventSplittingEnabled = false

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarFav.setOnClickListener {
            // Muda a animação do botão ao ser clicado
            binding.btnVoltarFav.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            finish()
        }

        // Quando clicado no botão de randomizar músicas da tela de favoritos
        binding.fabFavRandom.setOnClickListener {
            val favIntent = Intent(this@FavoritosActivity, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            favIntent.putExtra("indicador", 0)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe da MainActivity (String)
            favIntent.putExtra("classe", "FavoritosRandom")
            startActivity(favIntent)
        }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            escuroFav = true
            binding.btnVoltarFav.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.tituloActivityFav.setTextColor(ContextCompat.getColor(this, R.color.grey2))
            binding.barFav.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.fabFavRandom.setBackgroundColor(ContextCompat.getColor(this, R.color.black3))
        }else {escuroFav = false}
    }

    override fun onResume() {
        super.onResume()
        favoritosAdapter.atualizarFavoritos(listaFavoritos)
        // Caso não tenha nenhuma música favoritada ainda
        if (listaFavoritos.size < 1){
            // Esconde o botão de randomizar as músicas favoritas
            binding.fabFavRandom.visibility = View.INVISIBLE
            // Esconde o RecyclerView das músicas favoritas
            binding.favoritosRv.visibility = View.GONE
            // E mostra o aviso dizendo que não há músicas favoritas
            binding.avisoFavoritas.visibility = View.VISIBLE
            binding.imgCoracao.visibility = View.VISIBLE
        }
    }
}