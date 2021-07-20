/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.thumbnail;


import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class PSScreenCapture {

    protected static final Logger log = LogManager.getLogger(PSScreenCapture.class);

    public static final String EMPTY_THUMB_RESOURCE = "META-INF/resources/sys_resources/images/thumbnail/empty-thumb.jpg";
    public static final String WEB_CAP_JS_RESOURCE = "META-INF/resources/sys_resources/js/webcap.js";

    private static final String OS = System.getProperty("os.name")
            .toLowerCase();

    private static volatile Boolean install = true;

    private static final File PHANTOM_JS_INST_DIR = new File(PathUtils.getRxDir(null), "bin");


    private static File PHANTOM_JS = null;

    private static final File WEB_CAP_PATH = new File(PathUtils.getRxDir(null), "sys_resources/js/webcap.js");


    public static void generateEmptyThumb(String imagePathForGeneration)
            throws IOException {
        copyResource(EMPTY_THUMB_RESOURCE, new File(imagePathForGeneration));

    }



    public static void takeCapture(String urlForCapture, String imagePath,
                                   int width, int height) {
        if(OS.contains("windows"))
            PHANTOM_JS=new File(PHANTOM_JS_INST_DIR,"phantomjs.exe");
        else
            PHANTOM_JS=new File(PHANTOM_JS_INST_DIR,"phantomjs");


        extractExecutable();


        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            urlForCapture = urlForCapture.replace("\\", "/");

            CommandLine commandline = CommandLine.parse(PHANTOM_JS.getAbsolutePath())
                    .addArgument(WEB_CAP_PATH.getAbsolutePath(), true)
                    .addArgument(urlForCapture, true)
                    .addArgument(imagePath, true)
                    .addArgument(String.valueOf(width), true)
                    .addArgument(String.valueOf(height), true);


            DefaultExecutor exec = new DefaultExecutor();
            PumpStreamHandler streamHandler = new PumpStreamHandler(new ExecLogHandler(log, Level.DEBUG), new ExecLogHandler(log, Level.ERROR));

            exec.setStreamHandler(streamHandler);
            exec.setExitValue(0);
            ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
            exec.setWatchdog(watchdog);

            exec.execute(commandline);

        } catch (Exception e) {
            log.error("Error taking screen capture using phantomjs with error: {}" ,PSExceptionUtils.getMessageForLog(e));
            log.debug(e);

        }
    }

    private static synchronized void extractExecutable() {

        if (install)
        {
            copyWebCapResource();
            extractPhantomJs();
            install = false;
        }
    }

    private static synchronized void extractPhantomJs() {
        if (!PHANTOM_JS.exists()) {

                if (install && !PHANTOM_JS.exists()) {
                    ClassLoader classLoader = PSScreenCapture.class.getClassLoader();
                    String instFilename = "";
                    String phantomBinary = "phantomjs";

                    if(OS.contains("windows")){
                        instFilename = "phantomjs-win.zip";
                        phantomBinary = "phantomjs.exe";
                    }else if(OS.contains("mac")){
                        instFilename = "phantomjs-macosx-macosx.zip";
                    }else{
                        instFilename = "phantomjs-linux.tar.bz2";
                    }

                    try (InputStream in = PSScreenCapture.class.getResourceAsStream("/phantomjs-inst/" + instFilename);
                         BufferedInputStream bis = new BufferedInputStream(in)
                    ) {
                        Path srcDir = Files.createTempDirectory("phantominst");
                        srcDir.toFile().deleteOnExit();

                        if (instFilename.endsWith(".zip"))
                            extractZip(bis, instFilename, srcDir.toFile());
                        else if (instFilename.endsWith(".bz2")) {
                            extractB2z(bis, instFilename, srcDir.toFile());
                        }
                        Path sourceFile = Paths.get(srcDir.resolve("bin" + File.separatorChar + phantomBinary).toString());
                        if(!sourceFile.toFile().exists()){
                            //Try full package path based on OS.
                            if(OS.contains("windows")){
                                sourceFile = Paths.get(srcDir.resolve("phantomjs-2.1.1-windows" + File.separatorChar + "bin" + File.separatorChar + phantomBinary).toString());
                            }else if(OS.contains("mac")){
                                sourceFile = Paths.get(srcDir.resolve("phantomjs-2.1.1-macosx" + File.separatorChar  + "bin" + File.separatorChar + phantomBinary).toString());
                            }else{
                                sourceFile = Paths.get(srcDir.resolve("phantomjs-2.1.1-linux-x86_64" + File.separatorChar + "bin" + File.separatorChar + phantomBinary).toString());
                            }
                        }
                        Path targetFile = Paths.get(PathUtils.getRxDir(null).getPath() + File.separatorChar + "bin" + File.separatorChar + phantomBinary);

                        Path bin = Paths.get(PathUtils.getRxDir(null).getPath() + File.separatorChar + "bin");
                        //Create bin folder if it is missing
                        if(Files.notExists(bin)){
                            Files.createDirectory(bin);
                        }
                        //Now copy executable to final location
                        Files.copy(sourceFile,targetFile,  StandardCopyOption.REPLACE_EXISTING);

                        try {
                            Set<PosixFilePermission> permissions = new HashSet<>();

                            permissions.add(PosixFilePermission.GROUP_EXECUTE);
                            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
                            permissions.add(PosixFilePermission.OWNER_EXECUTE);

                            Files.setPosixFilePermissions(targetFile, permissions);
                        }catch(UnsupportedOperationException e){
                            log.warn("Unable to set Posix permissions, not supported on this operating system");
                        }
                    } catch (IOException ex) {
                        log.error("Error getting install from resource: {}" , PSExceptionUtils.getMessageForLog(ex));
                        log.debug(ex);
                    }

                }
            }
    }

    private static void copyWebCapResource() {
        if (!WEB_CAP_PATH.exists()) {
                copyResource(WEB_CAP_JS_RESOURCE, WEB_CAP_PATH);
        }
    }

    private static void extractB2z(InputStream fs, String filename, File targetDir) {

        targetDir.mkdirs();
        File tarFile = new File(StringUtils.substringBefore(filename, ".bz2"));
        try (
                BufferedInputStream in = new BufferedInputStream(fs);
                OutputStream out = new FileOutputStream(tarFile)) {
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            final byte[] buffer = new byte[1024];
            int n;
            while (-1 != (n = bzIn.read(buffer))) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            log.error("IO Exception while extracting bz2 archive:{}" ,
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
        }

        extractTar(targetDir, tarFile);


    }

    private static void extractTar(File targetDir, File tarFile) {
        targetDir.mkdirs();
        try (TarArchiveInputStream fin = new TarArchiveInputStream(new FileInputStream(tarFile))) {
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(targetDir, entry.getName());
                if (!curfile.toPath().normalize().startsWith(targetDir.toPath())) {
                    log.error("Invalid File Path: {}" , curfile.toPath());
                    return;
                }
                if(curfile.getName().endsWith("phantomjs"))
                    curfile.setExecutable(true);

                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                try(FileOutputStream fos = new FileOutputStream(curfile)) {
                    IOUtils.copy(fin,fos );
                }
            }
        } catch (FileNotFoundException e) {
            log.error("Cannot find file extracting tar for phantomjs install: {}" , e.getMessage());
            log.debug(e);
        } catch (IOException e) {
            log.error("Error reading extracting tar file for phantomjs install: {}" , e.getMessage());
            log.debug(e);
        }
    }

    private static void extractZip(InputStream fs, String instFilename, File targetDir) {
        try (
             ArchiveInputStream i = new ArchiveStreamFactory()
                     .createArchiveInputStream(fs)) {
            ArchiveEntry entry = null;
            while ((entry = i.getNextEntry()) != null) {
                log.debug("Extracting {} to {}" ,entry.getName(), targetDir);
                if (!i.canReadEntryData(entry)) {
                    log.error("Cannot read entry {}" , entry.getName());
                    continue;
                }
                String name = fileName(targetDir, entry);
                File f = new File(name);

                if(name.endsWith("phantomjs"))
                    f.setExecutable(true);

                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(i, o);
                    }
                }
            }
        } catch (ArchiveException | IOException e) {
            log.error("Unexpected Archive error while installing phantomjs for the thumbnail service: {}" , e.getMessage());
            log.debug(e);
        }
    }

    private static String fileName(File targetDir, ArchiveEntry entry) {

        return new File(targetDir, StringUtils.substringAfter(entry.getName(), "/")).getAbsolutePath();
    }

    private static void copyResource(String resourceName, File destination) {
        ClassLoader classLoader = PSScreenCapture.class.getClassLoader();

        OutputStream resStreamOut = null;

        try ( InputStream stream = classLoader.getResourceAsStream(resourceName)){

            if(stream==null){
                log.warn("Unable to locate thumbnail resource: {}" , resourceName);
            }else{
                FileUtils.copyInputStreamToFile(stream,destination);
            }
        } catch (IOException e) {
            log.warn("Unable to copy thumbnail resource: {}" , PSExceptionUtils.getMessageForLog(e));
            log.debug(e);
        }
    }

    private static class ExecLogHandler extends LogOutputStream {
        private Logger log;
        private Level level;

        public ExecLogHandler(Logger log, Level logLevel) {
            super(logLevel.intLevel());
            this.level = logLevel;
            this.log = log;
        }

        @Override
        protected void processLine(String line, int logLevel) {
            log.log(level, line);
        }
    }



}
