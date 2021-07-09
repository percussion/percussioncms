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
package com.percussion.server.command;

import com.percussion.design.objectstore.PSServerCacheSettings;
import com.percussion.server.cache.PSCacheException;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.cache.PSCacheStatisticsSnapshot;
import com.percussion.services.memory.IPSCacheAccess;
import com.percussion.services.memory.PSCacheAccessLocator;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSConsoleCommandCache abstract class is the base for all cache console
 * command handlers.
 */
public abstract class PSConsoleCommandCache extends PSConsoleCommand
{
   /**
    * The constructor for this class.
    *
    * @param cmdArgs the argument string to use when executing this command, may
    * be <code>null</code> or empty.
    */
   PSConsoleCommandCache(String cmdArgs)
   {
      super(cmdArgs);
   }

   /**
    * Sets the cache debug logging enabled/disabled.
    *
    * @param enable if <code>true</code> enables logging debug messages to
    * console, otherwise disables.
    */
   protected void setCacheDebugLogging( boolean enable)
   {
      PSCacheManager cacheManager = PSCacheManager.getInstance();
      cacheManager.setIsDebugLoggingEnabled( enable );
   }

   /**
    * Flushes the cache.
    *
    * @param keys the cache keys to be used for flushing, may be <code>null
    * </code>
    */
   protected void flushCache(Map keys)
   {
      PSCacheManager cacheManager = PSCacheManager.getInstance();

      if(keys == null)
         cacheManager.flush();
      else
         cacheManager.flush(keys);
   }

   /**
    * Starts caching.
    *
    * @return <code>false</code> if the caching is already started, otherwise
    * <code>true</code>
    *
    * @throws IllegalStateException if the cache manager is not yet inited.
    *
    * @throws clause for PSCacheException
    */
   protected boolean startCache() throws PSCacheException
   {
      return startStopCache( true );
   }

   /**
    * Stops caching.
    *
    * @return <code>false</code> if the caching is already stopped, otherwise
    * <code>true</code>
    *
    * @throws IllegalStateException if the cache manager is not yet inited.
    *
    * @throws clause for PSCacheException
    */
   protected boolean stopCache() throws PSCacheException
   {
      return startStopCache( false );
   }

   /**
    * Starts or stops caching.
    *
    * @param start supply <code>true</code> to start caching and <code>false
    * </code> to stop caching.
    *
    * @return  <code>false</code> if the caching is already started/stopped and
    * we are trying to start/stop, otherwise <code>true</code>
    *
    * missing @throws clause for PSCacheException
    */
   private boolean startStopCache(boolean start) throws PSCacheException
   {
      PSCacheManager cacheManager = PSCacheManager.getInstance();
      PSServerCacheSettings cacheSettings =
         cacheManager.getServerCacheSettings();

      if( (start && cacheManager.hasCacheStarted()) ||  //checks for start command
         !(start || cacheManager.hasCacheStarted()) )   //checks for stop command
      {
         return false;
      }

      cacheManager.init( new PSServerCacheSettings(
         start, 
         cacheSettings.isFolderCacheEnabled(), 
         cacheSettings.getMaxMemoryUsage(),
         cacheSettings.getMaxDiskUsage(), 
         cacheSettings.getMaxPageSize(),
         cacheSettings.getAgingTime() ) );
      return true;

   }

   /**
    * Restarts the cache.
    *
    * @throws IllegalStateException if the cache manager is not yet inited.
    *
    * @throws clause for PSCacheException
    */
   protected void restartCache() throws PSCacheException
   {
      PSCacheManager cacheManager = PSCacheManager.getInstance();
      PSServerCacheSettings cacheSettings =
         cacheManager.getServerCacheSettings();

      cacheManager.init(new PSServerCacheSettings(true, 
            cacheSettings.isFolderCacheEnabled(), 
            cacheSettings.getMaxMemoryUsage(),
            cacheSettings.getMaxDiskUsage(), 
            cacheSettings.getMaxPageSize(),
            cacheSettings.getAgingTime()));
   }

   /**
    * Creates the cache statistics element. See {@link
    * com.percussion.server.cache.PSCacheStatisticsSnapshot#toXml() toXml} for
    * the structure of the element.

    * @param doc the document to use to create the element, may not be <code>
    * null</code>
    *
    * @return the XML element representing cache statistics, never
    * <code>null</code>
    *
    * @throws IllegalArgumentException if doc is <code>null</code>
    * @throws IllegalStateException if the cache manager is not yet initialized.
    */
   static Element getCacheStatistics(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element legacyCache = getLegacyCacheStatistics(doc);
      Element ehCacheStatistics = getEhCacheStatistics(doc);
      
      Element root = doc.createElement("LazyLoadCacheStatistics");
      
      root.appendChild(ehCacheStatistics);
      root.appendChild(legacyCache);
      
      root.setAttribute("size", "" + root.getChildNodes().getLength());
      return root;
   }
   
   /**
    * Get the assembly and resource cache statistics.
    * 
    * @param doc the document, use to create the returned element, assumed
    *    not <code>null</code>.
    *    
    * @return the XML representation of the statistics, never <code>null</code>.
    */
   private static Element getLegacyCacheStatistics(Document doc)
   {
      PSCacheManager cacheManager = PSCacheManager.getInstance();
      PSCacheStatisticsSnapshot cacheStatistics =
         cacheManager.getStatisticsSnapShot();
      Element parent = doc.createElement("AssemblyResourceCacheStatistics");
      parent.appendChild(cacheStatistics.toXml( doc ));
      
      parent.setAttribute("size", "" + parent.getChildNodes().getLength());
      return parent;
   }

   /**
    * Get the EhCache statistics.
    * 
    * @param doc the document used to create the returned object.
    *    Assumed not <code>null</code>.
    * 
    * @return the XML representation of the cache statistics, 
    *    never <code>null</code>. 
    */
   private static Element getEhCacheStatistics(Document doc)
   {
      Element parentEl = doc.createElement("EhCacheStatistics");
      IPSCacheAccess cache = PSCacheAccessLocator.getCacheAccess();      
      List<PSCacheStatisticsSnapshot> statList = cache.getStatistics();

      long totalItems = 0;
      long memUsage = 0;
      long diskUsage = 0;
      
      // convert statistics to XML
      for (PSCacheStatisticsSnapshot stat : statList)
      {
         memUsage += stat.getMemoryUsage();
         diskUsage += stat.getDiskUsage();
         totalItems += stat.getTotalItems();
         
         parentEl.appendChild( stat.toXml(doc));
      }

      parentEl.setAttribute("size", "" + statList.size());
      parentEl.setAttribute("memoryUsage", 
            PSCacheStatisticsSnapshot.getProperUnitInBytes(memUsage));
      parentEl.setAttribute("diskUsage",   
            PSCacheStatisticsSnapshot.getProperUnitInBytes(diskUsage));
      parentEl.setAttribute("totalItems",  totalItems + "");
      
      return parentEl;
   }
   
   /**
    * Gets the basic result document. The structure of the document is as
    * follows.
    * <PRE><CODE>
    *      &lt;ELEMENT PSXConsoleCommandResults   (command)&gt;
    *
    *      &lt;--
    *         the command that was executed
    *      --&gt;
    *      &lt;ELEMENT command (#PCDATA)&gt;
    * </CODE></PRE>
    *
    * @return the result document, never <code>null</code>
    */
   protected Document getResultsDocument()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", getCommandName() +
         " " + m_cmdArgs);
      return doc;
   }

   /**
    * Gets the command name. This should be overridden by the derived classes
    * to provide their command name.
    *
    * @return the command name, never <code>null</code> or empty.
    */
   public String getCommandName()
   {
      return "cache";
   }
}

