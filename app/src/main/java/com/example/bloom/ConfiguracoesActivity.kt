package com.example.bloom

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bloom.databinding.ActivityConfiguracoesBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

// Classe da activity
class ConfiguracoesActivity : AppCompatActivity() {

    // binding é a variável do ViewBinding para ligar as views ao código
    private lateinit var binding: ActivityConfiguracoesBinding

    // Declaração de objetos/classes estáticas para poder utilizar
    companion object{
        var switch1 : Boolean = false
        var nomeUserNovo : String = ""
    }

    // Método chamado quando o aplicativo/activity é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityMainBinding (activity_main.xml)
        setContentView(binding.root)

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            binding.btnVoltarConfig.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.btnBugs.setColorFilter(ContextCompat.getColor(this, R.color.grey2), android.graphics.PorterDuff.Mode.SRC_IN)
            binding.tituloActivityConfig.setTextColor(ContextCompat.getColor(this, R.color.grey2))
            binding.btnMudarNome.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.btnFeedback.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.btnTerceiros.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
        }

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarConfig.setOnClickListener{
            // Muda a animação do botão ao ser clicado
            binding.btnVoltarConfig.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            finish()
        }

        binding.btnBugs.setOnClickListener{
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            binding.btnBugs.isEnabled = false
            // Muda a animação do botão ao ser clicado
            binding.btnBugs.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_popup_exit))
            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
            val bugsSheet = InfoSheet().build(this) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Título
                title("Bugs conhecidos")
                // Mensagem do AlertDialog
                content("Segue abaixo bugs conhecidos pelos desenvolvedores:" +
                        "\n\n● Ao pausar a música, mudar o tema do Android (claro ou escuro) e voltar para tela principal ou já estiver nela quando mudar, o aplicativo fecha." +
                        "\n● Ao favoritar ou desfavoritar a música, e mudar o tema do Android (claro ou escuro) a música não é salva nos favoritos." +
                        "\n\nOs bugs são analisados para serem resolvidos.\nCaso houver mais algum que você identificou e não está nesta lista, por favor, nos envie um feedback explicando o problema de preferência com seu e-mail, versão do Android e modelo do telefone.")
                // Esconde os ambos os botões
                displayButtons(false)
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { binding.btnBugs.isEnabled = true }
            }
            // Mostra o AlertDialog
            bugsSheet.show()
        }

        // Ao clicar no switch da configuração 1, passa o método para checar se o switch está ligado (true) ou desligado (false)
        binding.switchConfig.setOnCheckedChangeListener{ _, _ -> checarSwitch() }

        // Ao clicar no botão de mudar o nome do usuário
        binding.btnMudarNome.setOnClickListener {
            // SharedPreferences, para retornar o nome do usuário
            val nomeEditor = getSharedPreferences("NOME", MODE_PRIVATE)
            val nomeUser = nomeEditor.getString("nomeUser", PermissaoActivity.nomeUser)

            // Diálogo para o usuário inserir o seu nome
            InputSheet().show(this) {
                // Estilo do sheet (BottomSheet)
                style(SheetStyle.DIALOG)
                // Título do BottomSheetDialog
                title("Como devemos te chamar agora?")
                // Conteúdo da sheet (Edit Texts)
                with(InputEditText("nome") {
                    required(true)
                    drawable(R.drawable.ic_round_person_24)
                    // Se o valor do nome do usuário não for nulo
                    // Então mostra como valor padrão
                    if (nomeUser != null) {
                        defaultValue(nomeUser)
                    }
                    label("Insira seu novo nome ou apelido")
                })
                // Cor do botão "confirmar"
                positiveButtonColorRes(R.color.purple1)
                // Botão confirmar do BottomSheet
                onPositive("Confirmar") { result ->
                    // Retorna o valor string da input "nome_playlist"
                    nomeUserNovo = result.getString("nome").toString()

                    // Aplica o getSharedPreferences para salvar o nome do usuário
                    val editor = getSharedPreferences("NOME", MODE_PRIVATE).edit()
                    // E salva e aplica o valor da string
                    editor.putString("nomeUser", nomeUserNovo)
                    editor.apply()

                    // Mostra um toast confirmando a troca de nome
                    Toast.makeText(this@ConfiguracoesActivity, "Nome atualizado", Toast.LENGTH_SHORT).show()
                }
                // Cor do botão negativo
                negativeButtonColorRes(R.color.grey3)
            }
        }

        // Ao clicar no botão de feedback, envia o usuário para a tela de feedback
        binding.btnFeedback.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            binding.btnFeedback.isEnabled = false
            InputSheet().show(this) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Altera o botão de fechar o dialogo
                // Título do BottomSheetDialog
                title("Feedback")
                // Cor do título
                titleColorRes(R.color.purple1)
                // Conteúdo da sheet (Edit Texts)
                // EditText do assunto do email
                with(InputEditText("assunto_email") {
                    required(true)
                    drawable(R.drawable.ic_round_folder_24)
                    label("Insira o assunto do feedback")
                    hint("Bug, sugestão, etc...")
                })
                // EditText do conteúdo (feedback) do email
                with(InputEditText("feedback_email") {
                    required(true)
                    drawable(R.drawable.ic_round_person_24)
                    label("Feedback")
                    hint("Eu amei o aplicativo!...")
                })
                // EditText do remetente do email
                with(InputEditText("remetente_email") {
                    required(false)
                    drawable(R.drawable.ic_round_person_24)
                    hint("Insira seu nome ou email (opcional)")
                })
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { binding.btnFeedback.isEnabled = true }
                // Cor do botão "confirmar"
                positiveButtonColorRes(R.color.purple1)
                // Botão confirmar do BottomSheet
                onPositive("Enviar") { result ->
                    // Retorna o valor string da input "assunto_email"
                    val assunto = result.getString("assunto_email").toString()
                    // Retorna o valor string da input "remetente_email"
                    val remetente = result.getString("remetente_email").toString()
                    // Retorna o valor string da input "feedback_email" e junta ela ao remetente
                    val feedback = result.getString("feedback_email").toString() + "\n\nEnviado por: $remetente"

                    // Endereço de email para onde será enviado o feedback (email e senha do aplicativo)
                    val usuario = "bloomapp2022@outlook.com"
                    val senha = "BlooApp2022"

                    // Obejeto gerenciador de conexão, para poder controlar o serviço de conexão a internet do aplicativo
                    val gerenciadorConexao = this@ConfiguracoesActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                    // Se o dispositivo estiver conectado ou conectando a internet
                    if (gerenciadorConexao.activeNetworkInfo?.isConnectedOrConnecting == true){
                        // Então cria uma Thread a parte
                       Thread{
                           // E tenta o código abaixo
                           try {
                               // Criando o objeto propriedades
                               // Essa parte do código utiliza o protocolo de transferência de email
                               // Simple Mail Transfer Protocol (SMTP)
                               val propriedades = Properties()
                               // Para fazer a autenticação do usuário com o comando AUTH
                               propriedades["mail.smtp.auth"] = "true"
                               // starttls é um comando para transformar uma ligação não encriptada numa ligação encriptada
                               // sem a necessidade de recorrer a uma porta segura especifica, podendo utilizar qualquer protocolo
                               // de encriptação que esteja disponível no servidor, ou seja, serve para segurança dos dados enviados.
                               propriedades["mail.smtp.starttls.enable"] = "true"
                               // O servidor de conexão do smtp
                               propriedades["mail.smtp.host"] = "smtp.outlook.com"
                               // A porta do servidor SMTP que está conectado (587 é a padrão para envio de emails)
                               propriedades["mail.smtp.port"] = "587"

                               // Objeto sessão que retorna o objeto com as propriedades do protocolo SMTP
                               // Basicamente, essa é a parte em que há a tentativa de autenticar o usuário e senha
                               // para conectar a sessão do outlook e fazer o envio do email.
                               val sessao = Session.getInstance(propriedades, object : Authenticator(){
                                   override fun getPasswordAuthentication(): PasswordAuthentication {
                                       return PasswordAuthentication(usuario, senha)
                                   }
                               })

                               // Criação do email passando a sessão como parâmetro de MimeMessage
                               val email = MimeMessage(sessao)
                               // Define o assunto
                               email.subject = assunto
                               // O conteúdo do email
                               email.setText(feedback)
                               // Remetente do email (nesse caso, o remetente será a própria conta criada do aplicativo)
                               email.setFrom(InternetAddress(usuario))
                               // Essa parte define quem receberá o email, no caso, utilizamos "Para" (To), e o próprio
                               // endereço de email do aplicativo para onde irá o feedback
                               email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(usuario))
                               // Por último, usando a classe Transport e o método send, o email é enviado.
                               Transport.send(email)

                           // Caso o email não seja enviado
                           }catch (e: Exception){ return@Thread }
                       // Inicia a Thread
                       }.start()

                        // Após o envio, é mostrado um AlertDialog de agradecimento pelo feedback
                        InfoSheet().show(this@ConfiguracoesActivity) {
                            // Estilo do sheet (AlertDialog)
                            style(SheetStyle.DIALOG)
                            // Título do AlertDialog
                            title("Feedback enviado")
                            // Cor do título
                            titleColorRes(R.color.purple1)
                            // Mensagem do AlertDialog
                            content("Ficamos felizes em receber seu feedback! Continue aproveitando suas músicas e muito obrigado!")
                            // Esconde os ambos os botões
                            displayButtons(false)
                        }

                    // Caso contrário (não esteja conectado ou se conectando a internet)
                    }else{
                        // Mostra um toast de erro
                        Toast.makeText(this@ConfiguracoesActivity, "Você precisa estar conectado a internet", Toast.LENGTH_SHORT).show()
                    }
                }
                // Cor do botão negativo
                negativeButtonColorRes(R.color.grey3)
            }
        }

        binding.btnTerceiros.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            binding.btnTerceiros.isEnabled = false
            // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
            val terceirosSheet = InfoSheet().build(this) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Título
                title("Softwares de Terceiros")
                // Mensagem do AlertDialog
                content("Segue abaixo a lista de softwares utilizados no desenvolvimento do aplicativo:" +
                        "\n\n- Android Studio por Google" +
                        "\n- IntelliJ IDEA Community Edition por JetBrains" +
                        "\n- Glide por bumptech" +
                        "\n- Sheets por maxkeppeler" +
                        "\n- Android Gif Drawable por koral--" +
                        "\n- JavaMail-API-Android por Oracle")
                // Esconde os ambos os botões
                displayButtons(false)
                // Torna o objeto clicável novamente quando o diálogo for fechado
                onClose { binding.btnBugs.isEnabled = true }
            }
            // Mostra o AlertDialog
            terceirosSheet.show()
        }
    }

    // Método para checar o switch quando o usuário clicar nele
    private fun checarSwitch(){
        // Se o switch estiver ligado
        if (binding.switchConfig.isChecked){
            // Então switch1 é verdadeiro
            switch1 = true
            // Aplica o getSharedPreferences para salvar a escolha do usuário
            val editor = getSharedPreferences("SWITCH1", MODE_PRIVATE).edit()
            // E aplica o valor booleano específico, nesse caso, true.
            editor.putBoolean("switchAud", switch1)
            editor.apply()
        // Caso contrário (se o switch estiver desligado)
        }else{
            // Então switch1 é falso
            switch1 = false
            // Aplica o getSharedPreferences para salvar a escolha do usuário
            val editor = getSharedPreferences("SWITCH1", MODE_PRIVATE).edit()
            // E aplica o valor booleano específico, nesse caso, false
            editor.putBoolean("switchAud", switch1)
            editor.apply()
        }
    }

    // Método onResume, para quando o usuário volta a activity
    override fun onResume() {
        super.onResume()
        // SharedPreferences, para retornar as definições do usuário quanto a opção de esconder áudios do WhatsApp
        val switchEditor = getSharedPreferences("SWITCH1", MODE_PRIVATE)
        val switchAud = switchEditor.getBoolean("switchAud", switch1)
        // Se o valor da configuração for true então liga o switch permanecerá ligado ao entrar na tela
        binding.switchConfig.isChecked = switchAud
    }
}