package com.wbrawner.plausible.android.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wbrawner.plausible.android.Plausible
import com.wbrawner.plausible.android.sample.ui.CustomEventScreen
import com.wbrawner.plausible.android.sample.ui.MainScreen
import com.wbrawner.plausible.android.sample.ui.theme.PlausibleAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlausibleAndroidTheme {
                LaunchedEffect(key1 = Unit) {

                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        Plausible.pageView(destination.route?: "/")
                    }
                    NavHost(navController = navController, startDestination = "/") {
                        composable("/") {
                            MainScreen(navController)
                        }
                        composable("/page") {
                            CustomEventScreen(navController)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Plausible.pageView("app://localhost/")
        Toast.makeText(this, "Sent pageview event", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PlausibleAndroidTheme {
    }
}