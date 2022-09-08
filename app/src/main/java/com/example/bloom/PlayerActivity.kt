package com.example.bloom

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.bloom.databinding.ActivityPlayerBinding
import com.maxkeppeler.sheets.core.IconButton
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputRadioButtons
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import java.io.File

// Classe do Player, com a implementação do ServiceConnection que monitora a conexão com o serviço
class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private lateinit var musicaAdapter : MusicaAdapter // Variável que leva a classe MusicAdapter

    // Declaração de objetos/classes estáticas
    companion object{
        var filaMusica : ArrayList<Musica> = ArrayList() // Fila de reprodução das músicas
        var posMusica : Int = 0                          // Posição da música, valor padrão de 0
        var tocando : Boolean = false                    // Variável para definir se a música está tocando ou não, por padrão: "false"
        var musicaService : MusicaService? = null        // Serviço da música, por padrão fica como null
        var musicaAtual : String = ""                    // Variável que recebe o id da música atual tocando
        var repetindo : Boolean = false                  // Variável para definir se a música está repetindo ou não, por padrão: "false"
        var favoritado = false                           // Variável para definir se a música está favoritada ou não
        var favIndex : Int = -1                          // Variável indicadora da música favoritada
        var telaCheia : Boolean = false                  // Variável para definir se o player está em tela cheia ou não
        // var randomizando : Boolean = false

        // Variáveis para indentificar qual opção do timer o usuário selecionou
        var min15 : Boolean = false
        var min30 : Boolean = false
        var min60 : Boolean = false

        @SuppressLint("StaticFieldLeak")
        lateinit var binding : ActivityPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código
    }

    // Método chamado quando o aplicativo é iniciado
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        temaPlayer()
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlayerBinding (activity_player.xml)
        setContentView(binding.root)

        iniciarLayout()

        // FUNÇÕES DA ACTIVITY (TELA)
        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnFecharTpl.setOnClickListener {finish()}
        // Texto do cabeçalho do player, mostrando ao usuário a quantidade total de músicas dele
        binding.musicaAtualTotal.text = "Você tem ${MainActivity.listaMusicaMain.size} músicas no total."
        // Ao clicar no botão de opções extras
        binding.btnExtraTpl.setOnClickListener {
            // Cria o popup menu
            val contexto: Context = ContextThemeWrapper(this, R.style.PopupMenuStyle)
            val popup = PopupMenu(contexto, binding.btnExtraTpl, Gravity.CENTER)
            popup.setForceShowIcon(true)
            // Infla o menu do card
            popup.inflate(R.menu.player_menu)
            // Adicionando o listener das opções do menu
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    // Detalhes da música
                    R.id.detalhes_musica -> {
                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                        val detalhesSheet = InfoSheet().build(this) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Mensagem do AlertDialog
                            content("Título: ${filaMusica[posMusica].titulo}" +
                                    "\nArtista: ${filaMusica[posMusica].artista}" +
                                    "\nAlbum: ${filaMusica[posMusica].album}" +
                                    "\nDuração ${formatarDuracao(filaMusica[posMusica].duracao)}" +
                                    "\n\nDiretório: ${filaMusica[posMusica].caminho}")
                            // Esconde os ambos os botões
                            displayButtons(false)
                        }
                        // Mostra o AlertDialog
                        detalhesSheet.show()
                    }
                    // Excluir a música
                    R.id.delete_musica -> {
                        // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                        val permSheet = InfoSheet().build(this) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Título do AlertDialog
                            title("Deseja mesmo excluir a música?")
                            // Cor do título
                            titleColorRes(R.color.purple1)
                            // Mensagem do AlertDialog
                            content("Excluir a música \"${filaMusica[posMusica].titulo}\" de ${filaMusica[posMusica].artista}?\n\nAtenção: se a música que você estiver tentando excluir não for apagada, você precisará apaga-la manualmente no armazenamento do dispositivo.")
                            // Botão positivo que exclui a música em questão
                            positiveButtonColorRes(R.color.purple1)
                            onPositive("Sim, excluir") {
                                // Criando o objeto "musica" com base nos dados da música que foi selecionada
                                val musica = Musica(filaMusica[posMusica].id, filaMusica[posMusica].titulo, filaMusica[posMusica].artista, filaMusica[posMusica].album, filaMusica[posMusica].duracao, filaMusica[posMusica].imagemUri, filaMusica[posMusica].caminho)
                                // Criando o objeto "arquivo" que leva o objeto "musica" e o seu caminho (url do arquivo no armazenamento do dispositivo)
                                val arquivo = File(filaMusica[posMusica].caminho)
                                // Exclui a música do armazenamento do dispositivo
                                arquivo.delete()
                                // Remove a música da lista
                                filaMusica.remove(musica)
                                // Atualiza a tela (lista da fila de reprodução)
                                musicaAdapter.atualizarLista(filaMusica)
                                // Carrega o player novamente com a próxima música automaticamente pois a anterior foi removida
                                carregarMusica()
                                criarPlayer()
                            }
                            // Botão negativo que apenas fecha o diálogo
                            negativeButtonColorRes(R.color.grey3)
                        }
                        // Mostra o AlertDialog
                        permSheet.show()
                    }
                }
                true
            }
            // Mostra o menu popup
            popup.show()
        }

        // FUNÇÕES EXTRAS
        // Ao clicar no botão de favorito, favorita a música atual do player
        binding.btnFavTpl.setOnClickListener {
            favIndex = checarFavoritos(filaMusica[posMusica].id)
            // Se a música já estiver favoritada
            if (favoritado){
                // Então defina a variável favoritado para false
                favoritado = false
                // Mude o ícone para o coração vazio de desfavoritado
                binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_border_24)
                setBtnsNotify()
                // E remova a música da lista de favoritos utilizando o indicador favIndex
                FavoritosActivity.listaFavoritos.removeAt(favIndex)
            // Caso contrário (música desfavoritada)
            }else{
                // Então defina a variável favoritado para true
                favoritado = true
                // Mude o ícone para o coração cheio de favoritado
                binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_24)
                setBtnsNotify()
                // E adicione a música atual a lista de favoritos
                FavoritosActivity.listaFavoritos.add(filaMusica[posMusica])
            }
        }

        // Ao clicar no botão de compartilhar música
        binding.btnCompart.setOnClickListener {
            // Cria a intent para o compartilhamento
            val compartIntent = Intent()
            // Define a ação que será feita na intent (ACTION_SEND), ("enviar")
            compartIntent.action = Intent.ACTION_SEND
            // Define o tipo do conteúdo que será enviado, no caso, audio
            compartIntent.type = "audio/*"
            // Junto da intent, com putExtra, estão indo os dados da música com base no caminho da música atual do player.
            compartIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filaMusica[posMusica].caminho))
            // Inicia a intent chamando o método "createChooser" que mostra um BottomSheetDialog com opções de compartilhamento (Bluetooth, Whatsapp, Drive)
            // E um título que aparece no BottomSheetDialog
            startActivity(Intent.createChooser(compartIntent, "Selecione como você vai compartilhar a música"))
        }

        // Quando clicado no botão de timer, uma BottomSheet bar aparece para o usuário
        // selecionar entre 3 opções de tempos em que ele quer que a música encerre.
        binding.btnTimer.setOnClickListener {
            // Tempo leva todos os booleans das opções de tempo
            val tempo = min15 || min30 || min60
            // Se caso nenhum deles forem "true", então chame o método para o BottomSheetBar
            if (!tempo){
                timerSheet()
                // Caso contrário, diga que o timer já está ativado a partir de um novo BottomSheetBar
            }else{
                // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
                binding.btnTimer.isEnabled = false
                val permSheet = InfoSheet().build(this) {
                    // Estilo do sheet (BottomSheet)
                    style(SheetStyle.BOTTOM_SHEET)
                    // Altera o botão de fechar o dialogo
                    closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
                    // Cor do título
                    titleColorRes(R.color.purple1)
                    // Título do BottomSheetDialog
                    title("O timer já está ativado!")
                    // Mensagem do BottomSheetDialog
                    content("Deseja parar ou alterar o timer?")
                    // Torna o objeto clicável novamente quando o diálogo for fechado
                    onClose { binding.btnTimer.isEnabled = true }
                    // Botão positivo que redireciona o usuário para a tela de configurações e detalhes do aplicativo
                    positiveButtonColorRes(R.color.purple1)
                    // Se o usuário quiser parar o timer e alterará-lo
                    onPositive("Parar e alterar") {
                        // Define todos os booleans de tempo para falso
                        min15 = false
                        min30 = false
                        min60 = false
                        // Altera a cor do botão de timer para a cor padrão
                        binding.btnTimer.setImageResource(R.drawable.ic_baseline_timer_24)
                        // E abre o timerSheet novamente
                        timerSheet()
                    }

                    // Botão negativo que encerra o timer
                    negativeButtonColorRes(R.color.grey3)
                    // Se o usuário quiser apenas parar o timer
                    onNegative("Parar") {
                        // Define todos os booleans de tempo para falso
                        min15 = false
                        min30 = false
                        min60 = false
                        // Altera a cor do botão de timer para a cor padrão
                        binding.btnTimer.setImageResource(R.drawable.ic_baseline_timer_24)
                        // Toast indicando que o timer foi encerrado
                        Toast.makeText(this@PlayerActivity, "Timer encerrado", Toast.LENGTH_SHORT).show()
                    }
                }
                // Mostra o BottomSheetDialog
                permSheet.show()
            }
        }

        // Quando clicado no botão de equalizador, ele tentará levar o usuário
        // para o painel de controle de equalizador de som padrão do Android.
        binding.btnEqual.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            binding.btnEqual.isEnabled = false
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
                // Torna o objeto clicável novamente
                binding.btnEqual.isEnabled = true
            }catch (e: Exception){
                // Se por algum motivo, o smartphone não for capaz de acessar o equalizador, o toast abaixo é apresentado
                Toast.makeText(this, "Equalizador não suportado", Toast.LENGTH_SHORT).show()
                // Torna o objeto clicável novamente
                binding.btnEqual.isEnabled = true
            }
        }

        // Para deixar o player em tela cheia
        binding.btnFullScreen.setOnClickListener {
            // Chama o método "telaCheia()"
            telaCheia()
        }

        // Para mostrar a fila de reprodução atual
        binding.btnFila.setOnClickListener {
            // Chama o método "mostrarFilaAtual()"
            mostrarFilaAtual()
        }

        // Quando clicar no card fecha ele e mostra as views escondidas
        binding.btnFecharCard.setOnClickListener {
            binding.btnEspecial.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.controlesBtnTpl.visibility = View.VISIBLE
            binding.cardFilaRv.visibility = View.GONE
        }

        // Se o usuário clicar na tela
        binding.root.setOnClickListener {
            // Também chama o método "telaCheia()"
            // Caso telaCheia for true ele desliga a tela cheia e vice versa
            telaCheia()
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
                    setBtnsNotify()
                }
            }
            // Quando o usuário tocar no indicador ou na SeekBar
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            // Quando o usuário soltar o indicador ou a SeekBar
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    // Método para deixar a tela do player com um tema personalizado
    private fun temaPlayer(){
        // Define o tema como sem actionBar
        application.setTheme(R.style.Theme_BloomPlayer)
        setTheme(R.style.Theme_BloomPlayer)
    }

    // Método para definir a reprodução normal da lista de músicas
    private fun reproducaoNormal(){
        // Se a reprodução for normal, então a música não está repetindo
        repetindo = false
        //randomizando = false
        // O ícone do botão muda para o ícone de reprodução normal
        binding.btnRepetir.setImageResource(R.drawable.ic_round_repeat_24)
    }

    // Método para definir a repetição da música atual
    private fun reproducaoRepetir() {
        // Se a reprodução não for normal, então a música está repetindo
        repetindo = true
        //randomizando = false
        // O ícone do botão muda para o ícone de reprodução de uma única música
        binding.btnRepetir.setImageResource(R.drawable.ic_round_repeat_one_24)
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
            binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
            // Chama o método para mostrar a barra de notificação da música com os devidos botões
            setBtnsNotify()
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
            // Retorna o id da música atual para a variável
            musicaAtual = filaMusica[posMusica].id
        } catch (e: Exception) {
            return
        }
    }

    // Método que carrega os dados da música e os coloca nas views do player
    private fun carregarMusica(){
        // Passa o método e o id da música atual para checar se ela está favoritada
        favIndex = checarFavoritos(filaMusica[posMusica].id)
        // Utilizando Glide, Procura na fila de músicas a posição da música em específico
        // e retorna sua imagem de álbum no lugar da ImageView da mesma
        Glide.with(applicationContext)
            // Carrega a posição da música e a uri da sua imagem
            .load(filaMusica[posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(binding.imgMusicaTpl)

        // Objeto que leva todas as transformações da imagem do background do player
        val multiTransform = MultiTransformation(
            // Efeito embaçado na imagem
            BlurTransformation(40, 5),
            // Filtro de cor escuro na imagem
            ColorFilterTransformation(Color.argb(70, 40, 40, 40))
        )

        // Utilizando Glide, está sendo retornado a imagem da música e colocado como background do Player
        // O intuito desse código é apenas deixar a tela do player mais bonita e "profissional"
        Glide.with(applicationContext)
            // Carrega a posição da música e a uri da sua imagem
            .load(filaMusica[posMusica].imagemUri)
            // Faz a aplicação da imagem com as transformações e um placeholder caso a música não tenha nenhuma imagem
            // ou ela ainda não tenha sido carregada
            .apply(RequestOptions.bitmapTransform(multiTransform))
            // Alvo da aplicação da imagem, como o alvo da aplicação não é um componente simples, e sim, o background de um LinearLayout
            // é necessário utilizar o método CustomTraget do Glide.
            .into(object : CustomTarget<Drawable?>() {
                // Quando retornar a imagem
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                    // Aplica a imagem da música ao background do root da activity, ou seja, o LinearLayout que engloba todas as views.
                    binding.root.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    return
                }
            })

        // Carrega os dados corretos para a música atual sendo reproduzida
        binding.tituloMusicaTpl.text = filaMusica[posMusica].titulo
        binding.artistaMusicaTpl.text = filaMusica[posMusica].artista
        binding.albumMusicaTpl.text = filaMusica[posMusica].album

        // Se estiver com o timer ligado, o botão permanece "ligado"
        if(min15 || min30 || min60) binding.btnTimer.setImageResource(R.drawable.ic_round_timer_24)

        if (favoritado) {
            binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_24)
        } else{
            binding.btnFavTpl.setImageResource(R.drawable.ic_round_favorite_border_24)
        }

        if (repetindo) binding.btnRepetir.setImageResource(R.drawable.ic_round_repeat_one_24)
    }

    @SuppressLint("SetTextI18n")
    private fun iniciarLayout(){
        // Marca como selecionado a caixa de texto do nome da música, para fazer a animação de correr o texto
        binding.tituloMusicaTpl.isSelected = true

        // Usuário impedido de clicar na root inicialmente pois o player não estará em tela cheia
        binding.root.isEnabled = false

        // Para otimização do RecyclerView, o seu tamanho é fixo,
        // mesmo quando itens são adicionados ou removidos.
        binding.filaRv.setHasFixedSize(true)
        // Para otimização do RecyclerView, 13 itens fora da tela serão "segurados"
        // para depois potencialmente usá-los de novo (Reciclagem de itens).
        binding.filaRv.setItemViewCacheSize(13)
        // O LayoutManager é responsável por medir e posicionar as visualizações dos itens dentro de um RecyclerView,
        // bem como determinar a política de quando reciclar visualizações de itens que não são mais visíveis para o usuário.
        binding.filaRv.layoutManager = LinearLayoutManager(this@PlayerActivity)
        // Evita que o usuário consiga clicar em dois itens ao mesmo tempo
        binding.filaRv.isMotionEventSplittingEnabled = false

        // Para reprodução das músicas
        // Recebe os dados enviados ao ser enviado a tela pela intent
        // Recebe o valor Int da posição da música, o valor padrão é 0
        posMusica = intent.getIntExtra("indicador", 0)
        // Quando a intent receber a String "classe", fara o seguinte:
        when(intent.getStringExtra("classe")){
            // Caso o valor da string "classe" seja "Pesquisa", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de músicas da pesquisa feita a ela,
            // e o método para carregar os dados da música são chamados
            "Pesquisa" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                // Conecta o serviço ao player e automaticamente cria o serviço enquanto a conexão existir,
                // essa conexão, define uma dependência do player ao serviço
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(MainActivity.listaMusicaPesquisa)
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "Adapter", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de músicas da tela principal a ela,
            // e o método para carregar os dados da música são chamados
            "Adapter" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(MainActivity.listaMusicaMain)
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "Main", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de músicas da tela principal a ela junto de um método
            // pré-existente para os ArrayLists chamado "shuffle", que randomiza a lista.
            // e o método para carregar os dados da música são chamados
            "Main" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(MainActivity.listaMusicaMain)
                filaMusica.shuffle()
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "Favoritos", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de favoritos a ela,
            // e o método para carregar os dados da música são chamados
            "Favoritos" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                // Conecta o serviço ao player e automaticamente cria o serviço enquanto a conexão existir,
                // essa conexão, define uma dependência do player ao serviço
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(FavoritosActivity.listaFavoritos)
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "FavoritosShuffle", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista de favoritos a ela junto de um método
            // pré-existente para os ArrayLists chamado "shuffle", que randomiza a lista.
            // e o método para carregar os dados da música são chamados
            "FavoritosRandom" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(FavoritosActivity.listaFavoritos)
                filaMusica.shuffle()
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "ConteudoPlaylist", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista da playlist específica a ela,
            // e o método para carregar os dados da música são chamados
            "ConteudoPlaylist" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist)
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "ConteudoPlaylistRandom", a fila de reprodução do player se torna um ArrayList
            // e então, será adicionado todas as músicas da lista da playlist específica a ela junto de um método
            // pré-existente para os ArrayLists chamado "shuffle", que randomiza a lista.
            // e o método para carregar os dados da música são chamados
            "ConteudoPlaylistRandom" ->{
                // Cria a intent com a classe MusicService
                val bindIntent = Intent(this, MusicaService::class.java)
                bindService(bindIntent, this, BIND_AUTO_CREATE)
                startService(bindIntent)
                filaMusica = ArrayList()
                filaMusica.addAll(PlaylistsActivity.playlists.modelo[ConteudoPlaylistActivity.posPlaylistAtual].playlist)
                filaMusica.shuffle()
                carregarMusica()

                // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
                // ao RecyclerView por meio do mesmo Adapter
                musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
                // Setando o Adapter para este RecyclerView
                binding.filaRv.adapter = musicaAdapter
            }
            // Caso o valor da string "classe" seja "MiniPlayer", apenas carregue os dados da música,
            // assim nenhuma alteração no media player é feito e o usuário é jogado para tela do player.
            "MiniPlayer" ->{
                reproducaoAtual()
            }
        }
    }

    private fun reproducaoAtual(){
        carregarMusica()
        binding.decTempoSeekBar.text =
            formatarDuracao(musicaService!!.mPlayer!!.currentPosition.toLong())
        binding.fimTempoSeekBar.text =
                formatarDuracao(musicaService!!.mPlayer!!.duration.toLong())
        binding.seekBarMusica.progress = musicaService!!.mPlayer!!.currentPosition
        binding.seekBarMusica.max = musicaService!!.mPlayer!!.duration
        if (tocando) {
            binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
        } else {
            binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
        }
        // Passando ao adapter o contexto (tela) e a lista de músicas que será adicionada
        // ao RecyclerView por meio do mesmo Adapter
        musicaAdapter = MusicaAdapter(this@PlayerActivity, filaMusica, filaReproducao = true)
        // Setando o Adapter para este RecyclerView
        binding.filaRv.adapter = musicaAdapter
    }

    // Método para o botão play e pause
    private fun tocarPausarMusica(){
        // Se estiver tocando, então pause a música
        checarFavoritos(filaMusica[posMusica].id)
        if (tocando){
            // Troca o ícone para o ícone de play no player e na barra de notificação
            binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
            if (favoritado){
                musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_24)
            }else{
                musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_border_24)
            }
            // Muda o valor da variável tocando para false
            tocando = false
            // E o player pausa a musica
            musicaService!!.stopForeground(false)
            musicaService!!.mPlayer!!.pause()
            // Caso contrário (se estiver pausada), toque a música
        }else{
            // Troca o ícone para o ícone de pause no player e na barra de notificação
            binding.btnPpTpl.setImageResource(R.drawable.ic_round_pause_circle_24)
            if (favoritado){
                musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_24)
            }else{
                musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_border_24)
            }
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

    // Método para poder chamar o BottomSheetDialog ao botão do timer ser clicado
    private fun timerSheet(){
        // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
        binding.btnTimer.isEnabled = false
        // Cria e mostra a InputSheet
        InputSheet().show(this@PlayerActivity) {
            // Estilo do sheet (BottomSheet)
            style(SheetStyle.BOTTOM_SHEET)
            // Altera o botão de fechar o dialogo
            closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
            // Título do BottomSheetDialog
            title("Timer da música")
            // Cor do título
            titleColorRes(R.color.purple1)
            // Conteúdo da sheet (Radio Buttons)
            with(InputRadioButtons("timer_opt") {
                // Marca como um campo obrigatório ou não para liberar o botão "ok"
                required(true)
                // Título das opções
                label("Daqui quanto tempo você quer que a música pare?")
                // Opções disponíveis numa lista de arrays (MutableList)
                options(mutableListOf("15 minutos", "30 minutos", "1 hora"))
            })
            // Cor do botão "confirmar"
            positiveButtonColorRes(R.color.purple1)
            // Torna o objeto clicável novamente quando o diálogo for fechado
            onClose { binding.btnTimer.isEnabled = true }
            // Botão confirmar do BottomSheet
            onPositive("Confirmar") { result ->
                // Altera a cor do botão do timer para uma cor que indique que ele está ligado
                binding.btnTimer.setImageResource(R.drawable.ic_round_timer_24)
                // Quando o resultado, convertido para String for igual as strings abaixo
                when(result.toString()){
                    "Bundle[{timer_opt=0}]" -> {
                        // Envia um toast dizendo que a música irá parar conforme o tempo escolhido
                        Toast.makeText(this@PlayerActivity, "A música irá parar daqui 15 minutos", Toast.LENGTH_SHORT).show()
                        // Define o boolean min15 como true
                        min15 = true
                        // Cria uma Thread a parte para funcionalidade do timer
                        // Ela fica em estado "sleep" no tempo definido abaixo em milisegundos
                        Thread{ Thread.sleep((15 * 60000).toLong())
                            // Quando a Thread termina o "sleep", ela executa o código abaixo
                            if (min15){
                                pausarTimer()
                                min15 = false
                            }
                        }.start()
                    }
                    "Bundle[{timer_opt=1}]" -> {
                        // Envia um toast dizendo que a música irá parar conforme o tempo escolhido
                        Toast.makeText(this@PlayerActivity, "A música irá parar daqui 30 minutos", Toast.LENGTH_SHORT).show()
                        // Define o boolean min15 como true
                        min30 = true
                        // Cria uma Thread a parte para funcionalidade do timer
                        // Ela fica em estado "sleep" no tempo definido abaixo em milisegundos
                        Thread{ Thread.sleep((30 * 60000).toLong())
                            // Quando a Thread termina o "sleep", ela executa o código abaixo
                            if (min30){
                                pausarTimer()
                                min30 = false
                            }
                        }.start()
                    }
                    "Bundle[{timer_opt=2}]" -> {
                        // Envia um toast dizendo que a música irá parar conforme o tempo escolhido
                        Toast.makeText(this@PlayerActivity, "A música irá parar daqui uma hora", Toast.LENGTH_SHORT).show()
                        // Define o boolean min15 como true
                        min60 = true
                        // Cria uma Thread a parte para funcionalidade do timer
                        // Ela fica em estado "sleep" no tempo definido abaixo em milisegundos
                        Thread{ Thread.sleep((60 * 60000).toLong())
                            // Quando a Thread termina o "sleep", ela executa o código abaixo
                            if (min60){
                                pausarTimer()
                                min60 = false
                            }
                        }.start()
                    }
                }
            }
            // Cor do botão negativo
            negativeButtonColorRes(R.color.grey3)
        }
    }

    private fun pausarTimer(){
        musicaService!!.stopForeground(false)
        musicaService!!.mPlayer!!.pause()
        // Troca o ícone para o ícone de play no player, no miniplaer e na barra de notificação
        binding.btnPpTpl.setImageResource(R.drawable.ic_round_play_circle_24)
        MiniPlayerFragment.binding.btnPpMp.setImageResource(R.drawable.ic_round_play_circle_24)
        if(favoritado){
            musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_24)
        }else{
            musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_border_24)
        }
        // Volta a cor padrão do botão timer
        binding.btnTimer.setImageResource(R.drawable.ic_baseline_timer_24)
        // E muda os valores das variáveis "min15" e "tocando" para false
        tocando = false
    }

    // Método para fazer a tela ficar em tela cheia
    private fun telaCheia(){
        // Se não estiver em tela cheia (false)
        if (!telaCheia){
            // Esconde os seguintes layouts da tela
            binding.cabecalhoPlayer.visibility = View.INVISIBLE
            binding.controlesBtnTpl.visibility = View.INVISIBLE
            binding.btnEspecial.visibility = View.INVISIBLE

            // Aplica a animação de translação vertical no bodyPlayer, levando 200px para baixo
            // em uma duração de 3000 milisegundos (3 segundos)
            ObjectAnimator.ofFloat(binding.bodyPlayer, "translationY", 200f).apply {
                duration = 3000
                start()
            }

            // Aplica a animação de translação vertical no bodyPlayer, levando 150px para cima
            // em uma duração de 3000 milisegundos (3 segundos)
            ObjectAnimator.ofFloat(binding.progressBar, "translationY", -150f).apply {
                duration = 3000
                start()
            }

            // Marca a variável tela cheia como true
            telaCheia = true
            // Atualiza a disponibilidade do root da activity (Enabled = true), o que
            // significa que agora ele pode ser clicado
            binding.root.isEnabled = true

            // Limpa as flags anteriores
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            // Adiciona uma nova flag para colocar a activity em tela cheia
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Caso contrário (Se já estiver em tela cheia (true))
        }else{
            // Mostra os seguintes layouts da tela
            binding.cabecalhoPlayer.visibility = View.VISIBLE
            binding.controlesBtnTpl.visibility = View.VISIBLE
            binding.btnEspecial.visibility = View.VISIBLE

            // Aplica a animação de translação vertical no bodyPlayer, levando para o lugar padrão
            // em uma duração de 3000 milisegundos (3 segundos)
            ObjectAnimator.ofFloat(binding.bodyPlayer, "translationY", 0f).apply {
                duration = 3000
                start()
            }

            // Aplica a animação de translação vertical no bodyPlayer, levando para o lugar padrão
            // em uma duração de 3000 milisegundos (3 segundos)
            ObjectAnimator.ofFloat(binding.progressBar, "translationY", 0f).apply {
                duration = 3000
                start()
            }

            // Marca a variável tela cheia como false
            telaCheia = false
            // Atualiza a disponibilidade do root da activity (Enabled = false), o que
            // significa que agora ele não pode ser clicado
            binding.root.isEnabled = false

            // Limpa as flags anteriores
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            // Adiciona uma nova flag para colocar a activity em tela cheia
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        }
    }

    // Método para mostrar o card com a fila de reprodução atual
    private fun mostrarFilaAtual(){
        binding.btnEspecial.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.controlesBtnTpl.visibility = View.GONE
        binding.cardFilaRv.visibility = View.VISIBLE
    }

    // Método quando o serviço se conectar ao player
    override fun onServiceConnected(name: ComponentName?, servico: IBinder?) {
        // Se o serviço da música estiver inicialmente nulo, assim não executará duas vezes o código abaixo
        // e quando o usuário selecionar outra nova música, ela não iniciará pausada
        if (musicaService == null){
            // Define o objeto appBinder como o AppBinder da classe Service
            val appBinder = servico as MusicaService.AppBinder
            // O Service chama o método "reproducaoAtual()"
            musicaService = appBinder.reproducaoAtual()
            // Retorna o serviço do sistema "AUDIO_SERVICE" como o administrador de áudio
            musicaService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Passa o método para requisitar o foco de áudio, passando como parâmetro a classe do listener (musicaService)
            // o tipo de streaming (STREAM_MUSIC), e a operação a ser feita (AUDIOFOCUS_GAIN).
            musicaService!!.audioManager.requestAudioFocus(musicaService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
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
        setBtnsNotify()
        // Também é chamado o método para requisição dos dados para reprodução da próxima música
        criarPlayer()
        // E por fim, carrega os dados de layout da música (título, artista, imagem, etc.)
        carregarMusica()

        // Seleciona o texto do título para fazê-lo se movimentar e mostrar o texto inteiro
        MiniPlayerFragment.binding.tituloMusicaMp.isSelected = true
        Glide.with(applicationContext)
            // Carrega a posição da música e a uri da sua imagem
            .load(filaMusica[posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(MiniPlayerFragment.binding.imgMusicaMp)

        // Carrega os dados corretos para a música atual sendo reproduzida
        MiniPlayerFragment.binding.tituloMusicaMp.text = filaMusica[posMusica].titulo
        MiniPlayerFragment.binding.artistaMusicaMp.text = filaMusica[posMusica].artista
        if (favoritado){
            MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_miniplayer_24)
        }else{
            MiniPlayerFragment.binding.btnFavMp.setImageResource(R.drawable.ic_round_favorite_border_miniplayer_24)
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