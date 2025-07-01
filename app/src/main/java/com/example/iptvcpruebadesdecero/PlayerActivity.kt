package com.example.iptvcpruebadesdecero

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.iptvcpruebadesdecero.databinding.ActivityPlayerBinding
import com.example.iptvcpruebadesdecero.model.Canal
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer as VLCMediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper

/**
 * Actividad que maneja la reproducción de streams IPTV.
 * Utiliza VLC para reproducir el contenido multimedia.
 * Implementa un reproductor de video a pantalla completa con controles personalizados.
 */
class PlayerActivity : AppCompatActivity() {
    // Binding para acceder a las vistas de manera segura
    private lateinit var binding: ActivityPlayerBinding

    // Lista de canales y posición actual
    private var canales: List<Canal> = emptyList()
    private var currentPosition: Int = -1

    // Instancia del reproductor VLC
    private var libVLC: LibVLC? = null
    private var vlcPlayer: VLCMediaPlayer? = null

    // Variables para control de UI
    private var isPlaying = true
    private var controlsVisible = false
    private val handler = Handler(Looper.getMainLooper())
    private val hideControlsRunnable = Runnable { hideControls() }
    private val splashTimeoutRunnable = Runnable {
        binding.loadingAnimation.visibility = View.GONE
        binding.playerView.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Error de carga")
            .setMessage("No se pudo iniciar la reproducción del canal. Puede que el canal no tenga video o haya un problema de red/codec.")
            .setPositiveButton("Cerrar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            updateProgressBar()
            handler.postDelayed(this, 1000)
        }
    }

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

        setupLoadingAnimation()
        setupVLCPlayer()
        setupControls()
        setupTouchListener()
        showControls()
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
     * Configura los controles personalizados.
     */
    private fun setupControls() {
        // Botón de volver
        binding.backButton.setOnClickListener {
            finish()
        }

        // Botón de pausa/reproducción
        binding.playPauseButton.setOnClickListener {
            if (isPlaying) {
                vlcPlayer?.pause()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_play)
            } else {
                vlcPlayer?.play()
                binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            }
            isPlaying = !isPlaying
            resetControlsTimer()
        }

        // Botón de canal anterior
        binding.previousButton.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                loadChannel(currentPosition)
            }
            resetControlsTimer()
        }

        // Botón de siguiente canal
        binding.nextButton.setOnClickListener {
            if (currentPosition < canales.size - 1) {
                currentPosition++
                loadChannel(currentPosition)
            }
            resetControlsTimer()
        }

        // Botón de adelantar 5 segundos
        binding.forwardButton.setOnClickListener {
            vlcPlayer?.let { player ->
                val currentTime = player.time
                player.time = currentTime + 5000
            }
            resetControlsTimer()
        }

        // Botón de retroceder 5 segundos
        binding.rewindButton.setOnClickListener {
            vlcPlayer?.let { player ->
                val currentTime = player.time
                player.time = maxOf(0, currentTime - 5000)
            }
            resetControlsTimer()
        }

        // Barra de progreso (SeekBar)
        binding.progressSeekBar.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            var userSeeking = false
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && vlcPlayer != null && vlcPlayer!!.length > 0) {
                    val newTime = (progress / 1000.0 * vlcPlayer!!.length).toLong()
                    binding.currentTimeText.text = formatMillis(newTime)
                }
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                userSeeking = true
            }
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                userSeeking = false
                if (vlcPlayer != null && vlcPlayer!!.length > 0) {
                    val newTime = (seekBar!!.progress / 1000.0 * vlcPlayer!!.length).toLong()
                    vlcPlayer!!.time = newTime
                }
            }
        })
    }

    /**
     * Configura el listener de toques para mostrar/ocultar controles.
     */
    private fun setupTouchListener() {
        binding.playerView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (controlsVisible) {
                        hideControls()
                    } else {
                        showControls()
                    }
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Muestra los controles y programa su ocultación automática.
     */
    private fun showControls() {
        controlsVisible = true
        binding.topBarLayout.visibility = View.VISIBLE
        binding.controlsBar.visibility = View.VISIBLE
        binding.progressBarLayout.visibility = View.VISIBLE
        resetControlsTimer()
    }

    /**
     * Oculta los controles.
     */
    private fun hideControls() {
        controlsVisible = false
        binding.topBarLayout.visibility = View.GONE
        binding.controlsBar.visibility = View.GONE
        binding.progressBarLayout.visibility = View.GONE
    }

    /**
     * Reinicia el timer para ocultar controles automáticamente.
     */
    private fun resetControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable)
        handler.postDelayed(hideControlsRunnable, 3000) // Ocultar después de 3 segundos
    }

    /**
     * Carga un canal específico.
     */
    private fun loadChannel(position: Int) {
        val canal = canales.getOrNull(position)
        if (canal != null) {
            binding.channelNameText.text = canal.nombre
            binding.loadingAnimation.visibility = View.VISIBLE
            // Detener reproducción actual
            vlcPlayer?.stop()
            // Preparar nuevo media
            val media = Media(libVLC, Uri.parse(canal.url))
            media.setHWDecoderEnabled(true, false)
            // Forzar salida de audio a android_audiotrack
            media.addOption(":aout=android_audiotrack")
            vlcPlayer?.media = media
            // Asegurar volumen
            vlcPlayer?.volume = 100
            handler.removeCallbacks(splashTimeoutRunnable)
            handler.postDelayed(splashTimeoutRunnable, 10000)
            vlcPlayer?.play()
            // Seleccionar automáticamente la primera pista de audio disponible
            vlcPlayer?.let { player ->
                val audioTracks = player.audioTracks
                if (audioTracks != null && audioTracks.isNotEmpty()) {
                    val firstTrackId = audioTracks[0].id
                    player.setAudioTrack(firstTrackId)
                }
            }
            isPlaying = true
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            // Iniciar actualización de barra de progreso
            handler.removeCallbacks(updateProgressRunnable)
            handler.post(updateProgressRunnable)
        }
    }

    /**
     * Configura el reproductor VLC con la URL proporcionada.
     * Inicializa el reproductor, configura los listeners y comienza la reproducción.
     */
    private fun setupVLCPlayer() {
        try {
            // Inicializar LibVLC
            libVLC = LibVLC(this, arrayListOf("--no-drop-late-frames", "--no-skip-frames"))
            vlcPlayer = VLCMediaPlayer(libVLC)

            // Obtener la URL del canal actual
            val canal = canales.getOrNull(currentPosition)
            if (canal == null) {
                Toast.makeText(this, "No se pudo obtener el canal.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Mostrar nombre del canal
            binding.channelNameText.text = canal.nombre

            // Asignar el VLCVideoLayout al reproductor
            vlcPlayer?.attachViews(binding.playerView, null, false, false)

            // Preparar el Media
            val media = Media(libVLC, Uri.parse(canal.url))
            media.setHWDecoderEnabled(true, false)
            // Forzar salida de audio a android_audiotrack
            media.addOption(":aout=android_audiotrack")
            vlcPlayer?.media = media
            // Asegurar volumen
            vlcPlayer?.volume = 100
            handler.removeCallbacks(splashTimeoutRunnable)
            handler.postDelayed(splashTimeoutRunnable, 10000)

            // Listener para eventos de VLC
            vlcPlayer?.setEventListener { event ->
                runOnUiThread {
                    when (event.type) {
                        0x100 -> { // Playing
                            handler.removeCallbacks(splashTimeoutRunnable)
                            binding.loadingAnimation.visibility = View.GONE
                            binding.playerView.visibility = View.VISIBLE
                            isPlaying = true
                            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
                            // Seleccionar automáticamente la primera pista de audio disponible
                            vlcPlayer?.let { player ->
                                val audioTracks = player.audioTracks
                                if (audioTracks != null && audioTracks.isNotEmpty()) {
                                    val firstTrackId = audioTracks[0].id
                                    player.setAudioTrack(firstTrackId)
                                }
                            }
                            // Iniciar actualización de barra de progreso
                            handler.removeCallbacks(updateProgressRunnable)
                            handler.post(updateProgressRunnable)
                        }
                        0x10C -> { // VIDEO_OUTPUT (268)
                            handler.removeCallbacks(splashTimeoutRunnable)
                            binding.loadingAnimation.visibility = View.GONE
                            binding.playerView.visibility = View.VISIBLE
                            // Iniciar actualización de barra de progreso
                            handler.removeCallbacks(updateProgressRunnable)
                            handler.post(updateProgressRunnable)
                        }
                        0x103 -> { // Buffering
                            binding.loadingAnimation.visibility = View.VISIBLE
                            binding.playerView.visibility = View.VISIBLE
                        }
                        0x102 -> { // EndReached
                            handler.removeCallbacks(splashTimeoutRunnable)
                            binding.loadingAnimation.visibility = View.GONE
                            // Detener actualización de barra de progreso
                            handler.removeCallbacks(updateProgressRunnable)
                        }
                        0x101 -> { // Error
                            handler.removeCallbacks(splashTimeoutRunnable)
                            binding.loadingAnimation.visibility = View.GONE
                            AlertDialog.Builder(this)
                                .setTitle("Error de Reproducción (VLC)")
                                .setMessage("No se pudo reproducir el canal con VLC.")
                                .setPositiveButton("Cerrar") { _, _ -> finish() }
                                .setCancelable(false)
                                .show()
                            // Detener actualización de barra de progreso
                            handler.removeCallbacks(updateProgressRunnable)
                        }
                    }
                }
            }

            // Reproducir
            vlcPlayer?.play()
            // Seleccionar automáticamente la primera pista de audio disponible
            vlcPlayer?.let { player ->
                val audioTracks = player.audioTracks
                if (audioTracks != null && audioTracks.isNotEmpty()) {
                    val firstTrackId = audioTracks[0].id
                    player.setAudioTrack(firstTrackId)
                }
            }
            isPlaying = true
            binding.playPauseButton.setImageResource(android.R.drawable.ic_media_pause)
            // Iniciar actualización de barra de progreso
            handler.removeCallbacks(updateProgressRunnable)
            handler.post(updateProgressRunnable)
        } catch (e: Exception) {
            handler.removeCallbacks(splashTimeoutRunnable)
            binding.loadingAnimation.visibility = View.GONE
            if (!isFinishing) {
                AlertDialog.Builder(this)
                    .setTitle("Error Crítico del Reproductor VLC")
                    .setMessage("No se pudo inicializar el reproductor VLC:\n\n${e.message}")
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
        vlcPlayer?.play()
    }

    /**
     * Método llamado cuando la actividad se detiene.
     * Pausa la reproducción si el reproductor está disponible.
     */
    override fun onStop() {
        super.onStop()
        vlcPlayer?.pause()
    }

    /**
     * Método llamado cuando la actividad se destruye.
     * Libera los recursos del reproductor.
     */
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(hideControlsRunnable)
        handler.removeCallbacks(splashTimeoutRunnable)
        handler.removeCallbacks(updateProgressRunnable)
        try {
            vlcPlayer?.stop()
            vlcPlayer?.detachViews()
            vlcPlayer?.release()
            libVLC?.release()
        } catch (_: Exception) {}
        vlcPlayer = null
        libVLC = null
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

    private fun updateProgressBar() {
        vlcPlayer?.let { player ->
            val duration = player.length
            val position = player.time
            if (duration > 0) {
                val progress = ((position.toDouble() / duration) * 1000).toInt()
                binding.progressSeekBar.progress = progress
                binding.currentTimeText.text = formatMillis(position)
                binding.totalTimeText.text = formatMillis(duration)
            } else {
                binding.progressSeekBar.progress = 0
                binding.currentTimeText.text = "00:00"
                binding.totalTimeText.text = "00:00"
            }
        }
    }

    private fun formatMillis(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
} 