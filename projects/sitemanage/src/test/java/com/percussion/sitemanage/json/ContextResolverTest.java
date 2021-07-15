/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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

package com.percussion.sitemanage.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.percussion.share.data.PSPagedItemList;

import java.io.StringWriter;
import java.util.HashMap;

import org.junit.Test;

public class ContextResolverTest
{
    private String exampleJSON = "{\r\n" + 
            "\r\n" + 
            "\"PagedItemList\": {\r\n" + 
            "\"childrenCount\": 4,\r\n" + 
            "\"startIndex\": 1,\r\n" + 
            "\r\n" + 
            "\"childrenInPage\": [\r\n" + 
            "\r\n" + 
            "  {\r\n" + 
            "\"name\": \"Sites\",\r\n" + 
            "\"revisionable\": false,\r\n" + 
            "\"leaf\": false,\r\n" + 
            "\"hasItemChildren\": true,\r\n" + 
            "\"hasFolderChildren\": true,\r\n" + 
            "\"hasSectionChildren\": true,\r\n" + 
            "\"path\": \"/Sites/\",\r\n" + 
            "\"columnData\": \"\",\r\n" + 
            "\r\n" + 
            "\"typeProperties\": {\r\n" + 
            "\"entries\": \"\"\r\n" + 
            "},\r\n" + 
            "\"folderPath\": \"//Sites\"\r\n" + 
            "}\r\n" + 
            "]\r\n" + 
            "}\r\n" + 
            "}";
    
    private String test2JSON = "{\r\n" + 
            "  \"ArrayList\" : [ {\r\n" + 
            "    \"name\" : \"Publish\",\r\n" + 
            "    \"enabled\" : true\r\n" + 
            "  }, {\r\n" + 
            "    \"name\" : \"Schedule...\",\r\n" + 
            "    \"enabled\" : true\r\n" + 
            "  }, {\r\n" + 
            "    \"name\" : \"Remove from Site\",\r\n" + 
            "    \"enabled\" : true\r\n" + 
            "  } ]\r\n" + 
            "}";
    @Test
    public void testSerializeJSON() throws Exception {
        JacksonContextResolver resolver = new JacksonContextResolver();
        ObjectMapper mapper = resolver.getContext(PSPagedItemList.class);
        System.out.println(exampleJSON);
        PSPagedItemList inputItem = mapper.readValue(exampleJSON, PSPagedItemList.class);
        HashMap<String, String> properties = new HashMap<String,String>();
        properties.put("test1", "test1Val");
        inputItem.getChildrenInPage().get(0).getTypeProperties().setEntries(null);
        System.out.println(inputItem);
        StringWriter out = new StringWriter();
        mapper.writeValue(out, inputItem);
        System.err.println(out.toString());
        
    }
}
