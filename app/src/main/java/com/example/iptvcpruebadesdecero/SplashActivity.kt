package com.example.iptvcpruebadesdecero

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {
    private lateinit var lottieAnimationView: LottieAnimationView
    private val handler = Handler(Looper.getMainLooper())
    private val splashDuration = 7000L // 7 segundos de duración
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        lottieAnimationView = findViewById(R.id.lottieAnimationView)
        setupLottieAnimation()
    }

    private fun setupLottieAnimation() {
        try {
            Log.d(TAG, "Iniciando carga de animación")
            
            lottieAnimationView.setAnimation("InicioTv2.json")
            lottieAnimationView.playAnimation()
            
            handler.postDelayed({
                startLoginActivity()
            }, splashDuration)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar la animación: ${e.message}", e)
            startLoginActivity()
        }
    }

    private fun startLoginActivity() {
        Log.d(TAG, "Iniciando LoginActivity")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::lottieAnimationView.isInitialized) {
            lottieAnimationView.cancelAnimation()
        }
    }
} 