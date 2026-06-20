package com.kosmos.android.alert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kosmos.android.ui.designsystem.tokens.KosmosTheme

class RainAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(EXTRA_TITLE)
            ?: "Rain in about an hour"
        val body = intent.getStringExtra(EXTRA_BODY)
            ?: "Carry cover if you head out."

        setContent {
            KosmosTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCC0A0B1A)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                            .background(Color(0xFF1E2030), androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("🌧", fontSize = 36.sp)
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White,
                        )
                        Text(
                            text = body,
                            fontSize = 15.sp,
                            color = Color(0xFFB0B0C8),
                            lineHeight = 22.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Got it")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_TITLE = "alert_title"
        private const val EXTRA_BODY = "alert_body"

        fun intent(context: Context, title: String, body: String): Intent =
            Intent(context, RainAlertActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_BODY, body)
            }

        fun previewIntent(context: Context): Intent = intent(
            context,
            title = "Rain in about an hour",
            body = "Heavy rain likely — grab a raincoat, not an umbrella. Roads turn slippery fast.",
        )
    }
}
