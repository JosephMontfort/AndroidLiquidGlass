package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val BluetoothGlyphIcon: ImageVector
    get() {
        if (_bluetoothGlyphIcon != null) return _bluetoothGlyphIcon!!
        _bluetoothGlyphIcon = ImageVector.Builder(
            name = "BluetoothGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.5f, 6.5f)
                lineTo(17.5f, 17.5f)
                lineTo(12f, 22.5f)
                lineTo(12f, 1.5f)
                lineTo(17.5f, 6.5f)
                lineTo(6.5f, 17.5f)
            }
        }.build()
        return _bluetoothGlyphIcon!!
    }
private var _bluetoothGlyphIcon: ImageVector? = null

internal val CellularDataGlyphIcon: ImageVector
    get() {
        if (_cellularDataGlyphIcon != null) return _cellularDataGlyphIcon!!
        _cellularDataGlyphIcon = ImageVector.Builder(
            name = "CellularDataGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 22f)
                lineTo(22f, 22f)
                lineTo(22f, 2f)
                close()
            }
        }.build()
        return _cellularDataGlyphIcon!!
    }
private var _cellularDataGlyphIcon: ImageVector? = null

internal val HotspotGlyphIcon: ImageVector
    get() {
        if (_hotspotGlyphIcon != null) return _hotspotGlyphIcon!!
        _hotspotGlyphIcon = ImageVector.Builder(
            name = "HotspotGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 12f)
                lineTo(12.01f, 12f)
                moveTo(8.5f, 8.5f)
                curveTo(10.5f, 6.5f, 13.5f, 6.5f, 15.5f, 8.5f)
                moveTo(6f, 6f)
                curveTo(9.5f, 2.5f, 14.5f, 2.5f, 18f, 6f)
                moveTo(8.5f, 15.5f)
                curveTo(10.5f, 17.5f, 13.5f, 17.5f, 15.5f, 15.5f)
                moveTo(6f, 18f)
                curveTo(9.5f, 21.5f, 14.5f, 21.5f, 18f, 18f)
            }
        }.build()
        return _hotspotGlyphIcon!!
    }
private var _hotspotGlyphIcon: ImageVector? = null

internal val VpnGlyphIcon: ImageVector
    get() {
        if (_vpnGlyphIcon != null) return _vpnGlyphIcon!!
        _vpnGlyphIcon = ImageVector.Builder(
            name = "VpnGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12.65f, 10f)
                curveTo(11.83f, 7.67f, 9.61f, 6f, 7f, 6f)
                curveTo(3.69f, 6f, 1f, 8.69f, 1f, 12f)
                curveTo(1f, 15.31f, 3.69f, 18f, 7f, 18f)
                curveTo(9.61f, 18f, 11.83f, 16.33f, 12.65f, 14f)
                lineTo(17f, 14f)
                lineTo(17f, 18f)
                lineTo(21f, 18f)
                lineTo(21f, 14f)
                lineTo(23f, 14f)
                lineTo(23f, 10f)
                lineTo(12.65f, 10f)
                close()
                moveTo(7f, 14f)
                curveTo(5.9f, 14f, 5f, 13.1f, 5f, 12f)
                curveTo(5f, 10.9f, 5.9f, 10f, 7f, 10f)
                curveTo(8.1f, 10f, 9f, 10.9f, 9f, 12f)
                curveTo(9f, 13.1f, 8.1f, 14f, 7f, 14f)
                close()
            }
        }.build()
        return _vpnGlyphIcon!!
    }
private var _vpnGlyphIcon: ImageVector? = null

internal val HeadphonesGlyphIcon: ImageVector
    get() {
        if (_headphonesGlyphIcon != null) return _headphonesGlyphIcon!!
        _headphonesGlyphIcon = ImageVector.Builder(
            name = "HeadphonesGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 3f)
                curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
                lineTo(3f, 19f)
                curveTo(3f, 20.66f, 4.34f, 22f, 6f, 22f)
                lineTo(8f, 22f)
                curveTo(9.1f, 22f, 10f, 21.1f, 10f, 20f)
                lineTo(10f, 15f)
                curveTo(10f, 13.9f, 9.1f, 13f, 8f, 13f)
                lineTo(5f, 13f)
                lineTo(5f, 12f)
                curveTo(5f, 8.13f, 8.13f, 5f, 12f, 5f)
                curveTo(15.87f, 5f, 19f, 8.13f, 19f, 12f)
                lineTo(19f, 13f)
                lineTo(16f, 13f)
                curveTo(14.9f, 13f, 14f, 13.9f, 14f, 15f)
                lineTo(14f, 20f)
                curveTo(14f, 21.1f, 14.9f, 22f, 16f, 22f)
                lineTo(18f, 22f)
                curveTo(19.66f, 22f, 21f, 20.66f, 21f, 19f)
                lineTo(21f, 12f)
                curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
                close()
            }
        }.build()
        return _headphonesGlyphIcon!!
    }
private var _headphonesGlyphIcon: ImageVector? = null

internal val WatchGlyphIcon: ImageVector
    get() {
        if (_watchGlyphIcon != null) return _watchGlyphIcon!!
        _watchGlyphIcon = ImageVector.Builder(
            name = "WatchGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(20f, 12f)
                curveTo(20f, 8.76f, 17.91f, 6.02f, 15f, 5.23f)
                lineTo(15f, 2f)
                lineTo(9f, 2f)
                lineTo(9f, 5.23f)
                curveTo(6.09f, 6.02f, 4f, 8.76f, 4f, 12f)
                curveTo(4f, 15.24f, 6.09f, 17.98f, 9f, 18.77f)
                lineTo(9f, 22f)
                lineTo(15f, 22f)
                lineTo(15f, 18.77f)
                curveTo(17.91f, 17.98f, 20f, 15.24f, 20f, 12f)
                close()
                moveTo(6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f)
                curveTo(18f, 15.31f, 15.31f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                close()
            }
        }.build()
        return _watchGlyphIcon!!
    }
private var _watchGlyphIcon: ImageVector? = null

internal val CarGlyphIcon: ImageVector
    get() {
        if (_carGlyphIcon != null) return _carGlyphIcon!!
        _carGlyphIcon = ImageVector.Builder(
            name = "CarGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(18.92f, 6.01f)
                curveTo(18.72f, 5.42f, 18.16f, 5f, 17.5f, 5f)
                lineTo(6.5f, 5f)
                curveTo(5.84f, 5f, 5.28f, 5.42f, 5.08f, 6.01f)
                lineTo(3f, 12f)
                lineTo(3f, 20f)
                curveTo(3f, 20.55f, 3.45f, 21f, 4f, 21f)
                lineTo(5f, 21f)
                curveTo(5.55f, 21f, 6f, 20.55f, 6f, 20f)
                lineTo(6f, 19f)
                lineTo(18f, 19f)
                lineTo(18f, 20f)
                curveTo(18f, 20.55f, 18.45f, 21f, 19f, 21f)
                lineTo(20f, 21f)
                curveTo(20.55f, 21f, 21f, 20.55f, 21f, 20f)
                lineTo(21f, 12f)
                lineTo(18.92f, 6.01f)
                close()
                moveTo(6.85f, 7f)
                lineTo(17.14f, 7f)
                lineTo(18.22f, 10f)
                lineTo(5.78f, 10f)
                lineTo(6.85f, 7f)
                close()
                moveTo(7.5f, 16f)
                curveTo(6.67f, 16f, 6f, 15.33f, 6f, 14.5f)
                curveTo(6f, 13.67f, 6.67f, 13f, 7.5f, 13f)
                curveTo(8.33f, 13f, 9f, 13.67f, 9f, 14.5f)
                curveTo(9f, 15.33f, 8.33f, 16f, 7.5f, 16f)
                close()
                moveTo(16.5f, 16f)
                curveTo(15.67f, 16f, 15f, 15.33f, 15f, 14.5f)
                curveTo(15f, 13.67f, 15.67f, 13f, 16.5f, 13f)
                curveTo(17.33f, 13f, 18f, 13.67f, 18f, 14.5f)
                curveTo(18f, 15.33f, 17.33f, 16f, 16.5f, 16f)
                close()
            }
        }.build()
        return _carGlyphIcon!!
    }
private var _carGlyphIcon: ImageVector? = null

internal val CheckGlyphIcon: ImageVector
    get() {
        if (_checkGlyphIcon != null) return _checkGlyphIcon!!
        _checkGlyphIcon = ImageVector.Builder(
            name = "CheckGlyph",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF007AFF)),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 12.5f)
                lineTo(9.5f, 18f)
                lineTo(20f, 6.5f)
            }
        }.build()
        return _checkGlyphIcon!!
    }
private var _checkGlyphIcon: ImageVector? = null
