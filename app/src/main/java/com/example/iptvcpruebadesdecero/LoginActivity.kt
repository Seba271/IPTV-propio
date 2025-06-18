package com.example.iptvcpruebadesdecero

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iptvcpruebadesdecero.databinding.ActivityLoginBinding

/**
 * Actividad de login que permite al usuario autenticarse antes de acceder a la aplicación.
 * Credenciales por defecto: usuario: admin, contraseña: admin
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    
    // Credenciales por defecto para pruebas
    private val DEFAULT_USERNAME = "admin"
    private val DEFAULT_PASSWORD = "admin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        setupListeners()
    }

    /**
     * Configura las vistas iniciales
     */
    private fun setupViews() {
        try {
            // Configurar el estado inicial del botón de login
            updateLoginButtonState()
            
            // Configurar el estado inicial de los campos de error
            binding.textInputLayoutUsername.error = null
            binding.textInputLayoutPassword.error = null
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupViews: ${e.message}", e)
        }
    }

    /**
     * Configura los listeners para los campos de texto y botones
     */
    private fun setupListeners() {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "Error en setupListeners: ${e.message}", e)
        }
    }

    /**
     * Realiza la validación de login
     */
    private fun performLogin() {
        try {
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

            // Simular validación (en una app real, aquí iría la llamada al servidor)
            validateCredentials(username, password)
        } catch (e: Exception) {
            Log.e(TAG, "Error en performLogin: ${e.message}", e)
            showError("Error durante el login: ${e.message}")
        }
    }

    /**
     * Valida las credenciales del usuario
     */
    private fun validateCredentials(username: String, password: String) {
        try {
            // Simular delay de red
            binding.root.postDelayed({
                if (username == DEFAULT_USERNAME && password == DEFAULT_PASSWORD) {
                    // Login exitoso
                    Log.d(TAG, "Login exitoso para usuario: $username")
                    showSuccess("Login exitoso")
                    
                    // Navegar a MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Credenciales incorrectas
                    Log.w(TAG, "Credenciales incorrectas para usuario: $username")
                    showError("Usuario o contraseña incorrectos")
                    binding.editTextPassword.requestFocus()
                    binding.editTextPassword.selectAll()
                }
                
                // Ocultar progreso y habilitar botón
                binding.progressBar.visibility = View.GONE
                binding.buttonLogin.isEnabled = true
            }, 1000) // Simular 1 segundo de delay
        } catch (e: Exception) {
            Log.e(TAG, "Error en validateCredentials: ${e.message}", e)
            showError("Error al validar credenciales: ${e.message}")
            binding.progressBar.visibility = View.GONE
            binding.buttonLogin.isEnabled = true
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