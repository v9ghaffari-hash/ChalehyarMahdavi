package ir.chaleyar.mahdi

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(Color.rgb(250, 247, 255))
        }

        val title = TextView(this).apply {
            text = "چله‌یار مهدوی"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(65, 45, 110))
        }

        val message = TextView(this).apply {
            text = "به چله‌یار مهدوی خوش آمدید 🌷\n\nبرای تعجیل در فرج امام زمان (عج)، با امید و دعا همراه شویم."
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(0, 32, 0, 32)
        }

        layout.addView(title)
        layout.addView(message)
        setContentView(layout)
    }
}
