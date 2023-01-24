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

import static org.apache.commons.lang.StringUtils.isBlank;


import java.text.Collator;

/**
 * The base class for all named data objects.  All named data objects should extend this class or some derivative.
 */
public abstract class PSAbstractNamedObject extends PSAbstractDataObject implements Comparable<PSAbstractNamedObject>
{
    private static final long serialVersionUID = 1L;

    private String name;

    /**
     * The name that uniquely identifies the object.
     * 
     * @return should not be <code>null</code> or empty 
     *    unless the object is not finished being processed.
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Determines if the specified name is valid for this object.  By default, a valid name is not blank.
     * 
     * @param name
     * @return <code>true</code> if the name is valid, <code>false</code> otherwise.
     */
    protected boolean isValidName(String name)
    {
        return !isBlank(name);
    }
    
    public int compareTo(PSAbstractNamedObject o)
    {
        return Collator.getInstance().compare(this.getName(), o.getName());
    }
}
