package app.clauncher.helper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.clauncher.R

class FakeHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
        setContentView(R.layout.activity_fake_home)
    }
}