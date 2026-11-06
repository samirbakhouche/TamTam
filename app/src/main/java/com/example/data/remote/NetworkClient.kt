package com.example.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient(private var baseUrl: String = "https://ais-dev-ywbbdoogqm3oxemnpid6sj-757345323820.europe-west2.run.app/") {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    var apiService: TamTamApiService = createRetrofit(baseUrl)
        private set

    private fun createRetrofit(url: String): TamTamApiService {
        val safeUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(safeUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TamTamApiService::class.java)
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        apiService = createRetrofit(newUrl)
    }

    // --- WebSockets Realtime Engine ---
    private var webSocket: WebSocket? = null
    private val _incomingEvents = MutableSharedFlow<String>(extraBufferCapacity = 50)
    val incomingEvents: SharedFlow<String> = _incomingEvents.asSharedFlow()

    fun connectWebSocket(token: String?) {
        val wsUrl = baseUrl.replace("http://", "ws://").replace("https://", "wss://") + "ws"
        val request = Request.Builder().url(wsUrl).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("TamTamWS", "WebSocket connected: $wsUrl")
                if (!token.isNullOrEmpty()) {
                    val authPayload = """{"event":"auth:handshake","payload":{"token":"$token"}}"""
                    webSocket.send(authPayload)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("TamTamWS", "Received WS Message: $text")
                _incomingEvents.tryEmit(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("TamTamWS", "WebSocket error: ${t.message}")
            }
        })
    }

    fun sendWebSocketMessage(json: String) {
        webSocket?.send(json)
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Normal Closure")
        webSocket = null
    }
}
