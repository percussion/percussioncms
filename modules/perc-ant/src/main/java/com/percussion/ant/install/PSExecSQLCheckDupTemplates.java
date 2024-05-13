package com.percussion.ant.install;

import com.percussion.error.PSExceptionUtils;
import com.percussion.install.InstallUtil;
import com.percussion.install.PSLogger;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSJdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class PSExecSQLCheckDupTemplates extends PSExecSQLStmt{
    private static final Logger log = LogManager.getLogger(PSExecSQLCheckDupTemplates.class);
    private String qualifyingTableName = "";
    private String qualifyingTableNameDelete = "";
    private String column = "";
    private String columnDelete = "";
    @Override
    public void execute() {
        String propFile = getRootDir() + File.separator
                + "rxconfig/Installer/rxrepository.properties";

        File f = new File(propFile);
        if (!(f.exists() && f.isFile())) {
            log.error("Unable to connect to the repository datasource file: {}", propFile);
            if(isFailonerror() && !isSilenceErrors()){
                throw new BuildException("Unable to connect to the repository datasource file:" + propFile);
            }
            return;
        }

        try (FileInputStream in = new FileInputStream(f)) {
            Properties props = new Properties();
            props.load(in);
            PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(props);
            if (getRootDir() != null && !"".equals(getRootDir())) {
                InstallUtil.setRootDir(getRootDir());
            }
            String pw = dbmsDef.getPassword();
            String driver = dbmsDef.getDriver();
            String server = dbmsDef.getServer();
            String database = dbmsDef.getDataBase();
            String uid = dbmsDef.getUserId();
            PSLogger.logInfo("Driver : " + driver + " Server : " + server + " Database : " + database + " uid : " + uid);
            try (Connection conn = InstallUtil.createConnection(driver,
                    server,
                    database,
                    uid,
                    pw
            )) {
                //get the fully qualified table name from normal table name.
                String finalTableName = PSSqlHelper.qualifyTableName(qualifyingTableName.trim(), dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());
                String finalTableNameDelete = PSSqlHelper.qualifyTableName(qualifyingTableNameDelete.trim(), dbmsDef.getDataBase(), dbmsDef.getSchema(), dbmsDef.getDriver());

                String sqlSelect = String.format("SELECT COUNT(*) FROM %s WHERE %s IN (510,511,513,516) ",finalTableName, column);
                PSLogger.logInfo("Executing select statement : " + sqlSelect);
                try (Statement stmtSelect = conn.createStatement();
                     Statement stmtDeleteRows = conn.createStatement()) {
                    ResultSet rs = stmtSelect.executeQuery(sqlSelect);
                    int count = -1;
                    while (rs.next()) {
                        count = rs.getInt(1);
                    }
                    rs.close();

                    if (count==0) {
                        String deleteRows = String.format("DELETE FROM %s WHERE %s IN (510,511,513,516)",finalTableNameDelete, columnDelete);
                        stmtDeleteRows.executeUpdate(deleteRows);
                    }
                } catch (Exception e) {
                    handleException(e);
                }
            } catch (Exception ex) {
                handleException(ex);
            } finally {
                if (driver.equalsIgnoreCase(PSJdbcUtils.DERBY_DRIVER))
                    InstallUtil.shutDownDerby();
            }

        } catch (PSJdbcTableFactoryException | IOException e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    public String getQualifyingTableName()
    {
        return qualifyingTableName;
    }

    public void setQualifyingTableName(String qualifyingTableName)
    {
        if (qualifyingTableName == null)
            qualifyingTableName = "";
        this.qualifyingTableName = qualifyingTableName;
    }

    public String getQualifyingTableNameDelete()
    {
        return qualifyingTableNameDelete;
    }

    public void setQualifyingTableNameDelete(String qualifyingTableNameDelete)
    {
        if (qualifyingTableNameDelete == null)
            qualifyingTableNameDelete = "";
        this.qualifyingTableNameDelete = qualifyingTableNameDelete;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        if (column == null)
            column = "";
        this.column = column;
    }

    public String getColumnDelete()
    {
        return columnDelete;
    }

    public void setColumnDelete(String columnDelete)
    {
        if (columnDelete == null)
            columnDelete = "";
        this.columnDelete = columnDelete;
    }

}
