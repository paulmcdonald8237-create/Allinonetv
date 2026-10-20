
package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val screenBgColor = Color.rgb(8, 14, 30)
    private val blue = Color.rgb(30, 110, 220)
    private val white = Color.WHITE
    private val grey = Color.LTGRAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(28, 24, 28, 24)
            setBackgroundColor(screenBgColor)
        }

        val scroll = ScrollView(this)
        scroll.isFillViewport = true

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Allinonetv"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "YOUR TV, ALL IN ONE PLACE"
            textSize = 14f
            setTextColor(Color.rgb(80, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 24)
        }

        val heading = TextView(this).apply {
            text = "Explore Allinonetv"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }

        val status = TextView(this).apply {
            text = "Welcome! Select a section below."
            textSize = 16f
            setTextColor(grey)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 20)
        }

        content.addView(title)
        content.addView(subtitle)
        content.addView(heading)

        fun addMenuButton(label: String, message: String) {
            val button = Button(this).apply {
                text = label
                textSize = 17f
                isAllCaps = false
                setTextColor(white)
                background = GradientDrawable().apply {
                    setColor(blue)
                    cornerRadius = 14f
                }
                minHeight = 58
                isFocusable = true
                isFocusableInTouchMode = true
                contentDescription = label
                setOnClickListener {
                    status.text = message
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 6, 0, 6)
            content.addView(button, params)
        }

        addMenuButton(
            "Live TV",
            "Live TV selected. Connect a provider to load channels."
        )

        addMenuButton(
            "Movies",
            "Movies selected. Your provider catalogue will appear here."
        )

        addMenuButton(
            "Series",
            "Series selected. Your provider catalogue will appear here."
        )

        addMenuButton(
            "TV Guide (EPG)",
            "TV Guide selected. Programme information will be added next."
        )

        addMenuButton(
            "Favourites",
            "Favourites selected. Favourite channel support is coming next."
        )

        addMenuButton(
            "Provider Login / Settings",
            "Provider setup is the next development step. No provider is connected yet."
        )

        content.addView(status)

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }
}
