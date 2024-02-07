package cr.libre.firmador.signers;

import cr.libre.firmador.cards.CardSignInfo;
import cr.libre.firmador.documents.Document;
import cr.libre.firmador.gui.GUIShell;
import cr.libre.firmador.gui.swing.SignProgressDialogWorker;
import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

import java.beans.PropertyChangeEvent;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static cr.libre.firmador.signers.SignerScheduler.MAX_FILES_PROCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class TestSignerScheduler {
    private final String testPDFPath =  FileSystems.getDefault().getPath("testDocs", "testPDF.pdf").toString();
    private final String testODTPath =  FileSystems.getDefault().getPath("testDocs", "testODT.odt").toString();
    private final String testDOCXPath =  FileSystems.getDefault().getPath("testDocs", "testDOCX.docx").toString();
    private final GUIShell gui = mock(GUIShell.class);  // all methods are mocked - it won't ask for the PIN
    private final SignProgressDialogWorker progressMonitor = spy(new SignProgressDialogWorker());
    private final Document testDocumentPDF1 = spy(new Document(this.gui, this.testPDFPath));
    private final Document testDocumentPDF2 = spy(new Document(this.gui, this.testPDFPath));
    private final Document testDocumentODT = spy(new Document(this.gui, this.testODTPath));
    private final Document testDocumentDOCX = spy(new Document(this.gui, this.testDOCXPath));
    private final Semaphore waitforfiles = spy(new Semaphore(1));
    private final Semaphore maxoffilesperprocess = spy(new Semaphore(MAX_FILES_PROCESS));
    private final CardSignInfo cardSignInfo = new CardSignInfo(1, "12345", "111111111");
    private final SignerWorker task = spy(new SignerWorker(this.signerScheduler, this.progressMonitor, this.gui, this.testDocumentPDF1, this.cardSignInfo));
    private SignerScheduler signerScheduler;

    @RegisterExtension
    LogCapturer signerSchedulerLog = LogCapturer.create().captureForType(SignerScheduler.class, Level.DEBUG);

    @BeforeEach
    void createInstanceAndAssignSpiesAndMockMethods(){
        this.signerScheduler = new SignerScheduler(this.gui, this.progressMonitor);

        // assign spies, so we can test and check what is happening with the semaphores everywhere
        this.signerScheduler.setWaitforfiles(this.waitforfiles);
        this.signerScheduler.setMaxoffilesperprocess(this.maxoffilesperprocess);

        // do nothing when the documents call the sign method - actual signs are not required for tests in this class
        doNothing().when(this.testDocumentODT).sign(any());
        doNothing().when(this.testDocumentPDF1).sign(any());
        doNothing().when(this.testDocumentDOCX).sign(any());
        doNothing().when(this.testDocumentPDF2).sign(any());
    }

    // ------ constructor method tests ------

    @Test
    void testCreateSignerScheduler(){
        SignerScheduler signerScheduler1 = new SignerScheduler(this.gui, this.progressMonitor);

        assertEquals(this.gui, signerScheduler1.getGui());
        assertEquals(this.progressMonitor, signerScheduler1.getProgressMonitor());
        assertInstanceOf(ArrayList.class, signerScheduler1.getFiles());
        assertTrue(signerScheduler1.getFiles().isEmpty());
    }

    // ------ run method tests ------

    @Test
    void testRunWithEmptyList() throws InterruptedException {
        assertFalse(this.signerScheduler.getStop());
        assertTrue(this.signerScheduler.getFiles().isEmpty());

        this.signerScheduler.start();
        Thread.sleep(100);   // let it run for 100ms before it is interrupted, so it can run the code as expected

        assertFalse(this.signerScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verifyNoInteractions(this.maxoffilesperprocess);  // this semaphore is not used since list is empty
        verify(this.progressMonitor, never()).setTitle(String.format("Proceso de firmado de %d  documentos", signerScheduler.getFiles().size()));  // method was never called
        verify(this.progressMonitor, never()).setHeaderTitle("Firmando documento");  // method was never called
        verify(this.progressMonitor, never()).setVisible(true);  // method was never called
    }

    @Test
    void testRunWithNonEmptyListAndNotNullCard() throws InterruptedException{
        when(this.gui.getPin()).thenReturn(this.cardSignInfo);  // set the getPin method return value, so no need to have an actual card connected

        this.signerScheduler.addDocument(this.testDocumentPDF1);

        assertFalse(this.signerScheduler.getStop());
        assertFalse(this.signerScheduler.getFiles().isEmpty());

        this.signerScheduler.start();
        Thread.sleep(1000);   // let it run for 1s before it is interrupted, so it can run the code as expected

        assertFalse(this.signerScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setTitle(String.format("Proceso de firmado de %d documento(s)", signerScheduler.getFiles().size()));  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setHeaderTitle("Firmando documento");  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setVisible(true);  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setProgressStatus(0);  // method was called at least one time

        verify(this.maxoffilesperprocess, times(1)).acquire();  // method acquire was called once since the list had one document
        verify(this.progressMonitor, times(1)).setHeaderTitle("Firmando " + this.testDocumentPDF1.getName());  // method was called once with the document name
        assertTrue(this.signerScheduler.getFiles().isEmpty());  // list is empty after the document is processed
    }

    @Test
    void testRunWithNonEmptyListAndNotNullCardWithInterruptedException() throws InterruptedException{
        when(this.gui.getPin()).thenReturn(this.cardSignInfo);  // set the getPin method return value, so no need to have an actual card connected

        this.signerScheduler.addDocument(this.testDocumentPDF1);
        assertFalse(this.signerScheduler.getStop());
        assertFalse(this.signerScheduler.getFiles().isEmpty());

        InterruptedException e = spy(new InterruptedException());
        doThrow(e).when(this.maxoffilesperprocess).acquire();

        this.signerScheduler.start();
        Thread.sleep(1000);   // let it run for 1s before it is interrupted, so it can run the code as expected

        verify(e).printStackTrace();  //  the printStackTrace method for the exception was called
        assertFalse(this.signerSchedulerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.signerSchedulerLog.getEvents().get(0);
        assertTrue(logEntry.getMessage().contains("Interrupción al obtener bloqueo del hilo en documento: " + this.testDocumentPDF1.getPathName()));
        assertInstanceOf(InterruptedException.class, logEntry.getThrowable());  // the right type of exception happened

        assertFalse(this.signerScheduler.getStop());  // the scheduler was not interrupted, the variable was not changed
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setTitle(String.format("Proceso de firmado de %d documento(s)", signerScheduler.getFiles().size()));  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setHeaderTitle("Firmando documento");  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setVisible(true);  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setProgressStatus(0);  // method was called at least one time

        verify(this.maxoffilesperprocess, times(1)).acquire();  // method acquire was called once before the exception was thrown
        verify(this.progressMonitor, never()).setHeaderTitle("Firmando " + this.testDocumentPDF1.getName());  // method was never called
        assertTrue(this.signerScheduler.getFiles().isEmpty());  // list is empty since the document was removed before the exception was thrown
    }

    @Test
    void testRunWithNonEmptyListAndNullCard() throws InterruptedException{
        when(this.gui.getPin()).thenReturn(null);  // set the getPin method return value to make sure it is null as required

        this.signerScheduler.addDocument(this.testDocumentPDF1);
        this.signerScheduler.addDocument(this.testDocumentDOCX);

        assertFalse(this.signerScheduler.getStop());
        assertFalse(this.signerScheduler.getFiles().isEmpty());

        this.signerScheduler.start();
        Thread.sleep(800);   // let it run for 800ms before it is interrupted, so it can run the code as expected

        assertFalse(this.signerScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setTitle(String.format("Proceso de firmado de %d documento(s)", signerScheduler.getFiles().size()));  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setHeaderTitle("Firmando documento");  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setVisible(true);  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setProgressStatus(0);  // method was called at least one time

        verifyNoInteractions(this.maxoffilesperprocess);  // method acquire was not called since card is null
        verify(this.progressMonitor, never()).setHeaderTitle("Firmando " + this.testDocumentPDF1.getName());  // method was not called since card is null
        verify(this.progressMonitor, never()).setHeaderTitle("Firmando " + this.testDocumentDOCX.getName());  // method was not called since card is null

        verify(this.progressMonitor, atLeastOnce()).setHeaderTitle("Proceso cancelado");  // method was called at least one time
        verify(this.progressMonitor, atLeastOnce()).setVisible(false);  // method was called at least one time
        assertTrue(this.signerScheduler.getFiles().isEmpty());  // list was cleared because the card is null
    }

    @Test
    void testRunCheckSemaphoresUse() throws InterruptedException{
        when(this.gui.getPin()).thenReturn(this.cardSignInfo);  // set the getPin method return value, so no need to have an actual card connected

        this.signerScheduler.addDocument(this.testDocumentODT);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.signerScheduler.addDocument(this.testDocumentPDF1);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.signerScheduler.addDocument(this.testDocumentPDF2);
        Thread.sleep(500); // give it sometime between adding docs to let it adjust
        this.signerScheduler.addDocument(this.testDocumentDOCX);

        this.signerScheduler.start();
        Thread.sleep(1000);   // let it run for 1s before it is interrupted, so it can run the code as expected

        int number0fDocs = 4;
        assertFalse(this.signerScheduler.getStop());  // the scheduler was not interrupted
        verify(this.waitforfiles, atLeastOnce()).acquire();  // method acquire was called at least one time
        verify(this.maxoffilesperprocess, times(number0fDocs)).acquire();  // method acquire was called once per document
        verify(this.waitforfiles, times(number0fDocs)).release();  // method release was called once per document
        verify(this.maxoffilesperprocess, times(number0fDocs)).release();  // method release was called once per document when done
        assertTrue(this.signerScheduler.getFiles().isEmpty());  // list is empty after the documents are processed

        verify(this.progressMonitor, times(1)).setHeaderTitle("Firmando " + this.testDocumentODT.getName());  // method was called when the document was processed
        verify(this.progressMonitor, times(2)).setHeaderTitle("Firmando " + this.testDocumentPDF1.getName());  // method was called when the document was processed - 2 times since there are 2 PDFs with the same file
        verify(this.progressMonitor, times(1)).setHeaderTitle("Firmando " + this.testDocumentDOCX.getName());  // method was called when the document was processed

        // check semaphores have the right status at the end
        assertFalse(this.maxoffilesperprocess.hasQueuedThreads());  // no more pending files to process
        assertTrue(this.waitforfiles.hasQueuedThreads());  // it is waiting for new file
    }

    @Test
    void testRunWithInterruptedException() throws InterruptedException {
        assertFalse(this.signerScheduler.getStop());
        assertTrue(this.signerScheduler.getFiles().isEmpty());

        InterruptedException e = spy(new InterruptedException());
        doThrow(e).when(this.waitforfiles).acquire();

        this.signerScheduler.run();

        assertTrue(this.signerScheduler.getStop()); // there was an exception so stop variable was changed
        verify(e).printStackTrace();  //  the printStackTrace method for the exception was called
    }

    // ------ nextStep method tests ------

    @Test
    void testNextStep(){
        String message = "This is a test";
        this.signerScheduler.nextStep(message);

        verify(this.progressMonitor, times(1)).setProgressStatus(5);
        verify(this.progressMonitor, times(1)).setNote(message + " 5%.\n");
    }

    @Test
    void testNextStepWithMsgNull(){
        this.signerScheduler.nextStep(null);

        verify(this.progressMonitor, times(1)).setProgressStatus(5);
        verify(this.progressMonitor, times(1)).setNote(" 5%.\n");
    }

    @Test
    void testNextStepWithProgressStatusGreaterThan100(){
        this.signerScheduler.setProgressStatus(110);

        String message = "This is a test";
        this.signerScheduler.nextStep(message);

        assertEquals(99, this.signerScheduler.getProgressStatus());
        verify(this.progressMonitor, times(1)).setProgressStatus(99);
        verify(this.progressMonitor, times(1)).setNote(message + " 99%.\n");
    }

    @Test
    void testNextStepWithInterruptedException() throws InterruptedException {
        InterruptedException e = spy(new InterruptedException());
        SignerScheduler signerScheduler1 = spy(new SignerScheduler(this.gui, this.progressMonitor));

        doThrow(e).when(signerScheduler1).sleep(anyInt());

        String message = "This is a test";
        signerScheduler1.nextStep(message);

        verify(e).printStackTrace();  //  the printStackTrace method for the exception was called
        assertFalse(this.signerSchedulerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.signerSchedulerLog.getEvents().get(0);
        assertTrue(logEntry.getMessage().contains("Interrupción al correr el estado del progreso con múltiples ficheros"));
        assertInstanceOf(InterruptedException.class, logEntry.getThrowable());  // the right type of exception happened
    }

    // ------ propertyChange method tests ------

    @Test
    void testPropertyChangeWithoutProgressEvent(){
        this.signerScheduler.propertyChange(new PropertyChangeEvent(this.progressMonitor, "something_else", 5, 10));

        verifyNoInteractions(this.progressMonitor);
        verifyNoInteractions(this.maxoffilesperprocess);
        assertNull(this.signerScheduler.getTask());
        assertFalse(this.progressMonitor.isCanceled());
        assertTrue(this.signerSchedulerLog.getEvents().isEmpty());  // nothing was logged
    }

    @Test
    void testPropertyChangeWithProgressEventNotCancelledOrDone() {
        this.signerScheduler.setTask(this.task);

        this.signerScheduler.propertyChange(new PropertyChangeEvent(this.progressMonitor, "progress", 5, 10));

        // nothing was executed since the property name is something else but progress
        verify(this.progressMonitor, times(1)).setProgressStatus(10);
        verify(this.progressMonitor, times(1)).setNote("Completando... 10%.\n");
        assertFalse(this.signerScheduler.getTask().isDone());
        assertFalse(this.progressMonitor.isCanceled());
        assertTrue(this.signerSchedulerLog.getEvents().isEmpty());  // nothing was logged
    }

    @Test
    void testPropertyChangeWithProgressEventAndProgressMonitorCancelled(){
        this.signerScheduler.setTask(this.task);

        when(this.progressMonitor.isCanceled()).thenReturn(true);
        when(this.task.isDone()).thenReturn(false);

        this.signerScheduler.propertyChange(new PropertyChangeEvent(this.progressMonitor, "progress", 10, 20));

        assertFalse(this.signerScheduler.getTask().isDone());
        assertTrue(this.signerScheduler.getProgressMonitor().isCanceled());
        verify(this.progressMonitor, times(1)).setProgressStatus(20);
        verify(this.progressMonitor, times(1)).setNote("Completando... 20%.\n");
        verify(this.maxoffilesperprocess, times(1)).release();  // the method was called once
        assertTrue(this.signerScheduler.getTask().isCancelled());  // the task was cancelled
        assertFalse(this.signerSchedulerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.signerSchedulerLog.getEvents().get(0);
        assertTrue(logEntry.getMessage().contains("Tarea cancelada"));
    }

    @Test
    void testPropertyChangeWithProgressEventAndTaskDone(){
        this.signerScheduler.setTask(this.task);

        when(this.progressMonitor.isCanceled()).thenReturn(false);
        when(this.task.isDone()).thenReturn(true);

        this.signerScheduler.propertyChange(new PropertyChangeEvent(this.progressMonitor, "progress", 20, 30));

        assertTrue(this.signerScheduler.getTask().isDone());
        assertFalse(this.signerScheduler.getProgressMonitor().isCanceled());
        verify(this.progressMonitor, times(1)).setProgressStatus(30);
        verify(this.progressMonitor, times(1)).setNote("Completando... 30%.\n");
        verifyNoInteractions(this.maxoffilesperprocess);  // the semaphore was not used at all
        assertFalse(this.signerScheduler.getTask().isCancelled());  // the task was not cancelled
        assertFalse(this.signerSchedulerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.signerSchedulerLog.getEvents().get(0);
        assertTrue(logEntry.getMessage().contains("Tarea completada"));
    }

    // ------ setProgress method tests ------

    @Test
    void testSetProgress(){
        int progress = 18;
        this.signerScheduler.setProgress(progress);

        verify(this.progressMonitor, times(1)).setProgressStatus(progress);
        assertEquals(progress, this.signerScheduler.getProgressStatus());
    }

    // ------ done method tests ------

    @Test
    void testDoneAllPermitsAvailable() throws InterruptedException{
        this.maxoffilesperprocess.acquire();  // it will be released on done

        this.signerScheduler.done();

        assertEquals(MAX_FILES_PROCESS, this.maxoffilesperprocess.availablePermits());  // all permits are released
        verify(this.maxoffilesperprocess, times(1)).release();  // method release was called once
        verify(this.progressMonitor, times(1)).setVisible(false);  // method set visible was called
        verify(this.gui, times(1)).signAllDone();  // method signAllDone was called
    }

    @Test
    void testDoneNotAllPermitsAvailable() throws InterruptedException {
        this.maxoffilesperprocess.acquire();  // one of these will be released on done
        this.maxoffilesperprocess.acquire();

        this.signerScheduler.done();

        assertNotEquals(MAX_FILES_PROCESS, this.maxoffilesperprocess.availablePermits());  // one permit is still not released
        verify(this.maxoffilesperprocess, times(1)).release();  // method release was called once
        verifyNoInteractions(this.progressMonitor);  // method setVisible was not called
        verifyNoInteractions(this.gui);  // method signAllDone was not called
    }

    // ------ addDocument method tests ------

    @Test
    void testAddDocument(){
        assertTrue(this.signerScheduler.getFiles().isEmpty());

        this.signerScheduler.addDocument(this.testDocumentPDF1);

        assertFalse(this.signerScheduler.getFiles().isEmpty());
        assertEquals(this.testDocumentPDF1, this.signerScheduler.getFiles().get(0));
        verify(this.waitforfiles, times(1)).release();  // method release was called once after the doc is added
    }


    // ------ addDocuments method tests ------

    @Test
    void testAddDocuments(){
        assertTrue(this.signerScheduler.getFiles().isEmpty());

        ArrayList<Document> docsToAdd= new ArrayList<>(List.of(this.testDocumentPDF1, this.testDocumentDOCX, this.testDocumentODT));
        this.signerScheduler.addDocuments(docsToAdd);

        assertFalse(this.signerScheduler.getFiles().isEmpty());
        assertEquals(docsToAdd, this.signerScheduler.getFiles());
        verify(this.waitforfiles, times(1)).release();  // method release was called once after all the docs are added
    }
}
