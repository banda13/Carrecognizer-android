package com.ai.deep.andy.carrecognizer.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.design.widget.FloatingActionButton
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ai.deep.andy.carrecognizer.R
import org.json.JSONObject

object MyAnimationUtils {

    class MyBounceInterpolator(amplitude: Double, frequency: Double) : android.view.animation.Interpolator {
        private var mAmplitude = 1.0
        private var mFrequency = 10.0

        init {
            mAmplitude = amplitude
            mFrequency = frequency
        }

        override fun getInterpolation(time: Float): Float {
            return (-1.0 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1).toFloat()
        }
    }

    interface carDetectorAnimationCallback{
        fun animationEnded()
    }

    fun playCaptureButtonAnimation(isCar: Boolean, context : Context, target: FloatingActionButton, duration: Long, listener : carDetectorAnimationCallback){
        val myAnim: Animation = AnimationUtils.loadAnimation(context, R.anim.bounce)
        val interpolator : MyAnimationUtils.MyBounceInterpolator = MyAnimationUtils.MyBounceInterpolator(0.2, 20.0)
        myAnim.interpolator = interpolator
        myAnim.duration = duration

        val colorFrom = if(isCar) {
            Color.RED
        }
        else {
            Color.GREEN
        }
        val colorTo = if(isCar) {
            Color.GREEN
        }
        else {
            Color.RED
        }
        val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = duration
        colorAnimation.addUpdateListener {
            target.backgroundTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }

        myAnim.setAnimationListener(
            object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    target.backgroundTintList = ColorStateList.valueOf(colorTo)
                    listener.animationEnded()
                }
            }
        )

        colorAnimation.start()
        target.startAnimation(myAnim)

    }
}