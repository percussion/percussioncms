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

package com.percussion.assetmanagement.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/***
 * Basic tests for the CSV Row base class.
 * 
 * @author natechadwick
 *
 */
public class PSAbstractBaseCSVReportRowTest {

	private PSTestCSVReportRow row=null;
	
	@Before
	public void Setup(){
		row = new PSTestCSVReportRow();
		row.col1="A1";
		row.col2="A2";
		row.col3multiline = "A3a\r\nA3b\r\nA3c";
		row.col4empty="";
	}
	
	@Test
	public void testToCSVRow(){
		String test = row.toCSVRow();
		assertEquals("Values should match",test,"\"A1\",\"A2\",\"A3a\r\nA3b\r\nA3c\",\"\"\r\n");
	}
	
	@Test
	public void testDelimitValue(){
		String test = row.delimitValue("myval");
		
		assertEquals("Values should match","\"myval\"", test);
		
	}
	
	@Test
	public void testCSVEscapeString(){
		String test = row.csvEscapeString("The world is a \"vampire\"");
		
		assertTrue("String shouldn't have any quotes", !test.contains("\""));
		
	}

}
