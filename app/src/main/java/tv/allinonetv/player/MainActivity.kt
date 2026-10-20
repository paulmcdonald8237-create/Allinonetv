
package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val screenBgColor = Color.rgb(8, 14, 30)
    private val blue = Color.rgb(30, 110, 220)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun makeRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 24, 28, 24)
            setBackgroundColor(screenBgColor)
        }
    }

    private fun addTitle(root: LinearLayout, titleText: String) {
        root.addView(TextView(this).apply {
            text = titleText
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })
    }

    private fun addButton(
        root: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        root.addView(Button(this).apply {
            text = label
            textSize = 17f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(blue)
                cornerRadius = 16f
            }
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            58
        ).apply {
            setMargins(0, 6, 0, 6)
        })
    }

    private fun showHome() {
        val root = makeRoot()
        addTitle(root, "Allinonetv")

        root.addView(TextView(this).apply {
            text = "Your TV, all in one place"
            textSize = 17f
            setTextColor(Color.rgb(80, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 18)
        })

        addButton(root, "Live TV") {
            showMessage(
                "Live TV",
                "Your channels will appear here once streaming is connected."
            )
        }

        addButton(root, "Movies") {
            showMessage(
                "Movies",
                "Your movie catalogue will appear here once connected."
            )
        }

        addButton(root, "Series") {
            showMessage(
                "Series",
                "Your series catalogue will appear here once connected."
            )
        }

        addButton(root, "TV Guide (EPG)") {
            showMessage(
                "TV Guide",
                "Your TV programme guide will appear here once connected."
            )
        }

        addButton(root, "Favourites") {
            showMessage(
                "Favourites",
                "Your saved channels and programmes will appear here."
            )
        }

        addButton(root, "Customer Login") {
            showLogin()
        }

        setContentView(root)
    }

    private fun showLogin() {
        val root = makeRoot()
        addTitle(root, "Allinonetv Login")

        root.addView(TextView(this).apply {
            text = "Sign in to your account"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 18)
        })

        val username = makeInput("Username")
        username.inputType = InputType.TYPE_CLASS_TEXT

        val password = makeInput("Password")
        password.inputType =
            InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_VARIATION_PASSWORD

        root.addView(username)
        root.addView(password)

        addButton(root, "Log In") {
            val user = username.text.toString().trim()
            val pass = password.text.toString()

            if (user.isEmpty() || pass.isEmpty()) {
                showMessage(
                    "Missing Details",
                    "Please enter your username and password."
                )
            } else {
                showMessage(
                    "Login Not Connected Yet",
                    "The login screen is ready, but secure account verification still needs to be connected."
                )
            }
        }

        addButton(root, "Scan QR Code") {
            showMessage(
                "QR Code Login",
                "QR scanning will be added after the account connection is set up."
            )
        }

        addButton(root, "Back to Home") {
            showHome()
        }

        setContentView(root)
    }

    private fun showMessage(title: String, message: String) {
        val root = makeRoot()
        addTitle(root, title)

        root.addView(TextView(this).apply {
            text = message
            textSize = 17f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        addButton(root, "Back to Home") {
            showHome()
        }

        setContentView(root)
    }

    private fun makeInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 16f
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setSingleLine(true)
            setPadding(14, 8, 14, 8)

            background = GradientDrawable().apply {
                setColor(Color.rgb(24, 34, 54))
                cornerRadius = 12f
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                56
            ).apply {
                setMargins(0, 6, 0, 6)
            }
        }
    }
}
