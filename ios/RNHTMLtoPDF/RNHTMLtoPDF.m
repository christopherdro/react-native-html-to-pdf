
//  Created by Christopher on 9/3/15.

#import <CoreGraphics/CoreGraphics.h>
#import <UIKit/UIKit.h>
#import <React/RCTConvert.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTView.h>
#import <React/UIView+React.h>
#import <React/RCTUtils.h>
#import "RNHTMLtoPDF.h"

#define PDFSize CGSizeMake(612,792)

@implementation UIPrintPageRenderer (PDF)
- (NSData*) printToPDF:(NSInteger**)_numberOfPages
                   backgroundColor:(UIColor*)_bgColor
{
    NSMutableData *pdfData = [NSMutableData data];
    UIGraphicsBeginPDFContextToData( pdfData, self.paperRect, nil );

    [self prepareForDrawingPages: NSMakeRange(0, self.numberOfPages)];

    CGRect bounds = UIGraphicsGetPDFContextBounds();

    for ( int i = 0 ; i < self.numberOfPages ; i++ )
    {
        UIGraphicsBeginPDFPage();


        CGContextRef currentContext = UIGraphicsGetCurrentContext();
        CGContextSetFillColorWithColor(currentContext, _bgColor.CGColor);
        CGContextFillRect(currentContext, self.paperRect);

        [self drawPageAtIndex: i inRect: bounds];
    }

    *_numberOfPages = self.numberOfPages;

    UIGraphicsEndPDFContext();
    return pdfData;
}
@end

@implementation RNHTMLtoPDF {
    RCTEventDispatcher *_eventDispatcher;
    RCTPromiseResolveBlock _resolveBlock;
    RCTPromiseRejectBlock _rejectBlock;
    NSString *_html;
    NSString *_fileName;
    NSString *_filePath;
    UIColor *_bgColor;
    NSInteger *_numberOfPages;
    CGSize _PDFSize;
    WKWebView *_webView;
    float _paddingBottom;
    float _paddingTop;
    float _paddingLeft;
    float _paddingRight;
    BOOL _base64;
    BOOL autoHeight;
}

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

@synthesize bridge = _bridge;

- (instancetype)init
{
    if (self = [super init]) {
        _webView = [[WKWebView alloc] initWithFrame:self.bounds];
        _webView.navigationDelegate = self;
        [self addSubview:_webView];
        autoHeight = false;
    }
    return self;
}

RCT_EXPORT_METHOD(convert:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {

    if (options[@"html"]){
        _html = [RCTConvert NSString:options[@"html"]];
    }

    if (options[@"fileName"]){
        _fileName = [RCTConvert NSString:options[@"fileName"]];
    } else {
        _fileName = [[NSProcessInfo processInfo] globallyUniqueString];
    }

    // Default Color
    _bgColor = [UIColor colorWithRed: (246.0/255.0) green:(245.0/255.0) blue:(240.0/255.0) alpha:1];
    if (options[@"bgColor"]){
        NSString *hex = [RCTConvert NSString:options[@"bgColor"]];
        hex = [hex uppercaseString];
        NSString *cString = [hex stringByTrimmingCharactersInSet:
            [NSCharacterSet whitespaceAndNewlineCharacterSet]];

        if ((cString.length) == 7) {
            NSScanner *scanner = [NSScanner scannerWithString:cString];

            UInt32 rgbValue = 0;
            [scanner setScanLocation:1]; // Bypass '#' character
            [scanner scanHexInt:&rgbValue];

            _bgColor = [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
                                       green:((float)((rgbValue & 0x00FF00) >>  8))/255.0 \
                                        blue:((float)((rgbValue & 0x0000FF) >>  0))/255.0 \
                                       alpha:1.0];
        }
    }

    if (options[@"directory"] && [options[@"directory"] isEqualToString:@"Documents"]){
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsPath = [paths objectAtIndex:0];

        _filePath = [NSString stringWithFormat:@"%@/%@.pdf", documentsPath, _fileName];
    } else {
        _filePath = [NSString stringWithFormat:@"%@%@.pdf", NSTemporaryDirectory(), _fileName];
    }

    if (options[@"base64"] && [options[@"base64"] boolValue]) {
        _base64 = true;
    } else {
        _base64 = false;
    }

    if (options[@"height"] && options[@"width"]) {
        float width = [RCTConvert float:options[@"width"]];
        float height = [RCTConvert float:options[@"height"]];
        _PDFSize = CGSizeMake(width, height);
    } else {
        _PDFSize = PDFSize;
    }

    if (options[@"paddingBottom"]) {
        _paddingBottom = [RCTConvert float:options[@"paddingBottom"]];
    } else {
        _paddingBottom = 10.0f;
    }

    if (options[@"paddingLeft"]) {
        _paddingLeft = [RCTConvert float:options[@"paddingLeft"]];
    } else {
        _paddingLeft = 10.0f;
    }

    if (options[@"paddingTop"]) {
        _paddingTop = [RCTConvert float:options[@"paddingTop"]];
    } else {
        _paddingTop = 10.0f;
    }

    if (options[@"paddingRight"]) {
        _paddingRight = [RCTConvert float:options[@"paddingRight"]];
    } else {
        _paddingRight = 10.0f;
    }

    if (options[@"padding"]) {
        _paddingTop = [RCTConvert float:options[@"padding"]];
        _paddingBottom = [RCTConvert float:options[@"padding"]];
        _paddingLeft = [RCTConvert float:options[@"padding"]];
        _paddingRight = [RCTConvert float:options[@"padding"]];
    }

    NSString *path = [[NSBundle mainBundle] bundlePath];
    NSURL *baseURL = [NSURL fileURLWithPath:path];
    dispatch_async(dispatch_get_main_queue(), ^{
        [_webView loadHTMLString:_html baseURL:baseURL];
    });

    _resolveBlock = resolve;
    _rejectBlock = reject;

}
-(void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation {
    if (webView.isLoading)
    return;
    
    UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
    [render addPrintFormatter:webView.viewPrintFormatter startingAtPageAtIndex:0];
    
    // Define the printableRect and paperRect
    // If the printableRect defines the printable area of the page
    CGRect paperRect = CGRectMake(0, 0, _PDFSize.width, _PDFSize.height);
    CGRect printableRect = CGRectMake(_paddingLeft, _paddingTop, _PDFSize.width-(_paddingLeft + _paddingRight), _PDFSize.height-(_paddingBottom + _paddingTop));
    
    
    [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
    [render setValue:[NSValue valueWithCGRect:printableRect] forKey:@"printableRect"];
    
    NSData * pdfData = [render printToPDF:&_numberOfPages backgroundColor:_bgColor ];
    
    if (pdfData) {
        NSString *pdfBase64 = @"";
        
        [pdfData writeToFile:_filePath atomically:YES];
        if (_base64) {
            pdfBase64 = [pdfData base64EncodedStringWithOptions:0];
        }
        NSDictionary *data = [NSDictionary dictionaryWithObjectsAndKeys:
                              pdfBase64, @"base64",
                              [NSString stringWithFormat: @"%ld", (long)_numberOfPages], @"numberOfPages",
                              _filePath, @"filePath", nil];
        _resolveBlock(data);
    } else {
        NSError *error;
        _rejectBlock(RCTErrorUnspecified, nil, RCTErrorWithMessage(error.description));
    }
}

@end
