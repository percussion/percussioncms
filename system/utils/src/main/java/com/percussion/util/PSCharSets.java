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
package com.percussion.util;

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class PSCharSets
{
   // CS_MAP_FILE must be defined before use in a static initializer (below)
   /**
    * Name of the file in which character set maps are stored.
    */
    public static final String CS_MAP_FILE = "csmaps.xml";   

   /**
    * Gets the IANA name for the encoding alias. If no IANA name is available,
    * gets an equivalent encoding name that is supported by IBM's XML parser.
    * If no standard version of this name is available, and no equivalent
    * encoding name is available, the parameter itself is returned.
    *
    * @param encodingAlias The (possibly non-standard) encoding name.
    */
   public static String getStdName(String encodingAlias)
   {
      String ret = encodingAlias;
      PSEncoding enc = (PSEncoding)ms_encodings.get(encodingAlias.toUpperCase());
      if (enc != null)
         ret = enc.getStdName();

// DBG>
//       System.out.println(encodingAlias + "--->" + ret);
// <DBG

      return ret;
   }

   /**
    * Gets the Java name for the encoding alias. If no Java name is available,
    * and no equivalent encoding name is available, the parameter itself is
    * returned.
    *
    * @param encodingAlias The (possibly non-standard) encoding name.
    */
   public static String getJavaName(String encodingAlias)
   {
      String ret = encodingAlias;
      PSEncoding enc = (PSEncoding)ms_encodings.get(encodingAlias.toUpperCase());
      if (enc != null)
         ret = enc.getJavaName();

// DBG>
//       System.out.println(encodingAlias + "--->" + ret);
// <DBG

      return ret;
   }

   /**
    * Gets the IANA name for the default Java encoding for this locale.
    *
    * The return value is equivalent to getStdName(System.getProperty("file.encoding")).
    */
   public static String getLocalStdName()
   {
// DBG>
//       System.out.println("localStdName()=" + ms_localStdName);
// <DBG
      return ms_localStdName;
   }

   /**
    * Get the default Java encoding for this locale.
    */
   public static String getLocalJavaName()
   {
// DBG>
//       System.out.println("localJavaName()=" + ms_localJavaName);
// <DBG
      return ms_localJavaName;
   }

   /**
    * Get the standard name of the preferred encoding for
    * Rhythmyx. This encoding is guaranteed to be acceptable for
    * XML parsers and HTTP servers, and should be some kind of Unicode
    * so that we can be sure all characters are representable.
    * <P>
    * The return value is the standard name for rxJavaEnc()
   */
   public static String rxStdEnc()
   {
      return "UTF-8";
   }

   /**
    * Get the standard name of the preferred encoding for
    * Rhythmyx. This encoding is guaranteed to be acceptable for
    * Sun's Java methods which take a character encoding, and should be
    * some kind of Unicode so that we can be sure all characters are
    * representable.
    * <P>
    * The return value is the Java name for rxStdEnc()
    */
   public static String rxJavaEnc()
   {
      return "UTF8";
   }

   /**
    * Initializing mappings from the given properties file. This should happen
    * once, statically.
    */
   private static void initializeMappings(String filename)
      throws IOException, SAXException
   {
      InputStream in = PSCharSets.class.getResourceAsStream(filename);

      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
         fromXml(doc.getDocumentElement());
      }
      finally
      {
         try { in.close(); } catch (IOException e) { /* ignore */ }
      }

      ms_localJavaName = System.getProperty("file.encoding");
      ms_localStdName = getStdName(ms_localJavaName);
   }


   private static void fromXml(Element el)
   {
      PSXmlTreeWalker   walker = new PSXmlTreeWalker(el);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
         | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for (Element curNode = walker.getNextElement("PSEncoding", firstFlags);
         curNode != null;
         curNode = walker.getNextElement("PSEncoding", nextFlags))
      {
         addEncoding(new PSEncoding(curNode));
      }
   }

   private static void addEncoding(PSEncoding enc)
   {
      // aliased as itself
      ms_encodings.put(enc.getStdName().toUpperCase(), enc);

      // aliased as its Java name
      ms_encodings.put(enc.getJavaName().toUpperCase(), enc);

      // add this encoding under all its aliases
      for (Iterator i = enc.aliases(); i.hasNext(); )
      {
         String alias = i.next().toString();
         ms_encodings.put(alias.toUpperCase(), enc);
      }
   }

   private static String ms_localJavaName;
   private static String ms_localStdName;
   private static Map<String, PSEncoding> ms_encodings = new HashMap<String, PSEncoding>();

   static
   {
      try
      {
         initializeMappings(CS_MAP_FILE);
      }
      catch (SAXException e)
      {
         System.err.println("Error: Could not initialize charset map: "
            + e.toString());
      }
      catch (FileNotFoundException e)
      {
         // we ignore this -- the file may not exist,  in which case all charsets
         // will map back to themselves, possibly resulting in runtime exceptions
         System.err.println("Character encoding map file not found. Try to load it through the object store handler later.");
      }
      catch (IOException e)
      {
         System.err.println("Error: Could not initialize charset map: "
            + e.toString());
      }
      catch (AccessControlException e)
      {
         // we ignore this, since we load it later when the connection is
         // established through the object store handler
         System.out.println("No access to character encoding map file. Try to load it through the object store handler later.");
      }
   }

   /**
    * Represents an encoding, its standard name, its java name,
    * and all aliases.
    */
   private static class PSEncoding
   {
      /** Construct this encoding from an XML element */
      public PSEncoding(Element el)
      {
         m_aliases = new ArrayList<String>();
         fromXml(el);
      }

      public void fromXml(Element el)
      {
         m_stdName = el.getAttribute("stdName");
         m_javaName = el.getAttribute("javaName");

         m_description = el.getAttribute("desc");
         if (m_description == null)
            m_description = "";

         // loop over all Alias definitions
         PSXmlTreeWalker   walker = new PSXmlTreeWalker(el);
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS
            | PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         for (Element curNode = walker.getNextElement("Alias", firstFlags);
            curNode != null;
            curNode = walker.getNextElement("Alias", nextFlags))
         {
            String alias = curNode.getAttribute("name");
            if (alias != null && alias.length() != 0)
               addAlias(alias.toUpperCase());
         }
      }

      public void addAlias(String alias)
      {
         m_aliases.add(alias);
      }

      public Iterator aliases()
      {
         return m_aliases.iterator();
      }

      public String getJavaName()
      {
         return m_javaName;
      }

      public String getStdName()
      {
         return m_stdName;
      }

      public String getDescription()
      {
         return m_description;
      }

      public String toString()
      {
         return m_stdName;
      }

      private List<String> m_aliases;
      private String m_javaName;
      private String m_stdName;
      private String m_description;
   }

   public static final int CS_ASCII     = 0x01;
   public static final int CS_UTF8      = 0x02;
   public static final int CS_ISO8859_1 = 0x04;

   /**
    * Guesses whether the encoding is UTF-8, ISO-8859-1, or US-ASCII.
    * <P>
    * <B>UTF-8</B>
    * <P>
    * UCS characters U+0000 to U+007F (ASCII) are encoded simply as
    * bytes 0x00 to 0x7F (ASCII compatibility). This means that files
    * and strings which contain only 7-bit ASCII characters have the
    * same encoding under both ASCII and UTF-8.
    * <P>
    * All UCS characters >U+007F are encoded as a sequence of several
    * bytes, each of which has the most significant bit set. Therefore,
    * no ASCII byte (0x00-0x7F) can appear as part of any other character.
    * <P>
    * The first byte of a multibyte sequence that represents a non-ASCII
    * character is always in the range 0xC0 to 0xFD and it indicates how
    * many bytes follow for this character. All further bytes in a multibyte
    * sequence are in the range 0x80 to 0xBF. This allows easy
    * resynchronization and makes the encoding stateless and robust
    * against missing bytes. 
    * <P>
    * <B>ISO-8859-1</B>
    * <P>
    * ISO 8859-X character sets use the characters 0xa0 through 0xff to
    * represent national characters, while the characters in the
    * 0x20-0x7f range are those used in the US-ASCII (ISO 646) character
    * set.  Thus, ASCII text is a proper subset of all ISO 8859-X
    * character sets.
    * <P>
    * The characters 0x80 through 0x9f are earmarked as extended control
    * chracters, and are not used for encoding characters.  These characters
    * are not currently used to specify anything.
    * <P>
    * <B>US-ASCII</B> If all characters are below 127, then we could really
    * return either UTF-8 or ISO-8859-1 because they are both identical in
    * this range. Instead, we just return null (no guess).
    */
   public static int guessEncoding(byte[] bytes)
   {
      return guessEncoding(bytes, 0, bytes.length);
   }

   public static int guessEncoding(byte[] bytes, int off, int len)
   {
      int cs = CS_ASCII | CS_UTF8 | CS_ISO8859_1;
      boolean sawHigh = false;

      for (int i = off; i < len; i++)
      {
         byte b = bytes[i];

         // would this be a control char in ISO-8859
         if (isCtrlChar_ISO8859(b))
         {
            cs &= ~CS_ISO8859_1;
         }

         if (b >= (byte)0xc0 && b <= (byte)0xfd)
         {
            sawHigh = true;
            // if this is UTF-8, then this byte must indicate
            // the number of following bytes which are part
            // of this multi-byte character. All of the
            // following bytes must have the high bit on.
            int numFollow = numFollowingBytes_UTF8(b);

            // is the array too short to have that number of
            // bytes following this one ... ?
            if (numFollow > (len - i) || numFollow == 0)
            {
               cs &= ~CS_UTF8;
            }

            // make sure all of these have the high bit on
            for (int j = i+1; j <= (i+numFollow); j++)
            {
               byte bb = bytes[j];
               if (bb >= 0)
               {
                  // High bit of follow char was off.
                  cs &= ~CS_UTF8;
               }

               if (isCtrlChar_ISO8859(bb))
               {
                  cs &= ~CS_ISO8859_1;
               }
            }

            // if we got this far, it means that the string
            // is still valid in both UTF-8 and ISO-8859-1
            i += numFollow;
         }
      }

      if (sawHigh)
         cs &= ~CS_ASCII;

      return cs;
   }

   private static boolean isCtrlChar_ISO8859(byte b)
   {
      return (b >= (byte)0x80 && b <= (byte)0x9f);
   }

   // get the number of consecutive high order 1 bits
   // before the first 0 bit
   private static int numFollowingBytes_UTF8(byte b)
   {
      int mask = 0x80;
      int num = 0;
      for (num = 0; num < 8; num++)
      {
         if ( ((mask >> num) & b) == 0)
            break;
      }

      return num-1;
   }
}
