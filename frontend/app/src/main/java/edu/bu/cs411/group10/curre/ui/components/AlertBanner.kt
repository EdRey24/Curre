package edu.bu.cs411.group10.curre.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.theme.CurreNavy
import edu.bu.cs411.group10.curre.ui.theme.CurreOrange
import edu.bu.cs411.group10.curre.ui.theme.CurreOrangeSoft
import edu.bu.cs411.group10.curre.ui.theme.CurreTextMuted

// Displays a safety/emergency banner on the home screen
@Composable
fun AlertBanner(
    contactsCount: Int,
    title: String = "Emergency Alert Ready",
    subtitle: String = "Stay safe on every adventure!"
) {
    // outer card container
    Card(
        shape = RoundedCornerShape(20.dp), // rounded corners
        colors = CardDefaults.cardColors(containerColor = CurreOrangeSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Horizontal layout inside the card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // left spacing
            Spacer(modifier = Modifier.width(18.dp))

            // warning icon
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = "Emergency alert",
                tint = CurreOrange
            )

            // space between icon and text
            Spacer(modifier = Modifier.width(14.dp))

            // text section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // title text
                Text(
                    text = title,
                    color = CurreNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))

                // subtitle text
                Text(
                    text = subtitle,
                    color = CurreTextMuted,
                    fontSize = 14.sp
                )
            }

            // circle badge showing number of emergency contacts
            Box(
                modifier = Modifier
                    .size(32.dp) // circle size
                    .clip(CircleShape)
                    .background(CurreOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contactsCount.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // right spacing
            Spacer(modifier = Modifier.width(18.dp))
        }
    }
}