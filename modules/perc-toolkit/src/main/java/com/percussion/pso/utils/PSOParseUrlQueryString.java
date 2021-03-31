/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.utils;

import com.percussion.server.PSRequestParsingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Parses the name=value components of a URL's query string into a Map.
 * Based on (obfuscated) com.percussion.server.content.PSFormContentParser.
 */
public class PSOParseUrlQueryString
{
   /**
    * Add the request parameters defined in the specified parameter string
    * to the specified hash map.
    *
    * @param paramString the URL query string, may not be <code>null</code>,
    * can be empty.
    *
    * @throws IllegalArgumentException if paramString is <code>null</code>
    *
    * @throws PSRequestParsingException if an error occurs parsing the
    * parameters.
    */
   public static Map<String, Object> parseParameters(String paramString)
      throws PSRequestParsingException
   {
      if (paramString == null)
         throw new IllegalArgumentException( "paramString can not be null" );

      String curTok;
      String curValue = "";
      String curName = null;
      String lastTok = STR_URLENCODING_PARAM_TOKEN;

      
      Map<String,Object> params = new HashMap<String,Object>()
      {
         /**
          * Maps the specified value to the specified key.  If a mapping
          * already exists, an <code>ArrayList</code> of all values for the key
          * is mapped.
          *
          * @param key key with which the specified value is to be associated.
          * @param value value to be associated with the specified key.
          * @return previous value associated with specified key, or
          * <code>null</code> if there was no mapping for key.  A
          * <code>null</code> return can also indicate that the HashMap
          * previously associated <code>null</code> with the specified key.
          */
         @SuppressWarnings(value={"unchecked"})
         public Object put(String key, Object value)
         {
            Object oldValue = super.put( key, value );
            if (oldValue != null)
            {
               if (oldValue instanceof ArrayList)
               {
                  ((ArrayList) oldValue).add( value );
                  super.put( key, oldValue );
               }
               else
               {
                  ArrayList l = new ArrayList();
                  l.add( oldValue );
                  l.add( value );
                  super.put( key, l );
               }
            }
            return oldValue;
         }
      };

      /* Replace all ampersand entity refs w/ just the ampersand for XHTML
         compliance (ampersands can't exist directly in an xhmtl file because
         they aren't allowed in xml) */

      paramString = convertEntities( paramString );

      StringTokenizer tok = new StringTokenizer(
         paramString,
         STR_URLENCODING_VALUE_TOKEN + STR_URLENCODING_PARAM_TOKEN,
         true );

      while (tok.hasMoreTokens())
      {
         curTok = tok.nextToken();
         if (curTok.equals( STR_URLENCODING_PARAM_TOKEN ))
         {
            if (curName != null)
            {
               params.put( curName, curValue );
               curName = null;
               curValue = "";
            }
            lastTok = curTok;
         }
         else if (curTok.equals( STR_URLENCODING_VALUE_TOKEN ))
         {
            lastTok = curTok;
         }
         else
         {
            // must be on a name now
            if (lastTok.equals( STR_URLENCODING_PARAM_TOKEN ))
               curName = urlDecode( curTok );
            else
               curValue = urlDecode( curTok );
         }
      }

      if (curName != null)
         params.put( curName, curValue );

      return params;
   }

   private static String urlDecode(String str)
      throws PSRequestParsingException
   {
      StringBuffer newStr = new StringBuffer( str.length() );
      int iSrc = 0;
      int iDst = 0;
      char ch;

      /* initialize it to the full capacity
       * (otherwise it exceptions when calling newStr.setCharAt)
       */
      newStr.setLength( str.length() );

      for (; iSrc < str.length();)
      {
         if ((ch = str.charAt( iSrc++ )) == URLENCODING_SPACE_TOKEN)
            ch = URLENCODING_SPACE_REAL;
         else if (ch == URLENCODING_HEX_TOKEN)
         {
            try
            {
               char ch1 = str.charAt( iSrc++ );
               char ch2 = str.charAt( iSrc++ );

               ch = (char) (Integer.parseInt( "" + ch1 + ch2, 16 ));
            } catch (StringIndexOutOfBoundsException e)
            {
               /* this can only happen when it's poorly formed! */
               Object[] args = {str};
               throw new PSRequestParsingException(
                     FORM_PARSER_BAD_HEX_CHAR, args );
            }
         }

         newStr.setCharAt( iDst++, ch );
      }

      /* now let it know where we actually terminated the value */
      newStr.setLength( iDst );

      return newStr.toString();
   }

   private static final int FORM_PARSER_BAD_HEX_CHAR	= 1307;


   /**
    * Replaces all occurrences of entity refs allowed in URL query strings
    * with their corresponding character. Currently, this is just &amp;
    * <p>In XHTML, the ampersand used to separate params in the query string
    * must be escaped because it is an xml document.
    *
    * @param query The query string from a URL. If <code>null</code> or empty,
    * it is returned unmodified.
    *
    * @return The supplied query with all entity refs replaced with their
    * corresponding character.
    */
   private static String convertEntities(String query)
   {
      if (null == query)
         return null;
      StringBuffer buf = new StringBuffer( query );
      boolean done = false;
      int len = AMPERSAND_ENTITY.length();
      int nextPos = 0;  // position in search string of next entity
      /* This is a little complex because we are searching one string and
       * modifying a copy of that string that is changing as we go. What we
       * do is keep a count of how many chars have been removed from the
       * dynamic string and use that count to correctly position the index
       * calculated from the static string.
       */
      for (int lostChars = 0; !done; lostChars += len - 1)
      {
         nextPos = query.indexOf( AMPERSAND_ENTITY, nextPos );
         if (nextPos < 0)
            done = true;
         else
         {
            int pos = nextPos - lostChars;
            buf.replace( pos, pos + len, STR_URLENCODING_PARAM_TOKEN );
            nextPos += len;
         }
      }
      return buf.toString();
   }


   private static final char URLENCODING_SPACE_TOKEN = '+';
   private static final char URLENCODING_SPACE_REAL = ' ';
   private static final char URLENCODING_HEX_TOKEN = '%';
  
   private static final String STR_URLENCODING_PARAM_TOKEN = "&";
   private static final String STR_URLENCODING_VALUE_TOKEN = "=";
   /** This is the entity reference for an ampersand character, used by xml. */
   private static final String AMPERSAND_ENTITY = "&amp;";

}
