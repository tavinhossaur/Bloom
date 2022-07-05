package com.example.bloom

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.ActivityPlayerBinding

// Classe do Player, com a implementação do ServiceConnection que monitora a conexão com o serviço
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    // Declaração de objetos/classes estáticas
    companion object{
        lateinit var filaMusica : ArrayList<Musica> // Fila de reprodução das músicas
        var posMusica : Int = 0 // Posição da música, valor padrão de 0
        var tocando : Boolean = false // Variável para definir se a música está tocando ou não, por padrão: "false"
        var musicaService : MusicaService? = null // Serviço da música, por padrão fica como null
        var repetindo : Boolean = false // Variável para definir se a música está repetindo ou não, por padrão: "false"
        // var randomizando : Boolean = false
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código
    }

    // Método chamado quando o aplicativo é iniciado
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlayerBinding (activity_player.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)
        window.statusBarColor = ContextCompat.getColor(this, R.color.grey2)

        // Cria a intent com a classe MusicService
        val bindIntent = Intent(this, MusicaService::class.java)
        // Conecta o serviço ao player e automaticamente cria o serviço enquanto a conexão existir,
        // essa conexão, define uma dependência do player ao serviço
        bindService(bindIntent, this, BIND_AUTO_CREATE)
        // Permite que a conexão permaneça em execução indefinidamente mesmo que o usuário saia do aplicativo
        // ou seja a música continuará reproduzindo, mesmo quando o usuário sair do app.
        startService(bindIntent)

        iniciarLayout()

        // FUNÇÕES DA ACTIVITY (TELA)
        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnFecharTpl.setOnClickListener {finish()}

        // FUNÇÕES DA EXTRAS
        // Quando clicado no botão de equalizador, ele tentará levar o usuário
        // para o painel de controle de equalizador de som padrão do Android.
        binding.btnEqual.setOnClickListener {
            try {
                // Intent recebendo a activity alvo dela (ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                val equalizadorIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                // Dados extras enviados junto da intent:
                // Sessão de áudio (A sessão atual de áudio sendo reproduzida)
                equalizadorIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicaService!!.mPlayer!!.audioSessionId)
                // O nome do pacote do aplicativo (package com.example.bloom)
                equalizadorIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                // E o tipo de equalizador que será mostrado para o usuário, no caso: "CONTENT_TYPE_MUSIC" (música)
                equalizadorIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                // Lança a intent junto do método para retornar resultados da mesma.
                resultadoIntent.launch(equalizadorIntent)
            }catch (e: Exception){
                // Se por algum motivo, o smartphone não for capaz de acessar o equalizador, o toast abaixo é apresentado
                Toast.makeText(this, "Equalizador não suportado", Toast.LENGTH_SHORT).show()
            }
        }

        // FUNÇÕES DE CONTROLE DO PLAYER
        // Ao clicar no botão play/pause, chama o método para tocar ou pausar a música
        binding.btnPpTpl.setOnClickListener {tocarPausarMusica()}
        // Ao clicar no botão "previous", chama o método trocar música
        // com valor "false" para o Boolean "proximo"
        binding.btnAnte.setOnClickListener {trocarMusica(false)}
        // Ao clicar no botão "next", chama o método trocar música
        // com valor "true" para o Boolean "proximo"
        binding.btnProx.setOnClickListener {trocarMusica(true)}

        // Por padrão o modo da reprodução é definido como 0 (Reprodução normal)
        var modoReproducao = 0

        // Quando for clicado no botão de repetir a música, terão 3 opções
        binding.btnRepetir.setOnClickListener {
            // Toda vez que é clicado aumenta +1 no modo de reprodução até 3
            // Quando chega no 3, as opções resetam
            modoReproducao = (++modoReproducao) % 2 // % 3
            // Quando o modo de reprodução for
            when(modoReproducao){
                // 0 - Reprodução normal da música
                0 -> reproducaoNormal()
                // 1 - Repete a música atual
                1 -> reproducaoRepetir()
                // 2 - Reproduz a playlist randômicamente
                //2 -> reproducaoRandom()
            }
        }

        // Método onSeekBarChangeListener para a barra de progresso da música
        binding.seekBarMusica.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            // Quando o progresso da música mudar
            override fun onProgressChanged(seekBar: SeekBar?, progresso: Int, fromUser: Boolean) {
                // Verificará se foi mudado pelo usuário, e então irá para o lugar onde foi clicado (progresso)
                if (fromUser) {
                    musicaService!!.mPlayer!!.seekTo(progresso)
                }
            }
            // Quando o usuário tocar no indicador ou na SeekBar
            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            // Quando o usuário soltar o indicador ou a SeekBar
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
    }

    // Método para definir a reprodução normal da lista de músicas
    private fun reproducaoNormal(){
        // Se a reprodução for normal, então a música não está repetindo
        repetindo = false
        //randomizando = false
        // O ícone do botão muda para o ícone de reprodução normal
        binding.btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_24)
        // A cor é definida como a cor padrão dos botões do player
        binding.btnRepetir.setColorFilter(resources.getColor(R.color.black1))
        // E um toast é apresentado
        Toast.makeText(this, "Reprodução normal", Toast.LENGTH_SHORT).show()
    }

    // Método para definir a repetição da música atual
    private fun reproducaoRepetir() {
        // Se a reprodução não for normal, então a música está repetindo
        repetindo = true
        //randomizando = false
        // O ícone do botão muda para o ícone de reprodução de uma única música
        binding.btnRepetir.setImageResource(R.drawable.ic_baseline_repeat_one_24)
        // A cor é definida como a cor roxa padrão o app, para indicar que a opção está ligada
        binding.btnRepetir.setColorFilter(resources.getColor(R.color.purple1))
        // E um toast é apresentado
        Toast.makeText(this, "Repetir a música", Toast.LENGTH_SHORT).show()
    }

    /* Método para definir a reprodução randômica da lista de músicas
    private fun reproducaoRandom(){
        repetindo = false
        randomizando = true
        binding.btnRepetir.setImageResource(R.drawable.ic_baseline_shuffle_24)
        binding.btnRepetir.setColorFilter(resources.getColor(R.color.purple1))
        Toast.makeText(this, "Playlist randomizada", Toast.LENGTH_SHORT).show()
    }
     */

    // Método que cria o player da música e faz ela reproduzir
    private fun criarPlayer() {
        try {
            // Se a variável do player for nula, então ela se torna o MediaPlayer
            if (musicaService!!.mPlayer == null) musicaService!!.mPlayer = MediaPlayer()
            // .reset = Reseta e o coloca em "idle" (parado e esperando próximo comando)
            musicaService!!.mPlayer!!.reset()
            // .setDataSource = Coloca a player no estado "initialized" (música selecionada pronta)
            musicaService!!.mPlayer!!.setDataSource(filaMusica[posMusica].caminho)
            // .prepare = Coloca em estado de "prepared" (esperando pra ser iniciado ou parado)
            musicaService!!.mPlayer!!.prepare()
            // .start = Coloca em estado de "started" (inicia o método esperado (started, paused, stopped))
            musicaService!!.mPlayer!!.start()
            // Por padrão, quando o usuário for jogado para a tela do player após ter clicado na música
            // ela vai tocar
            tocando = true
            // E o ícone do botão será o de pausa, já que está tocando
            binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
            // Chama o método para mostrar a barra de notificação da música
            musicaService!!.mostrarNotificacao(R.drawable.ic_baseline_pause_notification_bar)
            // Insere o texto do tempo decorrente formatado da seekBar, com base na posição atual da música no player
            binding.decTempoSeekBar.text = formatarDuracao(musicaService!!.mPlayer!!.currentPosition.toLong())
            // Insere o texto do tempo final formatado da Seek Bar, com base na duração total da música no player
            binding.fimTempoSeekBar.text = formatarDuracao(musicaService!!.mPlayer!!.duration.toLong())
            // Define o progresso padrão da Seek Bar como 0, ou seja, o indicador de progresso sempre estará no início da barra
            // e por consequência, a música começará sempre do início
            binding.seekBarMusica.progress = 0
            // O progresso máximo do indicador da Seek Bar é definido pela duração total da música
            binding.seekBarMusica.max = musicaService!!.mPlayer!!.duration
            // Método que detecta se a música atual foi completada
            musicaService!!.mPlayer!!.setOnCompletionListener(this)
        } catch (e: Exception) {
            return
        }
    }

    // Método que carrega os dados da música e os coloca nas views do player
    private fun carregarMusica(){
        // Utilizando Glide, Procura na fila de músicas a posição da música em específico
        // e retorna sua imagem de álbum no lugar da ImageView da mesma
        Glide.with(this)
            // Carrega a posição da música e a uri da sua imagem
            .load(filaMusica[posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(binding.imgMusicaTpl)

        // Carrega os dados corretos para a música atual sendo reproduzida
        binding.tituloMusicaTpl.text = filaMusica[posMusica].titulo
        binding.artistaMusicaTpl.text = filaMusica[posMusica].artista
        binding.albumMusicaTpl.text = filaMusica[posMusica].album
    }

    @SuppressLint("SetTextI18n")
    private fun iniciarLayout(){
        // Texto do cabeçalho do player, mostrando ao usuário a quantidade total de músicas dele
        binding.musicaAtualTotal.text = "Você tem ${MainActivity.ListaMusicaMain.size} músicas."

        // Para reprodução das músicas
        // Recebe os dados enviados ao ser enviado a tela pela intent
        // Recebe o valor Int da posição da música, o valor padrão é 0
        posMusica = intent.getIntExtra("indicador", 0)
        // Quando a intent receber a String "classe", fara o seguinte:
        when(intent.getStringExtra("classe")){
            // Caso o valor da string "classe" seja "Adapter", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de músicas da tela principal a ela,
            // e o método para carregar os dados da música são chamados
            "Adapter" ->{
                filaMusica = ArrayList()
                filaMusica.addAll(MainActivity.ListaMusicaMain)
                // Chama o método para carregar os dados da música
                carregarMusica()
            }
            // Caso o valor da string "classe" seja "Main", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de músicas da tela principal a ela junto de um método
            // pré-existente para os ArrayLists chamado "shuffle", que randomiza a lista.
            // e o método para carregar os dados da música são chamados
            "Main" ->{
                filaMusica = ArrayList()
                filaMusica.addAll(MainActivity.ListaMusicaMain)
                filaMusica.shuffle()
                // Chama o método para carregar os dados da música
                carregarMusica()
            }
        }
    }

    // Método para o botão play e pause
    private fun tocarPausarMusica(){
        // Se estiver tocando, então pause a música
        if (tocando){
            // Troca o ícone para o ícone de play no player e na barra de notificação
            binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_play)
            musicaService!!.mostrarNotificacao(R.drawable.ic_baseline_play_notification_bar)
            // Muda o valor da variável tocando para false
            tocando = false
            // E o player pausa a musica
            musicaService!!.mPlayer!!.pause()
        // Caso contrário (se estiver pausada), toque a música
        }else{
            // Troca o ícone para o ícone de pause no player e na barra de notificação
            binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
            musicaService!!.mostrarNotificacao(R.drawable.ic_baseline_pause_notification_bar)
            // Muda o valor da variável tocando para true
            tocando = true
            // E o player toca a musica
            musicaService!!.mPlayer!!.start()
        }
    }

    // Método para os botões previous (anterior) e next (próximo)
    // com um valor boolean para indentificar qual operação deve ser feita
    private fun trocarMusica(proximo : Boolean){
        // Se proximo for "true", então vá para próxima música, carregue seus dados e toque-a
        if(proximo) {
            // Chama o método para mudar a posição da música com valor "true"
            mudarPosMusica(adicionar = true)
            carregarMusica()
            criarPlayer()
            // Se proximo for "false", então vá para música anterior, carregue seus dados e toque-a
        }else{
            // Chama o método para mudar a posição da música com valor "false"
            mudarPosMusica(adicionar = false)
            carregarMusica()
            criarPlayer()
        }
    }

    // Método quando o serviço se conectar ao player
    override fun onServiceConnected(name: ComponentName?, servico: IBinder?) {
        // Define o objeto appBinder como o AppBinder da classe Service
        val appBinder = servico as MusicaService.AppBinder
        // O Service chama o método "reproducaoAtual()"
        musicaService = appBinder.reproducaoAtual()
        // Cria o reprodutor e reproduz a música selecionada
        criarPlayer()
        // O Service chama o método para carregar a SeekBar com as devidas definições
        musicaService!!.carregarSeekBar()
    }

    // Método quando o serviço estiver desconectado ao Player
    override fun onServiceDisconnected(p0: ComponentName?) {
        // Quando desconectado, o serviço se torna nulo e a reprodução de músicas é encerrada
        musicaService = null
    }

    // Método para que, quando a música for completada
    override fun onCompletion(mPlayer: MediaPlayer?) {
        // Será chamado o método para mudar a posição da música, com valor true, ou seja, irá para próxima música
        mudarPosMusica(adicionar = true)
        // Também é chamado o método para requisição dos dados para reprodução da próxima música
        criarPlayer()
        // E por fim, carrega os dados de layout da música (título, artista, imagem, etc.)
        try {
            carregarMusica()
        } catch (e: Exception){
            return
        }
    }

    // Uma classe que pode chamar APIs do tipo startActivityForResult sem ter que gerenciar códigos de solicitação
    // e converter solicitação/resposta para uma intent.
    // Esse método serve para iniciar atividades dentro e fora do aplicativo recebendo um resultado, no caso, está sendo utilizado para
    // o equalizador das músicas do aplicativo, para assim, os dados que são gerados lá, como a predefinição de som escolhida pelo usuário,
    // seja recebida pelo aplicativo e a alteração do som ocorra.
    private var resultadoIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { resultado ->
        // Se o resultado for "OK", então não houve erros e crashes na intent, e ela pode seguir normalmente
        if (resultado.resultCode == Activity.RESULT_OK) {
            // getData(), pega os resultados da intent, a predefinição de som escolhida por exemplo
            resultado.data
            // retorna esses dados e os registra no resultado da activity.
            return@registerForActivityResult
        }
    }
}