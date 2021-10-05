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
package com.percussion.xsl.encoding;

import com.icl.saxon.charcode.PluggableCharacterSet;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for adding new character sets to Saxon from an XML document.
 * The document defines which character codes are present in the set.  The 
 * format of the document is defined by: 
 * <code>http://www.unicode.org/unicode/reports/tr22/CharacterMapping.dtd</code>
 * <p>
 * Source of the XML documents: 
 * <code>http://oss.software.ibm.com/cvs/icu/charset/data/xml/</code>
 */
public abstract class PSGenericCharacterSet implements PluggableCharacterSet
{
   private static final Logger log = LogManager.getLogger(PSGenericCharacterSet.class);
   /**
    * Creates an instance of a <code>PluggableCharacterSet</code> for the 
    * specified character encoding by loading the specified resource file into
    * a boolean array.
    * 
    * @param encodingName the name of the encoding defined by this instance,
    * not <code>null</code> or empty.
    * 
    * @param rscsName the name of a resource file that contains the XML
    * definition of the encoding, not <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if either parameter is <code>null</code>
    * or empty, or if the resource specified by <code>rscsName</code> does
    * not exist.
    * 
    * @throws IOException if there is a problem reading the resource file.
    */ 
   protected PSGenericCharacterSet(String encodingName, String rscsName)
      throws IOException
   {
      if (null == encodingName || 0 == encodingName.trim().length())
         throw new IllegalArgumentException(
            "encodingName must not be null or empty");
      
      if (null == rscsName || 0 == rscsName.trim().length())
         throw new IllegalArgumentException(
            "rscsName must not be null or empty");
      
      m_encodingName = encodingName;
      
      // look for a cached encoding array
      m_characterInEncoding = (boolean[]) ms_cache.get( encodingName );
      if (null == m_characterInEncoding)
      {
         m_characterInEncoding = new boolean[MAP_SIZE];
         for (int i = 0; i < m_characterInEncoding.length; i++)
         {
            m_characterInEncoding[i] = false; // default is to exclude character
         }
         
         InputStream encodingMap = getClass().getResourceAsStream( rscsName );
         if (null == encodingMap)
            throw new IllegalArgumentException("Resource not found: " + rscsName);

         try
         {
            loadMappingDocument( encodingMap );
         
            /* update the Map here (instead of earlier in the method) so that 
             * the cache only contains ready-to-go arrays.  avoids another
             * thread using the array as it is being loaded
             */
            ms_cache.put( encodingName, m_characterInEncoding );
         } catch (SAXException e)
         {
            warn( e );
         } finally
         {
            try
            {
               encodingMap.close();
            } catch (IOException e)
            {
               /* ignore */
            }
         }
      }    
   }
   

   /**
    * Reads the character set encoding document described by fileName and
    * for each character code defined, sets that element of the 
    * m_CharacterInEncoding array to true.  If any errors occur, they will be
    * logged to stderr.
    * 
    * @param encodingFile contains the character set definition document,
    * assumed not <code>null</code>
    * 
    * @throws IOException if there is a problem reading from the stream.
    * @throws SAXException if there is a problem building a XML document
    * from the stream.
    */ 
   private void loadMappingDocument(InputStream encodingFile)
      throws IOException, SAXException
   {
      Document encodingDoc = PSXmlDocumentBuilder.createXmlDocument(
            encodingFile, false);
         
      // read each character in the mapping and populate an array hash
      PSXmlTreeWalker tree = new PSXmlTreeWalker(encodingDoc);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element el = tree.getNextElement(MAP_ELEMENT_NAME, firstFlags);
      for (; null != el; el = tree.getNextElement(MAP_ELEMENT_NAME, nextFlags))
      {
         String characterCodeString = 
               tree.getElementData(CODE_ATTRIBUTE_NAME, false);
         try
         {
            // codes are in hex format
            int characterCode = Integer.parseInt(characterCodeString, 16);
            m_characterInEncoding[characterCode] = true;
         }
         catch (NumberFormatException e)
         {
            warn( e );
         }
      }
   }

 
   /**
    * When an error is received, print its message and stack trace to standard 
    * error
    * 
    * @param e the exception (no message printed if <code>null</code>)
    */
   private static void warn(Exception e)
   {
      if (null != e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
   }
 
 
   /**
    * Determines if a Unicode character code is supported by this character set
    * encoding.  This method is declared final to improve performance.
    * 
    * @param i the Unicode character code, must not be less than zero.
    * 
    * @return <code>true</code> if the character is encoded by this set;
    * <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if <code>i</code> is less than zero.
    */ 
   public final boolean inCharset(int i)
   {
      if (i < 0)
         throw new IllegalArgumentException("Character code is less than zero.");
      
      if (i >= MAP_SIZE)
         return false;
      else
         return m_characterInEncoding[i];
   }


   /**
    * Gets the name of the character encoding defined by this instance.
    * This method is declared final to improve performance.
    * 
    * @return the name of the character encoding scheme, never empty or 
    * <code>null</code>.
    */ 
   public final String getEncodingName()
   {
      return m_encodingName;
   }


   /**
    * Maps a Unicode character code to a boolean value.  <code>true</code>
    * indicates the character code is supported by this encoding.
    * Created in the constructor, and never modified after.
    */ 
   private boolean[] m_characterInEncoding;
   
   /**
    * Name of the character set encoding defined by this instance.  Assigned
    * in the ctor, and never <code>null</code> or empty after that.
    */ 
   private String m_encodingName;
   
   /**
    * Maintains a cache of the character encoding arrays (as they are expensive
    * to construct).  Keyed by the encoding's name (<code>String</code>).
    * Values are <code>boolean[]</code>.  Never <code>null</code>, may be empty.
    */ 
   private static final Map ms_cache = new HashMap();

   /** The size of the encoding arrays; the maximum Unicode code */ 
   private static final int MAP_SIZE = 0xffff;
   
   /** Name of the XML node that represents a character */
   private static final String MAP_ELEMENT_NAME = "a";
   
   /** Name of the XML attribute that contains the Unicode code */
   private static final String CODE_ATTRIBUTE_NAME = "@u";
   
}
