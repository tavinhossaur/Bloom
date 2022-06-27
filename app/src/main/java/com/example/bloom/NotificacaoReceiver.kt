package com.example.bloom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

// A classe NotificacaoReceiver tem como principal função receber as intents de controle da música na barra de notificação
// e lidar com as mesmas
class NotificacaoReceiver : BroadcastReceiver() {
    // Método que, quando receber uma intent
    override fun onReceive(context: Context?, intent: Intent?) {
        // Analisará se ela é, por meio da declaração when, a intent de randomizar, voltar uma música, tocar, etc.
        // E executará a ação da intent específica.
        when(intent?.action){
            // Chama o método para randomizar as músicas
            ClasseApplication.RANDOMIZAR -> Toast.makeText(context, "Randomizar", Toast.LENGTH_SHORT).show()
            // Chama o método para voltar para música anterior
            ClasseApplication.ANTERIOR -> proxMusicaAnte(proximo = false, context = context!!)
            // Chama o método para tocar ou pausar a música atual
            ClasseApplication.TOCAR ->
                // Se estiver tocando, então pause e torne possível a exclusão da barra de notificação
                if(PlayerActivity.tocando){
                    pausar()
                    PlayerActivity.musicaService!!.stopForeground(false)
                // Caso contrário (Não estiver tocando), então toque a música
                }else{
                    tocar()
                }
            // Chama o método para ir para próxima música
            ClasseApplication.PROXIMO -> proxMusicaAnte(proximo = true, context = context!!)
            // Chama o método para favoritar a música
            ClasseApplication.FAVORITAR -> Toast.makeText(context, "Favoritar", Toast.LENGTH_SHORT).show()
            // Quando a barra de notificação for limpa, chame o método para resetar o player
            ClasseApplication.LIMPAR -> resetarPlayer()
            }
        }
    }

    // Método para tocar a música pela barra de notificação
    private fun tocar(){
        // Toca a música e muda o ícone do botão na barra e no player
        PlayerActivity.tocando = true
        PlayerActivity.musicaService!!.mPlayer!!.start()
        PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_baseline_pause_notification_bar)
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
    }

    // Método para pausar a música pela barra de notificação
    private fun pausar(){
        // Pausa a música e muda o ícone do botão na barra e no player
        PlayerActivity.tocando = false
        PlayerActivity.musicaService!!.mPlayer!!.pause()
        PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_baseline_play_notification_bar)
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_play)
    }

    // Método para checar se o usuário clicou no botão próxima música ou música anterior,
    // e sincronizar a música atual do player com a música atual que aparece na barra de notificação da música
    private fun proxMusicaAnte(proximo : Boolean, context: Context){
        mudarPosMusica(adicionar = proximo)
        PlayerActivity.musicaService!!.criarPlayer()
        PlayerActivity.musicaService!!.carregarMusica()
        Glide.with(context)
            // Carrega a posição da música e a uri da sua imagem
            .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(PlayerActivity.binding.imgMusicaTpl)
        // Chama o método para tocar a música
        tocar()
    }

    // Método para resetar o player quando a barra de notificação da música for apagada.
    // assim, quando o usuário a apagar, e voltar a tela do player, ele poderá iniciar a música novamente.
    private fun resetarPlayer(){
        try {
            // Se a variável do player for nula, então ela se torna o MediaPlayer
            if (PlayerActivity.musicaService!!.mPlayer == null) PlayerActivity.musicaService!!.mPlayer = MediaPlayer()
            // .reset = Reseta e o coloca em "idle" (parado e esperando próximo comando)
            PlayerActivity.musicaService!!.mPlayer!!.reset()
            // .setDataSource = Coloca a player no estado "initialized" (música selecionada pronta)
            PlayerActivity.musicaService!!.mPlayer!!.setDataSource(PlayerActivity.filaMusica[PlayerActivity.posMusica].caminho)
            // .prepare = Coloca em estado de "prepared" (esperando pra ser iniciado ou parado)
            PlayerActivity.musicaService!!.mPlayer!!.prepare()
        } catch (e: Exception) {
            return
        }
    }