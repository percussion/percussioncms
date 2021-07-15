/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
