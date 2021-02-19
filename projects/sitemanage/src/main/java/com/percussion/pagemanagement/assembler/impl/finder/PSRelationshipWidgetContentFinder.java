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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.pagemanagement.assembler.impl.finder;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.impl.finder.PSContentFinderBase;
import com.percussion.services.assembly.impl.finder.PSRelationshipFinderUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.injectDependencies;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Find contents related to a page and/or template by Active Assembly
 * relationships where the "sys_slotid" property equals to the specified widget 
 * instance.
 * 
 * The parameters of the finder are:
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>max_results</td>
 * <td>Optional parameter. It is the maximum number of the returned result
 * from the find method if specified, zero or negative indicates no limit. 
 * It defaults to zero if not specified.</td>
 * </tr>
 * <tr>
 * <tr>
 * <td>order_by</td>
 * <td>Optional parameter. If it is specified, then the returned items will 
 * be re-ordered according to the specified value; otherwise the returned 
 * items are ordered by {@link PSContentFinderBase.ContentItem}.</td>
 * </tr>
 * </table>
 * 
 * @author YuBingChen
 */
public class PSRelationshipWidgetContentFinder extends PSWidgetContentFinder
{
    public static final String IS_MATCH_BY_NAME = "IS_MATCH_BY_NAME";
    
    /**
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
     *      java.io.File)
     */
    @Override
       
    public void init(IPSExtensionDef def, File codeRoot)
          throws PSExtensionException
    {
       m_finderUtils.init();
       injectDependencies(this);
    }

    /**
     * Finds the relationships where the owner is the given ID and the category of the relationships is active assembly.
     * @param owner the ID of the relationship owner, not <code>null</code>.
     * @return the list of relationships, never <code>null</code>, may be empty.
     */
    public List<PSRelationship> findRelationshipByOwner(IPSGuid owner)
    {
        return m_finderUtils.findRelationshipByOwner(owner);
    }
    
    /**
     * Determines if the given relationship matches the supplied widget criteria.
     * @param rel the relationship, not <code>null</code>.
     * @param criteria the widget criteria, not <code>null</code>.
     * @return <code>true</code> if matches; otherwise return <code>false</code>.
     */
    public boolean isMatchRelationship(PSRelationship rel, WidgetCriteria criteria, Map<String, Object> params)
    {
        return m_finderUtils.isTargetRelationship(rel, criteria, params);
    }
    
    /**
     */   
    @Override
    protected Set<ContentItem> getContentItems(IPSAssemblyItem sourceItem,
            PSWidgetInstance widget, Map<String, Object> params) throws PSNotFoundException {
        if (!(widget instanceof PSWidgetInstance))
            throw new IllegalArgumentException("Cannot create widget criteria from object: " + widget);

        WidgetCriteria criteria = new WidgetCriteria((PSWidgetInstance)widget);
        return m_finderUtils.getContentItems(sourceItem, criteria, params);
    }

    /**
     * The comparator used to order the returned list from
     * @return the comparator, never <code>null</code>.
     */
    protected Comparator<ContentItem> getComparator(PSWidgetInstance widget) throws PSNotFoundException {
       return new ContentItemOrder(widget);
    }
    
    /**
     * Comparator to order widget/slot relationships
     */
    private class ContentItemOrder implements Comparator<ContentItem>
    {
       private WidgetCriteria m_criteria;
       
       public ContentItemOrder(PSWidgetInstance widget) throws PSNotFoundException {
           m_criteria = new WidgetCriteria(widget);    
       }
       
       /**
        * Compare widget/slot items for ordering
        * 
        * @param s1 widget/slot item one, never <code>null</code>
        * @param s2 widget/slot item two, never <code>null</code>
        * @return positive number for increasing order, negative for decreasing
        *         order, zero for no change
        */
       public int compare(ContentItem s1, ContentItem s2)
       {
          notNull(s1);
          notNull(s2);

          if (isBlank(m_criteria.widgetName))
              return compareUnnamed(s1, s2);

          if (isBlank(s1.getWidgetName()) && isNotBlank(s2.getWidgetName()))
              return 1;

          if (isNotBlank(s1.getWidgetName()) && isBlank(s2.getWidgetName()))
              return -1;
          
          return compareUnnamed(s1, s2);
       }
       
       /**
        * Compare items as unnamed items.
        * @param s1 widget/slot item one, never <code>null</code>
        * @param s2 widget/slot item two, never <code>null</code>
        * @return positive number for increasing order, negative for decreasing
        *         order, zero for no change
        */
       private int compareUnnamed(ContentItem s1, ContentItem s2)
       {
           if (s1.getSortrank() != s2.getSortrank())
               return s1.getSortrank() - s2.getSortrank();
               
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
    }
    
    /**
     * Widget criteria is typically used by the widget content finder,
     * where we need to filter the relationships by the specified criteria.
     */    
    public static class WidgetCriteria
    {
        private long widgetId;
        private String widgetName;
        private long contentTypeId;
        
        /**
         * Create the widget criteria from widget instance.
         * 
         * @param widget the widget instance, never <code>null</code>.
         */
        public WidgetCriteria(PSWidgetInstance widget) throws PSNotFoundException {
            notNull(widget, "widget");
            
            PSWidgetInstance w = (PSWidgetInstance) widget;
            
            String ctType = w.getDefinition().getWidgetPrefs().getContenttypeName();
            try
            {
                if (StringUtils.isEmpty(ctType))
                    contentTypeId = -1L;
                else
                    contentTypeId = PSItemDefManager.getInstance().contentTypeNameToId(ctType);
            }
            catch (PSInvalidContentTypeException e)
            {
                String errMsg = "Cannot find content type name = " + ctType;
                m_log.error(errMsg, e);
                throw new PSNotFoundException(errMsg);
            }
            widgetId = Long.parseLong(w.getItem().getId());
            widgetName = w.getItem().getName();
        }
        
        public String getWidgetName()
        {
            return widgetName;
        }
    }
    
    
    /**
     * Inherit the shared relationship finder utility class, but override the
     * {@link #queryRelationships(IPSAssemblyItem, IPSGuid)}.
     *
     * @author YuBingChen
     */
    private class ContentFinderUtils extends PSRelationshipFinderUtils<WidgetCriteria>
    {
        /*
         * (non-Javadoc)
         * @see com.percussion.services.assembly.impl.finder.PSRelationshipFinderUtils#isTargetRelationship(com.percussion.design.objectstore.PSRelationship, java.lang.Object)
         */
        protected boolean isTargetRelationship(PSRelationship rel, WidgetCriteria criteria, Map<String, Object> params)
        {
            if (!isMatchByName(params))
            {
                return super.matchesSlotId(rel, criteria.widgetId);
            }
            
            // match slot_id only for unnamed relationship and widget
            String relWidgetName = rel.getProperty(PSRelationshipConfig.PDU_WIDGET_NAME);
            if (isBlank(relWidgetName) && isBlank(criteria.widgetName))
                return super.matchesSlotId(rel, criteria.widgetId);
            
            // this can happen when rename an widget in "Layout tab", but user has not "Save" yet
            // so the original unnamed asset still remain to be unnamed.
            if (isBlank(relWidgetName) && isNotBlank(criteria.widgetName))
            {
                return super.matchesSlotId(rel, criteria.widgetId);                
            }
            
            if (!equalsIgnoreCase(criteria.widgetName, relWidgetName))
                return false;
            
            return matchContentType(rel, criteria);
        }

        /**
         * Determines if the finding method is matching by widget / asset name or not.
         * When rendering template assets, it always matches the widget ID to the slot id of the relationship.
         * However, when rendering a page asset, it will try to match the named widget to named page asset(s).
         * 
         * @param params the parameters passed to the finder, it may be <code>null</code> or empty.
         * 
         * @return <code>true</code> if matching by name (while rendering with page assets); otherwise matching by
         * widget ID (while rendering with template assets).
         */
        private boolean isMatchByName(Map<String, Object> params)
        {
            if (params == null)
                return true;
            
            Boolean bMatchByName = (Boolean) params.get(IS_MATCH_BY_NAME);
            return (bMatchByName != null) ? bMatchByName.booleanValue() : true;
        }
        
        private boolean matchContentType(PSRelationship r, WidgetCriteria criteria)
        {
            int depId = r.getDependent().getId();
            IPSItemEntry item = getCmsMgr().findItemEntry(depId);
            
            return item == null ? false : item.getContentTypeId() == criteria.contentTypeId;            
        }
        
        public List<PSRelationship> findRelationshipByOwner(IPSGuid id)
        {
            return super.getRelationships(id);
        }
    }
    
    private IPSCmsObjectMgr getCmsMgr()
    {
        if (m_cmsMgr != null)
            return m_cmsMgr;
        
        m_cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
        return m_cmsMgr;
    }
    
    private IPSCmsObjectMgr m_cmsMgr;
    
    /**
     * The finder utility object, never <code>null</code>. It is initialized
     * by {@link #init(IPSExtensionDef, File)}, and never modified after that.
     */
    private ContentFinderUtils m_finderUtils = new ContentFinderUtils();
    
    /**
     * Logger for this class
     */
    private static Log m_log = LogFactory.getLog(PSRelationshipWidgetContentFinder.class);
    
}