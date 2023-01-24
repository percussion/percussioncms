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

package com.percussion.category.web.service;

import com.percussion.category.data.PSCategory;
import com.percussion.category.data.PSCategoryNode;
import com.percussion.category.marshaller.PSCategoryMarshaller;
import com.percussion.category.marshaller.PSCategoryUnMarshaller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSJerseyRestClientTest extends TestCase {
	
	//FB: ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD NC 1-16-16
	private  PSCategoryServiceRestClient client;
	
	@BeforeClass
    public void setUp() throws Exception {
         
        client = new PSCategoryServiceRestClient();
    }
	
	@Test
	public void testGetCategries() throws PSDataServiceException {
		PSCategory result = client.getCategoryList("xyz");
		
		assertNotNull(result);
		validateData(PSCategoryMarshaller.marshalToJson(result));
		
	}
	
	@Test
	public void testUpdateCategries() throws PSDataServiceException {
		PSCategory resultcat = client.getCategoryList("xyz");
		String result = PSCategoryMarshaller.marshalToJson(resultcat);
		assertNotNull(result);
		result = result.replace("Children", "topLevelNodes");
		result = result.replace("Child", "childNodes");
		
		if(result.contains("\"topLevelNodes\":{")) {
			result = result.replace("\"topLevelNodes\":{", "\"topLevelNodes\":[{");
		}
		
		result = result.replace("\"childNodes\":{", "\"childNodes\":[{");
		Pattern pattern = Pattern.compile("\\}\\}");
		Matcher m = pattern.matcher(result);
		while(m.find()) {
			result = result.replace("}}", "}]}");
			
			m = pattern.matcher(result);
		}
		PSCategory category = PSCategoryUnMarshaller.unMarshalFromString(result);
		PSCategory updatedResult = client.updateCategories(category, "xyz");
		validateData(PSCategoryMarshaller.marshalToJson(updatedResult));
	}
	
	
	
	private void validateData(String result) {
		result = result.replace("Children", "topLevelNodes");
		result = result.replace("Child", "childNodes");
		
		if(result.contains("\"topLevelNodes\":{")) {
			result = result.replace("\"topLevelNodes\":{", "\"topLevelNodes\":[{");
		}
		
		result = result.replace("\"childNodes\":{", "\"childNodes\":[{");
		Pattern pattern = Pattern.compile("\\}\\}");
		Matcher m = pattern.matcher(result);
		while(m.find()) {
			result = result.replace("}}", "}]}");
			
			m = pattern.matcher(result);
		}
		//System.out.println(result);

		PSCategory category = PSCategoryUnMarshaller.unMarshalFromString(result);
		
		assertNotNull("categories cannnot be null", category);
		/*
		assertEquals(2, category.getTopLevelNodes().size());
		
		for(PSCategoryNode node : category.getTopLevelNodes()) {
			
			if(node.getTitle().equals("Sample Category 1")) {
				assertEquals(5, node.getChildNodes().size());
				
				for(PSCategoryNode childNode : node.getChildNodes()) {
					if(childNode.getTitle().equals("Sample Category 1 1")) {
						assertEquals(1, childNode.getChildNodes().size());
					}
				}
			}
			else if(node.getTitle().equals("Sample Category 2")) 
				assertEquals(5, node.getChildNodes().size());
		} */
	}

}
