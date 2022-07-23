package com.example.bloom

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.bloom.PlayerActivity.Companion.favoritado
import com.example.bloom.databinding.FragmentMiniPlayerBinding

// A classe do miniplayer é um Fragment, uma porção ou parte de uma interface de usuário
class MiniPlayerFragment : Fragment(){

    // Declaração de um objeto/classe estática
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentMiniPlayerBinding // binding é a variável do ViewBinding para ligar as views ao código
    }

    // Criação da view do miniplayer
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewMiniPlayer = inflater.inflate(R.layout.fragment_mini_player, container, false)
        // Inicialização do binding
        binding = FragmentMiniPlayerBinding.bind(viewMiniPlayer)
        binding.root.visibility = View.INVISIBLE

        binding.root.setOnClickListener {
            val miniPlayerIntent = Intent(requireContext(), PlayerActivity::class.java)
            // Quando o usuário é levado a tela do player, também é enviado os dados de posição da música (Int)
            miniPlayerIntent.putExtra("indicador", PlayerActivity.posMusica)
            // Quando o usuário é levado a tela do player, também é enviado os dados da classe do miniplayer (String)
            miniPlayerIntent.putExtra("classe", "MiniPlayer")
            ContextCompat.startActivity(requireContext(), miniPlayerIntent, null)
        }

        // FUNÇÕES DE CONTROLE DO MINIPLAYER
        // Ao clicar no botão de favorito, favorita a música atual do player
        binding.tituloMusicaMp.setOnClickListener {
            PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
            // Se a música já estiver favoritada
            if (favoritado){
                // Então defina a variável favoritado para false
                favoritado = false
                // Mude o ícone para o coração vazio de desfavoritado
                binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_favorite_border_24,0)
                setBtnsNotify()
                // E remova a música da lista de favoritos utilizando o indicador favIndex
                FavoritosActivity.listaFavoritos.removeAt(PlayerActivity.favIndex)
                // Caso contrário (música desfavoritada)
            }else{
                // Então defina a variável favoritado para true
                favoritado = true
                // Mude o ícone para o coração cheio de favoritado
                binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_favorite_24,0)
                setBtnsNotify()
                // E adicione a música atual a lista de favoritos
                FavoritosActivity.listaFavoritos.add(PlayerActivity.filaMusica[PlayerActivity.posMusica])
            }
        }

        // Ao clicar no botão play/pause, chama o método para tocar ou pausar a música
        binding.btnPpMp.setOnClickListener {
            // Se estiver tocando, então pause e torne possível a exclusão da barra de notificação
            if(PlayerActivity.tocando){
                pausar()
                PlayerActivity.musicaService!!.stopForeground(false)
                // Caso contrário (Não estiver tocando), então toque a música
            }else{
                tocar()
            }
        }

        // Ao clicar no botão "previous", chama o método trocar música
        // com valor "false" para o Boolean "proximo"
        binding.btnAnteMp.setOnClickListener {
            mudarPosMusica(adicionar = false)
            PlayerActivity.musicaService!!.criarPlayer()
            carregarMusica()
            // Muda o ícone da barra de notificação
            setBtnsNotify()
            tocar()
        }

        // Ao clicar no botão "next", chama o método trocar música
        // com valor "true" para o Boolean "proximo"
        binding.btnProxMp.setOnClickListener {
            mudarPosMusica(adicionar = true)
            PlayerActivity.musicaService!!.criarPlayer()
            carregarMusica()
            // Muda o ícone da barra de notificação
            setBtnsNotify()
            tocar()
        }
        return viewMiniPlayer
    }

    // Método para mostrar o miniplayer
    override fun onResume() {
        super.onResume()
        // Se o serviço da música não estiver nulo
        if (PlayerActivity.musicaService != null){
            // Mostre o miniplayer
            binding.root.visibility = View.VISIBLE
            // Seleciona o título da música para título se mover
            binding.tituloMusicaMp.isSelected = true
            // e carregue os dados da música nele
            carregarMusica()

            // Se estiver tocando
            if (PlayerActivity.tocando){
                // Ícone de pausa
                binding.btnPpMp.setImageResource(R.drawable.ic_baseline_pause)
                // Caso contrário (se não estiver tocando)
            }else{
                // Ícone de play
                binding.btnPpMp.setImageResource(R.drawable.ic_baseline_play)
            }
        }
    }

    // Método para carregar os dados da música
    private fun carregarMusica(){
        PlayerActivity.favIndex = checarFavoritos(PlayerActivity.filaMusica[PlayerActivity.posMusica].id)
        Glide.with(requireContext())
            // Carrega a posição da música e a uri da sua imagem
            .load(PlayerActivity.filaMusica[PlayerActivity.posMusica].imagemUri)
            // Faz a aplicação da imagem com um placeholder caso a música não tenha nenhuma imagem ou ela ainda não tenha sido carregada
            .apply(RequestOptions().placeholder(R.drawable.bloom_lotus_icon_grey).centerCrop())
            // Alvo da aplicação da imagem
            .into(binding.imgMusicaMp)

        // Carrega os dados corretos para a música atual sendo reproduzida
        binding.tituloMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].titulo
        binding.artistaMusicaMp.text = PlayerActivity.filaMusica[PlayerActivity.posMusica].artista

        if (favoritado) {
            binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_favorite_24,0)
        } else{
            binding.tituloMusicaMp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_favorite_border_24,0)
        }
    }

    // Método para tocar a música pela barra de notificação
    private fun tocar(){
        // Toca a música e muda o ícone do botão na barra e no player
        PlayerActivity.tocando = true
        PlayerActivity.musicaService!!.mPlayer!!.start()
        setBtnsNotify()
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_pause)
        binding.btnPpMp.setImageResource(R.drawable.ic_baseline_pause)
    }

    // Método para pausar a música pela barra de notificação
    private fun pausar(){
        // Pausa a música e muda o ícone do botão na barra e no player
        PlayerActivity.tocando = false
        PlayerActivity.musicaService!!.mPlayer!!.pause()
        setBtnsNotify()
        PlayerActivity.binding.btnPpTpl.setImageResource(R.drawable.ic_baseline_play)
        binding.btnPpMp.setImageResource(R.drawable.ic_baseline_play)
    }

}