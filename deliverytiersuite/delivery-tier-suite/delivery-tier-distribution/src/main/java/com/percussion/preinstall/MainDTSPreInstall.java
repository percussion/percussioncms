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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.preinstall;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainDTSPreInstall {
    private static String DISTRIBUTION_DIR="distribution";
    private static final String PERC_JAVA_HOME="perc.java.home";
    private static final String JAVA_HOME="java.home";
    private static final String PERCUSSION_VERSION="perc.version";
    private static final String INSTALL_TEMPDIR="percDTSInstallTmp_";
    private static final String PERC_ANT_JAR="perc-ant";
    private static final String ANT_INSTALL="installDts.xml";

    /**
     * Find a jar by path pattern to avoid hard coding / forcing version.
     *
     * @param execPath Folder containing the jar
     * @param fileNameWithPattern A File name with a glob pattern like perc-ant-*.jar
     * @return Path to the ant jar
     * @throws IOException
     */
    private static Path getVersionLessJarFilePath(Path execPath, String fileNameWithPattern) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(execPath.toAbsolutePath(), fileNameWithPattern)) {
            List<Path> paths = new ArrayList<>();
            for (Path path : ds) {
                paths.add(path);
            }
            if (paths.isEmpty()) {
                throw new IOException(fileNameWithPattern + " not found.");
            } else if (paths.size() == 1) {
                return paths.get(0);
            } else {
                System.out.println("Warning: Multiple " + fileNameWithPattern + " jars found, selecting the first one: " + paths.get(0).toAbsolutePath().toString());
                return paths.get(0);
            }
        }
    }

    private static File tmpFolder;
    public static void main(String[] args) {
        int exitCode = 0;
        try {

            String javaHome = System.getProperty(PERC_JAVA_HOME);
            if(javaHome == null || javaHome.trim().equalsIgnoreCase(""))
                javaHome = System.getProperty(JAVA_HOME);
            String javabin = "";

            if(System.getProperty("file.separator").equals("/")) {
                javabin = javaHome + "/bin/java";
            }
            else {
                javabin = javaHome + "/bin/java.exe";
            }

            String percVersion= System.getProperty(PERCUSSION_VERSION);
            if(percVersion== null)
                percVersion="";

            System.out.println("perc.java.home="+javaHome);
            System.out.println("java.executable="+javabin);
            System.out.println("perc.version=" + percVersion);

            if (args.length<1)
            {
                System.out.println("Must specify installation or upgrade folder");
                System.exit(0);
            }

            System.out.println("Installation folder ="+args[0]);
            Path installPath = Paths.get(args[0]);
            String isProduction="true";
            isProduction=System.getProperty("install.prod.dts");
            System.out.println("====Will remove below code if value of is Production comes fine PSDeliveryTierServerTYpePanel"+isProduction);
            if(isProduction!=null){
                if(isProduction.equals("$DTS_SERVER_TYPE$")){
                    isProduction="true";
                }
            }else{
                isProduction="true";//change done for dev environment
            }



            Path installSrc;
            Path currentJar = Paths.get(MainDTSPreInstall.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isDirectory(currentJar)) {
                installSrc = Files.createTempDirectory(INSTALL_TEMPDIR);
                System.out.println("install.tempdir=" + installSrc);
                // add option to not delete for debugging
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override

                    public void run() {
                        try {
                            Files.walk(installSrc)
                                    .sorted(Comparator.reverseOrder())
                                    .map(Path::toFile)
                                    .forEach(File::delete);
                        } catch (IOException ex) {
                            System.out.println("An error occurred processing installation files. " +  ex.getMessage());
                        }
                    }
                });

                extractArchive(currentJar, installSrc, DISTRIBUTION_DIR);
            } else {
                System.out.println("Running from extracted jar");
                installSrc = currentJar.resolve(DISTRIBUTION_DIR);
            }


            Path execPath = installSrc.resolve(Paths.get("rxconfig","Installer"));
            Path installAntJarPath = execPath.resolve(
                    getVersionLessJarFilePath(
                    execPath,PERC_ANT_JAR + "-*.jar"));

            exitCode =  execJar(installAntJarPath,execPath,installPath,isProduction);

        } catch (Exception e) {
            System.out.println("An unexpected error occurred processing installation files. " + e.getMessage());
            throw  new AntJobFailedException(String.format("Installation failed. %s", e.getMessage()));
        }
        System.out.println(String.format("Done extracting exit code %d", exitCode));
        if(exitCode != 0){
            throw  new AntJobFailedException(String.format("Installation failed. Exit code: %d ",exitCode));
        }
    }



    public static void extractArchive(Path archiveFile, Path destPath,String folderPrefix) throws IOException {

        Files.createDirectories(destPath); // create dest path folder(s)

        try (ZipFile archive = new ZipFile(archiveFile.toFile())) {

            // sort entries by name to always create folders first
            List<? extends ZipEntry> entries = archive.stream()
                    .sorted(Comparator.comparing(ZipEntry::getName))
                    .collect(Collectors.toList());

            // copy each entry in the dest path
            for (ZipEntry entry : entries) {
                String entryName = entry.getName();
                if (!entryName.startsWith(folderPrefix))
                    continue;

                String name = entryName.substring(folderPrefix.length()+1);
                if (name.length()==0)
                    continue;

                Path entryDest = destPath.resolve(name);

                if (entry.isDirectory()) {
                    Files.createDirectory(entryDest);
                    continue;
                }
                System.out.println("Creating file "+entryDest);
                Files.copy(archive.getInputStream(entry), entryDest);


            }
        }
    }

    public static int execJar(Path jar, Path execPath, Path installDir,String isProduction) throws IOException,
            InterruptedException {

        String dir=installDir.toAbsolutePath().toString();
        String javaHome = System.getProperty(PERC_JAVA_HOME);
        if(javaHome == null || javaHome.trim().equalsIgnoreCase(""))
            javaHome = System.getProperty(JAVA_HOME);

        String javabin="";
        if(System.getProperty("file.separator").equals("/")) {
            javabin = javaHome + "/bin/java";
        }
        else {
            javabin = javaHome + "/bin/java.exe";
        }
        System.out.println("isProduction:" + isProduction);
        System.out.println("Install Dir:" + dir);
        System.out.println("Java Executable:" + javabin);


        ProcessBuilder builder = new ProcessBuilder(
                javabin,"-Dinstall.prod.dts="+isProduction,"-Dfile.encoding=UTF8","-Dsun.jnu.encoding=UTF8","-Dinstall.dir="+dir, "-jar", jar.toAbsolutePath().toString(),"-f",ANT_INSTALL).directory(execPath.toFile());
        Process process = builder.inheritIO().start();
        process.waitFor();
        return process.exitValue();
    }


            }
