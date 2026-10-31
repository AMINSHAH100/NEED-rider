package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import need.rider.R
import com.example.data.local.entity.OrderEntity
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@Composable
fun ActiveDeliveryMap(
    order: OrderEntity?,
    progressFraction: Float, // 0.0f to 1.0f along route
    isHeadingToCustomer: Boolean,
    currentLat: Double = 43.5898,
    currentLng: Double = 10.4764,
    speedKmh: Float = 28f,
    isLiveGpsActive: Boolean = true,
    onSimulateStep: () -> Unit = {},
    onResetSimulation: () -> Unit = {},
    isBn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isSatelliteMode by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 46f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )
    val routeDashOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashOffset"
    )

    // Function to launch external normal navigation apps (Google Maps / Waze etc.)
    val openInExternalMapApp = {
        val destLat = if (!isHeadingToCustomer) (order?.restaurantLat ?: 43.5898) else (order?.customerLat ?: 43.5800)
        val destLng = if (!isHeadingToCustomer) (order?.restaurantLng ?: 10.4764) else (order?.customerLng ?: 10.4850)
        val destLabel = if (!isHeadingToCustomer) (order?.restaurantName ?: "Restaurant") else (order?.customerName ?: "Customer")

        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            try {
                val uri = Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng(${Uri.encode(destLabel)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                try {
                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                } catch (ex: Exception) {
                    Toast.makeText(context, if (isBn) "ম্যাপ অ্যাপ খুলতে সমস্যা হয়েছে" else "Could not open map app", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.6f), RoundedCornerShape(22.dp))
    ) {
        // 1. Map Layer (Satellite Photographic vs Standard Vector Map)
        if (isSatelliteMode) {
            // Satellite Aerial Photographic Texture
            Image(
                painter = painterResource(id = R.drawable.img_satellite_map),
                contentDescription = "Satellite Imagery Map",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Semi-transparent cinematic contrast gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF020617).copy(alpha = 0.60f),
                                Color(0xFF0F172A).copy(alpha = 0.20f),
                                Color(0xFF020617).copy(alpha = 0.70f)
                            )
                        )
                    )
            )
        }

        // 2. Dynamic GPS Overlay Canvas (Roads, Turn polyline, Radar pulse, GPS pins)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            if (!isSatelliteMode) {
                // High-visibility Clean Normal Navigation Street Map
                drawRect(
                    color = Color(0xFF1E293B), // Modern Slate-900 navigation background
                    size = Size(canvasW, canvasH)
                )

                // Green Urban Parks
                drawRoundRect(
                    color = Color(0xFF14532D).copy(alpha = 0.85f),
                    topLeft = Offset(canvasW * 0.08f, canvasH * 0.12f),
                    size = Size(canvasW * 0.28f, canvasH * 0.22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = Color(0xFF14532D).copy(alpha = 0.75f),
                    topLeft = Offset(canvasW * 0.65f, canvasH * 0.55f),
                    size = Size(canvasW * 0.28f, canvasH * 0.30f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                )

                // Water Canal / Gulshan Lake
                val waterPath = Path().apply {
                    moveTo(canvasW * 0.42f, 0f)
                    cubicTo(
                        canvasW * 0.46f, canvasH * 0.35f,
                        canvasW * 0.38f, canvasH * 0.65f,
                        canvasW * 0.48f, canvasH
                    )
                }
                drawPath(
                    path = waterPath,
                    color = Color(0xFF0284C7).copy(alpha = 0.75f),
                    style = Stroke(width = 24f, cap = StrokeCap.Round)
                )
            }

            // High-detail Road Overlays
            val roadAsphaltColor = if (isSatelliteMode) {
                Color(0xFF1E293B).copy(alpha = 0.45f)
            } else {
                Color(0xFF334155) // Crisp asphalt for Normal mode
            }

            val roadBorderColor = if (isSatelliteMode) {
                Color(0xFF64748B).copy(alpha = 0.30f)
            } else {
                Color(0xFF475569)
            }

            // Major Arterial Corridors (Kemal Ataturk & Gulshan Ave)
            val mainRoadsY = listOf(canvasH * 0.35f, canvasH * 0.75f)
            for (y in mainRoadsY) {
                drawLine(
                    color = roadBorderColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 22f
                )
                drawLine(
                    color = roadAsphaltColor,
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 18f
                )
                drawLine(
                    color = if (isSatelliteMode) Color(0xFFCBD5E1).copy(alpha = 0.35f) else Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            }

            val mainRoadsX = listOf(canvasW * 0.22f, canvasW * 0.78f)
            for (x in mainRoadsX) {
                drawLine(
                    color = roadBorderColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 22f
                )
                drawLine(
                    color = roadAsphaltColor,
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 18f
                )
                drawLine(
                    color = if (isSatelliteMode) Color(0xFFCBD5E1).copy(alpha = 0.35f) else Color(0xFFE2E8F0).copy(alpha = 0.6f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                )
            }

            // Secondary Intersecting streets
            val minorRoadsY = listOf(0.18f, 0.55f, 0.90f)
            for (i in minorRoadsY) {
                val y = canvasH * i
                drawLine(
                    color = if (isSatelliteMode) Color(0xFF475569).copy(alpha = 0.25f) else Color(0xFF334155).copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 8f
                )
            }

            val minorRoadsX = listOf(0.48f)
            for (i in minorRoadsX) {
                val x = canvasW * i
                drawLine(
                    color = if (isSatelliteMode) Color(0xFF475569).copy(alpha = 0.25f) else Color(0xFF334155).copy(alpha = 0.7f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 8f
                )
            }

            // Waypoints for Route Navigation
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

            // Outer Glow & Route Stroke
            val outerGlowColor = if (isSatelliteMode) Color(0xFF00E5FF).copy(alpha = 0.25f) else RiderPrimary.copy(alpha = 0.35f)
            drawPath(
                path = routePath,
                color = outerGlowColor,
                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = routePath,
                color = RiderPrimary.copy(alpha = 0.6f),
                style = Stroke(width = 16f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Core route line
            drawPath(
                path = routePath,
                color = Color.White,
                style = Stroke(width = 10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = routePath,
                color = RiderPrimary,
                style = Stroke(
                    width = 6f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), routeDashOffset)
                )
            )

            // Current Rider Interpolated Position
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
                color = Color.Black.copy(alpha = 0.5f),
                radius = 22f,
                center = pRest.copy(y = pRest.y + 4f)
            )
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 20f,
                center = pRest
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = pRest
            )

            // Customer Destination Marker
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 22f,
                center = pCust.copy(y = pCust.y + 4f)
            )
            drawCircle(
                color = OnlineGreen,
                radius = 20f,
                center = pCust
            )
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = pCust
            )

            // Rider GPS Radar Rings & Beacon
            drawCircle(
                color = RiderPrimary.copy(alpha = pulseAlpha * 0.4f),
                radius = pulseRadius * 2.2f,
                center = currentRiderPos
            )
            drawCircle(
                color = RiderPrimary.copy(alpha = pulseAlpha),
                radius = pulseRadius * 1.4f,
                center = currentRiderPos
            )
            // Outer white beacon ring
            drawCircle(
                color = Color.White,
                radius = 18f,
                center = currentRiderPos
            )
            // Inner vibrant rider dot
            drawCircle(
                color = RiderPrimary,
                radius = 13f,
                center = currentRiderPos
            )
            // Center directional core
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = currentRiderPos
            )
        }

        // 3. Top Navigation Heads-Up Card (HUD)
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.94f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = RiderPrimary,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Turn Navigation",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(7.dp)
                            .rotate(if (!isHeadingToCustomer) 0f else 90f)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (!isHeadingToCustomer) {
                            if (isBn) "রেস্তোরাঁর দিকে যান (Banani 11)" else "Head to Restaurant (Banani 11)"
                        } else {
                            if (isBn) "কাস্টমারের ঠিকানায় যান (Gulshan-1)" else "Head to Customer (Gulshan-1)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = if (!isHeadingToCustomer) {
                            if (isBn) "৩০০ মি পর ডানে কামাল আতাতুর্ক এভিনিউ" else "In 300m turn right onto Kemal Ataturk Ave"
                        } else {
                            if (isBn) "সোজা গুলশান এভিনিউ ধরে এগিয়ে চলুন" else "Continue straight onto Gulshan Ave"
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    val remainingKm = String.format("%.1f", (order?.distanceKm ?: 3.2) * (1f - progressFraction).coerceAtLeast(0.1f))
                    Text(
                        text = "$remainingKm km",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnlineGreen
                    )
                    Text(
                        text = "${((order?.estimatedMinutes ?: 15) * (1f - progressFraction)).toInt().coerceAtLeast(1)} min",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // 4. Prominent Map Mode Segmented Switcher & External Map App Launcher
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 64.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Segmented Map Toggle Pill: [ 🛰️ স্যাটেলাইট ] | [ 🗺️ সাধারণ ম্যাপ ]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Satellite Button
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (isSatelliteMode) Color(0xFF0284C7) else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { isSatelliteMode = true }
                    ) {
                        Text(
                            text = if (isBn) "🛰️ স্যাটেলাইট" else "🛰️ Satellite",
                            fontSize = 10.sp,
                            fontWeight = if (isSatelliteMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSatelliteMode) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Normal Vector Map Button
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (!isSatelliteMode) RiderPrimary else Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .clickable { isSatelliteMode = false }
                    ) {
                        Text(
                            text = if (isBn) "🗺️ সাধারণ" else "🗺️ Normal",
                            fontSize = 10.sp,
                            fontWeight = if (!isSatelliteMode) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isSatelliteMode) Color.White else Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Normal External Maps Button (Opens Google Maps / Phone's Navigation App)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { openInExternalMapApp() }
                    .testTag("open_external_map_app_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open in Normal Map App",
                        tint = OnlineGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "ম্যাপ অ্যাপ" else "Map App",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 5. Waypoint Labels floating on Map
        // Restaurant Tag
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 42.dp, y = 102.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isBn) "রেস্তোরাঁ পিকআপ" else "Pickup",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Customer Tag
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, OnlineGreen),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-26).dp, y = (-54).dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    tint = OnlineGreen,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isBn) "কাস্টমার ড্রপঅফ" else "Dropoff",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // 6. Bottom Map Controls & Simulation Action
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed indicator badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f)),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = OnlineGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (progressFraction > 0.05f && progressFraction < 0.98f) "29 km/h" else "0 km/h",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Live Hardware GPS Telemetry Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnlineGreen.copy(alpha = 0.6f)),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(OnlineGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GPS: ${String.format("%.4f", currentLat)}, ${String.format("%.4f", currentLng)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Real Google Maps Live Navigation Button
            Button(
                onClick = openInExternalMapApp,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("launch_live_turn_nav_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isBn) "গুগল ম্যাপে লাইভ ট্র্যাক" else "Google Maps Live Nav",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
