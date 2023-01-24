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
