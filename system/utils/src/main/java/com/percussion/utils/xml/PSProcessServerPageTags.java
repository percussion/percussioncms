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
package com.percussion.utils.xml;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides all functionality to handle server page code for the
 * split and merge processes. The approach we took has two main steps:
 * <ol>
 * <li>The source HTML is passed through the preProcess method to replace
 * all server page source code with our own markup. While doing this the
 * original code is stored in a map together with the key of our markup.</li>
 * <li>This specially marked HTML file is now ready to be split using tidy.</li>
 * <li>After the split process we put back the original server page code.
 * If the original code was part of an attribute, it will be escaped before
 * put back so the input parser is happy. Otherwise the original code is wrapped with
 * '<xsl:text disable-output-escaping="yes"><![CDATA[' ... ']]></xsl:text>'
 * to tell the parser not to escape the generated output.</li>
 * </ol>
 */
public class PSProcessServerPageTags extends Object
{
   private static final Logger log = LogManager.getLogger(PSProcessServerPageTags.class);
    /**
    * Constructs and initializes the state machine.
    *
    * @param filePath the absolute path of the server page tags XML file.
    *
    * @throws IOException if the server page tag file is invalid.
    */
   public PSProcessServerPageTags(File filePath) throws IOException
   {
      m_serverPageTags = getXMLDocument(filePath);
      initTagVectors();
   }
   /**
    * Call this to process the provided source for server page tags.
    *
    * @param htmlSource the source HTML page to pre-process server page tags.
    * @return the processed HTML string.
    */
   public String preProcess(String htmlSource)
   {
      m_htmlSource = htmlSource;

      // initialize the markup hash map and make sure our key is unique
      int counter = 0;
      m_codeMap.clear();
      m_escapeMap.clear();
      while (m_htmlSource.indexOf(m_keyPrefix) != -1)
         m_keyPrefix += counter;

      m_htmlTarget = new StringBuilder(m_htmlSource.length());

      m_current = 0;
      m_lastClose = 0;
      int closingTagIndex = setNextOpeningTag(m_current);
      while (m_nextOpen != -1)
      {
         int skipTagIndex = nextSkipTag(m_current);
         if (m_nextOpen < m_nextSkip || m_nextSkip == -1)
            markIt(closingTagIndex);
         else
            skipIt(skipTagIndex);

         closingTagIndex = setNextOpeningTag(m_current);
      }
      m_htmlTarget.append(m_htmlSource.substring(m_current,
                                                 m_htmlSource.length()));

      return m_htmlTarget.toString();
   }

   /**
    * This goes through the map created in the pre process and replaces the
    * XSpLit markups with its original server page code.
    *
    * @param xslSource the source XSL string
    * @return the processed XSL string
    */
   public String postProcess(String xslSource)
   {
      StringBuilder xslTarget = new StringBuilder(xslSource);
      Vector<String> topElements = new Vector<String>();

      String key = "";
      String serverPageBlock = "";

      int stylesheetStart = 0;
      int pos = 0;
      Iterator keys = m_codeMap.keySet().iterator();
      while (keys.hasNext())
      {
         stylesheetStart = xslTarget.toString().indexOf("<xsl:stylesheet");
         key = (String) keys.next();
         pos = xslTarget.toString().indexOf(key);
         String strDisable = (String) m_escapeMap.get(key);
         if (strDisable != null && strDisable.equalsIgnoreCase("yes"))
            serverPageBlock = (String) m_codeMap.get(key);
         else
            serverPageBlock = escape(key, (String) m_codeMap.get(key),
                                     stylesheetStart, pos);

         if (serverPageBlock.startsWith("<xsl:include") ||
             serverPageBlock.startsWith("<xsl:import"))
         {
            topElements.add(serverPageBlock);
            xslTarget.replace(pos, pos+key.length(), "");
         }
         else
         {
            if (pos != -1)
               xslTarget.replace(pos, pos+key.length(), serverPageBlock);
            else
               log.error("ERROR - postProcess: missing XSpLit markup key! {}" , key);
         }
      }

      // add top elements
      stylesheetStart = xslTarget.toString().indexOf("<xsl:stylesheet");
      pos = xslTarget.toString().indexOf(">", stylesheetStart + ("<xsl:stylesheet").length())+1;
      if (pos != -1)
      {
         for (int i=0; i<topElements.size(); i++)
         {
            String strTop = topElements.elementAt(i);

            xslTarget.insert(pos++, "\n");
            xslTarget.insert(pos, strTop);
            pos = pos+strTop.length();
         }
      }

      return xslTarget.toString();
   }

   /**
    * In case this key did replace an attribute entry, we need to escape the
    * code block.
    * Whether or not this was an attribute entry is determined by the key
    * used. If the key is wrapped with HTML comment opening/closing tags it
    * is not an attribute entry, otherwise it is.
    *
    * @param key the key used to mark up the code block.
    * @param the codeBlock to escape.
    * @param stylesheetStart the start position of the stylesheet declaration.
    * @param current the currently processed position.
    * @return the escaped code block if this is for an attribute entry.
    */
   private String escape(String key, String codeBlock,
                         int stylesheetStart, int current)
   {
      if (key.startsWith("<!--"))
      {
         String temp = ms_strCDATABegin + codeBlock + ms_strCDATAEnd;
         if (current < stylesheetStart)
            return temp;
         else
            return ms_strXslTextBegin + temp + ms_strXslTextEnd;
      }

      StringBuilder escapedBlock = new StringBuilder(codeBlock);
      escapedBlock.replace(0, 1, "&lt;");
      int length = escapedBlock.length();
      escapedBlock.replace(length-1, length, "&gt;");

      return escapedBlock.toString();
   }

   /**
    * This function will replace all server page blocks found with an
    * enumerated key and put it together with the key into a hash map. After
    * the splitting process we must put back the removed code into the XSL
    * document created, marking it as CDATA.
    *
    * @param tagIndex the closing tag index to use.
    */
   @SuppressWarnings("unchecked")
   private void markIt(int tagIndex)
   {
      int oldCurrent = m_current;

      String strClosingTag = (String) m_closingTags.elementAt(tagIndex);
      String strOpeningTag = (String) m_openingTags.elementAt(tagIndex);
      log.debug("Opening Tag is: {}" , strOpeningTag);
      int nextOpening = getNextOpeningTag(m_nextOpen+strOpeningTag.length(), tagIndex);
      int nextClosing = m_htmlSource.indexOf(strClosingTag,
                                             m_nextOpen+strOpeningTag.length());

      // try the default closing tag
      if (nextClosing == -1)
      {
         strClosingTag = "/>";
         nextClosing = m_htmlSource.indexOf(strClosingTag,
                                            m_nextOpen+strOpeningTag.length());
      }

      if (nextClosing == -1)
      {
         // skip this and report error
         m_current = m_nextOpen+strOpeningTag.length();
         log.error("ERROR - scriptIt: illegal source HTML! Missing closing tag.");
      }

      while (nextOpening != -1 && (nextClosing > nextOpening || nextClosing == -1))
      {
         // skip nested opening-closing pairs
         nextClosing = m_htmlSource.indexOf(strClosingTag,
                                            nextClosing+strClosingTag.length());
      }
      if (nextClosing != -1)
      {
         m_current = nextClosing+strClosingTag.length();

         boolean isAttr = isAttribute(oldCurrent);
         String key = getNextKey(isAttr);
         m_htmlTarget.append(key);

         String strKey = getPostProcessKey(isAttr);
         m_codeMap.put(strKey, m_htmlSource.substring(oldCurrent, m_current));
         m_escapeMap.put(strKey, m_disableEscaping.elementAt(tagIndex));

         m_lastClose = m_current;
      }
      else
      {
         // make sure we skip this and report error
         m_current = m_nextOpen+strOpeningTag.length();
         log.error("ERROR - scriptIt: illegal source HTML! Unbalanced closing tags.");
      }
   }

   /**
    * This method returns whether the current handled server page block is
    * part of an attribut or not.
    *
    * @param end the end position until we search for the attribute closing tag.
    * @return <code>true</code> if part of an attribute, <code>false</code>
    *    otherwise.
    */
   private boolean isAttribute(int end)
   {
      int close = m_lastClose;
      int open;
      while (close < end && close != -1)
      {
         open = m_htmlSource.indexOf("<", close);
         if (open < end && open != -1)
            close = m_htmlSource.indexOf(">", open);
         else
            break;
      }

      return (close > end && close != -1);
   }

   /**
    * This will return the next key to for our server page markup. It will be
    * the key prefix plus an incremented counter.
    *
    * @param isAttribute whether or not to get the attribute or regular key.
    */
   private synchronized String getNextKey(boolean isAttribute)
   {
      if (isAttribute)
         return m_keyPrefix + "_" + (++ms_keyCount);

      return "<!-- " + m_keyPrefix + "_" + (++ms_keyCount) + " -->";
   }

   /**
    * The XSL output is adding 2 spaces to the open and close comment tag.
    * Use this function to save the key we are looking for in the post
    * process.
    *
    * @param isAttribute whether or not to get the attribute or regular key.
    */
   private synchronized String getPostProcessKey(boolean isAttribute)
   {
      if (isAttribute)
         return m_keyPrefix + "_" + ms_keyCount;

      return "<!--   " + m_keyPrefix + "_" + ms_keyCount + "   -->";
   }

   /**
    * This will skip the current position to the next occurence of the skip
    * tag provided.
    *
    * @param tagIndex the index of the skip tag to use.
    */
   private void skipIt(int tagIndex)
   {
      int oldCurrent = m_current;
      if (m_skipTags.isEmpty())
      {
         m_nextSkip = -1;
         return;
      }

      String strTag = (String) m_skipTags.elementAt(tagIndex);
      int index = m_htmlSource.indexOf(strTag, m_nextSkip);
      if (index != -1)
      {
         m_current = index+strTag.length();
         m_htmlTarget.append(m_htmlSource.substring(oldCurrent, m_current));
      }
      else
         log.error("ERROR - skipIt: illegal state!");
   }

   /**
    * Calculates and setx the index of the next opening tag starting from
    * the provided index.
    *
    * @param start the index to start from.
    * @return the vector index of the openingTag for which we found the next
    *    position.
    */
   private int setNextOpeningTag(int start)
   {
      // assume there is no more opening tags
      m_nextOpen = -1;

      int tagIndex = -1;
      int temp = -1;
      for (int i=0, count=m_openingTags.size(); i<count; i++)
      {
         temp = m_htmlSource.indexOf((String) m_openingTags.elementAt(i),
                                     start);
         if (temp != -1)
         {
            if (m_nextOpen == -1 || temp < m_nextOpen)
            {
               tagIndex = i;
               m_nextOpen = temp;
            }
         }
      }

      if (m_nextOpen != -1)
      {
         m_current = m_nextOpen;
         m_htmlTarget.append(m_htmlSource.substring(start, m_current));
      }

      return tagIndex;
   }

   /**
    * Calculates the index of the next opening tag starting from the provided
    * index.
    *
    * @param start the index to start from.
    * @param tagIndex the tag index to use
    * @return the next found opening tag.
    */
   private int getNextOpeningTag(int start, int tagIndex)
   {
      return m_htmlSource.indexOf((String) m_openingTags.elementAt(tagIndex),
                                  start);
   }

   /**
    * Calculate the next skip opening tag starting at the given position.
    *
    * @param start the index to start from.
    * @return the vector index of the skip tag for which we found the next
    *    position.
    */
   private int nextSkipTag(int start)
   {
      // assume there is no more opening tags
      m_nextSkip = -1;

      if (m_skipTags.isEmpty())
      {
         // no skip tags defined
         m_nextSkip = -1;
         return -1;
      }

      int tagIndex = -1;
      int temp = -1;
      for (int i=0, count=m_skipTags.size(); i<count; i++)
      {
         temp = m_htmlSource.indexOf((String) m_skipTags.elementAt(i),
                                     start);
         if (temp != -1)
         {
            if (m_nextSkip == -1 || temp < m_nextSkip)
            {
               tagIndex = i;
               m_nextSkip = temp;
            }
         }
      }

      return tagIndex;
   }

   /**
    * Initialize the tag vectors. The vectors of opening and closing tags
    * are created from the external XML file 'serverPageTags.xxml'. The skip
    * vector is currently hardcoded.
    *
    * @throws IOException if the server page tag file is invalid.
    */
   private void initTagVectors() throws IOException
   {
      NodeList openings = m_serverPageTags.getElementsByTagName("opening");
      NodeList closings = m_serverPageTags.getElementsByTagName("closing");
      NodeList disableEscaping = m_serverPageTags.getElementsByTagName("disableEscaping");

      if (openings == null || closings == null || disableEscaping == null)
         throw new IOException("Invalid TagFile");

      int count = openings.getLength();
      if (count != closings.getLength() || count != disableEscaping.getLength())
         throw new IOException("Unbalanced TagFile");

      m_openingTags = new Vector<>(count);
      m_closingTags = new Vector<>(count);
      m_disableEscaping = new Vector<>(count);
      for (int i=0; i<count; i++)
      {
         Node openingNode = openings.item(i).getFirstChild();
         if (openingNode instanceof Text)
            m_openingTags.add(((Text) openingNode).getData());

         Node closingNode = closings.item(i).getFirstChild();
         if (closingNode instanceof Text)
            m_closingTags.add(((Text) closingNode).getData());

         Node disableEscapingNode = disableEscaping.item(i).getFirstChild();
         if (disableEscapingNode instanceof Text)
            m_disableEscaping.add(((Text) disableEscapingNode).getData());
      }

      m_skipTags = new Vector<>(2);
      m_skipTags.add("\"");
      m_skipTags.add("'");
   }

   /**
    * This parses the XML document from the provided file. An error is
    * reported to the user if something goes wrong.
    *
    * @param xmlFile the file to parse from.
    * @return the document we have read and parsed. If something failed we
    *    will return null.
    */
   private static Document getXMLDocument(File xmlFile)
   {
      try
      {
         FileReader reader = new FileReader(xmlFile);
         InputSource src = new InputSource(reader);
         DocumentBuilder db = PSXmlDocumentBuilder.getDocumentBuilder(false);

         return db.parse(src);
      }
      catch (FileNotFoundException e)
      {
         log.error("Error: Could not find the JSP / ASP tags file: {}" ,
                            xmlFile.getAbsolutePath());
      }
      catch (IOException e)
      {
         log.error("Error: Could not initialize JSP / ASP tags: {}",
                            e.getMessage());
         log.debug(e.getMessage(),e);
      }
      catch (SAXException e)
      {
         log.error("Error: Could not initialize JSP / ASP tags: {}",
                            e.getMessage());
      }

      return null;
   }

   /**
    * This is the hash table which will be used to store the removed server
    * page code.
    */
   private ConcurrentHashMap m_codeMap = new ConcurrentHashMap<>();
   /**
    * This is the hash table which will be used to store the enable/disable
    * escape information. The keys correspond to the keys in the code map.
    */
   private ConcurrentHashMap m_escapeMap = new ConcurrentHashMap<>();
   /**
    * The key prefix used to mark removed server page code.
    */
   private String m_keyPrefix = "XSpLit_Server_Page_Block";
   /**
    * The key counter.
    */
   private static int ms_keyCount = 0;
   /**
    * A vector of opening tags.
    */
   private Vector m_openingTags = null;
   /**
    * A vector of closing tags.
    */
   private Vector m_closingTags = null;
   /**
    * A vector of disable escaping information.
    */
   private Vector m_disableEscaping = null;
   /**
    * A vector of skip tags.
    */
   private Vector m_skipTags = null;
   /**
    * The source HTML string to pre-process server page tags for.
    */
   private String m_htmlSource = null;
   /**
    * The target HTML string to which we build the result to.
    */
   private StringBuilder m_htmlTarget = null;
   /**
    * The current index of the state machine.
    */
   private int m_current = 0;
   /**
    * The next index of opening tag found. -1 indicates there is no next
    * opening tag index.
    */
   private int m_nextOpen = 0;
   /**
    * The last closeing tag position marked.
    */
   private int m_lastClose = 0;
   /**
    * The next index of skip tag found. -1 indicates there is no next
    * skip tag index.
    */
   private int m_nextSkip = 0;
   /**
    * All documentation opening tags.
    */
   private static final Vector<String> ms_openDocTags = new Vector<>();
   /**
    * All documentation closing tags.
    */
   private static final Vector<String> ms_closeDocTags = new Vector<>();
   /**
    * Initialize the documentation tags.
    */
   static
   {
      ms_openDocTags.add("<!--");
      ms_closeDocTags.add("-->");

      ms_openDocTags.add("<%--");
      ms_closeDocTags.add("--%>");
   }
   /**
    * The document which holds the JSP / APS tags that need special handling
    * for tidy. If the file is not found, the splitter still works fine for
    * all cases where no JSP and/or ASP tags are used.
    */
   private Document m_serverPageTags;
   /**
    * The CDATA wrapper opening part.
    */
   private static final String ms_strCDATABegin = "<![CDATA[\n";
   /**
    * The CDATA wrapper closing part.
    */
   private static final String ms_strCDATAEnd = "\n]]>";
   /**
    * The xsl:text wrapper opening part.
    */
   private static final String ms_strXslTextBegin = 
      "\n<xsl:text disable-output-escaping=\"yes\">";
   /**
    * The xsl:text wrapper closing part.
    */
   private static final String ms_strXslTextEnd = "</xsl:text>";
}
