/* Firmador is a program to sign documents using AdES standards.

Copyright (C) Firmador authors.

This file is part of Firmador.

Firmador is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Firmador is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Firmador.  If not, see <http://www.gnu.org/licenses/>.  */

package cr.libre.firmador;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class TestSettingsManager {

    private final String osName = System.getProperty("os.name").toLowerCase();
    private final String defaultHomePath = this.osName.contains("windows") ? System.getenv("APPDATA") : System.getProperty("user.home");
    private final String testHomePath = FileSystems.getDefault().getPath(defaultHomePath, "unit-tests").toString();
    private final String homePropertyName = this.osName.contains("windows") ? "APPDATA" : "user.home";
    private final String configDirPathEnding = ".config/firmadorlibre";
    private final SettingsManager settingsManager = SettingsManager.getInstance();

    private void deleteDir(String path){
        try {
            FileUtils.deleteDirectory(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setTestHomePathAndResetSettingsManagerPath(){
        System.setProperty(this.homePropertyName, this.testHomePath);
        this.settingsManager.setPath((Path) null);
    }

    @AfterEach
    public void deleteTestHomePathDirAndResetHomePath() {
        System.setProperty(this.homePropertyName, this.defaultHomePath);
        deleteDir(this.testHomePath);
    }

    // ------ getConfigDir method tests ------

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testGetConfigDirInWindows() {
        // TODO - implement this and make sure it works on Windows
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void testGetConfigDirNotInWindows(){
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testGetConfigDirWhenPathIsNotDirInWindows(){
        // TODO - implement this and make sure it works on Windows
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void testGetConfigDirWhenPathIsNotDirNotInWindows(){
        String nonDirectoryTestPath = FileSystems.getDefault().getPath(this.testHomePath, "non-existent").toString();
        assertFalse(Files.exists(FileSystems.getDefault().getPath(nonDirectoryTestPath)));  // the directory does not exist yet

        System.setProperty("user.home",  nonDirectoryTestPath);
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(nonDirectoryTestPath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));  // the directory was created correctly
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    void testGetConfigDirThrowsIOException(){
        AtomicReference<Path> resultPath = new AtomicReference<>();
        IOException thrown = assertThrows(IOException.class, () -> {
            System.setProperty("user.home",  "/root/");  // so it will throw an exception because permissions
            resultPath.set(this.settingsManager.getConfigDir());
        });

        assertEquals("java.nio.file.AccessDeniedException: /root/.config", thrown.toString());
        assertNull(resultPath.get());  // it is null since it died before returning
    }

    // ------ getPathConfigFile method tests ------

    @Test
    void testGetPathConfigFileWhenPathIsNull(){
        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        String fileName = "testGetPathConfigFileWhenPathIsNull.config";
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getPathConfigFile(fileName)));

        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, fileName);
        assertEquals(expectedPath, resultPath.get());
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    void testGetPathConfigFileWhenPathIsNotNull(){
        String fileName = "testGetPathConfigFileWhenPathIsNotNull.config";
        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, fileName);  // not the default calculated path
        this.settingsManager.setPath(expectedPath);
        assertNotNull(this.settingsManager.getPath());  // the path is not null before calling the method

        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getPathConfigFile(fileName)));

        assertEquals(expectedPath, resultPath.get());
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    void testGetPathConfigFileThrowsIOException(){
        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        String fileName = "testGetPathConfigFileThrowsIOException.config";
        AtomicReference<Path> resultPath = new AtomicReference<>();
        IOException thrown = assertThrows(IOException.class, () -> {
            System.setProperty("user.home",  "/root/");  // so it will throw an exception because permissions
            resultPath.set(this.settingsManager.getPathConfigFile(fileName));
        });

        assertEquals("java.nio.file.AccessDeniedException: /root/.config", thrown.toString());
        assertNull(resultPath.get());  // it is null since it died before returning
    }

    // ------ getConfigFile (with name param) method tests ------

    @Test
    void testGetConfigFileWithNameParam(){
        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        String fileName = "testGetConfigFileWithNameParam.config";
        AtomicReference<String> resultString = new AtomicReference<>();
        assertDoesNotThrow(() -> resultString.set(this.settingsManager.getConfigFile(fileName)));

        String expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, fileName).toString();
        assertEquals(expectedPath, resultString.get());
        assertEquals(expectedPath, this.settingsManager.getPath().toString());  // the path variable was also set
    }

    @Test
    void testGetConfigFileWithNameParamThrowsIOException(){
        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        String fileName = "testGetConfigFileWithNameParamThrowsIOException.config";
        AtomicReference<String> resultPath = new AtomicReference<>();
        IOException thrown = assertThrows(IOException.class, () -> {
            System.setProperty("user.home",  "/root/");  // so it will throw an exception because permissions
            resultPath.set(this.settingsManager.getConfigFile(fileName));
        });

        assertEquals("java.nio.file.AccessDeniedException: /root/.config", thrown.toString());
        assertNull(resultPath.get());  // it is null since it died before returning
    }

    // ------ getPath method tests ------

    @Test
    void testGetPath(){
        assertNull(this.settingsManager.getPath());  // path is null at the start

        // set something else to the path to test the get
        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, "testGetPath");
        this.settingsManager.setPath(expectedPath);

        // new path is set and returned correctly
        assertNotNull(this.settingsManager.getPath());
        assertInstanceOf(Path.class, this.settingsManager.getPath());
        assertEquals(expectedPath, this.settingsManager.getPath());
    }

    // ------ setPath methods tests ------

    @Test
    void testSetPathWithPathParam(){
        // set a Path to the Path variable
        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, "testSetPathWithPathParam");
        assertInstanceOf(Path.class, expectedPath);
        this.settingsManager.setPath(expectedPath);

        // new path is set correctly
        assertNotNull(this.settingsManager.getPath());
        assertInstanceOf(Path.class, this.settingsManager.getPath());  // Path is still a Path variable no matter the param
        assertEquals(expectedPath, this.settingsManager.getPath());
    }

    @Test
    void testSetPathWithStringParam(){
        // set a String to the Path variable
        String expectedPath = FileSystems.getDefault().getPath(this.testHomePath, "testSetPathWithStringParam").toString();
        assertInstanceOf(String.class, expectedPath);
        this.settingsManager.setPath(expectedPath);

        // new path is set correctly
        assertNotNull(this.settingsManager.getPath());
        assertInstanceOf(Path.class, this.settingsManager.getPath());  // Path is still a Path variable no matter the param
        assertEquals(FileSystems.getDefault().getPath(expectedPath), this.settingsManager.getPath());
    }

    // ------ getInstance method tests ------

    @Test
    void testGetInstance(){
        SettingsManager resultInstance1 = SettingsManager.getInstance();
        assertNotNull(resultInstance1);
        assertInstanceOf(SettingsManager.class, resultInstance1);

        SettingsManager resultInstance2 = SettingsManager.getInstance();
        assertNotNull(resultInstance2);
        assertInstanceOf(SettingsManager.class, resultInstance2);

        // both instances are the same - it is a Singleton
        assertEquals(resultInstance1, resultInstance2);
    }

    // ------ getProperty method tests ------

    @Test
    void testGetProperty(){
        // get a non-existent property - default is empty string
        assertEquals("", this.settingsManager.getProperty("non-existent"));

        // get an existing property - set it first
        String expectedPropertyValue = "this is a get test";
        this.settingsManager.setProperty("testGetProperty", expectedPropertyValue);

        assertEquals(expectedPropertyValue, this.settingsManager.getProperty("testGetProperty"));
    }

    // ------ setProperty method tests ------

    @Test
    void testSetProperty(){
        String expectedPropertyValue = "this is a set test";
        this.settingsManager.setProperty("testSetProperty", expectedPropertyValue);

        assertEquals(expectedPropertyValue, this.settingsManager.getProperty("testSetProperty"));
    }

    // ------ getConfigFile (without params) method tests ------

    @Test
    void testGetConfigFileWithoutParamsWhenPathIsNull(){}

    @Test
    void testGetConfigFileWithoutParamsWhenPathIsNotNull(){}

    @Test
    void testGetConfigFileWithoutParamsThrowsIOException(){}

    // ------ loadConfig method tests ------

    @Test
    void testLoadConfigWhenConfigFileExists(){}

    @Test
    void testLoadConfigWhenConfigFileDoesNotExist(){}

    @Test
    void testLoadConfigWithIOException(){}

    // ------ saveConfig method tests ------

    @Test
    void testSaveConfig(){}

    @Test
    void testSaveConfigWithIOException(){}

    @Test
    void testSaveConfigFinallyClause(){}

    @Test
    void testSaveConfigFinallyClauseWithIOException(){}

    // ------ getListFromString method tests ------

    @Test
    void testGetListFromStringWhenReturnsDefaultData(){}

    @Test
    void testGetListFromStringWithReturnsPlugins(){}

    // ------ getSettings method tests ------

    @Test
    void testGetSettingsWhenLoaded(){}

    @Test
    void testGetSettingsWhenNotLoaded(){}

    // ------ getFloatFromString method tests ------

    @Test
    void testGetFloatFromString(){}

    @Test
    void testGetFloatFromStringWithException(){}

    // ------ getListRepr method tests ------

    @Test
    void testGetListRepr(){
        // test both, list with data and empty list
    }

    // ------ setSettings method tests ------

    @Test
    void testSetSettings(){}

    @Test
    void testSetSettingsWithExtrapkcs11LibProperty(){
        // test 3 scenarios, set, not set and remove
    }

    @Test
    void testSetSettingsWithImageProperty(){
        // test 3 scenarios, set, not set and remove
    }

    @Test
    void testSetSettingsWithSaveConfig(){}

    @Test
    void testGetAndCreateSettings(){}

    // ------ getAndCreateSettings method tests ------

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNull(){
        // test 3 scenarios, path is null, path is not null && path does not exist, and path is not null && path exists
    }

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNotNull(){}

    @Test
    void testGetAndCreateSettingsWithException(){}

}
