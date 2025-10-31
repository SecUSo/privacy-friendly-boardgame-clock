package org.secuso.privacyfriendlyboardgameclock.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap) = ByteArrayOutputStream().apply {
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, this)
        toByteArray().toString()
    }
    @TypeConverter
    fun toBitmap(bytes: String) = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.length)
}