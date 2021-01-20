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
package com.percussion.design.objectstore;

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Contains the configuration settings for the cache used by the server.
 */
public class PSServerCacheSettings  extends PSComponent
{

   /**
   * Intializes the default storage and aging settings for the manager. Caching
   * is disabled by default. The defaults storage and aging settings when
   * caching is enabled are:
   * <br>
   * <ol>
   * <li>Memory Usage - 100 MB</li>
   * <li>Disk Usage - 1 GB</li>
   * <li>Page Size - 100 KB</li>
   * <li>Aging Time - Unlimited, never expires(-1)</li>
   * </ol>
   * @see #toXml for more description of the settings.
   *
   */
   public PSServerCacheSettings()
   {

   }

   /**
   * Intializes the storage and aging settings for the manager.  Must be called
   * before the instance of the class is used.
   *
   * @param enabled Is caching enabled for the server?  Provide
   * <code>true</code> if it is to be enabled, <code>false</code> if not.
   * @param folderCacheEnabled Is folder caching enabled for the server?  
   * Provide <code>true</code> if it is to be enabled, <code>false</code> if not.
   * @param memUsage The max amount of memory (in bytes) to use before items are
   * moved to disk or flushed if no space is available.   Objects will be moved
   * or flushed based on a least recently used algorithm.  Supply
   * <code>-1</code> to allow unlimited memory usage.  Must not be less than
   * <code>-1</code>.
   * @param diskUsage The max amount of space (in bytes) to use on disk before
   * forcing the least recently used items to be flushed.  Provide
   * <code>-1</code> to allow unlimited disk usage.  Must not be less than
   * <code>-1</code>.
   * @param pageSize The maximum page size the server should use to allow
   * caching of an item/page that is less than or equal to this. Provide <code>
   * -1</code> to allow item/page of any size to cache, or else a number greater
   * than <code>0</code>.
   * @param agingTime The amount of time (in mins) to allow an object to be idle
   * in the cache before it is flushed. Provide <code>-1</code> to allow
   * unlimited time (never expires), or else a number greater than <code>0
   * </code>.
   */
   public PSServerCacheSettings(boolean enabled, boolean folderCacheEnabled,
         long memUsage, long diskUsage, long pageSize, long agingTime)
   {
      setEnabled( enabled );
      setMaxMemoryUsage( memUsage );
      setMaxDiskUsage( diskUsage );
      setMaxPageSize( pageSize );
      setAgingTime( agingTime );
      m_isFolderCacheEnabled = folderCacheEnabled;
   }


   /**
    * Construct this object from its XML representation.
    *
    * @param source The element to use, may not <code>null</code>.  See
    * {@link #toXml(Document)} for the expected format.
    * @param parentDoc the Java object which is the parent of this object,
    * may be <code>null</code>.
    * @param parentComponents   the parent objects of this object, may be <code>
    * null</code>.
    * @throws PSUnknownNodeTypeException if <code>source</code> is not in
    * the expected format.
    */
   public PSServerCacheSettings(Element source, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if(source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source, parentDoc, parentComponents);
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. The format is:
    * <pre><code>
    * &lt;!--
    *    The settings to use for the cache used by the server.
    *
    *    Attributes:
    *       id - The unique id of this component.
    *       enabled - determines if caching is enabled for the server.  Is
    *          enabled by default.
    *       enabledFolderCache - determines if folder caching is enabled for 
    *          the server.  Is enabled by default.
    *       maxMemory - The maximum amount of memory the server can use to cache
    *          items, in bytes.  Provide "-1" to indicate no limit.
    *       maxDiskSpace - The maximum amount of diskspace the server can use to
    *          cache items, in bytes.  Provide "-1" to indicate no limit.  May
    *          not be "0" if maxMemory is also "0".
    *       maxPageSize - The maximum page size the server can use to cache an
    *          item, in bytes.  Provide "-1" to indicate no limit.
    *          May not be "0".
    *       agingTime - The amount of time, in minutes, that an item can remain
    *          in the cache before it is automatically flushed.  Provide "-1" to
    *          allow items to remain indefinitely.
    * -->
    * &lt;ELEMENT PSXServerCacheSettings (EMPTY)>
    *  &lt;!ATTLIST PSXServerCacheSettings
    *     id ID  #REQUIRED
    *     enabled (yes | no) "yes"
    *     folderCacheEnabled (yes | no) "yes"
    *     maxMemory CDATA #REQUIRED
    *     maxDiskSpace CDATA #REQUIRED
    *     maxPageSize CDATA #REQUIRED
    *     agingTime CDATA #REQUIRED
    *     >
    * </code></pre>
    *
    * @param doc the document used to create element, may not be <code>null
    * </code>
    *
    * @return The newly created XML element node, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(ID_ATTR, String.valueOf(getId()));
      if(isEnabled())
         root.setAttribute(ENABLED_ATTR, "yes");
      else
         root.setAttribute(ENABLED_ATTR, "no");

      if(isFolderCacheEnabled())
         root.setAttribute(FOLDER_CACHE_ENABLED_ATTR, "yes");
      else
         root.setAttribute(FOLDER_CACHE_ENABLED_ATTR, "no");
      
      root.setAttribute(MAX_MEMORY_ATTR, String.valueOf(getMaxMemoryUsage()));
      root.setAttribute(MAX_DISK_ATTR, String.valueOf(getMaxDiskUsage()));
      root.setAttribute(MAX_PAGE_ATTR, String.valueOf(getMaxPageSize()));
      root.setAttribute(AGING_TIME_ATTR, String.valueOf(getAgingTime()));

      return root;
   }

   /**
    * Indicates whether folder cache is enabled or disabled.
    * 
    * @return <code>true</code> if folder cache is enabled; otherwise return
    *    <code>false</code>.
    */
   public boolean isFolderCacheEnabled()
   {
      return m_isFolderCacheEnabled;
   }
   
   /**
    * This method is called to populate this object from its XML representation.
    *
    * @param sourceNode   The XML element node to populate from, not <code>null
    * </code>.  See {@link #toXml(Document)} for the format expected.
    *
    * @param parentDoc The parent document that contains the element, may be
    * <code>null</code>.
    * @param parentComponents a collection of all the components created in
    * the process of creating this component.  May be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is not in
    * the expected format.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException(
            "sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // REQUIRED: get the ID attribute
         data = tree.getElementData(PSComponent.ID_ATTR);
         try
         {
           m_id = Integer.parseInt(data);
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         // REQUIRED: get the fieldSetRef attribute
         data = tree.getElementData(ENABLED_ATTR);
         if (data == null)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               ENABLED_ATTR,
               "null"
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         if(data.equalsIgnoreCase("yes"))
            setEnabled( true );
         else
            setEnabled( false );

         try
         {
            data = tree.getElementData(MAX_MEMORY_ATTR);
            setMaxMemoryUsage( Long.parseLong(data) );
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               MAX_MEMORY_ATTR,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         try
         {
            data = tree.getElementData(MAX_DISK_ATTR);
            setMaxDiskUsage( Long.parseLong(data) );
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               MAX_DISK_ATTR,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         try
         {
            data = tree.getElementData(MAX_PAGE_ATTR);
            setMaxPageSize( Long.parseLong(data) );
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               MAX_PAGE_ATTR,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         try
         {
            data = tree.getElementData(AGING_TIME_ATTR);
            setAgingTime( Long.parseLong(data) );
         }
         catch (Exception e)
         {
            Object[] args =
            {
               XML_NODE_NAME,
               AGING_TIME_ATTR,
               ((data == null) ? "null" : data)
            };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
         
         data = tree.getElementData(FOLDER_CACHE_ENABLED_ATTR);
         if (data == null)
            m_isFolderCacheEnabled = true;
         else
            m_isFolderCacheEnabled = data.equalsIgnoreCase("yes");
         
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Sets the caching property enable/disabled.
    *
    * @param enabled if <code>true</code> sets caching enabled, otherwise
    * disabled.
    */
   private void setEnabled(boolean enabled)
   {
      m_isEnabled = enabled;
   }

   /**
    * Sets the maximum memory usage.
    *
    * @param memUsage memory usage in bytes
    */
   private void setMaxMemoryUsage(long memUsage)
   {
      if(memUsage < -1)
      {
         throw new IllegalArgumentException(
            "Max Memory Usage can not be less than -1.");
      }
      if(memUsage == 0 && m_maxDiskUsage == 0)
      {
         throw new IllegalArgumentException(
            "Both memory and disk usage can not be 0.");
      }
      m_maxMemoryUsage = memUsage;
   }

   /**
    * Sets the maximum disk sapce usage.
    *
    * @param diskUsage disk space usage in bytes
    */
   private void setMaxDiskUsage(long diskUsage)
   {
      if(diskUsage < -1)
      {
         throw new IllegalArgumentException(
            "Max Disk Usage can not be less than -1.");
      }
      if(m_maxMemoryUsage == 0 && diskUsage == 0)
      {
         throw new IllegalArgumentException(
            "Both memory and disk usage can not be 0.");
      }
      m_maxDiskUsage = diskUsage;
   }

   /**
    * Sets the maximum page size.
    *
    * @param pageSize maximum page size in bytes
    */
   private void setMaxPageSize(long pageSize)
   {
      if(pageSize == 0 || pageSize < -1)
      {
         throw new IllegalArgumentException(
            "Max Page Size can not be less than -1 or equal to 0.");
      }
      m_maxPageSize = pageSize;
   }

   /**
    * Sets the cache aging time.
    *
    * @param agingTime aging time in min
    */
   private void setAgingTime(long agingTime)
   {
      if(agingTime == 0 || agingTime < -1)
      {
         throw new IllegalArgumentException(
            "Aging time can not be equal to 0 or less than -1.");
      }
      m_agingTime = agingTime;
   }

   /**
    * Performs a shallow copy of the data in the supplied
    * <code>PSServerCacheSettings</code> to this object.
    *
    * @param c A valid <code>PSServerCacheSettings</code> object. Cannot be
    * <code>null</code>.
    */
   public void copyFrom(PSServerCacheSettings c)
   {
      try {
         super.copyFrom(c);
      }
      catch(IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
      m_isEnabled = c.isEnabled();
      m_maxMemoryUsage = c.getMaxMemoryUsage();
      m_maxDiskUsage = c.getMaxDiskUsage();
      m_agingTime = c.getAgingTime();
      m_maxPageSize = c.getMaxPageSize();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSServerCacheSettings)) return false;
      if (!super.equals(o)) return false;
      PSServerCacheSettings that = (PSServerCacheSettings) o;
      return m_isEnabled == that.m_isEnabled &&
              m_isFolderCacheEnabled == that.m_isFolderCacheEnabled &&
              m_maxMemoryUsage == that.m_maxMemoryUsage &&
              m_maxDiskUsage == that.m_maxDiskUsage &&
              m_maxPageSize == that.m_maxPageSize &&
              m_agingTime == that.m_agingTime;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_isEnabled, m_isFolderCacheEnabled, m_maxMemoryUsage, m_maxDiskUsage, m_maxPageSize, m_agingTime);
   }

   /**
    * Determines if caching is enabled for the server.
    *
    * @return <code>true</code> if it is enabled, <code>false</code> otherwise.
    */
   public boolean isEnabled()
   {
      return m_isEnabled;
   }

   /**
    * Gets the maximum amount of memory the sever should use for caching items.
    *
    * @return The amount in bytes, <code>-1</code> to indicate unlimited use,
    * or else a number greater than or equal to <code>0</code>.
    */
   public long getMaxMemoryUsage()
   {
      return m_maxMemoryUsage;
   }

   /**
    * Gets the maximum amount of disk space sever should use for caching items.
    *
    * @return The amount in bytes, <code>-1</code> to indicate unlimited space,
    * or else a number greater than or equal to <code>0</code>.
    */
   public long getMaxDiskUsage()
   {
      return m_maxDiskUsage;
   }

   /**
    * Gets the maximum page size that the sever should use for caching an item.
    * If the item/page size exceeds this size that will not be cached.
    *
    * @return The amount in bytes, <code>-1</code> to indicate unlimited size,
    * or else a number greater than or equal to <code>0</code>.
    */
   public long getMaxPageSize()
   {
      return m_maxPageSize;
   }

   /**
    * Gets the time after which items cached by the server should expire.
    *
    * @return The time in minutes, <code>-1</code> to indicate that it never
    * expires, or else a number greater than <code>0</code>.
    */
   public long getAgingTime()
   {
      return m_agingTime;
   }

   /**
    * The flag to determine whether the caching is enabled/disabled, gets
    * initialized when the instance is created.
    */
   private boolean m_isEnabled = false;

   /**
    * Indicates whether the folder cache is enabled or disabled. The folder 
    * cache is enabled by default.
    */
   private boolean m_isFolderCacheEnabled = true;
   
   /**
    * The maximum amount of memory (in bytes), the sever should use for caching
    * items, gets initialized when the instance is created.
    */
   private long m_maxMemoryUsage = DEFAULT_MEM_USAGE;

   /**
    * The maximum amount of disk space (in bytes) the sever should use for
    * caching items, gets initialized when the instance is created.
    */
   private long m_maxDiskUsage = DEFAULT_DISK_USAGE;

   /**
    * The maximum page size (in bytes) the server should use for caching an
    * item, gets initialized when the instance is created.
    */
   private long m_maxPageSize = DEFAULT_PAGE_SIZE;

   /**
    * The time (in mins) after which items cached by the server should expire,
    * gets initialized when the instance is created.
    */
   private long m_agingTime = -1;

   /**
    * Default setting for unlimited aging time (for cache not to expire).
    */
   public final static long DEFAULT_AGING_TIME = 60;

   /**
    * Default setting for maximum disk usage in bytes. (1 GB)
    */
   public final static long DEFAULT_DISK_USAGE = 1073741824;

   /**
    * Default setting for maximum memory usage in bytes. (100 MB)
    */
   public final static long DEFAULT_MEM_USAGE = 104857600;

   /**
    * Default setting for maximum page size in bytes that can be cached(100 KB).
    */
   public final static long DEFAULT_PAGE_SIZE = 102400;

   /**
    * The xml node to represent this object.
    */
   public final static String XML_NODE_NAME = "PSXServerCacheSettings";

   /**
    * The xml attribute names of the node representing this object.
    */
   public final static String ENABLED_ATTR = "enabled";
   public final static String FOLDER_CACHE_ENABLED_ATTR = "folderCacheEnabled";
   public final static String MAX_MEMORY_ATTR = "maxMemory";
   public final static String MAX_DISK_ATTR = "maxDiskSpace";
   public final static String MAX_PAGE_ATTR = "maxPageSize";
   public final static String AGING_TIME_ATTR = "agingTime";
}
