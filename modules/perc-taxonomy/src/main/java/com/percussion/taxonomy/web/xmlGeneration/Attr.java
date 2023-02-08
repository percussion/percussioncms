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

package com.percussion.taxonomy.web.xmlGeneration;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

/**
 * @author Steffen Gates May 9, 2011
 */
public class Attr {
    
    @Attribute(required = true)
    public String name;
    
    @Attribute(required = true)
    public int langID;
    
    @ElementList(inline = true)
    public List<Value> values;
    
    public Attr() {}
    
    public Attr(String name, int langID){
        this.name = name;
        this.langID = langID;
    }
    
    public void addValue(Value val){
        if(values == null){
            values = new ArrayList<Value>();
        }
        values.add(val);
    }
}
