package at.weise.als

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AlsActivity : AppCompatActivity() {

    private var adrenalineInterval = 5
    private var ticks = 0
    private var analyses = 0
    private var shocks = 0
    private var adrenaline = 0
    private var amiodarone = 0
    private var amiodaroneDose = 0
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var cprCycles: TextView
    private lateinit var cprAlsAdrenaline: TextView
    private lateinit var cprAlsAmiodarone: TextView
    private lateinit var adrenalineButton: Button
    private lateinit var amiodaroneButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_als)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) /* keep screen awake */
        /* make timer available */
        cprCycles = findViewById(R.id.cycles)
        cprAlsAdrenaline = findViewById(R.id.als_adrenaline)
        cprAlsAmiodarone = findViewById(R.id.als_amiodarone)
        /* make buttons available */
        adrenalineButton = findViewById(R.id.adrenalin)
        amiodaroneButton = findViewById(R.id.amiodaron)
        /* vibrator */
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        /* better labels */
        adrenalineButton.text = buttonText(R.string.adrenaline_short, R.string.adrenaline_dose)
        amiodaroneButton.text = buttonText(R.string.amiodarone_short, R.string.amiodarone_dose_first)
        /* start the counter */
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    fun shock(view: View) {
        analyses++
        shocks++
        showStatistic()
        showALS()
    }

    fun noShock(view: View) {
        analyses++
        showStatistic()
        showALS()
    }

    fun adrenalinApplied(view: View) {
        adrenaline++
        showALS()
    }

    fun amiodaronApplied(view: View) {
        amiodarone++
        if (amiodarone == 1) {
            amiodaroneDose += 300
        } else {
            amiodaroneDose += 150
        }
        showALS()
    }

    private fun showStatistic() {
        cprCycles.visibility = View.VISIBLE
        var textShocks = getString(R.string.shock)
        if (shocks != 1) {
            textShocks = getString(R.string.shock_multiple)
        }
        runOnUiThread {
            cprCycles.text =
                "" + shocks + " " + textShocks + " (" + analyses + " " + getString(R.string.analyses) + ")"
        }
    }

    private fun buttonText(text: Int, dose: Int): Spanned {
        return Html.fromHtml(getString(text) + "<sup>" + getString(dose) + "</sup>")
    }

    private fun showALS() {
        /* text */
        if (amiodarone > 0) {
            cprAlsAmiodarone.visibility = View.VISIBLE
        }
        if (adrenaline > 0) {
            cprAlsAdrenaline.visibility = View.VISIBLE
        }
        /* buttons */
        if (shocks >= 3 && amiodarone != 2) {
            findViewById<Button>(R.id.amiodaron).visibility = View.VISIBLE
        }
        if (amiodarone == 2) {
            findViewById<Button>(R.id.amiodaron).visibility = View.INVISIBLE
        }
        if (analyses > 0 && adrenaline != 10) {
            findViewById<Button>(R.id.adrenalin).visibility = View.VISIBLE
        }
        if (adrenaline == 10) {
            findViewById<Button>(R.id.adrenalin).visibility = View.INVISIBLE
        }
        runOnUiThread {
            if (adrenaline > 0) {
                cprAlsAdrenaline.text =
                    "" + adrenaline + "x " + getString(R.string.adrenaline) + " (" + adrenaline + "mg)"
            }
            if (amiodarone > 0) {
                cprAlsAmiodarone.text =
                    "" + amiodarone + "x " + getString(R.string.amiodarone) + " (" + amiodaroneDose + "mg)"
                amiodaroneButton.text = buttonText(R.string.amiodarone_short, R.string.amiodarone_dose_second)
            }
        }
    }

    private fun startTimer() {
        timer = fixedRateTimer(name = "CPR-Timer", initialDelay = 0, period = 1000) {
            if (ticks != 0 && ticks % adrenalineInterval == 0) {
                vibrate()
            }
            runOnUiThread {
                title = formatTime(ticks)
            }
            ticks++
        }
    }

    private fun vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = Math.floorDiv(totalSeconds, 60)
        val seconds = totalSeconds - minutes * 60
        return getString(R.string.cpr) + String.format(" %02d", minutes) + ":" + String.format("%02d", seconds)
    }

    private fun stopTimer() {
        timer.cancel()
    }
}