package PDFToText;
import java.io.FileWriter;
import java.io.IOException;

public class PDFToText {

    public static void main(String[] args) {
        PDFManager pdfManager = new PDFManager();
        pdfManager.setFilePath("/Users/debojyotipaul/Documents/CSE Summer projects/Project 7 - Finanace Companion/PDF Path/report.pdf");
        try {
            String text = pdfManager.toText();
            saveAsTextFile(text, "/Users/debojyotipaul/Documents/CSE Summer projects/Project 7 - Finanace Companion/report.txt");
            System.out.println("Text saved successfully.");
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public static void saveAsTextFile(String text, String outputPath) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(text);
        }
    }
}
