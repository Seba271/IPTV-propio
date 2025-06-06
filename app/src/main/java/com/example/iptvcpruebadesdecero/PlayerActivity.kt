package com.example.iptvcpruebadesdecero

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.iptvcpruebadesdecero.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

/**
 * Actividad que maneja la reproducción de streams IPTV.
 * Utiliza ExoPlayer para reproducir el contenido multimedia.
 * Implementa un reproductor de video a pantalla completa con controles personalizados.
 */
class PlayerActivity : AppCompatActivity() {
    // Binding para acceder a las vistas de manera segura
    private lateinit var binding: ActivityPlayerBinding
    // Instancia del reproductor ExoPlayer
    private var player: ExoPlayer? = null

    /**
     * Método de inicialización de la actividad.
     * Configura el reproductor y obtiene la URL del stream a reproducir.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la URL del stream desde el intent
        val url = intent.getStringExtra("url")
        if (url == null) {
            Toast.makeText(this, "Error: URL no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPlayer(url)
    }

    /**
     * Configura el reproductor ExoPlayer con la URL proporcionada.
     * Inicializa el reproductor, configura los listeners y comienza la reproducción.
     * 
     * @param url URL del stream a reproducir
     */
    private fun setupPlayer(url: String) {
        player = ExoPlayer.Builder(this).build().apply {
            // Configurar el media item con la URL del stream
            setMediaItem(MediaItem.fromUri(url))
            
            // Agregar listener para manejar errores de reproducción
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(
                        this@PlayerActivity,
                        "Error al reproducir el canal: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
            
            // Preparar y comenzar la reproducción
            prepare()
            playWhenReady = true
        }

        // Asignar el reproductor a la vista
        binding.playerView.player = player
    }

    /**
     * Método llamado cuando la actividad se inicia.
     * Reanuda la reproducción si el reproductor está disponible.
     */
    override fun onStart() {
        super.onStart()
        player?.play()
    }

    /**
     * Método llamado cuando la actividad se detiene.
     * Pausa la reproducción si el reproductor está disponible.
     */
    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    /**
     * Método llamado cuando la actividad se destruye.
     * Libera los recursos del reproductor.
     */
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    /**
     * Método llamado cuando cambia el foco de la ventana.
     * Oculta la interfaz del sistema cuando la actividad tiene el foco.
     * 
     * @param hasFocus Indica si la actividad tiene el foco
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    /**
     * Oculta la interfaz del sistema (barras de estado y navegación)
     * para una experiencia de reproducción a pantalla completa.
     */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
} 