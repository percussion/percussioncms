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

package com.percussion.ant.install;

import com.percussion.delivery.utils.security.PSSecureProperty;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

public class PSExecDTSSqlStmt extends PSExecSQLStmt {


    protected  String getDBPropertyFile(){
        String propFile = getRootDir() + File.separator
                + "Deployment/Server/conf/perc/perc-datasources.properties";
        return propFile;
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
            try {
                dpwd = PSEncryptor.getInstance().decrypt(pwd);
            } catch (PSEncryptionException | java.lang.IllegalArgumentException e) {
                dpwd = PSLegacyEncrypter.getInstance().decrypt(pwd,
                        PSJdbcDbmsDef.getPartOneKey());
            }
            if(dpwd == null || dpwd.isEmpty()){
                PSLogger.logError("Password Decryption failed");
            }
            String driverClassName = props.getProperty("jdbcDriver");
            if(driverClassName == null || driverClassName.isEmpty()){
                PSLogger.logError("Driver Class Name not defined in properties");
                return;
            }
            try {
                Class.forName(driverClassName).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                PSLogger.logError("Driver Class Not found" + driverClassName);
            }
            try (Connection conn = DriverManager.getConnection(jdbcUrl, user, dpwd)) {
                String strStmt = getSql();
                PSLogger.logInfo("Executing statement : " + strStmt);
                try(Statement stmt = conn.createStatement()) {
                    stmt.execute(strStmt);
                    PSLogger.logInfo("Successfully executed statement.");
                }
            }
        }catch( Exception ex ) {
                if(!isFailonerror()){
                    if(!isSilenceErrors()){
                        PSLogger.logError(ex.getMessage());
                        if(getPrintExceptionStackTrace()){
                            ex.printStackTrace();
                        }
                    }
                    return;
                }else{
                    if(!isSilenceErrors()) {
                        PSLogger.logError(ex.getMessage());
                        if(getPrintExceptionStackTrace()){
                            ex.printStackTrace();
                        }
                    }
                    throw new BuildException(ex);
                }
            }
        }
    }

