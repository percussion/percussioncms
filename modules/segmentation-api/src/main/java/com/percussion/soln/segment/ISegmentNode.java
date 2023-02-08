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
