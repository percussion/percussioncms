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
package com.percussion.pagemanagement.data;

import static com.percussion.pagemanagement.data.PSRegionTreeUtils.getWidgetRegions;
import static com.percussion.pagemanagement.data.PSRegionTreeUtils.visitNodes;
import static org.apache.commons.lang.Validate.*;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.percussion.pagemanagement.data.PSRegionTreeUtils.PSRegionNodeWrapper.Type;



/**
 * Utilities for {@link PSRegionNode} trees.
 * 
 * @author adamgent
 *
 */
public class PSRegionTreeUtils
{

    /**
     * Will visit the nodes defined by the order of the iterator.
     * @param it order of the nodes never <code>null</code>.
     * @param visitor never <code>null</code>.
     */
    public static void visitNodes(Iterator<PSRegionNode> it, IPSRegionNodeVisitor visitor) {
        notNull(it);
        notNull(visitor);
        while(it.hasNext()) {
            PSRegionNode node = it.next();
            node.accept(visitor);
        }
    }
    
    /**
     * Will visit the region nodes in natural order of the {@link PSRegionNode} tree. 
     * The order is preorder, depth first search traversal.
     * See: <a href="http://en.wikipedia.org/wiki/Tree_traversal">http://en.wikipedia.org/wiki/Tree_traversal</a>
     * 
     * @param rootNode
     * @param visitor
     */
    public static void visitNodes(PSRegionNode rootNode, IPSRegionNodeTreeVisitor visitor) {
        notNull(rootNode);
        notNull(visitor);
        Iterator<PSRegionNodeWrapper> it = new PSRegionNodeWrapperIterator(rootNode);
        while(it.hasNext()) {
            PSRegionNodeWrapper nw = it.next();
            if(nw.type == Type.START) {
                nw.node.accept(visitor.getStartRegionNodeVisitor());
            }
            else if (nw.type == Type.END) {
                nw.node.accept(visitor.getEndRegionNodeVisitor());
            }
            else {
                isTrue(false);
            }
        }
    }
    
    
    public static Map<String, PSRegion> regionMap(PSRegionNode rootNode) {
        Iterator<PSRegion> regions = iterateRegions(rootNode);
        Map<String, PSRegion> map = new HashMap<>();
        while(regions.hasNext()) {
            PSRegion r = regions.next();
            map.put(r.getRegionId(), r);
        }
        return map;
    }
    
    public static Iterator<PSRegion> iterateRegions(PSRegionNode rootNode) {
        PSRegionNodeWrapperIterator it = new PSRegionNodeWrapperIterator(rootNode);
        List<PSRegion> regions = new ArrayList<>();
        while(it.hasNext())
        {
            PSRegionNodeWrapper nw = it.next();
            
            if (nw.node instanceof PSRegion && nw.type == Type.START) {
                regions.add((PSRegion) nw.node);
            }
        }
        return regions.iterator();
    }
    
    
    public static List<? extends PSRegionNode> getChildren(PSRegionNode node) {
        if (node instanceof PSAbstractRegion) {
            PSAbstractRegion r = (PSAbstractRegion) node;
            if (r.getChildren() != null && ! r.getChildren().isEmpty()) {
                return r.getChildren();
            }
        }
        return new ArrayList<>();
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends PSAbstractRegion> List<T> getChildRegions(PSRegionNode node) {
        List<T> regions = new ArrayList<>();
        List<? extends PSRegionNode> children = getChildren(node);
        for(PSRegionNode child : children) {
            if(child instanceof PSAbstractRegion) {
                regions.add((T) child);
            }
        }
        return regions;
    }
    
    public static boolean isLeaf(PSRegionNode node) {
        if (node instanceof PSAbstractRegion) {
            List<? extends PSRegionNode>  nodes = ((PSAbstractRegion) node).getChildren();
            if (nodes != null &&  ! nodes.isEmpty() )
                return false;
        }
        return true;
    }
    
    
    protected static class PSRegionNodeWrapper
    {
        protected enum Type {
            START, END
        }

        protected PSRegionNode node;

        protected Type type;

        public PSRegionNodeWrapper(PSRegionNode node, Type type)
        {
            super();
            this.node = node;
            this.type = type;
        }

    }
   
    /**
     * Converts a region Abstract Syntax Tree back into 
     * template code.
     * @param rootNode never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static String treeToString(PSAbstractRegion rootNode) {
        StringWriter sw = new StringWriter();
        PSRegionTreeWriter tw = new PSRegionTreeWriter(sw);
        tw.write(rootNode);
        return sw.toString();
    }
    
    /**
     * Retrieves a {@link Set} containing the leaf regions of the template. A
     * leaf region is a region that does not contain another regions.
     * 
     * @param template {@link PSTemplate} object, cannot be <code>null</code>.
     * @return {@link Set}<{@link PSRegion}> never <code>null</code>.
     */
    public static Set<PSRegion> getWidgetRegions(PSRegionTree regionTree)
    {
        notNull(regionTree);

        if (regionTree.getRootRegion() == null)
        {
            return new HashSet<>();
        }

        Set<PSRegion> leafRegions = new HashSet<>();
        List<PSRegion> nodes = getChildRegions(regionTree.getRootRegion());
        for (PSRegion region : nodes)
        {
            getWidgetRegionsFromChilds(leafRegions, region);
        }
        return leafRegions;
    }

    /**
     * Recursively iterates over the nodes and get the leaf regions.
     * 
     * @param leafRegions {@link Set}<{@link PSRegion}> to save the leaf nodes.
     *            Must not be <code>null</code>.
     * @param node {@link PSRegion} representing the current node.
     */
    private static void getWidgetRegionsFromChilds(Set<PSRegion> leafRegions, PSRegion node)
    {
        if (isWidgetRegion(node))
        {
            leafRegions.add(node);
            return;
        }
        
        List<PSRegion> nodes = getChildRegions(node);
        for (PSRegion region : nodes)
        {
            getWidgetRegionsFromChilds(leafRegions, region);
        }
    }

    /**
     * Gets the leaf regions of the template and returns those that don't have
     * widgets in it.
     * 
     * @param regionTree
     * @return {@link Set}<{@link PSRegion}> never <code>null</code>.
     */
    public static Set<PSRegion> getEmptyWidgetRegions(PSRegionTree regionTree)
    {
        notNull(regionTree);
        
        Set<PSRegion> emptyLeafs = new HashSet<>();
        Set<PSRegion> leafs = getWidgetRegions(regionTree);
        Set<String> notEmptyRegions = regionTree.getRegionWidgetsMap().keySet();
        
        for(PSRegion region : leafs)
        {
            if(!notEmptyRegions.contains(region.getRegionId()))
            {
                emptyLeafs.add(region); 
            }
        }
        return emptyLeafs;
    }

    /**
     * A {@link PSRegion} is leaf if:
     * <p>
     * <li>its children collection is empty
     * <li>its children collection is not empty, but the children are instances
     * of {@link PSRegionCode}
     * 
     * @param region {@link PSRegion} object, must not be <code>null</code>
     * @return <code>true</code> if the region is leaf, <code>false</code>
     *         otherwise.
     */
    private static boolean isWidgetRegion(PSRegion region)
    {
        if (isEmpty(region.getChildren()))
        {
            return true;
        }

        if (region.getChildren().size() == 1 && region.getChildren().get(0) instanceof PSRegionCode)
        {
            return true;
        }

        return false;
    }
    
    protected static class PSRegionNodeWrapperIterator implements Iterator<PSRegionNodeWrapper> {        
        private Stack<PSRegionNodeWrapper> nodeStack = new Stack<>();
        
        
        public PSRegionNodeWrapperIterator(PSRegionNode rootNode)
        {
            super();
            nodeStack.push(new PSRegionNodeWrapper(rootNode, Type.START));
        }


        public boolean hasNext()
        {
            return ! nodeStack.isEmpty();
        }

        public PSRegionNodeWrapper next()
        {
            
            PSRegionNodeWrapper nodeWrapper = nodeStack.pop();
            PSRegionNode node = nodeWrapper.node;
            if (nodeWrapper.type == Type.START) {
                nodeStack.push(new PSRegionNodeWrapper(nodeWrapper.node, Type.END));
                if (node instanceof PSAbstractRegion) {
                    PSAbstractRegion r = (PSAbstractRegion) node;
                    if ( r.getChildren() != null && ! r.getChildren().isEmpty()) {
                        List<? extends PSRegionNode> children = r.getChildren();
                        children = new ArrayList<>(children);
                        Collections.reverse(children);
                        for( PSRegionNode child : children) {
                            nodeStack.push(new PSRegionNodeWrapper(child, Type.START));
                        }
                        
                    }
                }
            }
            return nodeWrapper;
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove is not yet supported");
        }
    
    }
    
}
