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
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AlsActivity : AppCompatActivity() {

    private var adrenalineInterval = 5
    private var CHANNEL_ID = "CHANNEL_ALS"
    private var ticks = 0
    private var analyses = 0
    private var shocks = 0
    private var adrenaline = 0
    private var amiodarone = 0
    private lateinit var channel: NotificationChannel
    private lateinit var timer: Timer
    private lateinit var vibrator: Vibrator
    private lateinit var cprCycles: TextView
    private lateinit var cprAlsAdrenaline: TextView
    private lateinit var cprAlsAmiodarone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_als);
        /* make timer available */
        cprCycles = findViewById(R.id.cycles)
        cprAlsAdrenaline = findViewById(R.id.als_adrenaline)
        cprAlsAmiodarone = findViewById(R.id.als_amiodarone)
        channel = NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "description"
        /* vibrator */
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        /* better labels */
        findViewById<Button>(R.id.amiodaron).setText(Html.fromHtml(resources.getString(R.string.amiodarone_short) + "<sup>150mg</sup>"))
        findViewById<Button>(R.id.adrenalin).setText(Html.fromHtml(resources.getString(R.string.adrenaline_short) + "<sup>1mg</sup>"))
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
            cprCycles.text =
                "" + shocks + " " + textShocks + " (" + analyses + " " + resources.getString(R.string.analyses) + ")"
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
                cprAlsAdrenaline.text =
                    "" + adrenaline + "x " + getString(R.string.adrenaline) + " (" + adrenaline + "mg)"
            }
            if (amiodarone > 0) {
                cprAlsAmiodarone.text =
                    "" + amiodarone + "x " + getString(R.string.amiodarone) + " (" + (amiodarone * 150) + "mg)"
            }
        }
    }

    private fun startTimer() {
        timer = fixedRateTimer(name = "CPR-Timer", initialDelay = 0, period = 1000) {
            if (ticks != 0 && ticks % adrenalineInterval == 0) {
                vibrate()
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                val mBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background) // notification icon
                    .setContentTitle("Notification!") // title for notification
                    .setContentText("Hello word") // message for notification
                    .setAutoCancel(true) // clear notification after click

                val intent = Intent(applicationContext, AlsActivity::class.java)
                val pi: PendingIntent =
                    PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mBuilder.setContentIntent(pi)
                notificationManager.notify(1, mBuilder.build())
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