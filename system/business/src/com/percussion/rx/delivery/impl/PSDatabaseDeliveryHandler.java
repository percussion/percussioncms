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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.delivery.impl;

import com.percussion.rx.delivery.IPSDeliveryItem;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.IPSDeliveryResult.Outcome;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import com.percussion.services.pubserver.IPSDatabasePubServerFilesService;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.PSDatabasePubServerServiceLocator;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSDatabasePubServer;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactory;
import com.percussion.utils.jdbc.PSJdbcUtils;
import com.percussion.utils.xml.PSSaxCopier;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * The database delivery handler delivers content to a database. The database
 * publishing information is published using the sys_DatabasePublisher dtd. The
 * information is split three components:
 * <ul>
 * <li>A connection reference contained on the outer <code>datapublisher</code>
 * element
 * <li>A schema that describes the tables to be saved in the
 * <code>tabledefset</code> element
 * <li>The data to be published for a single item in the
 * <code>tabledataset</code> element
 * </ul>
 * <h3>Unpublishing info extraction</h3>
 * When an item is delivered, the base class saves the data into the file
 * system. The unpublishing extractor uses the item to create an unpublishing
 * item. To create the unpublishing item, the schema is scanned to discover the
 * primary keys for each involved table. The document is copied and all rows
 * that are not primary keys are removed and the action is changed to "d" so the
 * rows will be removed. The resulting document is serialized to a byte array
 * and returned.
 * <h3>Publishing</h3>
 * On commit the <code>doDelivery</code> is called and the items are
 * processed. The connections for a given job are cached so they aren't
 * repeatedly opened. This is done in the {@link #commit(long)} method, and
 * saved on a per job basis.
 * <h3>Removal</h3>
 * The saved document is reconstituted and the same basic handling is performed
 * as for publishing.
 * <hr>
 * N.B. The DbmsConnection and DbmsInfo classes are copied from the old handler.
 * 
 * @author dougrand
 */
public class PSDatabaseDeliveryHandler extends PSBaseDeliveryHandler
{
   /**
    * An utility class, which contains a targeted database information.
    */
   public static class DbmsInfo
   {
      DbmsInfo()
      {
      }
      
      /**
       * Constructs the object from the supplied document.
       * 
       * @param doc The processed document, which contains the targted database,
       *            assume it is not <code>null</code>.
       */
      public DbmsInfo(Document doc) {
         Element root = doc.getDocumentElement();

         m_dbName = root.getAttribute("dbname");
         m_resourceName = root.getAttribute("resourceName");
         m_driverType = root.getAttribute("drivertype");
         m_origin = root.getAttribute("origin");
      }

      /**
       * Indicates if the supplied object equals to the current object.
       * 
       * @param obj The supplied object, it may be <code>null</code>
       * 
       * @return <code>true</code> if the two objects are the same; otherwise
       *         return <code>false</code>.
       */
      @Override
      public boolean equals(Object obj)
      {
         if (!(obj instanceof DbmsInfo))
            return false;

         DbmsInfo other = (DbmsInfo) obj;

         return new EqualsBuilder()
            .append(m_dbName, other.m_dbName)
            .append(m_resourceName, other.m_resourceName)
            .append(m_driverType, other.m_driverType)
            .append(m_origin, other.m_origin)
            .isEquals();
      }
      
      /*
       * //see base class method for details
       */
      @Override
      public String toString()
      {
         return "resouce=" + m_resourceName + ", type=" + m_driverType
               + ", origin=" + m_origin + ", dbName="
               + (m_dbName == null ? "null" : m_dbName);
      }

      /**
       * Generates object hash code. Overrides {@link Object#hashCode()}.
       */
      @Override
      public int hashCode()
      {
         return HashCodeBuilder.reflectionHashCode(this);
      }

      /**
       * Initialized by ctor, never <code>null</code>, may be empty. It is
       * the "dbname" attribute of the root element of the processed document.
       */
      String m_dbName;

      /**
       * Initialized by ctor, never <code>null</code>, may be empty. It is
       * the "resourceName" attribute of the root element of the processed
       * document.
       */
      String m_resourceName;

      /**
       * Initialized by ctor, never <code>null</code>, may be empty. It is
       * the "drivertype" attribute of the root element of the processed
       * document.
       */
      String m_driverType;

      /**
       * Initialized by ctor, never <code>null</code>, may be empty. It is
       * the "origin" attribute of the root element of the processed document.
       */
      String m_origin;
   }

   /**
    * A container class, which contains a database connection info and its
    * related meta data. Database connections are taken from and returned to a
    * connection pool by the surrounding application server.
    */
   public static class DbmsConnection
   {
      /**
       * Constructs an object from a supplied database info.
       * 
       * @param dbmsInfo The supplied database info, it is may not be
       *            <code>null</code>.
       * 
       * @throws Exception if error occurs while creating a connection to the
       *             targeted database.
       */
      public DbmsConnection(DbmsInfo dbmsInfo) throws Exception {
         if (dbmsInfo == null)
            throw new IllegalArgumentException("dbmsInfo may not be null");

         m_dbmsInfo = dbmsInfo;
         m_dbmsDef = new PSJdbcDbmsDef(dbmsInfo.m_dbName,
               dbmsInfo.m_resourceName, dbmsInfo.m_driverType,
               dbmsInfo.m_origin, null);

         m_conn = PSJdbcTableFactory.getConnection(m_dbmsDef);
         try
         {
            m_wasAutoCommit = m_conn.getAutoCommit();
         }
         catch (SQLException e)
         {
            ms_log.error("Failed to get auto commit", e);
         }
         
         if (ms_log.isDebugEnabled())
            ms_log.debug("Got connection for: " + m_dbmsInfo.toString());
      }
      
      /**
       * Sets various connection properties before using it for database 
       * publishing.
       *  
       * @throws SQLException if a database access error occurs
       */
      public void setConnection() throws SQLException
      {
         if (m_conn.getAutoCommit())
            m_conn.setAutoCommit(false);

         String url = m_conn.getMetaData().getURL();
         String dbname = PSJdbcUtils.getDatabaseFromUrl(url);
         
         // if is not Oracle driver && not specify db-name in connection URL
         // then set the database name according to the $db.database binding
         // of the template (if the connection has not set to the db-name)
         if ((!PSJdbcUtils.ORACLE_DRIVER
               .equalsIgnoreCase(m_dbmsInfo.m_driverType))
               && StringUtils.isBlank(dbname)
               && StringUtils.isNotBlank(m_dbmsInfo.m_dbName)
               && !m_dbmsInfo.m_dbName.equals(m_conn.getCatalog()))
         {
            if (ms_log.isDebugEnabled())
            {
               ms_log.debug("Attempt to set database name of the connection to '"
                           + m_dbmsInfo.m_dbName
                           + "' for: "
                           + m_dbmsInfo.toString());
            }

            m_conn.setCatalog(m_dbmsInfo.m_dbName);
         }
      }

      /**
       * Releases database connection if there is one.
       */
      public void close()
      {
         if (m_conn != null)
         {
            try
            {
               if (m_wasAutoCommit)
                  m_conn.setAutoCommit(true);
            }
            catch (Exception e)
            {
               // ignore
            }

            try
            {
               m_conn.close();
            }
            catch (Exception e)
            {
               // ignore
            }
            finally
            {
               m_conn = null;
            }
            
            if (ms_log.isDebugEnabled())
               ms_log.debug("Closed connection for: " + m_dbmsInfo.toString());
         }
      }

      public Connection getConnection()
      {
         return m_conn;
      }
      
      public PSJdbcDbmsDef getDbmsDef()
      {
         return m_dbmsDef;
      }
      
      public Map getTableMetaMap()
      {
         return m_tableMetaMap;
      }
      
      /**
       * The place holder to remember auto commit mode after acquired 
       * a database connection. This is used to reset auto commit to its
       * original mode right before close / release it.
       */
      boolean m_wasAutoCommit = false;
      
      /**
       * The database info, initialized by ctor, never <code>null</code> after
       * that.
       */
      DbmsInfo m_dbmsInfo;

      /**
       * The connection of the targeted database, initialized by ctor, never
       * <code>null</code> after that.
       */
      Connection m_conn;

      /**
       * The definition of the targeted database, initialized by ctor, never
       * <code>null</code> after that.
       */
      PSJdbcDbmsDef m_dbmsDef;

      /**
       * It maps table names to its related meta data. The key is the table name
       * in <code>String</code>; The value is the
       * <code>PSJdbcTableMetaData</code> object of the table. It can never be
       * <code>null</code>, but may be empty.
       * <p>
       * It is used to cache the meta data of the processed tables.
       */
      @SuppressWarnings("unchecked") Map m_tableMetaMap = new HashMap();
   }

   /**
    * This object is created on a per job basis to hold all the connections that
    * are allocated. At the end of <code>commit</code> this object is
    * discarded. 
    */
   public static class Connections
   {
      /**
       * Maintain a correspondence between the database connection information
       * and the resulting connection.
       */
      private Map<DbmsInfo, DbmsConnection> m_connections =
            new HashMap<>();

      /**
       * Maintain the connections for the different jobs. Connections are
       * established and cleared when the job commits.
       */
      private static Map<Long, Connections> ms_jobConnections
            = new HashMap<>();

      /**
       * Obtain information for a given {@link DbmsInfo}.
       * 
       * @param jobid the job id of the job being processed.
       * @param info the info object, never <code>null</code>.
       * @return the connection, never <code>null</code>.
       * @throws Exception
       */
      public static synchronized DbmsConnection getConnection(long jobid,
            DbmsInfo info) throws Exception
      {
         if (info == null)
         {
            throw new IllegalArgumentException("info may not be null");
         }

         Connections c = ms_jobConnections.get(jobid);
         if (c == null)
         {
            c = new Connections();
            ms_jobConnections.put(jobid, c);
         }

         DbmsConnection rval = c.m_connections.get(info);
         if (rval == null)
         {
            rval = new DbmsConnection(info);
            c.m_connections.put(info, rval);
         }
         rval.setConnection();
         
         return rval;
      }

      /**
       * Clear all connection information associated with a given job.
       * 
       * @param jobid the job's id
       */
      public static synchronized void clearConnections(long jobid)
      {
         Connections c = ms_jobConnections.get(jobid);
         if (c != null)
         {
            for(DbmsConnection conn : c.m_connections.values())
            {
               conn.close();
            }
            ms_jobConnections.remove(jobid);
         }
      }
   }

   /**
    * This content handler takes the original database publishing document and
    * does two things:
    * <ul>
    * <li>Actions are set to "d", which causes the affected rows to be removed.
    * <li>Non-primary key rows are removed, which saves a great deal of
    * storage.
    * </ul>
    * The result is a small publishing document that can be used to unpublish
    * the specific row.
    * <p>
    * During the copying phase, when a column tag starts that is not a primary
    * key column, the skip flag is set. This causes all other content handling
    * to ignore the found elements and other nodes until the end column element
    * is found.
    */
   public static class UnpublishingContentHandler extends PSSaxCopier
   {
      /**
       * Column element constant.
       */
      private static final String COLUMN = "column";

      /**
       * Row element constant.
       */
      private static final String ROW = "row";

      /**
       * Tabledef element constant.
       */
      private static final String TABLEDEF = "tabledef";

      /**
       * Primary key element constant.
       */
      private static final String PRIMARYKEY = "primarykey";

      /**
       * Name element constant.
       */
      private static final String NAME = "name";

      /**
       * Table element constant.
       */
      private static final String TABLE = "table";

      /**
       * A map from table name to primary keys. This map is built while the
       * table schema is copied. The value is a set of primary key column names.
       */
      Map<String, Set<String>> m_primaryKeys =
            new HashMap<>();

      /**
       * The current primary keys. This field is set during the data copying
       * phase when the table element is processed. The value is set to
       * <code>null</code> when the end table element is processed.
       */
      Set<String> m_currentKeys = null;

      /**
       * The current table being examined during the schema phase. This value is
       * set as the "tabledef" elements are processed. Used when the
       * "primarykey" elements are processed to associate the keys and the
       * tables together.
       */
      String m_currentTableDef = null;

      /**
       * Flag that is set to <code>false</code> when processing rows that
       * should be skipped.
       */
      boolean m_skipping = false;

      /**
       * In a primary key this is set to non-null and is used to capture the
       * names of the primary keys.
       */
      StringBuilder m_nameCapture = null;

      /**
       * Ctor.
       * 
       * @param writer the output StAX writer, never <code>null</code>.
       * @param renames a map of names to modify when copying elements, may be
       *            <code>null</code> or empty
       */
      public UnpublishingContentHandler(XMLStreamWriter writer,
            Map<String, String> renames) {
         super(writer, null, renames, true);
      }

      @Override
      public void endElement(String uri, String localName, String name)
            throws SAXException
      {
         String lcname = name.toLowerCase();

         if (m_skipping)
         {
            m_skipping = false;
            return;
         }

         if (lcname.equals(PRIMARYKEY))
         {
            m_nameCapture = null;
         }
         else if (m_currentTableDef != null && lcname.equals(NAME)
               && m_nameCapture != null)
         {
            Set<String> keys = m_primaryKeys.get(m_currentTableDef);
            if (keys == null)
            {
               keys = new HashSet<>();
               m_primaryKeys.put(m_currentTableDef, keys);
            }
            keys.add(m_nameCapture.toString().trim());
            m_nameCapture.setLength(0);
         }

         super.endElement(uri, localName, name);
      }

      @Override
      public void startElement(String uri, String localName, String name,
            Attributes attributes) throws SAXException
      {
         String lcname = name.toLowerCase();
         if (lcname.equals(COLUMN))
         {
            String column = attributes.getValue("name");
            if (m_currentKeys != null && !m_currentKeys.contains(column))
            {
               m_skipping = true;
            }
            else
            {
               m_skipping = false;
               super.startElement(uri, localName, name, attributes);
            }
         }
         else if (lcname.equals(ROW))
         {
            resetCharCount();
            try
            {
               m_writer.writeStartElement(name);
               m_writer.writeAttribute("action", "d");
            }
            catch (XMLStreamException e)
            {
               throw new SAXException(e);
            }
         }
         else
         {
            if (lcname.equals(TABLEDEF))
            {
               m_currentTableDef = attributes.getValue("name");
            }
            else if (lcname.equals(PRIMARYKEY))
            {
               m_nameCapture = new StringBuilder();
            }
            else if (lcname.equals(TABLE) || lcname.equals("childtable"))
            {
               String table = attributes.getValue("name");
               m_currentKeys = m_primaryKeys.get(table);
            }

            super.startElement(uri, localName, name, attributes);
         }
      }

      @Override
      public void characters(char[] ch, int start, int length)
            throws SAXException
      {
         if (m_skipping)
            return;
         if (m_nameCapture != null)
         {
            m_nameCapture.append(ch, start, length);
         }
         super.characters(ch, start, length);
      }

      @Override
      public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException
      {
         if (m_skipping)
            return;
         super.ignorableWhitespace(ch, start, length);
      }

   }

   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#releaseForDelivery(long)
    */
   protected void releaseForDelivery(long jobId)
   {
      Connections.clearConnections(jobId);
   }
   
   @Override
   protected byte[] getUnpublishingInfo(IPSDeliveryItem result, 
         @SuppressWarnings("unused") String path)
         throws Exception
   {
      try(InputStream is = new FileInputStream(result.getResultFile())) {
         InputSource src = new InputSource(is);
         XMLOutputFactory ofact = XMLOutputFactory.newInstance();
         try(StringWriter writer = new StringWriter()) {
            XMLStreamWriter formatter = ofact.createXMLStreamWriter(writer);

            SAXParserFactory f = PSSecureXMLUtils.getSecuredSaxParserFactory(new PSXmlSecurityOptions(
                    true,
                    true,
                    true,
                    false,
                    true,
                    false
            ));

            SAXParser parser = f.newSAXParser();

            DefaultHandler dh =
                    new PSDatabaseDeliveryHandler.UnpublishingContentHandler(
                            formatter, null);
            formatter.writeStartDocument();
            formatter.writeCharacters("\n");
            parser.parse(src, dh);
            formatter.writeEndDocument();
            formatter.close();
            String temp = writer.toString();
            temp = StringUtils.replace(temp, PSSaxCopier.RX_FILLER, "");
            return temp.getBytes(StandardCharsets.UTF_8);
         }
      }
   }

   @Override
   protected String getPublishingLocation(IPSDeliveryItem result)
   {
      if (result == null)
      {
         throw new IllegalArgumentException("result may not be null");
      }
      return "ref_" + result.getReferenceId();
   }

   @Override
   protected String getUnpublishingLocation(IPSDeliveryItem item)
         throws Exception
   {
      return getPublishingLocation(item);
   }

   /**
    * Do the actual database operation.
    * 
    * @param item the item being published or unpublished, never
    *            <code>null</code>
    * @param jobId the id of the associated job
    * @return
    */
   protected IPSDeliveryResult perform(Item item, long jobId)
   {
         if (item == null)
         {
            throw new IllegalArgumentException("item may not be null");
         }

         if (item.getFile() != null) {
            try(InputStream is = new FileInputStream(item.getFile())){
               return performAction(item, jobId, is);
            } catch(Exception e) {
               return getItemResult(Outcome.FAILED, item, jobId, "Database item publishing failed. The error was:" + e.toString());
            }finally {
               item.release();
            }
         } else {
            try(InputStream is = item.getResultStream()) {
               return performAction(item, jobId, is);
            } catch(Exception e){
               return getItemResult(Outcome.FAILED, item, jobId, "Database item publishing failed. The error was:" + e.toString());
            }finally{
               item.release();
            }
         }
   }

   private IPSDeliveryResult performAction(Item item, long jobId,InputStream is) throws Exception {
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
              new InputSource(is), false);

      DbmsInfo dbmsInfo = new DbmsInfo(doc);

      setDbmsInfoFromPubServer(dbmsInfo, item, jobId);
      DbmsConnection dbmsConn = Connections.getConnection(jobId, dbmsInfo);

      boolean logDebug = ms_log.isDebugEnabled();

      if (logDebug)
      {
         ms_log.debug("Published Item id=" + item.getId().toString()
                 + ", filePath = " + item.getFile().getAbsolutePath());
      }


      boolean transactionSupport = true;
      final StringBuilder error_builder = new StringBuilder();
      final StringBuilder message_builder = new StringBuilder();
      try( PrintStream ps =  new PrintStream(System.out)
      {
         @Override
         public void println(String msg)
         {
            if (StringUtils.isBlank(msg))
               return;

            if (msg.indexOf("Error") >= 0)
               error_builder.append(msg);
            else
               message_builder.append(msg);
            char c = msg.charAt(msg.length()-1);
            if (c != '\r' && c != '\n')
               message_builder.append('\n');
         }
      }){
         PSJdbcTableFactory.processTables(dbmsConn.m_conn, dbmsConn.m_dbmsDef,
                 dbmsConn.m_tableMetaMap, null, doc,ps
                 , logDebug, transactionSupport);

         if (error_builder.length() > 0)
         {
            return getItemResult(Outcome.FAILED, item, jobId,
                    error_builder.toString());
         }
         else
         {
            return getItemResult(Outcome.DELIVERED, item, jobId,
                    message_builder.toString());
         }
      }



   }

   /**
    * Set the supplied DBMS Info from the publish server, which is retrieved from item or job id.
    * Do nothing if the publish server does not exist.
    * 
    * @param dbmsInfo the DBMS info, assumed not <code>null</code>.
    * @param item the item, assumed not <code>null</code>.
    * @param jobId the job id.
    */
   private void setDbmsInfoFromPubServer(DbmsInfo dbmsInfo, Item item, long jobId)
   {
      IPSPubServer pubServer = getPubServer(item, jobId);
      if (pubServer == null)
         return;
      
      DbmsInfo dbms = getDbmsInfoFromPubServer(pubServer);
      dbmsInfo.m_dbName = dbms.m_dbName;
      dbmsInfo.m_driverType = dbms.m_driverType;
      dbmsInfo.m_origin = dbms.m_origin;
      dbmsInfo.m_resourceName = dbms.m_resourceName;
   }
   
   public static DbmsInfo getDbmsInfoFromPubServer(IPSPubServer pubServer)
   {
      notNull(pubServer);
      
      PSDatabasePubServer dbServer = new PSDatabasePubServer((PSPubServer)pubServer);
      String datasource = getPubServerFilesService().getJndiDsName(dbServer);
      
      DbmsInfo dbmsInfo = new DbmsInfo(); 
      dbmsInfo.m_dbName = dbServer.getDatabase();
      dbmsInfo.m_driverType = dbServer.getDriverType().getDriverName();
      dbmsInfo.m_origin = dbServer.getOwner();
      dbmsInfo.m_resourceName = datasource;
      return dbmsInfo;
   }
   /**
    * Gets the publish server from the supplied item or job.
    * @param item the item to publish, assumed not <code>null</code>
    * @param jobId the job id.
    * @return the publish server. It may be <code>null</code> if not found.
    */
   private IPSPubServer getPubServer(Item item, long jobId)
   {
  
      JobData data = m_jobData.get(jobId);
      if (data.m_pubServer != null)
         return data.m_pubServer;
   
      Long serverId = item.getPubServerId();
      if (serverId == null)
         return null;
      
      return getPubServerDao().findPubServer(serverId);
   }
   
   @Override
   protected IPSDeliveryResult doDelivery(Item item, long jobId, 
         @SuppressWarnings("unused") String location)
   {
      return perform(item, jobId);
   }

   
   @Override
   protected IPSDeliveryResult doRemoval(Item item, long jobId, 
         @SuppressWarnings("unused") String location)
   {
      return perform(item, jobId);
   }
   
   @Override
   public boolean isEmptyLocationAllowed()
   {
      return true;
   }

   /**
    * Lazy load publish server dao.
    * @return the publish server dao, never <code>null</code>.
    */
   private static IPSPubServerDao getPubServerDao()
   {
       if (pubServerDao == null)
           pubServerDao = PSPubServerDaoLocator.getPubServerManager();
       return pubServerDao;
   }
   
   private static IPSDatabasePubServerFilesService getPubServerFilesService()
   {
      if (dbFileService == null)
         dbFileService = PSDatabasePubServerServiceLocator.getPubServerFilesService();
      
      return dbFileService;
   }
   /**
    * Check the connection to the database to publish to.
    */
   @Override
   public boolean checkConnection(IPSPubServer pubServer, IPSSite site)
   {
      PSDatabasePubServer dbPubServer = new PSDatabasePubServer((PSPubServer)pubServer);
      dbPubServer.setServer(pubServer.getPropertyValue("server"));
      String error = getPubServerFilesService().testDatabasePubServer(dbPubServer);                
      if(!isBlank(error))
          return false;
      return true;
      
   }

   private static IPSPubServerDao pubServerDao = null;

   private static IPSDatabasePubServerFilesService dbFileService = null;
   
   /**
    * Logger.
    */
   private static final Logger ms_log = LogManager.getLogger(PSDatabaseDeliveryHandler.class);
}
