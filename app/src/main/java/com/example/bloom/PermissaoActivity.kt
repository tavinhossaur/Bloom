package com.example.bloom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bloom.databinding.ActivityPermissaoBinding
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.info.InfoSheet

class PermissaoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPermissaoBinding // binding é a variável do ViewBinding para ligar as views ao código

    // Método chamado quando o aplicativo é iniciado
    override fun onCreate(savedInstanceState: Bundle?) {
        modoEscuro()
        super.onCreate(savedInstanceState)

        // Inicialização do binding
        binding = ActivityPermissaoBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPermissaoBinding (activity_permissao.xml)
        setContentView(binding.root)

        // Botões de opção
        binding.btnPermitir.setOnClickListener { permitirPerm() }
        binding.btnCancelar.setOnClickListener { finish() }
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
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()

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