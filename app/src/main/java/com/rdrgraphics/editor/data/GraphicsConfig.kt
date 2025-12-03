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
    
    fun toUpdateMap(): Map<String, String> {
        return mapOf(
            "Fullscreen" to fullscreen.toString(),
            "Monitor" to monitor.toString(),
            "ResolutionX" to resolutionX.toString(),
            "ResolutionY" to resolutionY.toString(),
            "RefreshRate" to String.format("%.6f", refreshRate),
            "FrameRateLimit" to frameRateLimit.toString(),
            "Vsync" to vsync.toString(),
            "TripleBuffer" to tripleBuffer.toString(),
            "DynamicResolution" to dynamicResolution.toString(),
            "DynamicResolutionTargetFramerate" to dynamicResolutionTargetFramerate.toString(),
            "DynamicResolutionMinScaleIndex" to dynamicResolutionMinScaleIndex.toString(),
            "MinAnisotropicFiltering" to minAnisotropicFiltering.toString(),
            "MotionBlurStyle" to motionBlurStyle.toString(),
            "MotionBlurStrength" to String.format("%.6f", motionBlurStrength),
            "ShadowQuality" to shadowQuality.toString(),
            "ShadowSoftness" to shadowSoftness.toString(),
            "ShadowBlend" to shadowBlend.toString(),
            "WorldStreamingRadius" to String.format("%.6f", worldStreamingRadius),
            "TerrainStreamingFactor" to String.format("%.6f", terrainStreamingFactor),
            "TreeLevelOfDetail" to String.format("%.6f", treeLevelOfDetail),
            "TreeImposterHighLodStreamingDistance" to String.format("%.6f", treeImposterHighLodStreamingDistance),
            "GrassStreamingDistance" to String.format("%.6f", grassStreamingDistance),
            "bFocusPaused" to bFocusPaused.toString(),
            "bConstrainMouse" to bConstrainMouse.toString(),
            "HDR" to hdr.toString(),
            "PeakBrightness" to String.format("%.6f", peakBrightness),
            "PaperWhite" to String.format("%.6f", paperWhite),
            "aaAntiAliasing" to aaAntiAliasing.toString(),
            "FSR3UpscalingQuality" to fsr3UpscalingQuality.toString(),
            "FSR3AdditionalSharpness" to String.format("%.6f", fsr3AdditionalSharpness),
            "NVIDIAReflex" to nvidiaReflex.toString(),
            "DLSSUpscalingQuality" to dlssUpscalingQuality.toString(),
            "DLSSFrameGeneration" to dlssFrameGeneration.toString(),
            "MobilePreset" to mobilePreset.toString(),
            "ScreenPercentage" to String.format("%.6f", screenPercentage)
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, String>): GraphicsConfig {
            return GraphicsConfig(
                fullscreen = map["Fullscreen"]?.toBoolean() ?: false,
                monitor = map["Monitor"]?.toIntOrNull() ?: 1,
                resolutionX = map["ResolutionX"]?.toIntOrNull() ?: 1600,
                resolutionY = map["ResolutionY"]?.toIntOrNull() ?: 720,
                refreshRate = map["RefreshRate"]?.toFloatOrNull() ?: 0f,
                frameRateLimit = map["FrameRateLimit"]?.toIntOrNull() ?: 0,
                vsync = map["Vsync"]?.toIntOrNull() ?: 1,
                tripleBuffer = map["TripleBuffer"]?.toBoolean() ?: true,
                dynamicResolution = map["DynamicResolution"]?.toBoolean() ?: true,
                dynamicResolutionTargetFramerate = map["DynamicResolutionTargetFramerate"]?.toIntOrNull() ?: 0,
                dynamicResolutionMinScaleIndex = map["DynamicResolutionMinScaleIndex"]?.toIntOrNull() ?: 5,
                minAnisotropicFiltering = map["MinAnisotropicFiltering"]?.toIntOrNull() ?: 4,
                motionBlurStyle = map["MotionBlurStyle"]?.toIntOrNull() ?: 1,
                motionBlurStrength = map["MotionBlurStrength"]?.toFloatOrNull() ?: 0.1f,
                shadowQuality = map["ShadowQuality"]?.toIntOrNull() ?: 0,
                shadowSoftness = map["ShadowSoftness"]?.toIntOrNull() ?: 0,
                shadowBlend = map["ShadowBlend"]?.toIntOrNull() ?: 0,
                worldStreamingRadius = map["WorldStreamingRadius"]?.toFloatOrNull() ?: 100f,
                terrainStreamingFactor = map["TerrainStreamingFactor"]?.toFloatOrNull() ?: 1f,
                treeLevelOfDetail = map["TreeLevelOfDetail"]?.toFloatOrNull() ?: 1f,
                treeImposterHighLodStreamingDistance = map["TreeImposterHighLodStreamingDistance"]?.toFloatOrNull() ?: 0f,
                grassStreamingDistance = map["GrassStreamingDistance"]?.toFloatOrNull() ?: 0f,
                bFocusPaused = map["bFocusPaused"]?.toBoolean() ?: true,
                bConstrainMouse = map["bConstrainMouse"]?.toBoolean() ?: false,
                hdr = map["HDR"]?.toBoolean() ?: false,
                peakBrightness = map["PeakBrightness"]?.toFloatOrNull() ?: 100f,
                paperWhite = map["PaperWhite"]?.toFloatOrNull() ?: 80f,
                aaAntiAliasing = map["aaAntiAliasing"]?.toIntOrNull() ?: 1,
                fsr3UpscalingQuality = map["FSR3UpscalingQuality"]?.toIntOrNull() ?: 0,
                fsr3AdditionalSharpness = map["FSR3AdditionalSharpness"]?.toFloatOrNull() ?: 0f,
                nvidiaReflex = map["NVIDIAReflex"]?.toIntOrNull() ?: 0,
                dlssUpscalingQuality = map["DLSSUpscalingQuality"]?.toIntOrNull() ?: 0,
                dlssFrameGeneration = map["DLSSFrameGeneration"]?.toIntOrNull() ?: 0,
                mobilePreset = map["MobilePreset"]?.toIntOrNull() ?: 0,
                screenPercentage = map["ScreenPercentage"]?.toFloatOrNull() ?: 1f
            )
        }
    }
}
