/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');

var {
  AlertIOS,
  AppRegistry,
  NativeModules: {
    RNHTMLtoPDF,
    RNMail
  }
  StyleSheet,
  Text,
  View,
} = React;

var HTMLToPDFExample = React.createClass({
  
  componentDidMount() {
    var options = {
      html: '<h1>PDF TEST</h1>',
      fileName: 'test'
    };
    
  RNHTMLtoPDF.convert(options).then((result) => {
    RNMail.mail({
        subject: '',
        recipients: [''],
        body: '',
        attachmentPath: result,
        attachmentType: 'pdf',
      }, (error, event) => {
        if(error) {
          AlertIOS.alert('Error', 'Could not send mail. Please send a mail to support@example.com');
        }
      });
    });
  },

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
});

var styles = StyleSheet.create({
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

AppRegistry.registerComponent('HTMLToPDFExample', () => HTMLToPDFExample);
