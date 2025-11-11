package org.secuso.privacyfriendlyboardgameclock.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray = ByteArrayOutputStream().let {
        bitmap?.compress(Bitmap.CompressFormat.PNG, 0, it)
        it.toByteArray()
    }
    @TypeConverter
    fun toBitmap(bytes: ByteArray) = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}