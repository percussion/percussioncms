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

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;


@XmlRootElement(name = "Segment")
public class Segment implements Serializable {
    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -5079993095755731413L;
    private int folderId;
    private String folderPath;
    private String name;
    private String id;
    private boolean selectable = false;
    private Set<String> aliases;
    
    private static final StandardToStringStyle toStringStyle = new StandardToStringStyle();
    static {
        toStringStyle.setUseClassName(false);
        toStringStyle.setUseIdentityHashCode(false);
    }
    
    //private Map<String,ValueData> propertiesData;
   
    
    public String toString() {
        return new ToStringBuilder(this, toStringStyle)
            .append("name", name)
            .append("folderId", folderId)
            .append("folderPath", folderPath).toString();
    }
    
    public void setFolderId(int id) {
        this.folderId = id;
        if (id < 0 ) selectable = false;
        else selectable = true;
    }
    
    public String getName() {
        if (this.name == null ) return this.getFolderName();
        return this.name;
    }

    public String getFolderName() {
        if (folderPath == "//" || folderPath == null) return null;
        int index = folderPath.lastIndexOf("/");
        if (index < 0 ) return folderPath;
        String[] names = folderPath.split("/");
        if (names.length == 0) return null;
        return names[names.length - 1];
    }

    /**
     * This setter is to placate serializers looking 
     * for a setter for the folderName property.
     * 
     * Use the folder path instead.
     * 
     * @param folderName
     * @see #setFolderPath(String)
     * @see #getFolderName()
     */
    public void setFolderName(String folderName) {
        //We do nothing here. 
        //This is to placate serializers looking for folderName setter.
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * @see com.percussion.soln.segment.ISegment#isSelectable()
     */
    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setName(String label) {
        this.name = label;
    }
    
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } 
        else if (obj instanceof Segment) {
            return equals((Segment)obj);
        }
        else {
            return false;
        }
    }
    
    public boolean equals(Segment data) {
        return new EqualsBuilder()
            .append(folderId, data.folderId)
            .append(folderPath, data.folderPath)
            .append(id, data.folderId)
            .append(selectable, data.selectable)
            .append(aliases, data.aliases).isEquals();
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }



//    public Map<String, ValueData> getPropertiesData() {
//        return propertiesData;
//    }
//
//    public void setPropertiesData(Map<String, ValueData> propertiesData) {
//        this.propertiesData = propertiesData;
//    }

}
