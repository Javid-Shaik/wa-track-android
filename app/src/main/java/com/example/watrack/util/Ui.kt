package com.example.watrack.util

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

fun View.fadeIn(duration: Long = 500) {
    val anim: Animation = AlphaAnimation(0f, 1f)
    anim.duration = duration
    this.startAnimation(anim)
    this.visibility = View.VISIBLE
}

fun View.fadeOut(duration: Long = 500) {
    val anim: Animation = AlphaAnimation(1f, 0f)
    anim.duration = duration
    this.startAnimation(anim)
    this.visibility = View.GONE
}


