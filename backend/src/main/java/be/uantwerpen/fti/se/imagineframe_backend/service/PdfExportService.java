package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.pdfCreation.HeaderFooterPageEvent;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Component
public class PdfExportService {
    @Value("${pdfgeneration.iml_logo}")
    private String logoImagineLab;

    private final ProductRepository productRepository;

    private final float logoWidthPagePercentage = 0.3F;
    private final String localDateFormat = "dd MMMM yyyy HH:mm";
    private final Font fontTitle;
    private final Font fontSubTitle;
    private final Font fontDefault;
    private final Font fontHeader;
    private final Font fontFooter;
    private final Font fontTableHeaderRow;
    // #3399ff = RGB(51,153,255) is the highlight color of ImagineLab
    private final Color tableHeaderColor = new Color(51, 153, 255);
    private final float tableHeaderRowPadding;

    private final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    public PdfExportService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        try {
            // BaseFont is Helvetica.
            // Uses the CP1252 encoding: Western European (WinAnsi) — standard for most Western-language PDFs.
            // Will not be embedded in the PDF since it is a built-in font.
            BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            fontTitle = new Font(baseFont, 24, Font.BOLD, Color.BLACK);
            fontSubTitle = new Font(baseFont, 18, Font.BOLD, Color.BLACK);
            fontDefault = new Font(baseFont, 12, Font.NORMAL, Color.BLACK);
            fontHeader = new Font(baseFont, 10, Font.BOLD, Color.BLACK);
            fontFooter = new Font(baseFont, 10, Font.NORMAL, Color.BLACK);
            fontTableHeaderRow = new Font(baseFont, 16, Font.BOLD, Color.BLACK);
            tableHeaderRowPadding = 0.3F * fontTableHeaderRow.getSize();
        } catch (IOException e) {
            Random random = new Random(0);
            int randomInt = random.nextInt(1000);
            logger.error("Error {}, {}", randomInt, e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error, " + randomInt);
        }
    }

    // Method to test the creation of a report.
    //@PostConstruct
    public void testing() {
        String tempLocation = "./productReport.pdf";
        File file = new File(tempLocation);
        file.getParentFile().mkdirs();
        ByteArrayInputStream byteArrayInputStream = getProductStockPdfReport("Johny Doe");
        try {
            IOUtils.copy(byteArrayInputStream, new FileOutputStream(tempLocation));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Specific export methods
    @Transactional
    public ByteArrayInputStream getProductStockPdfReport(String username) {
        // 1, Set up the document
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = setupReport("Stock overview report", username, outputStream);

        // 2, Add tables to the document
        Iterable<Product> products = productRepository.findAll();
        for (Product product : products) {
            Set<Batch> batches = product.getBatches();
            LinkedHashMap<String, ArrayList<String>> dataMap = new LinkedHashMap<>();
            String number = "Number";
            String dateAdded = "Date added";
            String price = "Unit price";
            String quantity = "Quantity";
            dataMap.put(number, new ArrayList<>());
            dataMap.put(dateAdded, new ArrayList<>());
            dataMap.put(price, new ArrayList<>());
            dataMap.put(quantity, new ArrayList<>());
            if (!batches.isEmpty()) {
                double meanPrice = 0;
                int totalQuantity = 0;
                List<Batch> sortedBatches = batches.stream()
                        .sorted(Comparator.comparing(Batch::getAddedDate))
                        .toList();
                for (int batchNumber = 0; batchNumber < sortedBatches.size(); batchNumber++) {
                    Batch batch = sortedBatches.get(batchNumber);
                    meanPrice += (batch.getUnitPrice() / sortedBatches.size());
                    totalQuantity += batch.getQuantity();
                    dataMap.get(number).add(String.valueOf(batchNumber));
                    dataMap.get(dateAdded).add(batch.getAddedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
                    dataMap.get(price).add(String.format("%.2f", batch.getUnitPrice()));
                    dataMap.get(quantity).add(String.valueOf(batch.getQuantity()));
                }
                dataMap.get(number).add("Totals");
                dataMap.get(dateAdded).add("");
                dataMap.get(price).add(String.format("Average: %.2f", meanPrice));
                dataMap.get(quantity).add(String.valueOf(totalQuantity));
            }
            addTableToPdf(document, product.getName(), product.getDescription(), dataMap);
        }

        // 3, Close the document and return the ByteArrayStream.
        document.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    // General needed functions

    /**
     * Does the general setup to generate a report.
     *
     * @param title    title of the report.
     * @param username username of the person who asked for the report.
     * @return a document to work on further.
     */
    public Document setupReport(String title, String username, ByteArrayOutputStream outputStream) {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);

        // Add header and footer
        writer.setPageEvent(createHeaderAndFooterEvent(username));
        document.open();
        // Add content
        addLogoToPdf(document);
        addTitleToPdf(document, title);

        return document;
    }

    //@PostConstruct // Just for testing!
    public void generatePdf() {
        String tempLocation = "./test.pdf";
        File file = new File(tempLocation);
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(tempLocation)) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, fos);

            // Add header and footer
            writer.setPageEvent(createHeaderAndFooterEvent("John Doe"));

            document.open();

            // Add content
            addLogoToPdf(document);
            addTitleToPdf(document, "Testing my PDF generation class!");
            ArrayList<String> list = new ArrayList();
            for (int i = 0; i < 64; i++) {
                list.add("hi " + i + "!");
            }
            LinkedHashMap<String, ArrayList<String>> data = new LinkedHashMap<>();
            data.put("test text", list);
            data.put("column 2", list);
            data.put("column 3", list);
            addTableToPdf(document, "test table", "table description, Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sociosqu lacinia pellentesque duis at ridiculus per cras dignissim.", data);

            document.close();
        } catch (IOException e) {
            Random random = new Random(0);
            int randomInt = random.nextInt(1000);
            logger.error("Error {}, {}", randomInt, e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error, " + randomInt);
        }
        System.out.println("PDF generated");
    }

    protected HeaderFooterPageEvent createHeaderAndFooterEvent(String username) {
        String localDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern(localDateFormat));
        String text = String.format("This report was generated on %s by %s.", localDateString, username);
        return new HeaderFooterPageEvent("", text, fontHeader, fontFooter);
    }

    protected void addLogoToPdf(Document document) {
        Image image;
        try {
            image = Image.getInstance(logoImagineLab);
        } catch (IOException e) {
            Random random = new Random(0);
            int randomInt = random.nextInt(1000);
            logger.error("Error {}, {}", randomInt, e.getMessage());
            logger.error(Arrays.toString(e.getStackTrace()));
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error, " + randomInt);
        }
        image.setAlignment(Image.ALIGN_RIGHT);
        float vSpace = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
        image.scalePercent(logoWidthPagePercentage * 100 / (image.getWidth() / vSpace));
        document.add(image);
    }

    protected void addTitleToPdf(Document document, String title) {
        Paragraph p1 = new Paragraph();

        Paragraph titleParagraph = new Paragraph(title, fontTitle);
        titleParagraph.setAlignment(Element.ALIGN_LEFT);
        p1.add(titleParagraph);

        document.add(p1);
    }

    protected void addTableToPdf(Document document, String tableName, String tableDescription, LinkedHashMap<String, ArrayList<String>> data) {
        // A linked hash map is used to preserve the insertion order of the values/keys in the map.

        // 1, add table title
        Paragraph paragraphTableInfo = new Paragraph();
        leaveEmptyLine(paragraphTableInfo, 1);
        paragraphTableInfo.add(new Paragraph(tableName, fontSubTitle));
        if (!tableDescription.isBlank()) {
            paragraphTableInfo.add(new Paragraph(tableDescription, fontDefault));
        }
        leaveEmptyLine(paragraphTableInfo, 1);
        paragraphTableInfo.setAlignment(Element.ALIGN_LEFT);
        document.add(paragraphTableInfo);

        // 2, create table with correct number of columns
        PdfPTable table = new PdfPTable(data.size());
        table.setWidthPercentage(100);

        // 3, add first table row
        ArrayList<String> keys = new ArrayList<>(data.keySet());
        for (String key : keys) {
            PdfPCell cell = new PdfPCell(new Paragraph(key, fontTableHeaderRow));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingBottom(tableHeaderRowPadding);
            cell.setBackgroundColor(tableHeaderColor);
            table.addCell(cell);
        }

        // 4, add table content
        // This looks weird but the table is filled from left to right from top to bottom
        // This function makes sure it is added in the correct order.
        int numberOfCells = keys.size() * data.get(keys.getFirst()).size();
        for (int index = 0; index < numberOfCells; index++) {
            int keyIndex = index % keys.size();
            int valueIndex = index / keys.size();
            String key = keys.get(keyIndex);
            String value = data.get(key).get(valueIndex);
            PdfPCell cell = new PdfPCell(new Paragraph(value, fontDefault));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
        // 5, add table to document
        document.add(table);
    }

    private static void leaveEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}
