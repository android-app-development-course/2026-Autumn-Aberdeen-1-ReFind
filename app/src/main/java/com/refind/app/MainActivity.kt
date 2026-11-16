package com.refind.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.refind.app.ui.AppLanguage
import com.refind.app.ui.ReFindApp
import com.refind.app.ui.theme.ReFindTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppLanguage.init(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ReFindTheme {
                ReFindApp()
            }
        }
    }
}
