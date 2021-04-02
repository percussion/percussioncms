/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.preinstall;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Replace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Main {

    public static String DISTRIBUTION_DIR = "distribution";
    public static final String PERC_JAVA_HOME = "perc.java.home";
    public static final String JAVA_HOME = "java.home";
    public static final String PERCUSSION_VERSION = "percversion";
    public static final String INSTALL_TEMPDIR = "percInstallTmp_";
    public static final String PERC_ANT_JAR = "perc-ant";
    public static final String DEVELOPMENT = "DEVELOPMENT";
    public static final String ANT_INSTALL = "install.xml";
    public static final String JAVA_TEMP = "java.io.tmpdir";
    public static File tmpFolder;

    public static String developmentFlag = "false";
    public static String percVersion;
    public static volatile int currentLineNo;
    public static volatile int currentErrLineNo;
    public static volatile String debug="false";
    public static Integer processCode=0;
    public static Boolean error=false;

    public static void main(String[] args) {
        try {

            if (args.length < 1) {
                System.out.println("Must specify installation or upgrade folder");
                System.exit(0);
            }

            Path installPath = Paths.get(args[0]);

            debug = System.getProperty("DEBUG");
            if(debug == null || debug.equalsIgnoreCase("")){
                debug = "false";
            }

            String javaHome = System.getProperty(PERC_JAVA_HOME);
            if (javaHome == null || javaHome.trim().equalsIgnoreCase(""))
                javaHome = System.getProperty(JAVA_HOME);

            String javabin = "";

            if (System.getProperty("file.separator").equals("/")) {
                javabin = javaHome + "/bin/java";
            } else {
                javabin = javaHome + "/bin/java.exe";
            }

            percVersion = System.getProperty(PERCUSSION_VERSION);
            if (percVersion == null)
                percVersion = "";

            developmentFlag = System.getProperty(DEVELOPMENT);
            if (developmentFlag == null || DEVELOPMENT.trim().equalsIgnoreCase(""))
                developmentFlag = "false";

            System.out.println("perc.java.home=" + javaHome);
            System.out.println("java.bin=" + javabin);
            System.out.println("percversion=" + percVersion);
            System.out.println(DEVELOPMENT + "=" + developmentFlag);


            System.out.println("Installation folder is " + installPath.toAbsolutePath().toString());

            Path installSrc;
            Path currentJar = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isDirectory(currentJar)) {
                installSrc = Files.createTempDirectory(INSTALL_TEMPDIR);

                // add option to not delete for debugging
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override

                    public void run() {
                        //If the debug flag is set don't delete the files.
                        if(debug.equalsIgnoreCase("false")){
                            try {
                                Files.walk(installSrc)
                                        .sorted(Comparator.reverseOrder())
                                        .map(Path::toFile)
                                        .forEach(File::delete);
                            } catch (IOException ex) {
                                System.out.println("An error occurred while executing the installation, installation has likely failed. " + ex.getMessage());
                            }
                        }
                    }
                });


                extractArchive(currentJar, installSrc, DISTRIBUTION_DIR);



            } else {
                System.out.println("Running from extracted jar");
                installSrc = currentJar.resolve(DISTRIBUTION_DIR);
            }


            Path execPath = installSrc.resolve(Paths.get("rxconfig", "Installer"));
            Path installAntJarPath = execPath.resolve(PathUtils.getVersionLessJarFilePath(execPath,PERC_ANT_JAR + "-*.jar"));
            execJar(installAntJarPath, execPath, installPath);

        } catch (Exception e) {
            System.out.println("An error occurred while executing the installation, installation has likely failed. " + e.getMessage());
        }
        System.out.println("Done extracting");
    }


        public static void extractArchive(Path archiveFile, Path destPath, String folderPrefix) throws IOException {

        Files.createDirectories(destPath); // create dest path folder(s)

        try (ZipFile archive = new ZipFile(archiveFile.toFile())) {

            // sort entries by name to always create folders first
            List<? extends ZipEntry> entries = archive.stream()
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .collect(Collectors.toList());

            // copy each entry in the dest path
            for (ZipEntry entry : entries) {
                currentLineNo++;
                String entryName = entry.getName();
                if (!entryName.startsWith(folderPrefix))
                    continue;

                String name = entryName.substring(folderPrefix.length() + 1);
                if (name.length() == 0)
                    continue;

                Path entryDest = destPath.resolve(name);
                File newFile = new File(entryDest.toString());
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();

                if (entry.isDirectory()) {
                    Files.createDirectory(entryDest);
                    continue;
                }
                if(MainIAInstall.installerProxy!=null){
                    MainIAInstall.showProgress(MainIAInstall.installerProxy,currentLineNo,"Extracting temporary files...",entryDest.toString());
                }else {
                    System.out.println("Creating file " + entryDest);
                }
                Files.copy(archive.getInputStream(entry), entryDest);


            }
        }   catch(Exception ex){
            ex.printStackTrace();
            error=true;
        }
    }

    public static Integer execJar(Path jar, Path execPath, Path installDir) throws IOException,
            InterruptedException {

        try {

            String javaHome = System.getProperty(PERC_JAVA_HOME);
            if (javaHome == null || javaHome.trim().equalsIgnoreCase(""))
                javaHome = System.getProperty(JAVA_HOME);

            String javabin = "";

            if (System.getProperty("file.separator").equals("/")) {
                javabin = javaHome + "/bin/java";
            } else {
                javabin = javaHome + "\\bin\\java.exe";
            }

            //"-Dlistener=com.percussion.preinstall.AntBuildListener",
            ProcessBuilder builder = new ProcessBuilder(
                    javabin,"-Dfile.encoding=UTF-8","-Dsun.jnu.encoding=UTF-8", "-Dinstall.dir=" + installDir.toAbsolutePath().toString(), "-jar", jar.toAbsolutePath().toString(), "-f", ANT_INSTALL).directory(execPath.toFile());

            //pass in known flags
            builder.environment().put(DEVELOPMENT, developmentFlag);
            builder.environment().put(PERCUSSION_VERSION, percVersion);

            //Pass on the temp dir if set
            builder.environment().put(JAVA_TEMP, System.getProperty("java.io.tmpdir"));
            Process process = builder.inheritIO().start();

            try(InputStream inStream = process.getInputStream()) {
                try (InputStream inErrStream = process.getErrorStream()) {

                    InputStreamLineBuffer outBuff = new InputStreamLineBuffer(inStream);
                    InputStreamLineBuffer errBuff = new InputStreamLineBuffer(inErrStream);
                    Thread streamReader = new Thread(new Runnable() {
                        public void run() {
                            // start the input reader buffer threads
                            outBuff.start();
                            errBuff.start();

                            // while an input reader buffer thread is alive
                            // or there are unconsumed data left
                            while (outBuff.isAlive() || outBuff.hasNext() ||
                                    errBuff.isAlive() || errBuff.hasNext()) {

                                // get the normal output if at least 50 millis have passed
                                if (outBuff.timeElapsed() > 50)
                                    while (outBuff.hasNext()) {
                                        currentLineNo++;
                                        if (MainIAInstall.installerProxy != null) {
                                            MainIAInstall.showProgress(MainIAInstall.installerProxy, currentLineNo, "Installing files...", outBuff.getNext());
                                        } else {
                                            System.out.println(errBuff.getNext());
                                        }
                                    }
                                // get the error output if at least 50 millis have passed
                                if (errBuff.timeElapsed() > 50)
                                    while (errBuff.hasNext())
                                        currentErrLineNo++;

                                if (MainIAInstall.installerProxy != null) {
                                    MainIAInstall.showProgress(MainIAInstall.installerProxy, currentErrLineNo, "Installing files...", errBuff.getNext());
                                } else {
                                    System.err.println(errBuff.getNext());
                                }
                                // sleep a bit bofore next run
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                            System.out.println("Finish reading error and output stream");
                        }
                    });

            streamReader.start();

            process.waitFor();

            //Shutdown threads and streams
            streamReader.interrupt();
            process.getInputStream().close();
            process.getErrorStream().close();

            streamReader.join();
            updateUserSpringConfig(installDir);
            updateCategoryXMLForUpgrade(installDir);
            updateJettyServerPortAndSSLToPreUpgradeSettings(installDir);
            processCode = process.exitValue();
            if(processCode!=0){
                error=true;
            }

        }
            }
        }

        catch(Exception ex){
            ex.printStackTrace();
            processCode=-2;
            error=true;
        }
        return processCode;
    }

    public static void updateUserSpringConfig(Path installDir){
        String userSprinXMLDir = installDir.toAbsolutePath().toString()+"/jetty/base/webapps/Rhythmyx/WEB-INF/config/user/spring/";
        String[] ext = new String[] {"xml"};
        File userSprinXMLDirectory = new File(userSprinXMLDir);
        if(userSprinXMLDirectory.exists()){
        List<File> userSpringXMLFiles = (List<File>) FileUtils.listFiles(userSprinXMLDirectory, ext, false);
        for(File xmlFile : userSpringXMLFiles) {
            if (xmlFile.exists()) {

                replaceTokens(xmlFile,"<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \n" +
                        "   \"http://www.springframework.org/dtd/spring-beans.dtd\">\n" +
                        "<beans>", "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "xmlns:tx=\"http://www.springframework.org/schema/tx\"\n" +
                        "xmlns:context=\"http://www.springframework.org/schema/context\"\n" +
                        "xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n" +
                        "http://www.springframework.org/schema/beans/spring-beans-4.2.xsd\n" +
                        "http://www.springframework.org/schema/tx\n" +
                        "http://www.springframework.org/schema/tx/spring-tx-4.2.xsd\n" +
                        "http://www.springframework.org/schema/context\n" +
                        "http://www.springframework.org/schema/context/spring-context-4.2.xsd\">");

            }
        }
        }
    }

    public static void updateCategoryXMLForUpgrade(Path installDir){
        String categoryXMLDir = installDir.toAbsolutePath().toString()+"/rx_resources/category/category.xml";
        File categoryXML = new File(categoryXMLDir);
        AtomicReference<String> topLevelNodeToReplace= new AtomicReference<>("");
        AtomicReference<String> topLevelNodeEndToReplace= new AtomicReference<>("");
        AtomicReference<Boolean> topLevelNodeStringPresent = new AtomicReference<>(false);
        if(categoryXML.exists()){

            try (Stream<String> stream = Files.lines(Paths.get(categoryXMLDir))) {
                stream.forEach(s ->{
                    if(s.contains("<CategoryTree")){
                        topLevelNodeToReplace.set(s);
                    }
                    if(s.contains("</CategoryTree"));{
                        topLevelNodeEndToReplace.set(s);
                    }
                    if(s.contains("topLevelNodes")){
                        topLevelNodeStringPresent.set(true);
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
            }

            if(!topLevelNodeStringPresent.get()){
                replaceTokens(categoryXML, topLevelNodeToReplace.get(), topLevelNodeToReplace+"\n"+"<topLevelNodes>");
                replaceTokens(categoryXML, topLevelNodeEndToReplace.get(), "</topLevelNodes>\n"+topLevelNodeEndToReplace);
            }
        }
    }

    public static void replaceTokens(File file, String replaceToken, String replaceValue){
        Replace r = new Replace();
        r.setFile(file);
        r.setToken(replaceToken);
        r.createReplaceToken();
        r.setValue(replaceValue);
        r.createReplaceValue();
        r.execute();
    }

    public static void updateJettyServerPortAndSSLToPreUpgradeSettings(Path installDir) throws ParserConfigurationException, IOException, SAXException {
        String oldServerXMLDir = installDir.toAbsolutePath().toString()+"/JBossServerXML_BAK/";
        File oldServerXMLFile=  new File(oldServerXMLDir+"server.xml");
        if(oldServerXMLFile.exists()) {
            DocumentBuilderFactory dbf = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                    false
            );
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            try (FileInputStream fis = new FileInputStream(oldServerXMLFile)) {
                Document doc = db.parse(fis);
                NodeList nodeList = doc.getElementsByTagName("Connector");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Element e = (Element) nodeList.item(i);
                    boolean hasAttribute = e.hasAttribute("scheme");
                    if (hasAttribute && e.getAttribute("scheme").equalsIgnoreCase("http")) {
                        writeInstallationPropertiesForJetty(installDir, "jetty.http.port=", e.getAttribute("port"));
                    }
                    if (hasAttribute && e.getAttribute("scheme").equalsIgnoreCase("https")) {
                        writeInstallationPropertiesForJetty(installDir, "jetty.ssl.port=", e.getAttribute("port"));
                        String keyStorefileAttr = e.getAttribute("keystoreFile");
                        String keyStorefileName = "";
                        String keyStoreFilePath = "";
                        String keystorePassWord = e.getAttribute("keystorePass");
                        String[] splitArr;
                        if (keyStorefileAttr != "") {
                            splitArr = keyStorefileAttr.split("/");
                            keyStorefileName = splitArr[splitArr.length - 1];
                            if (System.getProperty("file.separator").equals("/")) {
                                keyStoreFilePath = "etc/" + keyStorefileName;
                            } else {
                                String wPath = installDir.toAbsolutePath().toString().replace("\\", "\\\\");
                                keyStoreFilePath = "etc\\\\" + keyStorefileName;
                            }
                        }
                        writeInstallationPropertiesForJetty(installDir, "jetty.sslContext.keyStorePath=", keyStoreFilePath);
                        writeInstallationPropertiesForJetty(installDir, "jetty.sslContext.trustStorePath=", keyStoreFilePath);
                        writeInstallationPropertiesForJetty(installDir, "jetty.sslContext.keyStorePassword=", keystorePassWord);
                        writeInstallationPropertiesForJetty(installDir, "jetty.sslContext.keyManagerPassword=", keystorePassWord);
                        writeInstallationPropertiesForJetty(installDir, "jetty.sslContext.trustStorePassword=", keystorePassWord);
                        writeInstallationPropertiesForJetty(installDir, "perc.ssl.protocols=", e.getAttribute("protocols"));
                    }
                }
            }
        }
    }

    public static void writeInstallationPropertiesForJetty(Path installDir, String replaceToken, String value) throws IOException {
        AtomicReference<String> replaceString = new AtomicReference<>("");
        AtomicReference<String> replaceValue = new AtomicReference<>("");

        String installationPropertiesFileDir = installDir.toAbsolutePath().toString()+"/jetty/base/etc/installation.properties";

        try (Stream<String> stream = Files.lines(Paths.get(installationPropertiesFileDir))) {
            stream.forEach(s ->{if(s.contains(replaceToken)){
                replaceString.set(s);
                replaceValue.set(replaceToken + value);
            }
            });
        }
        File installationPropertiesFile = new File(installDir.toAbsolutePath().toString()+"/jetty/base/etc/installation.properties");
        replaceTokens(installationPropertiesFile,replaceString.get(),replaceValue.get());
    }

}