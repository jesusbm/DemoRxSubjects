package com.globant.chelsea

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

typealias DrawingTask = (Canvas) -> Unit

class Display(context: Context, attrs: AttributeSet?): View(context, attrs) {

    private val drawingTasks = mutableListOf<DrawingTask>()

    val paintGoal = Paint().apply {
        color = Color.rgb(191, 215, 50)
        strokeWidth = 4F
        style = Paint.Style.FILL_AND_STROKE
    }

    val paintRed = Paint().apply {
        color = Color.RED
        strokeWidth = 4F
        style = Paint.Style.STROKE
    }

    val paintGray = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 0F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawCircle(width/2F, height/2F, 250F, paintGoal)
        canvas?.run {
            val iterator = drawingTasks.iterator()
            while(iterator.hasNext()) {
                val task = iterator.next()
                task(canvas)
                iterator.remove()
            }
        }
    }

    fun addDrawingTask(task: DrawingTask) {
        this.drawingTasks.add(task)
    }
}
