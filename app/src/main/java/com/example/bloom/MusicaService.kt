package com.example.bloom

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

// Um service é um componente do aplicativo que pode realizar operações longas e não fornece uma interface do usuário.
// É utilizado para serviços vinculados, como esse.
class MusicaService : Service(), AudioManager.OnAudioFocusChangeListener {
    // Variável da vinculação do serviço
    private var appBinder = AppBinder()
    // Variável do player das músicas
    var mPlayer : MediaPlayer? = null
    // Usado para o aplicativo publicar informações de reprodução de mídia na barra de notificação da música
    // e permitir interação com controles de mídia
    private lateinit var sessaoMusica : MediaSessionCompat
    // Objeto Runnable que recebe código para ser executado enquanto estiver ativo
    private lateinit var seekBarRun : Runnable
    // Gerenciador de áudio do celular
    lateinit var audioManager: AudioManager

    // Método callback onBind, método que espera a requisição do usuário pra um serviço
    // IBinder é a parte central para um melhor desempenho de chamadas em processo e interação com um objeto remoto
    override fun onBind(intent: Intent?): IBinder {
        // Representa a sessão atual da música
        sessaoMusica = MediaSessionCompat(baseContext, "Minha música")
        return appBinder
    }

    // Inner class é uma classe dentro de outra que tem acesso aos membros da classe maior (ServiceClass)
    inner class AppBinder : Binder(){
        // Método da reprodução da música que retorna o serviço toda vez que o método é chamado
        fun reproducaoAtual() : MusicaService{
            return this@MusicaService
        }
    }

    // Método da barra de notificação da música
    @SuppressLint("UnspecifiedImmutableFlag", "LaunchActivityFromNotification")
    fun mostrarNotificacao(tocarPausarBtn : Int, favoritosBtn : Int){

        // Cria o objeto que contém a ação de randomizar a reprodução de músicas
        //val repetirIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.REPETIR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        //val repetirPendingIntent = PendingIntent.getBroadcast(baseContext, 0, repetirIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de abrir o app na tela principal
        val notifyPlayerIntent = Intent(baseContext, MainActivity::class.java)
        // Leva o usuário para a tela principal ao clicar na notificação
        val notifyContentIntent = PendingIntent.getActivity(this, 0, notifyPlayerIntent, 0)

        // Cria o objeto que contém a ação de voltar a música
        val anteriorIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.ANTERIOR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val anteriorPendingIntent = PendingIntent.getBroadcast(baseContext, 0, anteriorIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de tocar/pausar a música atual
        val tocarIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.TOCAR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val tocarPendingIntent = PendingIntent.getBroadcast(baseContext, 0, tocarIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de ir para a próxima música da lista
        val proximoIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.PROXIMO)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val proximoPendingIntent = PendingIntent.getBroadcast(baseContext, 0, proximoIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de favoritar a música atual
        val favoritarIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.FAVORITAR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val favoritarPendingIntent = PendingIntent.getBroadcast(baseContext, 0, favoritarIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de limpar a barra de músicas
        val limparIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(ClasseApplication.LIMPAR)
        // Cria a intent pendente para a intent de limpar a notificação
        val limparPendingIntent = PendingIntent.getBroadcast(baseContext, 0, limparIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Utiliza o método de Musica.kt para retornar a imagem da música com base no caminho da música da lista
        val imgMusica = retornarImgMusica(PlayerActivity.filaMusica[PlayerActivity.posMusica].caminho)
        val imagemNotificacao =
            // Se o objeto imgMusica for diferente de nulo, então "imagemNotificação" será a decodificação do array de bytes da imagem da música
            if(imgMusica != null){
                BitmapFactory.decodeByteArray(imgMusica, 0, imgMusica.size)
                // Caso contrário, será a imagem padrão definida abaixo
            }else{
                BitmapFactory.decodeResource(resources, R.drawable.bloom_lotus_icon_grey)
            }

        // Criação da notificação
        val notificacao = NotificationCompat.Builder(baseContext, ClasseApplication.ID_CANAL)
            // Título da música na barra de notificação
            .setContentTitle(PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo)
            // Artista da música na barra de notificação
            .setContentText(PlayerActivity.filaMusica[PlayerActivity.posMusica].artista + " ● " + PlayerActivity.filaMusica[PlayerActivity.posMusica].album)
            // Ícone pequeno da barra de notificação
            .setSmallIcon(R.drawable.ic_round_favorite_24)
            // Ícone grande da logo do app na barra de notificação
            .setLargeIcon(imagemNotificacao)
            // Define o estilo da notificação, como o estilo padrão de notificações de um media player
            // juntamente de um token que representa a música atual sendo reproduzida para mostrá-la na notificação
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(sessaoMusica.sessionToken))
            // Visibilidade da notificação, utilizado para mostrar a notificação até quando estiver na tela de bloqueio
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Fornece uma intent pendente para quando a barra de notificação for clicada pelo usuário
            .setContentIntent(notifyContentIntent)
            // Fornece uma intent pendente para quando a barra de notificação for limpa pelo usuário
            .setDeleteIntent(limparPendingIntent)
            // Adição dos botões de funcionalidades de controle da música na barra de notificação
            //.addAction(R.drawable.ic_baseline_repeat_24, "Repetir", repetirPendingIntent)
            .addAction(R.drawable.ic_round_skip_previous_24, "Anterior", anteriorPendingIntent)
            .addAction(tocarPausarBtn, "Tocar/Pausar", tocarPendingIntent)
            .addAction(R.drawable.ic_round_skip_next_24, "Próximo", proximoPendingIntent)
            .addAction(favoritosBtn, "Favoritar", favoritarPendingIntent)
            // Retorna todas as configurações definidas acima e constrói a barra de notificação
            .build()
        // Toda vez que ocorre a troca de músicas automáticamente, a verificação de favoritos ocorre para não
        // dessincronizar o botão de favoritos da barra de notificação.
        PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        // Coloca a notificação em segundo plano e a inicia
        startForeground(13, notificacao)
    }

    // Método que cria o player da música
    fun criarPlayer() {
        PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        try {
            // Se a variável do player for nula, então ela se torna o MediaPlayer
            if (PlayerActivity.musicaService!!.mPlayer == null) PlayerActivity.musicaService!!.mPlayer = MediaPlayer()
            // .reset = Reseta e o coloca em "idle" (parado e esperando próximo comando)
            PlayerActivity.musicaService!!.mPlayer!!.reset()
            // .setDataSource = Coloca a player no estado "initialized" (música selecionada pronta)
            PlayerActivity.musicaService!!.mPlayer!!.setDataSource(PlayerActivity.filaMusica[PlayerActivity.posMusica].caminho)
            // .prepare = Coloca em estado de "prepared" (esperando pra ser iniciado ou parado)
            PlayerActivity.musicaService!!.mPlayer!!.prepare()
            // E o ícone do botão será o de pausa, já que está tocando
            PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_24)
            // Chama o método para mostrar a barra de notificação da música
            setBtnsNotify()
            // Insere o texto do tempo decorrente formatado da seekBar, com base na posição atual da música no player
            PlayerActivity.binding.decTempoSeekBar.text = formatarDuracao(mPlayer!!.currentPosition.toLong())
            // Insere o texto do tempo final formatado da Seek Bar, com base na duração total da música no player
            PlayerActivity.binding.fimTempoSeekBar.text = formatarDuracao(mPlayer!!.duration.toLong())
            // Define o progresso padrão da Seek Bar como 0, ou seja, o indicador de progresso sempre estará no início da barra
            // e por consequência, a música começará sempre do início
            PlayerActivity.binding.seekBarMusica.progress = 0
            // O progresso máximo do indicador da Seek Bar é definido pela duração total da música
            PlayerActivity.binding.seekBarMusica.max = mPlayer!!.duration
            PlayerActivity.musicaAtual = PlayerActivity.filaMusica[PlayerActivity.posMusica].id
        } catch (e: Exception) {
            return
        }
    }

    // Método que carrega os dados da música e os coloca nas views do player
    fun carregarMusica(){
        PlayerActivity.binding.tituloMusicaTpl.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo
        PlayerActivity.binding.artistaMusicaTpl.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].artista
        PlayerActivity.binding.albumMusicaTpl.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].album
        PlayerActivity.binding.fimTempoSeekBar.text = formatarDuracao(PlayerActivity.filaMusica[PlayerActivity.posMusica].duracao)
    }

    // Método para configuração da Seek Bar, para que ela percorra juntamente do progresso da música
    fun carregarSeekBar(){
        seekBarRun = Runnable{
            // Insere o texto do tempo decorrente formatado da seekBar, com base na posição atual da música no player
            PlayerActivity.binding.decTempoSeekBar.text = formatarDuracao(mPlayer!!.currentPosition.toLong())
            // O progresso da Seek Bar é definido como a posição atual da música
            PlayerActivity.binding.seekBarMusica.progress = mPlayer!!.currentPosition
            // Delay de tempo em que o indicador de progresso da Seek Bar é atualizado, ou seja, atualizado a cada 100 milissegundos
            Handler(Looper.getMainLooper()).postDelayed(seekBarRun, 100)
        }
        Handler(Looper.getMainLooper()).postDelayed(seekBarRun, 0)
    }

    // Metodo que verifica se o foco do áudio do celular foi mudado
    // Essa mudança ocorre quando há uma ligação, execução de outro áudio e notificações (sem modo silencioso)
    override fun onAudioFocusChange(foco: Int) {
        // Se o foco mudar
        if (foco <= 0){
            // Então pause a música
            PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
            MiniPlayerFragment.binding.btnPpMp.setImageResource(R.drawable.ic_round_play_circle_24)
            if(PlayerActivity.favoritado){
                mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_24)
            }else{
                mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_border_24)
            }
            PlayerActivity.tocando = false
            mPlayer!!.pause()
        // Caso contrário (foco não mudou, ou voltou para o foco normal depois de ter tocado outro áudio, ou uma chamada tenha sido encerrada)
        }else{
            // Então retorne a música
            PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
            MiniPlayerFragment.binding.btnPpMp.setImageResource(R.drawable.ic_round_pause_circle_24)
            if(PlayerActivity.favoritado){
                mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_24)
            }else{
                mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_border_24)
            }
            PlayerActivity.tocando = true
            mPlayer!!.start()
        }
    }
}