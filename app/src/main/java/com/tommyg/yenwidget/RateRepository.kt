package com.tommyg.yenwidget

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "rates")

object RateRepository {

    private val KEY_RATE = doublePreferencesKey("rate")
    private val KEY_UPDATED = stringPreferencesKey("updated")
    private val KEY_TODAY_DATE = stringPreferencesKey("today_date")
    private val KEY_TODAY_RATE = doublePreferencesKey("today_rate")
    private val KEY_PREV_RATE = doublePreferencesKey("prev_rate")

    data class Snapshot(val rate: Double?, val changePct: Double?, val updated: String?)

    suspend fun snapshot(context: Context): Snapshot {
        val p = context.dataStore.data.first()
        val rate = p[KEY_RATE]
        val prev = p[KEY_PREV_RATE]
        val change = if (rate != null && prev != null && prev > 0) {
            (rate - prev) / prev * 100
        } else null
        return Snapshot(rate, change, p[KEY_UPDATED])
    }

    suspend fun refresh(context: Context): Boolean = withContext(Dispatchers.IO) {
        val rate = fetch("https://open.er-api.com/v6/latest/USD")
            ?: fetch("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json")
            ?: return@withContext false

        val today = LocalDate.now().toString()
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
        context.dataStore.edit { p ->
            // Roll yesterday's first reading into prev so changePct = today vs yesterday
            if (p[KEY_TODAY_DATE] != today) {
                p[KEY_TODAY_RATE]?.let { p[KEY_PREV_RATE] = it }
                p[KEY_TODAY_DATE] = today
                p[KEY_TODAY_RATE] = rate
            }
            p[KEY_RATE] = rate
            p[KEY_UPDATED] = time
        }
        true
    }

    private fun fetch(url: String): Double? = try {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        val json = JSONObject(body)
        when {
            json.has("rates") -> json.getJSONObject("rates").getDouble("JPY")
            json.has("usd") -> json.getJSONObject("usd").getDouble("jpy")
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
