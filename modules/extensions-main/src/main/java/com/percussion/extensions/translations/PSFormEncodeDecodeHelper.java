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
package com.percussion.extensions.translations;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.EndTag;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.StringOutputSegment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to handle the encoding and decoding of HTML form tag elements
 * to and from a div tag marker used to represent the form tag. This is needed as a workaround
 * for EditLive single instance when form tags need to be present in the content.
 * 
 * Will also encode and decode embed tags as well.
 * @author erikserating
 *
 */
public class PSFormEncodeDecodeHelper
{

   private static final Logger log = LogManager.getLogger(PSFormEncodeDecodeHelper.class);
   /**
    * Looks for form tags and turns them into div tags with a special rxFormTagMarker
    * attribute that indicates the div tag is being used as a form tag placeholder.
    * @param content the html content string, cannot be <code>null</code>.
    * @return the encoded content string, never <code>null</code>.
    */
   public static String encode(String content)
   {
      if(content == null)
         throw new IllegalArgumentException("content cannot be null.");
      for(int i = 0; i < ms_includedTags.length; i++)
      {
         content = transformTag(content, ms_includedTags[i], true);
      }
      return content;
   }
 
   /**
    * Looks for div tags with a special rxFormTagMarker attribute that indicates
    * the div tag is being used as a form tag placeholder and turns them back into form tags.
    * @param content the html content string, cannot be <code>null</code>.
    * @return the encoded content string, never <code>null</code>.
    */
   public static String decode(String content)
   {
      if(content == null)
         throw new IllegalArgumentException("content cannot be null.");
      for(int i = 0; i < ms_includedTags.length; i++)
      {
         content = transformTag(content, ms_includedTags[i], false);
      }
      return content;
   }
   
   /**
    * Helper method to encapsulate the transformation of the tag from a encoded or
    * decoded state.
    * @param content cannot be <code>null</code>
    * @param tagname cannot be <code>null</code> or empty.
    * @param encode flag indicating this is a encode transform.
    * @return transformed string, never <code>null</code>.
    */
   private static String transformTag(
      String content, String tagname, boolean encode)
   {
      if(content == null)
         throw new IllegalArgumentException("content cannot be null.");
      if(tagname == null || tagname.length() == 0)
         throw new IllegalArgumentException("tagname cannot be null or empty.");
            
      String findTag = ELEM_DIV;
      String newTag = tagname;
      String marker = "rx" + toProperCase(tagname) + "TagMarker";
      boolean isScriptTag = marker.equals("rxScriptTagMarker");
      
      if(encode)
      {
         // Fix comment tags and make sure there is space before the script
         // tag element to get around Jericho parsing bug.         
         content = fixCommentTags(
            content.replaceAll("\\>\\<script", "> <script"));
         findTag = tagname;
         newTag = ELEM_DIV;
      }
      else
      {
         //replace the pageBreak elements with the proper PI
         content = Pattern.compile("<pageBreak>", Pattern.CASE_INSENSITIVE).
            matcher(content).replaceAll("<?pageBreak?>");
         content = Pattern.compile("</pageBreak>", Pattern.CASE_INSENSITIVE).
            matcher(content).replaceAll("");
      }
      
      final Source source = new Source(content);
      final OutputDocument outDoc = new OutputDocument(source);
      StringBuilder buff = new StringBuilder();
      StartTag sTag = null;
      EndTag eTag = null;
      int idx = 0;
      while ((sTag = source.findNextStartTag(idx, findTag)) != null)
      {
         buff.setLength(0);
         Attributes attrs = sTag.getAttributes();
         Attribute att = null;
         if(!encode)   
            att = attrs.get(marker);
         if (encode || att != null)
         {
            buff.append("<");
            buff.append(newTag);
            if(encode)
            {
               buff.append(SPACE);
               buff.append(marker);
               buff.append("=\"true\"");
            }
            Iterator it = attrs.iterator();
            while (it.hasNext())
            {
               Attribute at = (Attribute) it.next();
               if(!encode && at.getName().equalsIgnoreCase(marker))
                  continue;
               buff.append(SPACE);
               buff.append(at.getName());
               buff.append("=\"");
               buff.append(at.getValue());
               buff.append("\"");
            }
            buff.append(">");
            if(encode && isScriptTag)
               buff.append(HTML_COMMENT_BEGIN + UNIQUE);
            eTag = sTag.findEndTag();
            boolean empty = sTag.isEmptyElementTag();
            String endComm = "";
            if(encode && isScriptTag)
               endComm = UNIQUE + HTML_COMMENT_END;
            String endTag = endComm + "</" + newTag + ">";
            outDoc.add(new StringOutputSegment(sTag,
               buff.toString() + (empty ? endTag : "")));
            if(!empty)
               outDoc.add(new StringOutputSegment(eTag, endTag));
         }
         idx = sTag.getEnd();
      }
      String result = outDoc.toString();
      if(!encode && isScriptTag)
      {
         result = result.replaceAll(HTML_COMMENT_BEGIN + UNIQUE, "");
         result = result.replaceAll(UNIQUE + HTML_COMMENT_END, "");
      }
      return result;
   }
   
   /**
    * Fixes comments that have no spaces in them. This is to get around
    * a Jericho parsing bug where a comment without spaces such as
    * &lt;!--something--&gt; causes invalid parsing.
    * @param str the content string to be fixed, may be <code>null</code>.
    * @return the fixed up comment, may be <code>null</code>.
    */
   private static String fixCommentTags(String str)
   {
      if(str == null)
         return null;
      // This pattern finds all comments that look like <!--nospace--> and
      // replaces it with <!-- nospace -->.
      Pattern commentPattern1 =  
         Pattern.compile("\\<!--([^ ]{1}([^\\- ]|[\\r\\n]|-[^\\- ])*[ \\r\\n\\t]*[^ ]{1})--\\>",
            Pattern.CASE_INSENSITIVE);
      Matcher matcher = commentPattern1.matcher(str);
      return matcher.replaceAll("<!-- $1 -->");     
   }
   
   private static String toProperCase(String str)
   {
      StringBuilder buff = new StringBuilder(str.toLowerCase());
      if(buff.length() > 0)
      {
         buff.replace(0, 1, buff.substring(0, 1).toUpperCase());
      }
      return buff.toString();
   }
   
   // Main for testing
   public static void main(String[] args)
   {
      String encoded = encode(ms_test_string);
      String decoded = decode(encoded);
      log.info(encoded);
      log.info("\n========================================\n");
      log.info(decoded);
   }
   
   /**
    * Tags that will be included in the decoding and encoding.
    */
   public static String[] ms_includedTags = new String[]
   {
      "embed",
      "form",
      "script"
   };
         
   /**
    * Constant for the div element.
    */
   public static final String ELEM_DIV = "div";
   
   /**
    * Constant for a single space.
    */
   public static final String SPACE = " ";
   
   /**
    * Constant for HTML comment begin.
    */
   private static final String HTML_COMMENT_BEGIN = "<!--";
   
   /**
    * Constant for HTML comment end.
    */
   private static final String HTML_COMMENT_END = "-->";
   
   /**
    * Constant for the unique string that is concatenated to
    * a comment begin or end.
    */
   private static final String UNIQUE = "@@__8SCR";
   
   //Test string
   public static final String ms_test_string = 
      "<body>\n<form action=\"test.jsp\" method=\"post\">\n" + 
      "<pageBreak></pageBreak>\n" +
      "<input type=\"text\" value=\"field\">\n</form>\n" +
      "<object><embed></embed></object>\n" +
      "<form action=\"test2.jsp\" method=\"get\">\n" + 
      "<input type=\"button\" value=\"field\">\n</form>\n" +
      "<table><tbody><tr><td>\n" +
      "<!--somecomment--><script language=\"JavaScript1.1\" src=\"http://ads.vegas.com/js.ng/site=vegas&amp;size=html&amp;ppos=guide014&amp;ch1=v_realestate&amp;ch2=builders\" type=\"text/javascript\" />\n" +
      "</td></tr></tbody></table>\n" +
      "<script type=\"javascript\">\nvar dummy = 1;\n</script>\n</body>"; 
   
   
   
}
