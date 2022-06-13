package com.example.bloom

import java.util.concurrent.TimeUnit

// Dados da música do ArrayList de dados da mesma
data class Musica(
    val id:String,
    val titulo:String,
    val artista:String,
    val album:String,
    val duracao:Long = 0,
    val caminho: String)

// Método que formata a duração em milisegundos das músicas para o tempo comum (minutos : segundos)
fun formatarDuracao(duracao: Long) : String{
    val minutos = TimeUnit.MINUTES.convert(duracao, TimeUnit.MILLISECONDS)
    val segundos = (TimeUnit.SECONDS.convert(duracao, TimeUnit.MILLISECONDS)
            - minutos * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutos, segundos)
}