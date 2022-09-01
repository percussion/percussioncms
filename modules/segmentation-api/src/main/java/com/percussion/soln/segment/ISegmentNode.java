package com.percussion.soln.segment;

import java.util.List;
import java.util.Set;

public interface ISegmentNode {
    
    public String getName();

    public String getFolderName();
    
    public String getFolderPath();
    
    public int getFolderId();
    
    //Is a poly property can be a path or an id
    public String getId();

    public boolean isSelectable();
    
    public Set<String> getAliases();

    //TODO: Find out if segments really need custom properties.
    //public Map<String, Value> getProperties();

    public List<? extends ISegmentNode> getChildren();

}
