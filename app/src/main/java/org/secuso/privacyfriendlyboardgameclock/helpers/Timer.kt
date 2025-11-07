package org.secuso.privacyfriendlyboardgameclock.helpers

import android.os.Handler
import android.os.Looper

interface ITimer {
    val measuredTime: Long
    val currentElapsedTime: Long
    val isRunning: Boolean
    fun reset()
    fun start()
    fun stop()
    fun pause()
    fun resume()
    fun restart()
}

class Timer(startTime: Long = 0L): ITimer {
    private var startTime: Long? = null
    private var isPaused = false
    override var measuredTime = startTime
        private set
    override val currentElapsedTime: Long
        get() {
            return if (isRunning) {
                measuredTime + (System.currentTimeMillis() - measuredTime)
            } else {
                measuredTime
            }
        }
    override val isRunning: Boolean
        get() = startTime != null
    override fun reset() {
        startTime = null
        measuredTime = 0
    }

    override fun restart() {
        reset()
        start()
    }
    override fun start() {
        if (startTime == null) {
            startTime = System.currentTimeMillis()
        }
        isPaused = false
    }
    override fun stop() {
        if (startTime != null) {
            measuredTime = currentElapsedTime
        }
        isPaused = false
    }

    override fun pause() {
        if(isRunning) {
            stop()
            isPaused = true
        }
    }

    override fun resume() {
        if (!isRunning && isPaused) {
            start()
            isPaused = false
        }
    }
}

class CountdownTimer(private val timeToMeasure: Long, val onFinish: () -> Unit): ITimer {
    private val timer = Timer()
    private var remainingTime: Long = timeToMeasure
    val currentRemainingTime
        get() = remainingTime - timer.currentElapsedTime

    override fun reset() {
        timer.reset()
        remainingTime = timeToMeasure
    }

    override fun restart() {
        reset()
        start()
    }

    override fun start() {
        if (!timer.isRunning) {
            timer.start()
            // WARNING:
            // This could result in many actions called at roughly the same time
            // iff the countdown timer is started and stopped in near sub-milli time.
            // However, as we only start and stop the timer in expected human time (at least 30 ms),
            // this action should be called at most once every frame.
            Handler(Looper.getMainLooper()).postDelayed({
                if (currentRemainingTime <= 0) {
                    onFinish()
                }
            }, currentRemainingTime)
        }
    }
    override fun stop() = timer.stop()
    override val currentElapsedTime: Long
        get() = timer.currentElapsedTime
    override val isRunning: Boolean
        get() = timer.isRunning
    override val measuredTime: Long
        get() = timer.measuredTime

    override fun pause() = timer.pause()
    override fun resume() = timer.resume()

}