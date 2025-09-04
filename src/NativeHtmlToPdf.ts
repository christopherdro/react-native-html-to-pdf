import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface PDFOptions {
  html: string;
  fileName?: string;
  width?: number;
  height?: number;
  base64?: boolean;
  padding?: number;
  paddingTop?: number;
  paddingBottom?: number;
  paddingLeft?: number;
  paddingRight?: number;
  bgColor?: string;
  directory?: string;
  baseURL?: string;
  shouldPrintBackgrounds?: boolean;
}

export interface PDFResult {
  filePath: string;
  base64?: string;
  numberOfPages?: number;
}

export interface Spec extends TurboModule {
  convert(options: PDFOptions): Promise<PDFResult>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('HtmlToPdf');
