package com.example.bloom

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
import com.example.bloom.databinding.ActivityFavoritosBinding


class FavoritosActivity : AppCompatActivity() {


    private lateinit var binding : ActivityFavoritosBinding   // Variável usada para ligar os componentes da tela
    private lateinit var setMenuDrawer : ActionBarDrawerToggle

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Define o título da tela atual
        supportActionBar?.title = "Favoritos"
        // Elevação 0 na actionBar
        supportActionBar?.elevation = 0F

        // Inicialização do binding
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityFavoritosBinding (activity_favoritos.xml)
        setContentView(binding.root)

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        //binding.btnVoltarFav.setOnClickListener {finish()}

        // Abrir a gaveta lateral de opções (Drawer)
        setMenuDrawer = ActionBarDrawerToggle(this, binding.root, R.string.drawer_aberto, R.string.drawer_fechado)
        // Adiciona um listener para o layout do Drawer
        binding.root.addDrawerListener(setMenuDrawer)
        // Sincroniza o ícone do drawer ao estado dele
        setMenuDrawer.syncState()
        // Mostra o ícone de menu
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Muda a cor do ícone do drawer
        setMenuDrawer.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_Bloom)
        setTheme(R.style.Theme_Bloom)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }

    // Método para seleção de itens do drawer
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (setMenuDrawer.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    // Método para mostrar e indentificar o ícone de pesquisa na ActionBar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pesquisa_menu, menu)
        val searchView = menu?.findItem(R.id.pesquisa_view)?.actionView as SearchView
        // Fica lendo o que o usuário está digitando na barra de pesquisa
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            // O texto é sempre enviado, com ou sem a confirmação de pesquisa do usuário
            // Dessa forma, a lista atualiza na hora com base na pesquisa do usuário
            override fun onQueryTextSubmit(query: String?): Boolean = true

            // Quando o texto muda,
            override fun onQueryTextChange(newText: String?): Boolean {
                Toast.makeText(this@FavoritosActivity, newText.toString(), Toast.LENGTH_SHORT).show()
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }
}