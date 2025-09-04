#import <HtmlToPdfSpec/HtmlToPdfSpec.h>
#import <React/RCTView.h>
#import <WebKit/WebKit.h>

@interface HtmlToPdf : RCTView <NativeHtmlToPdfSpec, WKNavigationDelegate>

@end
