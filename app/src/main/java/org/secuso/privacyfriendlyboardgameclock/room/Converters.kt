package org.secuso.privacyfriendlyboardgameclock.room

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {
    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?) = ByteArrayOutputStream().let {
        bitmap?.compress(Bitmap.CompressFormat.PNG, 0, it)
        it.toByteArray().toString()
    }
    @TypeConverter
    fun toBitmap(bytes: String) = BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.length)
}