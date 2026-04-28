package com.example.hips

// This object is the Kotlin bridge to the native C++ steganography library.


// Single access point for the native steganography functions.
object Steganography {

    // Hard cap for messages so the app does not try to embed too much data.
    const val MAX_MESSAGE_BYTES = 100

    // Loads the native C++ library named hips.
    init {
        System.loadLibrary("hips")
    }

    // Returns the real byte size of the message, which matters more than character count.
    fun getUtf8Size(message: String): Int {
        return message.toByteArray(Charsets.UTF_8).size
    }

    // Native function that embeds a message into a JPEG.
    external fun embedJpegMessage(
        inputPath: String,
        outputPath: String,
        message: String
    ): Boolean

    // Native function that estimates how many bytes this JPEG can hold.
    external fun getEmbedCapacityBytes(
        inputPath: String
    ): Int

    // Native function that reads a hidden message from a JPEG, if one exists.
    external fun extractJpegMessage(
        inputPath: String
    ): String?
}




