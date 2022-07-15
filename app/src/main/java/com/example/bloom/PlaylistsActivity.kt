package com.example.bloom

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.bloom.databinding.ActivityPlaylistsBinding

class PlaylistsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPlaylistsBinding // binding é a variável do ViewBinding para ligar as views ao código
    private lateinit var setMenuDrawer : ActionBarDrawerToggle

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Define o título da tela atual
        supportActionBar?.title = "Playlists"
        // Elevação 0 na actionBar
        supportActionBar?.elevation = 0F

        // Inicialização do binding
        binding = ActivityPlaylistsBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlaylistsBinding (activity_playlists.xml)
        setContentView(binding.root)

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        //.btnVoltarPl.setOnClickListener {finish()}

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
}