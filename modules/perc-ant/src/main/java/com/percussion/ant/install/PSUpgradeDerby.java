/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.ant.install;


import com.percussion.error.PSExceptionUtils;
import com.percussion.install.PSLogger;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.Properties;


/***
 * Provides an ant task to upgrade a derby database.  Expects to be able to start the derby database in
 * embedded mode - single user
 */
public class PSUpgradeDerby extends PSAction {

    private String targetVersion;

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    private String databasePath;

    public String getDatabasePath() {
        return databasePath;
    }


    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
    }

    private String backupDirectory;

    public String getBackupDirectory() {
        return backupDirectory;
    }

    public void setBackupDirectory(String backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    private String userName;
    private String password;
    private String schema;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }


    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";

    private void dropPercMetadataPropertiesTable(Connection conn)  {
        org.apache.derby.impl.jdbc.EmbedStatement stmt = null;
        try {
            stmt = (org.apache.derby.impl.jdbc.EmbedStatement) conn.createStatement();
            String sql2 = "DROP TABLE PERC_PAGE_METADATA_PROPERTIES";
            stmt.executeUpdate(sql2);

        }catch (SQLException e) {
            PSLogger.logWarn("SQL State: Drop table for PERC_PAGE_METADATA_PROPERTIES failed, may be doesn't exist" + PSExceptionUtils.getMessageForLog(e) );
        }
        try {
            String sql = "DROP TABLE PERC_PAGE_METADATA";
            stmt.executeUpdate(sql);

        }catch (SQLException e){
            PSLogger.logWarn("SQL State: Drop table for PERC_PAGE_METADATA failed, may be doesn't exist" + PSExceptionUtils.getMessageForLog(e) );
        }
        try {
            String sql = "SELECT COUNT(*) from PERC_COOKIE_CONSENT";
            org.apache.derby.impl.jdbc.EmbedStatement prepStmt = (org.apache.derby.impl.jdbc.EmbedStatement) conn.createStatement();
            ResultSet rs = prepStmt.executeQuery(sql);
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            PSLogger.logInfo("Got Result in PERC_COOKIE_CONSENT.");
            if (count == 0) {
                PSLogger.logInfo("Rows in PERC_COOKIE_CONSENT are 0 thus deleting table.");
                String sql2 = "DROP TABLE PERC_COOKIE_CONSENT";
                stmt.execute(sql2);
            }

        }catch (SQLException e){
            PSLogger.logWarn("SQL State: Drop table for PERC_COOKIE_CONSENT failed, may be doesn't exist: "+ PSExceptionUtils.getMessageForLog(e)  );
        }

        try {
            String sql = "SELECT COUNT(*) from BLOG_POST_VISIT";
            org.apache.derby.impl.jdbc.EmbedStatement prepStmt = (org.apache.derby.impl.jdbc.EmbedStatement) conn.createStatement();
            ResultSet rs = prepStmt.executeQuery(sql);
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            PSLogger.logInfo("Got Result in BLOG_POST_VISIT.");
            if (count == 0) {
                PSLogger.logInfo("Rows in BLOG_POST_VISIT are 0 thus deleting table.");
                String sql2 = "DROP TABLE BLOG_POST_VISIT";
                stmt.execute(sql2);
            }

        }catch (SQLException e){
            PSLogger.logWarn("SQL State: Drop table for BLOG_POST_VISIT failed, may be doesn't exist" + PSExceptionUtils.getMessageForLog(e) );
        }
    }
    public synchronized void loadDerbyJDBCJar() throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PSLogger.logInfo("Loading DerbyDriver at RunTime");
        File derbyJDBCDriver = null;
        File dir = new File(getRootDir() + File.separator + "Deployment/Server/common/lib");
        FileFilter fileFilter = new WildcardFileFilter("derby-*.jar");
        File[] files = dir.listFiles(fileFilter);
        if(files != null){
            if(files.length == 1) {
                derbyJDBCDriver = files[0];
            }else{
                derbyJDBCDriver = files[0];
                PSLogger.logError("Multiple versions of DerbyDriver Exist in " + dir.toString());
            }
        }
        if(derbyJDBCDriver == null){
            PSLogger.logError("DerbyDriver is Missing");
            return;
        }
        PSLogger.logInfo("Loading DerbyDriver File " + derbyJDBCDriver.toString());
        java.net.URL url = derbyJDBCDriver.toURI().toURL();
        java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
        method.setAccessible(true); /*promote the method to public access*/
        method.invoke(Thread.currentThread().getContextClassLoader(), new Object[]{url});
    }

    @Override
    public void execute() throws BuildException {

        if (!Files.exists(Paths.get(databasePath))) {
            throw new BuildException("Database " + databasePath + " does not exist!");
        }

        if (!Files.exists(Paths.get(backupDirectory))) {
            throw new BuildException("Backup directory does not exist!");
        }

        try {
            Class.forName(driver).newInstance();
        } catch ( ClassNotFoundException e) {
        try {
            loadDerbyJDBCJar();
            Class.forName(driver).newInstance();
        }catch (Exception ex){
            throw new BuildException("Unable to load embedded Derby driver");
        }
        }catch (InstantiationException | IllegalAccessException  ex){
            throw new BuildException("Unable to load embedded Derby driver");
        }

        //Connection properties
        Properties props = new Properties();
        props.setProperty("user", userName);
        props.setProperty("password", password);
        props.setProperty("upgrade", "true");
        String connectionUrl = "jdbc:derby:" + databasePath;
        Connection conn;
        try {
            conn = DriverManager.getConnection(connectionUrl, props);
        } catch (SQLException e) {
            throw new BuildException(e);
        }

        try {
            if(getDatabasePath().contains("percmetadata")) {
                //Drop Metadata Tables on upgrade.
                dropPercMetadataPropertiesTable(conn);
            }
            DatabaseMetaData meta = conn.getMetaData();
            PSLogger.logInfo("Derby database version: " + meta.getDatabaseProductVersion() + " detected...");
            conn.close();
            props.remove("upgrade");
            props.putIfAbsent("shutdown", "true");
            PSLogger.logInfo("Shutting down database :" + databasePath);
            conn = DriverManager.getConnection(connectionUrl, props);
            conn.close();

        } catch (SQLNonTransientConnectionException e) {
            PSLogger.logWarn("SQL State:" + e.getSQLState());
            PSLogger.logWarn("SQL Error Code:" + e.getErrorCode());
            if (e.getErrorCode() == 45000 && e.getSQLState().equals("08006")) {
                PSLogger.logInfo("Database shutdown successfully.");
            } else {
                throw new BuildException(e);
            }
        } catch (SQLException e) {
            PSLogger.logWarn("SQL State:" + e.getSQLState());
            PSLogger.logWarn("SQL Error Code:" + e.getErrorCode());
            if (e.getErrorCode() == 45000 && e.getSQLState().equals("08006")) {
                PSLogger.logInfo("Database shutdown successfully.");
            } else {
                throw new BuildException(e);
            }
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
                //do nada}
            }


        }
    }

}
