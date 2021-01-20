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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
