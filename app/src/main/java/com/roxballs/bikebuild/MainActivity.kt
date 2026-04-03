package com.roxballs.bikebuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.roxballs.bikebuild.ui.theme.BikeBuilderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BikeBuilderTheme {
                BikeBuilderApp()
            }
        }
    }
}

