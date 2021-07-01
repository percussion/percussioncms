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

package com.percussion.ant.install;

import com.google.common.collect.ImmutableList;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSContainerUtilsFactory;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.jdbc.IPSDatasourceConfig;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;


/**
 * Update jetty configuration based upon JBoss for an upgrade
 *
 */
public class PSUpdateJettyConfigFromJBoss extends PSAction
{
    private static final Logger log = LogManager.getLogger(PSUpdateJettyConfigFromJBoss.class);

    public static final String PERCUSSION_SERVER_LAX = "PercussionServer.lax";
    public static final String PERCUSSION_SERVER_LINUX_LAX = "PercussionServer.bin.lax";
    public static final String LAX_NL_JAVA_OPTION_ADDITIONAL = "lax.nl.java.option.additional";

    public static final ImmutableList<String> SKIP_ARGS=ImmutableList.of(
            "-Dprogram.name",
            "-Djava.endorsed.dirs",
            "-Djava.library.path",
            "-XX:MaxPermSize",
            "-Djboss.server.log.dir",
            "-Xrunjdwp",
            "-Dfile.encoding"
    );

    /**
     * The repository location, relative to the Rhythmyx root.
     */
    private String repositoryLocation =
            "rxconfig/Installer/rxrepository.properties";

    /**
     * Accessor for the repository location
     */
    public String getRepositoryLocation()
    {
        return repositoryLocation;
    }

    public void updateInstallOrUpgradeDatasource(IPSJndiDatasource dataSource, IPSDatasourceConfig config ){
        Properties props = null;
        PSJdbcDbmsDef dbmsDef = null;

        try {
            PSLogger.logInfo("Updating rxrepository.properties with configured repository datasource...");
            props = new Properties();

            try(FileInputStream input = new FileInputStream(getRootDir() + File.separator
                    + getRepositoryLocation())) {
                props.load(input);
            }
            props.setProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, dataSource.getName());
            props.setProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, dataSource.getDriverName());
            props.setProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY,dataSource.getServer());
            props.setProperty(PSJdbcDbmsDef.PWD_PROPERTY, dataSource.getPassword());
            props.setProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY,dataSource.getDriverClassName());
            props.setProperty(PSJdbcDbmsDef.UID_PROPERTY, dataSource.getUserId());
            if(dataSource.isEncrypted()){
                props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "Y");
            }else{
                props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY, "N");
            }
            props.setProperty(PSJdbcDbmsDef.PWD_PROPERTY,dataSource.getPassword());
            props.setProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY, config.getDatabase());
            props.setProperty(PSJdbcDbmsDef.DSCONFIG_NAME, config.getDataSource());
            props.setProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY,config.getOrigin());
            props.setProperty(PSJdbcDbmsDef.DB_BACKEND_PROPERTY,
                    PSJdbcUtils.getDBBackendForDriver(dataSource.getDriverName()));

            //Make sure the file is writeable.
            Paths.get(getRootDir() + File.separator
                    + getRepositoryLocation()).toFile().setWritable(true);

            try (OutputStream output = new FileOutputStream(getRootDir() + File.separator
                    + getRepositoryLocation())) {
                props.store(output,"Updated by upgrade: " + Instant.now().toString());
            }
        } catch (FileNotFoundException e) {
            PSLogger.logError(e.getMessage());
            throw new BuildException(e.getLocalizedMessage());
        } catch (IOException e) {
            PSLogger.logError(e.getMessage());
            throw new BuildException(e.getLocalizedMessage());
        }
    }

    public void execute()
   {
       File root = new File(getRootDir());

       DefaultConfigurationContextImpl config = PSContainerUtilsFactory.getConfigurationContextInstance(root.toPath());
       BaseContainerUtils containerUtils = config.getConfig();

           try
           {

               config.load();
               IPSDatasourceResolver resolver = containerUtils.getDatasourceResolver();


               String repoDatasource = resolver.getRepositoryDatasource();

               IPSDatasourceConfig dsConfig = resolver.getDatasourceConfiguration(repoDatasource);

               List<IPSJndiDatasource> datasources = config.getConfig().getDatasources();

               Map<String, List<IPSDatasourceConfig>> configMap = resolver.getDatasourceConfigurations().stream()
                       .collect(Collectors.groupingBy(IPSDatasourceConfig::getDataSource));

               for (IPSJndiDatasource datasource:
                       datasources) {
                   boolean isRepoSource = datasource.getName().equals(dsConfig.getDataSource());
                   log.info("Migrating datasource "+datasource.getName()+" for user "+datasource.getUserId() + " and server "+ datasource.getServer() +" is repository ="+isRepoSource);
                   if (isRepoSource)
                   {
                       // Convert to embedded driver
                       if (datasource.getDriverClassName().equals("org.apache.derby.jdbc.ClientDriver")
                               && datasource.getServer().startsWith("//localhost:1527/CMDB"))
                       {
                           log.info("Migrating CM1 embedded derby database to use embedded driver");
                           datasource.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
                           datasource.setServer("CMDB");
                       }

                   }

                   if (datasource.getDriverClassName().equals("net.sourceforge.jtds.jdbc.Driver") ||
                           datasource.getDriverName().equals("jtds:sqlserver")
                          )
                   {
                       log.info("Migrating jtds driver to mssql");
                       datasource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                       datasource.setDriverName("sqlserver");
                       String server = datasource.getServer();
                       if (server.contains(";"))
                       {
                           server = StringUtils.substringBefore(server,";");
                           String params = StringUtils.substringAfter(server,";");
                           log.warn("Ignoring additional jtds parameters"+params+" changing to ms sqlserver driver");
                       }
                   }

                   List<IPSDatasourceConfig> configs = configMap.get(datasource.getName());
                   if (configs.size()>0)
                   {
                       log.info("There are "+configs.size()+" connection configurations for jndi datasource "+datasource.getName());
                       configs.stream().forEach(c -> log.info("--> "+c.getName()+": schema="+c.getOrigin()+", databaseName="+c.getDatabase()));
                   }else{
                       log.info("JNDI Datasource "+datasource.getName()+"Does not have any connection configuration for database name and schema");
                   }

                   //Update rxrepository.properties
                   updateInstallOrUpgradeDatasource(datasource,dsConfig);

               }

               config.save();



               migrateLaxJavaSettingsToJetty();


           }
           catch (Exception e)
           {

               PSLogger.logError("Failed to update jetty configuration from jboss" + e.getMessage());
           }

   }

    private void migrateLaxJavaSettingsToJetty() {
        File jvmIni = new File(getRootDir(), "jetty/base/start.d/jvm.ini");
        Properties props = new Properties();
        File laxFile = new File(getRootDir(), PERCUSSION_SERVER_LAX);
        if (!laxFile.exists()) {
            laxFile = new File(getRootDir(), PERCUSSION_SERVER_LINUX_LAX);
        }
        if (laxFile.exists()) {
            try (InputStream fis = new FileInputStream(laxFile)) {
                props.load(fis);
            } catch (IOException e) {
                // Ignore
            }
        } else {
            log.info("No PercussionServer.lax file found. skipping");
        }
        String prop = props.getProperty(LAX_NL_JAVA_OPTION_ADDITIONAL, "");
        CommandLine execCommandLine = new CommandLine("sh");
        execCommandLine.addArguments(prop, false);
        String[] processedArgs = execCommandLine.getArguments();

        List<String> newArgs = Arrays.stream(processedArgs)
                .filter(arg -> SKIP_ARGS.stream()
                        .noneMatch(arg::startsWith))
                .collect(Collectors.toCollection(ArrayList::new));

        log.info("Migrating jvm args " + newArgs);

        if (!jvmIni.exists()) {

            File defaultIni = new File(getRootDir(), "jetty/defaults/start.d/jvm.ini");
            if (defaultIni.exists()) {
                try {
                    FileUtils.copyFile(defaultIni, jvmIni);
                } catch (IOException e) {
                    log.error("Cannot copy default default/start.d/jvm.ini to jetty/base/start.d");
                }
            }

        }
        if (!jvmIni.exists()) {
            log.error("Cannot find jvm.ini in jetty to update.  Will use default java options");
        } else {
            PSPurgableTempFile tempFile = null;
            try {
                tempFile = new PSPurgableTempFile("psx",
                        ".bin", null);
            } catch (IOException e) {
                log.error("Cannot create temp file. ");
                return;
            }
            try (PrintWriter writer = new PrintWriter(tempFile)) {

                try (BufferedReader reader = new BufferedReader(new FileReader(jvmIni))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("-Xmx") || line.startsWith("-Xms")) {
                            //CMS-6725 - Upgrade is commenting out memory settings on upgrade
                            if (newArgs == null || newArgs.size() == 0) {
                                writer.println(line);
                            }

                        } else {
                            writer.println(line);
                        }
                    }


                    if (newArgs != null && newArgs.size() > 0) {
                        for (String arg : newArgs) {
                            log.info("#args from PercussionServer.lax :" + arg);
                            writer.println(arg);
                        }
                    }

                } catch (IOException e) {
                    log.error("Failed to read jvm.ini", e);
                }
            } catch (FileNotFoundException e) {
                log.error("Failed to find temp file for jvm.ini", e);
            }

            try {
                Files.copy(tempFile.toPath(), jvmIni.toPath(), java.nio.file.StandardCopyOption
                        .REPLACE_EXISTING);
                //Delete PercussionServer.lax file as didn't remove it in Install.xml
                if (laxFile != null && laxFile.exists())
                    Files.delete(laxFile.toPath());
            } catch (IOException e) {
                log.error("Failed to copy jvm.ini", e);
            }
            tempFile.release();
        }
    }
}
