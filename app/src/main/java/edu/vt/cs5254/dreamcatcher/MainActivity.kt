package edu.vt.cs5254.dreamcatcher

import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.vt.cs5254.dreamcatcher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //Name: Dhruveel Chouhan
    //pid: dhruveel10

    private  lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}