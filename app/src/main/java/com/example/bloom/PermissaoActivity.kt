package com.example.bloom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.example.bloom.databinding.ActivityMainBinding
import com.example.bloom.databinding.ActivityPermissaoBinding
import kotlinx.android.synthetic.main.activity_permissao.*

class PermissaoActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPermissaoBinding // binding é a variável do ViewBinding para ligar as views ao código

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialização do binding
        binding = ActivityPermissaoBinding.inflate(layoutInflater)
        // root ou getRoot retorna a view mais externa no arquivo de layout associado ao binding
        // no caso, a ActivityPermissaoBinding (activity_permissao.xml)
        setContentView(binding.root)
        setTheme(R.style.Theme_AppCompat_temaClaro)

        // Botões de opção
        binding.btnPermitir.setOnClickListener { permitirPerm() }
        binding.btnCancelar.setOnClickListener { finish() }
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
                val permAlert = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                permAlert.setTitle("Bloom não pedirá mais pelas permissões!")
                permAlert.setMessage("Se você deseja utilizar o aplicativo, conceda as permissões necessárias nas configurações do Bloom.\n\nAo clicar em \"Cancelar\" o aplicativo será encerrado.")
                permAlert.setCancelable(false) // Impede que o AlertDialog seja fechado se clicado na parte de fora dele ou utilizando o botão voltar do celular

                // Botão positivo que redireciona o usuário para a tela de configurações e detalhes do aplicativo
                permAlert.setPositiveButton("Configurações"){ _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS // Caminho para a tela que será redirecionado
                        data = Uri.fromParts("package", packageName, null) // Pacote (package) do aplicativo para que o usuário seja levado para as configurações deste aplicativo.
                    })
                }

                // Botão negativo que encerra o aplicativo
                permAlert.setNegativeButton("Cancelar"){ _, _ ->
                    finish()
                }

                // Constante utilizada para mostrar o AlertDialog
                val alert: AlertDialog = permAlert.create()
                alert.show()
            }
        }
    }
}