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

import io.github.netmikey.logunit.api.LogCapturer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import utils.TestUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestSettingsManager {

    private final String osName = System.getProperty("os.name").toLowerCase();
    private final String defaultHomePath = this.osName.contains("windows") ? System.getenv("APPDATA") : System.getProperty("user.home");
    private final String testHomePath = FileSystems.getDefault().getPath(defaultHomePath, "unit-tests").toString();
    private final String homePropertyName = this.osName.contains("windows") ? "APPDATA" : "user.home";
    private final String configDirPathEnding = this.osName.contains("windows") ? "firmadorlibre" : ".config/firmadorlibre";
    private final SettingsManager settingsManager = SettingsManager.getInstance();
    private final String pathWithNoAccess =  FileSystems.getDefault().getPath(testHomePath, "no-access").toString();

    @RegisterExtension
    LogCapturer settingsManagerLog = LogCapturer.create().captureForType(SettingsManager.class, Level.ERROR);

    private void setSettingsForTesting(){
        // set some settings to values different from default to test some things
        this.settingsManager.setProperty("withoutvisiblesign", "true");
        this.settingsManager.setProperty("overwritesourcefile", "true");
        this.settingsManager.setProperty("reason", "this is a test reason");
        this.settingsManager.setProperty("place", "this is a test place");
        this.settingsManager.setProperty("contact", "this is a test contact");
        this.settingsManager.setProperty("font", Font.SERIF);
        this.settingsManager.setProperty("plugins", "Test1|Test2|Test3");

        this.settingsManager.saveConfig();  // save the config to a file
    }

    private  HashMap<String, Object> getConfigFieldValuesForTesting() {
        return new HashMap<String, Object>() {
            {
                put("withoutVisibleSign", true);
                put("overwriteSourceFile", true);
                put("reason", "Test reason");
                put("place", "Test place");
                put("contact", "Test contact");
                put("dateFormat", "dd/MM/yyyy");
                put("defaultSignMessage", "Test default sign message");
                put("pageNumber", 2);
                put("signWidth", 11);
                put("signHeight", 11);
                put("fontSize", 11);
                put("font", Font.MONOSPACED);
                put("fontColor", "#ffffff");
                put("backgroundColor", "white");
                put("fontAlignment", "LEFT");
                put("portNumber", 1111);
                put("showLogs", true);
                put("activePlugins", new ArrayList<String>(List.of("cr.libre.firmador.plugins.DummyPlugin")));
            }
        };
    }

    private Settings createNewConfigForTesting(HashMap<String, Object> fieldValues){
        // set some settings different from default for testing
        Settings newConfig = new Settings();
        fieldValues.forEach((name, value) -> {
            try {
                newConfig.getClass().getDeclaredField(name).set(newConfig, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return newConfig;
    }

    private void setHomePath(String value){
        if(this.osName.contains("windows")){
            TestUtils.getModifiableEnvironment().put(this.homePropertyName, value);
        }else {
            System.setProperty(this.homePropertyName, value);
        }
    }

    @BeforeEach
    public void setTestHomePathAndResetSettingsManagerPathAndProps(){
        this.setHomePath(this.testHomePath);
        this.settingsManager.setPath((Path) null);
        this.settingsManager.setProps(new Properties());
    }

    @AfterEach
    public void deleteTestHomePathDirAndResetHomePath() {
        this.setHomePath(this.defaultHomePath);
        TestUtils.deleteDir(this.testHomePath);
    }

    // ------ getConfigDir method tests ------

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testGetConfigDirInWindows() {
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void testGetConfigDirNotInWindows(){
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));  // the directory was created correctly
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testGetConfigDirWhenPathIsNotDirInWindows(){
        String nonDirectoryTestPath = FileSystems.getDefault().getPath(this.testHomePath, "non-existent").toString();
        assertFalse(Files.exists(FileSystems.getDefault().getPath(nonDirectoryTestPath)));  // the directory does not exist yet

        this.setHomePath(nonDirectoryTestPath);
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(nonDirectoryTestPath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));  // the directory was created correctly
        try {
            assertTrue((boolean)Files.getAttribute(resultPath.get(), "dos:hidden"));  // dir is hidden in Windows
        }catch (IOException e){
            fail("Unable to check if the config dir is hidden", e);
        }
    }

    @Test
    @EnabledOnOs({OS.MAC, OS.LINUX})
    void testGetConfigDirWhenPathIsNotDirNotInWindows(){
        String nonDirectoryTestPath = FileSystems.getDefault().getPath(this.testHomePath, "non-existent").toString();
        assertFalse(Files.exists(FileSystems.getDefault().getPath(nonDirectoryTestPath)));  // the directory does not exist yet

        this.setHomePath(nonDirectoryTestPath);
        AtomicReference<Path> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> resultPath.set(this.settingsManager.getConfigDir()));

        Path expectedPath = FileSystems.getDefault().getPath(nonDirectoryTestPath, this.configDirPathEnding);
        assertEquals(expectedPath, resultPath.get());
        assertTrue(Files.isDirectory(resultPath.get()));  // the directory was created correctly
    }

    @Test
    @DisabledIfSystemProperty(named = "os.name", matches = "Windows Server.*")
    void testGetConfigDirThrowsIOException(){
        // TODO - Fix this test so it runs in Windows in the CI/CD pipeline in Gitlab

        AtomicReference<Path> resultPath = new AtomicReference<>();
        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        IOException exceptionThrown = assertThrows(IOException.class, () -> {
            this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception because of permissions
            resultPath.set(this.settingsManager.getConfigDir());
        });
        assertTrue(exceptionThrown.toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess));
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
    @DisabledIfSystemProperty(named = "os.name", matches = "Windows Server.*")
    void testGetPathConfigFileThrowsIOException(){
        // TODO - Fix this test so it runs in Windows in the CI/CD pipeline in Gitlab

        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        String fileName = "testGetPathConfigFileThrowsIOException.config";
        AtomicReference<Path> resultPath = new AtomicReference<>();
        IOException exceptionThrown = assertThrows(IOException.class, () -> {
            this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception because of permissions
            resultPath.set(this.settingsManager.getPathConfigFile(fileName));
        });

        assertTrue(exceptionThrown.toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess));
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
    @DisabledIfSystemProperty(named = "os.name", matches = "Windows Server.*")
    void testGetConfigFileWithNameParamThrowsIOException(){
        // TODO - Fix this test so it runs in Windows in the CI/CD pipeline in Gitlab

        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        String fileName = "testGetConfigFileWithNameParamThrowsIOException.config";
        AtomicReference<String> resultPath = new AtomicReference<>();
        IOException exceptionThrown = assertThrows(IOException.class, () -> {
            this.setHomePath(this.pathWithNoAccess); // so it will throw an exception because of permissions
            resultPath.set(this.settingsManager.getConfigFile(fileName));
        });

        assertTrue(exceptionThrown.toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess));
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
    void testGetConfigFileWithoutParamsWhenPathIsNull(){
        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        AtomicReference<String> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getConfigFilePrivateMethod = SettingsManager.class.getDeclaredMethod("getConfigFile");  // do this since it is a private method
            getConfigFilePrivateMethod.setAccessible(true);
            resultPath.set((String) getConfigFilePrivateMethod.invoke(this.settingsManager));
        });

        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties");  // this is the name by defect when path is null
        assertEquals(expectedPath.toString(), resultPath.get());
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    void testGetConfigFileWithoutParamsWhenPathIsNotNull(){
        String fileName = "testGetConfigFileWithoutParamsWhenPathIsNotNull.config";  // name different from the default value
        Path expectedPath = FileSystems.getDefault().getPath(this.testHomePath, fileName);  // not the default calculated path
        this.settingsManager.setPath(expectedPath);
        assertNotNull(this.settingsManager.getPath());  // the path is not null before calling the method

        AtomicReference<String> resultPath = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getConfigFilePrivateMethod = SettingsManager.class.getDeclaredMethod("getConfigFile");  // do this since it is a private method
            getConfigFilePrivateMethod.setAccessible(true);
            resultPath.set((String) getConfigFilePrivateMethod.invoke(this.settingsManager));
        });

        assertEquals(expectedPath.toString(), resultPath.get());
        assertEquals(expectedPath, this.settingsManager.getPath());  // the path variable was also set
    }

    @Test
    @DisabledIfSystemProperty(named = "os.name", matches = "Windows Server.*")
    void testGetConfigFileWithoutParamsThrowsIOException(){
        // TODO - Fix this test so it runs in Windows in the CI/CD pipeline in Gitlab

        assertNull(this.settingsManager.getPath());  // the path is null before calling the method

        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        AtomicReference<String> resultPath = new AtomicReference<>();
        // it is expecting this kind of exception since it uses a special way to call the private method
        InvocationTargetException exceptionThrown = assertThrows(InvocationTargetException.class, () -> {
            this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception because of permissions
            Method getConfigFilePrivateMethod = SettingsManager.class.getDeclaredMethod("getConfigFile");  // do this since it is a private method
            getConfigFilePrivateMethod.setAccessible(true);
            resultPath.set((String) getConfigFilePrivateMethod.invoke(this.settingsManager));
        });

        assertInstanceOf(IOException.class, exceptionThrown.getTargetException());  // the method threw the right type of exception at the end
        assertTrue(exceptionThrown.getTargetException().toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess));
        assertNull(resultPath.get());  // it is null since it died before returning
    }

    // ------ loadConfig method tests ------

    @Test
    void testLoadConfigWhenConfigFileExists(){
        this.settingsManager.setSettings(this.settingsManager.getSettings(), true); // create the file so the test can be performed correctly
        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertTrue(configFile.exists());  // the file actually exists
        this.settingsManager.setProps(new Properties());  // clean properties to check later that they were loaded correctly
        assertTrue(this.settingsManager.getProps().isEmpty());  // props is empty before the load

        AtomicReference<Boolean> resultBoolean = new AtomicReference<>();
        assertDoesNotThrow(() -> resultBoolean.set(this.settingsManager.loadConfig()));

        assertTrue(resultBoolean.get());  // it returns that the config was loaded
        assertFalse(this.settingsManager.getProps().isEmpty());  // props were loaded
    }

    @Test
    void testLoadConfigWhenConfigFileDoesNotExist(){
        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertFalse(configFile.exists());  // the file does not exist
        assertTrue(this.settingsManager.getProps().isEmpty());  // props is empty before the load

        AtomicReference<Boolean> resultBoolean = new AtomicReference<>();
        assertDoesNotThrow(() -> resultBoolean.set(this.settingsManager.loadConfig()));

        assertFalse(resultBoolean.get());  // it returns that the config was not loaded
        assertTrue(this.settingsManager.getProps().isEmpty());  // props continues to be empty, nothing was loaded

    }

    @Test
    @DisabledIfSystemProperty(named = "os.name", matches = "Windows Server.*")
    void testLoadConfigWithIOException(){
        // TODO - Fix this test so it runs in Windows in the CI/CD pipeline in Gitlab

        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertFalse(configFile.exists());  // the file does not exist
        assertTrue(this.settingsManager.getProps().isEmpty());  // props is empty before the load

        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        AtomicReference<Boolean> resultBoolean = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception because of permissions
            resultBoolean.set(this.settingsManager.loadConfig());
        });

        assertFalse(this.settingsManagerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.settingsManagerLog.getEvents().get(0);
        assertTrue(logEntry.getThrowable().toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess));
        assertInstanceOf(IOException.class, logEntry.getThrowable());  // the right type of exception happened
        assertFalse(resultBoolean.get());  // it is false since it is the default when an exception happens
        assertTrue(this.settingsManager.getProps().isEmpty());  // props continues to be empty, nothing was loaded
    }

    // ------ saveConfig method tests ------

    @Test
    void testSaveConfig(){
        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertFalse(configFile.exists());  // the file does not exist yet

        this.settingsManager.saveConfig();

        assertTrue(configFile.exists());  // the file was created correctly
    }

    @Test
    void testSaveConfigWithIOException(){
        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertFalse(configFile.exists());  // the file does not exist yet

        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception because of permissions
        this.settingsManager.saveConfig();

        assertFalse(this.settingsManagerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.settingsManagerLog.getEvents().get(0);
        assertTrue(logEntry.getThrowable().toString().contains("java.nio.file.AccessDeniedException: " + this.pathWithNoAccess)
            || logEntry.getThrowable().toString().contains("Access is denied"));
        assertInstanceOf(IOException.class, logEntry.getThrowable());  // the right type of exception happened
        assertFalse(configFile.exists());  // the file was not created because of the exception
    }

    // ------ getListFromString method tests ------

    @Test
    void testGetListFromStringWhenReturnsDefaultData(){
        List<String> defaultData = new ArrayList<>(List.of("Test1", "Test2"));
        AtomicReference<List<String>> resultList = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getListFromStringPrivateMethod = SettingsManager.class.getDeclaredMethod("getListFromString", String.class, List.class);  // do this since it is a private method
            getListFromStringPrivateMethod.setAccessible(true);
            resultList.set((List<String>) getListFromStringPrivateMethod.invoke(this.settingsManager, "", defaultData));
        });

        assertFalse(resultList.get().isEmpty());
        assertEquals(defaultData, resultList.get());  // it returns what was sent in defaultData param
    }

    @Test
    void testGetListFromStringWhenReturnsData(){
        String data = "Test1|Test2|Test3";
        AtomicReference<List<String>> resultList = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getListFromStringPrivateMethod = SettingsManager.class.getDeclaredMethod("getListFromString", String.class, List.class);  // do this since it is a private method
            getListFromStringPrivateMethod.setAccessible(true);
            resultList.set((List<String>) getListFromStringPrivateMethod.invoke(this.settingsManager, data, null));
        });

        List<String> expectedResult = new ArrayList<>(List.of("Test1", "Test2", "Test3"));
        assertFalse(resultList.get().isEmpty());
        assertEquals(expectedResult, resultList.get());  // it returns a list extracted from data param
    }

    @Test
    void testGetListFromStringWhenBothParamsAreEmpty(){
        String data = "";
        List<String> defaultData = new ArrayList<>();
        AtomicReference<List<String>> resultList = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getListFromStringPrivateMethod = SettingsManager.class.getDeclaredMethod("getListFromString", String.class, List.class);  // do this since it is a private method
            getListFromStringPrivateMethod.setAccessible(true);
            resultList.set((List<String>) getListFromStringPrivateMethod.invoke(this.settingsManager, data, defaultData));
        });

        assertTrue(resultList.get().isEmpty());  // it returns an empty list (created from the data param)
    }

    // ------ getSettings method tests ------

    @Test
    void testGetSettingsWhenLoaded(){
        this.setSettingsForTesting();  // make some properties different from default, so the load can be tested
        this.settingsManager.setProps(new Properties());  // clean properties to check later if they were loaded correctly from the file

        Settings resultSettings = this.settingsManager.getSettings();

        // the specially set settings were returned correctly
        assertTrue(resultSettings.withoutVisibleSign);
        assertTrue(resultSettings.overwriteSourceFile);
        assertEquals("this is a test reason", resultSettings.reason);
        assertEquals("this is a test place", resultSettings.place);
        assertEquals("this is a test contact", resultSettings.contact);
        assertEquals(Font.SERIF, resultSettings.font);
        assertEquals(new ArrayList<>(List.of("Test1", "Test2", "Test3")), resultSettings.activePlugins);
    }

    @Test
    void testGetSettingsWhenNotLoaded(){
        this.setSettingsForTesting();  // make some properties different from default, so the load can be tested
        this.settingsManager.setProps(new Properties());  // clean properties to check later if they were loaded correctly

        this.settingsManager.setPath((Path) null);  // so it is created again with the new value required for it to fail
        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);
        this.setHomePath(this.pathWithNoAccess);  // so it will throw an exception, so config doesn't get loaded
        Settings resultSettings = this.settingsManager.getSettings();

        // the values returned are the default values and not the saved ones since the load failed
        assertFalse(resultSettings.withoutVisibleSign);
        assertFalse(resultSettings.overwriteSourceFile);
        assertTrue(resultSettings.reason.isEmpty());
        assertTrue(resultSettings.place.isEmpty());
        assertTrue(resultSettings.contact.isEmpty());
        assertEquals(Font.SANS_SERIF, resultSettings.font);
        assertEquals(
                new ArrayList<>(List.of("cr.libre.firmador.plugins.DummyPlugin",
                        "cr.libre.firmador.plugins.CheckUpdatePlugin", "cr.libre.firmador.plugins.InstallerPlugin")),
                resultSettings.activePlugins);
    }

    // ------ getFloatFromString method tests ------

    @Test
    void testGetFloatFromString(){
        AtomicReference<Float> resultFloat = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getFloatFromStringPrivateMethod = SettingsManager.class.getDeclaredMethod("getFloatFromString", String.class);  // do this since it is a private method
            getFloatFromStringPrivateMethod.setAccessible(true);
            resultFloat.set((Float) getFloatFromStringPrivateMethod.invoke(this.settingsManager, "12,35"));
        });

        assertEquals(Float.valueOf(12.35f), resultFloat.get());
    }

    @Test
    void testGetFloatFromStringWithException(){
        AtomicReference<Float> resultFloat = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getFloatFromStringPrivateMethod = SettingsManager.class.getDeclaredMethod("getFloatFromString", String.class);  // do this since it is a private method
            getFloatFromStringPrivateMethod.setAccessible(true);
            resultFloat.set((Float) getFloatFromStringPrivateMethod.invoke(this.settingsManager, "invalid"));
        });

        assertFalse(this.settingsManagerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.settingsManagerLog.getEvents().get(0);
        assertEquals("java.lang.NumberFormatException: For input string: \"invalid\"", logEntry.getThrowable().toString());
        assertInstanceOf(NumberFormatException.class, logEntry.getThrowable());  // the right type of exception happened
        assertEquals(Float.valueOf(1), resultFloat.get());  // it returns the default value when something fails

    }

    // ------ getListRepr method tests ------

    @Test
    void testGetListReprWhenListHasData(){
        AtomicReference<String> resultString = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getListReprPrivateMethod = SettingsManager.class.getDeclaredMethod("getListRepr", List.class);  // do this since it is a private method
            getListReprPrivateMethod.setAccessible(true);
            resultString.set((String) getListReprPrivateMethod.invoke(this.settingsManager, new ArrayList<>(List.of("Test1", "Test2", "Test3"))));
        });

        assertEquals("Test1|Test2|Test3", resultString.get());
    }

    @Test
    void testGetListReprWhenEmptyList(){
        AtomicReference<String> resultString = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            Method getListReprPrivateMethod = SettingsManager.class.getDeclaredMethod("getListRepr", List.class);  // do this since it is a private method
            getListReprPrivateMethod.setAccessible(true);
            resultString.set((String) getListReprPrivateMethod.invoke(this.settingsManager, new ArrayList<>()));
        });

        assertTrue(resultString.get().isEmpty());  // the result is an empty string
    }

    // ------ setSettings method tests ------

    @Test
    void testSetSettingsWithoutSaveConfig() {
        HashMap<String, Object> fieldValues = getConfigFieldValuesForTesting();
        Settings newConfig = createNewConfigForTesting(fieldValues);

        this.settingsManager.setSettings(newConfig, false);

        // check all the values set in the newConfig object are now set correctly in the props field
        fieldValues.forEach((key, value) -> {
            if(Objects.equals(key, "activePlugins")) key = "plugins";
            if(value instanceof ArrayList) value = String.join("|", (ArrayList) value);

            assertEquals(value.toString(), this.settingsManager.getProperty(key.toLowerCase()));
        });

        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertFalse(configFile.exists());  // the file does not exist since it was not saved
    }

    @Test
    void testSetSettingsWithSaveConfig(){
        HashMap<String, Object> fieldValues = getConfigFieldValuesForTesting();
        Settings newConfig = createNewConfigForTesting(fieldValues);

        this.settingsManager.setSettings(newConfig, true);

        // check all the values set in the newConfig object are now set correctly in the props field
        fieldValues.forEach((key, value) -> {
            if(Objects.equals(key, "activePlugins")) key = "plugins";
            if(value instanceof ArrayList) value = String.join("|", (ArrayList) value);

            assertEquals(value.toString(), this.settingsManager.getProperty(key.toLowerCase()));
        });

        File configFile = new File(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "config.properties").toString());  // default file
        assertTrue(configFile.exists());  // the file exists since it was saved
    }

    @Test
    void testSetSettingsExtrapkcs11LibProperty(){
        Settings newConfig = new Settings();

        // test not set the value
        newConfig.extraPKCS11Lib = "";
        this.settingsManager.setSettings(newConfig, false);
        assertTrue(this.settingsManager.getProperty("extrapkcs11Lib").isEmpty());  // when the prop is not present the default is empty

        // test set the value
        newConfig.extraPKCS11Lib = "testPCKS11Lib";
        this.settingsManager.setSettings(newConfig, false);
        assertEquals(newConfig.extraPKCS11Lib, this.settingsManager.getProperty("extrapkcs11Lib")); // it was set correctly

        // test remove the previous value
        newConfig.extraPKCS11Lib = null;
        this.settingsManager.setProperty("extrapkcs11Lib", "testPCKS11Lib2");
        assertEquals("testPCKS11Lib2", this.settingsManager.getProperty("extrapkcs11Lib"));  // it has something saved
        this.settingsManager.setSettings(newConfig, false);
        assertTrue(this.settingsManager.getProperty("extrapkcs11Lib").isEmpty());  // the previous value gets deleted and the prop is not present anymore
    }

    @Test
    void testSetSettingsImageProperty(){
        Settings newConfig = new Settings();

        // test not set the value
        newConfig.image = null;
        this.settingsManager.setSettings(newConfig, false);
        assertTrue(this.settingsManager.getProperty("image").isEmpty());  // when the prop is not present the default is empty

        // test set the value
        newConfig.image = "testImage";
        this.settingsManager.setSettings(newConfig, false);
        assertEquals(newConfig.image, this.settingsManager.getProperty("image")); // it was set correctly

        // test remove the previous value
        newConfig.image = null;
        this.settingsManager.setProperty("image", "testImage2");
        assertEquals("testImage2", this.settingsManager.getProperty("image"));  // it has something saved
        this.settingsManager.setSettings(newConfig, false);
        assertTrue(this.settingsManager.getProperty("image").isEmpty());  // the previous value gets deleted and the prop is not present anymore
    }

    // ------ getAndCreateSettings method tests ------

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNullAndPathIsNull(){
        this.settingsManager.nullifySettingsVariable();
        assertNull(this.settingsManager.getPath());  // check path is null

        Settings resultSettings = this.settingsManager.getAndCreateSettings();  // first time settings is null

        assertNotNull(resultSettings);  // settings were assigned and returned
    }

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNullAndPathIsNotNullAndPathDoesNotExist(){
        this.settingsManager.nullifySettingsVariable();
        this.settingsManager.setPath(FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "some-non-existent-path"));
        assertNotNull(this.settingsManager.getPath());  // path was assigned to what is required
        assertFalse(Files.exists(this.settingsManager.getPath()));  // path does not exist

        Settings resultSettings = this.settingsManager.getAndCreateSettings();

        assertFalse(this.settingsManagerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.settingsManagerLog.getEvents().get(0);
        assertEquals("Config File does not exist", logEntry.getArguments().get(0));
        assertNotNull(resultSettings);  // default settings were returned
    }

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNullAndPathIsNotNullAndPathExists(){
        this.settingsManager.nullifySettingsVariable();
        String requiredPath = FileSystems.getDefault().getPath(this.testHomePath, this.configDirPathEnding, "some-new-path", "new-name.properties").toString();
        TestUtils.createFile(requiredPath);
        this.settingsManager.setPath(requiredPath);
        assertNotNull(this.settingsManager.getPath());  // path was assigned to what is required
        assertTrue(Files.exists(this.settingsManager.getPath()));  // path exists

        Settings resultSettings = this.settingsManager.getAndCreateSettings();

        assertNotNull(resultSettings);  // settings were assigned and returned
    }

    @Test
    void testGetAndCreateSettingsWhenSettingsIsNotNull(){
        this.settingsManager.nullifySettingsVariable();

        Settings resultSettings = this.settingsManager.getAndCreateSettings();  // so settings is assigned for the first time, it is null before this
        Settings resultSettings2 = this.settingsManager.getAndCreateSettings();

        assertEquals(resultSettings, resultSettings2);  // it returned the same instance the second time, meaning settings var was not null and was returned
    }

    @Test
    void testGetAndCreateSettingsWithException(){
        TestUtils.createDirectoryWithNoAccess(this.pathWithNoAccess);

        this.settingsManager.nullifySettingsVariable();
        this.settingsManager.setPath(this.pathWithNoAccess);

        Settings resultSettings = this.settingsManager.getAndCreateSettings();

        assertFalse(this.settingsManagerLog.getEvents().isEmpty());  // something was logged
        LoggingEvent logEntry = this.settingsManagerLog.getEvents().get(0);
        assertTrue(logEntry.getThrowable().toString().contains("java.io.FileNotFoundException: " + this.pathWithNoAccess));
        assertInstanceOf(IOException.class, logEntry.getThrowable());  // the right type of exception happened
        assertNotNull(resultSettings);  // settings were assigned and returned
    }

    // ------ getProps method tests ------
    @Test
    void testGetProps(){
        Properties resultProps = this.settingsManager.getProps();

        assertNotNull(resultProps);
        assertInstanceOf(Properties.class, resultProps);  // it returns the right data type
    }

    // ------ setProps method tests ------
    @Test
    void testSetProps(){
        Properties expectedProps = new Properties();
        this.settingsManager.setProps(expectedProps);

        assertEquals(expectedProps, this.settingsManager.getProps());  // it was set correctly
    }

    // ------ nullifySettingsVariable tests ------
    @Test
    void testNullifySettingsVariable(){
        Settings resultSettings = this.settingsManager.getAndCreateSettings();  // so settings is assigned for the first time if it was null before this
        Settings resultSettings2 = this.settingsManager.getAndCreateSettings();
        assertEquals(resultSettings, resultSettings2);  // it returned the same instance the second time, meaning settings var was not null and was returned

        this.settingsManager.nullifySettingsVariable();

        Settings resultSettings3 = this.settingsManager.getAndCreateSettings();
        assertNotEquals(resultSettings, resultSettings3);  // the settings variable was null so a new instance was created
    }

}
