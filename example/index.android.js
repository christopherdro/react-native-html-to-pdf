/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View
} from 'react-native';

import RNHTMLtoPDF from 'react-native-html-to-pdf';

export default class HTMLToPDF extends Component {

  async componentDidMount() {
    let options = {
      html: '<h1>Heading 1</h1><h2>Heading 2</h2><h3>Heading 3</h3>',
      fileName: 'test',
      base64: true
    };

    try {
      const results = await RNHTMLtoPDF.convert(options)
      console.log(results)
    } catch (err) {
      console.error(err)
    }
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome to React Native!
        </Text>
        <Text style={styles.instructions}>
          To get started, edit index.ios.js
        </Text>
        <Text style={styles.instructions}>
          Press Cmd+R to reload,{'\n'}
          Cmd+D or shake for dev menu
        </Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

AppRegistry.registerComponent('HTMLToPDF', () => HTMLToPDF);
