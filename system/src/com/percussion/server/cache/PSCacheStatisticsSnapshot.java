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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.server.cache;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides the following statistics based on cache usage:
 * <ul>
 * <li>Hit rate (hits/total requests) * 100</li>
 * <li>Total hits (since caching last enabled)</li>
 * <li>Total memory used</li>
 * <li>Total disk space used</li>
 * <li>Average size of an item</li>
 * <li>Total number of items in cache</li>
 * <li>Disk hit rate (# of hits retrieved from disk/ total # of hits)</li>
 * </ul>
 */
public class PSCacheStatisticsSnapshot
{
   /**
    * Creates a snapshot of the usage statistics.
    *
    * @param diskHits the number of times an item is retrieved from disk, may
    * not be less than <code>0</code>.
    * @param diskItems the number of items in disk, may not be less than <code>0
    * </code>.
    * @param diskUage the amount of disk space used in bytes, may not be less 
    * than <code>0</code>.
    * @param memItems the number of items in memory, may not be less than <code>
    * 0</code>.
    * @param memUsage the amount of memory used in bytes, may not be less than 
    * <code>0</code>.
    * @param misses the number of attempts failed to retrieve an item from cache,
    * may not be less than <code>0</code>
    * @param totalHits the total number of times an item is retrieved either
    * from memory or disk, may not be less than <code>0</code>.
    */
   public PSCacheStatisticsSnapshot(long diskHits, long diskItems, long diskUsage,
      long memItems, long memUsage, long misses, long totalHits)
   {
      if(diskHits < 0)
         throw new IllegalArgumentException("diskHits may not be < 0");

      if(diskItems < 0)
         throw new IllegalArgumentException("diskItems may not be < 0");

      if(diskUsage < 0)
         throw new IllegalArgumentException("diskUage may not be < 0");

      if(memItems < 0)
         throw new IllegalArgumentException("memItems may not be < 0");

      if(memUsage < 0)
         throw new IllegalArgumentException("memUsage may not be < 0");

      if(misses < 0)
         throw new IllegalArgumentException("misses may not be < 0");

      if(totalHits < 0)
         throw new IllegalArgumentException("totalHits may not be < 0");

      m_diskUsage = diskUsage;
      m_memUsage = memUsage;
      m_totalHits = totalHits;
      m_totalRequests = totalHits + misses;
      m_totalItems = memItems + diskItems;
      if(m_totalItems != 0)
      {
         m_avgItemHits = totalHits/m_totalItems;
         m_avgItemSize = (memUsage + diskUsage)/m_totalItems;
      }
      if(totalHits != 0)
      {
         m_diskHitRate = (int)((diskHits * 100)/totalHits);
         m_hitRate = (int)((totalHits * 100)/m_totalRequests);
      }
   }

   /**
    * Get the name of the cache statistics.
    * @return the name of the statistics, never <code>null</code>, may be empty
    *    if the name has not been set.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Set a supplied name for the cache statistics.
    * @param name the new name, never <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty.");
      
      m_name = name;
   }
   
   /**
    * Gets the number of hits divided by the total number of attempts
    * <code>(hits / hits + misses) * 100</code>.
    *
    * @return The hit rate as a percentage of total attempts, never less than
    * <code>0</code>.
    */
   public int getHitRate()
   {
      return m_hitRate;
   }

   /**
    * Gets the number of successful attempts to retrieve an item from the cache.
    *
    * @return The total hit count, never less than <code>0</code>.
    */
   public long getTotalHits()
   {
      return m_totalHits;
   }
   
   /**
    * Gets the number of total attempts/requests to retrieve an item from the 
    * cache.
    *
    * @return The total attempts count, never less than <code>0</code>.
    */
   public long getTotalRequests()
   {
      return m_totalRequests;
   }

   /**
    * Gets the amount of memory (in bytes) the items in the cache are using. 
    * Does not include the overhead involved in maintaing the metadata regarding
    * the storage of the item.
    *
    * @return The amount of memory in bytes, never less than <code>0</code>.
    */
   public long getMemoryUsage()
   {
      return m_memUsage;
   }

   /**
    * Gets the amount of space (in bytes) the items in the cache are using on 
    * disk.
    *
    * @return The amount of disk space in bytes, never less than <code>0</code>.
    */
   public long getDiskUsage()
   {
      return m_diskUsage;
   }

   /**
    * Gets the average size (in bytes) of all of the objects in the cache. This 
    * is the sum of the size of each object in the cache (in memory or on disk)
    * divided by the number of items in the cache.
    *
    * @return The average item size, never less than <code>0</code>.
    */
   public long getAvgItemSize()
   {
      return m_avgItemSize;
   }   
   
   /**
    * Gets the total number of items in cache.
    * 
    * @return the total number of items, never less than <code>0</code>.
    */
   public long getTotalItems()
   {
      return m_totalItems;
   }   

   /**
    * Gets the (# of hits retrieved from disk / total # of hits) * 100
    *
    * @return The disk hit rate as a percentage of total hits.
    */
   public int getDiskHitRate()
   {
      return m_diskHitRate;
   }
   
   
   /**
    * Gets the XML representation of this object. The structure of the element 
    * is as follows:
     * <PRE><CODE>
    *    &lt;--
    *       The cache statistics element with each attribute referring to
    *       each kind of statistics.
    *      --&gt;
    *      &lt;ELEMENT CacheStatistics (totalHits, totalRequests, hitRate, 
    *       diskHitRate, memoryUsage, diskUsage, totalItems, AverageItemSize)
    *     &gt;    *     
    *    &lt;--
    *       totalHits - Total number of successful attempts to access an item 
    *       from the cache.
    *      --&gt;
    *    &lt;ELEMENT totalHits (#PCDATA)&gt;
    *    &lt;--
    *       totalRequests - Total number of attempts to access an item from the 
    *       cache.
    *      --&gt;
    *    &lt;ELEMENT totalRequests (#PCDATA)&gt;
    *    &lt;--
    *       hitRate - The hit rate as a percentage of total attempts.
    *      --&gt;
    *    &lt;ELEMENT hitRate (#PCDATA)&gt; 
    *    &lt;--
    *       diskHitRate - The disk hit rate as a percentage of total hits.
    *      --&gt;
    *    &lt;ELEMENT diskHitRate (#PCDATA)&gt;
    *    &lt;--
    *       memoryUsage - Total memory used by cached items.
    *      --&gt;
    *    &lt;ELEMENT memoryUsage (#PCDATA)&gt;
    *    &lt;--
    *       diskUsage - Total disk space used by cached items.          
    *      --&gt;
    *    &lt;ELEMENT diskUsage (#PCDATA)&gt;
    *    &lt;--
    *       totalItems - The total number of times in the cache.
    *      --&gt;
    *    &lt;ELEMENT totalItems (#PCDATA)&gt;
    *    &lt;--
    *       AverageItemSize - The average cached item size.
    *      --&gt;
    *    &lt;ELEMENT AverageItemSize (#PCDATA)&gt;
    * </CODE></PRE>
    * 
    * @param doc the document to use to create the element, may not be <code>
    * null</code>
    * 
    * @return the xml element representing cache statistics, never 
    * <code>null</code>
    * 
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element el = doc.createElement("CacheStatistics");
      if (!StringUtils.isBlank(m_name))
         el.setAttribute("name", m_name);
      
      PSXmlDocumentBuilder.addElement( doc, el, "totalHits", 
         String.valueOf(getTotalHits()) );
      PSXmlDocumentBuilder.addElement( doc, el, "totalRequests", 
         String.valueOf(getTotalRequests()) );      
      PSXmlDocumentBuilder.addElement( doc, el, "hitRate", 
         String.valueOf(getHitRate()) + "%");     
      PSXmlDocumentBuilder.addElement( doc, el, "diskHitRate", 
         String.valueOf(getDiskHitRate()) + "%");              
      PSXmlDocumentBuilder.addElement( doc, el, "memoryUsage", 
         getProperUnitInBytes(getMemoryUsage()) );
      PSXmlDocumentBuilder.addElement( doc, el, "diskUsage", 
         getProperUnitInBytes(getDiskUsage()) );                    
      PSXmlDocumentBuilder.addElement( doc, el, "totalItems", 
         String.valueOf(getTotalItems()) );              
      PSXmlDocumentBuilder.addElement( doc, el, "AverageItemSize", 
         getProperUnitInBytes(getAvgItemSize()) );                   

      return el;
   }
   
   /**
    * Gets the proper unit to the supplied <code>size</code> of bytes and the 
    * returned value is converted to its unit (bytes/KB/MB).
    *
    * The following specifies the returned value:
    * <table border=1>
    * <tr><th>condition</th><th>return value</th></tr>
    * <tr><td> size < 1 KB </td><td> size in bytes</td></tr>
    * <tr><td> 1 KB <= size < 1 MB </td><td> converted size to KB rounded with 
    * scale '1' </td></tr>
    * <tr><td> 1 MB <= size </td><td> converted size to MB rounded with scale 
    * '1'</td></tr>
    * 
    * @param size the size to get the proper unit
    * 
    * @return the converted size with proper unit appended as a <code>String
    * </code>, never <code>null</code> or empty.
    */
   public static String getProperUnitInBytes( long size)
   {
      String sizeInProperUnit = String.valueOf( size );
      String unit = "bytes";
      
      if(size != 0)
      {
         double mbSize = (double)size/MEG_TO_BYTE;
         if(mbSize < 1)
         {
            double kbSize = (double)size/KILO_TO_BYTE;
            if(kbSize > 1)
            {
               sizeInProperUnit = getRoundedValue( kbSize );
               unit = "KB";               
            }
         }
         else
         {
            sizeInProperUnit = getRoundedValue( mbSize );
            unit = "MB";           
         }
      }
      
      return sizeInProperUnit + " " + unit;
   }
   
   /**
    * Gets rounded value up to a single digit after decimal point rounded to its
    * nearest neighbor or up in case of both neighbors are equidistant.
    * 
    * @param size the value to be rounded
    * 
    * @return the rounded value as a <code>String</code>, never <code>null
    * </code> or empty.
    */
   private static String getRoundedValue(double size)
   {
      return  (new BigDecimal(size)).setScale(
         1, BigDecimal.ROUND_HALF_UP).toString();
   }

   /**
    * The average number of times each item has been accessed from the
    * cache. Initialized in ctor and never modified after that.
    */
   private long m_avgItemHits;

   /**
    * The average size (in bytes) of all of the items in the cache. Initialized 
    * in ctor and never modified after that.
    */
   private long m_avgItemSize;

   /**
    * The disk hit rate as a percentage of total hits to retrieve an item from
    * cache. Initialized in ctor and never modified after that.
    */
   private int m_diskHitRate;

   /**
    * The amount of space (in bytes) the items in the cache are using on disk. 
    * Initialized in ctor and never modified after that.
    */
   private long m_diskUsage;

   /**
    * The successful hit rate as a percentage of total attempts. Initialized in
    * ctor and never modified after that.
    */
   private int m_hitRate;

   /**
    * The amount of memory (in bytes) the items in the cache are using. Does not
    * include the overhead involved in maintaing the metadata regarding the
    * storage of the item. Initialized in ctor and never modified after that.
    */
   private long m_memUsage;

   /**
    * The number of successful attempts to retrieve an item from the cache.
    * Initialized in ctor and never modified after that.
    */
   private long m_totalHits;
   
   /**
    * The number of attempts/requests to retrieve an item from the cache.
    * Initialized in ctor and never modified after that.
    */
   private long m_totalRequests;
   
   /**
    * The total number of items in the cache. Initialized in ctor and never 
    * modified after that.
    */
   private long m_totalItems;
   
   /**
    * The constant used to convert bytes to mega bytes or viceversa.
    */
   private static final double MEG_TO_BYTE = 1024*1024;   
   
   /**
    * The constant used to convert bytes to kilo bytes or viceversa.
    */
   private static final double KILO_TO_BYTE = 1024;
   
   /**
    * The name of the cache. It may be the region name for EhCache. It defaults
    * to empty, never <code>null</code>.
    */
   private String m_name = "";
}
