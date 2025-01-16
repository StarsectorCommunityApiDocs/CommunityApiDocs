package com.fs.starfarer.api.util;

import java.util.Random;

/**
 * Timer utility that fires at a random interval between minInterval and maxInterval.
 * <p>
 * To use, call {@code intervalUtil.advance(float amount);} in an {@code advance(float amount)} method (i.e. every frame).
 * Then, call {@code intervalUtil.intervalElapsed()} each frame to check if the timer has elapsed.
 * <p/>
 * It automatically resets itself after it elapses.
 *
 * @param minInterval minimum duration of the timer, in seconds
 * @param maxInterval maximum duration of the timer, in seconds
 */
public class IntervalUtil {

	private float minInterval;
	private float maxInterval;

	private float currInterval;
	private float elapsed = 0;
	private boolean intervalElapsed = false;

	private Random random;

    /**
     * Timer utility that fires at a random interval between minInterval and maxInterval.
     * <p>
     * To use, call {@code intervalUtil.advance(float amount);} in an {@code advance(float amount)} method (i.e. every frame).
     * Then, call {@code intervalUtil.intervalElapsed()} each frame to check if the timer has elapsed.
     * <p/>
     * It automatically resets itself after it elapses.
     *
     * @param minInterval minimum duration of the timer, in seconds
     * @param maxInterval maximum duration of the timer, in seconds
     */
    public IntervalUtil(float minInterval, float maxInterval) {
        setInterval(minInterval, maxInterval);
	}

	public void forceCurrInterval(float value) {
		currInterval = value;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public void randomize() {
		if (random != null) {
			advance(random.nextFloat() * minInterval);
		} else {
			advance((float) Math.random() * minInterval);
		}
	}

	public void forceIntervalElapsed() {
		elapsed = currInterval;
	}


	public float getElapsed() {
		return elapsed;
	}

	private void nextInterval() {
		if (random != null) {
			currInterval = minInterval + (maxInterval - minInterval) * random.nextFloat();
		} else {
			currInterval = minInterval + (maxInterval - minInterval) * (float) Math.random();
		}
		elapsed = 0;
		intervalElapsed = false;
	}

	public void advance(float amount) {
		if (intervalElapsed) {
			nextInterval();
		}
		elapsed += amount;
		if (elapsed >= currInterval) {
			intervalElapsed = true;
		}
	}

	/**
	 * Returns true once and only once when the current interval is over.  Must be called every frame, otherwise
	 * the time it returns true might be missed.
	 * @return
	 */
	public boolean intervalElapsed() {
		return intervalElapsed;
	}

	public float getIntervalDuration() {
		return currInterval;
	}

	public void setInterval(float min, float max) {
		this.minInterval = min;
		this.maxInterval = max;
		nextInterval();
	}

	public void setElapsed(float elapsed) {
		this.elapsed = elapsed;
	}

	public float getMinInterval() {
		return minInterval;
	}

	public float getMaxInterval() {
		return maxInterval;
	}


}
