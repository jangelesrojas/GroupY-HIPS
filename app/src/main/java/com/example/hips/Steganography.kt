package com.example.hips

object Steganography {

    const val MAX_MESSAGE_BYTES = 100

    init {
        System.loadLibrary("hips")
    }

    external fun embedJpegMessage(
        inputPath: String,
        outputPath: String,
        message: String
    ): Boolean

    external fun getEmbedCapacityBytes(
        inputPath: String
    ): Int

    fun getUtf8Size(message: String): Int {
        return message.toByteArray(Charsets.UTF_8).size
    }

    fun isWithinProjectLimit(message: String): Boolean {
        return getUtf8Size(message) <= MAX_MESSAGE_BYTES
    }

    fun canEmbed(inputPath: String, message: String): Boolean {
        val messageBytes = getUtf8Size(message)
        val capacityBytes = getEmbedCapacityBytes(inputPath)
        return messageBytes <= MAX_MESSAGE_BYTES && messageBytes <= capacityBytes
    }
}


