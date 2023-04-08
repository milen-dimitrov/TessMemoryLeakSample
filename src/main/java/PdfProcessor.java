import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.stream.IntStream;

class PdfProcessor implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(PdfProcessor.class);
    private final PDDocument pdDocument;
    private final String[] pages;
    private final PDFRenderer renderer;
    private final ExecutorService scheduleExecutor = Executors.newFixedThreadPool(4);

    public PdfProcessor(PDDocument pdDocument) {

        this.pdDocument = pdDocument;
        renderer = new PDFRenderer(pdDocument);
        pages = new String[pdDocument.getNumberOfPages()];
    }

    public void processPages() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        BlockingQueue<Integer> unprocessedPages = new LinkedBlockingQueue<>();

        for (int pageNum = 1; pageNum <= pdDocument.getNumberOfPages(); pageNum++)
            unprocessedPages.put(pageNum);

        Runnable threadProcess = () ->
        {
            Integer pageNum;

            do {
                pageNum = unprocessedPages.poll();

                if (pageNum != null) {
                    pages[pageNum - 1] = processPageWithOCR(pageNum);
                    logger.info("Processed page " + pageNum);
                }
            }
            while (pageNum != null);
        };

        CompletableFuture<?>[] futures = IntStream.rangeClosed(1, 4)
                .mapToObj(w -> CompletableFuture.runAsync(threadProcess, scheduleExecutor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Processing pages finished in " + duration + " ms\n");
    }

    public String getText()
    {
        StringBuilder sb = new StringBuilder();

        for (String page : pages) {
            sb.append(page);
        }

        return sb.toString();
    }

    private String processPageWithOCR(int pageNum) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        tesseract.setLanguage("deu+eng");

        BufferedImage pageImage = renderPage(pageNum - 1);
        try {
            return tesseract.doOCR(pageImage);
        } catch (TesseractException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedImage renderPage(int pageNum) {
        synchronized (renderer) {
            try {
                return renderer.renderImage(pageNum, 2.0f, ImageType.RGB);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        try {
            pdDocument.close();
            scheduleExecutor.shutdownNow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
