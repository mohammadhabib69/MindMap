import com.mindmap.export.pdf.PdfExportService;
import com.mindmap.model.Note;
import java.io.File;

public class TestPdf {
    public static void main(String[] args) throws Exception {
        PdfExportService service = new PdfExportService();
        Note note = new Note();
        note.setTitle("বাংলা ও English Note");
        note.setContent("বাংলা ভাষা একটি সুন্দর ভাষা। Machine Learning is a field of Artificial Intelligence. Data Structures → Graph → Algorithm.");
        note.setSubject("Research");
        note.setDifficulty("Medium");
        
        File f = new File("test_out.pdf");
        service.exportSingleNote(note, f);
        System.out.println("PDF generated at: " + f.getAbsolutePath());
    }
}
