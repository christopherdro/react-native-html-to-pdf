package android.print

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Base64
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile

/**
 * Converts HTML to PDF.
 * 
 * Can convert only one task at a time, any requests to do more conversions before
 * ending the current task are ignored.
 */
class PdfConverter private constructor() : Runnable {

    companion object {
        private const val TAG = "HtmlToPdf"
        
        @Volatile
        private var sInstance: PdfConverter? = null
        
        @JvmStatic
        fun getInstance(): PdfConverter {
            return sInstance ?: synchronized(this) {
                sInstance ?: PdfConverter().also { sInstance = it }
            }
        }
    }

    private var mContext: Context? = null
    private var mHtmlString: String? = null
    private var mPdfFile: File? = null
    private var mPdfPrintAttrs: PrintAttributes? = null
    private var mIsCurrentlyConverting = false
    private var mWebView: WebView? = null
    private var mShouldEncode = false
    private var mResultMap: WritableMap? = null
    private var mPromise: Promise? = null
    private var mBaseURL: String? = null
    private var mTimeoutHandler: Handler? = null
    private var mTimeoutRunnable: Runnable? = null
    private val CONVERSION_TIMEOUT_MS = 30000L // 30 seconds timeout

    override fun run() {
        try {
            mContext?.let { context ->
                mWebView = WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            try {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                                    throw RuntimeException("call requires API level 19")
                                } else {
                                    val documentAdapter = createPrintDocumentAdapter()
                                    documentAdapter.onLayout(
                                        null, 
                                        getPdfPrintAttrs(), 
                                        null, 
                                        object : PrintDocumentAdapter.LayoutResultCallback() {
                                            override fun onLayoutFailed(error: CharSequence?) {
                                                Log.e(TAG, "PDF layout failed: $error")
                                                mPromise?.reject("PDF_LAYOUT_FAILED", error?.toString() ?: "Layout failed")
                                                destroy()
                                            }
                                        },
                                        null
                                    )
                                    
                                    documentAdapter.onWrite(
                                        arrayOf(PageRange.ALL_PAGES),
                                        getOutputFileDescriptor(),
                                        null,
                                        object : PrintDocumentAdapter.WriteResultCallback() {
                                            override fun onWriteFinished(pages: Array<PageRange>?) {
                                                try {
                                                    var base64 = ""
                                                    if (mShouldEncode) {
                                                        mPdfFile?.let { file ->
                                                            base64 = encodeFromFile(file)
                                                        }
                                                    }

                                                    mPdfFile?.let { file ->
                                                        val myDocument = PDDocument.load(file)
                                                        val pagesToBePrinted = myDocument.numberOfPages

                                                        mResultMap?.apply {
                                                            putString("filePath", file.absolutePath)
                                                            putString("numberOfPages", pagesToBePrinted.toString())
                                                            putString("base64", base64)
                                                        }
                                                        mPromise?.resolve(mResultMap)
                                                        myDocument.close()
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error finishing PDF write", e)
                                                    mPromise?.reject("PDF_WRITE_ERROR", e.message, e)
                                                } finally {
                                                    // Force reset the flag FIRST, before attempting cleanup
                                                    mIsCurrentlyConverting = false
                                                    destroy()
                                                }
                                            }

                                            override fun onWriteFailed(error: CharSequence?) {
                                                val errorResult = error?.toString() 
                                                    ?: "Please retry, Error occurred generating the pdf"
                                                Log.e(TAG, "PDF write failed: $errorResult")
                                                mPromise?.reject("PDF_WRITE_FAILED", errorResult)
                                                
                                                mIsCurrentlyConverting = false
                                                destroy()
                                            }

                                            override fun onWriteCancelled() {
                                                Log.d(TAG, "PDF write cancelled")
                                                mPromise?.reject("PDF_WRITE_CANCELLED", "PDF generation was cancelled")
                                                
                                                mIsCurrentlyConverting = false
                                                destroy()
                                            }
                                        }
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in onPageFinished", e)
                                mPromise?.reject("PDF_PAGE_LOAD_ERROR", "Error processing loaded page: ${e.message}", e)
                                
                                mIsCurrentlyConverting = false
                                destroy()
                            }
                        }

                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            Log.e(TAG, "WebView error: $description (code: $errorCode)")
                            mPromise?.reject("WEBVIEW_ERROR", "WebView error: $description")
                            
                            mIsCurrentlyConverting = false
                            destroy()
                        }
                    }

                    settings.apply {
                        textZoom = 100
                        defaultTextEncodingName = "utf-8"
                        allowFileAccess = true
                        javaScriptEnabled = true
                    }

                    mHtmlString?.let { html ->
                        loadDataWithBaseURL(mBaseURL, html, "text/HTML", "utf-8", null)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in run method", e)
            mPromise?.reject("PDF_WEBVIEW_SETUP_ERROR", "Failed to setup WebView for PDF conversion: ${e.message}", e)
            
            mIsCurrentlyConverting = false
            destroy()
        }
    }

    fun getPdfPrintAttrs(): PrintAttributes? {
        return mPdfPrintAttrs ?: getDefaultPrintAttrs()
    }

    fun setPdfPrintAttrs(printAttrs: PrintAttributes?) {
        mPdfPrintAttrs = printAttrs
    }
    
    /**
     * Force reset the converter state. Use this if the converter gets stuck.
     */
    fun forceReset() {
        Log.w(TAG, "Force resetting PdfConverter state")
        destroy()
    }
    
    /**
     * Get current conversion state for debugging
     */
    fun isCurrentlyConverting(): Boolean {
        return mIsCurrentlyConverting
    }

    @Throws(Exception::class)
    fun convert(
        context: Context?,
        htmlString: String?,
        file: File?,
        shouldEncode: Boolean,
        resultMap: WritableMap?,
        promise: Promise?,
        baseURL: String?
    ) {
        if (context == null) throw Exception("context can't be null")
        if (htmlString == null) throw Exception("htmlString can't be null")
        if (file == null) throw Exception("file can't be null")

        if (mIsCurrentlyConverting) {
            Log.w(TAG, "PDF conversion already in progress, ignoring new request")
            promise?.reject("CONVERSION_IN_PROGRESS", "Another PDF conversion is currently in progress")
            return
        }

        Log.d(TAG, "Starting PDF conversion for file: ${file.absolutePath}")

        try {
            mContext = context
            mHtmlString = htmlString
            mPdfFile = file
            mIsCurrentlyConverting = true
            mShouldEncode = shouldEncode
            mResultMap = resultMap
            mPromise = promise
            mBaseURL = baseURL
            
            // Set up timeout mechanism
            setupTimeout()
            
            runOnUiThread(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up PDF conversion", e)
            mIsCurrentlyConverting = false // Reset state on error
            cancelTimeout()
            promise?.reject("CONVERSION_SETUP_ERROR", "Failed to setup PDF conversion: ${e.message}", e)
        }
    }

    private fun getOutputFileDescriptor(): ParcelFileDescriptor? {
        return try {
            mPdfFile?.let { file ->
                file.createNewFile()
                ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e)
            null
        }
    }

    private fun getDefaultPrintAttrs(): PrintAttributes? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            null
        } else {
            PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(
                    PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600)
                )
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()
        }
    }

    private fun runOnUiThread(runnable: Runnable) {
        mContext?.let { context ->
            val handler = Handler(context.mainLooper)
            handler.post(runnable)
        }
    }

    private fun setupTimeout() {
        cancelTimeout() // Cancel any existing timeout
        
        mTimeoutHandler = Handler(Looper.getMainLooper())
        mTimeoutRunnable = Runnable {
            Log.w(TAG, "PDF conversion timed out after ${CONVERSION_TIMEOUT_MS}ms")
            mPromise?.reject("PDF_CONVERSION_TIMEOUT", "PDF conversion timed out")
            destroy()
        }
        
        mTimeoutHandler?.postDelayed(mTimeoutRunnable!!, CONVERSION_TIMEOUT_MS)
    }
    
    private fun cancelTimeout() {
        mTimeoutRunnable?.let { runnable ->
            mTimeoutHandler?.removeCallbacks(runnable)
        }
        mTimeoutHandler = null
        mTimeoutRunnable = null
    }

    private fun destroy() {
        try {
            // Cancel timeout first
            cancelTimeout()
            
            // Clean up WebView properly
            mWebView?.let { webView ->
                try {
                    webView.stopLoading()
                    webView.destroy()
                } catch (e: Exception) {
                    Log.w(TAG, "Error destroying WebView", e)
                }
            }
            
            // Reset all state variables
            mContext = null
            mHtmlString = null
            mPdfFile = null
            mPdfPrintAttrs = null
            mWebView = null
            mShouldEncode = false
            mResultMap = null
            mPromise = null
            mBaseURL = null
            mIsCurrentlyConverting = false
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in destroy() method", e)
            // Even if everything else fails, we MUST reset the converting flag
            mIsCurrentlyConverting = false
        }
    }

    @Throws(IOException::class)
    private fun encodeFromFile(file: File): String {
        return RandomAccessFile(file, "r").use { randomAccessFile ->
            val fileBytes = ByteArray(randomAccessFile.length().toInt())
            randomAccessFile.readFully(fileBytes)
            Base64.encodeToString(fileBytes, Base64.NO_WRAP)
        }
    }
}
