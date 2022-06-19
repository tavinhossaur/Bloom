package com.example.bloom

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    // Declaração de um objeto/classe estática
    companion object{
       lateinit var listaMusicaTpl : ArrayList<Musica> // Lista de reprodução do player
       var tocando : Boolean = false // Variável para identificar se a música está tocando ou não
       var playerM : MediaPlayer? = null // Player das músicas
       var posMusica : Int = 0 // Posição das músicas
    }

    private lateinit var binding : ActivityPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarLayout()

     /*   binding.btnFecharTpl.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        } */

        // Ao clicar no botão play/pause
        binding.btnPpTpl.setOnClickListener {
            // Se estivar tocando, então pause a música
            if(tocando){
                pausarMusica()
            // Caso contrário, toque a música
            }else{
                tocarMusica()
            }
        }

        // Ao clicar no botão "previous", chama o método trocar música
        // com valor "false" para o Boolean "proximo"
        binding.btnAnte.setOnClickListener {trocarMusica(false)}
        // Ao clicar no botão "next", chama o método trocar música
        // com valor "true" para o Boolean "proximo"
        binding.btnProx.setOnClickListener {trocarMusica(true)}
    }

    // Método que carrega os dados da música e os coloca nas views do player
    private fun carregarMusica(){
        // Utilizando Glide, Procura na lista de músicas a posição da música em específico
        // e retorna sua imagem de álbum no lugar da ImageView da mesma
        Glide.with(this)
            // Carrega a posição da música e a uri da sua imagem
            .load(listaMusicaTpl[posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(binding.imgMusicaTpl)

        binding.tituloMusicaTpl.text = listaMusicaTpl[posMusica].titulo
        binding.artistaMusicaTpl.text = listaMusicaTpl[posMusica].artista
        binding.albumMusicaTpl.text = listaMusicaTpl[posMusica].album
        binding.fimTempoTpl.text = formatarDuracao(listaMusicaTpl[posMusica].duracao)
    }

    // Método que cria o player da música e faz ela reproduzir
    private fun criarPlayer(){
        try {
            // Se a variável do player for nula, então ela se torna o MediaPlayer
            if (playerM == null) playerM = MediaPlayer()
            // .reset = Reseta e o coloca em "idle" (parado e esperando próximo comando)
            playerM!!.reset()
            // .setDataSource = Coloca a player no estado "initialized" (música selecionada pronta)
            playerM!!.setDataSource(listaMusicaTpl[posMusica].caminho)
            // .prepare = Coloca em estado de "prepared" (esperando pra ser iniciado ou parado)
            playerM!!.prepare()
            // .start = Coloca em estado de "started" (inicia o método esperado (started, paused, stopped))
            playerM!!.start()
            // Por padrão, quando o usuário for jogado para a tela do player após ter clicado na música
            // ela vai tocar
            tocando = true
            // E o ícone do botão será o de pausa, já que está tocando
            binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
        } catch (e : Exception){
            return
        }
    }

    @SuppressLint("SetTextI18n")
    private fun iniciarLayout(){
        // Inicialização do binding
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPlayerBinding (activity_player.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)

        // Texto do cabeçalho do player, mostrando ao usuário a quantidade total de músicas dele
        binding.musicaAtualTotal.text = "Você tem ${MainActivity.ListaMusicaMain.size} músicas."

        // Para reprodução das músicas
        // Recebe os dados enviados ao ser enviado a tela pela intent
        posMusica = intent.getIntExtra("posMusica", 0)
        // Quando a intent receber os dados da classe MusicaAdapter, a lista de reprodução do player será um ArrayList
        // e então, será adicionado todas as músicas da lista de músicas da tela principal a ela,
        // e os métodos para iniciar o player são chamados
        when(intent.getStringExtra("classeAdapter")){
            "MusicaAdapter" -> {
                listaMusicaTpl = ArrayList()
                listaMusicaTpl.addAll(MainActivity.ListaMusicaMain)
                // Chama o método para carregar os dados da música
                carregarMusica()
                // Chama o método para criar o player da música
                criarPlayer()
            }
        }
    }

    // Método para o botão play
    private fun tocarMusica(){
        // Troca o ícone para o ícone de pausa e muda a cor para a padrão
        binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
        // Muda o valor da variável tocando para true
        tocando = true
        // E o player inicia a musica
        playerM!!.start()
    }

    // Método para o botão pause
    private fun pausarMusica(){
        // Troca o ícone para o ícone de play e muda a cor para a padrão
        binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_play)
        // Muda o valor da variável tocando para false
        tocando = false
        // E o player pausa a musica
        playerM!!.pause()
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

    // Método para alterar a posição da música de forma correta evitando crashes e bugs
    private fun mudarPosMusica(adicionar : Boolean){
        // Se estiver adicionando, e a posição da música for igual o tamanho da lista -1,
        // então a posição da música deverá ser 0, caso contrário apenas vá para próxima música
        if (adicionar){
            if (listaMusicaTpl.size - 1 == posMusica){
                posMusica = 0
            }else{ ++posMusica }
        // Caso contrário se a posição da música for igual a 0, então a posição da música
        // deverá ser o tamanho da lista -1, caso contrário apenas vá para a música anterior
        }else{
            if (0 == posMusica){
                posMusica = listaMusicaTpl.size - 1
            }else{ --posMusica }
        }
    }
}