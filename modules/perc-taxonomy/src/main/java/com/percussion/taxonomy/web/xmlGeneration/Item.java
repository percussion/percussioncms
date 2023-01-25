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
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

public class Item {

    @Attribute
    public String id;
    @Attribute(required = false)
    public String parent_id;
    @Attribute(required = false)
    public String state;
    @Attribute
    public String title;
    
    @Element
    public Content content;
    
    @ElementList(required = false, inline = true, entry="attribute")
    public List<Attr> attributes;

    public Item() {}

    public Item(String id, String parent_id, String title, String content, String link, String onclick) {
        this.id = id;
        this.parent_id = parent_id;
        if (link !=null){
        	if (onclick!=null){
        		this.content = new Content(content, link, onclick);
        	}else{
        		this.content = new Content(content, link);
        	}
        }else{
        	this.content = new Content(content);
        }
        this.title = title;
    }
    
    public void addAttribute(Attr attr){
        if(attributes == null){
            attributes = new ArrayList<Attr>();
        }
        attributes.add(attr);
    }    
}
