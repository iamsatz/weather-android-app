package com.kosmos.android.ui.radar

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.kosmos.android.data.RainViewerClient

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RadarMapView(
    result: RainViewerClient.RadarResult,
    frameIndex: Int,
    modifier: Modifier = Modifier,
) {
    val frame = result.frames.getOrElse(frameIndex) { result.frames.last() }
    val html = buildRadarHtml(result.lat, result.lon, result.zoom, frame.path)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadDataWithBaseURL("https://local/", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://local/", html, "text/html", "UTF-8", null)
        },
    )
}

private fun buildRadarHtml(lat: Double, lon: Double, zoom: Int, radarPath: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>html,body,#map{margin:0;padding:0;height:100%;background:#1a2332;}</style>
</head>
<body>
<div id="map"></div>
<script>
var map = L.map('map', {zoomControl:true, attributionControl:false}).setView([$lat, $lon], $zoom);
L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {maxZoom: 14}).addTo(map);
L.circleMarker([$lat, $lon], {radius:8, color:'#4db371', fillColor:'#4db371', fillOpacity:0.9, weight:2}).addTo(map);
L.tileLayer('https://tilecache.rainviewer.com$radarPath/256/{z}/{x}/{y}/2/1_1.png', {opacity:0.65, maxZoom: 14}).addTo(map);
</script>
</body>
</html>
""".trimIndent()
