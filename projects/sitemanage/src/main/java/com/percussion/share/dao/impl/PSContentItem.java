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
package com.percussion.share.dao.impl;

import static org.apache.commons.lang.Validate.*;

import java.util.HashMap;
import java.util.Map;

import com.percussion.share.data.IPSContentItem;
import com.percussion.share.data.PSDataItemSummary;

/**
 * A Generic Low level representation of an item in the system backed
 * by a Rhythmyx content item.
 * @author adamgent
 *
 */
public class PSContentItem extends PSDataItemSummary implements IPSContentItem
{

    /**
     * never <code>null</code>.
     */
    private Map<String, Object> fields = new HashMap<>();
    

    /**
     * @{inheritDoc}
     */
    public Map<String, Object> getFields()
    {
        return fields;
    }

    /**
     * @{inheritDoc}
     */
    public void setFields(Map<String, Object> fields)
    {
        notNull(fields, "fields");
        this.fields = fields;
    }
    
    

    /**
     * Well not really safe to serialize
     */
    private static final long serialVersionUID = -3451673795623212592L;

}
