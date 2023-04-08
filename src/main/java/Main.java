import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        testPdfOCR();
    }

    public static void testPdfOCR() {

        File initialFile = new File("sample.pdf");

        for (int i = 0; i < 50; i++) {

            try (PDDocument pdDocument = PDDocument.load(Files.newInputStream(initialFile.toPath()));
                 PdfProcessor pdfProcessor = new PdfProcessor(pdDocument)) {

                pdfProcessor.processPages();

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }


}