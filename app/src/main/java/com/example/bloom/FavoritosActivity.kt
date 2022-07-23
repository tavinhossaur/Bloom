package com.example.bloom

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityFavoritosBinding


class FavoritosActivity : AppCompatActivity() {


    private lateinit var binding : ActivityFavoritosBinding   // Variável usada para ligar os componentes da tela
    private lateinit var favoritosAdapter : FavoritosAdapter

    companion object{
        var listaFavoritos : ArrayList<Musica> = ArrayList()
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityFavoritosBinding (activity_favoritos.xml)
        setContentView(binding.root)

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.favoritosRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.favoritosRv.setItemViewCacheSize(13)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.favoritosRv.layoutManager = LinearLayoutManager(this@FavoritosActivity)

        // Criando uma variável do Adapter com o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        favoritosAdapter = FavoritosAdapter(this@FavoritosActivity, listaFavoritos)
        // Setando o Adapter para este RecyclerView
        binding.favoritosRv.adapter = favoritosAdapter

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarFav.setOnClickListener {finish()}

        if (listaFavoritos.size <= 1){
            binding.fabFavRandom.visibility = View.INVISIBLE
        }

        binding.fabFavRandom.setOnClickListener {
            val favIntent = Intent(this@FavoritosActivity, PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            favIntent.putExtra("indicador", 0)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe da MainActivity (String)
            favIntent.putExtra("classe", "FavoritosRandom")
            startActivity(favIntent)
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
}