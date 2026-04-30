package edu.bu.cs411.group10.curre.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.bu.cs411.group10.curre.ui.model.RoutePointDto
import edu.bu.cs411.group10.curre.ui.model.RunDto
import edu.bu.cs411.group10.curre.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Marker
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.Paint

// Date formatting
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main screen showing details of a single run
 * Includes:
 * - Date
 * - Route preview
 * - Metrics (duration, pace, calories, miles)
 */
@Composable
fun RunDetailsScreen(
    run: RunDto,
    onBackClick: () -> Unit
) {
    // Format timestamp → readable date string
    val fullDate = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        .format(Date(run.startedAt))

    // Convert total duration into minutes + seconds
    val durationMinutes = run.durationSeconds / 60
    val durationSeconds = run.durationSeconds % 60

    // Convert pace (seconds per mile) → mm:ss format
    val avgPaceMinutes = (run.avgPaceSecsPerMile / 60).toInt()
    val avgPaceSeconds = (run.avgPaceSecsPerMile % 60).toInt()

    // Scaffold = base layout container
    Scaffold(
        containerColor = CurreBackground
    ) { innerPadding ->

        // Scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CurreBackground)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // HEADER
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Back button
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = CurreNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title + date
                    Column {
                        Text(
                            text = "Run Details",
                            color = CurreNavy,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        )
                        Text(
                            text = fullDate,
                            color = CurreTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // ROUTE CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CurreSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        // Top row (title + distance)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = "Route",
                                    tint = CurreOrange
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Route",
                                    color = CurreNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Distance badge (top right)
                            Box(
                                modifier = Modifier
                                    .background(CurreLime, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = String.format("%.2f Mi", run.distanceMiles),
                                    color = CurreNavy,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Draw route preview
                        RoutePreview(routePoints = run.routePoints)
                    }
                }
            }

            // TOP METRICS (Duration + Pace)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Schedule,
                        iconTint = Color.Black,
                        value = "${durationMinutes}m ${durationSeconds}s",
                        label = "Duration",
                        backgroundColor = CurreStreakCard,
                        contentColor = CurreNavy
                    )

                    DetailMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Bolt,
                        iconTint = Color.White,
                        value = String.format("%d:%02d", avgPaceMinutes, avgPaceSeconds),
                        label = "Avg Pace (min/mi)",
                        backgroundColor = CurreOrange,
                        contentColor = Color.White
                    )
                }
            }

            // BOTTOM METRICS (Calories + Miles)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconTint = CurreLime,
                        value = run.calories.toString(),
                        label = "Calories Burned",
                        backgroundColor = CurreSurface,
                        contentColor = CurreNavy
                    )

                    DetailMetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.LocationOn,
                        iconTint = CurreOrange,
                        value = String.format("%.2f", run.distanceMiles),
                        label = "Miles",
                        backgroundColor = CurreSurface,
                        contentColor = CurreNavy
                    )
                }
            }
        }
    }
}

/**
 * Reusable metric card used for all stats (duration, calories, etc.)
 */
@Composable
private fun DetailMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Icon at top
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            // Main value (big text)
            Text(
                text = value,
                color = contentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )

            // Label text
            Text(
                text = label,
                color = if (contentColor == Color.White) Color.White else CurreTextMuted,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Draws the route using OpenStreetMap (osmdroid) from GPS points
 */
@Composable
private fun RoutePreview(
    routePoints: List<RoutePointDto>
) {
    val context = LocalContext.current
    Configuration.getInstance().userAgentValue = context.packageName

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CurreSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // If not enough data, show message
        if (routePoints.size < 2) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Not enough route data to display.",
                    color = CurreTextMuted
                )
            }
        } else {
            // Draw route using OpenStreetMap
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setMultiTouchControls(true)

                        // 1. Group the flat list back into sub-lists based on the segmentIndex
                        val groupedSegments = routePoints.groupBy { it.segmentIndex }.values

                        // 2. Loop through each segment and draw an independent Polyline
                        groupedSegments.forEach { segmentPoints ->
                            if (segmentPoints.size >= 2) {
                                val segmentGeoPoints = segmentPoints.map { GeoPoint(it.latitude, it.longitude) }

                                val routeLine = Polyline().apply {
                                    setPoints(segmentGeoPoints)
                                    outlinePaint.color = "#FF5722".toColorInt() // CurreOrange
                                    outlinePaint.strokeWidth = 15f
                                    outlinePaint.strokeCap = Paint.Cap.ROUND
                                    outlinePaint.strokeJoin = Paint.Join.ROUND
                                }
                                overlays.add(routeLine)
                            }
                        }

                        // 3. Create a flat list of GeoPoints just for the bounding box and markers
                        val allGeoPoints = routePoints.map { GeoPoint(it.latitude, it.longitude) }

                        if (allGeoPoints.isNotEmpty()) {
                            // Helper function to create a circular dot
                            fun createDotDrawable(colorHex: String): ShapeDrawable {
                                return ShapeDrawable(OvalShape()).apply {
                                    intrinsicWidth = 40
                                    intrinsicHeight = 40
                                    paint.color = colorHex.toColorInt()
                                    paint.style = Paint.Style.FILL
                                }
                            }

                            // Add Start and End Markers
                            val startMarker = Marker(this).apply {
                                position = allGeoPoints.first()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                icon = createDotDrawable("#CDDC39") // CurreLime
                            }

                            val endMarker = Marker(this).apply {
                                position = allGeoPoints.last()
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                icon = createDotDrawable("#FF5722") // CurreOrange
                            }

                            overlays.add(startMarker)
                            overlays.add(endMarker)

                            // Center and zoom the map
                            val boundingBox = BoundingBox.fromGeoPoints(allGeoPoints)
                            post {
                                zoomToBoundingBox(boundingBox, false, 200)
                            }
                        }
                    }
                }
            )
        }
    }
}