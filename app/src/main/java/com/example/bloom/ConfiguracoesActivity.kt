package com.example.bloom

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
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
    lateinit var binding: ActivityConfiguracoesBinding

    // Declaração de objetos/classes estáticas para poder utilizar
    companion object{
        var switch1 : Boolean = false
    }

    // Método chamado quando o aplicativo/activity é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityMainBinding (activity_main.xml)
        setContentView(binding.root)

        // Ao clicar no botão fechar, a activity é simplesmente encerrada.
        binding.btnVoltarConfig.setOnClickListener {finish()}

        // Ao clicar no switch da configuração 1, passa o método para checar se o switch está ligado (true) ou desligado (false)
        binding.switchConfig1.setOnClickListener { checarSwitch() }

        // Ao clicar no botão de feedback, envia o usuário para a tela de feedback
        binding.btnFeedback.setOnClickListener {
            // Previne que o usuário crie duas sheets ao dar dois cliques rápidos
            binding.btnFeedback.isEnabled = false
            InputSheet().show(this) {
                // Estilo do sheet (AlertDialog)
                style(SheetStyle.DIALOG)
                // Altera o botão de fechar o dialogo
                // closeIconButton(IconButton(com.maxkeppeler.sheets.R.drawable.sheets_ic_close, R.color.white))
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
                // EditText do remetente do email
                with(InputEditText("remetente_email") {
                    required(false)
                    drawable(R.drawable.ic_round_person_24)
                    hint("Insira seu nome ou email (opcional)")
                })
                // EditText do conteúdo (feedback) do email
                with(InputEditText("feedback_email") {
                    required(true)
                    drawable(R.drawable.ic_round_person_24)
                    label("Feedback")
                    hint("Eu amei o aplicativo!...")
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
                    val feedback = result.getString("feedback_email").toString() + "\n\nde $remetente"

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
                           }catch (e: Exception){
                               // Mostra um toast com o erro que aconteceu
                               Toast.makeText(this@ConfiguracoesActivity, e.toString(), Toast.LENGTH_SHORT).show()
                           }
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
    }

    // Método para deixar o aplicativo no seu modo padrão
    private fun modoEscuro(){
        application.setTheme(R.style.Theme_BloomNoActionBar)
        setTheme(R.style.Theme_BloomNoActionBar)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black3)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.navigationBarColor = ContextCompat.getColor(this, R.color.black3)
    }

    // Método para checar o switch quando o usuário clicar nele
    private fun checarSwitch(){
        // Se o switch estiver ligado
        if (binding.switchConfig1.isChecked){
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
        // SharedPreferences, para salvar as definições do usuário
        val switchEditor = getSharedPreferences("SWITCH1", MODE_PRIVATE)
        val switchAud = switchEditor.getBoolean("switchAud", switch1)
        // Se o valor do switch for true
        binding.switchConfig1.isChecked = switchAud
    }
}