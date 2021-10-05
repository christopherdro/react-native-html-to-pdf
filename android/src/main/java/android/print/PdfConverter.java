/*
 * Created on 11/15/17.
 * Written by Islam Salah with assistance from members of Blink22.com
 */

package android.print;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.webkit.WebSettings;
import java.io.File;
import android.util.Base64;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import com.tom_roush.pdfbox.pdmodel.PDDocument;

/**
 * Converts HTML to PDF.
 * <p>
 * Can convert only one task at a time, any requests to do more conversions before
 * ending the current task are ignored.
 */
public class PdfConverter implements Runnable {

    private static final String TAG = "PdfConverter";
    private static PdfConverter sInstance;

    private Context mContext;
    private String mHtmlString;
    private File mPdfFile;
    private PrintAttributes mPdfPrintAttrs;
    private boolean mIsCurrentlyConverting;
    private WebView mWebView;
    private boolean mShouldEncode;
    private WritableMap mResultMap;
    private Promise mPromise;
    private String mBaseURL;

    private PdfConverter() {
    }

    public static synchronized PdfConverter getInstance() {
        if (sInstance == null)
            sInstance = new PdfConverter();

        return sInstance;
    }

    @Override
    public void run() {
        mWebView = new WebView(mContext);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                    throw new RuntimeException("call requires API level 19");
                else {
                    PrintDocumentAdapter documentAdapter = mWebView.createPrintDocumentAdapter();
                    documentAdapter.onLayout(null, getPdfPrintAttrs(), null, new PrintDocumentAdapter.LayoutResultCallback() {
                    }, null);
                    documentAdapter.onWrite(new PageRange[]{PageRange.ALL_PAGES}, getOutputFileDescriptor(), null, new PrintDocumentAdapter.WriteResultCallback() {
                        @Override
                        public void onWriteFinished(PageRange[] pages) {
                            try {
                                String base64 = "";
                                if (mShouldEncode) {
                                    base64 = encodeFromFile(mPdfFile);
                                }

                                PDDocument myDocument = PDDocument.load(mPdfFile);
                                int pagesToBePrinted = myDocument.getNumberOfPages();

                                mResultMap.putString("filePath", mPdfFile.getAbsolutePath());
                                mResultMap.putString("numberOfPages", String.valueOf(pagesToBePrinted));
                                mResultMap.putString("base64", base64);
                                mPromise.resolve(mResultMap);
                            } catch (IOException e) {
                                mPromise.reject(e.getMessage());
                            } finally {
                                destroy();
                            }
                        }

                        @Override
                        public void onWriteFailed(CharSequence error) {
                            String errorResult = "Please retry, Error occurred generating the pdf";
                            if (error != null) {
                                errorResult = error.toString();
                            }
                            mPromise.reject(errorResult);
                            destroy();
                        }

                        @Override
                        public void onWriteCancelled() {
                            destroy();
                        }

                    });
                }
            }
        });
        WebSettings settings = mWebView.getSettings();
        settings.setTextZoom(100);
        settings.setDefaultTextEncodingName("utf-8");
        mWebView.loadDataWithBaseURL(mBaseURL, mHtmlString, "text/HTML", "utf-8", null);
    }

    public PrintAttributes getPdfPrintAttrs() {
        return mPdfPrintAttrs != null ? mPdfPrintAttrs : getDefaultPrintAttrs();
    }

    public void setPdfPrintAttrs(PrintAttributes printAttrs) {
        this.mPdfPrintAttrs = printAttrs;
    }

    public void convert(Context context, String htmlString, File file, boolean shouldEncode, WritableMap resultMap,
            Promise promise, String baseURL) throws Exception {
        if (context == null)
            throw new Exception("context can't be null");
        if (htmlString == null)
            throw new Exception("htmlString can't be null");
        if (file == null)
            throw new Exception("file can't be null");

        if (mIsCurrentlyConverting)
            return;

        mContext = context;
        mHtmlString = htmlString;
        mPdfFile = file;
        mIsCurrentlyConverting = true;
        mShouldEncode = shouldEncode;
        mResultMap = resultMap;
        mPromise = promise;
        mBaseURL = baseURL;
        runOnUiThread(this);
    }

    private ParcelFileDescriptor getOutputFileDescriptor() {
        try {
            mPdfFile.createNewFile();
            return ParcelFileDescriptor.open(mPdfFile, ParcelFileDescriptor.MODE_TRUNCATE | ParcelFileDescriptor.MODE_READ_WRITE);
        } catch (Exception e) {
            Log.d(TAG, "Failed to open ParcelFileDescriptor", e);
        }
        return null;
    }

    private PrintAttributes getDefaultPrintAttrs() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return null;

        return new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setResolution(new PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();

    }

    private void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(mContext.getMainLooper());
        handler.post(runnable);
    }

    private void destroy() {
        mContext = null;
        mHtmlString = null;
        mPdfFile = null;
        mPdfPrintAttrs = null;
        mIsCurrentlyConverting = false;
        mWebView = null;
        mShouldEncode = false;
        mResultMap = null;
        mPromise = null;
    }

    private String encodeFromFile(File file) throws IOException{
      RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
      byte[] fileBytes = new byte[(int)randomAccessFile.length()];
      randomAccessFile.readFully(fileBytes);
      return Base64.encodeToString(fileBytes, Base64.DEFAULT);
    }
}
