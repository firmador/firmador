package cr.libre.firmador.previewers;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIShell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static cr.libre.firmador.previewers.PreviewScheduler.MAX_FILES_PROCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestPreviewScheduler {
    private final String testPDFPath =  FileSystems.getDefault().getPath("testDocs", "testPDF.pdf").toString();
    private final String testODTPath =  FileSystems.getDefault().getPath("testDocs", "testODT.odt").toString();
    private final String testDOCXPath =  FileSystems.getDefault().getPath("testDocs", "testDOCX.docx").toString();
    private final GUIShell gui = new GUIShell();
    private final Document testDocumentPDF1 = new Document(this.gui, this.testPDFPath);
    private final Document testDocumentPDF2 = new Document(this.gui, this.testPDFPath);
    private final Document testDocumentODT = new Document(this.gui, this.testODTPath);
    private final Document testDocumentDOCX = new Document(this.gui, this.testDOCXPath);
    private final Semaphore waitforfiles = spy(new Semaphore(1));
    private final Semaphore maxoffilesperprocess = spy(new Semaphore(MAX_FILES_PROCESS));
    private PreviewScheduler previewScheduler;

    @BeforeEach
    void createInstanceAndAssignSpies(){
        this.previewScheduler = new PreviewScheduler();
        // assign spies, so we can test and check what is happening with the semaphores everywhere
        this.previewScheduler.setWaitforfiles(this.waitforfiles);
        this.previewScheduler.setMaxoffilesperprocess(this.maxoffilesperprocess);
    }

    // ------ constructor method tests ------

    @Test
    void testCreatePreviewScheduler(){
        PreviewScheduler previewScheduler1 = new PreviewScheduler();

        assertInstanceOf(ArrayList.class, previewScheduler1.getFiles());
        assertTrue(previewScheduler1.getFiles().isEmpty());
    }

    @Test
    void testCreatePreviewSchedulerWithGUI(){
        PreviewScheduler previewSchedulerWithGui = new PreviewScheduler(this.gui);

        assertEquals(this.gui, previewSchedulerWithGui.getGui());
        assertInstanceOf(ArrayList.class, previewSchedulerWithGui.getFiles());
        assertTrue(previewSchedulerWithGui.getFiles().isEmpty());
    }

    // ------ run method tests ------

    @Test
    void testRunWithEmptyList() throws InterruptedException {
        assertFalse(this.previewScheduler.getStop());
        assertTrue(this.previewScheduler.getFiles().isEmpty());

        this.previewScheduler.start();
        Thread.sleep(100);   // let it run for 100ms before it is interrupted, so it can run the code as expected

        assertFalse(this.previewScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verifyNoInteractions(this.maxoffilesperprocess);  // this semaphore is not used since list is empty
    }

    @Test
    void testRunWithNonEmptyList() throws InterruptedException {
        this.previewScheduler.addDocument(this.testDocumentPDF1);
        assertFalse(this.previewScheduler.getStop());
        assertFalse(this.previewScheduler.getFiles().isEmpty());

        this.previewScheduler.start();
        Thread.sleep(100);   // let it run for 100ms before it is interrupted, so it can run the code as expected

        assertFalse(this.previewScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.maxoffilesperprocess, times(1)).acquire();  // method acquire was called once since the list had one document
        assertTrue(this.previewScheduler.getFiles().isEmpty());  // list is empty after the document is processed
    }

    /**
     * @Test void testRunCheckSemaphoresUse() throws InterruptedException{
     *       this.previewScheduler.start();
     * 
     *       this.previewScheduler.addDocument(this.testDocumentODT);
     *       Thread.sleep(500); // give it sometime between adding docs to let it
     *       adjust this.previewScheduler.addDocument(this.testDocumentPDF1);
     *       Thread.sleep(500); // give it sometime between adding docs to let it
     *       adjust this.previewScheduler.addDocument(this.testDocumentPDF2);
     *       Thread.sleep(500); // give it sometime between adding docs to let it
     *       adjust this.previewScheduler.addDocument(this.testDocumentDOCX);
     * 
     *       Thread.sleep(4000); // let it run for 4s before it is interrupted, so
     *       it can run the code as // expected
     * 
     *       int number0fDocs = 4; assertFalse(this.previewScheduler.getStop()); //
     *       the scheduler was not interrupted verify(this.waitforfiles,
     *       atLeastOnce()).acquire(); // method acquire was called at least one
     *       time verify(this.maxoffilesperprocess, times(number0fDocs)).acquire();
     *       // method acquire was called once per document
     *       verify(this.waitforfiles, times(number0fDocs)).release(); // method
     *       release was called once per document verify(this.maxoffilesperprocess,
     *       times(number0fDocs)).release(); // method release was called once per
     *       document when done
     *       assertTrue(this.previewScheduler.getFiles().isEmpty()); // list is
     *       empty after the documents are processed
     * 
     *       // check semaphores have the right status at the end
     *       assertFalse(this.maxoffilesperprocess.hasQueuedThreads()); // no more
     *       pending files to process
     *       assertTrue(this.waitforfiles.hasQueuedThreads()); // it is waiting for
     *       new file }
     **/
    @Test
    void testRunWithInterruptedException() throws InterruptedException {
        assertFalse(this.previewScheduler.getStop());
        assertTrue(this.previewScheduler.getFiles().isEmpty());

        InterruptedException e = spy(new InterruptedException());
        doThrow(e).when(this.waitforfiles).acquire();

        this.previewScheduler.run();

        assertTrue(this.previewScheduler.getStop()); // there was an exception so stop variable was changed
        verify(e).printStackTrace();  //  the printStackTrace method for the exception was called
    }

    // ------ addDocument method tests ------

    @Test
    void testAddDocument(){
        assertTrue(this.previewScheduler.getFiles().isEmpty());

        this.previewScheduler.addDocument(this.testDocumentPDF1);

        assertFalse(this.previewScheduler.getFiles().isEmpty());
        assertEquals(this.testDocumentPDF1, this.previewScheduler.getFiles().get(0));
        verify(this.waitforfiles, times(1)).release();  // method release was called once after the doc is added
    }

    // ------ done method tests ------

    @Test
    void testDone(){
        this.previewScheduler.done();

        verify(this.maxoffilesperprocess, times(1)).release();  // method release was called once
    }
}
