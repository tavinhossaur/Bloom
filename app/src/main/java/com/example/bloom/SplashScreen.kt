package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_AppCompat_temaClaro)
        window.statusBarColor = ContextCompat.getColor(this, R.color.end_gradient)

        setContentView(R.layout.activity_splash_screen)
        goHome() // A função é chamada assim que essa activity inicia, pois a função dela é apenas aparecer por alguns segundos
    }

    // Método que leva o usuário a alguma das telas após 2 segundos
    private fun goHome(){
        // Se o usuário ainda não tiver concedido a permissão de acessar o armazenamento, vá para a tela de aviso sobre a permissão
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
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