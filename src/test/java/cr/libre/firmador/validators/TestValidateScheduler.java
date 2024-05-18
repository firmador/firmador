package cr.libre.firmador.validators;

import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIShell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static cr.libre.firmador.validators.ValidateScheduler.MAX_FILES_PROCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class TestValidateScheduler {
    private final String testPDFPath =  FileSystems.getDefault().getPath("testDocs", "testPDF.pdf").toString();
    private final String testODTPath =  FileSystems.getDefault().getPath("testDocs", "testODT.odt").toString();
    private final String testDOCXPath =  FileSystems.getDefault().getPath("testDocs", "testDOCX.docx").toString();
    private final GUIShell gui = spy(new GUIShell());
    private final Document testDocumentPDF1 = new Document(this.gui, this.testPDFPath);
    private final Document testDocumentPDF2 = new Document(this.gui, this.testPDFPath);
    private final Document testDocumentODT = new Document(this.gui, this.testODTPath);
    private final Document testDocumentDOCX = new Document(this.gui, this.testDOCXPath);
    private final Semaphore waitforfiles = spy(new Semaphore(1));
    private final Semaphore maxoffilesperprocess = spy(new Semaphore(MAX_FILES_PROCESS));
    private ValidateScheduler validateScheduler;

    @BeforeEach
    void createInstanceAndAssignSpies(){
        this.validateScheduler = new ValidateScheduler(this.gui);
        // assign spies, so we can test and check what is happening with the semaphores everywhere
        this.validateScheduler.setWaitforfiles(this.waitforfiles);
        this.validateScheduler.setMaxoffilesperprocess(this.maxoffilesperprocess);
    }

    // ------ constructor method tests ------

   @Test
    void testCreateValidateScheduler(){
        ValidateScheduler validateScheduler1 = new ValidateScheduler(this.gui);

        assertEquals(this.gui, validateScheduler1.getGui());
        assertInstanceOf(ArrayList.class, validateScheduler1.getFiles());
        assertTrue(validateScheduler1.getFiles().isEmpty());
    }

    // ------ run method tests ------

    @Test
    void testRunWithEmptyList() throws InterruptedException {
        assertFalse(this.validateScheduler.getStop());
        assertTrue(this.validateScheduler.getFiles().isEmpty());

        this.validateScheduler.start();
        Thread.sleep(1000);   // let it run for 1s before it is interrupted, so it can run the code as expected

        assertFalse(this.validateScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verifyNoInteractions(this.maxoffilesperprocess);  // this semaphore is not used since list is empty
    }

    @Test
    void testRunWithNonEmptyList() throws InterruptedException {
        this.validateScheduler.addDocument(this.testDocumentPDF1);
        assertFalse(this.validateScheduler.getStop());
        assertFalse(this.validateScheduler.getFiles().isEmpty());

        this.validateScheduler.start();
        Thread.sleep(100);   // let it run for 100ms before it is interrupted, so it can run the code as expected

        assertFalse(this.validateScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.maxoffilesperprocess, times(1)).acquire();  // method acquire was called once since the list had one document
        assertTrue(this.validateScheduler.getFiles().isEmpty());  // list is empty after the document is processed
    }
    /**
    @Test
    void testRunCheckSemaphoresUse() throws InterruptedException{
        this.validateScheduler.start();

        this.validateScheduler.addDocument(this.testDocumentODT);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.validateScheduler.addDocument(this.testDocumentPDF1);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.validateScheduler.addDocument(this.testDocumentPDF2);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.validateScheduler.addDocument(this.testDocumentDOCX);

        Thread.sleep(1000);   // let it run for 1s before it is interrupted, so it can run the code as expected

        int number0fDocs = 4;
        assertFalse(this.validateScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.maxoffilesperprocess, times(number0fDocs)).acquire();  // method acquire was called once per document
        verify(this.waitforfiles, times(number0fDocs)).release();  // method release was called once per document
        verify(this.maxoffilesperprocess, times(number0fDocs)).release();  // method release was called once per document when done
        assertTrue(this.validateScheduler.getFiles().isEmpty());  // list is empty after the documents are processed

        // check semaphores have the right status at the end
        assertFalse(this.maxoffilesperprocess.hasQueuedThreads());  // no more pending files to process
        assertTrue(this.waitforfiles.hasQueuedThreads());  // it is waiting for new file
    }
    **/
    @Test
    void testRunWithInterruptedException() throws InterruptedException {
        assertFalse(this.validateScheduler.getStop());
        assertTrue(this.validateScheduler.getFiles().isEmpty());

        InterruptedException e = spy(new InterruptedException());
        doThrow(e).when(this.waitforfiles).acquire();

        this.validateScheduler.run();

        assertTrue(this.validateScheduler.getStop()); // there was an exception so stop variable was changed
        verify(e).printStackTrace();  //  the printStackTrace method for the exception was called
    }

    // ------ addDocument method tests ------

    @Test
    void testAddDocument(){
        assertTrue(this.validateScheduler.getFiles().isEmpty());

        this.validateScheduler.addDocument(this.testDocumentPDF1);

        assertFalse(this.validateScheduler.getFiles().isEmpty());
        assertEquals(this.testDocumentPDF1, this.validateScheduler.getFiles().get(0));
        verify(this.waitforfiles, times(1)).release();  // method release was called once after the doc is added
    }

    // ------ addDocuments method tests ------

    @Test
    void testAddDocuments(){
        assertTrue(this.validateScheduler.getFiles().isEmpty());

        ArrayList<Document> docsToAdd= new ArrayList<>(List.of(this.testDocumentPDF1, this.testDocumentDOCX, this.testDocumentODT));
        this.validateScheduler.addDocuments(docsToAdd);

        assertFalse(this.validateScheduler.getFiles().isEmpty());
        assertEquals(docsToAdd, this.validateScheduler.getFiles());
        verify(this.waitforfiles, times(1)).release();  // method release was called once after all the docs are added
    }

    // ------ done method tests ------

    @Test
    void testDoneAllPermitsAvailable() throws InterruptedException{
        this.maxoffilesperprocess.acquire();  // it will be released on done

        this.validateScheduler.done();

        assertEquals(MAX_FILES_PROCESS, this.maxoffilesperprocess.availablePermits());  // all permits are released
        verify(this.maxoffilesperprocess, times(1)).release();  // method release was called once
        verify(this.gui, times(1)).validateAllDone();  // method validateAllDone was called
    }

    @Test
    void testDoneNotAllPermitsAvailable() throws InterruptedException {
        this.maxoffilesperprocess.acquire();  // one of these will be released on done
        this.maxoffilesperprocess.acquire();

        this.validateScheduler.done();

        assertNotEquals(MAX_FILES_PROCESS, this.maxoffilesperprocess.availablePermits());  // one permit is still not released
        verify(this.maxoffilesperprocess, times(1)).release();  // method release was called once
        verifyNoInteractions(this.gui);  // method validateAllDone was not called
    }
}
