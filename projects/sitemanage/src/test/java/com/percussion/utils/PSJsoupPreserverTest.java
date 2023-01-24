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
