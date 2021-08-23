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
package com.percussion.services.assembly.impl.finder;

import com.percussion.cms.PSCmsException;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSContentFinder;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.data.PSFilterItem;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.utils.orm.PSDataCollectionHelper;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSStopwatch;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.percussion.services.assembly.impl.finder.PSContentFinderUtils.getValue;

/**
 * The base content finder provides the common functionality needed 
 * by each content finder implementation. The general pattern is to implement 
 * the abstract method {@link #getContentItems(IPSAssemblyItem, T, Map)}. 
 * This method provides the information to a general implementation of the
 * {@link #find(IPSAssemblyItem, Object, Map)} method.
 * <p>
 * It is important to note that while this mechanism works well for most
 * cases, there may be cases where an implementer must simply write their own
 * <code>find</code> method.
 * <p>
 * The base class find method filters and organizes the returned items
 * into a set of assembly items to be assembled.
 * 
 * @author dougrand
 * @param <T> 
 */
public abstract class PSContentFinderBase<T extends Object> 
   implements IPSContentFinder<T>
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
   }

   /**
    * Represents a single slot item to be filtered, sorted and output as an
    * assembly item
    */
   public static class ContentItem extends PSFilterItem
   {      
      /**
       * The template to be applied, may be <code>null</code>.
       */
      private IPSGuid m_template;
      
      /**
       * The relationship, may be <code>null</code> as an item may be part
       * of some calculated relationship.
       */
      private IPSGuid m_relationshipId;

      private IPSGuid m_ownerId;
      
      /**
       * The ordering information for the slot item, used to determine ordering
       * out of the content finder.
       */
      private int m_sortrank;

      /**
       * see {@link #getWidgetName()}
       */
      private String m_widgetName;
      
      /**
       * Ctor
       * 
       * @param itemId content item guid, never <code>null</code>
       * @param templateId template guid, may be <code>null</code>
       * @param sortrank sort order
       */
      public ContentItem(IPSGuid itemId, IPSGuid templateId, int sortrank) 
      {
         super(itemId, null, null);
         m_template = templateId;
         m_sortrank = sortrank;
      }
      
      /**
       * @return Returns the sortrank.
       */
      public int getSortrank()
      {
         return m_sortrank;
      }

      /**
       * @param sortrank The sortrank to set.
       */
      public void setSortrank(int sortrank)
      {
         m_sortrank = sortrank;
      }

      /**
       * @return Returns the template.
       */
      public IPSGuid getTemplate()
      {
         return m_template;
      }

      /**
       * @param template The template to set.
       */
      public void setTemplate(IPSGuid template)
      {
         m_template = template;
      }

      /**
       * @return Returns the relationshipId.
       */
      public IPSGuid getRelationshipId()
      {
         return m_relationshipId;
      }

      public IPSGuid getOwnerId()
      {
         return m_ownerId;
      }
      
      public void setOwnerId(IPSGuid ownerId)
      {
         m_ownerId = ownerId;
      }
      
      /**
       * @param relationshipId The relationshipId to set.
       */
      public void setRelationshipId(IPSGuid relationshipId)
      {
         m_relationshipId = relationshipId;
      }
      
      /**
       * The name of the widget of the relationship.
       * @return the may be <code>null</code> or empty.
       */
      public String getWidgetName()
      {
         return m_widgetName;
      }
      
      /**
       * Sets the new widget name.
       * @param name the new widget name, it may be <code>null</code> or empty.
       */
      public void setWidgetName(String name)
      {
         m_widgetName = name;
      }
   }
   
   /**
    * Gets the items to be operated on. The items do not need to be ordered or
    * filtered. This method is supplied by an implementer is normally called by
    * the {@link #find(IPSAssemblyItem, Object, Map)} method to get initial
    * content.
    * <p>
    * It is important to note that while this mechanism works well for most
    * cases, there may be cases where an implementer must simply write their own
    * find method.
    * 
    * @param sourceItem the source assembly item,never <code>null</code>.
    * @param slot the ID of the container. It may be <code>null</code>
    * 
    * @return a set of items, never <code>null</code>, but may be empty. The
    *         items can be filtered, re-ordered and turned into assembly items.
    *         The set must be ordered if the containers are to be ordered. Use
    *         {@link ContentItemOrder} to order the items in the set.
    */   
   @SuppressWarnings({"cast"})
   abstract protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
         T slot, Map<String, Object> params) throws PSNotFoundException, RepositoryException, PSFilterException, PSAssemblyException;

   @SuppressWarnings({"unchecked"})
   public List<IPSAssemblyItem> find(IPSAssemblyItem sourceItem,
         T slot, Map<String, Object> params)
           throws RepositoryException, PSFilterException, PSAssemblyException, PSNotFoundException {
      if (sourceItem == null)
      {
         throw new IllegalArgumentException("sourceItem may not be null");
      }
      if (slot == null)
      {
         throw new IllegalArgumentException("slot may not be null");
      }

      // get the items
      Set<ContentItem> allContentItems = getContentItems(sourceItem, slot, params);

      String templatename = getValue(params, "template", null);
      int max_results = Integer.parseInt(getValue(params,
            PARAM_MAX_RESULTS, "0"));

      Set<ContentItem> slotitems = getFilteredItems(allContentItems, sourceItem,
            params, max_results, slot);
      
      // Now create the return array
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      
      // Create return items for assembly
      List<IPSAssemblyItem> items = new ArrayList<>();

      int index = 1;
      boolean had_more = false;
      for (ContentItem slotitem : slotitems)
      {
         // Limit results here, after the filtering has occurred
         if (max_results > 0 && index > max_results)
         {
            had_more = true;
            break;
         }

         IPSAssemblyItem clone = createAssemblyItem(slotitem, sourceItem, 
               templatename, asm, slot);
         clone.setOwnerId(slotitem.getOwnerId());
         
         items.add(clone);
      }
      Map<String, Object> sys = (Map<String, Object>) sourceItem.getBindings().get("$sys");
      if (sys != null)
      {
         sys.put("hasMore", had_more);
      }

      return items;
   }

   
   /**
    * Creates an assembly item for the specified item that is in the target
    * container. The created assembly item is cloned from a specified source
    * item, and the other properties are set according to the given parameters.
    * 
    * @param slotitem the item in the target container, never <code>null</code>.
    * @param originalItem the original assembly item, used to clone an 
    * assembly item, never <code>null</code>.
    * @param templatename the name of the template to assemble, 
    * may be <code>null</code>.
    * @param asm the assembly service, never <code>null</code>.
    * @param slot the target container of the created assembly item.
    * 
    * @return the created assembly item, never <code>null</code>.
    * 
    * @throws PSAssemblyException if failed to create the assembly item.
    */
   protected IPSAssemblyItem createAssemblyItem(ContentItem slotitem, 
         IPSAssemblyItem originalItem, String templatename, IPSAssemblyService asm,
         T slot) throws PSAssemblyException
   {
      IPSAssemblyResult workitem = (IPSAssemblyResult) originalItem;
      IPSAssemblyItem clone = null;
      try
      {
         Map<String, IPSGuid> optionalParams = new HashMap<>();

         if (slotitem.getSiteId() != null)
         {
            optionalParams.put(IPSHtmlParameters.SYS_SITEID, slotitem
                  .getSiteId());
            if (originalItem.getSiteId() != null
                  && !slotitem.getSiteId().equals(originalItem.getSiteId()))
            {
               optionalParams.put(IPSHtmlParameters.SYS_ORIGINALSITEID,
                     originalItem.getSiteId());
            }
         }
         else if (originalItem.getSiteId() != null)
         {
            // Note that the site id will be cloned with the request if
            // not overwritten here
            optionalParams.put(IPSHtmlParameters.SYS_SITEID, originalItem
                  .getSiteId());
         }

         if (slotitem.getFolderId() != null)
         {
            optionalParams.put(IPSHtmlParameters.SYS_FOLDERID, slotitem
                  .getFolderId());
         }
         optionalParams.put(IPSHtmlParameters.SYS_RELATIONSHIPID, slotitem
               .getRelationshipId());
         
         clone = getCloneAssemblyItem(workitem, asm, templatename, slotitem
               .getItemId(), slotitem.getTemplate(), optionalParams);

         if (slotitem.getFolderId() == null)
         {
            clone.removeParameterValue(IPSHtmlParameters.SYS_FOLDERID);
         }

         return clone;
      }
      catch (Exception e)
      {
         throw new PSAssemblyException(IPSAssemblyErrors.ITEM_CREATION, e);
      }
   }

   /**
    * The comparator used to order the returned list from {@link #find(IPSAssemblyItem, Object, Map)}
    * @return the comparator, never <code>null</code>.
    */
   protected Comparator<ContentItem> getComparator(T slot) throws PSNotFoundException {
      return new ContentItemOrder();
   }
   
   /**
    * Comparator to order slot relationships
    */
   protected static class ContentItemOrder implements Comparator<ContentItem>
   {
      /**
       * Compare slot items for ordering
       * 
       * @param s1 slot item one, never <code>null</code>
       * @param s2 slot item two, never <code>null</code>
       * @return positive number for increasing order, negative for decreasing
       *         order, zero for no change
       */
      public int compare(ContentItem s1, ContentItem s2)
      {
         if (s1 == null)
         {
            throw new IllegalArgumentException("s1 may not be null");
         }
         if (s2 == null)
         {
            throw new IllegalArgumentException("s2 may not be null");
         }
         if (s1.getSortrank() == s2.getSortrank())
         {
            /*
             * If this comparator returns zero, a set based on this comparator
             * will treat the two slot items as equal (and only store one of
             * them).
             * 
             * Therefore, if by some chance the sort ranks are the same, compare
             * the items using their relationship ids (if set) or their item
             * ids.
             */
            IPSGuid id1 = s1.getRelationshipId();
            IPSGuid id2 = s2.getRelationshipId();
            if (id1 == null || id2 == null)
            {
               id1 = s1.getItemId();
               id2 = s2.getItemId();
            }
            return (id1.longValue() < id2.longValue() ? -1 : 1);
         }
         return s1.getSortrank() - s2.getSortrank();
      }
   }

   /**
    * Filters the specified items.
    * <p>
    * Note, it filters the items in chunks of
    * {@link PSDataCollectionHelper#MAX_IDS} or less. This is to avoid the calls
    * to {@link PSDataCollectionHelper#createIdSet(Session, Collection)} and
    * {@link PSDataCollectionHelper#clearIdSet(Session, long)}, which may leads to database
    * deadlocks when involving a lot of IDs (insert, query and delete).
    * 
    * @param allItems the to be filtered items, never <code>null</code>, but
    * may be empty.
    * @param sourceItem the source assembly item, guaranteed never
    * <code>null</code> because this is called from
    * {@link IPSSlotContentFinder#find(IPSAssemblyItem, Object, Map)}
    * @param selectors the selectors, may be <code>null</code>
    * @param max_results the max number of items that are needed for the
    * returned items. There is no limit if it is less than 1.
    * 
    * @return a set of slot items, never <code>null</code>, that are
    * filtered, ordered and turned into assembly items. The set must be ordered
    * if the slots are to be ordered. Use {@link #getComparator(Object)} to order slot
    * items in the set.
    * 
    * @throws PSFilterException if an error occurs during filtering process.
    */
   protected Set<ContentItem> getFilteredItems(Set<ContentItem> allItems,
         IPSAssemblyItem sourceItem, Map<String, Object> selectors,
         int max_results, T slot) throws PSFilterException, PSNotFoundException {
      // figure out maximum filtered items, which must be less than
      // PSDataCollectionHelper.MAX_IDS - 1; optimize to 100 or 
      // twice as "max_results" if it is > 0
      //
      // Note, filtering 5000 items, 
      //       took about 5.5s (in 100 chunks) vs. 4s (in 649 chunks).
      //       However, in production env. items are most likely qualified
      //       (e.g. in public state). In case any performance issue with
      //       100 chunk/grouping, we can fall back to 649 later 9/9/2009.
      int maxChunkItems = PSDataCollectionHelper.MAX_IDS - 1;
      if (max_results > 0 && (max_results * 2) < maxChunkItems)
      {
         int tmpChunkItems = Math.max(100, max_results * 2);
         maxChunkItems = Math.min(tmpChunkItems, maxChunkItems);
      }

      Set<ContentItem> slotitems = new TreeSet<>(getComparator(slot));
      Set<ContentItem> tmpItems = new TreeSet<>(getComparator(slot));
      IPSAssemblyResult workitem = (IPSAssemblyResult) sourceItem;

      PSStopwatch watch = new PSStopwatch();
      watch.start();

      for (ContentItem slotitem : allItems)
      {
         // stop if we have enough
         if (max_results > 0 && (slotitems.size() >= max_results || 
               tmpItems.size() >= max_results))
         {
            break;
         }
         
         slotitem.setSiteId(workitem.getSiteId());
         tmpItems.add(slotitem);
         
         // filter in chunks of items
         if (tmpItems.size() >= maxChunkItems)
         {
            Set<ContentItem> filteredItems = filter(tmpItems, workitem
                  .getFilter(), selectors);
            slotitems.addAll(filteredItems);
            tmpItems.clear();
         }
      }
      // get the remaining if there are any
      if (tmpItems.size() > 0)
      {
         Set<ContentItem> filteredItems = filter(tmpItems, workitem.getFilter(),
               selectors);
         slotitems.addAll(filteredItems);
      }

      watch.stop();
      if (ms_log.isDebugEnabled())
      {
         ms_log.debug("slotitems size = " + slotitems.size() + ", maxchunk = "
               + maxChunkItems + ", elapse = " + watch.toString());
      }

      return slotitems;
   }
   
   /**
    * Filter slot items
    * 
    * @param items items to filter
    * @param filter filter to apply
    * @param selectors the parameters to supply to the filter, may be
    *           <code>null</code> or empty
    * @return a list of filtered items, may be empty but never <code>null</code>
    * @throws PSFilterException
    */
   protected Set<ContentItem> filter(Set<ContentItem> items, IPSItemFilter filter,
         Map<String, Object> selectors) throws PSFilterException
   {
      List<IPSFilterItem> temp = new ArrayList<>();
      Map<String, String> params = new HashMap<>();
      //FIXME: This code is not handling null keys or values gracefully
      for (Map.Entry<String, Object> selector : selectors.entrySet())
      {
         Object value = selector.getValue();
         if (value instanceof String[])
         {
            String p[] = (String[]) value;
            if (p.length > 0)
            {
               params.put(selector.getKey(), p[0]);
            }
         }
         else
         {
            params.put(selector.getKey(), value.toString());
         }
      }
      temp.addAll(items);
      if (filter != null)
      {
         temp = filter.filter(temp, params);
         items.clear();
         for (IPSFilterItem f : temp)
         {
            items.add((ContentItem) f);
         }
      }

      return items;
   }

   /**
    * Clone and setup the assembly item in preparation to create an item for the
    * content in the slot.
    * 
    * @param originalItem the original assembly item, never <code>null</code>
    * @param asm the assembly service, never <code>null</code>
    * @param templatename the name of the template to assemble, might be
    *           <code>null</code>. 
    * @param itemid the guid for the item being assembled, never
    *           <code>null</code>
    * @param templateid the id of the template, may be <code>null</code>, but
    *           either templatename or templateid must be specified
    * @param optionalParams optional parameters to add, may be empty but not
    *           <code>null</code>
    * @return item for content in slot
    * @throws CloneNotSupportedException
    * @throws PSAssemblyException
    * @throws PSCmsException
    */
   static IPSAssemblyItem getCloneAssemblyItem(
         IPSAssemblyItem originalItem, IPSAssemblyService asm,
         String templatename, IPSGuid itemid, IPSGuid templateid,
         Map<String, IPSGuid> optionalParams)
         throws CloneNotSupportedException, PSAssemblyException, PSCmsException
   {
      if (originalItem == null)
      {
         throw new IllegalArgumentException("workitem may not be null");
      }
      if (asm == null)
      {
         throw new IllegalArgumentException("asm may not be null");
      }
      if (itemid == null)
      {
         throw new IllegalArgumentException("itemid may not be null");
      }
      if (templatename == null && templateid == null)
      {
         throw new IllegalArgumentException(
               "templatename and templateid may not both be null");
      }
      if (optionalParams == null)
      {
         throw new IllegalArgumentException("optionalParams may not be null");
      }
      IPSAssemblyItem clone;
      clone = (IPSAssemblyItem) originalItem.clone();
      clone.setNode(null);
      clone.setPath(null);
      PSLegacyGuid relguid = (PSLegacyGuid) itemid;
      // Set the contentid and revision for the related item
      clone.setParameterValue(IPSHtmlParameters.SYS_CONTENTID, Integer
            .toString(relguid.getContentId()));
      clone.setParameterValue(IPSHtmlParameters.SYS_REVISION, Integer
            .toString(relguid.getRevision()));

      clone.setFolderId(0); // reset here, so it can be set "correctly" later

      // Add optional data from the slotitem
      IPSGuid site = optionalParams.get(IPSHtmlParameters.SYS_SITEID);
      if (site != null)
      {
         clone.setParameterValue(IPSHtmlParameters.SYS_SITEID, Long
               .toString(site.longValue()));
         clone.setSiteId(new PSGuid(PSTypeEnum.SITE, site.longValue()));
      }
      IPSGuid origsite = optionalParams.get(IPSHtmlParameters.SYS_ORIGINALSITEID);
      if (origsite != null)
      {
         clone.setParameterValue(IPSHtmlParameters.SYS_ORIGINALSITEID, Long
               .toString(origsite.longValue()));  
      }
      IPSGuid folder = optionalParams.get(IPSHtmlParameters.SYS_FOLDERID);
      if (folder != null)
      {
         clone.setParameterValue(IPSHtmlParameters.SYS_FOLDERID, Long
               .toString(folder.longValue()));
      }
      IPSGuid rid = optionalParams.get(IPSHtmlParameters.SYS_RELATIONSHIPID);
      if (rid != null)
      {
         clone.setParameterValue(IPSHtmlParameters.SYS_RELATIONSHIPID, Long
               .toString(rid.getUUID()));
      }

      // Remove any command, not appropriate for snippets

      // remove sys_command for legacy template only 
      if (clone.getTemplate().getAssembler().equals(
               IPSExtension.LEGACY_ASSEMBLER))
      {
         clone.removeParameterValue(IPSHtmlParameters.SYS_COMMAND);
      }

      clone.removeParameterValue(IPSHtmlParameters.SYS_PART);
      // Set the template
      if (StringUtils.isNotBlank(templatename))
      {
         String origTemplateName = clone.getTemplate().getName();
         if (!templatename.equals(origTemplateName))
         {
            clone.setTemplate(null);
            clone.removeParameterValue(IPSHtmlParameters.SYS_VARIANTID);
            clone.removeParameterValue(IPSHtmlParameters.SYS_TEMPLATE);
            if (ms_numeric.matcher(templatename).matches())
            {
               IPSGuid tguid = new PSGuid(PSTypeEnum.TEMPLATE, templatename);
               clone.setTemplate(asm.loadUnmodifiableTemplate(tguid));
            }
            else
            {
               clone.setParameterValue(IPSHtmlParameters.SYS_TEMPLATE,
                     templatename);
            }
         }
      }
      else
      {
         clone.setTemplate(asm.loadUnmodifiableTemplate(templateid));
      }
      clone.normalize();
      return clone;
   }

   /**
    * Logger
    */
   private static final Logger ms_log = LogManager.getLogger(PSContentFinderBase.class);
 
   /**
    * Numeric pattern using Java Regex
    */
   private static Pattern ms_numeric = Pattern.compile("\\d+");

   /**
    * The parameter that holds an order by clause (if any)
    */
   public static final String ORDER_BY = "order_by";
}
