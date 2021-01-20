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
package com.percussion.data;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.StringOutputSegment;
import au.id.jericho.lib.html.Tag;

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A Utility class that contains useful cleanup methods for
 * pre and post processing a stylesheet
 */
public class PSStylesheetCleanupUtils
{

   /**
    * Private constructor so this class won't be instantiated
    */
   private PSStylesheetCleanupUtils(){}
   
   /**
    * Cleans up HTML string to make it valid XHTML based on the rules
    * specified in the passed in stylesheet cleanup filter.
    * 
    * @param htmlText the html/xhtml source to process, may be 
    * <code>null</code> or empty.
    * 
    * @param filter the stylesheet cleanup filter that specifies the
    * allowed namespace pieces, cannot be <code>null</code>
    * 
    * @return filtered text string, may be <code>null</code>
    * or empty if the supplied <code>htmlText</code> is 
    * <code>null</code> or empty
    */
   public static String namespaceCleanup(
      String htmlText, PSStylesheetCleanupFilter filter)
   {
      if(filter == null)
         throw new IllegalArgumentException(
            "Stylesheet cleanup filter cannot be null.");
      if(htmlText == null || htmlText.trim().length() == 0)
         return htmlText;
      // The StringBuffer is used to build the modified
      // start tags
      StringBuffer sb = new StringBuffer();
      OutputDocument outDoc = new OutputDocument(htmlText);
      Source source = new Source(htmlText);
      StartTag sTag = null;
      Attributes attributes = null;
      boolean replaceTag = false;
      // Loop through every start tag
      for(Iterator it = source.findAllStartTags().iterator(); it.hasNext();)
      {
         replaceTag = false;
         sTag = (StartTag)it.next();
         if(!isNormalMarkupTag(sTag))
            continue; // Skip if this is not a normal markup tag
         
         // Remove elements that use namespaces leaving the normal
         // child tags and text alone
         String[] qname = parseQName(sTag.getName()); 
         if(!filter.isNSElementAllowed(qname[0], qname[1]))
         {
            outDoc.add(new StringOutputSegment(sTag, ""));
            if(!sTag.isEmptyElementTag())
            {
               EndTag eTag = sTag.findEndTag();
               if(eTag != null)
                  outDoc.add(new StringOutputSegment(eTag, ""));
            }
            continue;
         }
         
         attributes = sTag.getAttributes();
         Iterator attrs = attributes.iterator();
         sb.setLength(0);
         sb.append("<");
         sb.append(getTagNameAsIs(sTag));
        
         // Remove namespace declarations or attribute that use a 
         // namespace.
         while(attrs.hasNext())
         {            
            Attribute attr = (Attribute)attrs.next();
            String attrName = attr.getName().toLowerCase();
            qname = parseQName(attrName);
            // Handle namespace declarations
            if(qname[1].toLowerCase().equals("xmlns")) 
            {
               if(!filter.isNSDeclarationAllowed(qname[0], attr.getValue()))
               {
                  replaceTag = true;
                  continue;
               }
            }
            else
            {
               // Handle attributes
               if(!filter.isNSAttributeAllowed(qname[0], qname[1]))
               {
                  replaceTag = true;
                  continue;
               }
            }
            sb.append(" ");
            sb.append(attr.getName());
            String val = attr.getValue();
            // Set the enclosing character for attributes to double quote and if
            // the attribute value contains a double quote then reset it to
            // single quote.
            String enclChar = "\"";
            if(val.contains("\""))
               enclChar = "'";
            sb.append("="+enclChar);
            sb.append(attr.getValue());
            sb.append(enclChar);   
                     
         }
         if(sTag.isEmptyElementTag())
         {
            sb.append(" /");
            replaceTag = true;
         }
         sb.append(">");   
         if(replaceTag)
         {
            outDoc.add(new StringOutputSegment(sTag, sb.toString()));
         }
      }
      return outDoc.toString();

   } 
   
   /**
    * Determines if this markup tag is just a plain markup tag as
    * opposed to an XML declaration tag or a server side scripting tag 
    * or a doc type tag.
    * @param tag the tag to be checked, assumed not <code>null</code>.
    * @return <code>true</code> if this is a normal markup tag.
    */
   public static boolean isNormalMarkupTag(StartTag tag)
   {
      return (
         !tag.isComment()
         && !tag.isCommonServerTag()
         && !tag.isDocTypeDeclaration()
         && !tag.isMasonComponentCall()
         && !tag.isMasonComponentCalledWithContent()
         && !tag.isMasonNamedBlock()
         && !tag.isMasonTag()
         && !tag.isPHPTag()
         && !tag.isProcessingInstruction()
         && !tag.isServerTag()
         && !tag.isXMLDeclaration());
   }
   
   /**
    * Returns the tags name as cased in the source
    * @param tag the tag from which to parse the name,
    * assumed not <Code>null</code>.
    * @return the tag name as appears in the original 
    * source.
    */
   private static String getTagNameAsIs(Tag tag)
   {
      String source = tag.getSourceText();
      StringTokenizer st = new StringTokenizer(source, "</> ");
      if(st.countTokens() >= 1)
      {
         return st.nextToken();
      }
      return tag.getName();      
      
   }
   
   /**
    * Parses out the qualified name for an element, attribute or namespace
    * declaration and returns the parts in an array.
    * <p><pre>
    * [0] = the namespace, will be null if no namespace
    * [1] = the name
    * </pre></p>
    * @param name the name of an element or attribute (i.e. foo:test),
    * cannot be <code>null</code> or empty.
    * @return array of name parts, never <code>null</code>
    */
   private static String[] parseQName(String name)
   {
      if(name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("Name cannot be null or empty.");
      String[] results = new String[2];
      StringTokenizer st = new StringTokenizer(name, ":");
      if(st.countTokens() == 1)
      {
         results[1] = st.nextToken(); 
      }
      else if(name.toLowerCase().indexOf("xmlns") == -1)
      {
         results[0] = st.nextToken();
         results[1] = st.nextToken();
      }
      else
      {
         results[1] = st.nextToken();
         results[0] = st.nextToken();
      }
      return results;
   }
}
