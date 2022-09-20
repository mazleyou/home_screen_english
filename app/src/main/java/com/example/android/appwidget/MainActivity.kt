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

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android.appwidget.util.readCsv

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetProviders = widgetManager.getInstalledProvidersForPackage(packageName, null)
        val providerInfo = widgetProviders[0]


        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColors()
            } else {
                lightColors()
            }
            MaterialTheme(colors) {
                Scaffold {
                    LazyColumn(contentPadding = it) {
                        item {
                            WidgetInfoCard(providerInfo)
                        }
                    }
                }
            }
        }
    }
}

private fun AppWidgetProviderInfo.pin(context: Context) {
    val successCallback = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AppWidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WidgetInfoCard(providerInfo: AppWidgetProviderInfo) {
    val context = LocalContext.current
    val label = providerInfo.loadLabel(context.packageManager)
    val sentence = readCsv(context.resources.openRawResource(R.raw.b))


    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Button(onClick = {
                providerInfo.pin(context)
            }) {
                Text(text = label)
            }
        }
        Column {
            Text(text = sentence[0].eng)
        }
    }
}

