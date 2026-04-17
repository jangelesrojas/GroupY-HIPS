package com.example.hips

object Steganography {

    const val MAX_MESSAGE_BYTES = 100

    init {
        System.loadLibrary("hips")
    }

    fun getUtf8Size(message: String): Int {
        return message.toByteArray(Charsets.UTF_8).size
    }

    external fun embedJpegMessage(
        inputPath: String,
        outputPath: String,
        message: String
    ): Boolean

    external fun getEmbedCapacityBytes(
        inputPath: String
    ): Int

    external fun extractJpegMessage(
        inputPath: String
    ): String?
}



