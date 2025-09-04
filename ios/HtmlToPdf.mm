#import "HtmlToPdf.h"
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <UIKit/UIKit.h>

#define PDFSize CGSizeMake(612, 792)

@implementation UIPrintPageRenderer (PDF)
- (NSData *)printToPDF:(NSInteger *)_numberOfPages
       backgroundColor:(UIColor *)_bgColor {
  NSMutableData *pdfData = [NSMutableData data];
  UIGraphicsBeginPDFContextToData(pdfData, self.paperRect, nil);

  [self prepareForDrawingPages:NSMakeRange(0, self.numberOfPages)];

  CGRect bounds = UIGraphicsGetPDFContextBounds();

  for (int i = 0; i < self.numberOfPages; i++) {
    UIGraphicsBeginPDFPage();

    CGContextRef currentContext = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(currentContext, _bgColor.CGColor);
    CGContextFillRect(currentContext, self.paperRect);

    [self drawPageAtIndex:i inRect:bounds];
  }

  *_numberOfPages = self.numberOfPages;

  UIGraphicsEndPDFContext();
  return pdfData;
}
@end

@implementation HtmlToPdf {
  RCTEventDispatcher *_eventDispatcher;
  RCTPromiseResolveBlock _resolveBlock;
  RCTPromiseRejectBlock _rejectBlock;
  NSString *_html;
  NSString *_fileName;
  NSString *_filePath;
  UIColor *_bgColor;
  NSInteger _numberOfPages;
  CGSize _PDFSize;
  WKWebView *_webView;
  float _paddingBottom;
  float _paddingTop;
  float _paddingLeft;
  float _paddingRight;
  BOOL _base64;
  BOOL autoHeight;
}

- (instancetype)init {
  if (self = [super init]) {
    _webView = [[WKWebView alloc] initWithFrame:self.bounds];
    _webView.navigationDelegate = self;
    [self addSubview:_webView];
    autoHeight = false;
  }
  return self;
}
RCT_EXPORT_MODULE()

- (void)add:(double)a
          b:(double)b
    resolve:(RCTPromiseResolveBlock)resolve
     reject:(RCTPromiseRejectBlock)reject {
  NSNumber *result = [[NSNumber alloc] initWithInteger:a + b];
  resolve(result);
}

- (void)convert:(JS::NativeHtmlToPdf::PDFOptions &)options
        resolve:(RCTPromiseResolveBlock)resolve
         reject:(RCTPromiseRejectBlock)reject {

  if (options.html()) {
    _html = options.html();
  }

  if (options.fileName()) {
    _fileName = options.fileName();
  } else {
    _fileName = [[NSProcessInfo processInfo] globallyUniqueString];
  }

  // Default Color
  _bgColor = [UIColor colorWithRed:(246.0 / 255.0)
                             green:(245.0 / 255.0)
                              blue:(240.0 / 255.0)
                             alpha:1];
  if (options.bgColor()) {
    NSString *hex = options.bgColor();
    hex = [hex uppercaseString];
    NSString *cString = [hex
        stringByTrimmingCharactersInSet:[NSCharacterSet
                                            whitespaceAndNewlineCharacterSet]];

    if ((cString.length) == 7) {
      NSScanner *scanner = [NSScanner scannerWithString:cString];

      UInt32 rgbValue = 0;
      [scanner setScanLocation:1]; // Bypass '#' character
      [scanner scanHexInt:&rgbValue];

      _bgColor =
          [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16)) / 255.0
                          green:((float)((rgbValue & 0x00FF00) >> 8)) / 255.0
                           blue:((float)((rgbValue & 0x0000FF) >> 0)) / 255.0
                          alpha:1.0];
    }
  }

  if (options.directory() &&
      [options.directory() isEqualToString:@"Documents"]) {
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                         NSUserDomainMask, YES);
    NSString *documentsPath = [paths objectAtIndex:0];

    _filePath =
        [NSString stringWithFormat:@"%@/%@.pdf", documentsPath, _fileName];
  } else {
    _filePath = [NSString
        stringWithFormat:@"%@%@.pdf", NSTemporaryDirectory(), _fileName];
  }

  if (options.base64().has_value() && options.base64().value()) {
    _base64 = true;
  } else {
    _base64 = false;
  }

  if (options.height() && options.height()) {
    float width = float(options.width().value());
    float height = float(options.height().value());
    _PDFSize = CGSizeMake(width, height);
  } else {
    _PDFSize = PDFSize;
  }

  if (options.paddingBottom()) {
    _paddingBottom = float(options.paddingBottom().value());
  } else {
    _paddingBottom = 10.0f;
  }

  if (options.paddingLeft()) {
    _paddingLeft = float(options.paddingLeft().value());
  } else {
    _paddingLeft = 10.0f;
  }

  if (options.paddingTop()) {
    _paddingTop = float(options.paddingTop().value());
  } else {
    _paddingTop = 10.0f;
  }

  if (options.paddingRight()) {
    _paddingRight = float(options.paddingRight().value());
  } else {
    _paddingRight = 10.0f;
  }

  if (options.padding()) {
    _paddingTop = float(options.padding().value());
    _paddingBottom = float(options.padding().value());
    _paddingLeft = float(options.padding().value());
    _paddingRight = float(options.padding().value());
  }

  if (@available(iOS 16.4, *)) {
    if (options.shouldPrintBackgrounds().has_value() &&
        options.shouldPrintBackgrounds().value()) {
      _webView.configuration.preferences.shouldPrintBackgrounds = true;
    }
  }

  NSString *path = [[NSBundle mainBundle] bundlePath];
  NSURL *baseURL = [NSURL fileURLWithPath:path];
  dispatch_async(dispatch_get_main_queue(), ^{
    [self->_webView loadHTMLString:self->_html baseURL:baseURL];
  });

  _resolveBlock = resolve;
  _rejectBlock = reject;
}
- (void)webView:(WKWebView *)webView
    didFinishNavigation:(WKNavigation *)navigation {
  if (webView.isLoading)
    return;

  UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
  [render addPrintFormatter:webView.viewPrintFormatter startingAtPageAtIndex:0];

  // Define the printableRect and paperRect
  // If the printableRect defines the printable area of the page
  CGRect paperRect = CGRectMake(0, 0, _PDFSize.width, _PDFSize.height);
  CGRect printableRect =
      CGRectMake(_paddingLeft, _paddingTop,
                 _PDFSize.width - (_paddingLeft + _paddingRight),
                 _PDFSize.height - (_paddingBottom + _paddingTop));

  [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
  [render setValue:[NSValue valueWithCGRect:printableRect]
            forKey:@"printableRect"];

  NSData *pdfData = [render printToPDF:&_numberOfPages
                       backgroundColor:_bgColor];

  if (pdfData) {
    NSString *pdfBase64 = @"";

    [pdfData writeToFile:_filePath atomically:YES];
    if (_base64) {
      pdfBase64 = [pdfData base64EncodedStringWithOptions:0];
    }
    NSDictionary *data = [NSDictionary
        dictionaryWithObjectsAndKeys:pdfBase64, @"base64",
                                     [NSString
                                         stringWithFormat:@"%ld",
                                                          (long)_numberOfPages],
                                     @"numberOfPages", _filePath, @"filePath",
                                     nil];

    _resolveBlock(data);
  } else {
    NSError *error;
    _rejectBlock(RCTErrorUnspecified, nil,
                 RCTErrorWithMessage(error.description));
  }
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeHtmlToPdfSpecJSI>(params);
}

@end
