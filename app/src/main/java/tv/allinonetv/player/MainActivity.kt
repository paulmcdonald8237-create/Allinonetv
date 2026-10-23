
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
        "https://allinonetv-master-panel.vercel.app/api/customer-login"

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
            showMessage(
                "Live TV",
                "Streaming playback still needs to be connected."
            )
        }

        button(root, "Movies") {
            showMessage(
                "Movies",
                "Movie catalogue integration is not yet complete."
            )
        }

        button(root, "Series") {
            showMessage(
                "Series",
                "Series catalogue integration is not yet complete."
            )
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
        label(root, "Sign in with your customer account")

        val email = input("Email address", email = true)
        val password = input("Password", password = true)

        root.addView(email)
        root.addView(password)

        button(root, "Log In") {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()

            when {
                emailText.isBlank() || passwordText.isBlank() ->
                    showMessage(
                        "Missing Details",
                        "Enter your email address and password."
                    )

                else -> customerLogin(emailText, passwordText)
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

    private fun customerLogin(email: String, password: String) {
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
                    put("email", email)
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
                        response.optString("email", email)
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
                                "The login details were not accepted, or your account is not authorised."
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
