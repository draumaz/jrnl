/*
 * Copyright (c) 2022-2023. Isaak Hanimann.
 * This file is part of PsychonautWiki Journal.
 *
 * PsychonautWiki Journal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * PsychonautWiki Journal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PsychonautWiki Journal.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 */

package com.isaakhanimann.journal.ui.tabs.journal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.isaakhanimann.journal.R
import com.isaakhanimann.journal.data.room.experiences.ExperienceRepository
import com.isaakhanimann.journal.data.room.experiences.entities.AdaptiveColor
import com.isaakhanimann.journal.data.room.experiences.relations.IngestionWithCompanionAndCustomUnit
import com.isaakhanimann.journal.data.substances.classes.roa.RoaDuration
import com.isaakhanimann.journal.data.substances.repositories.SearchRepository
import com.isaakhanimann.journal.ui.tabs.journal.experience.components.DataForOneEffectLine
import com.isaakhanimann.journal.ui.tabs.journal.experience.timeline.AllTimelinesModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

private const val LIVE_UPDATE_CHANNEL_ID = "live_update_channel"
private const val LIVE_UPDATE_NOTIFICATION_ID = 1

data class LiveUpdateModel(
    val ingestionWithCompanion: IngestionWithCompanionAndCustomUnit,
    val timelineModel: AllTimelinesModel,
    val duration: RoaDuration,
)

@Singleton
class LiveUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val experienceRepo: ExperienceRepository,
    private val searchRepository: SearchRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private val tickerFlow: Flow<Instant> = flow {
        while (true) {
            emit(Instant.now())
            delay(60000) // update every minute
        }
    }

    val liveUpdateFlow = experienceRepo.getSortedIngestionsWithSubstanceCompanionsFlow(limit = 1)
        .combine(tickerFlow) { ingestions, now ->
            val latest = ingestions.firstOrNull() ?: return@combine null
            val substance =
                searchRepository.substanceRepo.getSubstance(latest.ingestion.substanceName)
            val roa = substance?.getRoa(latest.ingestion.administrationRoute)
            val duration = roa?.roaDuration ?: return@combine null

            // Check if active: within total duration + some buffer (e.g. 2 hours afterglow)
            val totalMaxSeconds = duration.total?.maxInSec ?: (duration.peak?.maxInSec ?: 3600f * 4)
            val afterglowMaxSeconds = duration.afterglow?.maxInSec ?: 3600f * 4

            val totalActiveSeconds = totalMaxSeconds + afterglowMaxSeconds
            val elapsedSeconds = Duration.between(latest.ingestion.time, now).seconds

            if (elapsedSeconds < 0 || elapsedSeconds > totalActiveSeconds) {
                return@combine null
            }

            val timelineModel = AllTimelinesModel(
                dataForLines = listOf(
                    DataForOneEffectLine(
                        substanceName = latest.ingestion.substanceName,
                        route = latest.ingestion.administrationRoute,
                        roaDuration = duration,
                        height = 1f,
                        horizontalWeight = 1f,
                        color = latest.substanceCompanion?.color ?: AdaptiveColor.TEAL,
                        startTime = latest.ingestion.time,
                        endTime = latest.ingestion.endTime
                    )
                ),
                dataForRatings = emptyList(),
                timedNotes = emptyList(),
                areSubstanceHeightsIndependent = true
            )

            return@combine LiveUpdateModel(
                ingestionWithCompanion = latest,
                timelineModel = timelineModel,
                duration = duration
            )
        }.stateIn(
            initialValue = null,
            scope = scope,
            started = SharingStarted.Eagerly
        )

    fun setup() {
        createNotificationChannel()
        scope.launch {
            liveUpdateFlow.collect { liveUpdate ->
                if (liveUpdate != null) {
                    showNotification(liveUpdate)
                } else {
                    cancelNotification()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "Live Updates"
        val descriptionText = "Shows active substance ingestions"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(LIVE_UPDATE_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            setShowBadge(false)
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(liveUpdate: LiveUpdateModel) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val now = Instant.now()
        val startTime = liveUpdate.ingestionWithCompanion.ingestion.time
        val duration = liveUpdate.duration

        val onsetSec = duration.onset?.maxInSec ?: 0f
        val comeupSec = duration.comeup?.maxInSec ?: 0f
        val peakSec = duration.peak?.maxInSec ?: 0f
        val offsetSec = duration.offset?.maxInSec ?: 0f

        val onsetEnd = startTime.plusSeconds(onsetSec.toLong())
        val peakStart = onsetEnd.plusSeconds(comeupSec.toLong())
        val peakEnd = peakStart.plusSeconds(peakSec.toLong())
        val offsetEnd = peakEnd.plusSeconds(offsetSec.toLong())

        val phaseName = when {
            now.isBefore(onsetEnd) -> "Onset"
            now.isBefore(peakStart) -> "Comeup"
            now.isBefore(peakEnd) -> "Peak"
            now.isBefore(offsetEnd) -> "Offset"
            else -> "Afterglow"
        }

        val substanceName = liveUpdate.ingestionWithCompanion.ingestion.substanceName
        
        val remoteViews = RemoteViews(context.packageName, R.layout.notification_live_update)
        remoteViews.setTextViewText(R.id.notification_title, substanceName)
        remoteViews.setTextViewText(R.id.notification_phase, phaseName)
        
        val graphBitmap = drawTimelineGraph(liveUpdate)
        remoteViews.setImageViewBitmap(R.id.notification_graph, graphBitmap)

        val notification = NotificationCompat.Builder(context, LIVE_UPDATE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCustomContentView(remoteViews)
            .setCustomBigContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(LIVE_UPDATE_NOTIFICATION_ID, notification)
    }

    private fun drawTimelineGraph(liveUpdate: LiveUpdateModel): Bitmap {
        val width = 1000
        val height = 300
        val paddingHorizontal = 80f
        val paddingVertical = 60f
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        val startTime = liveUpdate.ingestionWithCompanion.ingestion.time
        val duration = liveUpdate.duration
        
        val onsetSec = duration.onset?.maxInSec ?: 0f
        val comeupSec = duration.comeup?.maxInSec ?: 0f
        val peakSec = duration.peak?.maxInSec ?: 0f
        val offsetSec = duration.offset?.maxInSec ?: 0f
        val afterglowSec = duration.afterglow?.maxInSec ?: (3600f * 4) // Default 4h afterglow for visualization
        
        val totalSec = onsetSec + comeupSec + peakSec + offsetSec + afterglowSec
        val pixelsPerSec = (width - 2 * paddingHorizontal) / totalSec
        
        val drawHeight = height - 2 * paddingVertical
        val baseLineY = height - paddingVertical
        
        val adaptiveColor = liveUpdate.ingestionWithCompanion.substanceCompanion?.color ?: AdaptiveColor.TEAL
        val paintColor = adaptiveColor.getComposeColor(false).toArgb()
        
        val path = Path()
        val strokePaint = Paint().apply {
            this.color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            pathEffect = CornerPathEffect(30f)
        }
        
        val fillPaint = Paint().apply {
            this.color = paintColor
            alpha = 40
            style = Paint.Style.FILL
            isAntiAlias = true
            pathEffect = CornerPathEffect(30f)
        }

        // Points
        val x0 = paddingHorizontal
        val x1 = x0 + onsetSec * pixelsPerSec
        val x2 = x1 + comeupSec * pixelsPerSec
        val x3 = x2 + peakSec * pixelsPerSec
        val x4 = x3 + offsetSec * pixelsPerSec
        val x5 = x4 + afterglowSec * pixelsPerSec
        
        // Main path (Ingestion to Offset End)
        path.moveTo(x0, baseLineY)
        path.lineTo(x1, baseLineY)
        path.lineTo(x2, paddingVertical)
        path.lineTo(x3, paddingVertical)
        path.lineTo(x4, baseLineY)
        
        canvas.drawPath(path, strokePaint)
        
        // Fill
        val fillPath = Path(path)
        fillPath.lineTo(x4, baseLineY)
        fillPath.lineTo(x0, baseLineY)
        fillPath.close()
        canvas.drawPath(fillPath, fillPaint)
        
        // Afterglow (dotted)
        val afterglowPaint = Paint(strokePaint).apply {
            pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
        }
        canvas.drawLine(x4, baseLineY, x5, baseLineY, afterglowPaint)
        
        // Markers for stages
        val markerPaint = Paint().apply {
            this.color = paintColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x0, baseLineY, 12f, markerPaint) // Ingestion
        
        // Current time marker
        val now = Instant.now()
        val elapsed = Duration.between(startTime, now).seconds
        if (elapsed in 0..totalSec.toLong()) {
            val nowX = x0 + elapsed * pixelsPerSec
            val nowMarkerPaint = Paint().apply {
                this.color = android.graphics.Color.parseColor("#FF5252") // M3 Error/Attention Red
                strokeWidth = 5f
                isAntiAlias = true
            }
            canvas.drawLine(nowX, 0f, nowX, height.toFloat(), nowMarkerPaint)
            
            val nowLabelPaint = Paint().apply {
                this.color = nowMarkerPaint.color
                textSize = 28f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            canvas.drawText("NOW", nowX, 30f, nowLabelPaint)
        }
        
        // Time Labels
        val textPaint = Paint().apply {
            this.color = android.graphics.Color.DKGRAY
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val zoneId = ZoneId.systemDefault()
        canvas.drawText(startTime.atZone(zoneId).format(timeFormatter), x0, height.toFloat() - 10f, textPaint)
        canvas.drawText(startTime.plusSeconds(totalSec.toLong()).atZone(zoneId).format(timeFormatter), x5, height.toFloat() - 10f, textPaint)
        
        return bitmap
    }

    private fun cancelNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(LIVE_UPDATE_NOTIFICATION_ID)
    }
}
