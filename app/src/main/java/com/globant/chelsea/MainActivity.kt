package com.globant.chelsea

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.globant.chelsea.databinding.ActivityMainBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var animation: Animation? = null
    private var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myButton.setOnClickListener { launchAnimationSequence() }
    }

    private fun launchAnimationSequence() {
        animation = BouncingBallAnimation(binding.myDisplay.width, binding.myDisplay.height)
        animation?.let {
            setupAnimationObservers(it, binding.myDisplay)
            startAnimationLoop(it)
        }
    }

    private fun setupAnimationObservers(animation: Animation, display: Display) {
        disposables.dispose()
        disposables = CompositeDisposable()
        disposables.add(
            animation.position.flatMap { currentPosition ->
                var alpha = 0
                animation.path.map { previousPosition ->
                    alpha += 15
                    Triple(previousPosition, previousPosition == currentPosition, alpha)
                }
            }.subscribe { (position, isCurrentPosition, alpha) ->
                if (isCurrentPosition) {
                    display.addDrawingTask { canvas -> canvas.drawCircle(position.first, position.second, 12F, display.paintRed) }
                    display.invalidate()
                } else {
                    display.addDrawingTask { canvas -> canvas.drawCircle(position.first, position.second, 10F, display.paintGray.apply { this.alpha = alpha } ) }
                }
            })
        disposables.addAll(
            animation.finalPosition
                .doOnSubscribe {
                    binding.myButton.isEnabled = false
                    binding.myText.text = ""
                }
                .subscribe {
                    binding.myButton.isEnabled = true
                    binding.myText.text = getGameResult(animation, it)
                })
    }

    private fun startAnimationLoop(animation: Animation) {
        //see: https://discuss.kotlinlang.org/t/is-this-accessible-in-sams/1477
        val singleStep = object: Runnable {
            override fun run() {
                val stepSuccessful = animation.step()
                if (stepSuccessful) {
                    handler.postDelayed(this, 40)
                }
            }
        }
        handler.post(singleStep)
    }

    private fun getGameResult(animation: Animation, finalPosition: Position): String {
        return if (animation.isWinnerPosition(finalPosition)) {
            "YOU WON"
        } else {
            "YOU LOSE"
        }
    }
}
