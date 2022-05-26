package com.example.controller.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.example.controller.data.ImageModel

class ControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val listImage: MutableList<Image> = mutableListOf()
    private var currentImage: Image? = null
    private var currentTypeEvent: TypeEvent = TypeEvent.NONE
    private var lastPoint: PointF = PointF()
    private var centerPoint: PointF = PointF()

    private val allMatrix: Matrix = Matrix()
    private val saveMatrix: Matrix = Matrix()
    private val invertMatrix: Matrix = Matrix()
    private var lastDistance: Float = 0f
    private val timeLine = TimeLine(1L)
    private var flingAnimation: FlingAnimation? = null

    protected val gestureDetector: GestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                fling(e1, e2, velocityX, velocityY)
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        })


    init {
        for (i in 0..10) {
            val start = i * 3000L
            val end = (i + 1) * 3000L
            addImage(ImageModel(start, end))
        }
    }

    fun addImage(imageModel: ImageModel) {
        val image = Image(imageModel)
        image.updateTime()
        image.updateHeight(height)
        image.updateMatrix(allMatrix)
        listImage.add(image)
        updateTime()
        postInvalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        for (image in listImage) image.updateHeight(height)
        timeLine.updateSize(width,height)
        updateMatrix()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            timeLine.draw(canvas)
            for (image in listImage) image.draw(canvas)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                flingAnimation?.cancel()
                downEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                moveEvent(event)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                multiplePointDown(event)
            }
            MotionEvent.ACTION_UP -> {
                currentTypeEvent = TypeEvent.NONE
            }
        }
        postInvalidate()
        return true
    }

    private fun multiplePointDown(event: MotionEvent) {
        if (event.pointerCount >= 2) {
            currentTypeEvent = TypeEvent.ZOOM
            lastDistance = event.getX(1) - event.getX(0)
            centerPoint.set((event.getX(1) + event.getX(0)) / 2f, height / 2f)
            saveMatrix.set(allMatrix)
        }
    }

    private fun downEvent(event: MotionEvent) {
        checkType(event)
        lastPoint.set(event.x, event.y)
        saveMatrix.set(allMatrix)
    }

    private fun moveEvent(event: MotionEvent) {
        when (currentTypeEvent) {
            TypeEvent.MOVE_ALL -> {
                moveAll(event)
            }
            TypeEvent.MOVE_END -> {
                changeTimeEnd(event)
            }
            TypeEvent.ZOOM -> {
                zoom(event)
            }
        }
    }

    private fun zoom(event: MotionEvent) {
        if (event.pointerCount >= 2) {
            val newDistance = event.getX(1) - event.getX(0)
            allMatrix.set(saveMatrix)
            allMatrix.postScale(newDistance / lastDistance, 1f,centerPoint.x,centerPoint.y)
            updateMatrix()
        }
    }

    private fun moveAll(event: MotionEvent) {
        allMatrix.set(saveMatrix)
        allMatrix.postTranslate(event.x - lastPoint.x, 0f)
        updateMatrix()
        standardize()
    }

    private fun changeTimeEnd(event: MotionEvent) {
        val point: FloatArray = floatArrayOf(event.x, 0f, lastPoint.x, 0f)
        invertMatrix.mapPoints(point)
        var time = ((point[0] - point[2]) / msToPx).toLong()
        lastPoint.set(event.x, event.y)
        Log.d("kkkkkk", "changeTimeEnd: " + time)
        var check = false
        for (image in listImage) {
            if (image != currentImage) {
                if (!check) continue
                image.imageModel.startTime += time
                image.imageModel.endTime += time
                image.updateTime()
            } else {
                check = true
                if (image.imageModel.endTime + time < image.imageModel.startTime + 1000) {
                    time = image.imageModel.startTime + 1000 - image.imageModel.endTime
                }
                image.imageModel.endTime += time
                image.updateTime()
            }
        }
        updateTime()
        updateMatrix()
    }
    private fun updateTime(){
        if (listImage.size>0){
            timeLine.duration = listImage.last().imageModel.endTime
            timeLine.updateTime()
        }
    }

    private fun updateMatrix() {
        for (image in listImage) image.updateMatrix(allMatrix)
        timeLine.updateMatrix(allMatrix)
        allMatrix.invert(invertMatrix)
    }

    private fun checkType(event: MotionEvent) {
        var type = currentImage?.checkTouch(event.x, event.y) ?: Image.TouchType.NONE
        when (type) {
            Image.TouchType.NONE -> {
                for (image in listImage) {
                    type = image.checkTouch(event.x, event.y)
                    if (type == Image.TouchType.CENTER) {
                        currentImage = image
                        break
                    }
                }
            }
            Image.TouchType.START -> {
                currentTypeEvent = TypeEvent.MOVE_START
            }
            Image.TouchType.END -> {
                currentTypeEvent = TypeEvent.MOVE_END

            }
            else -> {
            }
        }
        if (type != Image.TouchType.END) {
            currentTypeEvent = TypeEvent.MOVE_ALL
        }
    }
    private fun fling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float) {
        saveMatrix.set(allMatrix)
        flingAnimation?.cancel()
        flingAnimation = FlingAnimation(FloatValueHolder())
        flingAnimation?.setStartVelocity(velocityX)
        flingAnimation?.friction = 0.2f
        flingAnimation?.addUpdateListener { _, value, _ ->
            allMatrix.set(saveMatrix)
            allMatrix.postTranslate(value, 0f)
            updateMatrix()
            standardize()
            postInvalidate()
        }
        flingAnimation?.start()
    }

    private fun standardize() {
        if (listImage.size>0){
            val first = listImage.first().rectImage
            val last = listImage.last().rectImage
            if (last.right - width < 100){
                allMatrix.postTranslate(width-last.right-100,0f)
                updateMatrix()
            }
            if (first.left > 100){
                allMatrix.postTranslate(100-first.left,0f)
                updateMatrix()
            }

        }
    }


    enum class TypeEvent {
        NONE, MOVE_START, MOVE_END, MOVE_ALL, ZOOM
    }

    companion object {
        const val msToPx = 0.1f // ms = x px
    }
}