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
package com.percussion.assetmanagement.data;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.share.data.IPSContentItem;

@XmlRootElement(name = "Asset")
public class PSAsset extends PSAssetSummary implements IPSContentItem
{ 
    private Map<String, Object> fields = new HashMap<>();

    public Map<String, Object> getFields()
    {
        return fields;
    }

    public void setFields(Map<String, Object> fields)
    {
        this.fields = fields;
    }
    
    private static final long serialVersionUID = 8252999104256582955L;   
}
