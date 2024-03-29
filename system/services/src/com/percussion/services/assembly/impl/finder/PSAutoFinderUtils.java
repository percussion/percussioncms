/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.assembly.impl.finder;


import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItemOrder;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.collections.PSFacadeMap;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.DataException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.services.assembly.IPSContentFinder.PARAM_QUERY;
import static com.percussion.services.assembly.IPSContentFinder.PARAM_TYPE;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getLocale;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;
import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.setSiteFolderId;

/**
 * This class provides various helper methods for auto content finders.
 */
@PSBaseBean("sys_autoFinderUtils")
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class PSAutoFinderUtils implements IPSAutoFinderUtils {


   public PSAutoFinderUtils(){ /* NOOP - for spring */ }

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
   @Override
   public Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
                                           long slotId, Map<String, Object> params, IPSGuid templateId) throws PSSiteManagerException, PSNotFoundException, RepositoryException {
      Set<ContentItem> rval = new TreeSet<>(new ContentItemOrder());
      String type;
      String query = "";
      String locale = getLocale(sourceItem, params);
      type = getValue(params, PARAM_TYPE, "sql").toLowerCase();

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
         log.error("Exception during query: {} for slot id: {} the formatted sql is: {} Error: {}",
                 query,
                 slotId,
                 se.getSQL(),
                 PSExceptionUtils.getMessageForLog(se));
         throw se;

      } catch (PSSiteManagerException | PSNotFoundException | RepositoryException e) {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw(e);
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
   private static final Logger log = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);
}
