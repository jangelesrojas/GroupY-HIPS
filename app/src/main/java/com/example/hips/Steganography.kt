package com.example.hips

object Steganography {
    init {
        System.loadLibrary("hips")
    }

    /**
     * Encodes a message into an array of ARGB_8888 pixels using LSB steganography.
     * @param pixels The original image pixels.
     * @param message The secret message to encode.
     * @return A new array of pixels containing the encoded message, or null if the message is too long.
     */
    external fun encodeMessage(pixels: IntArray, message: String): IntArray?

    /**
     * Decodes a message from an array of ARGB_8888 pixels using LSB steganography.
     * @param pixels The image pixels containing the encoded message.
     * @return The decoded secret message.
     */
    external fun decodeMessage(pixels: IntArray): String?
}
