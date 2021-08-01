package at.weise.als

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AlsActivity : FragmentActivity() {

    private var ticks = 0
    private var analyses = 0
    private var shocks = 0
    private var adrenaline = 0
    private var amiodarone = 0
    private lateinit var timer: Timer
    private lateinit var cprTime: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var cprCycles: TextView
    private lateinit var cprAlsAdrenaline: TextView
    private lateinit var cprAlsAmiodarone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_als);
        /* make timer available */
        cprTime = findViewById(R.id.timer)
        cprCycles = findViewById(R.id.cycles)
        cprAlsAdrenaline = findViewById(R.id.als_adrenaline)
        cprAlsAmiodarone = findViewById(R.id.als_amiodarone)
        /* vibrator */
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        /* better labels */
        findViewById<Button>(R.id.amiodaron).setText(Html.fromHtml("Amio.<sup>150mg</sup>"))
        findViewById<Button>(R.id.adrenalin).setText(Html.fromHtml("Adr.<sup>1mg</sup>"))
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
        showALS()
    }

    private fun showStatistic() {
        cprCycles.visibility = View.VISIBLE
        var textShocks = getString(R.string.shock)
        if (shocks != 1) {
            textShocks = getString(R.string.shock_multiple)
        }
        runOnUiThread {
            cprCycles.text = "" + shocks + " " + textShocks + " (+" + (analyses - shocks) + ")"
        }
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
        if (shocks >= 3 && amiodarone != 3) {
            findViewById<Button>(R.id.amiodaron).visibility = View.VISIBLE
        }
        if (amiodarone == 3) {
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
                cprAlsAdrenaline.text = "" + adrenaline + "x " + getString(R.string.adrenaline_short) + " (" + adrenaline + "mg)"
            }
            if (amiodarone > 0) {
                cprAlsAmiodarone.text = "" + amiodarone + "x " + getString(R.string.amiodarone_short) + " (" + (amiodarone * 150) + "mg)"
            }
        }
    }

    private fun startTimer() {
        timer = fixedRateTimer(name = "CPR-Timer", initialDelay = 0, period = 1000) {
            ticks++
            if (ticks != 0 && ticks % (60*4) == 0) {
                vibrate()
            }
            runOnUiThread {
                cprTime.text = formatTime(ticks)
            }
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