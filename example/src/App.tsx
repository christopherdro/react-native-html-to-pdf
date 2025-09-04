import React, { useState, useCallback } from 'react';
import {
  ScrollView,
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
} from 'react-native';

import {
  generatePDF,
  type PDFOptions,
  type PDFResult,
} from 'react-native-html-to-pdf';

function App(): React.JSX.Element {
  const [isGenerating, setIsGenerating] = useState(false);
  const [lastResult, setLastResult] = useState<PDFResult | null>(null);
  const htmlContent = `
<html>
<head>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; }
    h1 { color: #333; text-align: center; }
    h2 { color: #666; border-bottom: 2px solid #eee; padding-bottom: 5px; }
    .highlight { background-color: #ffeb3b; padding: 2px 4px; }
    .info-box { background-color: #f0f8ff; border: 1px solid #0066cc; padding: 15px; border-radius: 5px; margin: 10px 0; }
    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
    th { background-color: #f2f2f2; }
  </style>
</head>
<body>
  <h1>React Native HTML to PDF Demo</h1>
  
  <div class="info-box">
    <h2>📱 Modern React Native Module</h2>
    <p>This PDF was generated using the <span class="highlight">modernized react-native-html-to-pdf</span> module, updated from 2016 to 2024 standards!</p>
  </div>

  <h2>🚀 New Features</h2>
  <ul>
    <li>✅ React Native 0.81+ Support</li>
    <li>✅ New Architecture (Turbo Modules) Ready</li>
    <li>✅ TypeScript Definitions</li>
    <li>✅ Modern Error Handling</li>
    <li>✅ Updated Dependencies</li>
  </ul>

  <h2>📊 Technical Details</h2>
  <table>
    <tr><th>Property</th><th>Value</th></tr>
    <tr><td>Generation Date</td><td>${new Date().toLocaleString()}</td></tr>
    <tr><td>React Native Version</td><td>0.81.1</td></tr>
    <tr><td>Module Version</td><td>0.13.0</td></tr>
    <tr><td>Architecture</td><td>Bridge + Turbo Module Support</td></tr>
  </table>

  <h2>🎨 Styling Support</h2>
  <p>This module supports full HTML and CSS styling, including:</p>
  <ul>
    <li><strong>Bold text</strong> and <em>italic text</em></li>
    <li><span style="color: red;">Colored text</span></li>
    <li><span style="font-size: 20px;">Different font sizes</span></li>
    <li>Background colors and borders</li>
    <li>Tables and lists</li>
  </ul>

  <div style="text-align: center; margin-top: 30px; padding: 20px; background-color: #e8f5e8; border: 2px solid #4caf50;">
    <h3>🎉 Successfully Created PDF!</h3>
  </div>
</body>
</html>
  `;

  const submitPDF = useCallback(async () => {
    console.log('[App] generatePDF called');
    if (!htmlContent.trim()) {
      Alert.alert('Error', 'Please enter some HTML content');
      return;
    }

    setIsGenerating(true);

    const options: PDFOptions = {
      html: htmlContent,
      fileName: 'HTMLToPDF-Demo',
      base64: true,
      width: 612,
      height: 792,
      padding: 24,
      bgColor: '#FFFFFF',
    };

    try {
      const result = await generatePDF(options);
      console.log('PDF Generated:', result);
      setLastResult(result);

      Alert.alert(
        '✅ PDF Generated Successfully!',
        `File: ${result.filePath}\nPages: ${
          result.numberOfPages
        }\nBase64 length: ${result.base64?.length || 0} chars`,
        [{ text: 'OK', style: 'default' }]
      );
    } catch (error) {
      console.error('PDF Generation Error:', error);
      Alert.alert(
        '❌ PDF Generation Failed',
        `Error: ${error instanceof Error ? error.message : String(error)}`,
        [{ text: 'OK', style: 'destructive' }]
      );
    } finally {
      setIsGenerating(false);
    }
  }, [htmlContent]);

  return (
    <ScrollView
      contentInsetAdjustmentBehavior="automatic"
      showsVerticalScrollIndicator={false}
      style={styles.container}
    >
      <Text style={styles.title}>📄 HTML to PDF Demo</Text>

      {/* Generate PDF Button */}
      <TouchableOpacity
        style={[styles.button, styles.generateButton]}
        onPress={submitPDF}
        disabled={isGenerating}
      >
        {isGenerating ? (
          <View style={styles.buttonContent}>
            <ActivityIndicator size="small" color="#fff" />
            <Text style={styles.buttonText}>Generating PDF...</Text>
          </View>
        ) : (
          <Text style={styles.buttonText}>🚀 Generate PDF</Text>
        )}
      </TouchableOpacity>

      {/* Last Result Display */}
      {lastResult && (
        <View style={[styles.resultCard]}>
          <Text style={[styles.resultTitle]}>✅ Last Generated PDF:</Text>
          <Text style={[styles.resultText]}>
            📁 Path: {lastResult.filePath}
          </Text>
          <Text style={[styles.resultText]}>
            📄 Pages: {lastResult.numberOfPages}
          </Text>
          {lastResult.base64 && (
            <Text style={[styles.resultText]}>
              💾 Base64: {lastResult.base64.length} characters
            </Text>
          )}
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    textAlign: 'center',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    fontWeight: '400',
    textAlign: 'center',
    marginBottom: 30,
  },
  button: {
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
    alignItems: 'center',
    marginVertical: 8,
  },
  generateButton: {
    backgroundColor: '#007AFF',
  },
  resetButton: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: '#007AFF',
  },
  buttonContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
    marginLeft: 8,
  },
  resultCard: {
    padding: 16,
    borderRadius: 8,
    marginVertical: 16,
    borderWidth: 1,
    borderColor: '#0066cc',
  },
  resultTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  resultText: {
    fontSize: 14,
    marginBottom: 4,
    fontFamily: 'monospace',
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    marginTop: 24,
    marginBottom: 12,
  },
  htmlInput: {
    borderWidth: 1,
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    fontFamily: 'monospace',
    minHeight: 200,
    marginBottom: 16,
  },
  infoSection: {
    marginTop: 30,
    padding: 16,
    backgroundColor: 'rgba(0, 122, 255, 0.1)',
    borderRadius: 8,
  },
  infoTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  infoText: {
    fontSize: 14,
    lineHeight: 20,
  },
});

export default App;
