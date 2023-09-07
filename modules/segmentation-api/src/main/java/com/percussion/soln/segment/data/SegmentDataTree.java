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

package com.percussion.soln.segment.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.percussion.soln.segment.Segment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SegmentDataTree {
    
    private static final String ERROR_SEGMENT_CANNOT_BE_NULL = "Segment cannot be null";

    protected static final String PATH_START = "//";

    protected static final String PATH_SEPARATOR = "/";

    /**
     * Root Node.
     */
    private Segment rootNode;
    
    /**
     * Key = folderPath
     * Value = SegmentData.
     */
    private Map<String, Segment> paths;
    /**
     * Key = folderPath
     * Value = Childrens folderPath
     */
    private Map</* path */ String, /* path list */ Set<String>> segmentChildren;
    /**
     * Key = segment id.
     * Value = folderPath
     */
    private Map</* id */ String, /* path */ String> ids;
    /**
     * Key = folder id.
     * Value = folderPath.
     */
    private Map</* folder id */ Integer, /* path */ String> folderIds;
    
    private int lastPlaceHolderId;
    private String rootPath = PATH_START;
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(SegmentDataTree.class);
    
    
    public SegmentDataTree() {
        reset();
    }

    
    protected void reset() {
        paths = new HashMap<String, Segment>();
        ids = new HashMap<String,String>();
        folderIds = new HashMap<Integer,String>();
        segmentChildren = new HashMap<String, Set<String>>();
        lastPlaceHolderId = -1;
        setRootPath(PATH_START);
    }
    
    protected int nextPlaceHolderId() {
        return --lastPlaceHolderId;
    }
    
    private void validatePath(String path) throws IllegalArgumentException {
        if (path == null || path.trim().equals("") || ! path.startsWith(PATH_START)) {
            throw new IllegalArgumentException("path cannot be empty and must start with " + PATH_START 
            + " bad path: " + path);
        }
    }
    
    
    protected String getParentPath(String path) {
        validatePath(path);
        if (path.equals(PATH_START)) {
            return null;
        }
        
        int index = path.lastIndexOf(PATH_SEPARATOR);
        
        if (index < rootPath.length()) {
            return rootPath;
        }
        
        return path.subSequence(0, index).toString();
    }


    protected Segment retrieveOrCreateNode(String path) {
        if ( paths.containsKey(path) ) {
            return paths.get(path);
        }
        validatePath(path);
        if ( ! path.startsWith(rootPath) ) {
            log.warn("Path should start with rootPath: " + rootPath + " bad path: " + path);
            log.warn("This path " + path + " will not be visible as its ancestor(s) is not the root node.");
        }

        String parentPath = getParentPath(path);
        retrieveOrCreateNode(parentPath);
        Segment childNode = createPlaceHolderNode(path);
        registerNode(childNode);
        
        return childNode;
    }
    
    protected Segment createPlaceHolderNode(String path) {
        Segment newNode = new Segment();
        newNode.setFolderPath(path);
        newNode.setFolderId(nextPlaceHolderId());
        newNode.setId(Integer.toString(nextPlaceHolderId()));
        return newNode;
    }
    

    protected void addChildren(Segment parent, List<Segment> segments) {
        Set<String> children = getChildrenPaths(parent);
        for(Segment child: segments) {
            children.add(child.getFolderPath());
        }
    }
    

    
    public void update(Collection<Segment> data) {
        if (data == null)  throw new IllegalArgumentException("Segment data cannot be null");
        for (Segment d : data) {
            registerNode(d);
        }
    }

    protected Set<String> getChildrenPaths(Segment node) {
        return segmentChildren.get(node.getFolderPath());
        
    }
    
    protected void unregisterNode(final String path) {
        Segment data = getSegmentForPath(path);
        if (data != null) {
            folderIds.remove(data.getFolderId());
            ids.remove(data.getId());       
            paths.remove(path);
            segmentChildren.remove(path);
        }
    }
    
    protected void registerNode(Segment node) {
        
        //Check for an existing node and unregister it.
        Set<String> children = null;
        if (getSegmentForPath(node.getFolderPath()) != null) {
            children = getChildrenPaths(node);
            unregisterNode(node.getFolderPath());
        }
        
        validateSegmentData(node);
        ids.put(node.getId(), node.getFolderPath());
        paths.put(node.getFolderPath(), node);
        folderIds.put(node.getFolderId(), node.getFolderPath());
        if (children == null) {
            children = new HashSet<String>();
        }
        segmentChildren.put(node.getFolderPath(), children);
        String parentPath = getParentPath(node.getFolderPath());
        // If parent path is null its the root node.
        if (parentPath != null) {
            Segment parent = retrieveOrCreateNode(getParentPath(node.getFolderPath()));
            addChildren(parent, Arrays.asList(node));
        }
    }
    
    protected void validateSegmentData(Segment data) {
        if (data == null) throw new IllegalArgumentException(ERROR_SEGMENT_CANNOT_BE_NULL);
        if (data.getFolderPath() == null) throw new IllegalArgumentException("Segment data Folder path is null");
    }
    
    
    public Collection<Segment> getSegments() {
        List<Segment> segments = new ArrayList<Segment>();
        segments.addAll(paths.values());
        return segments;
    }
    
    public Collection<Segment> retrieveSegmentsWithNameOrAlias(String alias) {
        if (alias == null) throw new IllegalArgumentException("Alias cannot be null.");
        Collection<Segment> segments = getSegments();
        Iterator<Segment> it = segments.iterator();
        while(it.hasNext()) {
            Segment d = it.next();
            Set<String> aliases = d.getAliases();
            /*
             * First see if the name matches then check the aliases of the segment.
             */
            boolean noNameMatches = ! alias.equalsIgnoreCase(d.getName());
            boolean noAliasMatches = true;
            if ( noNameMatches && aliases != null ) {
                for (String a : aliases) {
                    if (alias.equalsIgnoreCase(a)) {
                        noAliasMatches = false;
                        break;
                    }
                }
            }
            if ( noNameMatches && noAliasMatches ) it.remove();
        }
        return segments;
    }
    
    public List<Segment> getChildren(Segment data) {
        if(data == null) throw new IllegalArgumentException(ERROR_SEGMENT_CANNOT_BE_NULL);
        List<Segment> children = new ArrayList<Segment>();
        Set<String> paths = getChildrenPaths(data);
        if (paths != null) {
            for (String path : paths) {
                Segment child = getSegmentForPath(path);
                children.add(child);
            }
        }
        return children;
    
    }
    
    protected List<String> getAncestorsPaths(final String path) {
        List<String> paths = new ArrayList<String>();
        String current = path;
        while (current != null) {
            paths.add(current);
            //Stop if we are at the root path.
            if (current.equals(getRootPath())) {
                current = null;
            }
            else {
                current = getParentPath(current);
            }
        }
        
        return paths;
    }
    
    /**
     * Returns the ancestors of a given segment including the given segment.
     * The order is upwards. ie the given segment is first.
     * 
     * @param data given segment.
     * @return ancestors.
     */
    public List<Segment> getAncestors(Segment data) {
        if(data == null) throw new IllegalArgumentException(ERROR_SEGMENT_CANNOT_BE_NULL);
        List<Segment> ancestors = new ArrayList<Segment>();
        List<String> paths = getAncestorsPaths(data.getFolderPath());
        for ( String path : paths ) {
            ancestors.add(getSegmentForPath(path));
        }
        
        return ancestors;
    }
    
    public Segment getSegmentForPath(String path) {
        return paths.get(path);
    }
    
    public Segment getSegmentForId(String id) {
    	 String path;
         if (isPath(id)) {
             path = id;
         }
         else if (isId(id)) {
             path = ids.get(id);
         }
         else {
             log.trace("#getSegmentForId given id is probably bad: " + id);
             path = id;
         }
         return getSegmentForPath(path);
    }
    
    private boolean isPath(String path) {
        return (path != null && path.startsWith(PATH_START)); 
    
    }
    private boolean isId(String id) {
        if (id == null) return false;
        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    public Segment getSegmentForFolderId(int id) {
        return getSegmentForPath(folderIds.get(id));
    }


    public Segment getRootSegment() {
        return getSegmentForPath(getRootPath());
    }
    
    public String getRootPath() {
        return rootPath;
    }


    public void setRootPath(String rootPath) {
        if (rootPath == null) throw new IllegalArgumentException("rootPath cannot be null");
        this.rootNode = createPlaceHolderNode(rootPath);
        registerNode(rootNode);
        this.rootPath = rootPath;
    }
}
