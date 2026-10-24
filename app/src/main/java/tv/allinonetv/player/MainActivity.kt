
package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val bg = Color.rgb(8, 14, 30)
    private val blue = Color.rgb(30, 110, 220)

    private val prefs by lazy {
        getSharedPreferences("allinonetv_profiles", MODE_PRIVATE)
    }

    private data class Provider(val name: String, val url: String)

    private var providers = mutableListOf<Provider>()
    private var selectedUrl = ""

    private val customerLoginUrl =
        "https://allinonetv-master-panel-git-xtre-67a7c5-paulmcdonald8237-create.vercel.app/api/customer-login"

    private val customerCatalogueUrl =
        "https://allinonetv-master-panel-git-xtre-67a7c5-paulmcdonald8237-create.vercel.app/api/customer-catalogue"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadProviders()
        showHome()
    }

    private fun makeRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
            setBackgroundColor(bg)
        }
    }

    private fun setScreen(root: LinearLayout) {
        val scroll = ScrollView(this)
        scroll.isFillViewport = true
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun title(root: LinearLayout, text: String) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        })
    }

    private fun label(root: LinearLayout, text: String) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 10)
        })
    }

    private fun button(
        root: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
        root.addView(Button(this).apply {
            this.text = text
            textSize = 16f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(blue)
                cornerRadius = 16f
            }
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 58
        ).apply {
            setMargins(0, 5, 0, 5)
        })
    }

    private fun input(
        hintText: String,
        password: Boolean = false,
        email: Boolean = false
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
            setPadding(14, 8, 14, 8)

            inputType = when {
                password -> InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
                email -> InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                else -> InputType.TYPE_CLASS_TEXT
            }

            background = GradientDrawable().apply {
                setColor(Color.rgb(24, 34, 54))
                cornerRadius = 12f
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 56
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
    }

    private fun showHome() {
        val root = makeRoot()
        title(root, "Allinonetv")
        label(root, "Your TV, all in one place")

        button(root, "Live TV") {
            loadCatalogue("live", "Live TV")
        }

        button(root, "Movies") {
            loadCatalogue("movies", "Movies")
        }

        button(root, "Series") {
            loadCatalogue("series", "Series")
        }

        button(root, "TV Guide (EPG)") {
            showMessage(
                "TV Guide",
                "Programme listings need an EPG connection."
            )
        }

        button(root, "Favourites") {
            showMessage(
                "Favourites",
                "Saved channels will appear here when implemented."
            )
        }

        button(root, "Manage Providers") {
            showProviders()
        }

        button(root, "Customer Login") {
            showLogin()
        }

        setScreen(root)
    }

    private fun showProviders() {
        val root = makeRoot()
        title(root, "Manage Providers")
        label(root, "Add an authorised HTTPS server")

        val nameInput = input("Provider name")
        val urlInput = input("Server URL (https://...)")

        root.addView(nameInput)
        root.addView(urlInput)

        button(root, "Add Provider") {
            val name = nameInput.text.toString().trim()
            val rawUrl = urlInput.text.toString().trim().trimEnd('/')

            val uri = runCatching { Uri.parse(rawUrl) }.getOrNull()
            val validUrl = uri != null &&
                uri.scheme.equals("https", ignoreCase = true) &&
                !uri.host.isNullOrBlank()

            when {
                name.isBlank() -> showMessage(
                    "Provider Name Required",
                    "Enter a name for this provider."
                )

                !validUrl -> showMessage(
                    "Invalid Server URL",
                    "Enter a valid HTTPS server address."
                )

                providers.any {
                    it.url.equals(rawUrl, ignoreCase = true)
                } -> showMessage(
                    "Already Added",
                    "That server is already saved."
                )

                else -> {
                    providers.add(Provider(name, rawUrl))
                    saveProviders()
                    showProviders()
                }
            }
        }

        label(root, "Saved providers")

        if (providers.isEmpty()) {
            label(root, "No providers added yet.")
        } else {
            providers.forEach { provider ->
                button(root, provider.name) {
                    selectedUrl = provider.url
                    showMessage(
                        "Provider Selected",
                        "${provider.name} selected. Customer login uses your Allinonetv account."
                    )
                }
            }
        }

        button(root, "Back to Home") {
            showHome()
        }

        setScreen(root)
    }

    private fun showLogin() {
        val root = makeRoot()
        title(root, "Allinonetv Login")
        label(root, "Sign in with your Xtream service credentials")

        val username = input("Xtream username")
        val password = input("Xtream password", password = true)

        root.addView(username)
        root.addView(password)

        button(root, "Log In") {
            val usernameText = username.text.toString().trim()
            val passwordText = password.text.toString()

            when {
                usernameText.isBlank() || passwordText.isBlank() ->
                    showMessage(
                        "Missing Details",
                        "Enter your Xtream username and password."
                    )

                else -> customerLogin(usernameText, passwordText)
            }
        }

        button(root, "Scan QR Code") {
            showMessage(
                "QR Code Login",
                "QR login has not been implemented yet."
            )
        }

        button(root, "Back to Home") {
            showHome()
        }

        setScreen(root)
    }

    private fun customerLogin(username: String, password: String) {
        thread {
            var connection: HttpURLConnection? = null

            try {
                connection = (URL(customerLoginUrl).openConnection()
                    as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                    )
                    setRequestProperty(
                        "Accept",
                        "application/json"
                    )
                }

                val payload = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }.toString()

                connection.outputStream.use { output ->
                    output.write(payload.toByteArray(Charsets.UTF_8))
                }

                val status = connection.responseCode

                val responseText = (
                    if (status in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }
                )?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                }.orEmpty()

                val response = runCatching {
                    JSONObject(responseText)
                }.getOrNull()

                if (status in 200..299 &&
                    !response?.optString("token").isNullOrBlank()
                ) {
                    val token = response!!.getString("token")
                    val customerEmail =
                        response.optJSONObject("customer")?.optString("username", username) ?: username
                    val displayName =
                        response.optString("displayName", "")

                    prefs.edit()
                        .putString("customer_token", token)
                        .putString("customer_email", customerEmail)
                        .putString("customer_name", displayName)
                        .apply()

                    runOnUiThread {
                        showMessage(
                            "Login Successful",
                            "Your Allinonetv customer account is connected."
                        )
                    }
                } else {
                    val errorMessage = response
                        ?.optString("error")
                        ?.takeIf { it.isNotBlank() }
                        ?: when (status) {
                            401, 403 ->
                                "The Xtream login details were not accepted, or the service may be inactive or expired."
                            in 500..599 ->
                                "The Master Panel encountered a server error. Please try again later."
                            else ->
                                "The login could not be completed. Please try again."
                        }

                    runOnUiThread {
                        showMessage("Login Failed", errorMessage)
                    }
                }
            } catch (_: Exception) {
                runOnUiThread {
                    showMessage(
                        "Connection Error",
                        "Could not reach the Allinonetv Master Panel. Check your internet connection and try again."
                    )
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun loadCatalogue(type: String, screenTitle: String) {
        val token = prefs.getString("customer_token", null)

        if (token.isNullOrBlank()) {
            showMessage(
                "Customer Login Required",
                "Please sign in with your Allinonetv customer account before loading $screenTitle."
            )
            return
        }

        thread {
            var connection: HttpURLConnection? = null

            try {
                val endpoint = "$customerCatalogueUrl?type=$type"
                connection = (URL(endpoint).openConnection()
                    as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }

                val status = connection.responseCode
                val responseText = (
                    if (status in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }
                )?.bufferedReader(Charsets.UTF_8)?.use {
                    it.readText()
                }.orEmpty()

                val response = runCatching {
                    JSONObject(responseText)
                }.getOrNull()

                if (status in 200..299 && response != null) {
                    val items = response.optJSONArray("items") ?: JSONArray()
                    runOnUiThread {
                        showCatalogue(screenTitle, items)
                    }
                } else {
                    val error = response?.optString("error")
                        ?.takeIf { it.isNotBlank() }
                        ?: "Could not load the catalogue. Please try again."

                    runOnUiThread {
                        showMessage(
                            if (status == 401) "Please Sign In Again" else "Catalogue Error",
                            error
                        )
                    }
                }
            } catch (_: Exception) {
                runOnUiThread {
                    showMessage(
                        "Connection Error",
                        "Could not reach the Allinonetv catalogue service. Check your connection and try again."
                    )
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun typeForTitle(title: String): String = when (title) {
        "Live TV" -> "live"
        "Movies" -> "movies"
        else -> "series"
    }

    private fun requestPlayback(type: String, id: String, name: String) {
        val token = prefs.getString("customer_token", null)
        if (token.isNullOrBlank()) {
            showMessage("Customer Login Required", "Please sign in again before playback.")
            return
        }
        thread {
            var connection: HttpURLConnection? = null
            try {
                val endpoint = Uri.parse(customerCatalogueUrl).buildUpon()
                    .appendQueryParameter("type", type)
                    .appendQueryParameter("playbackId", id)
                    .build().toString()
                connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 15000
                    readTimeout = 20000
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Accept", "application/json")
                }
                val status = connection.responseCode
                val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val response = runCatching { JSONObject(body) }.getOrNull()
                val streamUrl = response?.optString("playbackUrl").orEmpty()
                if (status in 200..299 && streamUrl.startsWith("https://")) {
                    runOnUiThread {
                        startActivity(android.content.Intent(this, PlayerActivity::class.java).apply {
                            putExtra("stream_url", streamUrl)
                            putExtra("stream_title", name)
                        })
                    }
                } else {
                    val message = response?.optString("error")?.takeIf { it.isNotBlank() }
                        ?: "Could not prepare this stream. Please try another item."
                    runOnUiThread { showMessage("Playback Error", message) }
                }
            } catch (_: Exception) {
                runOnUiThread { showMessage("Playback Error", "Could not connect to the playback service.") }
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun showCatalogue(screenTitle: String, items: JSONArray) {
        val root = makeRoot()
        title(root, screenTitle)

        if (items.length() == 0) {
            label(root, "No items were returned for this catalogue.")
        } else {
            label(root, "${items.length()} items loaded. Select an item for details.")

            val visibleCount = items.length()
            for (i in 0 until visibleCount) {
                val item = items.optJSONObject(i) ?: continue
                val name = item.optString("name", "Untitled")
                val category = item.optString("categoryName", "")
                val id = item.optString("id", "")

                button(root, name) {
                    if (screenTitle == "Series") {
                        showMessage("Series Playback", "Series episode playback is the next step. Please test a live channel or movie first.")
                        return@button
                    }
                    requestPlayback(typeForTitle(screenTitle), id, name)
                    /*
                    val details = buildString {
                        append(name)
                        if (category.isNotBlank()) {
                            append("\nCategory: ")
                            append(category)
                        }
                        if (id.isNotBlank()) {
                            append("\nCatalogue ID: ")
                            append(id)
                        }
                        append("\n\nPlayback is the next integration step.")
                    }
                    showMessage("Item Details", details)
                    */
                }
            }

        }

        button(root, "Back to Home") {
            showHome()
        }

        setScreen(root)
    }

    private fun showMessage(
        messageTitle: String,
        message: String
    ) {
        val root = makeRoot()
        title(root, messageTitle)
        label(root, message)

        button(root, "Back to Home") {
            showHome()
        }

        button(root, "Customer Login") {
            showLogin()
        }

        setScreen(root)
    }

    private fun loadProviders() {
        providers.clear()
        val saved = prefs.getString("providers", "[]") ?: "[]"

        runCatching {
            val array = JSONArray(saved)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val name = item.optString("name")
                val url = item.optString("url")

                if (name.isNotBlank() && url.isNotBlank()) {
                    providers.add(Provider(name, url))
                }
            }
        }
    }

    private fun saveProviders() {
        val array = JSONArray()

        providers.forEach { provider ->
            array.put(JSONObject().apply {
                put("name", provider.name)
                put("url", provider.url)
            })
        }

        prefs.edit()
            .putString("providers", array.toString())
            .apply()
    }
}
