package com.htmltopdf

import android.os.Environment
import android.print.PrintAttributes
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import java.io.File
import java.io.IOException
import java.util.UUID

@ReactModule(name = HtmlToPdfModule.NAME)
class HtmlToPdfModule(private val reactContext: ReactApplicationContext) :
    NativeHtmlToPdfSpec(reactContext) {

    companion object {
        const val NAME = "HtmlToPdf"
        private const val TAG = "HtmlToPdf"

        // Constants from original Java code
        private const val HTML = "html"
        private const val FILE_NAME = "fileName"
        private const val DIRECTORY = "directory"
        private const val BASE_64 = "base64"
        private const val BASE_URL = "baseURL"
        private const val HEIGHT = "height"
        private const val WIDTH = "width"
        private const val PDF_EXTENSION = ".pdf"
        private const val PDF_PREFIX = "PDF_"
    }

    override fun getName(): String = NAME

    override fun convert(options: ReadableMap, promise: Promise) {
        Log.d(TAG, "convert called with options: $options")

        try {
            // Check if we need to force reset the converter (as a safety measure)
            val pdfConverter = android.print.PdfConverter.getInstance()
            
            // If this is a retry after getting CONVERSION_IN_PROGRESS, force reset
            if (options.hasKey("forceReset") && options.getBoolean("forceReset")) {
                Log.d(TAG, "Force resetting PDF converter as requested")
                pdfConverter.forceReset()
            }
            // Get HTML content
            val htmlString = if (options.hasKey(HTML)) {
                options.getString(HTML)
            } else {
                null
            }

            if (htmlString.isNullOrEmpty()) {
                Log.e(TAG, "Invalid htmlString parameter")
                promise.reject("INVALID_HTML", "RNHTMLtoPDF error: Invalid htmlString parameter.")
                return
            }

            // Get fileName
            val fileName = if (options.hasKey(FILE_NAME)) {
                val name = options.getString(FILE_NAME)
                if (name != null && !isFileNameValid(name)) {
                    Log.e(TAG, "Invalid fileName parameter: $name")
                    promise.reject("INVALID_FILENAME", "RNHTMLtoPDF error: Invalid fileName parameter.")
                    return
                }
                name ?: (PDF_PREFIX + UUID.randomUUID().toString())
            } else {
                PDF_PREFIX + UUID.randomUUID().toString()
            }

            // Determine destination file
            val destinationFile = if (options.hasKey(DIRECTORY)) {
                val directoryName = options.getString(DIRECTORY)
                val state = Environment.getExternalStorageState()
                val path = if (Environment.MEDIA_MOUNTED == state) {
                    File(reactContext.getExternalFilesDir(null), directoryName)
                } else {
                    File(reactContext.filesDir, directoryName)
                }

                if (!path.exists() && !path.mkdirs()) {
                    Log.e(TAG, "Could not create folder structure: ${path.absolutePath}")
                    promise.reject("FOLDER_ERROR", "RNHTMLtoPDF error: Could not create folder structure.")
                    return
                }
                File(path, fileName + PDF_EXTENSION)
            } else {
                getTempFile(fileName)
            }

            // Handle custom page size
            val pageSize = if (options.hasKey(HEIGHT) && options.hasKey(WIDTH)) {
                val width = options.getInt(WIDTH)
                val height = options.getInt(HEIGHT)
                Log.d(TAG, "Custom page size: ${width}x${height}")

                PrintAttributes.Builder()
                    .setMediaSize(
                        PrintAttributes.MediaSize(
                            "custom", "CUSTOM",
                            (width * 1000 / 72.0).toInt(),
                            (height * 1000 / 72.0).toInt()
                        )
                    )
                    .setResolution(
                        PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600)
                    )
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            } else {
                null
            }

            // Get other options
            val shouldEncode = options.hasKey(BASE_64) && options.getBoolean(BASE_64)
            val baseURL = if (options.hasKey(BASE_URL)) options.getString(BASE_URL) else null

            Log.d(TAG, "Converting HTML to PDF:")
            Log.d(TAG, "- File: ${destinationFile.absolutePath}")
            Log.d(TAG, "- Base64: $shouldEncode")
            Log.d(TAG, "- BaseURL: $baseURL")

            // Convert to PDF
            convertToPDF(
                htmlString = htmlString,
                file = destinationFile,
                shouldEncode = shouldEncode,
                promise = promise,
                baseURL = baseURL,
                printAttributes = pageSize
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error in convert method", e)
            promise.reject("PDF_GENERATION_ERROR", e.message, e)
        }
    }

        private fun convertToPDF(
        htmlString: String,
        file: File,
        shouldEncode: Boolean,
        promise: Promise,
        baseURL: String?,
        printAttributes: PrintAttributes?
    ) {
        try {
            Log.d(TAG, "Starting PDF conversion using PdfConverter")
            
            // Create the result map that will be passed to PdfConverter
            val resultMap: WritableMap = Arguments.createMap()
            
            // Get PdfConverter instance and set custom print attributes if provided
            val pdfConverter = android.print.PdfConverter.getInstance()
            
            if (printAttributes != null) {
                pdfConverter.setPdfPrintAttrs(printAttributes)
            }
            
            // Convert HTML to PDF using the actual PdfConverter
            pdfConverter.convert(
                reactContext,
                htmlString,
                file,
                shouldEncode,
                resultMap,
                promise,
                baseURL
            )
            
            Log.d(TAG, "PDF conversion initiated successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during PDF conversion", e)
            promise.reject("PDF_CONVERSION_ERROR", "Failed to convert HTML to PDF: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    private fun getTempFile(fileName: String): File {
        val outputDir = reactApplicationContext.cacheDir
        return File.createTempFile(fileName, PDF_EXTENSION, outputDir)
    }

    @Throws(Exception::class)
    private fun isFileNameValid(fileName: String): Boolean {
        return File(fileName).canonicalFile.name == fileName
    }
}
