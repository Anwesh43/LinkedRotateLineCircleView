package com.anwesh.uiprojects.rotatelinecircleview

/**
 * Created by anweshmishra on 01/04/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val lines : Int = 2
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#43A047")
val backColor : Int = Color.parseColor("#BDBDBD")
val deg : Float = 45f
val rFactor : Float = 3.5f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorValue(a : Int, b : Int): Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sjf() : Float = 1f - (this % 2)

fun Canvas.drawExpandingCircle(x : Float, r : Float, paint : Paint) {
    save()
    translate(x, 0f)
    drawCircle(0f, 0f, r, paint)
    restore()
}

fun Canvas.drawLine(size : Float, paint : Paint) {
    drawLine(0f, 0f, Math.max(0f, size), 0f, paint)
}

fun Canvas.drawRotateLineCircle(i : Int, sc1 : Float, sc2 : Float, size : Float, paint : Paint) {
    val sci1 : Float = sc2.divideScale(i, lines)
    val sci11 : Float = sci1.divideScale(0, 2)
    val sci12 : Float = sci1.divideScale(1, 2)
    val x : Float = size * sci11
    val r : Float = (size / rFactor) * sc1
    save()
    rotate(deg * sci12 * i.sjf())
    drawLine((x - r), paint)
    drawExpandingCircle(x, r, paint)
    restore()
}

fun Canvas.drawRLCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h / 2)
    for (j in 0..(lines - 1)) {
        drawRotateLineCircle(0, sc1, sc2, size, paint)
    }
    restore()
}