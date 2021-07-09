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

package com.percussion.utils;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class PSJsoupPreserverTest {
	
	private static String testCase1 = "<PRESERVE><div class=\"example\"></PRESERVE>";
	private static String testCase2 = "<PRESERVE></div><!-- /.example --></PRESERVE>";
	private static String testCase3 = "<PRESERVE><?php \n"
         + "\n"
         + "//Set stream options\n"
         + "$context = stream_context_create(array('http' => array('ignore_errors' => true)));\n"
         + "if(!isset($_GET['tfa_next'])) {\n"
         + "echo file_get_contents('https://app.formassembly.com/rest/forms/view/329018',false,$context);\n"
         + "} else {\n"
         + "echo file_get_contents('https://app.formassembly.com/rest'.$_GET['tfa_next'],false,$context);\n"
         + "}/n"
         + "?>/n"
         
         
         + "</PRESERVE>";
	


			

	  @Test
	    public void testCases() throws Exception
	    {
		  String preserved = PSJsoupPreserver.formatPreserveTagsForJSoupParse(testCase1);
		 
		  Document doc = Jsoup.parseBodyFragment(preserved);
		  
		  assertEquals(testCase1, PSJsoupPreserver.formatPreserveTagsForOutput(doc.body().html()));
		  
		  
		  preserved = PSJsoupPreserver.formatPreserveTagsForJSoupParse(testCase2);
		  doc = Jsoup.parseBodyFragment(preserved);
		  assertEquals(testCase2, PSJsoupPreserver.formatPreserveTagsForOutput(doc.body().html()));
		  
		  preserved = PSJsoupPreserver.formatPreserveTagsForJSoupParse(testCase3);
		  doc = Jsoup.parseBodyFragment(preserved);
		  assertEquals(testCase3, PSJsoupPreserver.formatPreserveTagsForOutput(doc.body().html()));
		  
		  
	    }
		

}
