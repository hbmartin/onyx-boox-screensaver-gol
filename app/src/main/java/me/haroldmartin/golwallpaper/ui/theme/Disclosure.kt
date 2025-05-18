package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp

@Composable
fun Disclosure(isOpen: Boolean, size: Dp = LARGE) = Image(
    modifier = Modifier
        .size(size)
        .rotate(if (isOpen) 180f else 90f),
    imageVector = TRIANGLE,
    contentDescription = "Triangle",
)

@Suppress("MagicNumber")
val TRIANGLE: ImageVector = ImageVector.Builder(
    name = "Triangle",
    defaultWidth = XLARGE,
    defaultHeight = XLARGE,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = 1.0f,
        stroke = SolidColor(Color(0xFF000000)),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
    ) {
        moveTo(13.73f, 4f)
        arcToRelative(
            a = 2f,
            b = 2f,
            theta = 0f,
            isMoreThanHalf = false,
            isPositiveArc = false,
            dx1 = -3.46f,
            dy1 = 0f,
        )
        lineToRelative(-8f, 14f)
        arcTo(
            horizontalEllipseRadius = 2f,
            verticalEllipseRadius = 2f,
            theta = 0f,
            isMoreThanHalf = false,
            isPositiveArc = false,
            x1 = 4f,
            y1 = 21f,
        )
        horizontalLineToRelative(16f)
        arcToRelative(
            a = 2f,
            b = 2f,
            theta = 0f,
            isMoreThanHalf = false,
            isPositiveArc = false,
            dx1 = 1.73f,
            dy1 = -3f,
        )
        close()
    }
}.build()
