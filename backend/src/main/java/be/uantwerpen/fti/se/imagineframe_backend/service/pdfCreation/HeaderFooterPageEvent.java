package be.uantwerpen.fti.se.imagineframe_backend.service.pdfCreation;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class HeaderFooterPageEvent extends PdfPageEventHelper {
    private String header;
    private String footer;
    private Font headerFont;
    private Font footerFont;

    public HeaderFooterPageEvent(String header, String footer, Font headerFont, Font footerFont) {
        this.header = header;
        this.footer = footer;
        this.headerFont = headerFont;
        this.footerFont = footerFont;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            float pageHeight = document.getPageSize().getHeight();
            float pageWidth = document.getPageSize().getWidth();
            float headerWidth = headerFont.getBaseFont().getWidthPoint(header, headerFont.getSize());
            float footerWidth = footerFont.getBaseFont().getWidthPoint(footer, footerFont.getSize());

            // Add header
            cb.beginText();
            cb.setFontAndSize(headerFont.getBaseFont(), headerFont.getSize());
            cb.moveText(pageWidth - document.rightMargin() - headerWidth, pageHeight - document.topMargin() + headerFont.getSize());
            cb.showText(header);
            cb.endText();

            // Add footer
            cb.beginText();
            cb.setFontAndSize(footerFont.getBaseFont(), footerFont.getSize());
            cb.moveText(pageWidth - document.rightMargin() - footerWidth, document.bottomMargin() - footerFont.getSize());
            cb.showText(footer);
            cb.endText();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }
}
