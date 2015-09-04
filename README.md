# react-native-html-to-pdf

Convert html strings to PDF documents using React Native

### Add it to your project

1. Run `npm install react-native-html-to-pdf --save`
2. Open your project in XCode, right click on [Libraries](http://url.brentvatne.ca/jQp8) and select [Add Files to "Your Project Name](http://url.brentvatne.ca/1gqUD).
3. Add `libRNHTMLtoPDF.a` to `Build Phases -> Link Binary With Libraries`
   [(Screenshot)](http://url.brentvatne.ca/17Xfe).

## Usage
```javascript

var React = require('react-native');

var {
  AlertIOS,
  AppRegistry,
  NativeModules: {
    RNHTMLtoPDF,
  }
  StyleSheet,
  Text,
  TouchableHighlight,
  View,
} = React;

var Example = React.createClass({

  createPDF() {
    var options = {
      html: '<h1>PDF TEST</h1>', // HTML String
      
      fileName: 'test'           // Optional: Custom Filename excluded extention
                                 // Default: Randomly generated

      directory: 'docs'          // Optional: 'docs' will save the file in the `Documents`
                                 // Default: Temp directory
    };
    
    HTMLtoPDF.convert(options).then((filePath) => {
      console.log(filePath);
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

## Example
The example project included demonstrates how you can create a PDF file from a html string and email it as an attachment using `react-native-mail`.
