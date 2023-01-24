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
package com.percussion.share.data;

import java.util.Map;
import java.util.Map.Entry;


/**
 * A Generic Low level representation of an item in the system backed
 * by a Rhythmyx content item.
 * <p>
 * 
 * @author adamgent
 *
 */
public interface IPSContentItem extends IPSItemSummary
{
    /**
     * A {@link Map} of all the {@link String} fields.
     * The {@link Entry#getKey()} of the {@link Map} is the name of the field.
     * The {@link Entry#getValue()} of the {@link Map} is value of the field.
     * @return never <code>null</code>.
     */
    public Map<String, Object> getFields();
    
    /**
     * 
     * @param fields never <code>null</code>.
     */
    public void setFields(Map<String, Object> fields);

}
