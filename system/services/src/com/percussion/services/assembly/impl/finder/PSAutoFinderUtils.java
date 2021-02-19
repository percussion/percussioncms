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
package com.percussion.services.assembly.impl.finder;


import static com.percussion.services.assembly.IPSContentFinder.PARAM_QUERY;
import static com.percussion.services.assembly.IPSContentFinder.PARAM_TYPE;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getLocale;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.setSiteFolderId;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItemOrder;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.collections.PSFacadeMap;
import com.percussion.utils.guid.IPSGuid;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.DataException;

/**
 * This class provides various helper methods for auto content finders.
 */
public class PSAutoFinderUtils
{
   /**
    * Gets the content items returned by a query, which is specified in
    * the parameter. The allowed parameters are the following:
    * <table>
    * <tr>
    * <th>Parameter</th>
    * <th>Description</th>
    * </tr>
    * <tr>
    * <td>query</td>
    * <td>The JSR-170 query to be performed</td>
    * </tr>
    * <tr>
    * <td>type</td>
    * <td>The type of the query, either "sql" or "xpath". Note that only "sql"
    * support is officially supported at this time.</td>
    * </tr>
    * <tr>
    * <td>mayHaveCrossSiteLinks</td>
    * <td>If this is true, then a The maximum number of results to use from the query, zero or negative
    * indicates no limit.</td>
    * </tr>
    * </table>
    * 
    * @param sourceItem the source assembly item, never <code>null</code>. for
    * the default method, it is the owner of the related (returned) items.
    * @param slotId the ID of the container. This is not used.
    * @param params the parameters passed to the finder. There is only one
    * optional parameter, {@link PSContentFinderBase#ORDER_BY}. The returned
    * items will be re-ordered according to the specified parameter; otherwise
    * the returned items are ordered by {@link PSContentFinderBase.ContentItem}.
    * @param templateId the template ID to be set for the returned items, 
    * it may be <code>null</code>.
    * 
    * @return a set of related items, never <code>null</code>, but may be empty.
    * The items can be re-ordered with {@link ContentItemOrder}. 
    */   
   public Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         long slotId, Map<String, Object> params, IPSGuid templateId)
   {
      Set<ContentItem> rval = new TreeSet<>(new ContentItemOrder());
      String type;
      String query = "";
      String locale = getLocale(sourceItem, params);
      type = getValue(params, PARAM_TYPE, "sql").toLowerCase();
      //template = getValue(selectors, PARAM_TEMPLATE, null);
      query = getValue(params, PARAM_QUERY, null);
      String cslink = getValue(params,
            PARAM_MAY_HAVE_CROSS_SITE_LINKS, null);
      boolean includeSiteId = StringUtils.isNotBlank(cslink)
            && cslink.equalsIgnoreCase("true");
      if (StringUtils.isBlank(query))
      {
         throw new IllegalArgumentException("query is a required argument");
      }

      // Parse and adjust query
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      // Run the query
      Map<String, Object> queryargs = new PSFacadeMap<>(params);
      QueryResult result = null;
      try
      {
         PSQuery iquery = (PSQuery) cmgr.createQuery(query, type);
         // Setup the projection
         iquery.addProjectionField(IPSContentPropertyConstants.RX_SYS_CONTENTID);
         iquery.addProjectionField(IPSContentPropertyConstants.RX_SYS_REVISION);
         result = cmgr.executeQuery(iquery, -1, queryargs, locale);
         RowIterator riter = result.getRows();
         int order = 1;
         boolean isFldInQuery = iquery.doesQueryHasField(
               IPSContentPropertyConstants.RX_SYS_FOLDERID);
         while (riter.hasNext())
         {
            IPSGuid itemId;
            Row r = riter.nextRow();
            Value cid = r
                  .getValue(IPSContentPropertyConstants.RX_SYS_CONTENTID);
            Value rid = r.getValue(IPSContentPropertyConstants.RX_SYS_REVISION);
            itemId = new PSLegacyGuid((int) cid.getLong(), (int) rid.getLong());
            ContentItem si = new ContentItem(itemId, templateId, order++);
            // if we have a folder id in the query set the folder id on slot
            // item
            if (isFldInQuery)
            {
               Value fid = r
                     .getValue(IPSContentPropertyConstants.RX_SYS_FOLDERID);
               //If items are not in any folder then the fid will become null.
               //We do not add the folder id to slot item in that case.
               if(fid!=null)
                  si.setFolderId(new PSLegacyGuid((int) fid.getLong(), 0));
            }
            if(includeSiteId)
            {
              setSiteFolderId(si, true);
            }
            rval.add(si);
         }
      }
      catch (DataException se)
      {
         String errMsg = "Exception during query \"" + query + "\" for slot id=" + slotId + ", the formatted sql is: "
         + se.getSQL();
         ms_log.error(errMsg, se);
         throw new RuntimeException(errMsg, se);

      }
      catch (Exception e)
      {
         String errMsg = "Exception during query \"" + query + "\" for slot id=" + slotId;
         ms_log.error(errMsg, e);
         throw new RuntimeException(errMsg, e);
      }
      return rval;
   }

   /**
    * Name of the parameter, which indicates if need to set the site id for the
    * returned items.
    */
   private static final String PARAM_MAY_HAVE_CROSS_SITE_LINKS = 
      "mayHaveCrossSiteLinks";

   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSAutoFinderUtils.class);
}
