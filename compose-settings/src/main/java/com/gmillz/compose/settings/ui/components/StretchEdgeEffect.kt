package com.gmillz.compose.settings.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.view.animation.AnimationUtils
import androidx.annotation.IntDef
import androidx.core.os.BuildCompat
import com.gmillz.compose.settings.util.EdgeEffectCompat
import kotlin.math.abs
import kotlin.math.sign


/**
 * This class performs the graphical effect used at the edges of scrollable widgets
 * when the user scrolls beyond the content bounds in 2D space.
 *
 *
 * EdgeEffect is stateful. Custom widgets using EdgeEffect should create an
 * instance for each edge that should show the effect, feed it input data using
 * the methods [.onAbsorb], [.onPull], and [.onRelease],
 * and draw the effect using [.draw] in the widget's overridden
 * [android.view.View.draw] method. If [.isFinished] returns
 * false after drawing, the edge effect's animation is not yet complete and the widget
 * should schedule another drawing pass to continue the animation.
 *
 *
 * When drawing, widgets should draw their main content and child views first,
 * usually by invoking `super.draw(canvas)` from an overridden `draw`
 * method. (This will invoke onDraw and dispatch drawing to child views as needed.)
 * The edge effect may then be drawn on top of the view's content using the
 * [.draw] method.
 */
class StretchEdgeEffect
/**
 * Construct a new EdgeEffect with a theme appropriate for the provided context.
 * @param context Context used to provide theming and resource information for the EdgeEffect
 */
    (context: Context?) : EdgeEffectCompat(context) {
    @IntDef(TYPE_NONE, TYPE_STRETCH)
    @Retention(AnnotationRetention.SOURCE)
    annotation class EdgeEffectType

    private var mDistance = 0f
    private var mVelocity // only for stretch animations
            = 0f
    private var mStartTime: Long = 0
    private var mState = STATE_IDLE
    private var mPullDistance = 0f
    private var mWidth = 0f
    private var mHeight = 0f

    @IntDef(POSITION_TOP, POSITION_BOTTOM, POSITION_LEFT, POSITION_RIGHT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class EdgeEffectPosition

    private var mInvalidate = EMPTY_RUNNABLE
    private var mPostInvalidateOnAnimation = EMPTY_RUNNABLE
    private val mTmpOut = FloatArray(5)

    constructor(
        context: Context?,
        invalidate: Runnable,
        postInvalidateOnAnimation: Runnable
    ) : this(context) {
        mInvalidate = invalidate
        mPostInvalidateOnAnimation = postInvalidateOnAnimation
    }

    fun setOnInvalidate(invalidate: Runnable) {
        mInvalidate = invalidate
    }

    fun setPostInvalidateOnAnimation(postInvalidateOnAnimation: Runnable) {
        mPostInvalidateOnAnimation = postInvalidateOnAnimation
    }

    @get:EdgeEffectType
    private val currentEdgeEffectBehavior: Int
        get() = if (!BuildCompat.isAtLeastO()) {
            TYPE_NONE
        } else if (!ValueAnimator.areAnimatorsEnabled()) {
            TYPE_NONE
        } else {
            TYPE_STRETCH
        }

    /**
     * Set the size of this edge effect in pixels.
     *
     * @param width Effect width in pixels
     * @param height Effect height in pixels
     */
    override fun setSize(width: Int, height: Int) {
        mWidth = width.toFloat()
        mHeight = height.toFloat()
    }

    /**
     * Reports if this EdgeEffect's animation is finished. If this method returns false
     * after a call to [.draw] the host widget should schedule another
     * drawing pass to continue the animation.
     *
     * @return true if animation is finished, false if drawing should continue on the next frame.
     */
    override fun isFinished(): Boolean {
        return mState == STATE_IDLE
    }

    /**
     * Immediately finish the current animation.
     * After this call [.isFinished] will return true.
     */
    override fun finish() {
        mState = STATE_IDLE
        mDistance = 0f
        mVelocity = 0f
    }

    private fun invalidateIfNotFinished() {
        if (!isFinished) {
            mInvalidate.run()
        }
    }

    /**
     * A view should call this when content is pulled away from an edge by the user.
     * This will update the state of the current visual effect and its associated animation.
     * The host view should always [android.view.View.invalidate] after this
     * and draw the results accordingly.
     *
     *
     * Views using EdgeEffect should favor [.onPull] when the displacement
     * of the pull point is known.
     *
     * @param deltaDistance Change in distance since the last call. Values may be 0 (no change) to
     * 1.f (full length of the view) or negative values to express change
     * back toward the edge reached to initiate the effect.
     */
    override fun onPull(deltaDistance: Float) {
        onPull(deltaDistance, 0.5f)
    }

    /**
     * A view should call this when content is pulled away from an edge by the user.
     * This will update the state of the current visual effect and its associated animation.
     * The host view should always [android.view.View.invalidate] after this
     * and draw the results accordingly.
     *
     * @param deltaDistance Change in distance since the last call. Values may be 0 (no change) to
     * 1.f (full length of the view) or negative values to express change
     * back toward the edge reached to initiate the effect.
     * @param displacement The displacement from the starting side of the effect of the point
     * initiating the pull. In the case of touch this is the finger position.
     * Values may be from 0-1.
     */
    override fun onPull(deltaDistance: Float, displacement: Float) {
        val edgeEffectBehavior = currentEdgeEffectBehavior
        if (edgeEffectBehavior == TYPE_NONE) {
            finish()
            return
        }
        val now = AnimationUtils.currentAnimationTimeMillis()
        if (mState != STATE_PULL) {
            // Restore the mPullDistance to the fraction it is currently showing -- we want
            // to "catch" the current stretch value.
            mPullDistance = mDistance
        }
        mState = STATE_PULL
        mStartTime = now
        mPullDistance += deltaDistance
        // Don't allow stretch beyond 1
        mPullDistance = Math.min(1f, mPullDistance)
        mDistance = Math.max(0f, mPullDistance)
        mVelocity = 0f
        if (mDistance == 0f) {
            mState = STATE_IDLE
        }
        invalidateIfNotFinished()
    }

    /**
     * A view should call this when content is pulled away from an edge by the user.
     * This will update the state of the current visual effect and its associated animation.
     * The host view should always [android.view.View.invalidate] after this
     * and draw the results accordingly. This works similarly to [.onPull],
     * but returns the amount of `deltaDistance` that has been consumed. If the
     * [.getDistance] is currently 0 and `deltaDistance` is negative, this
     * function will return 0 and the drawn value will remain unchanged.
     *
     * This method can be used to reverse the effect from a pull or absorb and partially consume
     * some of a motion:
     *
     * <pre class="prettyprint">
     * if (deltaY < 0) {
     * float consumed = edgeEffect.onPullDistance(deltaY / getHeight(), x / getWidth());
     * deltaY -= consumed * getHeight();
     * if (edgeEffect.getDistance() == 0f) edgeEffect.onRelease();
     * }
    </pre> *
     *
     * @param deltaDistance Change in distance since the last call. Values may be 0 (no change) to
     * 1.f (full length of the view) or negative values to express change
     * back toward the edge reached to initiate the effect.
     * @param displacement The displacement from the starting side of the effect of the point
     * initiating the pull. In the case of touch this is the finger position.
     * Values may be from 0-1.
     * @return The amount of `deltaDistance` that was consumed, a number between
     * 0 and `deltaDistance`.
     */
    override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
        val edgeEffectBehavior = currentEdgeEffectBehavior
        if (edgeEffectBehavior == TYPE_NONE) {
            return 0f
        }
        val finalDistance = Math.max(0f, deltaDistance + mDistance)
        val delta = finalDistance - mDistance
        if (delta == 0f && mDistance == 0f) {
            return 0f // No pull, don't do anything.
        }
        onPull(delta, displacement)
        return delta
    }

    /**
     * Returns the pull distance needed to be released to remove the showing effect.
     * It is determined by the [.onPull] `deltaDistance` and
     * any animating values, including from [.onAbsorb] and [.onRelease].
     *
     * This can be used in conjunction with [.onPullDistance] to
     * release the currently showing effect.
     *
     * @return The pull distance that must be released to remove the showing effect.
     */
    override fun getDistance(): Float {
        return mDistance
    }

    /**
     * Call when the object is released after being pulled.
     * This will begin the "decay" phase of the effect. After calling this method
     * the host view should [android.view.View.invalidate] and thereby
     * draw the results accordingly.
     */
    override fun onRelease() {
        mPullDistance = 0f
        if (mState != STATE_PULL && mState != STATE_PULL_DECAY) {
            return
        }
        mState = STATE_RECEDE
        mVelocity = 0f
        mStartTime = AnimationUtils.currentAnimationTimeMillis()
        invalidateIfNotFinished()
    }

    /**
     * Call when the effect absorbs an impact at the given velocity.
     * Used when a fling reaches the scroll boundary.
     *
     *
     * When using a [android.widget.Scroller] or [android.widget.OverScroller],
     * the method `getCurrVelocity` will provide a reasonable approximation
     * to use here.
     *
     * @param velocity Velocity at impact in pixels per second.
     */
    override fun onAbsorb(velocity: Int) {
        val edgeEffectBehavior = currentEdgeEffectBehavior
        if (edgeEffectBehavior == TYPE_STRETCH) {
            mState = STATE_RECEDE
            mVelocity = velocity * ON_ABSORB_VELOCITY_ADJUSTMENT
            mStartTime = AnimationUtils.currentAnimationTimeMillis()
            invalidateIfNotFinished()
        } else {
            finish()
        }
    }

    @JvmOverloads
    fun applyStretch(
        canvas: Canvas,
        @EdgeEffectPosition position: Int,
        translationX: Int = 0,
        translationY: Int = 0
    ) {
        mTmpOut[0] = 0f
        getScale(mTmpOut, position)
        if (mTmpOut[0] == 1f) {
            canvas.scale(
                mTmpOut[1],
                mTmpOut[2],
                mTmpOut[3] - translationX,
                mTmpOut[4] - translationY
            )
        }
    }

    fun getScale(out: FloatArray, @EdgeEffectPosition position: Int) {
        val edgeEffectBehavior = currentEdgeEffectBehavior
        if (edgeEffectBehavior == TYPE_STRETCH) {
            if (mState == STATE_RECEDE) {
                updateSpring()
            }
            if (mDistance != 0f) {
                val vec = dampStretchVector(Math.max(-1f, Math.min(1f, mDistance)))
                val scale = 1f + vec
                /* apply, scaleX, scaleY, pivotX, pivotY */out[0] = 1f
                when (position) {
                    POSITION_TOP -> {
                        out[1] = 1f
                        out[2] = scale
                        out[3] = 0f
                        out[4] = 0f
                    }

                    POSITION_BOTTOM -> {
                        out[1] = 1f
                        out[2] = scale
                        out[3] = 0f
                        out[4] = mHeight
                    }

                    POSITION_LEFT -> {
                        out[1] = scale
                        out[2] = 1f
                        out[3] = 0f
                        out[4] = 0f
                    }

                    POSITION_RIGHT -> {
                        out[1] = scale
                        out[2] = 1f
                        out[3] = mWidth
                        out[4] = 0f
                    }
                }
            }
        } else {
            // Animations have been disabled or this is TYPE_STRETCH and drawing into a Canvas
            // that isn't a Recording Canvas, so no effect can be shown. Just end the effect.
            mState = STATE_IDLE
            mDistance = 0f
            mVelocity = 0f
        }
        var oneLastFrame = false
        if ((mState == STATE_RECEDE && mDistance == 0f) && mVelocity == 0f) {
            mState = STATE_IDLE
            oneLastFrame = true
        }
        if (mState != STATE_IDLE || oneLastFrame) {
            mPostInvalidateOnAnimation.run()
        }
    }

    /**
     * Draw into the provided canvas. Assumes that the canvas has been rotated
     * accordingly and the size has been set. The effect will be drawn the full
     * width of X=0 to X=width, beginning from Y=0 and extending to some factor <
     * 1.f of height. The effect will only be visible on a
     * hardware canvas, e.g. [RenderNode.beginRecording].
     *
     * @param canvas Canvas to draw into
     * @return true if drawing should continue beyond this frame to continue the
     * animation
     */
    override fun draw(canvas: Canvas): Boolean {
        return false
    }

    /**
     * Return the maximum height that the edge effect will be drawn at given the original
     * [input size][.setSize].
     * @return The maximum height of the edge effect
     */
    override fun getMaxHeight(): Int {
        return mHeight.toInt()
    }

    private fun updateSpring() {
        val time = AnimationUtils.currentAnimationTimeMillis()
        val deltaT = (time - mStartTime) / 1000f // Convert from millis to seconds
        if (deltaT < 0.001f) {
            return  // Must have at least 1 ms difference
        }
        mStartTime = time
        if (((abs(mVelocity) <= LINEAR_VELOCITY_TAKE_OVER) && abs(mDistance * mHeight) < LINEAR_DISTANCE_TAKE_OVER) && Math.signum(
                mVelocity
            ) == -sign(mDistance)
        ) {
            // This is close. The spring will slowly reach the destination. Instead, we
            // will interpolate linearly so that it arrives at its destination quicker.
            mVelocity = sign(mVelocity) * LINEAR_VELOCITY_TAKE_OVER
            val targetDistance = mDistance + mVelocity * deltaT / mHeight
            if (sign(targetDistance) != sign(mDistance)) {
                // We have arrived
                mDistance = 0f
                mVelocity = 0f
            } else {
                mDistance = targetDistance
            }
            return
        }
        val mDampedFreq = NATURAL_FREQUENCY * Math.sqrt(1 - DAMPING_RATIO * DAMPING_RATIO)

        // We're always underdamped, so we can use only those equations:
        val cosCoeff = (mDistance * mHeight).toDouble()
        val sinCoeff = 1 / mDampedFreq * ((DAMPING_RATIO * NATURAL_FREQUENCY
                * mDistance * mHeight) + mVelocity)
        val distance = (Math.pow(Math.E, -DAMPING_RATIO * NATURAL_FREQUENCY * deltaT)
                * (cosCoeff * Math.cos(mDampedFreq * deltaT)
                + sinCoeff * Math.sin(mDampedFreq * deltaT)))
        val velocity = (distance * -NATURAL_FREQUENCY * DAMPING_RATIO
                + Math.pow(Math.E, -DAMPING_RATIO * NATURAL_FREQUENCY * deltaT)
                * (-mDampedFreq * cosCoeff * Math.sin(mDampedFreq * deltaT)
                + mDampedFreq * sinCoeff * Math.cos(mDampedFreq * deltaT)))
        mDistance = distance.toFloat() / mHeight
        mVelocity = velocity.toFloat()
        if (mDistance > 1f) {
            mDistance = 1f
            mVelocity = 0f
        }
        if (isAtEquilibrium) {
            mDistance = 0f
            mVelocity = 0f
        }
    }// in pixels

    // Don't allow displacement to drop below 0. We don't want it stretching the opposite
    // direction if it is flung that way. We also want to stop the animation as soon as
    // it gets very close to its destination.
    /**
     * @return true if the spring used for calculating the stretch animation is
     * considered at rest or false if it is still animating.
     */
    private val isAtEquilibrium: Boolean
        private get() {
            val displacement = (mDistance * mHeight).toDouble() // in pixels
            val velocity = mVelocity.toDouble()

            // Don't allow displacement to drop below 0. We don't want it stretching the opposite
            // direction if it is flung that way. We also want to stop the animation as soon as
            // it gets very close to its destination.
            return displacement < 0 || (Math.abs(velocity) < VELOCITY_THRESHOLD
                    && displacement < VALUE_THRESHOLD)
        }

    private fun dampStretchVector(normalizedVec: Float): Float {
        val sign = if (normalizedVec > 0) 1f else -1f
        val overscroll = Math.abs(normalizedVec)
        val linearIntensity = LINEAR_STRETCH_INTENSITY * overscroll
        val scalar = Math.E / SCROLL_DIST_AFFECTED_BY_EXP_STRETCH
        val expIntensity = EXP_STRETCH_INTENSITY * (1 - Math.exp(-overscroll * scalar))
        return sign * (linearIntensity + expIntensity).toFloat()
    }

    companion object {
        /**
         * Completely disable edge effect
         */
        private const val TYPE_NONE = -1

        /**
         * Use a stretch for the edge effect.
         */
        private const val TYPE_STRETCH = 1

        /**
         * The velocity threshold before the spring animation is considered settled.
         * The idea here is that velocity should be less than 0.1 pixel per second.
         */
        private const val VELOCITY_THRESHOLD = 0.01

        /**
         * The speed at which we should start linearly interpolating to the destination.
         * When using a spring, as it gets closer to the destination, the speed drops off exponentially.
         * Instead of landing very slowly, a better experience is achieved if the final
         * destination is arrived at quicker.
         */
        private const val LINEAR_VELOCITY_TAKE_OVER = 200f

        /**
         * The value threshold before the spring animation is considered close enough to
         * the destination to be settled. This should be around 0.01 pixel.
         */
        private const val VALUE_THRESHOLD = 0.001

        /**
         * The maximum distance at which we should start linearly interpolating to the destination.
         * When using a spring, as it gets closer to the destination, the speed drops off exponentially.
         * Instead of landing very slowly, a better experience is achieved if the final
         * destination is arrived at quicker.
         */
        private const val LINEAR_DISTANCE_TAKE_OVER = 8.0

        /**
         * The natural frequency of the stretch spring.
         */
        private const val NATURAL_FREQUENCY = 24.657

        /**
         * The damping ratio of the stretch spring.
         */
        private const val DAMPING_RATIO = 0.98

        /**
         * The variation of the velocity for the stretch effect when it meets the bound.
         * if value is > 1, it will accentuate the absorption of the movement.
         */
        private const val ON_ABSORB_VELOCITY_ADJUSTMENT = 13f
        private const val LINEAR_STRETCH_INTENSITY = 0.016f
        private const val EXP_STRETCH_INTENSITY = 0.016f
        private const val SCROLL_DIST_AFFECTED_BY_EXP_STRETCH = 0.33f
        private const val TAG = "EdgeEffect"
        private const val STATE_IDLE = 0
        private const val STATE_PULL = 1
        private const val STATE_RECEDE = 3
        private const val STATE_PULL_DECAY = 4
        const val POSITION_TOP = 0
        const val POSITION_BOTTOM = 1
        const val POSITION_LEFT = 2
        const val POSITION_RIGHT = 3
        private val EMPTY_RUNNABLE = Runnable {}
    }
}
