# react-native-html-to-pdf-rd

Convert html strings to PDF documents using React Native

## Installation

1. Run `npm install react-native-html-to-pdf-rd --save`

### Option 1: Automatic

2. Run `react-native link`


- Add the following `WRITE_EXTERNAL_STORAGE` permission to `AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
Also starting from Android M, users need to be prompted for permission dynamically. Follow [this](https://facebook.github.io/react-native/docs/permissionsandroid) link for more details on how to do that.


## Usage
```javascript

import React, { Component } from 'react';

import {
  Text,
  TouchableHighlight,
  View,
} from 'react-native';

import RNHTMLtoPDF from 'react-native-html-to-pdf-rd';

export default class Example extends Component {
  async createPDF() {
    let options = {
      html: '<h1>PDF TEST</h1>',
      fileName: 'test',
      directory: 'Documents',
    };

    let file = await RNHTMLtoPDF.convert(options)
    // console.log(file.filePath);
    alert(file.filePath);
  }

  render() {
    return(
      <View>
        <TouchableHighlight onPress={this.createPDF}>
          <Text>Create PDF</Text>
        </TouchableHighlight>
      </View>
    )
  }
}
```

## Options

| Param | Type | Default | Note |
|---|---|---|---|
| `html` | `string` |  | HTML string to be converted
| `fileName` | `string` | Random  | Custom Filename excluding .pdf extension
| `base64` | `boolean` | false  | return base64 string of pdf file (not recommended)
| `directory` | `string` |default cache directory| Directory where the file will be created (`Documents` folder in example above). Please note, on iOS `Documents` is the only custom value that is accepted.
| `height` | number | 792  | Set document height (points)
| `width` | number | 612  | Set document width (points)


#### iOS Only

| Param | Type | Default | Note |
|---|---|---|---|
| `paddingLeft` | number | 10  | Outer left padding (points)
| `paddingRight` | number | 10  | Outer right padding (points)
| `paddingTop` | number | 10  | Outer top padding (points)
| `paddingBottom` | number | 10  | Outer bottom padding (points)
| `padding` | number | 10 | Outer padding for any side (points), overrides any padding listed before
| `bgColor` | string | #F6F5F0 | Background color in Hexadecimal


#### Android Only

| Param | Type | Default | Note |
|---|---|---|---|
| `fonts` | Array | | Allow custom fonts `['/fonts/TimesNewRoman.ttf', '/fonts/Verdana.ttf']`
