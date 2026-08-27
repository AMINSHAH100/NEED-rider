package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary
import kotlin.math.atan2

@Composable
fun ActiveDeliveryMap(
    order: OrderEntity?,
    progressFraction: Float, // 0.0f to 1.0f along route
    isHeadingToCustomer: Boolean,
    onSimulateStep: () -> Unit,
    onResetSimulation: () -> Unit,
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E293B))
    ) {
        // Dynamic Canvas Map
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // Background city grid blocks
            drawRect(
                color = Color(0xFF0F172A),
                size = Size(canvasW, canvasH)
            )

            // Green park blocks
            drawRoundRect(
                color = Color(0xFF064E3B).copy(alpha = 0.45f),
                topLeft = Offset(canvasW * 0.08f, canvasH * 0.12f),
                size = Size(canvasW * 0.28f, canvasH * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
            drawRoundRect(
                color = Color(0xFF064E3B).copy(alpha = 0.35f),
                topLeft = Offset(canvasW * 0.65f, canvasH * 0.55f),
                size = Size(canvasW * 0.28f, canvasH * 0.30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )

            // Lake / Canal
            val waterPath = Path().apply {
                moveTo(canvasW * 0.40f, 0f)
                cubicTo(
                    canvasW * 0.45f, canvasH * 0.3f,
                    canvasW * 0.35f, canvasH * 0.7f,
                    canvasW * 0.50f, canvasH
                )
            }
            drawPath(
                path = waterPath,
                color = Color(0xFF0284C7).copy(alpha = 0.30f),
                style = Stroke(width = 28f, cap = StrokeCap.Round)
            )

            // Secondary street network
            val minorRoadColor = Color(0xFF334155).copy(alpha = 0.6f)
            for (i in 1..5) {
                val y = canvasH * (i / 6f)
                drawLine(
                    color = minorRoadColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 6f
                )
            }
            for (i in 1..4) {
                val x = canvasW * (i / 5f)
                drawLine(
                    color = minorRoadColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 6f
                )
            }

            // Main Avenues (Avenue 1 & Kemal Ataturk)
            val mainRoadColor = Color(0xFF475569)
            drawLine(
                color = mainRoadColor,
                start = Offset(0f, canvasH * 0.35f),
                end = Offset(canvasW, canvasH * 0.35f),
                strokeWidth = 16f
            )
            drawLine(
                color = mainRoadColor,
                start = Offset(0f, canvasH * 0.75f),
                end = Offset(canvasW, canvasH * 0.75f),
                strokeWidth = 16f
            )
            drawLine(
                color = mainRoadColor,
                start = Offset(canvasW * 0.22f, 0f),
                end = Offset(canvasW * 0.22f, canvasH),
                strokeWidth = 16f
            )
            drawLine(
                color = mainRoadColor,
                start = Offset(canvasW * 0.78f, 0f),
                end = Offset(canvasW * 0.78f, canvasH),
                strokeWidth = 16f
            )

            // Waypoints for Route
            val pRiderStart = Offset(canvasW * 0.22f, canvasH * 0.85f)
            val pRest = Offset(canvasW * 0.22f, canvasH * 0.35f)
            val pCorner1 = Offset(canvasW * 0.78f, canvasH * 0.35f)
            val pCust = Offset(canvasW * 0.78f, canvasH * 0.75f)

            val fullWaypoints = if (!isHeadingToCustomer) {
                listOf(pRiderStart, pRest)
            } else {
                listOf(pRest, pCorner1, pCust)
            }

            // Draw Route Polyline
            val routePath = Path().apply {
                moveTo(fullWaypoints.first().x, fullWaypoints.first().y)
                for (pt in fullWaypoints.drop(1)) {
                    lineTo(pt.x, pt.y)
                }
            }

            // Outer route glow
            drawPath(
                path = routePath,
                color = RiderPrimary.copy(alpha = 0.35f),
                style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Core route line
            drawPath(
                path = routePath,
                color = RiderPrimary,
                style = Stroke(
                    width = 8f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(25f, 15f), 0f)
                )
            )

            // Compute current interpolated Rider position
            val totalSegments = fullWaypoints.size - 1
            val clampedFraction = progressFraction.coerceIn(0f, 1f)
            val segmentIndex = (clampedFraction * totalSegments).toInt().coerceAtMost(totalSegments - 1)
            val segmentFraction = (clampedFraction * totalSegments) - segmentIndex

            val pA = fullWaypoints[segmentIndex]
            val pB = fullWaypoints[segmentIndex + 1]
            val currentRiderPos = Offset(
                x = pA.x + (pB.x - pA.x) * segmentFraction,
                y = pA.y + (pB.y - pA.y) * segmentFraction
            )

            // Restaurant Marker
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 18f,
                center = pRest
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = pRest
            )

            // Customer Marker
            drawCircle(
                color = OnlineGreen,
                radius = 18f,
                center = pCust
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = pCust
            )

            // Rider Radar Pulse & Marker
            drawCircle(
                color = RiderPrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius * 1.5f,
                center = currentRiderPos
            )
            drawCircle(
                color = Color.White,
                radius = 16f,
                center = currentRiderPos
            )
            drawCircle(
                color = RiderPrimary,
                radius = 12f,
                center = currentRiderPos
            )
        }

        // Top Navigation Heads-Up Card (HUD)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = RiderPrimary,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Turn Navigation",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!isHeadingToCustomer) {
                            if (isBn) "রেস্তোরাঁর দিকে এগিয়ে যান (Banani 11)" else "Head to Restaurant (Banani 11)"
                        } else {
                            if (isBn) "কাস্টমারের ঠিকানায় যান (Gulshan-1)" else "Head to Customer (Gulshan-1)"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (!isHeadingToCustomer) {
                            if (isBn) "৩০০ মি পর ডান দিকে মোড় নিন" else "In 300m turn right onto Kemal Ataturk"
                        } else {
                            if (isBn) "সোজা এগিয়ে যান গুলশান এভিনিউতে" else "Continue straight onto Gulshan Ave"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val remainingKm = String.format("%.1f", (order?.distanceKm ?: 3.2) * (1f - progressFraction).coerceAtLeast(0.1f))
                    Text(
                        text = "$remainingKm km",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnlineGreen
                    )
                    Text(
                        text = "${((order?.estimatedMinutes ?: 15) * (1f - progressFraction)).toInt().coerceAtLeast(1)} min",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // Bottom Map Controls & Simulation Action
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed indicator badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = OnlineGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (progressFraction > 0.05f && progressFraction < 0.98f) "29 km/h" else "0 km/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Simulate Movement Step Button
            Button(
                onClick = onSimulateStep,
                colors = ButtonDefaults.buttonColors(containerColor = RiderPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("simulate_bike_step_button")
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBn) "ম্যাপে এগোন (+২৫%)" else "Advance Bike (+25%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Reset
            IconButton(
                onClick = onResetSimulation,
                modifier = Modifier
                    .background(Color(0xFF0F172A).copy(alpha = 0.9f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Route",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
