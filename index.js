import { NativeModules } from 'react-native';
const RnHTMLtoPDF = NativeModules.RNHTMLtoPDF

const pageOptions = {
    orientation: {
        Landscape: 'Landscape',
        Portrait: 'Portrait',
    },

    size: {
        A0: { id: 'A0', mm: { w: 841, h: 1189 }},
        A1: { id: 'A1', mm: { w: 594, h: 841 }},
        A2: { id: 'A2', mm: { w: 420, h: 594 }},
        A3: { id: 'A3', mm: { w: 297, h: 420 }},
        A4: { id: 'A4', mm: { w: 210, h: 297 }},
        A5: { id: 'A5', mm: { w: 148, h: 210 }},
        A6: { id: 'A6', mm: { w: 105, h: 148 }},
        A7: { id: 'A7', mm: { w: 74, h: 105 }},
        A8: { id: 'A8', mm: { w: 52, h: 74 }},
        UsGovernmentLetter: { id: 'UsGovernmentLetter', mm: { w: 203.2, h: 266.7 }},
        UsLetter: { id: 'UsLetter', mm: { w: 215.9, h: 279.4 }},
        UsLegal: { id: 'UsLegal', mm: { w: 279.4, h: 355.6 }},
    },
};


const pdfOptionsDefault = {
    page: {
        orientation: pageOptions.orientation.Portrait,
        size: pageOptions.size.UsLetter,
    },    
};

const RNHTMLtoPDF = {
    page: pageOptions,

    async convert(options) {

        if(!options.page) {
            options.page = pdfOptionsDefault.page;
        }
        if(!options.page.size) {
            options.page.size =  pdfOptionsDefault.page.size;
        }
        if(!options.page.orientation) {
            options.page.orientation =  pdfOptionsDefault.page.orientation;
        }        
        
        const result = await RnHTMLtoPDF.convert(options);
        return result;
    },
   
};

export default RNHTMLtoPDF