package com.example.rinconsur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.rinconsur.navigation.AppNavegacion
import com.example.rinconsur.ui.theme.RinconSurTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RinconSurTheme {
                AppNavegacion()
            }
        }
    }
}
