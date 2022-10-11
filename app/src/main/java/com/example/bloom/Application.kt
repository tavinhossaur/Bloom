package com.example.bloom

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager

// Classe Application utilizada para iniciar a funcionalidade das notificações junto do aplicativo
class Application : Application() {

    // Declaração de objetos/classes estáticas da barra de notificação
    companion object{
        const val ID_CANAL = "canal1"
        const val TOCAR = "tocar"
        const val PROXIMO = "proximo"
        const val ANTERIOR = "anterior"
        const val FAVORITAR = "favoritar"
        const val LIMPAR = "apagar"
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate() {
        super.onCreate()
        // Objeto do canal de notificação, com o ID, o nome visível para o usuário e a importância da notificação
        val canalNotificacao = NotificationChannel(ID_CANAL, "Tocando agora", NotificationManager.IMPORTANCE_HIGH)
        // Removendo som e vibração da notificação
        canalNotificacao.setSound(null, null)
        canalNotificacao.vibrationPattern = null
        canalNotificacao.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        canalNotificacao.importance = NotificationManager.IMPORTANCE_HIGH
        // Criando o gerenciador da notificação
        val gerenciadorNotificacao = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Registrando o canal notificação ao criador de canal de notificações (createNotificationChannel)
        gerenciadorNotificacao.createNotificationChannel(canalNotificacao)
    }
}