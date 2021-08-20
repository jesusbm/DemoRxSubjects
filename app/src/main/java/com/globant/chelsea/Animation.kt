package com.globant.chelsea

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.ReplaySubject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

typealias Position = Pair<Float, Float>

interface Animation {

    val position: Observable<Position>
    val path: Observable<Position>
    val finalPosition: Observable<Position>

    fun step(dt: Float = 0.45F): Boolean
    fun isWinnerPosition(finalPosition: Position): Boolean
}

class BouncingBallAnimation(private val width: Int, private val height: Int): Animation {

    private val Vo = 150F + 50F * (0.5F - Random.nextFloat())
    private var eff = 0.99F

    private val box =  Box(width.toFloat(), height.toFloat())
    private val ball = Ball(width / 2F, height / 2F, 10F)

    private var vx = Vo * cos(2 * PI.toFloat() * Random.nextFloat())
    private var vy = Vo * sin(2 * PI.toFloat() * Random.nextFloat())

    private val positionSubject = BehaviorSubject.createDefault(ball.getPosition())
    private val pathSubject = ReplaySubject.createWithSize<Position>(50)
    private val finalPositionSubject = AsyncSubject.create<Position>()

    override val position: Observable<Position>
        get() { return positionSubject.hide() }

    override val path: Observable<Position>
        get() { return pathSubject.hide() }

    override val finalPosition: Observable<Position>
        get() { return finalPositionSubject.hide() }

    override fun step(dt: Float): Boolean {
        val currentPosition = ball.getPosition()
        val newPosition = calculateNewPosition(dt).let {
            val collisionExist = handleCollision(it)
            if (collisionExist) {
                calculateNewPosition(dt)
            } else {
                it
            }
        }
        ball.moveTo(newPosition)
        decreaseVelocity()
        positionSubject.onNext(newPosition)
        pathSubject.onNext(currentPosition)
        finalPositionSubject.onNext(newPosition)
        return if (areCloserThan(currentPosition, newPosition, 2F)) {
            positionSubject.onComplete()
            pathSubject.onComplete()
            finalPositionSubject.onComplete()
            false
        } else {
            true
        }
    }

    override fun isWinnerPosition(finalPosition: Position): Boolean {
        return areCloserThan(
            Position(width / 2F, height / 2F),
            finalPosition,
            250F)
    }

    private fun handleCollision(newPosition: Position): Boolean {
        val touchingFace = box.getTouchingFace(newPosition, ball.radius)
        touchingFace?.let { face ->
            when(face) {
                Box.Face.LEFT,
                Box.Face.RIGHT -> vx = -vx
                Box.Face.TOP,
                Box.Face.BOTTOM -> vy = -vy
            }
        }
        return touchingFace != null
    }

    private fun decreaseVelocity() {
        vx *= eff
        vy *= eff
    }

    private fun calculateNewPosition(dt: Float) = with(ball.getPosition()) {
        Pair(first + dt * vx, second + dt * vy)
    }

    private fun areCloserThan(currentPosition: Position, newPosition: Position, distance: Float): Boolean {
        val dx = currentPosition.first - newPosition.first
        val dy = currentPosition.second - newPosition.second
        return sqrt(dx*dx + dy*dy) < distance
    }
}

class Box(private val width: Float, private val height: Float) {
    enum class Face { LEFT, TOP, RIGHT, BOTTOM }
    fun getTouchingFace(position: Position, margin: Float): Face? = with(position) {
        return when {
            isNegative(first - margin) -> Face.LEFT
            isNegative(second - margin) -> Face.TOP
            isNegative(width - first - margin ) -> Face.RIGHT
            isNegative(height - second - margin) -> Face.BOTTOM
            else -> null
        }
    }

    private fun isNegative(value: Float) = value < 0F
}

class Ball(var cx: Float, var cy: Float, val radius: Float) {
    fun moveTo(newPosition: Position) {
        cx = newPosition.first
        cy = newPosition.second
    }
    fun getPosition() = Position(cx, cy)
}
