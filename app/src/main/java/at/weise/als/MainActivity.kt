package at.weise.als

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Start CPR
    fun startCpr(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}