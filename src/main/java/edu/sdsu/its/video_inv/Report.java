package edu.sdsu.its.video_inv;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import edu.sdsu.its.video_inv.Models.Transaction;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import static com.itextpdf.text.PageSize.LETTER;

/**
 * Generate PDF Reports, especially useful for items that are non mutable, like Transactions.
 *
 * @author Tom Paulus
 *         Created on 1/1/17.
 */
public class Report {
    private static final Logger LOGGER = Logger.getLogger(Report.class);

    private static final CMYKColor COLOR_GRAY = new CMYKColor(40, 30, 20, 66);

    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLACK);

    private static final Font FONT_BODY = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_BODY_BOLD = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_BODY_ITALIC = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.BLACK);

    private static final Font FONT_CAPTION = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, COLOR_GRAY);

    private static final Font FONT_FOOTER_PAGE_NUM = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_FOOTER_TIMESTAMP = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, COLOR_GRAY);


    /**
     * Generate a PDF Transaction record for the provided transaction.
     * The returned file is a Temporary file and should be treated as such, make a copy and save it if necessary.
     *
     * @param transaction {@link Transaction} Transaction for which the report should be generated
     * @return {@link File} Temporary PDF File
     */
    public static File transactionReport(final Transaction transaction) {
        HashMap<Integer, String> categoryIcons = new HashMap<>();
        File outFile = null;

        try {
            outFile = File.createTempFile("trx-" + transaction.id, ".pdf");
            Document document = new Document(LETTER, 50, 50, 50f, 50);

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outFile));
            document.open();
            final URL logoURL = Report.class.getClassLoader().getResource("logo.png");
            if (logoURL == null) {
                LOGGER.error("Logo Not found in Resources Folder");
                return null;
            }

            // Logo
            Image logo = Image.getInstance(logoURL);
            logo.scaleToFit(new Rectangle(document.getPageSize().getWidth(), 50f));
            logo.setAbsolutePosition(50f, document.getPageSize().getHeight() - 100f);
            document.add(logo);

            // Barcode
            File barcodeFile = File.createTempFile("out", ".png");
            Barcode.generateCode128Barcode(transaction.id, barcodeFile);
            Image barcode = Image.getInstance(barcodeFile.getAbsolutePath());
            barcode.scaleToFit(new Rectangle(document.getPageSize().getWidth(), 40f));
            barcode.setAbsolutePosition(document.getPageSize().getWidth() - 125f, document.getPageSize().getHeight() - 95f);
            document.add(barcode);

            // HR under Logo and Barcode
            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorStroke(COLOR_GRAY);
            canvas.setLineWidth(.25f);
            canvas.moveTo(50f, document.getPageSize().getHeight() - 110f);
            canvas.lineTo(document.getPageSize().getWidth() - 50f, document.getPageSize().getHeight() - 110f);
            canvas.closePathStroke();

            // Header
            Paragraph header = new Paragraph();
            header.setFont(FONT_HEADER);
            header.add("Transaction Receipt – " + transaction.id.toUpperCase());
            header.setSpacingBefore(75f);
            document.add(header);

            // Info Table (Owner, Supervisor, Etc.)
            PdfPTable table = new PdfPTable(new float[]{2, 4, 0.5f, 2, 4});
            table.setWidthPercentage(100);
            table.setSpacingBefore(20f);
            table.setSpacingAfter(5f);

            // first row (Date & Direction)
            PdfPCell cell = new PdfPCell(new Phrase("Date: ", FONT_BODY_BOLD));
            cell.setFixedHeight(30);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(new SimpleDateFormat("MMMM dd, yyyy hh:mm aa").format(transaction.time), FONT_BODY));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(); // Blank Spacing Cell
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Direction: ", FONT_BODY_BOLD));
            cell.setFixedHeight(30);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase((transaction.direction ? "Check In" : "Check Out"), FONT_BODY));
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            // second row (Owner & Supervisor
            cell = new PdfPCell(new Phrase("Owner:", FONT_BODY_BOLD));
            cell.setFixedHeight(30);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            Phrase phrase = new Phrase();
            phrase.add(
                    new Chunk(String.format("%s %s", transaction.owner.firstName, transaction.owner.lastName), FONT_BODY)
            );
            phrase.add(
                    new Chunk(String.format(" (%s)", transaction.owner.username), FONT_BODY_ITALIC)
            );
            cell = new PdfPCell(phrase);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(); // Blank Spacing Cell
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("Supervisor: ", FONT_BODY_BOLD));
            cell.setFixedHeight(30);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            phrase = new Phrase();
            phrase.add(
                    new Chunk(String.format("%s %s", transaction.supervisor.firstName, transaction.supervisor.lastName), FONT_BODY)
            );
            phrase.add(
                    new Chunk(String.format(" (%s)", transaction.supervisor.username), FONT_BODY_ITALIC)
            );
            cell = new PdfPCell(phrase);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);

            document.add(table);

            // Transaction Components
            table = new PdfPTable(new float[]{.75f, 1.5f, 3.5f, 5});
            table.setHeaderRows(1);
            table.setSplitRows(false);
            table.setComplete(false);
            table.setWidthPercentage(100);
            table.setSpacingBefore(0f);
            table.setSpacingAfter(5f);

            table.getDefaultCell().setFixedHeight(30);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

            table.getDefaultCell().setBorder(Rectangle.BOTTOM);
            table.addCell(new Phrase("", FONT_BODY_BOLD));
            table.addCell(new Phrase("ID", FONT_BODY_BOLD));
            table.addCell(new Phrase("Name", FONT_BODY_BOLD));
            table.addCell(new Phrase("Comments", FONT_BODY_BOLD));

            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            int count = 0;
            for (Transaction.Component component : transaction.components) {
                String icon = categoryIcons.computeIfAbsent(component.category.id, k -> DB.getCategoryIcon(component.category.id));

                if (icon != null && !icon.isEmpty()) {
                    Image image = Image.getInstance(Base64.decodeBase64(icon));
                    image.scaleAbsoluteHeight(8);
                    table.addCell(image);
                } else {
                    table.addCell("");
                }

                Phrase phrase1 = new Phrase();
                phrase1.add(new Chunk(component.name + "\n", FONT_BODY));
                if (component.assetID != null && !component.assetID.isEmpty())
                    phrase1.add(new Chunk("Serial/Asset: " + component.assetID, FONT_CAPTION)
                            .setLineHeight(12));

                table.addCell(new Phrase(Integer.toString(component.pubID), FONT_BODY));
                table.addCell(phrase1);
                table.addCell(new Phrase(component.comments, FONT_BODY));

                if (count++ % 5 == 0) {
                    document.add(table);
                }
            }

            table.setComplete(true);
            document.add(table);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            return addPageNumbers(outFile);
        } catch (Exception e) {
            LOGGER.warn("Problem Adding Page Numbers to Transaction Report - " + transaction.id, e);
            return outFile;
        }
    }

    private static File addPageNumbers(File original) throws IOException, DocumentException {
        File stampedOutFile = File.createTempFile(original.getName() + "-stamped", ".pdf");
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(stampedOutFile));
        document.open();
        PdfReader reader = new PdfReader(original.getAbsolutePath());
        int numberOfPages = reader.getNumberOfPages();
        PdfImportedPage page;
        PdfCopy.PageStamp stamp;

        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

        for (int i = 0; i < numberOfPages; ) {
            page = copy.getImportedPage(reader, ++i);
            stamp = copy.createPageStamp(page);
            // add page numbers
            ColumnText.showTextAligned(
                    stamp.getUnderContent(), Element.ALIGN_RIGHT,
                    new Phrase(String.format("Page %d of %d", i, numberOfPages), FONT_FOOTER_PAGE_NUM),
                    545f, 30, 0);

            // add generated on footer
            ColumnText.showTextAligned(
                    stamp.getUnderContent(), Element.ALIGN_LEFT,
                    new Phrase(String.format("Generated by %s at %s ", getHost(), fmt.print(new DateTime())), FONT_FOOTER_TIMESTAMP),
                    50, 28, 0);
            stamp.alterContents();
            copy.addPage(page);
        }
        document.close();
        reader.close();

        return stampedOutFile;
    }

    private static String getHost() {
        String hostname = "Apache Tomcat"; // Default Hostname in case of error.

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            LOGGER.warn("Hostname can not be resolved", ex);
        }

        return hostname;
    }
}
