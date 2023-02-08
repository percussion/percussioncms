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

package com.percussion.pso.jexl;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.pso.utils.PathCleanupUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

/**
 * Tools for String manipulation. Just basics for now
 *
 * @author DavidBenua
 *
 */
public class PSOStringTools extends PSJexlUtilBase implements IPSJexlExpression
{
   
	 /**
	    * Logger for this class
	    */
	   private static final Logger log = LogManager.getLogger(PSOStringTools.class);

	
	/**
    * Default constructor. 
    */
   public PSOStringTools()
   {
   }
   
   
   /**
    * Gets a StringBuilder for use in concatenating Strings.  JEXL has does 
    * some funny type conversions, and this class forces everything to be
    * a String
    * @param value the initial value.
    * @return the StringBuilder.  Never <code>null</code>. 
    */
   @IPSJexlMethod(description="gets a StringBuilder for concatenating strings",
         params={@IPSJexlParam(name="value", description="initial value")})
   public StringBuilder getStringBuilder(String value)
   {
      return new StringBuilder(value);
   }

   /**
    * Gets an empty StringBuilder for use in concatenating Strings.  JEXL has does 
    * some funny type conversions, and this class forces everything to be
    * a String. 
    * @return the StringBuilder.  Never <code>null</code>. Always <code>empty</code> 
    */
   @IPSJexlMethod(description="gets a StringBuilder for concatenating strings",
         params={})
   public StringBuilder getStringBuilder()
   {
      return new StringBuilder();
   }
   
   /**
    * Gets a locale based on the string representation. 
    * @param locString the String. 
    * @return the target Locale object.  Will be <code>null</code> if the input is 
    * <code>null</code> or invalid.  
    */
   @IPSJexlMethod(description="gets a java.util.Locale based on the String representation",
         params={@IPSJexlParam(name="locString", description="locale as a String")})
   public Locale getLocale(String locString)
   {
      return PSI18nUtils.getLocaleFromString(locString); 
   }
   
   
   @IPSJexlMethod(description="Removes XML markup from a string.",
           params={@IPSJexlParam(name="body", description="A string with xml markup.")})
   public String removeXml(String body) throws IOException, SAXException {
       String wrapper = "<wrapper>" + body + "</wrapper>";
       StringReader reader = new StringReader(wrapper);
       Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
       Element root = doc.getDocumentElement();
       if (root == null) return "";
       return root.getTextContent();
   }

   @IPSJexlMethod(description="Truncates a string by words. " +
   		"A word is a group of one or more adjacent letters that are not whitespace (nbsp is a whitespace).",
           params={
           @IPSJexlParam(name="body", description="the string to truncate"),
           @IPSJexlParam(name="maxWords", description="The maximum number of words")
           })
   public String truncateByWords(String body, Number maxWords) {
       int size = body.length();
       int words = 0;
       boolean inWord = false;
       StringBuilder parse = new StringBuilder(body);
       for(int i = 0; i < size; i++) {
           int code = parse.codePointAt(i);
           if (Character.isWhitespace(code) || code == 0x00a0) {
               inWord = false;
               if (words == maxWords.intValue()) {
                   return parse.substring(0, i);
               }
           }
           else if ( ! inWord ){
               inWord = true;
               ++words;
           }
       }
       return body;
   }
   
	@IPSJexlMethod(description = "Creates a very clean path using - for word separators  "
			+ " If includesExtension is true then a final single . is maintained.", params = {
			@IPSJexlParam(name = "path", description = "the path to clean"),
			@IPSJexlParam(name = "forceLower", description = "make the path all lower case"),
			@IPSJexlParam(name = "includesExtension", description = "Keep a final . for the extension"),
			@IPSJexlParam(name = "stripExtension", description = "Do we strip any extension if exists"),
			@IPSJexlParam(name = "prefix", description = "Prefix to add to filename"),
			@IPSJexlParam(name = "suffix", description = "Suffix to add to filename before any extension"),
			@IPSJexlParam(name = "forceExtension", description = "Add or replace extension with this value") })
	public String cleanupPath(String path, boolean forceLower,
			boolean includesExtension, boolean stripExtension, String prefix,
			String suffix, String forceExtension) {
		return PathCleanupUtils.cleanupPathPart(path, forceLower,
				includesExtension, stripExtension, prefix, suffix,
				forceExtension);
	}

}
