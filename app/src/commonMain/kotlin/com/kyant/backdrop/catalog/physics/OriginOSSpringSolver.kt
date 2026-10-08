package com.kyant.backdrop.catalog.physics

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 1:1 Port of Vivo OriginOS 7 Phase Spring Physics Engine.
 *
 * Source receipts:
 * - ConversionUtils.java (com.originui.animation.utils.ConversionUtils)
 * - SpringFormulaInterpolator.java (com.originui.animation.interpolator.SpringFormulaInterpolator)
 * - SpringForce.java (com.originui.animation.google.SpringForce)
 * - VLiquidConfig.java (com.originui.uidesign.liquid.VLiquidConfig)
 */
object OriginOSConversionUtils {
    /**
     * Converts UI bounce factor to damping ratio (zeta).
     * Formula: zeta = 1.0 / (bounce + 1.0)
     */
    fun convertBounceToDampingRatio(bounce: Float): Float {
        return 1.0f / (bounce + 1.0f)
    }

    /**
     * Phase 1 natural frequency (omega_n1).
     * Formula: omega_n1 = PI / (duration * sqrt(1.0 - zeta^2))
     */
    fun convertDurationPhase1ToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        require(dampingRatio < 1.0f) { "Phase 1 dampingRatio must be < 1.0 (underdamped)" }
        return (PI / (duration * sqrt(1.0 - dampingRatio * dampingRatio))).toFloat()
    }

    /**
     * Phase 2 natural frequency (omega_n2).
     * Formula: omega_n2 = ln(1000.0 / sqrt(1.0 - zeta^2)) / (zeta * duration)
     */
    fun convertDurationPhase2ToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        require(dampingRatio < 1.0f) { "Phase 2 dampingRatio must be < 1.0" }
        return (ln(1000.0 / sqrt(1.0 - dampingRatio * dampingRatio)) / (dampingRatio * duration)).toFloat()
    }

    /**
     * Single segment natural frequency.
     */
    fun convertDurationToNaturalFreq(duration: Float, dampingRatio: Float): Float {
        if (dampingRatio == 1.0f) {
            val factor = (2.0 * PI / ln(2.0)).toFloat()
            return factor / duration
        }
        val num = dampingRatio.toDouble() / (sqrt(1.0 - dampingRatio * dampingRatio) * 0.001)
        return (ln(num) / (dampingRatio * duration)).toFloat()
    }
}

/**
 * Two-stage phase spring solver matching OriginOS 7's SpringForce.
 */
class OriginOSTwoPhaseSpring(
    val duration1: Float,
    val bounce1: Float,
    val duration2: Float,
    val bounce2: Float,
    val startVelocity: Float
) {
    val zeta1: Float = OriginOSConversionUtils.convertBounceToDampingRatio(bounce1)
    val omegaN1: Float = OriginOSConversionUtils.convertDurationPhase1ToNaturalFreq(duration1, zeta1)
    val omegaD1: Float = (omegaN1 * sqrt(1.0 - zeta1 * zeta1)).toFloat()
    val crestTime: Float = (PI / omegaD1).toFloat()

    val zeta2: Float = OriginOSConversionUtils.convertBounceToDampingRatio(bounce2)
    val omegaN2: Float = OriginOSConversionUtils.convertDurationPhase2ToNaturalFreq(duration2, zeta2)
    val omegaD2: Float = (omegaN2 * sqrt(1.0 - zeta2 * zeta2)).toFloat()

    val estimatedDuration: Float = crestTime + duration2

    /**
     * Evaluates position at time t (seconds) from startVal to endVal.
     */
    fun evaluate(t: Float, startVal: Float, endVal: Float): Float {
        if (t <= 0f) return startVal
        val displacement = startVal - endVal

        if (t <= crestTime) {
            // Phase 1 underdamped motion
            val decay = exp(-zeta1 * omegaN1 * t)
            val c1 = displacement
            val c2 = (zeta1 * omegaN1 * displacement + startVelocity) / omegaD1
            val sinVal = sin(omegaD1 * t)
            val cosVal = cos(omegaD1 * t)
            return endVal + decay * (c1 * cosVal + c2 * sinVal)
        } else {
            // State at crest time
            val decayCrest = exp(-zeta1 * omegaN1 * crestTime)
            val c1 = displacement
            val c2 = (zeta1 * omegaN1 * displacement + startVelocity) / omegaD1
            val sinCrest = sin(omegaD1 * crestTime)
            val cosCrest = cos(omegaD1 * crestTime)
            val xCrest = endVal + decayCrest * (c1 * cosCrest + c2 * sinCrest)

            // Velocity at crest time
            val vCrest = decayCrest * (
                (-zeta1 * omegaN1) * (c1 * cosCrest + c2 * sinCrest) +
                    omegaD1 * (-c1 * sinCrest + c2 * cosCrest)
            )

            // Phase 2 underdamped motion from crest
            val dt = t - crestTime
            val decay2 = exp(-zeta2 * omegaN2 * dt)
            val disp2 = xCrest - endVal
            val c1P2 = disp2
            val c2P2 = (zeta2 * omegaN2 * disp2 + vCrest) / omegaD2
            val sinVal2 = sin(omegaD2 * dt)
            val cosVal2 = cos(omegaD2 * dt)
            return endVal + decay2 * (c1P2 * cosVal2 + c2P2 * sinVal2)
        }
    }
}

/**
 * Single-stage spring solver matching OriginOS 7's single segment spring.
 */
class OriginOSSingleSpring(
    val duration: Float,
    val bounce: Float,
    val startVelocity: Float
) {
    val zeta: Float = OriginOSConversionUtils.convertBounceToDampingRatio(bounce)
    val omegaN: Float = OriginOSConversionUtils.convertDurationToNaturalFreq(duration, zeta)
    val omegaD: Float = (omegaN * sqrt((1.0 - zeta * zeta).coerceAtLeast(0.0001))).toFloat()
    val estimatedDuration: Float = duration

    fun evaluate(t: Float, startVal: Float, endVal: Float): Float {
        if (t <= 0f) return startVal
        if (t >= estimatedDuration * 1.5f) return endVal

        val displacement = startVal - endVal
        val decay = exp(-zeta * omegaN * t)
        val c1 = displacement
        val c2 = if (omegaD > 0.001f) (zeta * omegaN * displacement + startVelocity) / omegaD else 0f
        val sinVal = sin(omegaD * t)
        val cosVal = cos(omegaD * t)
        return endVal + decay * (c1 * cosVal + c2 * sinVal)
    }
}

/**
 * Exact OriginOS 7 VListPopupWindow Spring Configurations.
 * Extracted directly from VLiquidConfig.VListPopupWindow() in frameworkui.apk.
 */
object OriginOSPopupConfigs {
    // ENTRY ANIMATIONS (Expanding anchor -> popup menu)
    // Scale X: multiple(0.1, 1.0, 0.36, 0.28, 0.28, 0.01, 0.0)
    val entryScaleX = OriginOSTwoPhaseSpring(
        duration1 = 0.36f,
        bounce1 = 0.28f,
        duration2 = 0.28f,
        bounce2 = 0.01f,
        startVelocity = 0.0f
    )

    // Scale Y: multiple(0.1, 1.0, 0.29, 0.60, 0.71, 0.01, 5.0)
    val entryScaleY = OriginOSTwoPhaseSpring(
        duration1 = 0.29f,
        bounce1 = 0.60f,
        duration2 = 0.71f,
        bounce2 = 0.01f,
        startVelocity = 5.0f
    )

    // Translation X: multiple(startOffset, 0.0, 0.44, 0.24, 0.47, 0.01, 0.0)
    val entryTranslationX = OriginOSTwoPhaseSpring(
        duration1 = 0.44f,
        bounce1 = 0.24f,
        duration2 = 0.47f,
        bounce2 = 0.01f,
        startVelocity = 0.0f
    )

    // Translation Y: multiple(startOffset, 0.0, 0.32, 0.55, 0.54, 0.01, 8.0)
    val entryTranslationY = OriginOSTwoPhaseSpring(
        duration1 = 0.32f,
        bounce1 = 0.55f,
        duration2 = 0.54f,
        bounce2 = 0.01f,
        startVelocity = 8.0f
    )

    // EXIT ANIMATIONS (Collapsing popup menu -> anchor)
    // Scale X: single(1.0, 0.05, 0.32, 0.01, 8.0)
    val exitScaleX = OriginOSSingleSpring(
        duration = 0.32f,
        bounce = 0.01f,
        startVelocity = 8.0f
    )

    // Scale Y: single(1.0, 0.05, 0.39, 0.10, 8.0)
    val exitScaleY = OriginOSSingleSpring(
        duration = 0.39f,
        bounce = 0.10f,
        startVelocity = 8.0f
    )

    // Translation X: single(0.0, targetOffset, 0.26, 0.0, 8.0)
    val exitTranslationX = OriginOSSingleSpring(
        duration = 0.26f,
        bounce = 0.0f,
        startVelocity = 8.0f
    )

    // Translation Y: single(0.0, targetOffset, 0.41, 0.0, 0.0)
    val exitTranslationY = OriginOSSingleSpring(
        duration = 0.41f,
        bounce = 0.0f,
        startVelocity = 0.0f
    )

    // Total entry motion duration is the max of the individual estimated durations
    val maxEntryDuration: Float = maxOf(
        entryScaleX.estimatedDuration,
        entryScaleY.estimatedDuration,
        entryTranslationX.estimatedDuration,
        entryTranslationY.estimatedDuration
    ) // ~1.00s

    // Total exit motion duration
    val maxExitDuration: Float = maxOf(
        exitScaleX.estimatedDuration,
        exitScaleY.estimatedDuration,
        exitTranslationX.estimatedDuration,
        exitTranslationY.estimatedDuration
    ) // ~0.41s
}
