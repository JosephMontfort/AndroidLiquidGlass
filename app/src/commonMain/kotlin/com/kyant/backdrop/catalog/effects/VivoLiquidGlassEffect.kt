package com.kyant.backdrop.catalog.effects

import androidx.compose.ui.geometry.Size
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import org.intellij.lang.annotations.Language
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1:1 Implementation of Vivo OriginOS 7 Liquid Glass AGSL Shader.
 * Extracted directly from vivo-res.apk (res/raw/shader_liquid_glass_effect.agsl).
 *
 * Implements:
 * 1. Boundary Refraction Layer (RERF_MAG = 0.08, cosBoundary curvature)
 * 2. Rim Reflection Layer (REFL_OFFSET_MIN = 0.07, REFL_OFFSET_MAG = 0.005)
 * 3. 3D Light Specular Highlights (uLight1, uLight2 vectors)
 * 4. Analytic Continuous G2/SDF Boundary
 */
@Language("AGSL")
internal const val VivoLiquidGlassShaderString = """
uniform shader content;

uniform float2 size;
uniform float cornerRadius;
uniform float uGlobalIntensity;
uniform float uLightIntensity;
uniform float3 uLight1;
uniform float3 uLight2;

const float PI = 3.141592653589793;
const float EDGE_DIM = 0.35;
const float REFL_OFFSET_MIN = 0.05;
const float REFL_OFFSET_MAG = 0.005;
const float RERF_DIM = 0.85;
const float RERF_MAG = 0.08;
const float4 RIM_LIGHT_COLOR = float4(1.0, 1.0, 1.0, 0.45);

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
        float len = length(max(cornerCoord, 0.0));
        return len > 1e-5 ? sign(coord) * (max(cornerCoord, 0.0) / len) : float2(0.0);
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}

float3 blendScreen(float3 a, float3 b) {
    return 1.0 - (1.0 - a) * (1.0 - b);
}

float3 blendLighten(float3 a, float3 b) {
    return max(a, b);
}

float4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 p = coord - halfSize;
    float r = clamp(cornerRadius, 0.0, min(halfSize.x, halfSize.y));

    float dist = sdRoundedRect(p, halfSize, r);
    float boxMask = clamp(-dist + 0.5, 0.0, 1.0);

    if (boxMask <= 0.0) {
        return float4(0.0);
    }

    float4 baseTex = content.eval(coord);
    float2 norm = gradSdRoundedRect(p, halfSize, r);
    float normDist = clamp(dist / max(1.0, r), -1.0, 1.0);

    // 1. Vivo Refraction Layer
    float boundary = clamp((normDist - (-RERF_DIM)) / (0.001 - (-RERF_DIM)), 0.0, 1.0);
    boundary = mix(boundary, 0.0, smoothstep(0.0, 0.001, normDist));
    float cosBoundary = 1.0 - cos(boundary * PI * 0.5);
    float interior = smoothstep(0.001, 0.0, normDist);

    float2 refrOffset = -norm * RERF_MAG * interior * cosBoundary;
    float2 warpedCoord = coord + refrOffset * size;
    float4 blurWarped = content.eval(warpedCoord);

    float3 col = blurWarped.rgb;

    // 2. Vivo Tint & Specular Rim Light Layer
    float edge = clamp((normDist - (-EDGE_DIM)) / (0.001 - (-EDGE_DIM)), 0.0, 1.0);
    float3 grad = normalize(float3(norm.x, -norm.y, 0.0));
    float lighting1 = max(0.0, dot(grad, normalize(uLight1)));
    float lighting2 = max(0.0, dot(grad, normalize(uLight2)));

    float light = lighting1 + lighting2 + 0.4;
    light = smoothstep(0.0, 1.0, light / 1.8);
    light = mix(-1.0, 2.0, light);
    light = max(light, 0.0);

    float rimLightIntensity = light * uLightIntensity;
    float3 rimLight = RIM_LIGHT_COLOR.rgb * RIM_LIGHT_COLOR.a;

    float cosEdge = 1.0 - cos(edge * PI * 0.5);
    float2 reflOffset = (REFL_OFFSET_MIN + REFL_OFFSET_MAG * cosEdge) * norm;
    float4 reflTex = content.eval(coord + reflOffset * size);
    float3 reflectionColor = clamp(reflTex.rgb, 0.0, 1.0);

    float3 mergedEdgeColor = blendScreen(rimLight, reflectionColor) * rimLightIntensity;
    float3 edgeColor = blendLighten(col, mergedEdgeColor);

    col = mix(col, edgeColor, cosEdge);
    col = clamp(col, 0.0, 1.0);

    // 3. Blend with base layer via global intensity
    float3 finalRgb = mix(baseTex.rgb, col, uGlobalIntensity);
    return float4(finalRgb * boxMask, boxMask);
}
"""

fun BackdropEffectScope.vivoLiquidGlass(
    size: Size,
    cornerRadius: Float,
    globalIntensity: Float = 1.0f,
    lightIntensity: Float = 1.25f,
    lightAngle: Float = 45f
) {
    if (!isRuntimeShaderSupported()) return

    val rad = lightAngle * (PI / 180.0)
    val light1X = cos(rad).toFloat()
    val light1Y = sin(rad).toFloat()
    val light1Z = 0.8f

    val light2X = -0.5f
    val light2Y = 0.866f
    val light2Z = 0.5f

    runtimeShaderEffect(
        key = "VivoLiquidGlass",
        shaderString = VivoLiquidGlassShaderString,
        uniformShaderName = "content"
    ) {
        setFloatUniform("size", size.width, size.height)
        setFloatUniform("cornerRadius", cornerRadius)
        setFloatUniform("uGlobalIntensity", globalIntensity)
        setFloatUniform("uLightIntensity", lightIntensity)
        setFloatUniform("uLight1", light1X, light1Y, light1Z)
        setFloatUniform("uLight2", light2X, light2Y, light2Z)
    }
}
