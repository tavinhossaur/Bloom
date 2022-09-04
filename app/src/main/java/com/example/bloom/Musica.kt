package com.example.bloom

import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

// Dados da música do ArrayList de dados da mesma
data class Musica(
    val id:String,
    val titulo:String,
    val artista:String,
    val album:String,
    val duracao:Long = 0,
    val imagemUri:String,
    val caminho: String)

class Playlist {
    lateinit var nome : String
    lateinit var playlist : ArrayList<Musica>
    lateinit var criador : String
}

class ModeloPlaylist{
    var modelo : ArrayList<Playlist> = ArrayList()
}

// Método que formata a duração em milisegundos das músicas para o tempo comum (minutos : segundos)
fun formatarDuracao(duracao: Long) : String{
    val minutos = TimeUnit.MINUTES.convert(duracao, TimeUnit.MILLISECONDS)
    val segundos = (TimeUnit.SECONDS.convert(duracao, TimeUnit.MILLISECONDS)
            - minutos * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutos, segundos)
}

// Metódo para encerrar o aplicativo
fun encerrarProcesso(){
    if (!PlayerActivity.tocando && PlayerActivity.musicaService!!.mPlayer != null){
        PlayerActivity.musicaService!!.audioManager.abandonAudioFocus { PlayerActivity.musicaService }
        PlayerActivity.musicaService!!.stopForeground(true)
        PlayerActivity.musicaService!!.mPlayer!!.release()
        PlayerActivity.musicaService = null
        exitProcess(1)
    }
}

// Método para checar se a música esta favoritada ou não
fun checarFavoritos(id: String) : Int{
    PlayerActivity.favoritado = false
    FavoritosActivity.listaFavoritos.forEachIndexed{ index, musica ->
        if (id == musica.id){
            PlayerActivity.favoritado = true
            return index
        }
    }
    return -1
}

// Metódo para sincronizar os botões do player na barra de notificação
fun setBtnsNotify(){
    when{
        PlayerActivity.favoritado && PlayerActivity.tocando -> PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_24)
        !PlayerActivity.favoritado && PlayerActivity.tocando -> PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_pause_notify_24, R.drawable.ic_round_favorite_border_24)
        !PlayerActivity.tocando && PlayerActivity.favoritado -> PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_24)
        !PlayerActivity.favoritado && !PlayerActivity.tocando -> PlayerActivity.musicaService!!.mostrarNotificacao(R.drawable.ic_round_play_arrow_notify_24, R.drawable.ic_round_favorite_border_24)
    }
}

// Método que retorna a imagem da música em array de bytes para colocar na barra de notificação
fun retornarImgMusica(caminho: String): ByteArray?{
    val procurador = MediaMetadataRetriever()
    // Caminho que será procurado a música
    procurador.setDataSource(caminho)
    // Retorna a imagem do caminho passado em um array de bytes
    return procurador.embeddedPicture
}

// Método que checa se uma música que foi excluída está presente em alguma playlist, e apaga ela
// esse código é necessário pois o SharedPreferences mantém os dados da música mesmo ela tendo sido excluída.
fun checarMusicasApagadas(lista: ArrayList<Musica>): ArrayList<Musica>{
    lista.forEachIndexed { index, musica ->
        val file = File(musica.caminho)
        if(!file.exists())
            lista.removeAt(index)
    }
    return lista
}

// Método para alterar a posição da música e o modo de reprodução de forma correta evitando crashes e bugs
// O método está sendo criado no Musica.kt como método publico para que seja utilizado
// pela tela do player e do controle da barra de notificação
fun mudarPosMusica(adicionar : Boolean){
    // Se não estiver repetindo então execute o código abaixo
    if(!PlayerActivity.repetindo) {
        // Se estiver adicionando, e a posição da música for igual o tamanho da lista -1,
        // então a posição da música deverá ser 0, caso contrário apenas vá para próxima música
        if (adicionar) {
            if (PlayerActivity.filaMusica.size - 1 == PlayerActivity.posMusica) {
                PlayerActivity.posMusica = 0
            }else{
                ++PlayerActivity.posMusica
            }
            // Caso contrário se a posição da música for igual a 0, então a posição da música
            // deverá ser o tamanho da lista -1, caso contrário apenas vá para a música anterior
        } else {
            if (0 == PlayerActivity.posMusica) {
                PlayerActivity.posMusica = PlayerActivity.filaMusica.size - 1
            } else {
                --PlayerActivity.posMusica
            }
        }
    } // Se estiver repetindo, então não execute o código para ir para próxima música ou voltar para anterior
}
