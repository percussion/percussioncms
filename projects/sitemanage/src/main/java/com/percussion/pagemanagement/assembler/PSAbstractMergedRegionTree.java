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
/**
 * 
 */
package com.percussion.pagemanagement.assembler;

import com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner;
import com.percussion.pagemanagement.data.PSAbstractRegion;
import com.percussion.pagemanagement.data.PSRegionBranches;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.pagemanagement.data.PSRegionTreeUtils;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.share.service.exception.PSDataServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner.PAGE;
import static com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner.TEMPLATE;
import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getChildRegions;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Represents the merging of {@link PSRegionTree}
 * with {@link PSRegionBranches} as one unified tree.
 * <p>
 * The root nodes of the page region branches will replace the 
 * matching nodes of the tree branches if 
 * {@link #chooseTemplateOrPageRegion(PSAbstractRegion, PSAbstractRegion, PSMergedRegion)} ==
 * {@link PSMergedRegionOwner#PAGE}.
 * <p>
 * Implementations may also choose whether or not to expand sub regions through
 * {@link #isLeaf(PSMergedRegion)}.
 * 
 * @author adamgent
 *
 */
public abstract class PSAbstractMergedRegionTree {
    
    private Map<String, PSAbstractRegion> pageRegionMap;
    /**
     * Widget items overlay: template widgets with the page widgets overlayed.
     */
    private Map<String, List<PSWidgetItem>> mergedWidgetItems = new HashMap<>();
    
    /**
     * The widgets associated to a page.
     * <p>
     * Will never be <code>null</code> after {@link #merge(PSRegionTree, PSRegionBranches)}
     * is called. Maybe empty.
     */
    protected Map<String, List<PSWidgetItem>> pageWidgetItems;
    
    /**
     * The widgets associated to a template.
     * <p>
     * Will never be <code>null</code> after {@link #merge(PSRegionTree, PSRegionBranches)}
     * is called. Maybe empty.
     */
    protected Map<String, List<PSWidgetItem>> templateWidgetItems;
    
    /**
     * never <code>null</code> after merge.
     */
    private PSMergedRegion rootNode;
    /**
     * never <code>null</code> after merge.
     */
    private PSRegionTree templateRegionTree;
    
    
    private Map<String, PSMergedRegion> mergedRegionMap = new HashMap<>();
    /**
     * Regions that can be override by the page.
     */
    private List<PSMergedRegion> overriddenRegions = new ArrayList<>();


    /**
     * Creates the merged region tree for the given tree and branches.
     * <em>This should only be run once as reuse of the object through this method is undefined. 
     * Its recommended a new object should be be used for different inputs.</em>
     * @param templateRegionTree never <code>null</code>.
     * @param pageRegionBranches never <code>null</code>.
     */
    public void merge(PSRegionTree templateRegionTree, PSRegionBranches pageRegionBranches) throws PSDataServiceException {
        notNull(templateRegionTree);
        notNull(pageRegionBranches);
        this.templateRegionTree = templateRegionTree;
        this.templateWidgetItems = this.templateRegionTree.getRegionWidgetsMap();
        this.pageWidgetItems = pageRegionBranches.getRegionWidgetsMap();
        
        createPageRegionMap(pageRegionBranches.getRegions());
        rootNode = mergeTree(this.templateRegionTree.getRootRegion(), null);
    }
    
    private void createPageRegionMap(Collection<? extends PSAbstractRegion> pageRegionBranches) {
        pageRegionMap = new HashMap<>();
        
        if (pageRegionBranches == null)
           return;
        
        for (PSAbstractRegion pr: pageRegionBranches) {
            pageRegionMap.put(pr.getRegionId(), pr);
        }
    }
    /**
     * Recursive method.
     * @param regionNode not <code>null</code>
     * @param parent maybe <code>null</code>
     * @return not <code>null</code>
     */
    private PSMergedRegion mergeTree(PSAbstractRegion regionNode, PSMergedRegion parent) throws PSDataServiceException {
        notNull(regionNode);
        notNull(pageRegionMap);
        if (parent != null)
            notNull(parent.getOwner());
        notEmpty(regionNode.getRegionId());
        /*
         * TODO validate region hasn't been merged yet.
         */
        PSMergedRegion rvalue = null;
        PSMergedRegionOwner owner = parent == null ? TEMPLATE : parent.getOwner();
        PSAbstractRegion pageRegionOverride = pageRegionMap.get(regionNode.getRegionId());
        PSAbstractRegion overriddenRegion = null;
        boolean pageIsTryingToOverride = pageRegionOverride != null && owner == TEMPLATE;
        
        if (pageIsTryingToOverride) {
            /*
             * Determine whether to use the page or template 
             */
            owner = chooseTemplateOrPageRegion(regionNode, pageRegionOverride, parent);
            if (owner == PAGE) {
                overriddenRegion = regionNode;
                regionNode = pageRegionOverride;
            }
            else if( owner == TEMPLATE) {
                overriddenRegion = pageRegionOverride;
            }
            else {
                throw new UnsupportedOperationException("Do not support: " + owner);
            }
        }
        
        rvalue = createNode(regionNode, owner, overriddenRegion, parent);
        getMergedRegionMap().put(rvalue.getRegionId(), rvalue);
        
        if ( owner == PAGE && rvalue.getOverriddenRegion() != null) {
            overriddenRegions.add(rvalue);
        }
        
        if (isLeaf(rvalue)) {
            if (log.isDebugEnabled())
                log.debug("Region is leaf: " + rvalue );
        }
        else {
            //We recurse on the sub regions.
            List<PSMergedRegion> mergedSubRegions = new ArrayList<>();
            for ( PSAbstractRegion subRegion : getChildRegions(rvalue.getOriginalRegion())) {
                PSMergedRegion child = mergeTree(subRegion, rvalue);
                notNull(child);
                mergedSubRegions.add(child);
            }
            rvalue.setSubRegions(mergedSubRegions);
        }
        
        
        notNull(rvalue);
        return rvalue;
        
    }
    
    
    /**
     * Chooses between the template or page region for merging.
     * @param template never <code>null</code>.
     * @param page never <code>null</code>.
     * @param parent maybe <code>null</code>.
     * @return never <code>null</code>.
     */
    protected abstract PSMergedRegionOwner chooseTemplateOrPageRegion(PSAbstractRegion template, PSAbstractRegion page, PSMergedRegion parent);
    
    protected boolean hasRegionChildren(PSAbstractRegion template) {
        return ! PSRegionTreeUtils.getChildRegions(template).isEmpty();
    }
    protected boolean hasTemplateWidgets(PSAbstractRegion template) {
        return this.templateWidgetItems.get(template.getRegionId()) != null;
    }
    
    /**
     * <strong>Indicates</strong> whether or not merged region should have children.
     * <em>The  {@link PSMergedRegion#getOriginalRegion() original region} 
     * may actually have sub regions but implementations may choose to indicate not
     * to expand this region</em>
     * @param mr never <code>null</code>.
     * @return never <code>null</code>. If <code>true</code> will stop recursively merging sub-regions.
     */
    protected abstract boolean isLeaf(PSMergedRegion mr);

    /**
     * Gets the widget items for the merged region.
     * 
     * @param mr never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    protected abstract List<PSWidgetItem> getMergedWidgetItemsForRegion(PSMergedRegion mr);
    
    /**
     * Creates the node by loading the widget instances if needed.
     * @param region never <code>null</code>.
     * @param owner never <code>null</code>.
     * @param overriddenRegion maybe <code>null</code>.
     * @param parent maybe <code>null</code>.
     * @return never <code>null</code>.
     */
    protected PSMergedRegion createNode(PSAbstractRegion region, 
            PSMergedRegionOwner owner, 
            PSAbstractRegion overriddenRegion, 
            PSMergedRegion parent) throws PSDataServiceException {
        String id = region.getRegionId();
        notNull(id);
        PSMergedRegion mr = new PSMergedRegion(region);
        mr.setOwner(owner);
        mr.setOverriddenRegion(overriddenRegion);
        List<PSWidgetItem> widgetItems = getMergedWidgetItemsForRegion(mr);
        if (widgetItems != null) {
            mr.setWidgetInstances(loadWidgets(widgetItems));
            this.mergedWidgetItems.put(mr.getRegionId(), widgetItems);
        }
        return mr;
    }
    
    /**
     * Load the widget instances.
     * @param widgetItems never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected abstract List<PSWidgetInstance> loadWidgets(List<PSWidgetItem> widgetItems) throws PSDataServiceException;
    
    /**
     * Retrieves the root merged node.
     * @return maybe <code>null</code> if {@link #merge(PSRegionTree, PSRegionBranches)} has not been called.
     */
    public PSMergedRegion getRootNode()
    {
        return rootNode;
    }


    public Map<String, PSMergedRegion> getMergedRegionMap()
    {
        return mergedRegionMap;
    }

    public List<PSMergedRegion> getOverriddenRegions()
    {
        return overriddenRegions;
    }
    
    
    /**
     * Retrieves all merged regions that have widgets in them.
     * @return never <code>null</code>.
     */
    public Collection<PSMergedRegion> getWidgetRegions() {
        List<PSMergedRegion> regions = new ArrayList<>();
        for(String id : mergedWidgetItems.keySet()) {
            PSMergedRegion r = mergedRegionMap.get(id);
            if (r != null)
                regions.add(mergedRegionMap.get(id));
            else
                log.error(format("Widgets associated to non existant region. " +
                        "Widgets: {0}, RegionId: {1}", mergedWidgetItems.get(id), id));
        }
        return regions;
    }

    

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    protected final Logger log = LogManager.getLogger(getClass());

    
}
