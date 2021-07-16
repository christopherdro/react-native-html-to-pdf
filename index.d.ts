export interface Options {
    html: string;
    fileName?: string;
    base64?: boolean;
    directory?: string;
    height?: number;
    width?: number;
  
    // iOS only
    paddingLeft?: number;
    paddingRight?: number;
    paddingTop?: number;
    paddingBottom?: number;
    padding?: number;
    bgColor?: string;
  
    // android only
    fonts?: string[];
  }
  
  export interface Pdf {
    filePath?: string;
    base64?: string;
  }
  
  export function convert(options: Options): Promise<Pdf>