package tv.allinonetv.player

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(8, 14, 30))
        }

        val title = TextView(this).apply {
            text = "Allinonetv"
            textSize = 34f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Your TV, all in one place"
            textSize = 18f
            setTextColor(Color.rgb(80, 190, 255))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 28)
        }

        val info = TextView(this).apply {
            text = "Live TV  •  Movies  •  Series\n\nMulti-provider sign-in will be added next."
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(info)
        setContentView(root)
    }
}
