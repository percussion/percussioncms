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
