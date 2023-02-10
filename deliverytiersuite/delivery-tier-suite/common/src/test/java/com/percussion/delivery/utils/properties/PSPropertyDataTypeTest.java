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
package com.percussion.delivery.utils.properties;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author natechadwick
 *
 */
public class PSPropertyDataTypeTest {
	
	@Test
	public void testTypes(){
		
		Assert.assertEquals("string", PSPropertyDataType.STRING.getName());
		Assert.assertEquals("enum", PSPropertyDataType.ENUM.getName());
		Assert.assertEquals("number", PSPropertyDataType.NUMBER.getName());
		Assert.assertEquals("bool", PSPropertyDataType.BOOL.getName());
		Assert.assertEquals("hidden", PSPropertyDataType.HIDDEN.getName());
		Assert.assertEquals("date", PSPropertyDataType.DATE.getName());
		Assert.assertEquals("list", PSPropertyDataType.LIST.getName());
			
	}
	
	@Test
	public void testString(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("string");
		Assert.assertEquals(String.class, t.getJavaType());
		Assert.assertEquals("string", t.getName());		
	}

	@Test
	public void testEnum(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("enum");
		Assert.assertEquals(String.class, t.getJavaType());
		Assert.assertEquals("enum", t.getName());		
	}

	
	@Test
	public void testNumber(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("number");
		Assert.assertEquals(Number.class, t.getJavaType());
		Assert.assertEquals("number", t.getName());		
	}

	@Test
	public void testBool(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("bool");
		Assert.assertEquals(Boolean.class, t.getJavaType());
		Assert.assertEquals("bool", t.getName());		
	}
	

	@Test
	public void testList(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("list");
		Assert.assertEquals(List.class, t.getJavaType());
		Assert.assertEquals("list", t.getName());		
	}

	@Test
	public void testDate(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("date");
		Assert.assertEquals(Date.class, t.getJavaType());
		Assert.assertEquals("date", t.getName());		
	}


	@Test
	public void testHidden(){
		PSPropertyDataType t =  PSPropertyDataType.parseType("hidden");
		Assert.assertEquals(Object.class, t.getJavaType());
		Assert.assertEquals("hidden", t.getName());		
	}
	
	@Test
	public void testFromProp(){
		PSPropertyDefinition p = new PSPropertyDefinition();
		
		p.setDatatype("hidden");
		
		Assert.assertEquals(Object.class, PSPropertyDataType.fromDefinition(p).getJavaType());
		
		Assert.assertEquals("hidden", PSPropertyDataType.fromDefinition(p).getName());
		
	}
	
}
