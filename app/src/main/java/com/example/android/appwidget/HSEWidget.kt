/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.appwidget

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.example.android.appwidget.MainActivity
import com.example.android.appwidget.R
import com.example.android.appwidget.glance.*
import java.io.File
import java.io.InputStream
import java.time.Year

class HSEWidget : GlanceAppWidget() {
    override val stateDefinition = HSEInfoStateDefinition
    @SuppressLint("RemoteViewLayout")
    @Composable
    override fun Content() {
        val weatherInfo = currentState<HSEInfo>()
        GlanceTheme {
            when (weatherInfo) {
                HSEInfo.Loading -> {
                    AppWidgetBox(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is HSEInfo.Available -> {
                    HSELazyColumn(weatherInfo)
                }
                is HSEInfo.Unavailable -> {
                    AppWidgetColumn(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Data not available")
                        Button("Refresh", actionRunCallback<UpdateWeatherAction>())
                    }
                }
            }
        }
    }
}
fun String.addEmptyLines(lines: Int) = this + "\n".repeat(lines)

private val CheckboxKey = booleanPreferencesKey("checkbox")
private val SwitchKey = booleanPreferencesKey("switch")
private val SelectedKey = ActionParameters.Key<String>("key")

class CompoundButtonAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // The framework automatically sets the value of the toggled action (true/false)
        // Retrieve it using the ToggleableStateKey
        val toggled = parameters[ToggleableStateKey] ?: false
        updateAppWidgetState(context, glanceId) { prefs ->
            // Get which button the action came from
            val key = booleanPreferencesKey(parameters[SelectedKey] ?: return@updateAppWidgetState)
            // Update the state
            prefs[key] = toggled
        }
        HSEWidget().update(context, glanceId)
    }
}

@Composable
fun HSELazyColumn(
    weatherInfo: HSEInfo.Available,
    modifier: GlanceModifier = GlanceModifier
) {
    AppWidgetColumn(
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            item {

                Button(
                    modifier = GlanceModifier.fillMaxWidth(),
                    onClick = actionStartActivity<MainActivity>(),
                    maxLines = 3,
//                    text = "Title\ntitle\nTITLE".addEmptyLines(2),
                    text = weatherInfo.placeName,
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }
}

class UpdateWeatherAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Force the worker to refresh

        HSEWorker.enqueue(context = context, force = true)
    }
}

