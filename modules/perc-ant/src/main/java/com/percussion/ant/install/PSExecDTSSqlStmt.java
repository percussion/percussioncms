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

import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class PSExecDTSSqlStmt extends PSExecSQLStmt {

    /**
     * The sql statement to execute if database specific sql statment is
     * empty, never <code>null</code>, may be empty
     */
    private String preSql = "";

    private int preSqlValue = 0;

    protected  String getDBPropertyFile(){
        return   getRootDir() + File.separator
                + "Deployment/Server/conf/perc/perc-datasources.properties";
    }

    public String getPreSql() {
        return preSql;
    }

    public void setPreSql(String preSql) {
        this.preSql = preSql;
    }

    public int getPreSqlValue() {
        return preSqlValue;
    }

    public void setPreSqlValue(int preSqlValue) {
        this.preSqlValue = preSqlValue;
    }

    @Override
    public void execute()
    {
        String propFile = getDBPropertyFile();
        File f = new File(propFile);
        if (!(f.exists() && f.isFile())) {
            PSLogger.logInfo("DBPropertyFile Not Found: " + f.getPath());
            return;
        }
        PSSecureProperty.unsecureProperties(f);
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            props.load(in);
            String jdbcUrl = props.getProperty("jdbcUrl");
            String user = props.getProperty("db.username");
            String pwd = props.getProperty("db.password");
            String dpwd = pwd;
            if (getRootDir() == null || "".equals(getRootDir())) {
                PSLogger.logError("Root RootDir is missing");
                return;
            }

            if(dpwd == null || dpwd.isEmpty()){
                PSLogger.logError("Password Decryption failed");
            }
            String driverClassName = props.getProperty("jdbcDriver");
            if(driverClassName == null || driverClassName.isEmpty()){
                PSLogger.logError("Driver Class Name not defined in properties");
                return;
            }

            String dbType;
            switch(driverClassName){
                case "com.mysql.jdbc.Driver":
                    dbType="mysql";
                    break;
                case "com.microsoft.sqlserver.jdbc.SQLServerDriver":
                    dbType="sqlserver";
                    break;
                case "net.sourceforge.jtds.jdbc.Driver":
                    dbType="jtds:sqlserver";
                    break;
                case "oracle.jdbc.driver.OracleDriver":
                    dbType="oracle:thin";
                    break;
                default:
                    dbType="derby";
            }

            InstallUtil.setRootDir(this.getRootDir());
            try (Connection conn = InstallUtil.createConnection(dbType,
                    PSJdbcUtils.getServerFromUrl(jdbcUrl),user,dpwd)) {
                if(getPreSql() != null){
                    String strPreStmt = getPreSql();
                    PSLogger.logInfo("Executing statement : " + strPreStmt);
                    try(Statement stmt = conn.createStatement()) {
                        ResultSet rs = stmt.executeQuery(strPreStmt);
                        rs.next();
                        int count = rs.getInt(1);
                        rs.close();
                        PSLogger.logInfo("Got Result in PreStmt.");
                       if(count == getPreSqlValue()){
                           PSLogger.logInfo("Result in PreStmt match returned result.");
                           executeStmt(conn);
                       }
                    }
                }else{
                    executeStmt(conn);
                }
        }catch( Exception ex ) {
                if(!isFailonerror()){
                    if(!isSilenceErrors()){
                        PSLogger.logError(ex.getMessage());
                    }
                    return;
                }else{
                    if(!isSilenceErrors()) {
                        PSLogger.logError(ex.getMessage());
                    }
                    throw new BuildException(ex);
                }
            }finally {
                if(dbType.equals("derby"))
                    InstallUtil.shutDownDerby();
            }
        } catch (IOException e) {
            PSLogger.logError("IO Exception." + e.getMessage());

        }
    }

    private void executeStmt(Connection conn) throws SQLException {
        String strStmt = getSql();
        PSLogger.logInfo("Executing statement : " + strStmt);
        try(Statement stmt2 = conn.createStatement()) {
            stmt2.execute(strStmt);
            PSLogger.logInfo("Successfully executed statement.");
        }
    }

}


