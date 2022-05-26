package com.example.controller.view

import android.graphics.*
import android.util.Log
import com.example.controller.data.ImageModel
import kotlin.math.abs

class Image (var imageModel: ImageModel){
    val rectImage:RectF = RectF()
    private val rectOrigin:RectF = RectF()
    private val paint:Paint = Paint()
    init {
        val color:Int = ((imageModel.endTime + imageModel.startTime)%255).toInt()
        paint.color = Color.argb(100,(imageModel.startTime%255).toInt(),(imageModel.endTime%255).toInt(),color)
        updateTime()
    }
    fun updateHeight(height:Int){
        rectOrigin.top = 100f
        rectOrigin.bottom = height.toFloat()
    }
    fun updateTime(){
        rectOrigin.left = imageModel.startTime * ControllerView.msToPx
        rectOrigin.right = imageModel.endTime * ControllerView.msToPx
    }
    fun updateMatrix(matrix:Matrix){
        matrix.mapRect(rectImage,rectOrigin)
    }
    fun checkTouch(x:Float,y:Float):TouchType{
        if (rectImage.contains(x,y)){
            return TouchType.CENTER
        }
        if (abs(rectImage.left - x) < 80) return TouchType.START
        if (abs(rectImage.right - x) < 80) return TouchType.END

        return TouchType.NONE
    }
    fun draw(canvas: Canvas){
        Log.d("zzet", "draw: "+(rectImage))
        canvas.drawRect(rectImage,paint)
    }
    enum class TouchType{
        NONE,START,END,CENTER
    }
}