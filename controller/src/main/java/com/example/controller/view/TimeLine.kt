package com.example.controller.view

import android.graphics.*
import android.util.Log

class TimeLine(var duration: Long) {
    private var rectOrigin: RectF = RectF()
    private var rectDraw: RectF = RectF()
    private val paint: Paint = Paint()
    private var minSpace = 0.001f
    private var maxSpace = 0.005f
    private var spaceTime = 1000
    private var space = 5f

    init {
        paint.color = Color.RED
        paint.textSize = 20f
        updateTime()
    }

    fun updateSize(width: Int, height: Int) {
        rectOrigin.top = 0f
        rectOrigin.bottom = 80f
        minSpace = width / 10f
        maxSpace = width / 5f
    }

    fun updateTime() {
        rectOrigin.left = 0f
        rectOrigin.right = duration * ControllerView.msToPx
    }

    fun updateMatrix(matrix: Matrix) {
        matrix.mapRect(rectDraw, rectOrigin)
        update()
    }

    fun update() {
        val value = rectDraw.width() / duration
        var count = 0
        while (true) {
            spaceTime = if (value * spaceTime < minSpace) {
                if (spaceTime <= 4000) {
                    spaceTime * 2
                } else {
                    spaceTime + 4000
                }
            } else if (value * spaceTime > maxSpace) {
                if (spaceTime <= 4000) {
                    spaceTime / 2
                } else {
                    spaceTime - 4000
                }
            } else {
                break
            }
            if (spaceTime < 500){
                spaceTime = 500
                break
            }
//            count++
//            if (count>5)break

        }
        space = spaceTime * value

        Log.d("vvve", "update: ${spaceTime}  ${space}")


    }

    fun draw(canvas: Canvas) {
//        canvas.drawRect(rectDraw, paint)
        var x = rectDraw.left
        var time = 0
        while (x < rectDraw.right) {
            if (x>=0) {
                var y = 40f
                if (time%1000 == 0){
                    y = 80f
                }
                canvas.drawLine(x, 0f, x, y, paint)
                canvas.drawCircle(x+space/2, 40f, 3f, paint)


                val text = "${time/1000f}"
                canvas.drawText(text,0,text.length,x,80f,paint)
            }
            x += space
            time += spaceTime
        }

    }
}