package com.uvic.tf_202526.atarazaga_dcasany.Activitats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.uvic.tf_202526.atarazaga_dcasany.R
import kotlin.math.abs

class SplashActivity : AppCompatActivity() {

    private lateinit var mDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        mDetector = GestureDetectorCompat(this, MyGestureListener())

        val logo = findViewById<ImageView>(R.id.iv_logo)


        logo.setOnClickListener { view ->
            view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(20)
                .withEndAction {

                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(20)
                        .withEndAction {
                            val intent = Intent(this, AboutActivity::class.java)
                            startActivity(intent)
                        }
                        .start()
                }
                .start()
        }

        iniciarAnimacio()
    }

    private fun iniciarAnimacio() {
        val logo = findViewById<ImageView>(R.id.iv_logo)
        val title = findViewById<TextView>(R.id.tv_app_title)
        val subtitle = findViewById<TextView>(R.id.tv_app_subtitle)
        val streamerInst = findViewById<TextView>(R.id.tv_streamer_instruction)
        val viewerInst = findViewById<TextView>(R.id.tv_viewer_instruction)

        logo.animate().alpha(1f).translationY(-20f).setDuration(1000).setStartDelay(300).start()
        title.animate().alpha(1f).setDuration(800).setStartDelay(1000).start()
        subtitle.animate().alpha(1f).setDuration(800).setStartDelay(1200).start()
        streamerInst.animate().alpha(1f).setDuration(500).setStartDelay(1800).start()
        viewerInst.animate().alpha(1f).setDuration(500).setStartDelay(1800).start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private inner class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onDown(event: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null) return false
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (abs(diffY) > abs(diffX)) {
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown()
                    } else {
                        onSwipeUp()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun onSwipeUp() {
        Log.i("MERCH_APP", "Gesture: Swipe UP -> Anant a Streamer Login")
        Toast.makeText(this, getString(R.string.mode_creator), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, StreamerLoginActivity::class.java)
        startActivity(intent)
    }

    private fun onSwipeDown() {
        Log.i("MERCH_APP", "Gesture: Swipe DOWN -> Anant a Viewer Login")
        Toast.makeText(this, getString(R.string.mode_viewer), Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ViewerLoginActivity::class.java)
        startActivity(intent)
    }
}