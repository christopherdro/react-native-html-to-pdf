import HtmlToPdf, { type PDFOptions, type PDFResult } from './NativeHtmlToPdf';

export function generatePDF(options: PDFOptions): Promise<PDFResult> {
  return HtmlToPdf.convert(options);
}

export type { PDFOptions, PDFResult };
