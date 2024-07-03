package com.aliya.uimode.sample

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.aliya.uimode.UiModeManager
import com.aliya.uimode.sample.base.BaseActivity

class StyleTestActivity : BaseActivity() {


    val styleInt = arrayOf(R.style.backStyleRed800, R.style.backStyleRed100, R.style.backStyleRed)
    var index = 0

    val tvChange by lazy {
        findViewById<TextView>(R.id.tv_change_local_mode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_style_test)
        tvChange.setOnClickListener {
            val mode = delegate.localNightMode

            val next = when(mode) {
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES

                AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_UNSPECIFIED

                else -> AppCompatDelegate.MODE_NIGHT_NO
            }

            UiModeManager.setLocalNightMode(this,next)


            tvChange.text =  when(next) {
                AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> "未设置"
                AppCompatDelegate.MODE_NIGHT_NO -> "日间"

                AppCompatDelegate.MODE_NIGHT_YES -> "夜间"
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "跟随系统"

                else -> "日间"
            }
        }


        findViewById<View>(R.id.tv_simple1).setOnClickListener {
            index = index % 3
            UiModeManager.applyStyle(it, styleInt[index])
            index++

        }


    }
}