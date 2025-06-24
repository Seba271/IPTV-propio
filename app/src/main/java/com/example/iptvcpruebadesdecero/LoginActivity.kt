package com.example.iptvcpruebadesdecero

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.iptvcpruebadesdecero.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Actividad de login que permite al usuario autenticarse antes de acceder a la aplicación.
 * El usuario debe ingresar sus credenciales para descargar su lista de reproducción.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate iniciado")
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupListeners()
        Log.d(TAG, "onCreate completado")
    }

    /**
     * Configura las vistas iniciales
     */
    private fun setupViews() {
        try {
            Log.d(TAG, "Configurando vistas")
            // Configurar el estado inicial del botón de login
            updateLoginButtonState()
            
            // Configurar el estado inicial de los campos de error
            binding.textInputLayoutUsername.error = null
            binding.textInputLayoutPassword.error = null
            Log.d(TAG, "Vistas configuradas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupViews: ${e.message}", e)
        }
    }

    /**
     * Configura los listeners para los campos de texto y botones
     */
    private fun setupListeners() {
        try {
            Log.d(TAG, "Configurando listeners")
            // Listener para el botón de login
            binding.buttonLogin.setOnClickListener {
                performLogin()
            }

            // Listener para el botón de salir
            binding.buttonExit.setOnClickListener {
                finish()
            }

            // TextWatchers para validación en tiempo real
            binding.editTextUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    clearUsernameError()
                    updateLoginButtonState()
                }
            })

            binding.editTextPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    clearPasswordError()
                    updateLoginButtonState()
                }
            })

            // Listener para el Enter en el campo de contraseña
            binding.editTextPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    performLogin()
                    true
                } else {
                    false
                }
            }
            Log.d(TAG, "Listeners configurados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupListeners: ${e.message}", e)
        }
    }

    /**
     * Realiza la validación de login
     */
    private fun performLogin() {
        Log.d(TAG, "Iniciando proceso de login")
        val username = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        // Validar campos vacíos
        if (username.isEmpty()) {
            binding.textInputLayoutUsername.error = "El usuario es requerido"
            binding.editTextUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.error = "La contraseña es requerida"
            binding.editTextPassword.requestFocus()
            return
        }

        // Mostrar progreso
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonLogin.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urlString = "http://iptv.ctvc.cl:80/playlist/$username/$password/m3u_plus?output=hls"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val playlistContent = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()

                    // Guardar la playlist en el almacenamiento interno
                    savePlaylistToFile(playlistContent)

                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Login exitoso y playlist guardada")
                        showSuccess("Login exitoso")
                        // Navegar a MainActivity
                        Log.d(TAG, "Iniciando MainActivity")
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Log.w(TAG, "Error en el login, código de respuesta: $responseCode")
                        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            showError("Usuario o contraseña incorrectos")
                        } else {
                            showError("Error en el login: $responseCode")
                        }
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error de red durante el login: ${e.message}", e)
                    showError("Error de red. Verifique su conexión.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error inesperado durante el login: ${e.message}", e)
                    showError("Error inesperado durante el login.")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    // Ocultar progreso y habilitar botón
                    binding.progressBar.visibility = View.GONE
                    binding.buttonLogin.isEnabled = true
                }
            }
        }
    }

    private fun savePlaylistToFile(content: String) {
        try {
            val file = File(filesDir, "downloaded_playlist.m3u")
            FileOutputStream(file).use {
                it.write(content.toByteArray())
            }
            Log.d(TAG, "Playlist guardada en ${file.absolutePath}")
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar la playlist: ${e.message}", e)
            throw e // Re-lanzar para que el bloque catch principal lo maneje
        }
    }

    /**
     * Actualiza el estado del botón de login basado en los campos
     */
    private fun updateLoginButtonState() {
        try {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            
            binding.buttonLogin.isEnabled = username.isNotEmpty() && password.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error en updateLoginButtonState: ${e.message}", e)
        }
    }

    /**
     * Limpia el error del campo de usuario
     */
    private fun clearUsernameError() {
        binding.textInputLayoutUsername.error = null
    }

    /**
     * Limpia el error del campo de contraseña
     */
    private fun clearPasswordError() {
        binding.textInputLayoutPassword.error = null
    }

    /**
     * Muestra un mensaje de error
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Muestra un mensaje de éxito
     */
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 