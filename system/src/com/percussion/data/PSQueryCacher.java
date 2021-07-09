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

package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSBackEndDataTank;
import com.percussion.design.objectstore.PSBackEndJoin;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSDataMapper;
import com.percussion.design.objectstore.PSDataMapping;
import com.percussion.design.objectstore.PSDataSelector;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSQueryPipe;
import com.percussion.design.objectstore.PSResultPage;
import com.percussion.design.objectstore.PSResultPageSet;
import com.percussion.design.objectstore.PSResultPager;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.SortedSet;

import org.w3c.dom.Document;


/**
 * The PSQueryCacher class manages the cache for query requests. Requests
 * may be added to the cache. When entries become stale (exceed the amount
 * of time they may remain in cache) they will be removed.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSQueryCacher
{
   /**
    * Create a query cacher for the specified data set.
    *
    * @param      app      the application containing the data set
    *
    * @param      ds         the data set we will be caching requests for
    *
    * @param      pipe      the pipe we will be caching requests for
    *
    * @param      plan      the query execution plan
    */
   public PSQueryCacher(
      PSApplicationHandler app, PSDataSet ds,
      PSQueryPipe pipe, IPSExecutionStep[] plan)
      throws PSIllegalArgumentException
   {
      super();
        if (app != null);
        
      m_cacheType = CACHE_TYPE_NONE;

      // create the cache directory if it does not exist,
      // ignoring any errors -- this static boolean does not
      // need to be thread safe because nothing bad will happen
      // if we try to create it twice
      if (ms_shouldCreateCacheDir)
         ms_shouldCreateCacheDir = (!ms_CacheDirectory.mkdirs());

      // the server sets a cache limit we need to honor
      m_cacheSizeMax = PSServer.getServerConfiguration().getMaxCacheSizePerApp();

      PSDataSelector selector = pipe.getDataSelector();
      if ((selector != null) && selector.isCacheEnabled()) {
         if (selector.isCacheOnInterval()) {
            m_cacheType = CACHE_TYPE_INTERVAL;
            setCacheInterval(selector.getCacheAgeInterval());
         }
         else if (selector.isCacheOnTime()) {
            m_cacheType = CACHE_TYPE_TIME;
            setCacheTime(selector.getCacheAgeTime());
         }
         else if(selector.isCacheOnTimeAndInterval()) {
            m_cacheType = CACHE_TYPE_TIME_INTERVAL;
            setCacheInterval(selector.getCacheAgeInterval());
            setCacheTime(selector.getCacheAgeTime());
         }
      }

      if (m_cacheType == CACHE_TYPE_NONE)
         return;

      /* Now we need to figure out what the keys are for storing an entry in
       * cache. Optimally, we would like to cache HTML result pages. If a
       * HTML result page was not cached, the XML document would be nice. If
       * this too is unavailable, the SQL result set will suffice. If not,
       * we need to process the entire request from scratch.
       *
       * To cache SQL result sets:
       *
       *      1. We need to know what the lookup keys are for the SQL statement.
       *         Different keys will give back different result sets.
       *
       *      2. The SQL login credentials must match. Different users may
       *         not have access to the same data (particularly true when
       *         views are used).
       *
       * To cache XML documents:
       *
       *      1. Includes all criteria for caching SQL result sets.
       *
       *      2. See if any context-sensitive UDFs exist. In particular, UDFs
       *         which depend upon HTML parameters, CGI variables or cookies
       *         must be treated differently. If the UDF depends upon back-end
       *         column data, by having the same lookup keys for the SQL
       *         statement would imply the data the UDF will run on is
       *         also the same.
       *
       *      3. If a result pager is being used, psfirst must be checked to
       *         verify we want the same page's data.
       *
       *      4. Stylesheet selection criteria (conditionals). This is not as
       *         critical as we can re-run the stylesheet selectors on the
       *         document with minimal performance impact.
       *
       * To cache HTML results:
       *
       *      1. Includes all criteria for caching XML documents.
       *
       *      2. Stylesheet selection criteria are now required. The selection
       *         of a different style sheet implies a different HTML result
       *         page will be generated.
       */
      
      /* walk through the where clauses in the SQL statement to determine
       * what the keys are for caching SQL results
       */
      m_keysForResultSet = new java.util.ArrayList();

      for (int i = 0; i < plan.length; i++) {
         // if this is a statement, get the replacement columns it contains
         if (plan[i] instanceof com.percussion.data.PSStatement) {
            m_keysForResultSet.addAll(
               ((PSStatement)plan[i]).getReplacementValueExtractors());
         }
      }

      /* walk through the UDFs in the data mapper/joiner to get the keys
       * for the XML document (we will not include style sheet selection
       * in the XML document).
       */
      m_keysForXmlDocument = new java.util.ArrayList();
      m_keysForXmlDocument.addAll(m_keysForResultSet);   // need all SQL criteria

      PSDataMapping map;
      PSDataMapper mappings = pipe.getDataMapper();
      int size = (mappings == null) ? 0 : mappings.size();
      for (int i = 0; i < size; i++) {
         map = (PSDataMapping)mappings.get(i);
         if (map.getBackEndMapping() instanceof
            com.percussion.design.objectstore.PSExtensionCall)
         {
            addUdfExtractors( m_keysForXmlDocument, (PSExtensionCall)map.getBackEndMapping());
         }
      }

      // walk the joiner to get params from the translation UDF calls
      PSBackEndDataTank beTank = pipe.getBackEndDataTank();
      PSCollection joins = (beTank == null) ? null : beTank.getJoins();
      size = (joins == null) ? 0 : joins.size();
      for (int i = 0; i < size; i++) {
         /* if the join doesn't have a translator, this method will simply
          * ignore it. Otherwise, it will add the appropriate params to the
          * list
          */
         addUdfExtractors( m_keysForXmlDocument,
            ((PSBackEndJoin)joins.get(i)).getTranslator());
      }

      /* Get pagination related information */
      PSResultPager pager = ds.getResultPager();
      if ((pager != null) && (pager.getMaxRowsPerPage() >= 1)) {
         addReplacementValueExtractors(m_keysForXmlDocument, ms_pagerParam);
      }

      /* walk through the style sheets to get the HTML keys. Unlike the other
       * keys, each style sheet selector may define a different set of keys.
       * For instance, style sheet 1 may require the HTML parameter forEdit
       * be set to 1. The second style sheet may require the CGI variable
       * HTTP_USER_AGENT contain Mozilla. These are unrelated, so we must
       * check each condition separately.
       *
       * For this reason, we are not copying over the XML keys into the HTML
       * keys. Instead, we must first evaluate the XML keys to verify that
       * piece is correct, then the HTML keys to determine which style sheet
       * to use.
       */
      m_keysForResultPage = new java.util.ArrayList();
      if (ds.isOutputResultPages()) {
         PSResultPageSet pageSet = ds.getOutputResultPages();
         PSCollection pages = (pageSet == null) ? null : pageSet.getResultPages();
         size = (pages == null) ? 0 : pages.size();

         java.util.ArrayList styleKeys;
         PSResultPage page;
         PSConditional cond;
         PSCollection conditionals;
         int condSize;

         for (int i = 0; i < size; i++) {
            // create the key set for this style sheet
            styleKeys = new java.util.ArrayList();
            m_keysForResultPage.add(styleKeys);

            // and add in all the params for selecting it
            page = (PSResultPage)pages.get(i);
            conditionals = page.getConditionals();
            condSize = (conditionals == null) ? 0 : conditionals.size();
            for (int j = 0; j < condSize; j++) {
               cond = (PSConditional)conditionals.get(j);
               addReplacementValueExtractors(styleKeys, cond.getVariable());
               if (!cond.isUnary())
                  addReplacementValueExtractors(styleKeys, cond.getValue());
            }

            /* At this time, I do not believe we need to take the request
             * link generator params into consideration.
             * We map the target data set's parameters into
             * the one's defined for this data set to determine what the
             * values are for generating the URL. Since we look in the
             * selector/mapper, which we've already done here, we should
             * be fine.
             */
         }
      }
   }

   /**
    * Check the HTTP headers associated with the request to determine if
    * reading from the cache is permitted.
    *
    * @return            <code>true</code> if reading is permitted
    */
   public static boolean isCacheReadPermitted(PSRequest request)
   {
      // check the Pragma header for HTTP/1.0
      String pragmaHeader = request.getCgiVariable(
         IPSCgiVariables.CGI_HTTP_PRAGMA, "").toLowerCase();

      // check the Cache-Control header for HTTP/1.1
      String cacheHeader = request.getCgiVariable(
         IPSCgiVariables.CGI_HTTP_CACHE_CONTROL, "").toLowerCase();

      // when either of these is set to no-cache, it's not permitted
      return (pragmaHeader.indexOf("no-cache") == -1) &&
         (cacheHeader.indexOf("no-cache") == -1);
   }

   /**
    * Remove all entries from the cache.
    */
   public void clear() 
   {
      // this is what we sync on for clearAged, so do the same here
      synchronized (m_cacheSizeSync) {
         java.util.Iterator iterator = m_cacheSortByDate.iterator();
         while (iterator.hasNext()) {
            PSCachedEntry rmEntry = (PSCachedEntry)iterator.next();

            // remove the file from disk
            java.io.File file = rmEntry.getFile();
            try {
               file.delete();
            } catch (Exception e) {
               logCacheEntryException( file.getName(),
                  IPSDataErrors.CACHER_FILE_REMOVE_EXCEPTION, e);
            }
         }

         // now free all the hashes/sorts
         m_cacheSortByDate.clear();
         m_xmlCache.clear();
         m_pageCache.clear();
         m_cacheSizeCur = 0;
      }
   }

   /**
    * Get the file containing the cached response for this request.
    *
    * @param   data            the run-time execution data
    *
    * @return                  the file containing the response;
    *                           <code>null</code> if the
    *                           request cannot be handled from cache
    */
   public java.io.File getCacheFile(PSExecutionData data)
   {
      PSRequest request = data.getRequest();
      PSCachedEntry entry = null;

      switch (request.getRequestPageType()) {
         case PSRequest.PAGE_TYPE_HTML:      // request for an HTML page
         case PSRequest.PAGE_TYPE_UNKNOWN:   // request for raw data (eg, JPEG)
            return getResultPage(data);

         default:    // this is XML or TEXT
            // for all requests, we need to get at least the XML key
            StringBuffer buf = null;
            try {
               buf = getKeyBuffer(data);
               addReplacementValuesToKeyBuffer(buf, data, m_keysForXmlDocument);

               entry = getEntryFromCache(m_xmlCache, buf.toString());
            } catch (PSCacheEntryAgedException e) {
               /* aged, no problem */
            } catch (Exception e) {
               /* For now, we'll treat this as no big deal and ignore it.
                * This means the next request must fire the query again.
                 * Where this may be a problem is if this was caused by
                 * low disk space, etc. In these cases, we need to perform
                 * maintenance on the cache (eg, clear the disk cache of older
                 * entries.
                 */
               logCacheEntryException(
                  buf.toString(), IPSDataErrors.CACHER_LOAD_XML_EXCEPTION, e);
            }
            break;
      }

      return (entry != null) ? entry.getFile() : null;
   }

   /**
    * Add a result page from the cache, if it's currently available.
    * This is usually an HTML file.
    *
    * @param   data      the run-time data associated with this request
    *
    * @return            the HTML page; <code>null</code> if this is not
    *                     in cache
    */
   public java.io.File getResultPage(PSExecutionData data)
   {
      PSCachedEntry entry = null;
      String cacheKey = null;
      try {
         cacheKey = getCachedResultPageKey(data);
         entry = getEntryFromCache(m_pageCache, cacheKey);
      } catch (PSCacheEntryAgedException e) {
         // we found a match, but it aged - go no further
      } catch (Exception e) {
         // we hit an error, so let them requery
         logCacheEntryException(
            cacheKey, IPSDataErrors.CACHER_LOAD_RESPAGE_EXCEPTION, e);
      }

      return (entry != null) ? entry.getFile() : null;
   }

   /**
    * Add a result page from the cache, if it's currently available.
    * This is usually an HTML file.
    *
    * @param   data      the run-time data associated with this request
    *
    * @param   in         the page data as a stream
    *
    * @param   length   the length of the data stream
    */
   public void addResultPage(
      PSExecutionData data, java.io.InputStream in, int length)
   {
      String cacheKey = null;
      try {
         cacheKey = getCachedResultPageKey(data);
         PSCachedEntry entry
            = new PSCachedEntry(data.getRequest(), in, length);
         addEntryToCache(m_pageCache, cacheKey, entry);
      } catch (Exception e) {
         /* For now, we'll treat this as no big deal and ignore it. This
          * means the next request must fire the query again.
          * Where this may be a problem is if this was caused by
          * low disk space, etc. In these cases, we need to perform
          * maintenance on the cache (eg, clear the disk cache of older
          * entries.
          */
         logCacheEntryException(
            cacheKey, IPSDataErrors.CACHER_STORE_RESPAGE_EXCEPTION, e);
      }
   }

   /**
    * Get an XML document from the cache, if it's currently available.
    *
    * @param   data      the run-time data associated with this request
    *
    * @return            the XML document; <code>null</code> if this is not
    *                     in cache
    */
   public Document getXmlDocument(PSExecutionData data)
   {
      Document doc = null;
      String cacheKey = null;
      try {
         cacheKey = getCachedXmlDocumentKey(data);
         PSCachedEntry entry = getEntryFromCache(m_xmlCache, cacheKey);
         if (entry != null) {
            InputStream in = null;
            try {
               in = entry.getContent();
               doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            } finally {
               if (in != null) {
                  try { in.close(); }
                  catch (java.io.IOException e) { /* done anyway */ }
               }
            }
         }
      } catch (PSCacheEntryAgedException e) {
         // we found a match, but it aged - go no further
      } catch (Exception e) {
         // we hit an error, so let them requery
         doc = null;   // just in case it actually was created
         logCacheEntryException(
            cacheKey, IPSDataErrors.CACHER_LOAD_XML_EXCEPTION, e);
      }

      return doc;
   }

   /**
    * Get an XML document from the cache, if it's currently available.
    *
    * @param   data      the run-time data associated with this request
    *
    * @param   doc      the XML document to add to the cache
    */
   public void addXmlDocument(PSExecutionData data, Document doc)
   {
      String cacheKey = null;
      try {
         cacheKey = getCachedXmlDocumentKey(data);
         PSCachedEntry entry
            = new PSCachedEntry(data.getRequest(), doc);
         addEntryToCache(m_xmlCache, cacheKey, entry);
      } catch (Exception e) {
         /* For now, we'll treat this as no big deal and ignore it. This
          * means the next request must fire the query again.
          * Where this may be a problem is if this was caused by
          * low disk space, etc. In these cases, we need to perform
          * maintenance on the cache (eg, clear the disk cache of older
          * entries.
          */
         logCacheEntryException(
            cacheKey, IPSDataErrors.CACHER_STORE_XML_EXCEPTION, e);
      }
   }

   /**
    * Set the time interval for interval based caching.
    * <p>
    * When interval based caching is in use, requests will be aged out
    * of the cache after the specified interval elapses. Let's assume an
    * interval of 15 minutes is set. If a user makes a request at 12:00,
    * a query will be executed and the results will be stored in the cache
    * until 12:15. If another request comes in before that time, the cached
    * entry will be used. The first request received after 12:15 will cause
    * a new query to be executed. It will then be added to the cache for
    * 15 minutes.
    *
    * @param   minutes   the cache aging interval, in minutes
    */
   void setCacheInterval(int minutes)
   {
      m_intervalMinutes = minutes;
   }

   /**
    * Set the time of the day for time based caching.
    * <p>
    * When time based caching is in use, requests will be aged out
    * of the cache on or after the specified time. Let's assume a
    * time of 12:00 is set. The first user to make a request will cause
    * a query to be executed and the results will be stored in the cache
    * until 12:00. If the first user made the request at 11:50, the results
    * will be cached for 10 minutes. The next request received after 12:00
    * will cause a new query to be submitted. If the next request is at
    * 12:10, the results from that query will be held for 23 hours and 50
    * minutes -- that is, until 12:00 the next day.
    * <p>
    * When both time and interval based caching are enabled, requests will
    * be aged out of the cache based upon an interval from a specified
    * starting time. Let's use the start time of 12:00 and an interval of
    * 15 minutes. If the first query is submitted at 12:10. This will be
    * processed against the back-end and held in cache until 12:15. At
    * that point, it will be aged out of the cache. If the next request
    * comes along at 12:35 that will cause a new query. That request will
    * be aged at 12:45. As you can see, the interval is used to specify a
    * time of day rather than the amount of time the entry should remain
    * in cache. To have the query fire at even hours, specify an even start
    * point (eg, 12:00) and 120 minutes as the interval. If E2 starts at
    * 3:35, the first user request will be cached. The next request after
    * 4:00 will go against the back-end rather than cache. The cache will
    * then be used until 6:00, at which time the cache will be aged, and
    * so-on.
    *
    * @param   time      the time of day to age the cache at. Only the hours
    *                   and minutes will be used. All other components
    *                   will be cleared from the time.
    */
   void setCacheTime(java.util.Date time)
   {
      m_ageTimeOfDay = getTimeOfDay(time);
   }

   /**
    * Extract the time of day from the specified date. Only the hours
    * and minutes will be used. All other components will be cleared
    * from the time.
    *
    * @param   time      the date to use
    *
    * @return            the time of day, in minutes
    */
   public static int getTimeOfDay(java.util.Date time)
   {
      java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
      cal.setTime(time);

      return getTimeOfDay(cal);
   }

   /**
    * Extract the time of day from the specified calendar. Only the hours
    * and minutes will be used. All other components will be cleared
    * from the time.
    *
    * @param   cal      the calendar to use
    *
    * @return            the time of day, in minutes
    */
   public static int getTimeOfDay(java.util.Calendar cal)
   {
      return (cal.get(java.util.Calendar.HOUR_OF_DAY) * 60) +
         cal.get(java.util.Calendar.MINUTE);
   }

   public static java.util.Date generateIntervalExpirationTime(
      GregorianCalendar cal, int intervalMinutes)
   {
      // for interval based, we just add the minutes
      cal.add(java.util.Calendar.MINUTE, intervalMinutes);

      // we clear these as we're minute based anyway
      cal.set(java.util.Calendar.SECOND, 0);
      cal.set(java.util.Calendar.MILLISECOND, 0);

      return cal.getTime();
   }

   public static java.util.Date generateTimeIntervalExpirationTime(
      GregorianCalendar cal, int ageTimeOfDay, int intervalMinutes)
   {
      /* I'm not sure if all end-users would consider this logic
       * correct. In particular, we are not
       * keeping track of the first time we ran. For instance, if we
       * start at 3:00 and age every 35 minutes, after a day of processing
       * we would need to age at 3:30. However, we will use 3:00 as the
       * base, causing the 2:55 cache to age after only 5 minutes
       */
      int timeOfDay = getTimeOfDay(cal);
      if (timeOfDay >= ageTimeOfDay) {
         // let's see how much time has elapsed since the age start time
         timeOfDay -= ageTimeOfDay;
      }
      else {
         /* to calculate where we are in the interval, we need to
          * determine the amount of time it took to cause the day wrap
          * and add that to the current time of day
          */
         timeOfDay += ((24 * 60 /* minutes in a day */) - ageTimeOfDay);
      }

      /* the next interval can be calculated by subtracting the
       * amount of time past an interval from the interval length
       */
      cal.add(java.util.Calendar.MINUTE,
         intervalMinutes - (timeOfDay % intervalMinutes));

      // we clear these as we're minute based anyway
      cal.set(java.util.Calendar.SECOND, 0);
      cal.set(java.util.Calendar.MILLISECOND, 0);

      return cal.getTime();
   }

   public static java.util.Date generateTimeExpirationTime(
      GregorianCalendar cal, int ageTimeOfDay)
   {
      int timeOfDay = getTimeOfDay(cal);
      if (timeOfDay >= ageTimeOfDay) {
         /* if we're on or after the expiration time, we want to move 
          * the next expiration to the next day
          *
          * othewise, we'll be using the current date with the expiration
          * time. This may be as little as a minute away.
          */
         cal.add(java.util.Calendar.DATE, 1);
      }

      cal = new java.util.GregorianCalendar(
         cal.get(java.util.Calendar.YEAR),
         cal.get(java.util.Calendar.MONTH),
         cal.get(java.util.Calendar.DATE),
         ageTimeOfDay / 60,   // hours in the time
         ageTimeOfDay % 60);   // minutes in the time

      // we clear these as we're minute based anyway
      cal.set(java.util.Calendar.SECOND, 0);
      cal.set(java.util.Calendar.MILLISECOND, 0);

      return cal.getTime();
   }

   protected java.util.Date generateExpirationTime()
   {
      GregorianCalendar cal = new GregorianCalendar();

      if (m_cacheType == CACHE_TYPE_INTERVAL)
         return generateIntervalExpirationTime(cal, m_intervalMinutes);
      else if (m_cacheType == CACHE_TYPE_TIME)
         return generateTimeExpirationTime(cal, m_ageTimeOfDay);
      else if (m_cacheType == CACHE_TYPE_TIME_INTERVAL)
         return generateTimeIntervalExpirationTime(
            cal, m_ageTimeOfDay, m_intervalMinutes);

      // this means we're not caching, so return the current time
      return cal.getTime();
   }

   private void addUdfExtractors(List list, PSExtensionCall udfCall)
      throws PSIllegalArgumentException
   {
      if (udfCall == null)
         return;

      PSExtensionParamValue[] values = udfCall.getParamValues();
      int size = (values == null) ? 0 : values.length;
      for (int j = 0; j < size; j++) {
         addReplacementValueExtractors(list, values[j].getValue());
      }
   }

   private void addReplacementValueExtractors(List list, IPSReplacementValue value)
      throws PSIllegalArgumentException
   {
      if (value == null)
         return;

      // store extractors for all params not coming SQL or literals
      if (!(value instanceof com.percussion.design.objectstore.PSBackEndColumn) &&
         !(value instanceof com.percussion.design.objectstore.PSLiteral))
      {
         list.add(
            PSDataExtractorFactory.createReplacementValueExtractor(value));
      }
   }

   private PSCachedEntry getEntryFromCache(Hashtable cache, String key)
      throws PSCacheEntryAgedException
   {
      PSCachedEntry entry = (PSCachedEntry)cache.get(key);
      if (entry != null) {
         if (isCachedEntryStale(entry)) {
            // remove the entry from the cache
            PSCachedEntry rmEntry = null;
            synchronized (cache) {
               // we remove from the hashtable by locking it and
               // verifying a new entry wasn't put in the old entries
               // place during our check of its age
               rmEntry = (PSCachedEntry)cache.remove(key);
               if ((rmEntry != null) && !entry.equals(rmEntry)) {
                  // guess it's been updated, put it back in and
                  // try the date check again
                  cache.put(key, rmEntry);
                  entry = rmEntry;   // point to the new entry
               }
               else {
                  m_cacheSortByDate.remove(entry);   // remove from sort
                  entry = null;                     // flag we're stale
               }
            }

            // Note: if we somehow slept past the specified interval
            // while trying to lock m_pageCache, this new entry
            // could actually be stale, so recheck it now. 
            if ((entry != null) && isCachedEntryStale(entry)) {
               entry = null;      // flag we're stale
               // not removing from cache as we may end up in an
               // endless loop. Let the next guy in take care of that
            }
            else if (rmEntry != null) {
               // remove the file from disk as well
               java.io.File file = rmEntry.getFile();
               try {
                  file.delete();
                  m_cacheSizeCur -= (int) (rmEntry.getCachedFileSize() / 1024);
               }
               catch (Exception e) {
                  logCacheEntryException( file.getName(),
                     IPSDataErrors.CACHER_FILE_REMOVE_EXCEPTION, e);
               }
            }
         }

         // if it's now null, we found a hit but it aged out of cache
         if (entry == null)
            throw new PSCacheEntryAgedException();

         /* let the entry know it's been used again.
          *
          * this is critical if we decide to throw out entries not
          * frequently used when the cache starts to fill up.
          */
         entry.bumpHitCount();
      }

      return entry;
   }

   private boolean isCachedEntryStale(PSCachedEntry entry)
   {
      Date curTime = new Date();
      return !curTime.before(entry.getExpirationTime());
   }

   /**
    * Get the key which can be used to locate XML documents in the cache.
    */
   private String getCachedXmlDocumentKey(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      StringBuffer buf = getKeyBuffer(data);
      addReplacementValuesToKeyBuffer(buf, data, m_keysForXmlDocument);
      return buf.toString();
   }

   /**
    * Get the key which can be used to locate HTML pages in the cache.
    */
   private String getCachedResultPageKey(PSExecutionData data)
      throws com.percussion.data.PSDataExtractionException
   {
      StringBuffer buf = getKeyBuffer(data);
      addReplacementValuesToKeyBuffer(buf, data, m_keysForXmlDocument);

      if (m_keysForResultPage.size() != 0) {
         int index = data.getResultPageIndex();
         if ((index != -1) && (index < m_keysForResultPage.size()))
            addReplacementValuesToKeyBuffer(
               buf, data, (java.util.ArrayList)m_keysForResultPage.get(index));
      }

      return buf.toString();
   }

   /**
    * Get the string buffer which can be used to build the key for this
    * request. It stores the request page name in the returned buffer. The
    * caller must add all parameter values.
    */
   private StringBuffer getKeyBuffer(PSExecutionData data)
   {
      StringBuffer buf = new StringBuffer();
      buf.append('[');
      buf.append(data.getRequest().getRequestPage());
      buf.append(']');
      return buf;
   }

   /**
    * Append the parameter values to the specified key buffer.
    */
   private void addReplacementValuesToKeyBuffer(
      StringBuffer buf, PSExecutionData data, java.util.List params)
      throws com.percussion.data.PSDataExtractionException
   {
      Object o;
      int size = (params == null) ? 0 : params.size();
      for (int i = 0; i < size; i++) {
         o = ((IPSDataExtractor)params.get(i)).extract(data);
         buf.append('[');
         if (o != null)
            buf.append(o.toString());
         buf.append(']');
      }
   }

   /**
    * Add an entry to the specified hash.
    */
   private void addEntryToCache(
      Hashtable entryHash, String entryKey, PSCachedEntry entry)
   {
      /* calculate the size of this entry to see if we've exceeded the
       * max cache size. If so, we need a smart way to move entries out of
       * the cache (least recently used? least accessed? other?).
       */
      int entrySize = (int)(entry.getCachedFileSize() / 1024L);
      synchronized (m_cacheSizeSync) {
         if (wouldExceedSize(entrySize))
         {
            /* need to purge older entries now and adjust m_cacheSizeCur
             *
             * For now, just throw out entries that have aged.
             * In the future, we may want to factor in hitCount as well
             */
            SortedSet agedEntries = m_cacheSortByDate.headSet(new Date());
            java.util.Iterator iterator = agedEntries.iterator();
            while (iterator.hasNext()) {
               PSCachedEntry rmEntry = (PSCachedEntry)iterator.next();

               /* need to figure out which Hashtable this is in and
                * remove it.
                */
               synchronized (m_xmlCache) {
                  PSCachedEntry ce = (PSCachedEntry)m_xmlCache.get(entryKey);
                  if ((ce != null) && (ce == rmEntry))
                     m_xmlCache.remove(entryKey);
               }

               synchronized (m_pageCache) {
                  PSCachedEntry ce = (PSCachedEntry)m_pageCache.get(entryKey);
                  if ((ce != null) && (ce == rmEntry))
                     m_pageCache.remove(entryKey);
               }

               // remove the file from disk and decrement the cache size used
               java.io.File file = rmEntry.getFile();
               try {
                  file.delete();
                  m_cacheSizeCur -= (int) (rmEntry.getCachedFileSize() / 1024);
               } catch (Exception e) {
                  logCacheEntryException( file.getName(),
                     IPSDataErrors.CACHER_FILE_REMOVE_EXCEPTION, e);
               }
            }

            // remove the entries from the sorted cache
            m_cacheSortByDate.removeAll(agedEntries);

            // if we're still too big, don't cache it
            if (wouldExceedSize(entrySize))
            {
               Object[] args = { entryKey, String.valueOf(m_cacheSizeMax),
                  String.valueOf(m_cacheSizeCur), String.valueOf(entrySize) };
               com.percussion.log.PSLogManager.write(
                  new com.percussion.error.PSNonFatalError(
                  IPSDataErrors.CACHER_FULL, args));
               return;
            }
         }

         m_cacheSizeCur += entrySize;
      }

      Object o = entryHash.put(entryKey, entry);
      if (o != null)   // if we're replacing an entry, remove it
         m_cacheSortByDate.remove(o);
      m_cacheSortByDate.add(entry);
   }

   /**
    * Reports whether the addition of a cache entry with the given
    * size would exceed the max cache size. If the max cache size
    * is unlimited, this always returns <CODE>false</CODE>.
    *
    * @author   chadloder
    * 
    * @version 1.10 1999/09/21
    * 
    * @param   entrySize
    * 
    * @return   boolean true if an addition of that size would exceed
    * the max cache size, <CODE>false</CODE> if not (or if size is
    * unlimited).
    */
   private boolean wouldExceedSize(long entrySize)
   {
      return ((m_cacheSizeMax != -1)
         && (entrySize + m_cacheSizeCur) > m_cacheSizeMax);
   }

   private void logCacheEntryException(
      String cacheKey, int errorCode, Exception e)
   {
      Object[] args = {
         ((cacheKey == null) ? "" : cacheKey), e.toString() };
      com.percussion.log.PSLogManager.write(
         new com.percussion.error.PSNonFatalError(errorCode, args));
   }

   /**
    * Caching is not enabled.
    * <P>
    * This should never really happen, but we may as well account for it.
    */
   private static final int      CACHE_TYPE_NONE            =   0;

   /**
    * Caching is interval based.
    * <p>
    * When interval based caching is in use, requests will be aged out
    * of the cache after the specified interval elapses. Let's assume an
    * interval of 15 minutes is set. If a user makes a request at 12:00,
    * a query will be executed and the results will be stored in the cache
    * until 12:15. If another request comes in before that time, the cached
    * entry will be used. The first request received after 12:15 will cause
    * a new query to be executed. It will then be added to the cache for
    * 15 minutes.
    */
   private static final int      CACHE_TYPE_INTERVAL         =   1;

   /**
    * Caching is based upon a specified time of the day.
    * <p>
    * When time based caching is in use, requests will be aged out
    * of the cache on or after the specified time. Let's assume a
    * time of 12:00 is set. The first user to make a request will cause
    * a query to be executed and the results will be stored in the cache
    * until 12:00. If the first user made the request at 11:50, the results
    * will be cached for 10 minutes. The next request received after 12:00
    * will cause a new query to be submitted. If the next request is at
    * 12:10, the results from that query will be held for 23 hours and 50
    * minutes -- that is, until 12:00 the next day.
    */
   private static final int      CACHE_TYPE_TIME            =   2;

   /**
    * Caching is based upon both an interval and a specified time
    * of the day.
    * <p>
    * When both time and interval based caching are enabled, requests will
    * be aged out of the cache based upon an interval from a specified
    * starting time. Let's use the start time of 12:00 and an interval of
    * 15 minutes. If the first query is submitted at 12:10. This will be
    * processed against the back-end and held in cache until 12:15. At
    * that point, it will be aged out of the cache. If the next request
    * comes along at 12:35 that will cause a new query. That request will
    * be aged at 12:45. As you can see, the interval is used to specify a
    * time of day rather than the amount of time the entry should remain
    * in cache. To have the query fire at even hours, specify an even start
    * point (eg, 12:00) and 120 minutes as the interval. If E2 starts at
    * 3:35, the first user request will be cached. The next request after
    * 4:00 will go against the back-end rather than cache. The cache will
    * then be used until 6:00, at which time the cache will be aged, and
    * so-on.
    */
   private static final int      CACHE_TYPE_TIME_INTERVAL      =   3;


   // these are the keys (IPSDataExtractor objects) which are used to
   // get the key information to determine if we have a matching entry
   // the cached entry may be a HTML page, XML document or SQL result set
   private List         m_keysForResultSet;
   private List         m_keysForXmlDocument;
   private List         m_keysForResultPage;

   private int            m_cacheType;
   private int            m_intervalMinutes      = 0;
   private int            m_ageTimeOfDay         = 0;
   private Hashtable      m_xmlCache            = new Hashtable();
   private Hashtable      m_pageCache            = new Hashtable();
   private SortedSet      m_cacheSortByDate      = new java.util.TreeSet();

   private Object         m_cacheSizeSync      = new Object();
   private int            m_cacheSizeCur         = 0;
   private int            m_cacheSizeMax         = 0;

   /** the cache directory */
   private static File   ms_CacheDirectory      = new File("Cache");
   
   /** true if we should try to create the cache directory when a
       cacher object is constructed */
   private static boolean ms_shouldCreateCacheDir = true;

   private static PSHtmlParameter   ms_pagerParam = new PSHtmlParameter("psfirst");


   class PSCachedEntry implements java.io.Serializable, java.lang.Comparable
   {
      // for serialization only
      PSCachedEntry() { super(); }

      private PSCachedEntry(PSRequest request)
         throws java.io.IOException
      {
         super();

         // store the time this entry will expire
         m_expirationTime = generateExpirationTime();

         // and the file we're storing the cache entry as
         m_cachedFile = File.createTempFile(
            "psx", request.getRequestPageExtension(), ms_CacheDirectory);

         // we currently support the cache for the current execution only.
         // As such, we can mark it for delete on exit. In the future
         // we may want to remember what's been cached for subsequent reuse
      
      }

      PSCachedEntry(PSRequest request, Document doc)
         throws java.io.IOException
      {
         this(request);

         // see if we can figure out the preferred content encoding
         
         // store the XML document as a file
         Writer out = new OutputStreamWriter(
            new BufferedOutputStream(new FileOutputStream(m_cachedFile)),
               PSCharSetsConstants.rxJavaEnc());

         PSXmlDocumentBuilder.write(doc, out);
         out.flush();

         out.close();
      }

      PSCachedEntry(PSRequest request, java.io.InputStream in, int length)
         throws java.io.IOException
      {
         this(request);

         // store the HTML page as a file
         FileOutputStream out = new FileOutputStream(m_cachedFile);

         byte[] data = new byte[1024];
         int total, curRead;
         for (total = 0; total < length; total += curRead) {
            curRead = in.read(data);
            out.write(data, 0, curRead);
         }
         out.flush();
         out.close();
      }

      Date getExpirationTime()
      {
         return m_expirationTime;
      }

      long getCachedFileSize()
      {
         return m_cachedFile.length();
      }

      InputStream getContent()
         throws java.io.FileNotFoundException
      {
         return new FileInputStream(m_cachedFile);
      }

      synchronized int getHitCount()
      {
         return m_hitCount;
      }

      synchronized void bumpHitCount()
      {
         m_hitCount++;
      }

      File getFile()
      {
         return m_cachedFile;
      }

      // use the Comparable of date to allow for sorting of these by date
      public int compareTo(Object o) throws ClassCastException
      {
         // if it's already a date, compare it straight out
         if (o instanceof java.util.Date)
            return m_expirationTime.compareTo((java.util.Date)o);

         // otherwise, assume it's one of ours
         // (if it's not, ClassCastException is thrown as stated)
         return m_expirationTime.compareTo(
            ((PSCachedEntry)o).m_expirationTime);
      }


      // this is the file that will be cached
      private File      m_cachedFile = null;

      // this is the time after which this entry should be aged
      private Date      m_expirationTime = null;

      // how many requests for this page
      private int         m_hitCount = 1;   // start with initial hit
   }

   class PSCacheEntryAgedException extends java.lang.Exception
   {
      PSCacheEntryAgedException() { super(); }
   }
}

