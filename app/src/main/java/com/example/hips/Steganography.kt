package com.example.hips

object Steganography {
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
}


