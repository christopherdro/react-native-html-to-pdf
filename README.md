# react-native-html-to-pdf

Convert html strings to PDF documents using React Native

## Installation

```sh
npm install react-native-html-to-pdf
```

## Usage

## Usage

```javascript
import React, { Component } from 'react';

import { Text, TouchableHighlight, View } from 'react-native';

import { generatePDF } from 'react-native-html-to-pdf';

export default class Example extends Component {
  async createPDF() {
    let options = {
      html: '<h1>PDF TEST</h1>',
      fileName: 'test',
      base64: true,
    };

    let results = await generatePDF(options);
    console.log(results);
  }

  render() {
    return (
      <View>
        <TouchableHighlight onPress={this.createPDF}>
          <Text>Create PDF</Text>
        </TouchableHighlight>
      </View>
    );
  }
}
```

## Options

| Param       | Type      | Default                 | Note                                                                                                                                                       |
| ----------- | --------- | ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `html`      | `string`  |                         | HTML string to be converted                                                                                                                                |
| `fileName`  | `string`  | Random                  | Custom Filename excluding .pdf extension                                                                                                                   |
| `base64`    | `boolean` | false                   | return base64 string of pdf file (not recommended)                                                                                                         |
| `directory` | `string`  | default cache directory | Directory where the file will be created (`Documents` folder in example above). Please note, on iOS `Documents` is the only custom value that is accepted. |
| `height`    | number    | 792                     | Set document height (points)                                                                                                                               |
| `width`     | number    | 612                     | Set document width (points)                                                                                                                                |

#### iOS Only

| Param           | Type   | Default | Note                                                                     |
| --------------- | ------ | ------- | ------------------------------------------------------------------------ |
| `paddingLeft`   | number | 10      | Outer left padding (points)                                              |
| `paddingRight`  | number | 10      | Outer right padding (points)                                             |
| `paddingTop`    | number | 10      | Outer top padding (points)                                               |
| `paddingBottom` | number | 10      | Outer bottom padding (points)                                            |
| `padding`       | number | 10      | Outer padding for any side (points), overrides any padding listed before |
| `bgColor`       | string | #F6F5F0 | Background color in Hexadecimal                                          |

#### Android Only

| Param   | Type  | Default | Note                                                                    |
| ------- | ----- | ------- | ----------------------------------------------------------------------- |
| `fonts` | Array |         | Allow custom fonts `['/fonts/TimesNewRoman.ttf', '/fonts/Verdana.ttf']` |

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
