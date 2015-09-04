
//  Created by Christopher on 9/3/15.

#import <UIKit/UIKit.h>
#import "RCTConvert.h"
#import "RCTEventDispatcher.h"
#import "RCTView.h"
#import "RNHTMLtoPDF.h"
#import "UIView+React.h"

#define PDFSize CGSizeMake(595.2,841.8)
#define kPaperSizeLetter CGSizeMake(612,792)

@implementation UIPrintPageRenderer (PDF)
- (NSData*) printToPDF
{
    NSMutableData *pdfData = [NSMutableData data];
    UIGraphicsBeginPDFContextToData( pdfData, self.paperRect, nil );
    
    [self prepareForDrawingPages: NSMakeRange(0, self.numberOfPages)];
    
    CGRect bounds = UIGraphicsGetPDFContextBounds();
    
    for ( int i = 0 ; i < self.numberOfPages ; i++ )
    {
        UIGraphicsBeginPDFPage();
        [self drawPageAtIndex: i inRect: bounds];
    }
    
    
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
    UIWebView *_webView;
    BOOL autoHeight;
}

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (instancetype)init
{
    if (self = [super init]) {
        _webView = [[UIWebView alloc] initWithFrame:self.bounds];
        _webView.delegate = self;
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
    
    if (options[@"directory"] && [options[@"directory"] isEqualToString:@"docs"]){
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
        NSString *documentsPath = [paths objectAtIndex:0];
        
        _filePath = [NSString stringWithFormat:@"%@/%@.pdf", documentsPath, _fileName];
        
    } else {
        _filePath = [NSString stringWithFormat:@"%@%@.pdf", NSTemporaryDirectory(), _fileName];
    }
    
    [_webView loadHTMLString:_html baseURL:nil];
    
    _resolveBlock = resolve;
    _rejectBlock = reject;
    
}

- (void)webViewDidFinishLoad:(UIWebView *)awebView
{
    if (awebView.isLoading)
        return;
    
    UIPrintPageRenderer *render = [[UIPrintPageRenderer alloc] init];
    [render addPrintFormatter:awebView.viewPrintFormatter startingAtPageAtIndex:0];
    
    // Padding is desirable, but optional
    float padding = 10.0f;
    
    // Define the printableRect and paperRect
    // If the printableRect defines the printable area of the page
    CGRect paperRect = CGRectMake(0, 0, PDFSize.width, PDFSize.height);
    CGRect printableRect = CGRectMake(padding, padding, PDFSize.width-(padding * 2), PDFSize.height-(padding * 2));
    
    [render setValue:[NSValue valueWithCGRect:paperRect] forKey:@"paperRect"];
    [render setValue:[NSValue valueWithCGRect:printableRect] forKey:@"printableRect"];
    
    NSData *pdfData = [render printToPDF];
    
    if (pdfData) {
        [pdfData writeToFile:_filePath atomically:YES];
        _resolveBlock(_filePath);
    } else {
        NSError *error;
        _rejectBlock(error);
    }
}

@end

