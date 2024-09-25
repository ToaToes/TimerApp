package com.example.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.concurrent.timer
//permission
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.ActivityCompat
import androidx.compose.ui.res.vectorResource
import androidx.core.graphics.drawable.IconCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
          TimerApp()
        }
    }
}


@Composable
fun TimerApp(){

    val isDarkTheme = isSystemInDarkTheme()

    MaterialTheme(
        colorScheme =
        if(isDarkTheme)
            darkColorScheme(
                primary = Color(0xFF212121),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF424242),
                onSecondary = Color(0xFFFFFFFF),
            ) else
                lightColorScheme(
            primary = Color(0xFFE0E0E0),
            onPrimary = Color(0xFF000000),
            secondary = Color(0xFFE0E0E0),
            onSecondary = Color(0xFF000000),

            )

    ){
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary
        ){
            StopwatchScreen()
        }
    }

}

@Composable
fun StopwatchScreen(){
    var time by remember{mutableStateOf(0L)}
    var isRunning by remember{mutableStateOf(false)}
    //Added
    var laps by remember { mutableStateOf(listOf<Long>()) }
    //providing a coroutine scope that is tied to the lifecycle of the composable
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isRunning){
        if(isRunning){
            while(isRunning){
                delay(1000L)
                time += 1L
            }
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text(
            text = formatTime(time),
            fontSize = 48.sp,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){

            Button(
                onClick = {
                    if(isRunning){
                        isRunning = false
                    } else {
                        coroutineScope.launch{
                            isRunning = true
                        }
                    }
                },
                modifier = Modifier
                    .neumorphism()
                    .padding(8.dp)
            ){
                Text(text = if(isRunning) "Pause" else "Start")
            }
            Button(
                onClick = {
                    isRunning = false
                    time = 0L
                    laps = listOf()
                },
                modifier = Modifier
                    .neumorphism()
                    .padding(8.dp)
            ){
                Text(text = "Reset")
            }
            Button(
                onClick = {
                    if (isRunning) {
                        laps = laps + time
                    }
                },
                modifier = Modifier
                    .neumorphism()
                    .padding(8.dp)
            ) {
                Text(text = "Lap")
            }


        }
        LazyColumn {
            items(laps) { lapTime ->
                Text(
                    text = formatTime(lapTime),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


fun formatTime(time: Long): String{
    val seconds = time % 60
    val minutes = (time/60)%60
    val hours = (time/3600)
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Composable
fun Modifier.neumorphism(
    color: Color = Color(0xFFE0E0E0),
    shadowColor: Color = Color(0xFFBEBEBE),
    lightShadowColor: Color = Color(0xFFFFFFFF),
    borderRadius: Int = 12,
    shadowBlurRadius: Int = 8
): Modifier {
    return this
        .shadow(
            elevation = 8.dp,
            shape = RoundedCornerShape(borderRadius),
            ambientColor = shadowColor,
            spotColor = lightShadowColor
        )
        .background(color = color, shape = RoundedCornerShape(borderRadius))
        .border(width = 1.dp, color = lightShadowColor, shape = RoundedCornerShape(borderRadius))
        .border(width = 1.dp, color = shadowColor, shape = RoundedCornerShape(borderRadius))
}

// Add these dependencies in your build.gradle file
// implementation "androidx.core:core-ktx:1.6.0"
// implementation "androidx.work:work-runtime-ktx:2.7.1"
fun showNotification(context: Context, message: String) {
    val notificationId = 1
    val channelId = "timer_notification_channel"
    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_timer)
        .setContentTitle("Timer Notification")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notify(notificationId, builder.build())
    }
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Timer Channel"
        val descriptionText = "Channel for timer notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("timer_notification_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}





@Preview(showBackground = true)
@Composable
fun StopwatchPreview(){
    TimerApp()
}



