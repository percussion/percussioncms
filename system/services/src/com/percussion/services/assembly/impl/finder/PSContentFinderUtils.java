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

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase.ContentItem;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides various helper methods for content finders.
 *
 * @author YuBingChen
 */
public class PSContentFinderUtils
{
   /**
    * Get the specified argument from the passed selectors. If no argument is
    * present then the default is returned.
    * 
    * @param params the parameters used to look up the value, never
    *            <code>null</code>
    * @param key the key, never <code>null</code> or empty
    * @param defaultvalue the default value to use
    * @return the value, or <code>null</code> if not defined
    */
   public static String getValue(Map<String, ? extends Object> params, String key,
         String defaultvalue)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("params may not be null");
      }
      if (StringUtils.isBlank(key))
      {
         throw new IllegalArgumentException("key may not be null or empty");
      }
      Object val = params.get(key);
      if (val instanceof String)
         return (String) val;
      else if (val instanceof String[])
      {
         String vals[] = (String[]) val;
         if (vals.length == 0)
            throw new RuntimeException("No value for " + key);
         return vals[0];
      }
      else if (val != null)
         return val.toString();
      else
         return defaultvalue;
   }

   /**
    * Get the locale from the available information. The default value is taken
    * from the source node using the sys_lang field.
    * 
    * @param source the source assembly item, never <code>null</code>.
    * @param params the parameters that may contain
    *            {@link IPSHtmlParameters#SYS_LANG} value, never
    *            <code>null</code>
    * 
    * @return the locale, or <code>null</code> if it cannot be determined. The
    *         returned locale will be in normal form with an underscore for use
    *         with java, i.e. en_us not en-us.
    */
   public static String getLocale(IPSAssemblyItem source, Map<String, Object> params)
   {
      Node n = source.getNode();
      try
      {
         if (n.hasProperty(IPSHtmlParameters.SYS_LANG))
         {
            String locale = getValue(params, IPSHtmlParameters.SYS_LANG, n
                  .getProperty(IPSHtmlParameters.SYS_LANG).getString());
            return locale.replace("-", "_");
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         ms_log.error(e);
         return null;
      }
   }

   /**
    * Reorder the return set by a different algorithm. This first extracts the
    * content ids from the set. Then it does a JSR-170 query with the order
    * by clause on 'nt:base' and this returns the content IDs. That is then
    * turned into a map that yields ascending sort order, which is used with
    * a different comparator.
    * 
    * @param rval the original results, may be empty but not <code>null</code>
    * @param orderby the order-by string, never <code>null</code> or empty
    * @param locale the locale to use in the search to ensure the correct
    * collating sequence. If <code>null</code> or empty the JVM locale is used.
    * @return the reordered set
    */
   public static Set<ContentItem> reorderItems(Set<ContentItem> rval, String orderby, 
         String locale)
   {
      if (rval.size() == 0) return rval;
      
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      StringBuilder query = new StringBuilder();
      try
      {
         query.append("select ");
         query.append(IPSContentPropertyConstants.RX_SYS_CONTENTID);
         query.append(" from nt:base where ");
         boolean first = true;
         for(ContentItem item : rval)
         {
            PSLegacyGuid itemlg = (PSLegacyGuid) item.getItemId();
            if (first)
            {
               first = false;
            }
            else
            {
               query.append(" or ");
            }
            query.append(IPSContentPropertyConstants.RX_SYS_CONTENTID);
            query.append(" = ");
            query.append(itemlg.getContentId());
         }
         query.append(" order by ");
         query.append(orderby);
         Query q = cmgr.createQuery(query.toString(), Query.SQL);
         QueryResult res = cmgr.executeQuery(q, -1, null, locale);
         RowIterator riter = res.getRows();
         Set<ContentItem> newset
            = new TreeSet<ContentItem>(new PSQueryResultOrderComparator(riter));
         newset.addAll(rval);
         int index = 0;
         for(ContentItem item : newset)
         {
            item.setSortrank(index++);
         }
         rval = newset;
      }
      catch (Exception e)
      {
         ms_log.error("Problem reordering rel slot query", e);
      }
      return rval;
   }
   
   /**
    * Finds the site and folder of the item id in the slot item and sets it on
    * the supplied slot item. If the item exists in more than one site, then
    * sets it to the first site returned by
    * {@link IPSSiteManager#getItemSites(IPSGuid)} method.
    * 
    * @param slotItem container item on which the site id needs to be set, if
    *           <code>null</code> does nothing.
    * @param isSetFolderID if <code>true</code>, then setting the folder id.
    * @throws PSSiteManagerException
    */
   public static void setSiteFolderId(ContentItem slotItem, boolean isSetFolderID)
         throws PSSiteManagerException
   {
      if (slotItem == null)
         return;
      
      IPSGuid guid = slotItem.getItemId();
      IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
      List<IPSSite> itemSites = smgr.getItemSites(guid);
      if (!itemSites.isEmpty())
      {
         IPSGuid siteId = itemSites.get(0).getGUID();
         slotItem.setSiteId(siteId);
         if(isSetFolderID)
         {
            IPSGuid folderId = smgr.getSiteFolderId(siteId, guid);
            slotItem.setFolderId(new PSLegacyGuid(folderId.getUUID(), 0));
         }
      }
   }
   
   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSContentFinderUtils.class);
}
