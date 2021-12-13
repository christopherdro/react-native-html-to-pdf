package com.christopherdro.htmltopdf;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.os.Environment;
import android.print.PdfConverter;
import android.print.PrintAttributes;

public class RNHTMLtoPDFModule extends ReactContextBaseJavaModule {

    private static final String HTML = "html";
    private static final String FILE_NAME = "fileName";
    private static final String DIRECTORY = "directory";
    private static final String BASE_64 = "base64";
    private static final String BASE_URL = "baseURL";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";

    private static final String PDF_EXTENSION = ".pdf";
    private static final String PDF_PREFIX = "PDF_";

    private final ReactApplicationContext mReactContext;

  public RNHTMLtoPDFModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mReactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNHTMLtoPDF";
  }

  @ReactMethod
  public void convert(final ReadableMap options, final Promise promise) {
    try {
      File destinationFile;
      String htmlString = options.hasKey(HTML) ? options.getString(HTML) : null;
      if (htmlString == null) {
        promise.reject(new Exception("RNHTMLtoPDF error: Invalid htmlString parameter."));
        return;
      }

      String fileName;
      if (options.hasKey(FILE_NAME)) {
        fileName = options.getString(FILE_NAME);
        if (!isFileNameValid(fileName)) {
          promise.reject(new Exception("RNHTMLtoPDF error: Invalid fileName parameter."));
          return;
        }
      } else {
        fileName = PDF_PREFIX + UUID.randomUUID().toString();
      }

      if (options.hasKey(DIRECTORY)) {
        String state = Environment.getExternalStorageState();
        File path = (Environment.MEDIA_MOUNTED.equals(state)) ?
          new File(mReactContext.getExternalFilesDir(null), options.getString(DIRECTORY)) :
          new File(mReactContext.getFilesDir(), options.getString(DIRECTORY));

        if (!path.exists()) {
          if (!path.mkdirs()) {
            promise.reject(new Exception("RNHTMLtoPDF error: Could not create folder structure."));
            return;
          }
        }
        destinationFile = new File(path, fileName + PDF_EXTENSION);
      } else {
        destinationFile = getTempFile(fileName);
      }

      PrintAttributes pagesize=null;
      if(options.hasKey(HEIGHT) && options.hasKey(WIDTH)) {
        pagesize=new PrintAttributes.Builder()
                .setMediaSize(new PrintAttributes.MediaSize("custom","CUSTOM",
                        (int)(options.getInt(WIDTH)*1000/72.0),
                        (int)(options.getInt(HEIGHT)*1000/72.0))
                )
                .setResolution(new PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build();
      }

      convertToPDF(htmlString,
              destinationFile,
              options.hasKey(BASE_64) && options.getBoolean(BASE_64),
              Arguments.createMap(),
              promise,
              options.hasKey(BASE_URL) ? options.getString(BASE_URL) : null,
              pagesize
              );
    } catch (Exception e) {
      promise.reject(e);
    }
  }

  private void convertToPDF(String htmlString, File file, boolean shouldEncode, WritableMap resultMap, Promise promise,
      String baseURL,PrintAttributes printAttributes) throws Exception {
      PdfConverter pdfConverter=PdfConverter.getInstance();
      if(printAttributes!=null) pdfConverter.setPdfPrintAttrs(printAttributes);
      pdfConverter.convert(mReactContext, htmlString, file, shouldEncode, resultMap, promise, baseURL);
  }

  private File getTempFile(String fileName) throws IOException {
      File outputDir = getReactApplicationContext().getCacheDir();
      return File.createTempFile(fileName, PDF_EXTENSION, outputDir);

  }

  private boolean isFileNameValid(String fileName) throws Exception {
    return new File(fileName).getCanonicalFile().getName().equals(fileName);
  }
}

