package com.example.bloom

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
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
        //val repetirIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.REPETIR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        //val repetirPendingIntent = PendingIntent.getBroadcast(baseContext, 0, repetirIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Cria o objeto que contém a ação de abrir o app na tela principal
        val notifyPlayerIntent = Intent(baseContext, MainActivity::class.java)
        // Leva o usuário para a tela principal ao clicar na notificação
        val notifyContentIntent = PendingIntent.getActivity(this, 0, notifyPlayerIntent, FLAG_IMMUTABLE)

        // Cria o objeto que contém a ação de voltar a música
        val anteriorIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.ANTERIOR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val anteriorPendingIntent = PendingIntent.getBroadcast(baseContext, 0, anteriorIntent, FLAG_IMMUTABLE)

        // Cria o objeto que contém a ação de tocar/pausar a música atual
        val tocarIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.TOCAR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val tocarPendingIntent = PendingIntent.getBroadcast(baseContext, 0, tocarIntent, FLAG_IMMUTABLE)

        // Cria o objeto que contém a ação de ir para a próxima música da lista
        val proximoIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.PROXIMO)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val proximoPendingIntent = PendingIntent.getBroadcast(baseContext, 0, proximoIntent, FLAG_IMMUTABLE)

        // Cria o objeto que contém a ação de favoritar a música atual
        val favoritarIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.FAVORITAR)
        // Cria a intent pendente, que é passada para a ação dos botões da barra de notificação
        val favoritarPendingIntent = PendingIntent.getBroadcast(baseContext, 0, favoritarIntent, FLAG_IMMUTABLE)

        // Cria o objeto que contém a ação de limpar a barra de músicas
        val limparIntent = Intent(baseContext, NotificacaoReceiver::class.java).setAction(Application.LIMPAR)
        // Cria a intent pendente para a intent de limpar a notificação
        val limparPendingIntent = PendingIntent.getBroadcast(baseContext, 0, limparIntent, FLAG_IMMUTABLE)

        // Utiliza o método de Musica.kt para retornar a imagem da música com base no caminho da música da lista
        // O try catch evita que um crash aconteça caso o usuário exclua a música anterior ou a próxima da atual e a reprodução mude para uma delas
        val imgMusica = try{
            retornarImgMusica(PlayerActivity.filaMusica[PlayerActivity.posMusica].caminho)
        }catch (e: Exception){return}
        val imagemNotificacao =
            // Se o objeto imgMusica for diferente de nulo, então "imagemNotificação" será a decodificação do array de bytes da imagem da música
            if(imgMusica != null){
                BitmapFactory.decodeByteArray(imgMusica, 0, imgMusica.size)
                // Caso contrário, será a imagem padrão definida abaixo
            }else{
                BitmapFactory.decodeResource(resources, R.drawable.placeholder_grey)
            }

        // Criação da notificação
        val notificacao = NotificationCompat.Builder(baseContext, Application.ID_CANAL)
            // Título da música na barra de notificação
            .setContentTitle(PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo)
            // Artista da música na barra de notificação
            .setContentText(PlayerActivity.filaMusica[PlayerActivity.posMusica].artista + " ● " + PlayerActivity.filaMusica[PlayerActivity.posMusica].album)
            // Ícone pequeno da barra de notificação
            .setSmallIcon(R.drawable.bloom_logo_app)
            // Imagem da música atual na barra de notificação
            .setLargeIcon(imagemNotificacao)
            // Define o estilo da notificação, como o estilo padrão de notificações de um media player
            // juntamente de um token que representa a música atual sendo reproduzida para mostrá-la na notificação
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(sessaoMusica.sessionToken))
            // Prioridade da notificação para sempre dar prioridade alta para notificação do Bloom
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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

        // Constante da velocidade da seekbar da barra da notificação
        // Se estiver tocando então a velocidade é de 1F (1 float) e se não estiver tocando 0F
        val velocidadeSB = if(PlayerActivity.tocando) 1F else 0F

        // setMetadata serve para atualizar as informações da barra de notificação
        // esse código serve para mostrar a duração da música na notificação
        sessaoMusica.setMetadata(MediaMetadataCompat.Builder()
            // METADATA_KEY_DURATION retorna a duração da música atual
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayer!!.duration.toLong())
            .build())

        // Criando a SeekBar da barra de notificação
        val posicaoSeekBar = PlaybackStateCompat.Builder()
            // setState é referente ao estado (STATE_PLAYING) a posição atual da música.
            // velocidadeSB é a velocidade em que ela atualiza (se move)
            .setState(PlaybackStateCompat.STATE_PLAYING, mPlayer!!.currentPosition.toLong(), velocidadeSB)
            // Define uma ação para a seekBar (seekTo é o método de arrastar a bolinha da seekBar para alterar a decorrência da música)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
            .build()

        // Define o estado do playback (seekBar)
        sessaoMusica.setPlaybackState(posicaoSeekBar)
        // Define um callback para a barra de notificação
        sessaoMusica.setCallback(object: MediaSessionCompat.Callback(){
            // Chama essa função quando o botão do fone do usuário for clicado
            override fun onMediaButtonEvent(clique: Intent?): Boolean {
                // Se estiver tocando a música quando for clicado
                if(PlayerActivity.tocando){
                    // Então pausa a música
                    PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
                    PlayerActivity.tocando = false
                    mPlayer!!.pause()
                    setBtnsNotify()
                // Caso contrário (música pausada)
                }else{
                    // Então toca a música
                    PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
                    PlayerActivity.tocando = true
                    mPlayer!!.start()
                    setBtnsNotify()
                }
                // Retorna o clique do usuário
                return super.onMediaButtonEvent(clique)
            }
            // Método onSeekTo para mudar o tempo da música
            override fun onSeekTo(posicao: Long) {
                super.onSeekTo(posicao)
                mPlayer!!.seekTo(posicao.toInt())
                // Cria um novo estado para o playback (posição para a seekbar)
                val novaPosicaoSeekBar = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mPlayer!!.currentPosition.toLong(), velocidadeSB)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
                // Define o estado do playback (seekBar)
                sessaoMusica.setPlaybackState(novaPosicaoSeekBar)
            }
        })

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
            // Chama o método para definir os botões da barra de notificações
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
            // Define a música atual do player a música que estiver tocando
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
            if(PlayerActivity.favoritado){
                mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_24)
            }else{
                mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_border_24)
            }
            PlayerActivity.tocando = false
            mPlayer!!.pause()
        }
    }

    // Utilizando START_STICKY para não encerrar ou pausar a reprodução de músicas quando o smartphone
    // estiver com a tela apagada.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}