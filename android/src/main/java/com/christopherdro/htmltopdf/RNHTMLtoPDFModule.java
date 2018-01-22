package com.christopherdro.htmltopdf;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.Charset;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.text.FontFactory;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import java.nio.charset.Charset;

import android.os.Environment;

public class RNHTMLtoPDFModule extends ReactContextBaseJavaModule {

  private Promise promise;
  private final ReactApplicationContext mReactContext;
  private Set<String> customFonts = new HashSet<>();

  XMLWorkerFontProvider fontProvider = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);

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

      if (options.hasKey("fonts")) {
        if (options.getArray("fonts") != null) {
          final ReadableArray fonts = options.getArray("fonts");
          for (int i = 0; i < fonts.size(); i++) {
            customFonts.add(fonts.getString(i));
          }
        }
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
        base64 = Base64.encodeFromFile(filePath);
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
      Document doc = new Document();
      InputStream in = new ByteArrayInputStream(htmlString.getBytes());

      PdfWriter pdf = PdfWriter.getInstance(doc, new FileOutputStream(file));

      FontFactory.setFontImp(fontProvider);
      for (String font : customFonts) {
        fontProvider.register(font);
      }

      doc.open();

      final TagProcessorFactory tagProcessorFactory = Tags.getHtmlTagProcessorFactory();
      tagProcessorFactory.removeProcessor(HTML.Tag.IMG);
      tagProcessorFactory.addProcessor(new ImageTagProcessor(), HTML.Tag.IMG);
      final CssFilesImpl cssFiles = new CssFilesImpl();
      cssFiles.add(XMLWorkerHelper.getInstance().getDefaultCSS());
      final StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver(cssFiles);
      final HtmlPipelineContext hpc = new HtmlPipelineContext(new CssAppliersImpl(fontProvider));
      hpc.setAcceptUnknown(true).autoBookmark(true).setTagFactory(tagProcessorFactory);
      final HtmlPipeline htmlPipeline = new HtmlPipeline(hpc, new PdfWriterPipeline(doc, pdf));
      final Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);
      final XMLWorker worker = new XMLWorker(pipeline, true);
      final Charset charset = Charset.forName("UTF-8");
      final XMLParser xmlParser = new XMLParser(true, worker, charset);
      xmlParser.parse(in, charset);

      doc.close();
      in.close();

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

}
