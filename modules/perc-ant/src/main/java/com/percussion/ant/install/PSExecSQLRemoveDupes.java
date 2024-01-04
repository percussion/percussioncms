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

public class PSExecSQLRemoveDupes extends PSExecSQLStmt{
    private static final Logger log = LogManager.getLogger(PSExecSQLRemoveDupes.class);
    private String qualifyingTableName = "";
    private String columns = "";
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
                //Create the select query to get only those rows from table which are duplicate rows.
                String sqlSelect = String.format("SELECT %s FROM %s GROUP BY %s HAVING count(*)>1 ", columns, finalTableName, columns);
                PSLogger.logInfo("Executing select statement : " + sqlSelect);
                PreparedStatement pstmtInsert = null;
                try (Statement stmt = conn.createStatement();
                     Statement stmtDelete = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(sqlSelect);
                    ResultSetMetaData meta = rs.getMetaData();
                    StringBuilder columnNames = new StringBuilder();
                    StringBuilder bindVariables = new StringBuilder();
                    for (int i = 1; i <= meta.getColumnCount(); i++)
                    {
                        if (i > 1) {
                            columnNames.append(", ");
                            bindVariables.append(", ");
                        }
                        columnNames.append(meta.getColumnName(i));
                        bindVariables.append('?');
                    }
                    /*
                    Here, we are running two process.
                    First process is delete where we are deleting all the duplicate records from the table by running a delete query on the result set.
                    Second process is insert where we are inserting result set values back into the table.
                    by this way duplicate rows can be inserted only once in the table hence removing duplicates.
                     */
                    String sqlInsert = String.format("INSERT INTO %s (%s) VALUES (%s)", finalTableName, columnNames, bindVariables);
                    PSLogger.logInfo("Executing insert statement : " + sqlInsert);
                    pstmtInsert = conn.prepareStatement(sqlInsert);
                    while (rs.next()) {
                        StringBuilder queryDelete = new StringBuilder();
                        queryDelete.append("DELETE FROM ");
                        queryDelete.append(finalTableName);
                        queryDelete.append(" WHERE ");
                        for (int i = 1; i <= meta.getColumnCount(); i++) {
                            if (i > 1) {
                                queryDelete.append(" AND ");
                            }
                            queryDelete.append(meta.getColumnName(i));
                            queryDelete.append(" = ");
                            switch(meta.getColumnType(i)){
                                case Types.NCHAR:
                                case Types.CLOB:
                                case Types.CHAR:
                                case Types.LONGNVARCHAR:
                                case Types.NVARCHAR:
                                case Types.VARCHAR:
                                case Types.NCLOB:
                                    queryDelete.append("'").append(rs.getObject(i)).append("'");
                                    break;
                                default:
                                    queryDelete.append(rs.getObject(i));
                            }

                            pstmtInsert.setObject(i, rs.getObject(i));
                        }
                        PSLogger.logInfo("Executing delete statement : " + queryDelete);
                        stmtDelete.execute(queryDelete.toString());
                        pstmtInsert.addBatch();
                    }
                    pstmtInsert.executeBatch();
                } catch (Exception e) {
                    handleException(e);
                } finally {
                    if(pstmtInsert != null) {
                        pstmtInsert.close();
                        pstmtInsert = null;
                    }
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

    public String getColumns()
    {
        return columns;
    }

    public void setColumns(String columns)
    {
        if (columns == null)
            columns = "";
        this.columns = columns;
    }

}
