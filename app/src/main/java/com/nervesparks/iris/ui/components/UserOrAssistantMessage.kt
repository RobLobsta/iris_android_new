package com.nervesparks.iris.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nervesparks.iris.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserOrAssistantMessage(role: String, message: String, onLongClick: () -> Unit) {
    Row(
        horizontalArrangement = if (role == "user") Arrangement.End else Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        if (role == "assistant") MessageIcon(iconRes = R.drawable.logo, description = "Bot Icon")

        Box(
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .background(
                    color = if (role == "user") Color(0xFF171E2C) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = {},
                )
                .padding(8.dp),
        ) {
            Text(
                text = message.removePrefix("```"),
                style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFA0A0A5)),
                maxLines = 10,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (role == "user") MessageIcon(iconRes = R.drawable.user_icon, description = "User Icon")
    }
}

@Composable
private fun MessageIcon(iconRes: Int, description: String) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = description,
        modifier = Modifier.size(20.dp),
    )
}
