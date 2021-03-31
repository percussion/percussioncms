/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.jexl PSOStringTools.java
 *  
 * @author DavidBenua
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.pso.jexl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.pso.utils.PathCleanupUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

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
	   private static final Log log = LogFactory.getLog(PSOStringTools.class);

	
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
       StringBuffer parse = new StringBuffer(body);
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
