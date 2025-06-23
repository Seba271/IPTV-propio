package com.example.iptvcpruebadesdecero

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.iptvcpruebadesdecero.databinding.ActivityPlayerBinding
import com.example.iptvcpruebadesdecero.model.Canal
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

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

    // Lista de canales y posición actual
    private var canales: List<Canal> = emptyList()
    private var currentPosition: Int = -1

    /**
     * Método de inicialización de la actividad.
     * Configura el reproductor y obtiene la URL del stream a reproducir.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la lista de canales y la posición desde el intent de forma segura
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            canales = intent.getSerializableExtra("canales", ArrayList::class.java) as? List<Canal> ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            canales = intent.getSerializableExtra("canales") as? List<Canal> ?: emptyList()
        }
        currentPosition = intent.getIntExtra("position", -1)

        if (canales.isEmpty() || currentPosition == -1) {
            Toast.makeText(this, "Error: No se pudieron cargar los datos del canal.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.backButton.setOnClickListener {
            finish()
        }

        setupLoadingAnimation()
        setupPlayer()

        // Sincronizar el botón de volver con los controles del reproductor
        binding.playerView.setControllerVisibilityListener { visibility ->
            binding.backButton.visibility = visibility
        }
    }

    /**
     * Configura la animación de carga Lottie.
     */
    private fun setupLoadingAnimation() {
        binding.loadingAnimation.apply {
            setAnimation("CargaTV - 1749346448115.json")
            repeatCount = -1 // Repetir indefinidamente
            playAnimation()
        }
    }

    /**
     * Configura el reproductor ExoPlayer con la URL proporcionada.
     * Inicializa el reproductor, configura los listeners y comienza la reproducción.
     */
    private fun setupPlayer() {
        try {
            // Configurar un User-Agent personalizado para mejorar la compatibilidad
            val userAgent = "VLC/3.0.0 LibVLC/3.0.0"
            val httpDataSourceFactory = DefaultHttpDataSource.Factory().setUserAgent(userAgent)
            val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            // Convertir la lista de Canales a una lista de MediaItems para ExoPlayer
            val mediaItems = canales.map { MediaItem.fromUri(it.url) }

            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    // Entregar la lista de reproducción completa a ExoPlayer
                    setMediaItems(mediaItems, this@PlayerActivity.currentPosition, 0L)

                    // Agregar listener para manejar errores de reproducción y estados
                    addListener(object : Player.Listener {
                        override fun onEvents(player: Player, events: Player.Events) {
                            if (events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
                                // El usuario usó los botones de siguiente/anterior
                                this@PlayerActivity.currentPosition = player.currentMediaItemIndex
                                // Opcional: actualizar un título si lo tuvieras
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            binding.loadingAnimation.visibility = View.GONE
                            val cause = error.cause
                            var errorMessage = "Error al reproducir el canal: ${error.message}"
                            if (cause != null) {
                                errorMessage += "\n\nCausa: ${cause.javaClass.simpleName}\n${cause.message}"
                            }
                            android.util.Log.e("PlayerActivity", errorMessage, error)

                            if (!isFinishing) {
                                AlertDialog.Builder(this@PlayerActivity)
                                    .setTitle("Error de Reproducción")
                                    .setMessage(errorMessage)
                                    .setPositiveButton("Cerrar") { _, _ -> finish() }
                                    .setCancelable(false)
                                    .show()
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    binding.loadingAnimation.visibility = View.GONE
                                    binding.playerView.visibility = View.VISIBLE
                                }
                                Player.STATE_BUFFERING -> {
                                    binding.loadingAnimation.visibility = View.VISIBLE
                                    binding.playerView.visibility = View.VISIBLE
                                }
                                Player.STATE_ENDED -> {
                                    binding.loadingAnimation.visibility = View.GONE
                                }
                                Player.STATE_IDLE -> {
                                    binding.loadingAnimation.visibility = View.VISIBLE
                                }
                            }
                        }
                    })
                    
                    // Preparar y comenzar la reproducción
                    prepare()
                    playWhenReady = true
                }
            
            // Asignar el reproductor a la vista
            binding.playerView.player = player
        } catch (e: Exception) {
            android.util.Log.e("PlayerActivity", "Error en setupPlayer", e)
            if (!isFinishing) {
                AlertDialog.Builder(this)
                    .setTitle("Error Crítico del Reproductor")
                    .setMessage("No se pudo inicializar el reproductor:\n\n${e.message}")
                    .setPositiveButton("Cerrar") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            }
        }
    }

    /**
     * Método llamado cuando la actividad se inicia.
     * Reanuda la reproducción si el reproductor está disponible.
     */
    override fun onStart() {
        super.onStart()
        player?.play()

        // Sincronizar el botón de volver con los controles del reproductor
        binding.playerView.setControllerVisibilityListener { visibility ->
            binding.backButton.visibility = visibility
        }
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