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

package com.percussion.server.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;

import com.percussion.server.PSServer;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

/**
 * Parses ehcache.xml and updates the file using a result of SELECT COUNT(*)
 * results from specific tables defined in {@link PSAutotuneCacheRelationships}.
 * 
 * <br/>
 * 
 * @author chriswright
 *
 */
public class PSAutotuneCache
{
   /**
    * Default ctor.
    */
   private PSAutotuneCache()
   {
      // Spring
   }

   /**
    * Creates a document builder to parse the ehcache.xml file. From here, it
    * parses the document based on each 'cache' element. We get the
    * maxElementsInMemory for each cache field, and if that cache element is
    * present in the keys map, update it to match the result of the SELECT
    * COUNT(*) result increased by 10%.
    */
   public void updateEhcache() throws Exception
   {
      init();

      long spaceRequiredLgTables = calcSpaceForLargeTables();
      long spaceRequiredSmTables = calcSpaceForSmallerTables();
      long percentageOfHeapForCache = (long) (percentage * freeMemory);

      log.debug("The maximum space (in ehcache) required for all total tables in MB is: "
            + (DF2.format((double)(spaceRequiredLgTables + spaceRequiredSmTables) / KB_TO_MB)));

      log.debug("The amount of memory to be allocated to the ehcache in MB is: " + (percentageOfHeapForCache / BYTES_TO_MB));

      FileInputStream in = new FileInputStream(ehcache);
      Document doc = Jsoup.parse(in, "UTF-8", "", Parser.xmlParser());
      List<Element> cacheElems = doc.getElementsByTag("cache");
      for (Element cache : cacheElems)
      {
         String cacheName = cache.attr("name");
         if (ehcacheDbRowCountValues.containsKey(cacheName) && !largeTables.containsKey(cacheName))
         {
            if (percentageOfHeapForCache > 0)
               percentageOfHeapForCache = updateCacheItem(cacheName, cache, percentageOfHeapForCache);
         }
         if(cacheName.equals("item") || cacheName.equals("childitem")){
            //We want to adjust these to be virtually unlimited but with a TTL
            cache.attr(MAX_ELEMS_IN_MEMORY, "1000000");
            cache.attr("eternal","true");
            cache.attr("memoryStoreEvictionPolicy","LRU");
            
         }
      }

      // process large tables here
      for (String name : largeTableNames)
      {
         List<Element> elem = doc.getElementsByAttributeValue("name", name);
         if (ehcacheDbRowCountValues.containsKey(name) && !smallTables.containsKey(name))
         {
            if (percentageOfHeapForCache > 0)
               percentageOfHeapForCache = updateCacheItem(name, elem.get(0), percentageOfHeapForCache);
         }
      }

      writeEhCache(doc);
      
      if (in != null) { try { in.close(); } catch (IOException e) {} }

      log.debug("The presumed amount of free space after allocating for the ehcache in MB is: "
            + DF2.format(((double)this.freeMemory - percentageOfHeapForCache) / (BYTES_TO_MB)));
      log.info("The cache has been autotuned.");
   }

   public void init() throws Exception
   {
      Properties serverProps = PSServer.getServerProps();
      percentage = Integer.parseInt(serverProps.getProperty("autotuneCachePercentage", "40"));
      percentage /= 100.0;
      log.debug("The ehcache percentage to be used is: " + percentage);
      
      loadEhCacheFile();
      backupEhCache();
      setHeapSizes();
      getDatabaseCountValues();
   }

   /**
    * Writes the values ehcache.xml file.
    * @param doc - the processed document that needs writing.
    */
   private void writeEhCache(Document doc)
   {
      try
      {
         FileUtils.writeStringToFile(ehcache, doc.toString(), StandardCharsets.UTF_8);
      }
      catch (IOException e)
      {
         log.error("Error writing to ehcache.xml", e);
      }
   }

   private long updateCacheItem(String cacheName, Element elem, long percentageOfHeapForCache)
   {
      double regionRowCount = ehcacheDbRowCountValues.get(cacheName);
      Node cache = (Node) elem;
      double bytesToSubtract = 0.0;
      String maxElemValue = cache.attr(MAX_ELEMS_IN_MEMORY);

      log.debug("The cache element name is: " + cacheName);
      log.debug("The cache element value is: " + maxElemValue);
      log.debug("The current count from database for this table is: " + regionRowCount);

      regionRowCount += (regionRowCount * .10);

      log.debug("The projected new value after calculation will be: " + regionRowCount);

      if (regionRowCount > Integer.parseInt(maxElemValue) && regionRowCount != 0)
      {
         log.debug(MAX_ELEMS_IN_MEMORY + " is being updated as it contains a higher value: "
               + cacheName);
         String count = Double.toString(regionRowCount);
         cache.attr(MAX_ELEMS_IN_MEMORY, count);
      }
      
      if (largeTables.containsKey(cacheName)) {
         bytesToSubtract = Math.ceil((ehcacheDbRowCountValues.get(cacheName) * largeTables.get(cacheName)));
         return percentageOfHeapForCache - (long) bytesToSubtract;
      }
      
      bytesToSubtract = Math.ceil((ehcacheDbRowCountValues.get(cacheName) * smallTables.get(cacheName)));
      return percentageOfHeapForCache - (long) bytesToSubtract;
   }

   /**
    * Loads the ehcache.xml file.
    */
   private void loadEhCacheFile()
   {
      ClassLoader loader = PSAutotuneCache.class.getClassLoader();
      ehcache = new File(loader.getResource("/ehcache.xml").getFile());
   }

   /**
    * Calculates the MAX amount of MB that will be required
    * to hold all large table data.
    * 
    * @return the number of MB to hold all large table data.
    */
   private long calcSpaceForLargeTables()
   {
      long kb = 0;
      for (String key : largeTables.keySet())
      {
         kb += ehcacheDbRowCountValues.get(key) * largeTables.get(key);
      }
      log.debug("The current amount of MB allocated for entries in the cache for larger tables is: " + DF2.format((double) kb / KB_TO_MB));
      return kb;
   }

   /**
    * Iterates through the smallTables map and the result of
    * counts from corresponding rows in the db to determine
    * how many MB are allocated at maximum for each cache region.
    * 
    * @return a representation in bytes of how much space the small
    *         tables will consume.
    */
   private long calcSpaceForSmallerTables()
   {
      long kb = 0;
      for (String key : smallTables.keySet())
      {
         kb += ehcacheDbRowCountValues.get(key) * smallTables.get(key);
      }
      log.debug("The current amount of MB allocated for entries in " + "the cache for smaller tables are: " + DF2.format((double) kb / KB_TO_MB));
      return kb;
   }

   /**
    * Updates the HashMap with results of the SELECT COUNT(*)'s against the
    * database. This maps the count results to the appropriate ehcache.xml
    * fields.
    */
   private void getDatabaseCountValues() throws Exception
   {
      Connection conn = null;
      ResultSet resultSet = null;
      PreparedStatement stmt1 = null;
      try
      {
         conn = PSConnectionHelper.getDbConnection();
         PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();
         for (Entry<String, String> entry : cacheRelationships.entrySet())
         {
            if(PSSqlHelper.isExistingCMSTableName(entry.getKey(),false)){
               String table = PSSqlHelper.qualifyTableName(entry.getKey(), detail.getDatabase(), detail.getOrigin(),
                       detail.getDriver());
               String stmt = "SELECT COUNT(*) FROM " + table;

               stmt1 = PSPreparedStatement.getPreparedStatement(conn, stmt);
               resultSet = stmt1.executeQuery();

               resultSet.next();
               ehcacheDbRowCountValues.put(entry.getValue(), resultSet.getInt(1));
         }else{
               log.error("Skipping table " + entry.getKey() + " as it appears to be invalid.");
         }
         }
      }
      finally
      {
         if (resultSet != null)
            try
            {
               resultSet.close();
            }
            catch (Exception e)
            {
            }
         if (conn != null)
            try
            {
               conn.close();
            }
            catch (Exception e)
            {
            }
         if (stmt1 != null)
            try
            {
               stmt1.close();
            }
            catch (SQLException e)
            {
            }
      }
   }

   /**
    * Backs up the ehcache.xml file.
    *
    */
   private void backupEhCache()
   {
      FileInputStream input = null;
      FileOutputStream output = null;
      try
      {
         input = new FileInputStream(ehcache);
         File temp = File.createTempFile("ehcache", ".xml", ehcache.getParentFile());
         output = new FileOutputStream(temp);
         IOUtils.copy(input, output);
         log.info("ehcache.xml file has been backed up as " + temp.getAbsolutePath());
      }
      catch (NullPointerException e)
      {
         log.error(BACKUP_ERROR, e);
      }
      catch (FileNotFoundException e)
      {
         log.error(BACKUP_ERROR, e);
      }
      catch (IOException e)
      {
         log.error(BACKUP_ERROR, e);
      }
      finally
      {
         if (input != null)
            try
            {
               input.close();
            }
            catch (IOException e)
            {
            }
         if (output != null)
            try
            {
               output.close();
            }
            catch (IOException e)
            {
            }
      }
   }

   /**
    * Sets the memory variables for use in later calculations.
    */
   private void setHeapSizes()
   {
      this.heapSize = Runtime.getRuntime().totalMemory();
      this.heapMaxSize = Runtime.getRuntime().maxMemory();
      this.heapFreeSize = Runtime.getRuntime().freeMemory();
      this.freeMemory = this.heapMaxSize - (this.heapSize - this.heapFreeSize);

      log.debug(String.format("\n%s%d\n%s%d\n%s%d\n%s%d",
            "The heap size in MB is:", (this.heapSize / BYTES_TO_MB),
            "The heap max size in MB is: ", (this.heapMaxSize / BYTES_TO_MB),
            "The heap free size in MB is: ", (this.heapFreeSize / BYTES_TO_MB),
            "The amount of presumable free memory in MB is: ", (this.freeMemory / BYTES_TO_MB)));
   }

   /**
    * Spring property accessor.
    *
    * @return get the cache service
    */
   public IPSCacheAccess getCacheAccessor()
   {
      return m_cacheAccessor;
   }

   /**
    * Set the cache service.
    *
    * @param cache the service, never <code>null</code>
    */
   public void setCacheAccessor(IPSCacheAccess cache)
   {
      if (cache == null)
      {
         throw new IllegalArgumentException("cache may not be null");
      }
      m_cacheAccessor = cache;
   }

   /**
    * The total amount of memory currently available for current and future
    * objects, measured in bytes. Corresponds to the amount of memory CURRENTLY
    * available to the JVM.
    */
   private long heapSize;

   /**
    * The maximum amount of memory that the virtual machine will attempt to use,
    * measured in bytes. Exceeding this will result in OutOfMemory exception.
    */
   private long heapMaxSize;

   /**
    * An approximation to the total amount of memory currently available for
    * future allocated objects, measured in bytes.
    */
   private long heapFreeSize;

   /**
    * The amount of presumable free memory.
    */
   private long freeMemory;

   /**
    * Error to use when backing up ehcache.xml.
    */
   private static final String BACKUP_ERROR = "Error backing up ehcache.xml document.";

   /**
    * maxElementsInMemory cache setting in ehcache.xml.
    */
   private static final String MAX_ELEMS_IN_MEMORY = "maxElementsInMemory";

   /**
    * Maintains file reference to ehcache.xml.
    */
   private File ehcache = null;

   /**
    * Contains key/value pair for relationship between a table name and it's
    * region name in ehcache.
    */
   private static final Map<String, String> cacheRelationships = PSAutotuneCacheRelationships.DBTABLES_AND_CACHEFIELDS;

   /**
    * Contains a key/value pair relationship between the tables that get very
    * large in the database and their equivalent region associations in ehcache.
    */
   private static final Map<String, Double> largeTables = PSAutotuneCacheRelationships.LARGE_TABLES;

   /**
    * Contains a key/value pair relationship between the tables that don't get
    * as large and their equivalent region associations in ehcache.
    */
   private static final Map<String, Double> smallTables = PSAutotuneCacheRelationships.SMALL_TABLES;

   /**
    * Names of the large tables to be iterated in order of how they should be processed.
    */
   private static final String[] largeTableNames = PSAutotuneCacheRelationships.LARGE_TABLE_NAMES;

   /**
    * Maintains relationships between the ehcache region name and the
    * corresponding SELECT COUNT(*) result from the database.
    */
   private HashMap<String, Integer> ehcacheDbRowCountValues = new HashMap<String, Integer>();

   /**
    * Access to the cache manager.
    */
   private IPSCacheAccess m_cacheAccessor;
   
   /**
    * The percentage the ehcache is allowed to consume.
    */
   private double percentage = 40.0;
   
   /**
    * Convert bytes to MB
    */
   private static final long BYTES_TO_MB = 1024L * 1024L;
   
   /**
    * Convert kilobytes to MB
    */
   private static final long KB_TO_MB = 1024L;
   
   /**
    * Round decimals to two places.
    */
   private static final DecimalFormat DF2 =  new DecimalFormat(".#");

   /**
    * Logger.
    */
   public static final Logger log = Logger.getLogger(PSAutotuneCache.class.getName());
}
