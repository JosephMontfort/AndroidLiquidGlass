package com.kyant.backdrop.catalog

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

internal val ShareFilledIcon: ImageVector
    get() {
        if (_shareFilledIcon != null) return _shareFilledIcon!!
        _shareFilledIcon = ImageVector.Builder(
            name = "ShareFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 16.08f)
                curveTo(17.24f, 16.08f, 16.55f, 16.38f, 16.03f, 16.85f)
                lineTo(8.91f, 12.7f)
                curveTo(8.96f, 12.47f, 9f, 12.24f, 9f, 12f)
                curveTo(9f, 11.76f, 8.96f, 11.53f, 8.91f, 11.3f)
                lineTo(15.96f, 7.19f)
                curveTo(16.5f, 7.69f, 17.21f, 8f, 18f, 8f)
                curveTo(19.66f, 8f, 21f, 6.66f, 21f, 5f)
                curveTo(21f, 3.34f, 19.66f, 2f, 18f, 2f)
                curveTo(16.34f, 2f, 15f, 3.34f, 15f, 5f)
                curveTo(15f, 5.24f, 15.04f, 5.47f, 15.09f, 5.7f)
                lineTo(8.04f, 9.81f)
                curveTo(7.5f, 9.31f, 6.79f, 9f, 6f, 9f)
                curveTo(4.34f, 9f, 3f, 10.34f, 3f, 12f)
                curveTo(3f, 13.66f, 4.34f, 15f, 6f, 15f)
                curveTo(6.79f, 15f, 7.5f, 14.69f, 8.04f, 14.19f)
                lineTo(15.16f, 18.34f)
                curveTo(15.11f, 18.55f, 15.08f, 18.77f, 15.08f, 19f)
                curveTo(15.08f, 20.66f, 16.42f, 22f, 18.08f, 22f)
                curveTo(19.74f, 22f, 21.08f, 20.66f, 21.08f, 19f)
                curveTo(21.08f, 17.34f, 19.74f, 16f, 18.08f, 16f)
                close()
            }
        }.build()
        return _shareFilledIcon!!
    }

private var _shareFilledIcon: ImageVector? = null

internal val SendIcon: ImageVector
    get() {
        if (_sendIcon != null) return _sendIcon!!
        _sendIcon = ImageVector.Builder(
            name = "Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.01f, 21f)
                lineTo(23f, 12f)
                lineTo(2.01f, 3f)
                lineTo(2f, 10f)
                lineTo(17f, 12f)
                lineTo(2f, 14f)
                close()
            }
        }.build()
        return _sendIcon!!
    }

private var _sendIcon: ImageVector? = null

internal val SwapIcon: ImageVector
    get() {
        if (_swapIcon != null) return _swapIcon!!
        _swapIcon = ImageVector.Builder(
            name = "Swap",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.99f, 11f)
                lineTo(3f, 7f)
                lineTo(7f, 3f)
                lineTo(8.41f, 4.41f)
                lineTo(6.83f, 6f)
                horizontalLineTo(16f)
                verticalLineTo(8f)
                horizontalLineTo(6.83f)
                lineTo(8.41f, 9.59f)
                close()

                moveTo(17.01f, 13f)
                lineTo(21f, 17f)
                lineTo(17f, 21f)
                lineTo(15.59f, 19.59f)
                lineTo(17.17f, 18f)
                horizontalLineTo(8f)
                verticalLineTo(16f)
                horizontalLineTo(17.17f)
                lineTo(15.59f, 14.41f)
                close()
            }
        }.build()
        return _swapIcon!!
    }

private var _swapIcon: ImageVector? = null

internal val ReceiveIcon: ImageVector
    get() {
        if (_receiveIcon != null) return _receiveIcon!!
        _receiveIcon = ImageVector.Builder(
            name = "Receive",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 20f)
                horizontalLineTo(19f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                close()

                moveTo(11f, 4f)
                verticalLineTo(14.17f)
                lineTo(8.41f, 11.59f)
                lineTo(7f, 13f)
                lineTo(12f, 18f)
                lineTo(17f, 13f)
                lineTo(15.59f, 11.59f)
                lineTo(13f, 14.17f)
                verticalLineTo(4f)
                close()
            }
        }.build()
        return _receiveIcon!!
    }

private var _receiveIcon: ImageVector? = null
