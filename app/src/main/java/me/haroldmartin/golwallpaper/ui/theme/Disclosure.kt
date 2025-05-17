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
import androidx.compose.ui.unit.dp

@Composable
fun Disclosure(isOpen: Boolean) = Image(
    modifier = Modifier
        .size(16.dp)
        .rotate(if (isOpen) 180f else 90f),
    imageVector = Triangle,
    contentDescription = "Triangle",
)

@Suppress("MagicNumber")
val Triangle: ImageVector = ImageVector.Builder(
    name = "Triangle",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
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
        arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, -3.46f, 0f)
        lineToRelative(-8f, 14f)
        arcTo(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 4f, 21f)
        horizontalLineToRelative(16f)
        arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.73f, -3f)
        close()
    }
}.build()
