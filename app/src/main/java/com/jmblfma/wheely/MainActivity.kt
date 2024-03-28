package com.jmblfma.wheely

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.jmblfma.wheely.databinding.ActivityMainBinding
import de.hdodenhof.circleimageview.BuildConfig
import org.osmdroid.config.Configuration

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // sets the user agent to comply with OpenStreetMaps usage policy
        Configuration.getInstance().userAgentValue = applicationContext.packageName
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val intent = Intent(applicationContext, HomePageActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }
}