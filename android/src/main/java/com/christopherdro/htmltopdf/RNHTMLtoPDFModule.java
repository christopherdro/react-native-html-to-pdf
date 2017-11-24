package com.christopherdro.htmltopdf;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.UUID;

import android.util.Base64;
import java.io.IOException;
import java.io.RandomAccessFile;


import android.os.Environment;
import android.print.PdfConverter;

public class RNHTMLtoPDFModule extends ReactContextBaseJavaModule {

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
      String htmlString = options.hasKey("html") ? options.getString("html") : null;
      if (htmlString == null) return;

      String fileName;
      if (options.hasKey("fileName")) {
        fileName = options.getString("fileName");
      } else {
        fileName = UUID.randomUUID().toString();
      }

      if (options.hasKey("directory") && options.getString("directory").equals("docs")) {
        String state = Environment.getExternalStorageState();
          File path = (Environment.MEDIA_MOUNTED.equals(state)) ?
                  new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS)
                  : new File(mReactContext.getFilesDir(), Environment.DIRECTORY_DOCUMENTS);

        if (!path.exists()) path.mkdir();
        destinationFile = new File(path, fileName + ".pdf");
      } else {
        destinationFile = getTempFile(fileName);
      }

      String filePath = convertToPDF(htmlString, destinationFile);
      String base64 = "";

      if (options.hasKey("base64") && options.getBoolean("base64") == true) {
        base64 = encodeFromFile(destinationFile);
      }


      WritableMap resultMap = Arguments.createMap();
      resultMap.putString("filePath", filePath);
      resultMap.putString("base64", base64);

      promise.resolve(resultMap);
    } catch (Exception e) {
      promise.reject(e.getMessage());
    }
  }

  private String convertToPDF(String htmlString, File file) throws Exception {
    try {
      PdfConverter.getInstance()
              .convert(mReactContext, htmlString, file);
      String absolutePath = file.getAbsolutePath();

      return absolutePath;
    } catch (Exception e) {
      throw new Exception(e);
    }
  }

  private File getTempFile(String fileName) throws Exception {
    try {
      File outputDir = getReactApplicationContext().getCacheDir();
      File outputFile = File.createTempFile("PDF_" + UUID.randomUUID().toString(), ".pdf", outputDir);

      return outputFile;

    } catch (Exception e) {
      throw new Exception(e);
    }
  }

  private String encodeFromFile(File file) throws IOException{
    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
    byte[] fileBytes = new byte[(int)randomAccessFile.length()];
    randomAccessFile.readFully(fileBytes);
    return Base64.encodeToString(fileBytes, Base64.DEFAULT);
  }
}
