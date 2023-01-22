package com.example.bloom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bloom.databinding.ActivityPermissaoBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet
import com.maxkeppeler.sheets.input.InputSheet
import com.maxkeppeler.sheets.input.type.InputEditText

class PermissaoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPermissaoBinding // binding é a variável do ViewBinding para ligar as views ao código

    companion object{
       var nomeUser : String = ""
    }

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Bloom)
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityPermissaoBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPermissaoBinding (activity_permissao.xml)
        setContentView(binding.root)

        // Botões de opção
        binding.btnPermitir.setOnClickListener { permitirPerm() }
        binding.btnCancelar.setOnClickListener { finish() }

        // Ajuste de cores para o modo escuro do Android
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES){
            binding.btnPermitir.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
            binding.btnCancelar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.black6))
        }
    }

    // Função para checar a permissão e pedir se o usuário ainda não as tiver concedido
    private fun permitirPerm(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            val showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            // Caso os resultados não forem vazios e o array conter o elemento [0], então as permissões foram concedidas
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissões concedidas!", Toast.LENGTH_SHORT).show()
                binding.root.visibility = View.GONE
                // Diálogo para o usuário inserir o seu nome
                InputSheet().show(this) {
                    // Estilo do sheet (BottomSheet)
                    style(SheetStyle.DIALOG)
                    // Título do BottomSheetDialog
                    title("Como devemos te chamar?")
                    // Conteúdo da sheet (Edit Texts)
                    with(InputEditText("nome") {
                        required(true)
                        drawable(R.drawable.ic_round_person_24)
                        label("Você pode editar seu nome mais tarde nas configurações se desejar.")
                        hint("Insira seu nome ou apelido...")
                    })
                    // Cor do botão "confirmar"
                    positiveButtonColorRes(R.color.purple1)
                    // Botão confirmar do AlertDialog
                    onPositive("Confirmar") { result ->
                        // Retorna o valor string da input "nome_playlist"
                        nomeUser = result.getString("nome").toString()

                        // Aplica o getSharedPreferences para salvar o nome do usuário
                        val editor = getSharedPreferences("NOME", MODE_PRIVATE).edit()
                        // E salva e aplica o valor da string
                        editor.putString("nomeUser", nomeUser)
                        editor.apply()

                        Toast.makeText(this@PermissaoActivity, "Nome salvo! Olá, $nomeUser!", Toast.LENGTH_LONG).show()

                        // E então o usuário é enviado para tela principal
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }
                    // Cor do botão negativo
                    negativeButtonColorRes(R.color.grey3)
                    // Botão cancelar do AlertDialog
                    onNegative {
                        nomeUser = ""
                        // E o usuário é enviado para tela principal
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }
                }
            // Caso o usuário selecione 2x para negar a permissão, marque a checkbox para não perguntar novamente,
            // ou diretamente escolha para negar e não pedir mais a permissão
            // Um AlertDialog surgirá para dizer que o app não pedirá mais a permissão e portanto, ela deve ser concedida nas configurações do aplicativo
            }else if(!showRationale) {
                // Criação do AlertDialog utilizando o InfoSheet da biblioteca "Sheets"
                val permSheet = InfoSheet().build(this) {
                    // Estilo do sheet (AlertDialog)
                    style(SheetStyle.DIALOG)
                    // Título do AlertDialog
                    title("Bloom não pedirá mais pelas permissões!")
                    // Cor do título
                    titleColorRes(R.color.purple1)
                    // Impede que o usuário feche o diálogo clicando fora dele
                    cancelableOutside(false)
                    // Mensagem do AlertDialog
                    content("Se você deseja utilizar o aplicativo, conceda as permissões necessárias nas configurações do Bloom.\n\n Ao clicar em \"Cancelar\" o aplicativo será encerrado.")
                    // Botão positivo que redireciona o usuário para a tela de configurações e detalhes do aplicativo
                    positiveButtonColorRes(R.color.purple1)
                    onPositive("Permissões") {
                        startActivity(Intent().apply {
                            // Caminho para a tela que será redirecionado
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            // Pacote (package) do aplicativo para que o usuário seja levado para as configurações deste aplicativo.
                            data = Uri.fromParts("package", packageName, null)
                        })
                    }
                    // Botão negativo que encerra o aplicativo
                    negativeButtonColorRes(R.color.grey3)
                    onNegative {
                        finish()
                    }
                }
                // Mostra o AlertDialog
                permSheet.show()
            }
        }
    }
}