package com.example.randomquotes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var quoteText: TextView
    private lateinit var authorText: TextView
    private lateinit var newQuoteButton: Button
    private lateinit var copyButton: Button
    private lateinit var shareButton: Button

    // Coroutine scope for background tasks
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        quoteText = findViewById(R.id.quoteText)
        authorText = findViewById(R.id.authorText)
        newQuoteButton = findViewById(R.id.newQuoteButton)
        copyButton = findViewById(R.id.copyButton)
        shareButton = findViewById(R.id.shareButton)

        // Load a quote when app starts
        fetchQuote()

        // Load a new quote
        newQuoteButton.setOnClickListener {
            fetchQuote()
        }

        // Copy quote to clipboard
        copyButton.setOnClickListener {
            val fullQuote = "${quoteText.text}\n${authorText.text}"
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Quote", fullQuote)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Share quote using Android share sheet
        shareButton.setOnClickListener {
            val fullQuote = "${quoteText.text}\n${authorText.text}"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fullQuote)
            }
            startActivity(Intent.createChooser(shareIntent, "Share quote via"))
        }
    }

    // Fetch quote using coroutines
    private fun fetchQuote() {
        coroutineScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://zenquotes.io/api/random")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val jsonObject = jsonArray.getJSONObject(0)
                    val quote = jsonObject.getString("q")
                    val author = jsonObject.getString("a")
                    quote to author
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            if (result != null) {
                quoteText.text = "\"${result.first}\""
                authorText.text = "- ${result.second}"
            } else {
                Toast.makeText(this@MainActivity, "Failed to fetch quote", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel background tasks to prevent memory leaks
    }
}
