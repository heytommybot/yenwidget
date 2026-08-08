package com.tommyg.yenwidget

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.trainStore by preferencesDataStore(name = "train")

object TrainRepository {

    private const val STATUS_URL = "https://transit.yahoo.co.jp/diainfo/21/0"

    private val KEY_NORMAL = booleanPreferencesKey("normal")
    private val KEY_TEXT = stringPreferencesKey("text")
    private val KEY_UPDATED = stringPreferencesKey("updated")

    data class Status(val normal: Boolean?, val text: String?, val updated: String?)

    suspend fun status(context: Context): Status {
        val p = context.trainStore.data.first()
        return Status(p[KEY_NORMAL], p[KEY_TEXT], p[KEY_UPDATED])
    }

    suspend fun refresh(context: Context): Boolean = withContext(Dispatchers.IO) {
        val html = fetch() ?: return@withContext false
        val normal = html.contains("平常運転")
        val text = if (normal) "Normal service" else englishStatus(html)
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
        context.trainStore.edit { p ->
            p[KEY_NORMAL] = normal
            p[KEY_TEXT] = text
            p[KEY_UPDATED] = time
        }
        true
    }

    private fun englishStatus(html: String) = when {
        "運転見合わせ" in html -> "Suspended"
        "運転再開" in html -> "Service resuming"
        "直通運転中止" in html -> "Through service halted"
        "ダイヤ乱れ" in html -> "Schedule disruption"
        "遅延" in html -> "Delays"
        "運休" in html -> "Some trains cancelled"
        else -> "Service disruption"
    }

    private fun fetch(): String? = try {
        val conn = URL(STATUS_URL).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 16; Pixel 10 Pro XL) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"
        )
        val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        conn.disconnect()
        body
    } catch (e: Exception) {
        null
    }
}
