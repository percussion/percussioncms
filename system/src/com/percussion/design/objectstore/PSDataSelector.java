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

import com.percussion.error.PSException;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;


/**
 * The PSDataSelector class defines how data is being selected through
 * a particular query pipe (PSQueryPipe). Data is selected through one
 * of the following mechanisms:
 * <ul>
 * <li>defining WHERE clauses</li>
 * <li>using a native SELECT statement</li>
 * <p>
 * The data selector can also be used to define how data is cached. Since
 * performance is critical, the results of requests which may be repeated
 * often can be cached for better performance.
 *
 * @see PSQueryPipe#getDataSelector
 * @see PSQueryPipe
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataSelector extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSDataSelector(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct a data selector object.
    * <p>
    * The object is set to use WHERE clauses by default. No WHERE clauses
    * are initially set, which will cause all data to be selected. Be sure
    * to add WHERE clauses or change the selection method to the
    * appropriate type.
    * <p>
    * Caching is disabled by default. The cache type is, however, set to
    * be interval based. A default interval of 15 minutes is also set.
    */
   public PSDataSelector()
   {
      super();
      m_selector            = DS_BY_WHERE_CLAUSE;
      m_caching            = false;
      m_cacheType            = DS_CACHE_TYPE_INTERVAL;
      m_cacheAgeInterval   = 15;

      m_whereClauses = new PSCollection(
         com.percussion.design.objectstore.PSWhereClause.class);
      m_sortedColumns = new PSCollection(
         com.percussion.design.objectstore.PSSortedColumn.class);
   }

   /**
    * Are unique (distinct) values returned by the SELECT? Only one row will
    * be returned for each unique set of values defined by the SELECT's
    * column list. If a native SELECT statement is being used, this may
    * return <code>true</code> even though non-unique values will be
    * returned. It is up to the native SELECT statement to use the
    * appropriate syntax to generate unique result set values.
    *
    * @return      <code>true</code> if unique values are returned;
    *             <code>false</code> otherwise
    */
   public boolean isSelectUnique()
   {
      return ((m_selector & DS_DISTINCT) == DS_DISTINCT);
   }

   /**
    * Enable or disable returning only unique (distinct) values for the
    * SELECT. Only one row will be returned for each unique set of values
    * defined by the SELECT's column list. If a native SELECT statement is
    * being used, this setting will be ignored. It is up to the native
    * SELECT statement to use the appropriate syntax to generate unique
    * result set values.
    *
    * @param enable       <code>true</code> if unique values are returned;
    *                    <code>false</code> otherwise
    */
   public void setSelectUnique(boolean enable)
   {
      if (enable)
         m_selector |= DS_DISTINCT;
      else if ((m_selector & DS_DISTINCT) == DS_DISTINCT)
         m_selector ^= DS_DISTINCT;
   }

   /**
    * Is data being selecting by defining WHERE clauses?
    *
    * @return      <code>true</code> if this is the method being used,
    *             <code>false</code> otherwise
    */
   public boolean isSelectByWhereClause()
   {
      return ((m_selector & DS_BY_WHERE_CLAUSE) == DS_BY_WHERE_CLAUSE);
   }

   /**
    * Enable selecting data using the defined WHERE clauses. If no
    * WHERE clauses are defined, all data is selected.
    */
   public void setSelectByWhereClause()
   {
      m_selector = DS_BY_WHERE_CLAUSE | (m_selector & DS_FLAGS);
   }

   /**
    * Get the collection of WHERE clauses used to select data. This will
    * return the associated WHERE clauses, even if they are not being
    * used. Be sure to call the isSelectByWhereClause method to verify
    * that the WHERE clauses are being used for data selection.
    *
    * @return      a collection containing the WHERE clauses
    *             (PSWhereClause objects)
    * @see         #isSelectByWhereClause
    */
   public com.percussion.util.PSCollection getWhereClauses()
   {
      return m_whereClauses;
   }

   /**
    * This overwrite the WHERE clauses associated with this object with
    * the specified collection. If you only want to modify certain clauses,
    * add a new clause, etc. use getWhereClauses to get the existing
    * collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSDataSelector object. Any subsequent changes made to the object
    * by the caller will also effect the selector.
    *
    * @param clauses    the new WHERE clauses to use for this selector
    *
    * @see         #getWhereClauses
    * @see         PSWhereClause
    */
   public void setWhereClauses(com.percussion.util.PSCollection clauses)
   {
      if (clauses != null) {
         if (!com.percussion.design.objectstore.PSWhereClause.class.isAssignableFrom(
            clauses.getMemberClassType()))
         {
            throw new IllegalArgumentException("coll bad content type, WHERE clause: " +
               clauses.getMemberClassName());
         }
      }

      m_whereClauses = clauses;
   }

   /**
    * Get the collection of columns being used to sort the data. The columns
    * will be sorted in the order in which they are defined in the
    * collection.
    *
    * @return      a collection containing the sorted columns
    *             (PSSortedColumn objects)
    *
    * @see         com.percussion.design.objectstore.PSSortedColumn
    */
   public com.percussion.util.PSCollection getSortedColumns()
   {
      return m_sortedColumns;
   }

   /**
    * This overwrite the sorted columns associated with this object with
    * the specified collection. If you only want to modify certain sorted
    * columns, add a new sorted column, etc. use getSortedColumns to get
    * the existing collection and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored with
    * the PSDataSelector object. Any subsequent changes made to the object
    * by the caller will also effect the selector.
    *
    * @param cols     the new sorted columns to use for this selector
    *
    * @see             #getSortedColumns
    *
    * @see             PSSortedColumn
    */
   public void setSortedColumns(com.percussion.util.PSCollection cols)
   {
      if (cols != null) {
         String collectionClass = cols.getMemberClassName();
         if (!com.percussion.design.objectstore.PSSortedColumn.class.isAssignableFrom(
            cols.getMemberClassType()))
         {
            throw new IllegalArgumentException("coll bad content type, Sorted Column: " +
               cols.getMemberClassName());
         }
      }

      m_sortedColumns = cols;
   }

   /**
    * Is data being selecting by using a native SELECT statement?
    *
    * @return      <code>true</code> if this is the method being used,
    *             <code>false</code> otherwise
    */
   public boolean isSelectByNativeStatement()
   {
      return ((m_selector & DS_BY_NATIVE_STATEMENT) == DS_BY_NATIVE_STATEMENT);
   }

   /**
    * Enable selecting data by using a native SELECT statement.
    */
   public void setSelectByNativeStatement()
   {
      m_selector = DS_BY_NATIVE_STATEMENT | (m_selector & DS_FLAGS);
   }

   /**
    * Get the text of the native SELECT statement to use.
    *
    * @return     the text of the native SELECT statement to use
    */
   public java.lang.String getNativeStatement()
   {
      return m_nativeStatement;
   }

   /**
    * Set the text of the native SELECT statement to use. The SELECT
    * statement should be in the native syntax supported by the back-end
    * this selector is associated with. When joining across heterogeneous
    * data stores, use the E2 SELECT syntax.
    *
    * @param      text   the text of the native SELECT statement to use
    *
    * @see         #isSelectByNativeStatement
    */
   public void setNativeStatement(java.lang.String text)
   {
      IllegalArgumentException ex = validateNativeStatement(text);
      if (ex != null)
         throw ex;

      m_nativeStatement = text;
   }

   private IllegalArgumentException validateNativeStatement(String text)
   {
      if (   isSelectByNativeStatement() &&
            ( (text == null) || text.length() == 0) )
         return new IllegalArgumentException("datasel native statement required");

      return null;
   }

   /**
    * Is result caching enabled?
    *
    * @return      <code>true</code> if caching is enabled,
    *             <code>false</code> otherwise
    */
   public boolean isCacheEnabled()
   {
      return m_caching;
   }

   /**
    * Enable or disable result caching.
    * <p>
    * Since performance is critical, the results of requests which may be
    * repeated often can be cached for better performance.
    *
    * @param   enable   <code>true</code> to enable result caching,
    *                   <code>false</code> to disable it
    */
   public void setCacheEnabled(boolean enable)
   {
      m_caching = enable;
   }

   /**
    * Is caching interval based?
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
    * @return      <code>true</code> if interval based caching is enabled,
    *             <code>false</code> otherwise
    */
   public boolean isCacheOnInterval()
   {
      return (DS_CACHE_TYPE_INTERVAL == m_cacheType);
   }

   /**
    * Enable interval based caching.
    * <p>
    * When interval based caching is in use, requests will be aged out
    * of the cache after the specified interval elapses. Let's assume an
    * interval of 15 minutes is set. If a user makes a request at 12:00,
    * a query will be executed and the results will be stored in the cache
    * until 12:15. If another request comes in before that time, the cached
    * entry will be used. The first request received after 12:15 will cause
    * a new query to be executed. It will then be added to the cache for
    * 15 minutes. Use setCacheAgeInterval to set the interval.
    *
    * @see #setCacheAgeInterval
    */
   public void setCacheOnInterval()
   {
      m_cacheType = DS_CACHE_TYPE_INTERVAL;
   }

   /**
    * Get the cache aging interval.
    *
    * @return      the cache aging interval, in minutes
    * @see   #setCacheOnInterval
    * @see   #setCacheOnTimeAndInterval
    */
   public int getCacheAgeInterval()
   {
      return m_cacheAgeInterval;
   }

   /**
    * Set the cache aging interval.
    *
    * @param   interval    the cache aging interval, in minutes
    * @see   #setCacheOnInterval
    * @see   #setCacheOnTimeAndInterval
    */
   public void setCacheAgeInterval(int interval)
   {
      m_cacheAgeInterval = interval;
   }

   /**
    * Is caching based upon a specified time of the day?
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
    *
    * @return      <code>true</code> if time based caching is enabled,
    *             <code>false</code> otherwise
    */
   public boolean isCacheOnTime()
   {
      return (DS_CACHE_TYPE_TIME == m_cacheType);
   }

   /**
    * Enable caching based upon a specified time of the day.
    * <p>
    * When time based caching is in use, requests will be aged out
    * of the cache on or after the specified time. Let's assume a
    * time of 12:00 is set. The first user to make a request will cause
    * a query to be executed and the results will be stored in the cache
    * until 12:00. If the first user made the request at 11:50, the results
    * will be cached for 10 minutes. The next request received after 12:00
    * will cause a new query to be submitted. If the next request is at
    * 12:10, the results from that query will be held for 23 hours and 50
    * minutes -- that is, until 12:00 the next day. Use
    * setCacheAgeTime to set the aging time.
    *
    * @see #setCacheAgeTime
    */
   public void setCacheOnTime()
   {
      m_cacheType = DS_CACHE_TYPE_TIME;
   }

   /**
    * Get the time of day to age the cache at.
    *
    * @return      the time of day to age the cache at. Only the hours and
    *             minutes will be set.
    *
    * @see   #setCacheOnTime
    * @see   #setCacheOnTimeAndInterval
    */
   public java.util.Date getCacheAgeTime()
   {
      return m_cacheAgeTime;
   }

   /**
    * Set the time of day to age the cache at.
    *
    * @param   time      the time of day to age the cache at. Only the hours
    *                   and minutes will be used. All other components
    *                   will be cleared from the time.
    * @see   #setCacheOnTime
    * @see   #setCacheOnTimeAndInterval
    */
   public void setCacheAgeTime(java.util.Date time)
   {
      m_cacheAgeTime = time;
   }

   /**
    * Is caching based upon both an interval and a specified time
    * of the day?
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
    * @return      <code>true</code> if time and interval based caching is
    *             enabled, <code>false</code> otherwise
    */
   public boolean isCacheOnTimeAndInterval()
   {
      return (DS_CACHE_TYPE_TIME_INTERVAL == m_cacheType);
   }

   /**
    * Enable caching based upon both an interval and a specified time
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
    * so-on. Use setCacheAgeTime to set the aging time and
    * setCacheAgeInterval to set the interval.
    *
    * @see #setCacheAgeTime
    * @see #setCacheAgeInterval
    */
   public void setCacheOnTimeAndInterval()
   {
      m_cacheType = DS_CACHE_TYPE_TIME_INTERVAL;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param selector a valid PSDataSelector.
    */
   public void copyFrom( PSDataSelector selector )
   {
      copyFrom((PSComponent) selector );

      m_selector   =   selector.m_selector;
      m_caching   =   selector.isCacheEnabled();
      m_cacheType   =   selector.m_cacheType;
      m_cacheAgeInterval   =   selector.getCacheAgeInterval();
      m_cacheAgeTime   =   selector.getCacheAgeTime();
      m_whereClauses   =   selector.getWhereClauses();
      m_nativeStatement   =   selector.getNativeStatement();
      m_sortedColumns = selector.getSortedColumns();

      m_DateFormatter   =   selector.m_DateFormatter;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataSelector XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataSelector defines how data is being selected through a
    *       particular query pipe (PSQueryPipe). Data is selected through one
    *       of the following mechanisms:
    *
    *          - defining WHERE clauses and sort orders
    *          - using a native SELECT statement
    *
    *       The data selector can also be used to define how data is cached.
    *       Since performance is critical, the results of requests which may
    *       be repeated often can be cached for better performance.
    *    --&gt;
    *    &lt;!ELEMENT PSXDataSelector   (WhereClauses?, Sorting?, nativeStatement?, Caching)&gt;
    *
    *    &lt;!--
    *       attributes for this object:
    *
    *       whereClause - data is being selecting by by defining WHERE
    *       clauses.
    *
    *       userSelect - data is being selecting by using a native SELECT
    *       statement.
    *    --
    *    &gt;
    *    &lt;!ATTLIST PSXDataSelector
    *       method    (whereClause | nativeStatement)     #IMPLIED
    *       unique    %PSXIsEnabled                       #IMPLIED
    *    &gt;
    *
    *    &lt;!--
    *       the WHERE clauses used to select data. This
    *       contains the associated WHERE clauses, even if they are not being
    *       used. Be sure to check the method to verify that the WHERE
    *       clauses are being used for data selection.
    *    --&gt;
    *    &lt;!ELEMENT WhereClauses   (PSXWhereClause*)&gt;
    *
    *    &lt;!--
    *        the sorted columns for this query
    *    --&gt;
    *    &lt;!ELEMENT Sorting  (PSXSortedColumn*)&gt;
    *
    *    &lt;!--
    *       the text of the native SELECT statement to use. The SELECT
    *       statement should be in the native syntax supported by the
    *       back-end this selector is associated with. When joining across
    *       heterogeneous data stores, use the E2 SELECT syntax.
    *    --&gt;
    *    &lt;!ELEMENT nativeStatement   (#PCDATA)&gt;
    *
    *    &lt;!ELEMENT Caching          (ageTime?, ageInterval?)&gt;
    *
    *    &lt;!--
    *       attributes for the Caching object:
    *
    *       enabled - is result caching enabled? Since performance is
    *       critical, the results of requests which may be repeated often can
    *       be cached for better performance.
    *
    *       type:
    *          interval - is caching interval based? When interval based
    *          caching is in use, requests will be aged out of the cache
    *          after the specified interval elapses. Let's assume an
    *          interval of 15 minutes is set. If a user makes a request
    *          at 12:00, a query will be executed and the results will be
    *          stored in the cache until 12:15. If another request comes in
    *          before that time, the cached entry will be used. The first
    *          request received after 12:15 will cause a new query to be
    *          executed. It will then be added to the cache for 15 minutes.
    *
    *          time - Is caching based upon a specified time of the day?
    *          When time based caching is in use, requests will be aged out
    *          of the cache on or after the specified time. Let's assume a
    *          time of 12:00 is set. The first user to make a request will
    *          cause a query to be executed and the results will be stored
    *          in the cache until 12:00. If the first user made the request
    *          at 11:50, the results will be cached for 10 minutes. The next
    *          request received after 12:00 will cause a new query to be
    *          submitted. If the next request is at 12:10, the results from
    *          that query will be held for 23 hours and 50 minutes. That is,
    *          until 12:00 the next day.
    *
    *          timeAndInterval - Is caching based upon both an interval and a
    *          specified time of the day? When both time and interval based
    *          caching are enabled, requests will be aged out of the cache
    *          based upon an interval from a specified starting time. Let's
    *          use the start time of 12:00 and an interval of 15 minutes. If
    *          the first query is submitted at 12:10. This will be processed
    *          against the back-end and held in cache until 12:15. At that
    *          point, it will be aged out of the cache. If the next request
    *          comes along at 12:35 that will cause a new query. That request
    *          will be aged at 12:45. As you can see, the interval is used to
    *          specify a time of day rather than the amount of time the entry
    *          should remain in cache. To have the query fire at even hours,
    *          specify an even start point (eg, 12:00) and 120 minutes as the
    *          interval. If E2 starts at 3:35, the first user request will be
    *          cached. The next request after 4:00 will go against the
    *          back-end rather than cache. The cache will then be used until
    *          6:00, at which time the cache will be aged, and so-on.
    *    --&gt;
    *    &lt;!ATTLIST Caching
    *       enabled    %PSXIsEnabled                      #IMPLIED
    *       type       (interval | time | timeAndIterval) #IMPLIED
    *    &gt;
    *
    *    &lt;!--
    *       the cache aging interval.
    *    --&gt;
    *    &lt;!ELEMENT ageInterval      (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the time of day to age the cache at.
    *    --&gt;
    *    &lt;!ELEMENT ageTime          (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataSelector XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      //private          int             m_selector = 0;     //type of query
      if (isSelectByNativeStatement())
         root.setAttribute("method", XML_FLAG_METHOD_NATIVE);
      else
         root.setAttribute("method", XML_FLAG_METHOD_WHERE);
      root.setAttribute("unique", isSelectUnique() ? "yes" : "no");

      //private          PSCollection    m_whereClauses = null;
      if (m_whereClauses != null) {
         Element whereNode = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, "WhereClauses");

         int size = m_whereClauses.size();
         PSComponent entry = null;
         for (int i=0; i < size; i++)
         {
            entry = (PSComponent)m_whereClauses.get(i);
            if(null == entry)
               continue;
            whereNode.appendChild(entry.toXml(doc));
         }
      }

      //private          PSCollection    m_sortedColumns = null;
      if (m_sortedColumns != null) {
         Element sortNode = PSXmlDocumentBuilder.addEmptyElement(
            doc, root, "Sorting");

         int size = m_sortedColumns.size();
         PSComponent entry = null;
         for (int i=0; i < size; i++)
         {
            entry = (PSComponent)m_sortedColumns.get(i);
            if(null == entry)
               continue;
            sortNode.appendChild(entry.toXml(doc));
         }
      }

      //private          String          m_nativeStatement = "";
      PSXmlDocumentBuilder.addElement(   doc, root, "nativeStatement",
         m_nativeStatement);

      //private          boolean         m_caching = false;  //caching on or off
      //private          int             m_cacheType = DS_CACHE_TYPE_INTERVAL;   //cache type DS_CACHE_TYPE_XXXX
      Element cacheNode = doc.createElement ("Caching");
      cacheNode.setAttribute("enabled", m_caching ? "yes" : "no");
      cacheNode.setAttribute("type", getCacheTypeString());
      root.appendChild(cacheNode);

      //private          java.util.Date  m_cacheAgeTime = null;
      if (m_cacheAgeTime != null)
         PSXmlDocumentBuilder.addElement(   doc, cacheNode, "ageTime",
         m_DateFormatter.format(m_cacheAgeTime));

      //private          int             m_cacheAgeInterval = 0;
      PSXmlDocumentBuilder.addElement(   doc, cacheNode, "ageInterval",
         String.valueOf(m_cacheAgeInterval));

      return root;
   }

   /**
    * This method is called to populate a PSDataSelector Java object
    * from a PSXDataSelector XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataSelector
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
   throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (false == ms_NodeType.equals (sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         sTemp = tree.getElementData("unique");
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_selector = DS_DISTINCT;
         else
            m_selector = 0;

         // what's the selection method
         sTemp = tree.getElementData("method");
         if (sTemp == null) {
            Object[] args = { ms_NodeType, "method", "" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }
         else if (sTemp.equals(XML_FLAG_METHOD_WHERE))
            m_selector |= DS_BY_WHERE_CLAUSE;
         else if (sTemp.equals(XML_FLAG_METHOD_NATIVE))
            m_selector |= DS_BY_NATIVE_STATEMENT;
         else {
            Object[] args = { ms_NodeType, "method", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
         }

         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent(); // cur = <PSXDataSelector>

         // do where clauses
         m_whereClauses.clear();

         String curNodeType = PSWhereClause.ms_NodeType;
         if (tree.getNextElement("WhereClauses", firstFlags) != null)
         {
            if (tree.getNextElement(curNodeType, firstFlags) != null)
            {
               //private          PSCollection    m_whereClauses = null;
               PSWhereClause whereClause;
               do
               {
                  whereClause = new PSWhereClause(
                     (Element)tree.getCurrent(), parentDoc, parentComponents);
                  m_whereClauses.add(whereClause);
               }
               while (tree.getNextElement(curNodeType, nextFlags) != null);
            }
         }

         tree.setCurrent(cur);

         // do sorted columns
         m_sortedColumns.clear();

         if (tree.getNextElement("Sorting", firstFlags) != null)
         {
            curNodeType = PSSortedColumn.ms_NodeType;
            if (tree.getNextElement(curNodeType, firstFlags) != null)
            {
               PSSortedColumn col;
               do
               {
                  col = new PSSortedColumn(
                     (Element)tree.getCurrent(), parentDoc, parentComponents);
                  m_sortedColumns.add(col);
               }
               while (tree.getNextElement(curNodeType, nextFlags) != null);
            }
         }

         // pop
         tree.setCurrent(cur);

         try {      //private          String          m_nativeStatement = "";
            setNativeStatement(tree.getElementData("nativeStatement"));
         } catch (IllegalArgumentException e) {
            throw new PSUnknownNodeTypeException(ms_NodeType, "nativeStatement",
                           new PSException (e.getLocalizedMessage()));
         }

         //private   boolean   m_caching = false;  //caching on or off
         //private   int   m_cacheType = DS_CACHE_TYPE_INTERVAL; //DS_CACHE_TYPE_XXXX
         if (tree.getNextElement("Caching", firstFlags) != null) {
            sTemp = tree.getElementData("enabled", false);
            m_caching = (sTemp != null) && sTemp.equalsIgnoreCase("yes");

            sTemp = tree.getElementData("type", false);
            if ((sTemp == null) && m_caching)
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.DATASEL_CACHE_TYPE_REQD);
            else if (sTemp != null) {
               if (sTemp.equals(XML_FLAG_CACHE_TYPE_INTERVAL))
                  m_cacheType = DS_CACHE_TYPE_INTERVAL;
               else if (sTemp.equals(XML_FLAG_CACHE_TYPE_TIME))
                  m_cacheType = DS_CACHE_TYPE_TIME;
               else if (sTemp.equals(XML_FLAG_CACHE_TYPE_BOTH))
                  m_cacheType = DS_CACHE_TYPE_TIME_INTERVAL;
               else {
                  Object[] args = { ms_NodeType, "Caching/type", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
               }
            }

            //private          int             m_cacheAgeInterval = 0;
            sTemp = tree.getElementData("ageInterval", false);
            if ((sTemp != null) && (sTemp.length() > 0)) {
               try {
                  m_cacheAgeInterval = Integer.parseInt(sTemp);
               } catch (NumberFormatException e) {
                  Object[] args = { ms_NodeType, "Caching/ageInterval", sTemp };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }

            //private          java.util.Date  m_cacheAgeTime = null;
            sTemp = tree.getElementData("ageTime", false);
            if ((sTemp != null) && (sTemp.length() > 0)) {
               try {
                  m_cacheAgeTime = m_DateFormatter.parse(sTemp);
               } catch (ParseException e) {
                  Object[] args = { ms_NodeType, "Caching/ageTime",
                                    "(Value: " + sTemp + ") " + e.toString() };
                  throw new PSUnknownNodeTypeException(
                     IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
               }
            }
         }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      // validate type of query
      {
         Object[] args = null;
         // make sure mutually exclusive bits are not specified
         if ((0 != (DS_BY_WHERE_CLAUSE & m_selector))
            && (0 != (DS_BY_NATIVE_STATEMENT & m_selector)))
         {
            args = new Object[] { "" + m_selector };
            cxt.validationError(this,
               IPSObjectStoreErrors.DATASEL_SEL_TYPE_INVALID, args);
         }

         // turn on all invalid bits and make sure none of them are specified
         final int all_flags_compliment = ~(
            DS_BY_WHERE_CLAUSE | DS_BY_NATIVE_STATEMENT | DS_DISTINCT);

         if (0 != (m_selector & all_flags_compliment))
         {
            args = new Object[] { "" + m_selector };
            cxt.validationError(this,
               IPSObjectStoreErrors.DATASEL_SEL_TYPE_INVALID, args);
         }
      }

      // validate the cache type and the presence of any fields that
      // depend on the cache type
      if (m_caching)
      {
         switch (m_cacheType)
         {
         case DS_CACHE_TYPE_INTERVAL:
            break; // ok
         case DS_CACHE_TYPE_TIME:
            // fall through
         case DS_CACHE_TYPE_TIME_INTERVAL:
            // we need a cache age time if we cache by time or time+interval
            if (null == m_cacheAgeTime)
            {
               String dsName = "unknown";
               java.util.List parentList = cxt.getParentList();

               // go up the parent list looking for the data set name
               for (int i = parentList.size() - 1; i >= 0; i--)
               {
                  Object pnt = parentList.get(i);
                  if (pnt instanceof PSDataSet)
                  {
                     dsName = ((PSDataSet)pnt).getName();
                     break;
                  }
               }

               cxt.validationError(this,
                  IPSObjectStoreErrors.DATASEL_CACHE_AGE_TIME_REQUIRED,
                  dsName);

            }
            break; // end valid cache types
         default:
            cxt.validationError(
               this,
               IPSObjectStoreErrors.DATASEL_CACHE_TYPE_INVALID,
               "" + m_cacheType);

         }
      }

      // validate native statement
      IllegalArgumentException ex = validateNativeStatement(m_nativeStatement);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      if (m_cacheAgeInterval < 0)
         cxt.validationError(this,
            IPSObjectStoreErrors.DATASEL_CACHE_AGE_INTERVAL_INVALID,
            "" + m_cacheAgeInterval);

      // validate children
      cxt.pushParent(this);

      try
      {
         // validate where conditionals
         if (m_whereClauses != null)
         {
            for (int i = 0; i < m_whereClauses.size(); i++)
            {
               Object o = m_whereClauses.get(i);
               PSConditional cond = (PSConditional)o;
               cond.validate(cxt);
            }
         }

         // validate sorted columns
         if (m_sortedColumns != null)
         {
            for (int i = 0; i < m_sortedColumns.size(); i++)
            {
               Object o = m_sortedColumns.get(i);
               PSSortedColumn scol = (PSSortedColumn)o;
               scol.validate(cxt);
            }
         }
      }
      finally
      {
         cxt.popParent();
      }
   }


   private String getCacheTypeString()
   {
      String sRet = "";
      if (m_cacheType == DS_CACHE_TYPE_INTERVAL)
         sRet = XML_FLAG_CACHE_TYPE_INTERVAL;
      else if (m_cacheType == DS_CACHE_TYPE_TIME)
         sRet = XML_FLAG_CACHE_TYPE_TIME;
      else if (m_cacheType == DS_CACHE_TYPE_TIME_INTERVAL)
         sRet = XML_FLAG_CACHE_TYPE_BOTH;
      return sRet;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDataSelector)) return false;
      if (!super.equals(o)) return false;
      PSDataSelector that = (PSDataSelector) o;
      return m_selector == that.m_selector &&
              m_caching == that.m_caching &&
              m_cacheType == that.m_cacheType &&
              m_cacheAgeInterval == that.m_cacheAgeInterval &&
              Objects.equals(m_cacheAgeTime, that.m_cacheAgeTime) &&
              Objects.equals(m_whereClauses, that.m_whereClauses) &&
              Objects.equals(m_nativeStatement, that.m_nativeStatement) &&
              Objects.equals(m_sortedColumns, that.m_sortedColumns) &&
              Objects.equals(m_DateFormatter, that.m_DateFormatter);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_selector, m_caching, m_cacheType, m_cacheAgeInterval, m_cacheAgeTime, m_whereClauses, m_nativeStatement, m_sortedColumns, m_DateFormatter);
   }

   // the low word is the type
   private   static   final   int   DS_BY_WHERE_CLAUSE      =   0x00000001;
   private   static   final   int   DS_BY_NATIVE_STATEMENT   =   0x00000002;
   // the high word contains any flags
   private   static   final   int   DS_FLAGS                  =   0xFFFF0000;
   private   static   final   int   DS_DISTINCT               =   0x00010000;

   private   static   final   int   DS_CACHE_TYPE_INTERVAL         =   0;   //interval   based   cache
   private   static   final   int   DS_CACHE_TYPE_TIME            =   1;   //time   based   caching
   private   static   final   int   DS_CACHE_TYPE_TIME_INTERVAL   =   2;   //both.   Should   we   use   flags?

   private            int               m_selector   =   DS_BY_WHERE_CLAUSE;      //type of   query
   private            boolean            m_caching   =   false;   //caching   on   or   off
   private            int               m_cacheType   =   DS_CACHE_TYPE_INTERVAL;   //cache   type   DS_CACHE_TYPE_XXXX
   private            int               m_cacheAgeInterval   =   0;
   private            java.util.Date      m_cacheAgeTime   =   null;
   private            PSCollection      m_whereClauses   =   null;
   private            String            m_nativeStatement   =   "";
   private            PSCollection      m_sortedColumns = null;

   private   SimpleDateFormat   m_DateFormatter   =   new   SimpleDateFormat("hh:mm");

   private   static   final   String      XML_FLAG_CACHE_TYPE_INTERVAL   =   "interval";
   private   static   final   String      XML_FLAG_CACHE_TYPE_TIME      =   "time";
   private   static   final   String      XML_FLAG_CACHE_TYPE_BOTH      =   "timeAndInterval";

   private   static   final   String      XML_FLAG_METHOD_WHERE         =   "whereClause";
   private   static   final   String      XML_FLAG_METHOD_NATIVE         =   "nativeStatement";

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXDataSelector";
}
