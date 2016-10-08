package com.parkerdan.htmltopdf;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import android.os.Environment;
import android.util.Log;

public class RNHTMLtoPDFModule extends ReactContextBaseJavaModule {

  private Promise promise;
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

      String htmlString = options.getString("html");

      String filePath = getFilePath(htmlString);

      promise.resolve(filePath);

    } catch (Exception e) {
      promise.reject(e.getMessage());
    }
  }

  private String getFilePath(String htmlString) throws Exception {

    File path = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);

    if ( !path.exists() ) {
      path.mkdir();
    }

    File file = new File(path, "MyPdf.pdf");

    try {
      String html = "<html><head></head><body>" + htmlString + "</body></html>";

      Document doc = new Document();

      InputStream in = new ByteArrayInputStream(html.getBytes());

      PdfWriter pdf = PdfWriter.getInstance(doc, new FileOutputStream(file));

      doc.open();

      XMLWorkerHelper.getInstance().parseXHtml(pdf, doc,in);

      doc.close();

      in.close();

      String absolutePath = file.getAbsolutePath();

      return absolutePath;

    } catch (Exception e) {
      throw new Exception();
    }
  }
}
