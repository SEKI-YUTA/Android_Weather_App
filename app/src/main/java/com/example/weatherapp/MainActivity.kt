package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val baseUrl = "https://api.openweathermap.org/data/2.5/weather?lang=ja"
        val apiKey = "you need to paste your api key"

        val btnTokyo:Button = findViewById(R.id.btnTokyo)
        val btnOkinawa:Button = findViewById(R.id.btnOkinawa)
        val tvCity: TextView = findViewById(R.id.tvCity)
        val tvCityWeather:TextView = findViewById(R.id.tvCityWeather)
        val tvMax:TextView = findViewById(R.id.tvMax)
        val tvMin:TextView = findViewById(R.id.tvMin)
        val btnClear:Button = findViewById(R.id.btnClear)
        val btnTest:Button = findViewById(R.id.btnTest)

        btnTokyo.setOnClickListener {
            val weatherUrl = "$baseUrl&q=tokyo&appid=$apiKey"
            weatherTask(weatherUrl)
        }
        btnOkinawa.setOnClickListener {
            val weatherUrl = "$baseUrl&q=okinawa&appid=$apiKey"
            weatherTask(weatherUrl)
        }
        btnClear.setOnClickListener {
            tvCity.text = "都市名"
            tvCityWeather.text = "都市の天気"
            tvMax.text = "最高気温"
            tvMin.text = "最低気温"
        }

        btnTest.setOnClickListener {
            val javaLocation = System.getenv("JAVA_HOME")
            Log.d("MyLog", javaLocation)
        }
    }
    private fun weatherTask(weatherUrl: String) {
        lifecycleScope.launch {
            // http 通信
            val result = weatherBackgroundTask(weatherUrl)
            // お天気データを表示 (UIスレッド)
            weatherJsonTask(result)
        }
    }

    private suspend fun weatherBackgroundTask(weatherUrl: String):String {
        // IO ワーカースレッド
        // Main メインスレッド
        val response = withContext(Dispatchers.IO) {
            var httpResult = ""
            try {
                val urlObj = URL(weatherUrl)
                val br = BufferedReader(InputStreamReader(urlObj.openStream()))
//                httpResult = br.toString()
                httpResult = br.readText()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return@withContext httpResult
        }
        return response
    }

    private fun weatherJsonTask(result: String) {
        val tvCity: TextView = findViewById(R.id.tvCity)
        val tvCityWeather:TextView = findViewById(R.id.tvCityWeather)
        val tvMax:TextView = findViewById(R.id.tvMax)
        val tvMin:TextView = findViewById(R.id.tvMin)

        val jsonObj = JSONObject(result)
        val cityName = jsonObj.getString("name")
        tvCity.text = cityName

        val weatherJSONArray = jsonObj.getJSONArray("weather")
        val weatherJSON = weatherJSONArray.getJSONObject(0)
        val weather = weatherJSON.getString("description")
        tvCityWeather.text = weather

        val mainJSON = jsonObj.getJSONObject("main")
        val maxTmp = mainJSON.getInt("temp_max") - 273
        val minTmp = mainJSON.getInt("temp_min") - 273

        tvMax.text = "最高気温: ${maxTmp}℃"
        tvMin.text = "最低気温: ${minTmp}℃"
    }
}