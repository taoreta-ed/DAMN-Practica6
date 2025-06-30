// MainActivity.kt
package com.example.damn_practica6

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.damn_practica6.ui.theme.DAMNPractica6Theme

class MainActivity : ComponentActivity() {

    // Adaptador Bluetooth para interactuar con el hardware Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothAdapter.getDefaultAdapter()
    }

    // Solicitud de permisos de Bluetooth
    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // Verifica si todos los permisos necesarios fueron concedidos
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                // Si los permisos son concedidos, verifica si Bluetooth está habilitado
                checkBluetoothEnabled()
            } else {
                // Si los permisos no son concedidos, muestra un mensaje al usuario
                Toast.makeText(this, "Se requieren permisos de Bluetooth para usar esta aplicación.", Toast.LENGTH_LONG).show()
            }
        }

    // Solicitud para habilitar Bluetooth
    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Bluetooth habilitado, puedes continuar con la lógica de la aplicación
                Toast.makeText(this, "Bluetooth habilitado.", Toast.LENGTH_SHORT).show()
            } else {
                // El usuario denegó habilitar Bluetooth
                Toast.makeText(this, "Bluetooth no habilitado. La aplicación puede no funcionar correctamente.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DAMNPractica6Theme {
                // Un contenedor de superficie usando el color 'background' del tema
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Contenido principal de la aplicación
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("DAMN-Practica6: Navegador Bluetooth")
                        // Aquí se agregarán los botones para seleccionar el rol (Cliente/Servidor)
                    }
                }
            }
        }

        // Al iniciar la actividad, solicita los permisos de Bluetooth
        requestBluetoothPermissions()
    }

    // Función para solicitar los permisos de Bluetooth necesarios
    private fun requestBluetoothPermissions() {
        // Lista mutable para almacenar los permisos que necesitamos solicitar
        val permissionsToRequest = mutableListOf<String>()

        // Permisos específicos para Android 12 (API 31) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // Permisos para versiones anteriores a Android 12
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
            // ACCESS_FINE_LOCATION es necesario para el escaneo de Bluetooth en API 23+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // Si hay permisos pendientes, solicitarlos
        if (permissionsToRequest.isNotEmpty()) {
            requestBluetoothPermissions.launch(permissionsToRequest.toTypedArray())
        } else {
            // Si todos los permisos ya están concedidos, verifica si Bluetooth está habilitado
            checkBluetoothEnabled()
        }
    }

    // Función para verificar si Bluetooth está habilitado y solicitar su activación si no lo está
    private fun checkBluetoothEnabled() {
        if (bluetoothAdapter == null) {
            // El dispositivo no soporta Bluetooth
            Toast.makeText(this, "Tu dispositivo no soporta Bluetooth.", Toast.LENGTH_LONG).show()
        } else if (!bluetoothAdapter!!.isEnabled) {
            // Bluetooth no está habilitado, solicita al usuario que lo habilite
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            // Bluetooth está habilitado y los permisos concedidos, puedes continuar
            Toast.makeText(this, "Bluetooth está habilitado y listo.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DAMNPractica6Theme {
        Greeting("Android")
    }
}