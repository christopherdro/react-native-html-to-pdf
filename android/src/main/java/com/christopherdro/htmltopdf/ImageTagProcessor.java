package com.christopherdro.htmltopdf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.log.Level;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.codec.Base64;
import com.itextpdf.tool.xml.NoCustomContextException;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.WorkerContext;
import com.itextpdf.tool.xml.exceptions.LocaleMessages;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;

public class ImageTagProcessor extends com.itextpdf.tool.xml.html.Image {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /*
   * (non-Javadoc)
   * 
   * @see com.itextpdf.tool.xml.TagProcessor#endElement(com.itextpdf.tool.xml.Tag, java.util.List, com.itextpdf.text.Document)
   */
  @Override
  public List<Element> end(final WorkerContext ctx, final Tag tag, final List<Element> currentContent) {
    final Map<String, String> attributes = tag.getAttributes();
    String src = attributes.get(HTML.Attribute.SRC);
    List<Element> elements = new ArrayList<Element>(1);
    if (null != src && src.length() > 0) {
      Image img = null;
      if (src.startsWith("data:image/")) {
        final String base64Data = src.substring(src.indexOf(",") + 1);
        try {
          img = Image.getInstance(Base64.decode(base64Data));
        } catch (Exception e) {
          if (logger.isLogging(Level.ERROR)) {
            logger.error(
                String.format(LocaleMessages.getInstance().getMessage(LocaleMessages.HTML_IMG_RETRIEVE_FAIL), src), e);
          }
        }
        if (img != null) {
          try {
            final HtmlPipelineContext htmlPipelineContext = getHtmlPipelineContext(ctx);
            elements.add(getCssAppliers().apply(
                new Chunk((com.itextpdf.text.Image) getCssAppliers().apply(img, tag, htmlPipelineContext), 0, 0, true),
                tag, htmlPipelineContext));
          } catch (NoCustomContextException e) {
            throw new RuntimeWorkerException(e);
          }
        }
      }

      if (img == null) {
        elements = super.end(ctx, tag, currentContent);
      }
    }
    return elements;
  }
}