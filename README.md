# react-native-html-to-pdf

Convert html strings to PDF documents using React Native

## Installation

1. Run `npm install react-native-html-to-pdf --save`

### Automatic

2. Run `react-native link`

### Manual

#### iOS

2. Open your project in XCode, right click on [Libraries](http://url.brentvatne.ca/jQp8) and select [Add Files to "Your Project Name](http://url.brentvatne.ca/1gqUD).
3. Add `libRNHTMLtoPDF.a` to `Build Phases -> Link Binary With Libraries`
   [(Screenshot)](http://url.brentvatne.ca/17Xfe).

#### Android
The android module pulls in iText to convert html to pdf.  You are supposed to obtain a license for commercial use of iText.

- Edit `android/settings.gradle` to included

```java
include ':react-native-html-to-pdf'
project(':react-native-html-to-pdf').projectDir = new File(rootProject.projectDir,'../node_modules/react-native-html-to-pdf/android')
```

- Edit `android/app/build.gradle` file to include

```java
dependencies {
  ....
  compile project(':react-native-html-to-pdf')

}
```

- Edit `MainApplication.java` to include

```java
// import the package
import com.christopherdro.htmltopdf.RNHTMLtoPDFPackage;

// include package
new MainReactPackage(),
new RNHTMLtoPDFPackage()
```

## Usage
```javascript

var React = require('react');
var ReactNative = require('react-native');

var {
  AlertIOS,
  AppRegistry,
  StyleSheet,
  Text,
  TouchableHighlight,
  View,
} = ReactNative;

import RNHTMLtoPDF from 'react-native-html-to-pdf';

var Example = React.createClass({

  createPDF() {
    var options = {
      html: '<h1>PDF TEST</h1>', // HTML String

      // ****************** OPTIONS BELOW WILL NOT WORK ON ANDROID **************                              
      fileName: 'test',          /* Optional: Custom Filename excluded extension
                                    Default: Randomly generated
                                  */


      directory: 'docs',         /* Optional: 'docs' will save the file in the `Documents`
                                    Default: Temp directory
                                  */

      base64: true               /* Optional: get the base64 PDF value
                                    Default: false
                                 */

      height: 800                /* Optional: 800 sets the height of the DOCUMENT that will be produced

                                    Default: 612
                                  */
      width: 1056,               /* Optional: 1056 sets the width of the DOCUMENT that will produced
                                    Default: 792
                                  */
      padding: 24,                /* Optional: 24 is the # of pixels between the outer paper edge and
                                            corresponding content edge.  Example: width of 1056 - 2*padding
                                            => content width of 1008
                                    Default: 10
                                  */
    };

    RNHTMLtoPDF.convert(options).then((data) => {
      console.log(data.filePath);
      console.log(data.base64);
    });
  },

  render() {
    <View>
      <TouchableHighlight onPress={this.createPDF}>
        <Text>Create PDF</Text>
      </TouchableHighlight>
    </View>
  }
});
```
