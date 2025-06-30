// MainActivity.kt
package com.example.damn_practica6

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.damn_practica6.ui.theme.DAMNPractica6Theme
import com.google.accompanist.web.WebView // Importar WebView de Accompanist
import com.google.accompanist.web.rememberWebViewState // Importar rememberWebViewState
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

// UUID único para identificar nuestro servicio Bluetooth (se mantiene por estructura, no por uso real)
private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
private const val APP_NAME = "DAMN_Practica6_BT"

// Definimos un enum para manejar el estado de la pantalla
enum class AppScreen {
    ROLE_SELECTION,
    SERVER,
    CLIENT
}

// Clase de datos para simular un dispositivo Bluetooth para la UI
data class SimulatedBluetoothDevice(val name: String, val address: String)

class MainActivity : ComponentActivity() {

    // Adaptador Bluetooth para interactuar con el hardware Bluetooth
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // Solicitud de permisos de Bluetooth
    private val requestBluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Log.d("Permissions", "Todos los permisos de Bluetooth concedidos.")
                checkBluetoothEnabled()
            } else {
                Log.w("Permissions", "No todos los permisos de Bluetooth fueron concedidos.")
                val missingPermissions = permissions.entries.filter { !it.value }.map { it.key }
                val permissionNames = missingPermissions.joinToString { perm ->
                    when (perm) {
                        Manifest.permission.BLUETOOTH_SCAN -> "BLUETOOTH_SCAN (Android 12+)"
                        Manifest.permission.BLUETOOTH_ADVERTISE -> "BLUETOOTH_ADVERTISE (Android 12+)"
                        Manifest.permission.BLUETOOTH_CONNECT -> "BLUETOOTH_CONNECT (Android 12+)"
                        Manifest.permission.ACCESS_FINE_LOCATION -> "UBICACIÓN (necesario para escanear en Android 11 o inferior)"
                        else -> perm
                    }
                }
                Toast.makeText(this, "Se requieren los siguientes permisos: $permissionNames. Por favor, concédelos en la configuración de la aplicación.", Toast.LENGTH_LONG).show()
            }
        }

    // Solicitud para habilitar Bluetooth
    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth no habilitado. La aplicación puede no funcionar correctamente.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            DAMNPractica6Theme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.ROLE_SELECTION) }

                    when (currentScreen) {
                        AppScreen.ROLE_SELECTION -> RoleSelectionScreen(
                            onRoleSelected = { screen -> currentScreen = screen },
                            onToggleTheme = { isDarkTheme = !isDarkTheme }
                        )
                        AppScreen.SERVER -> ServerScreen {
                            currentScreen = AppScreen.ROLE_SELECTION
                        }
                        AppScreen.CLIENT -> ClientScreen(
                            onBack = { currentScreen = AppScreen.ROLE_SELECTION }
                        )
                    }
                }
            }
        }
        // Llamada a la función para manejar la solicitud inicial de permisos
        handleInitialPermissions()
    }

    // Función para solicitar los permisos de Bluetooth necesarios
    private fun handleInitialPermissions() {
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
            // Solo ACCESS_FINE_LOCATION es un permiso peligroso que necesita ser solicitado.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // Si hay permisos pendientes, solicitarlos
        if (permissionsToRequest.isNotEmpty()) {
            Log.d("Permissions", "Solicitando permisos: ${permissionsToRequest.joinToString()}")
            requestBluetoothPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("Permissions", "Todos los permisos ya concedidos al inicio.")
            // Si todos los permisos ya están concedidos, verifica si Bluetooth está habilitado
            checkBluetoothEnabled()
        }
    }

    private fun checkBluetoothEnabled() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Tu dispositivo no soporta Bluetooth.", Toast.LENGTH_LONG).show()
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        } else {
            Toast.makeText(this, "Bluetooth está habilitado y listo.", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun RoleSelectionScreen(onRoleSelected: (AppScreen) -> Unit, onToggleTheme: () -> Unit) {
    val isSystemInDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "DAMN-Practica6: Navegador Bluetooth",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { onRoleSelected(AppScreen.SERVER) }) {
            Text("Actuar como Servidor (Comparte Internet)")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRoleSelected(AppScreen.CLIENT) }) {
            Text("Actuar como Cliente (Navega por Internet)")
        }
        Spacer(modifier = Modifier.height(32.dp))
        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (isSystemInDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isSystemInDarkTheme) "Cambiar a Tema Claro" else "Cambiar a Tema Oscuro",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = if (isSystemInDarkTheme) "Tema Oscuro" else "Tema Claro",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// Composable para la pantalla de Servidor
@Composable
fun ServerScreen(onBack: () -> Unit) {
    var serverStatus by remember { mutableStateOf("Inactivo") }
    var connectedDevice by remember { mutableStateOf("Ninguno") }
    val context = LocalContext.current

    // Simulación del servidor: se activa al entrar en la pantalla
    DisposableEffect(Unit) {
        serverStatus = "Escuchando conexiones..."
        // Simular una conexión después de un tiempo
        val handler = android.os.Handler(context.mainLooper)
        val runnable = Runnable {
            serverStatus = "Conectado"
            connectedDevice = "Cliente Conectado"
            Toast.makeText(context, "Cliente conectado.", Toast.LENGTH_SHORT).show()
        }
        handler.postDelayed(runnable, 3000) // Simular conexión después de 3 segundos

        onDispose {
            handler.removeCallbacks(runnable)
            serverStatus = "Inactivo"
            connectedDevice = "Ninguno"
            Toast.makeText(context, "Servidor desconectado.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }
        Text(
            text = "Pantalla de Servidor",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Estado del Servidor: $serverStatus",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Dispositivo Conectado: $connectedDevice",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = {
            Toast.makeText(context, "Haciendo el dispositivo detectable.", Toast.LENGTH_SHORT).show()
        }) {
            Text("Hacer Dispositivo Detectable")
        }
    }
}

// Composable para la pantalla de Cliente
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("https://www.google.com") } // URL de ejemplo
    val webViewState = rememberWebViewState(url = urlInput)
    var scanningStatus by remember { mutableStateOf("Inactivo") }
    // Usamos nuestra clase de datos simulada
    val foundDevices = remember { mutableStateListOf<SimulatedBluetoothDevice>() }
    // Nuevo estado para controlar si el navegador está visible
    var showBrowser by remember { mutableStateOf(false) }
    // Estado para el dispositivo al que se "conectó"
    var connectedToServerName by remember { mutableStateOf("Ninguno") }

    // Inicializar la lista con "Servidor Prueba"
    LaunchedEffect(Unit) {
        if (foundDevices.isEmpty()) {
            foundDevices.add(SimulatedBluetoothDevice("Servidor Prueba", "00:00:00:00:00:00"))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }
        Text(
            text = "Pantalla de Cliente",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!showBrowser) {
            // Mostrar la lista de dispositivos y el botón de escanear
            Text(
                text = "Estado del Escaneo: $scanningStatus",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                scanningStatus = "Escaneando..."
                // Mantener "Servidor Prueba" y añadir otros simulados
                val initialDevices = listOf(SimulatedBluetoothDevice("Servidor Prueba", "00:00:00:00:00:00"))
                foundDevices.clear()
                foundDevices.addAll(initialDevices) // Asegura que "Servidor Prueba" esté siempre al inicio

                val handler = android.os.Handler(context.mainLooper)
                handler.postDelayed({
                    foundDevices.add(SimulatedBluetoothDevice("Red", "00:11:22:33:44:55"))
                    foundDevices.add(SimulatedBluetoothDevice("Laptop", "11:22:33:44:55:66"))
                    scanningStatus = "Escaneo finalizado. Dispositivos encontrados."
                    Toast.makeText(context, "Escaneo de dispositivos finalizado.", Toast.LENGTH_SHORT).show()
                }, 2000) // Simular 2 segundos de escaneo
            }) {
                Text("Escanear Dispositivos")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dispositivos Encontrados:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                items(foundDevices) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            // Simular conexión y mostrar navegador
                            Toast.makeText(context, "Conectando a ${device.name}...", Toast.LENGTH_SHORT).show()
                            connectedToServerName = device.name
                            showBrowser = true // Cambiar al estado de navegador
                            // Cargar una URL inicial cuando se "conecta"
                            urlInput = "https://www.google.com/search?q=${device.name.replace(" ", "+")}"
                            Toast.makeText(context, "Conectado a ${device.name}. Navegando...", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "Nombre: ${device.name}", style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Dirección: ${device.address}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            // Mostrar la interfaz del navegador
            Text(
                text = "Conectado a: $connectedToServerName",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Introduce URL") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    Toast.makeText(context, "Navegando a $urlInput", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.weight(1f)) {
                    Text("Navegar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    showBrowser = false // Volver a la lista de dispositivos
                    connectedToServerName = "Ninguno"
                    Toast.makeText(context, "Desconectado del servidor.", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.weight(1f)) {
                    Text("Desconectar")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                onCreated = { webView ->
                    webView.settings.javaScriptEnabled = true
                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            url?.let { view?.loadUrl(it) }
                            return true
                        }
                    }
                }
            )
        }
    }
}

// La función fetchUrlContent se mantiene, aunque no se usa directamente en esta simulación de UI.
fun fetchUrlContent(urlString: String): String {
    var connection: HttpURLConnection? = null
    var reader: BufferedReader? = null
    val response = StringBuilder()

    try {
        val url = URL(urlString)
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000 // 10 segundos
        connection.readTimeout = 15000 // 15 segundos

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line).append("\n")
            }
        } else {
            Log.e("fetchUrlContent", "Error HTTP: $responseCode para $urlString")
            return "Error HTTP: $responseCode"
        }
    } catch (e: IOException) {
        Log.e("fetchUrlContent", "Error al conectar o leer la URL: $urlString", e)
        return "Error de red: ${e.message}"
    } finally {
        reader?.close()
        connection?.disconnect()
    }
    return response.toString()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DAMNPractica6Theme {
        RoleSelectionScreen(onRoleSelected = {}, onToggleTheme = {})
    }
}
