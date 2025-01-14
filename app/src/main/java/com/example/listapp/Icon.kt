//
// written by Robert Brandner
// Based on https://github.com/stewartlord/identicon.js
//

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat.TRANSPARENT
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import java.security.MessageDigest

class IdenticonDrawable(string : String) : Drawable() {

    private val hash = md5Hash(string)
    private val pixels = hash.take(15).toCharArray().map { it.digitToInt(16) % 2 }
    private val rect = Rect(0, 0, 1, 1)

    private val paint = Paint().apply {
        val hue = (hash.takeLast(7).toInt(16).toFloat() / 0xfffffff) * 360f
        val saturation = 0.7f
        val lightness = 0.5f
        this.color = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
    }

    override fun draw(canvas: Canvas) {
        // Get the drawable's bounds
        val width: Int = bounds.width()
        val height: Int = bounds.height()

        fun Canvas.drawPixel(x: Int, y: Int, pixel: Int) {
            if (pixel == 0) return
            withTranslation(x.toFloat(), y.toFloat()) {
                drawRect(rect, paint)
            }
        }

        canvas.withScale(width / 5f, height / 5f) {
            for (i in 0..4) {
                drawPixel(2, i, pixels[i])
                drawPixel(1, i, pixels[i+5])
                drawPixel(3, i, pixels[i+5])
                drawPixel(0, i, pixels[i+10])
                drawPixel(4, i, pixels[i+10])
            }
        }

    }

    override fun setAlpha(alpha: Int) {
        //
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //
    }

    @Composable
    fun toBitmap(size: Dp): ImageBitmap {

//        val bitmap = Bitmap.createBitmap()
//        val vector = ImageVector.vectorResource()
        val density = LocalDensity.current
        val pixelSize = density.run { size.roundToPx() }
        val bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
//
        bounds.set(0, 0, pixelSize, pixelSize)
        draw(canvas)
        return bitmap.asImageBitmap()
//
//        return ImageVector.bitmap(imageBitmap)
    }


    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = TRANSPARENT

    @OptIn(ExperimentalStdlibApi::class)
    private fun md5Hash(string: String): String =
        MessageDigest.getInstance("MD5")
            .digest(string.toByteArray()).toHexString(HexFormat.Default)
}

@Composable
fun Identicon(
    inputString: String,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val bitmap = remember(inputString, size) {
        val drawableSize = with(density) { size.roundToPx() }
        val bitmap = Bitmap.createBitmap(drawableSize, drawableSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        val drawable = IdenticonDrawable(inputString)
        drawable.setBounds(0, 0, drawableSize, drawableSize)
        drawable.draw(canvas)

        bitmap
    }
    if (inputString.isEmpty())

        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "User Icon",
            modifier = Modifier.size(size)
        )
    else
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Identicon",
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
}