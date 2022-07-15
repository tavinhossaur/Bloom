package com.example.bloom

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.bloom.databinding.ActivitySplashScreenBinding
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.zip.Inflater

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash_screen)
        // A função é chamada assim que essa activity inicia,
        // pois a função da splash screen é apenas aparecer por alguns segundos
        goHome()
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_BloomNoActionBar)
        setTheme(R.style.Theme_BloomNoActionBar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }

    // Método que leva o usuário a alguma das telas após 2 segundos
    private fun goHome(){
        // Se o usuário ainda não tiver concedido a permissão de acessar o armazenamento, vá para a tela de aviso sobre a permissão
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(applicationContext, PermissaoActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 2000)
            // Se o usuário já tiver concedido a permissão de acessar o armazenamento, vá para a tela inicial
        }else{
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish() // Impede que o usuário volte a essa tela usando o botão voltar do celular
            }, 2000)
        }
    }
}