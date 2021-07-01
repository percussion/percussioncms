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
package com.percussion.services.filestorage;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.server.PSServer;
import com.percussion.services.filestorage.error.PSBinaryMigrationException;
import com.percussion.services.filestorage.impl.PSHashedFieldCataloger;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A service to handle a singleton background thread that will find all old
 * binary fields that can be migrated to a new hash field and populate the new
 * hash field.
 * 
 * @author stephenbolton
 * 
 */
public class PSFileStorageMigrator implements Runnable
{

    /**
     * @author stephenbolton
     * 
     */
    public enum Status {
        RUNNING, STOPPING, STOPPED
    }

    private IPSFileStorageService fsService;

    private Status status = Status.STOPPED;

    private static Thread thread;

    private static PSFileStorageMigrator instance = null;

    private static int queueSize = 0;

    private static int processedCount = 0;

    private static String errorMessage = "No Error";

    /**
     * initialize singleton file storage service
     * 
     */
    public void initServices()
    {
        if (fsService == null)
        {
            fsService = PSFileStorageServiceLocator.getFileStorageService();
        }
    }

    /**
     * Get singleton instance of this class.
     * 
     * @return the instance
     */
    public static PSFileStorageMigrator getInstance()
    {
        if (instance == null)
        {
            instance = new PSFileStorageMigrator();
        }
        return instance;
    }

    /**
     * Start running the migration process in a new thread
     * 
     */
    public synchronized void start()
    {

        if (thread == null || !thread.isAlive())
        {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }

    } // end start

    /**
     * Mark the status to stop the migration process.
     * 
     */
    public synchronized void stop()
    {
        this.status = Status.STOPPING;
    } // end stop

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        initServices();
        try
        {
            PSFileStorageMigrator.processedCount = 0;

            Map<String, Map<String, String>> migrateMap = getHashFieldMigrateMap();
            IPSFileStorageService fs = PSFileStorageServiceLocator.getFileStorageService();

            this.status = Status.RUNNING;
            PSFileStorageMigrator.errorMessage = "No Error";
            Connection c = null;

            Statement st = null;
            ResultSet rs = null;

            try
            {
                c = PSConnectionHelper.getDbConnection();

                queueSize = countToMigrateRows(migrateMap);

                for (Entry<String, Map<String, String>> entry : migrateMap.entrySet())
                {
                    if (entry.getValue() != null)
                    {
                        Map<String, String> info = entry.getValue();

                        if (info.get("binary") != null && info.get("type") != null && info.get("hash") != null
                                && info.get("filename") != null)
                        {

                            String table = info.get("tableName");
                            String column = info.get("hash");
                            String binColumn = info.get("binary");

                            st = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
                            boolean moreResults = true;
                            while (moreResults == true && this.status.equals(Status.RUNNING)
                                    && !PSServer.isShuttingDown())
                            {
                                st.setMaxRows(MAX_ROWS);
                                rs = st.executeQuery("select " + binColumn + "," + info.get("type") + ","
                                        + info.get("filename") + "," + info.get("hash") + ",contentid,revisionid from "
                                        + table + " where " + column + " is null and " + binColumn + " is not null");

                                int rsCount = 0;
                                while (rs.next() && this.status.equals(Status.RUNNING) && !PSServer.isShuttingDown())
                                {
                                    rsCount++;
                                    InputStream result = rs.getBinaryStream(1);
                                    String type = rs.getString(2);
                                    String filename = rs.getString(3);
                                    int contentid = rs.getInt(5);
                                    int revisionid = rs.getInt(6);
                                    String hash = "-1";
                                    try
                                    {
                                        // encoding set to null allow it to be calculated.
                                        hash = fs.store(result, type, filename, null);
                                    }
                                    catch (Exception e)
                                    {
                                        log.error("Cannot get hash for item " + table + "." + column
                                                + " with contentid=" + contentid + " and revision" + revisionid);
                                    }

                                    log.debug("Updating " + table + "." + column + " with contentid=" + contentid
                                            + " and revision" + revisionid + " with hash " + hash);
                                    rs.updateString(4, hash);
                                    rs.updateRow();
                                    if (!c.getAutoCommit())
                                    {
                                        c.commit();
                                    }
                                    this.processedCount++;
                                    this.queueSize--;

                                }

                                if (rsCount != MAX_ROWS)
                                    moreResults = false;

                            }
                        }
                    }

                }
            }
            catch (SQLException e)
            {
                throw new PSBinaryMigrationException("Cannot execute count query ", e);
            }
            catch (NamingException e)
            {
                throw new PSBinaryMigrationException("Cannot execute count query ", e);
            }

            finally
            {
                status = Status.STOPPED;
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                }
                try
                {
                    st.close();
                }
                catch (Exception e)
                {
                }
                try
                {
                    c.close();
                }
                catch (Exception e)
                {
                }
            }

        }

        catch (Throwable e)
        {
            Throwable t = ExceptionUtils.getRootCause(e);
            if (t == null)
                t = e;
            String message = t.getMessage();
            if (message == null)
                message = t.toString();
            log.error("Error running binary field migrator", e);
            errorMessage = message;
        }

    } // end run

    /**
     * Return the current queue size. If the migration is running this will be
     * the internal count if the migration is not running we will query to find
     * how many will be migrated.
     * 
     * @return the number of items to process
     */
    public int getQueueSize()
    {
        if (thread == null)
        {
            Map<String, Map<String, String>> migrateMap = getHashFieldMigrateMap();
            queueSize = countToMigrateRows(migrateMap);
        }

        return queueSize;
    }

    /**
     * Return the current run status
     * 
     * @return the status
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * Returns a hash map of binary fields with info on the corresponding hash,
     * content type, and filename fields
     * 
     * @return a Map containing the table info on the binary field and
     *         supporting field columns. key
     */
    public static Map<String, Map<String, String>> getHashFieldMigrateMap()
    {

        PSItemDefManager itemDefMgr = PSItemDefManager.getInstance();
        long[] typeIds = itemDefMgr.getAllContentTypeIds(-1);

        List<PSField> fields = new ArrayList<>();

        for (int i = 0; i < typeIds.length; i++)
        {
            PSItemDefinition itemDef;
            try
            {
                itemDef = itemDefMgr.getItemDef(typeIds[i], -1);

                fields.addAll(itemDef.getMappedParentFields());

                for (PSFieldSet fs : itemDef.getComplexChildren())
                {
                    fields.addAll(Arrays.asList(fs.getAllFields()));
                }
            }
            catch (PSInvalidContentTypeException e)
            {
                throw new PSBinaryMigrationException("Invalid content type ", e);
            }
        }

        Map<String, Map<String, String>> fieldInfoMap = new HashMap<>();

        for (PSField field : fields)
        {

            String fieldName = field.getSubmitName();
            String tableName = PSHashedFieldCataloger.getFieldTable(field);
            String columnName = PSHashedFieldCataloger.getFieldColumn(field);

            String base = substringBeforeLast(fieldName, "_");
            String fieldKey = base + ":" + tableName;
            String ext = fieldName.substring(fieldName.lastIndexOf("_") + 1);
            if (field.getDataType().equals(PSField.DT_BINARY))
            {
                fieldKey = fieldName + ":" + tableName;
                Map<String, String> fieldInfo = fieldInfoMap.get(fieldKey);
                if (fieldInfo == null)
                {
                    fieldInfo = new HashMap<>();
                    fieldInfo.put("base", fieldName);
                    fieldInfo.put("tableName", tableName);
                    fieldInfoMap.put(fieldKey, fieldInfo);
                }
                fieldInfo.put("binary", columnName);
            }
            else if (ext.equals("type") || ext.equals("hash") || ext.equals("filename"))
            {
                Map<String, String> fieldInfo = fieldInfoMap.get(fieldKey);
                if (fieldInfo == null)
                {
                    fieldInfo = new HashMap<>();
                    fieldInfo.put("base", fieldName);
                    fieldInfo.put("tableName", tableName);
                    fieldInfoMap.put(fieldKey, fieldInfo);
                }
                else
                {
                    fieldInfo.put(ext, columnName);
                }

            }
        }

        return fieldInfoMap;

    }

    /**
     * Strip off the suffix to work out the base fieldname.
     * 
     * @param str
     * @param separator
     * @return the base name
     */
    private static String substringBeforeLast(String str, String separator)
    {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(separator))
        {
            return str;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == -1)
        {
            return str;
        }
        return str.substring(0, pos);
    }

    /**
     * query to find how many field entries need to be migrated. This could be
     * more than the number of rows if there is more than one binary field in
     * the table.
     * 
     * @param migrateMap
     * @return the count.
     */
    public int countToMigrateRows(Map<String, Map<String, String>> migrateMap)
    {

        Connection c = null;

        Statement st = null;
        ResultSet rs = null;

        int itemcount = 0;
        try
        {
            c = PSConnectionHelper.getDbConnection();
            for (Entry<String, Map<String, String>> entry : migrateMap.entrySet())
            {

                Map<String, String> info = entry.getValue();

                if (info.get("binary") != null && info.get("type") != null && info.get("hash") != null
                        && info.get("filename") != null)
                {

                    String table = info.get("tableName");
                    String column = info.get("hash");
                    String binColumn = info.get("binary");

                    st = c.createStatement();

                    rs = st.executeQuery("select count(*) from " + table + " where " + column + " is null and "
                            + binColumn + " is not null");
                    rs.next();
                    int result = rs.getInt(1);
                    itemcount += result;
                    log.debug("select count(*) from " + table + " where " + column + " is null and " + binColumn
                            + " is not null");
                }
                else
                {
                    log.debug("Not mapping field for migration " + info);
                }

            }

        }
        catch (SQLException e)
        {
            throw new PSBinaryMigrationException("Cannot execute count query ", e);
        }
        catch (NamingException e)
        {

            throw new PSBinaryMigrationException("Cannot execute count query ", e);
        }
        finally
        {
            try
            {
                rs.close();
            }
            catch (Exception e)
            {
            }
            try
            {
                st.close();
            }
            catch (Exception e)
            {
            }
            try
            {
                c.close();
            }
            catch (Exception e)
            {
            }
        }
        return itemcount;
    }

    public static String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * Logger for this class
     */
    private static final Logger log = LogManager.getLogger(PSFileStorageMigrator.class);

    /**
     * Number of rows to process as a batch.
     */
    private static final int MAX_ROWS = 500;

}
