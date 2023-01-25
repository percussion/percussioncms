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

package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

@XmlRootElement(name="SoProMetadata")
public class PSSoProMetadata extends PSAbstractDataObject
{
    @NotNull
    @NotEmpty
    private String itemId;
    private String metadata;
    
    public PSSoProMetadata() { }
    
    public PSSoProMetadata(String itemId, String metadata) {
        this.itemId = itemId;
        this.metadata = metadata;
    }
    
    /**
     * @return itemId - content id
     */
    public String getItemId()
    {
        return itemId;
    }
    
    /**
     * @param itemId - Set the content id
     */
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }
    
    public String getMetadata()
    {
        return metadata;
    }
    
    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return this.metadata;
    }
}
