# HIPS

## Description

HIPS is an Android-based JPEG steganography application designed to hide short text messages inside normal-looking JPEG images. The goal of the project is to explore covert communication, where the message is not only protected, but also hidden inside ordinary media so that the communication itself is less obvious.
This project was built as a mobile cybersecurity and steganography prototype. Instead of storing hidden data in raw pixels, Luminary works with JPEG DCT coefficients through native C++ code and libjpeg. This allows the app to interact with the internal structure of JPEG files more directly.
HIPS solves the problem of sending short messages in a way that does not immediately look like normal text communication. A user can select or capture a JPEG image, embed a short message, save the output image, and later extract the hidden message using the app.
During development, our group learned about Android image handling, camera and gallery workflows, JPEG compression, DCT coefficients, JNI/native C++ integration, and the tradeoff between message capacity, image quality, and robustness. One of the biggest lessons was that more file preserving transfer methods, such as Gmail attachment, work better than platforms that recompress images, such as SMS/MMS or some chat apps.

## Table of Contents

- [Description](#description)
- [Features](#features)
- [Technology Used](#technology-used)
- [Installation](#installation)
- [Usage](#usage)
- [Testing and Transfer Notes](#testing-and-transfer-notes)
- [Limitations](#limitations)
- [Future Development](#future-development)
- [Credits](#credits)
- [License](#license)

## Features

- Android mobile application built with Kotlin and Jetpack Compose
- Cover-style launch screen to make the app appear less obvious at first glance
- Hidden navigation flow leading to the real application features
- PIN or pattern-based unlock screen
- JPEG image selection from the device gallery
- Camera capture support
- Short message embedding into JPEG images
- Message extraction from previously embedded JPEG images
- Dynamic image capacity checking before embedding
- Native C++ JPEG processing using JNI
- DCT coefficient-based steganography
- Redundancy and majority voting for more reliable extraction
- Gallery saving for embedded output images
- Light and dark theme support

## Technology Used

- Kotlin
- Jetpack Compose
- Android Studio
- CameraX
- JNI
- C++
- libjpeg / JPEG coefficient processing
- Android MediaStore
- Gradle
- GitHub

## Installation

To run this project locally:

1. Clone the repository

2. Open the project in Android Studio.

3. Switch to the correct project branch if needed:

4. Allow Android Studio to sync Gradle.

5. Connect an Android device with USB debugging enabled, or start an Android emulator.

6. Build and run the app from Android Studio.

For a real device demo, a physical Android phone is recommended because the project uses camera, gallery, and file-saving behavior that is easier to demonstrate on actual hardware.

## Usage

1. Open the Luminary app (HIPS).

2. Navigate past the cover screen into the hidden application flow, by hitting the star in the top left 5 times.

3. Unlock the app using the configured PIN or pattern (Default PIN 1234, can be changed in settings).

4. To embed a message:

   - Open the Embed screen.
   - Select a JPEG image from the gallery or capture a new image.
   - Enter a short message.
   - Confirm that the message fits within the image capacity.
   - Tap the embed option.
   - The app saves a new JPEG image to the gallery.

5. To extract a message:

   - Open the Extract screen.
   - Select a JPEG image that was created by Luminary.
   - Tap the extract option.
   - If a valid hidden message is found, the app displays it.

## Testing and Transfer Notes

HIPS works best when the JPEG file is not heavily compressed or rebuilt during transfer. 

Recommended transfer methods:

- Gmail attachment
- Google Drive upload/download
- USB file transfer
- Nearby Share / Quick Share
- Cloud storage services that preserve the original file
-Whats App

Riskier transfer methods:

- SMS/MMS
- Discord image transfer
- Instagram or Facebook Messenger image sending
- Any platform that resizes, recompresses, or converts the image

These platforms may still show the same visible image, but they can alter the internal JPEG DCT coefficients that store the hidden message or entirely rebuild the image.

## Limitations

Current limitations include:

- Messages must be short due to capacity and robustness tradeoffs.
- The app depends on some JPEG file preservation for reliable extraction.
- Platforms that do heavy recompress/rebuild of images may destroy the hidden data.
- Steganography is not encryption.
- Advanced steganalysis tools may be able to detect hidden patterns.
- Robust transfer through SMS/MMS is not currently guaranteed.

## Future Development

Future improvements could include:

- Stronger error correction
- More robust coefficient bucket encoding or QIM-style embedding
- Better resistance to SMS/MMS and chat app recompression
- More detailed testing across different Android devices and messaging platforms
- Improved UI feedback during long image-processing tasks
- Support for additional media formats

## Credits

Project developed by Group Y:
Jose Angeles Rojas
Shabeg Minhas
Kane Philips
Jonah Razoky
Alexander Sekulski

This project uses Android development tools and JPEG processing concepts, including:

- Android Studio
- Kotlin
- Jetpack Compose
- CameraX
- JNI
- C++
- libjpeg

## License

This project was developed for academic purposes. No formal open-source license has been selected at this time.
