package com.example.hips

object Steganography {
    init {
        System.loadLibrary("hips")
    }

    /**
     * Encodes a message into an array of ARGB_8888 pixels using steganography.
     * @param pixels The original image pixels.
     * @param message The secret message to encode.
     * @return A new array of pixels containing the encoded message, or null if the message is too long.
     */
    external fun encodeMessage(pixels: IntArray, message: String): IntArray?

    /**
     * Decodes a message from an array of ARGB_8888 pixels using steganography.
     * @param pixels The image pixels containing the encoded message.
     * @return The decoded secret message.
     */
    external fun decodeMessage(pixels: IntArray): String?

    /**
     * Encodes a message using DCT coefficients (pre-compression) via libjpeg-turbo.
     * @param jpegBytes The original JPEG image bytes.
     * @param message The secret message to encode.
     * @return A new byte array containing the encoded JPEG image, or null if it fails.
     */
    external fun encodeMessageDCT(jpegBytes: ByteArray, message: String): ByteArray?

    /**
     * Decodes a message from DCT coefficients using libjpeg-turbo.
     * @param jpegBytes The JPEG image bytes containing the encoded message.
     * @return The decoded secret message.
     */
    external fun decodeMessageDCT(jpegBytes: ByteArray): String?
}
