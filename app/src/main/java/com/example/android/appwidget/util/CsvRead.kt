package com.example.android.appwidget.util

import java.io.InputStream

fun readCsv(inputStream: InputStream): List<Sentence> {
    val reader = inputStream.bufferedReader()
    val header = reader.readLine()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (no, eng, kor) = it.split('=', ignoreCase = false, limit = 3)
            Sentence(no.trim().toInt(), eng.trim().removeSurrounding("\""), kor.trim().removeSurrounding("\""))
        }.toList()
}

