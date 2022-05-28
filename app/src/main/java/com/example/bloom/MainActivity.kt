package com.example.bloom

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloom.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding // Variável usada para ligar os componentes da tela

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checarPermissoes()
        setTheme(R.style.temaClaroNav)

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

        mini_player.setOnClickListener{startActivity(Intent(this, PlayerActivity::class.java))}
        btn_menu_main.setOnClickListener{setDrawer()}
        btn_search.setOnClickListener{openSearch()}
    }

    // Método que faz a checa se foram concedidas as permissões que o app precisa
    private fun checarPermissoes(){
        // Se o aplicativo ainda não tiver a permissão concedida, fara a requisição da mesma, caso contrário, nada acontece e a pessoa pode utilizar o aplicativo normalmente
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Handler().postDelayed({
                startActivity(Intent(applicationContext, PermissaoActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 500)
        }
    }

    // Método para abrir e fechar o DrawerLayout
    private fun setDrawer() {
        drawer_layout_main.openDrawer(GravityCompat.START)
        closeSearch()
    }

    private fun closeSearch(){
        bar_title_text.isVisible = true
        search_bar.isGone = true
    }

    // Função que é passada para o botão search, para que quando ele seja clicado, abra a EditText de pesquisa, e esconda a TextView "Todas as músicas"
    private fun openSearch() {
        search_bar.requestFocus()
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        bar_title_text.isGone = true
        search_bar.isVisible = true
    }
}