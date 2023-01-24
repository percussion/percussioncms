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
package com.percussion.pathmanagement.service.impl;

import com.percussion.pathmanagement.data.PSPathItem;

import java.util.Comparator;

/**
 * PathItem sorter to case insensitive sort alpha ascending.
 */
public class PSPathItemComparator implements Comparator<PSPathItem>
{
    private static PSPathItemComparator instance;
    
    /**
     */
    private PSPathItemComparator()
    {
        
    }
    
    public static Comparator<PSPathItem> getInstance()
    {
        if (instance == null)
            instance = new PSPathItemComparator();
        
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(PSPathItem a, PSPathItem b)
    {
        return a.getName().compareToIgnoreCase(b.getName());
    }
}
