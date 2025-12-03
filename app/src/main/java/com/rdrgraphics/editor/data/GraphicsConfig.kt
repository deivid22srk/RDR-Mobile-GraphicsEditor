package com.rdrgraphics.editor.data

data class GraphicsConfig(
    val fullscreen: Boolean = false,
    val monitor: Int = 1,
    val resolutionX: Int = 1600,
    val resolutionY: Int = 720,
    val refreshRate: Float = 0f,
    val frameRateLimit: Int = 0,
    val vsync: Int = 1,
    val tripleBuffer: Boolean = true,
    val dynamicResolution: Boolean = true,
    val dynamicResolutionTargetFramerate: Int = 0,
    val dynamicResolutionMinScaleIndex: Int = 5,
    val minAnisotropicFiltering: Int = 4,
    val motionBlurStyle: Int = 1,
    val motionBlurStrength: Float = 0.1f,
    val shadowQuality: Int = 0,
    val shadowSoftness: Int = 0,
    val shadowBlend: Int = 0,
    val worldStreamingRadius: Float = 100f,
    val terrainStreamingFactor: Float = 1f,
    val treeLevelOfDetail: Float = 1f,
    val treeImposterHighLodStreamingDistance: Float = 0f,
    val grassStreamingDistance: Float = 0f,
    val bFocusPaused: Boolean = true,
    val bConstrainMouse: Boolean = false,
    val hdr: Boolean = false,
    val peakBrightness: Float = 100f,
    val paperWhite: Float = 80f,
    val aaAntiAliasing: Int = 1,
    val fsr3UpscalingQuality: Int = 0,
    val fsr3AdditionalSharpness: Float = 0f,
    val nvidiaReflex: Int = 0,
    val dlssUpscalingQuality: Int = 0,
    val dlssFrameGeneration: Int = 0,
    val mobilePreset: Int = 0,
    val screenPercentage: Float = 1f
) {
    fun toXml(): String {
        return """<?xml version="1.0" encoding="UTF-8"?>
<rage__GraphicsOptions v="83.0">
	<Fullscreen value="$fullscreen"/>
	<Monitor value="$monitor"/>
	<ResolutionX value="$resolutionX"/>
	<ResolutionY value="$resolutionY"/>
	<RefreshRate value="${String.format("%.6f", refreshRate)}"/>
	<FrameRateLimit value="$frameRateLimit"/>
	<Vsync value="$vsync"/>
	<TripleBuffer value="$tripleBuffer"/>
	<DynamicResolution value="$dynamicResolution"/>
	<DynamicResolutionTargetFramerate value="$dynamicResolutionTargetFramerate"/>
	<DynamicResolutionMinScaleIndex value="$dynamicResolutionMinScaleIndex"/>
	<MinAnisotropicFiltering value="$minAnisotropicFiltering"/>
	<MotionBlurStyle value="$motionBlurStyle"/>
	<MotionBlurStrength value="${String.format("%.6f", motionBlurStrength)}"/>
	<ShadowQuality value="$shadowQuality"/>
	<ShadowSoftness value="$shadowSoftness"/>
	<ShadowBlend value="$shadowBlend"/>
	<WorldStreamingRadius value="${String.format("%.6f", worldStreamingRadius)}"/>
	<TerrainStreamingFactor value="${String.format("%.6f", terrainStreamingFactor)}"/>
	<TreeLevelOfDetail value="${String.format("%.6f", treeLevelOfDetail)}"/>
	<TreeImposterHighLodStreamingDistance value="${String.format("%.6f", treeImposterHighLodStreamingDistance)}"/>
	<GrassStreamingDistance value="${String.format("%.6f", grassStreamingDistance)}"/>
	<bFocusPaused value="$bFocusPaused"/>
	<bConstrainMouse value="$bConstrainMouse"/>
	<HDR value="$hdr"/>
	<PeakBrightness value="${String.format("%.6f", peakBrightness)}"/>
	<PaperWhite value="${String.format("%.6f", paperWhite)}"/>
	<aaAntiAliasing value="$aaAntiAliasing"/>
	<FSR3UpscalingQuality value="$fsr3UpscalingQuality"/>
	<FSR3AdditionalSharpness value="${String.format("%.6f", fsr3AdditionalSharpness)}"/>
	<NVIDIAReflex value="$nvidiaReflex"/>
	<DLSSUpscalingQuality value="$dlssUpscalingQuality"/>
	<DLSSFrameGeneration value="$dlssFrameGeneration"/>
	<MobilePreset value="$mobilePreset"/>
	<ScreenPercentage value="${String.format("%.6f", screenPercentage)}"/>
</rage__GraphicsOptions>"""
    }
}
