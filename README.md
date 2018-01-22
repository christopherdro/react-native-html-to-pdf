# react-native-html-to-pdf

Convert html strings to PDF documents using React Native

## Installation

1. Run `npm install react-native-html-to-pdf --save`

### Option 1: Automatic

2. Run `react-native link`

### Option 2: Manual

#### iOS

2. Open your project in XCode, right click on [Libraries](http://url.brentvatne.ca/jQp8) and select [Add Files to "Your Project Name](http://url.brentvatne.ca/1gqUD).
3. Add `libRNHTMLtoPDF.a` to `Build Phases -> Link Binary With Libraries`
   [(Screenshot)](http://url.brentvatne.ca/17Xfe).

#### Android
The android module pulls in iText to convert html to pdf. A license is required for commercial use.

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

import React, { Component } from 'react';

import {
  Text,
  TouchableHighlight,
  View,
} = from 'react-native';

import RNHTMLtoPDF from 'react-native-html-to-pdf';

class Example extends Component {
  async createPDF() {
    let options = {
      html: '<h1>PDF TEST</h1>',
      fileName: 'test',
      directory: 'docs',
    };

    let file = await RNHTMLtoPDF.convert(options)
    console.log(file.filePath);
  },

  render() {
    <View>
      <TouchableHighlight onPress={this.createPDF}>
        <Text>Create PDF</Text>
      </TouchableHighlight>
    </View>
  }
}
```

## Options

| Param | Type | Default | Note |
|---|---|---|---|
| `html` | `string` |  | HTML string to be converted
| `fileName` | `string` | Random  | Custom Filename excluding .pdf extension
| `base64` | boolean | false  | return base64 string of pdf file (not recommended)


#### iOS Only

| Param | Type | Default | Note |
|---|---|---|---|
| `height` | number | 792  | Set document height (points)
| `width` | number | 612  | Set document width (points)
| `padding` | number | 10  | Outer padding (points)


##### Android Only

| Param | Type | Default | Note |
|---|---|---|---|
| `fonts` | Array | | Allow custom fonts `['/fonts/TimesNewRoman.ttf', '/fonts/Verdana.ttf']`
