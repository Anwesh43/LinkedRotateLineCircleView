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

class RotateLineCircleView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class RLCNode(var i : Int, val state : State = State()) {

        private var next : RLCNode? = null
        private var prev : RLCNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = RLCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawRLCNode(i, state.scale, paint)
            prev?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : RLCNode {
            var curr : RLCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class RotateLineCircle(var i : Int) {

        private var curr : RLCNode = RLCNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : RotateLineCircleView) {

        private val rlc : RotateLineCircle = RotateLineCircle(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            rlc.draw(canvas, paint)
            animator.animate {
                rlc.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            rlc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : RotateLineCircleView {
            val view : RotateLineCircleView = RotateLineCircleView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}