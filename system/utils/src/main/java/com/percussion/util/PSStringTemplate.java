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
package com.percussion.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author DougRand
 *
 * <P>Allows the caller to use string templates with substitution variables. 
 * Variables are expressed in the string by an introduction sequence, which
 * defaults to "{", a series of alpha characters and an end sequence,
 * which defaults to "}". The start and end sequences should be 
 * punctuation characters to avoid problems.
 * 
 * <P>If the beginning character of the start sequence needs to be used in the
 * template as an output character, use the quote character, which defaults to
 * backslash, to escape it. 
 * 
 * <P>For future expansion, non-alpha characters are reserved. Also note that
 * whitespace characters should not be used in the start and end sequences.
 * 
 * <P>The template "The {person} goes to the {place}" will expand
 * to "The girl goes to the store" when the <code>expand</code>
 * method is called with a map that contains the pairs person/girl and
 * place/store. The map values can be of any type, the <code>toString</code>
 * method is called to covert them to a string.
 * 
 * <P>This class is invariant, it has no modifiable internal state after
 * construction.
 */
public class PSStringTemplate
{
   /*
    * You have got to be kidding me. Much better string template processing
    * is right in commons lang 
    * http://commons.apache.org/lang/api-2.5/src-html/org/apache/commons/lang/text/StrSubstitutor.html#line.92
    * -Adam Gent
    */
   /**
    * Used for exceptions in template handling
    */
   public static class PSStringTemplateException extends Exception
   {
      /**
       * Ctor for a template exception with a message.
       * @param message a message to associate with this exception,
       * may be <code>null</code> or empty.
       */
      public PSStringTemplateException(String message)
      {
         super(message);
      }
   }

   /**
    * Used to provide a custom dictionary lookup other than a map, which is
    * the default.
    *
    * @author paulhoward
    */
   public interface IPSTemplateDictionary
   {
      /**
       * Looks up key in this objects dictionary and returns the value found.
       * 
       * @param key Never <code>null</code> or empty. Case-sensitivity is
       * dependent upon the implementation.
       * 
       * @return Never <code>null</code>, may be empty if key is not in this
       * dictionary.
       */
      public String lookup(String key);
   }

   /**
    * This class is used as the default resolver for variable substitutions. 
    *
    * @author paulhoward
    */
   public static class PSMapDictionary implements IPSTemplateDictionary
   {
      /**
       * Basic ctor.
       * 
       * @param dict If <code>null</code>, any empty map is used. Keys must
       * be of type <code>String</code> or all lookups will fail. A <code>
       * toString</code> is performed on the value before use.
       */
      public PSMapDictionary(Map dict)
      {
         if (null == dict)
         {
            dict = new HashMap();
         }
         m_dictionary = dict;
      }
      /**
       * Looks for key in the map supplied in the ctor and returns the value
       * associated with that entry.
       * <p>See {@link IPSTemplateDictionary#lookup(String) inteface} for
       * more details.
       */      
      public String lookup(String key)
      {
         if (null == key)
         {
            throw new IllegalArgumentException("key cannot be null or empty");
         }
         Object o = m_dictionary.get(key);
         String val = null == o ? "" : o.toString();
         return val;
      }
      /**
       * Set in ctor, then never <code>null</code>, may be empty.
       */
      private Map m_dictionary;
   }

   /**
    * Construct a new template object. 
    * Calls {@link PSStringTemplate#PSStringTemplate(String, String, String, char)
    * PSStringTemplate(template, null, null, '\\'}}.
    * @param template A string containing variable references to be expanded,
    * may be <code>null</code> or empty
    */
   public PSStringTemplate(String template)
   {
      this(template, null, null, '\\');
   }

   /**
    * Construct a new template object.
    * Calls {@link PSStringTemplate#PSStringTemplate(String, String, 
    * String, char) PSStringTemplate(template, start, end, '\\'}}.
    * @param template A string containing variable references to be expanded,
    * may be <code>null</code> or empty
    * @param start A string that introduces variable references, if <code>null</code>
    * the default value is used
    * @param end A string that ends variable references, if <code>null</code>
    * the default value is used
    */
   public PSStringTemplate(String template, String start, String end)
   {
      this(template, start, end, '\\');
   }
   
   /**
    * Construct a new template object
    * @param template A string containing variable references to be expanded,
    * may be <code>null</code> or empty
    * @param start A string that introduces variable references, if 
    * <code>null</code> the default value is used
    * @param end A string that ends variable references, if <code>null</code>
    * the default value is used.
    * @param quote a character that escapes the next character to pass it through
    * literally
    */
   public PSStringTemplate(String template, String start, String end, char quote)
   {
      if (start == null || start.trim().length() == 0)
      {
         start = "{";
      }
      if (end == null || end.trim().length() == 0)
      {
         end = "}";
      }
      m_startSequence = start;
      m_endSequence = end;
      m_quoteCharacter = quote;
      m_template = template;
   }

   /**
    * Convenience method that calls {@link #expand(IPSTemplateDictionary)
    * expand(new MapDictionary(dict)}.
    * 
    * @param dict The keys in the passed <code>Map</code> are always of type
    * {@link String} and are case sensitive. So "foo" and "Foo" are different
    * keys and are specified differently in the template string. The values
    * are of any class. They are generally {@link String} objects, but 
    * any class that implements a useful and predictable 
    * {@link Object#toString()} method will work.
    */
   public String expand(Map dict) throws PSStringTemplateException
   {
      if (dict == null)
      {
         throw new IllegalArgumentException("dict must never be null");
      }
      return expand(new PSMapDictionary(dict));
   }

   /**
    * Expand the template given the passed variable references. Note that 
    * embedded variables in the expansion are not themselves expanded. 
    * 
    * @param dict A mapping of variable names to values, must 
    * never be <code>null</code>.
    * @return a string containing the original template with variables replaced
    * by their references. If a variable does not exist then it is replaced by
    * the empty string. Never returns <code>null</code>.  If the template 
    * supplied during construction was <code>null</code> or empty, an empty
    * string is returned.
    * 
    * @throws PSStringTemplateException if an error is encountered while
    * expanding the template. 
    */
   public String expand(IPSTemplateDictionary dict)
      throws PSStringTemplateException
   {
      if (null == dict)
      {
         throw new IllegalArgumentException("dictionary cannot be null");
      }
      return expand(dict, 0);
   }
   
   /**
    * Indicate if a start sequence with no matching end sequence should
    * be ignored or if an exception should be thrown, <code>false</code> by
    * default if never set.
    * 
    * @param ignore <code>true</code> to ignore an unmatched sequence, 
    * <code>false</code> to throw an {@link PSStringTemplateException} during
    * calls to {@link #expand(IPSTemplateDictionary)}.
    */
   public void setIgnoreUnmatchedSequence(boolean ignore)
   {
      m_ignoreUnmatchedSequence = ignore;
   }

   /**
    * Identical to {@link #expand(IPSTemplateDictionary)} with a parameter 
    * that allows the start position to be specified.
    *  
    * @param dict A mapping of variable names to values, assumed not <code>
    * null</code>.
    * @param i The start index, which must be &gt;= zero.
    * 
    * @return a string containing the original template with variables replaced
    * by their references. If a variable does not exist then it is replaced by
    * the empty string. Never returns <code>null</code>.
    * 
    * @throws PSStringTemplateException if an error is encountered while
    * expanding the template. 
    */
   private String expand(IPSTemplateDictionary dict, int i) 
      throws PSStringTemplateException
   {
      if (i < 0)
      {
         throw new IllegalArgumentException("Start index must be non-negative");
      }
      
      StringBuffer rval = new StringBuffer();
      
      if (StringUtils.isBlank(m_template))
         return rval.toString();
      
      int pos = i;
      int length = m_template.length();

      while (pos < length)
      {
         char ch = m_template.charAt(pos);
         if (ch == m_quoteCharacter)
         {
            // Take the next character as literal
            pos++;
            if (pos >= length)
            {
               throw new PSStringTemplateException(
                  "Found quote character at end of template");
            }
            rval.append(m_template.charAt(pos));
            pos++; // Next char
         }
         else if (m_template.startsWith(m_startSequence, pos))
         {
            pos = handleVariable(dict, pos, m_template, rval);
         }
         else
         {
            rval.append(ch);
            pos++;
         }
      }

      return rval.toString();
   }

   /**
    * Expand variable reference. 
    * 
    * @param dict Dictionary that contains the variable references, 
    * assumed not <code>null</code>.
    * @param pos Initial position of first character
    * @param template template string being expanded (allows future 
    * recursive handling)
    * @param rval Output string
    * @return new character position at the next character after the variable
    * reference
    * @throws PSStringTemplateException If a start sequence is found with no 
    * matching end sequence, and we are not ignoring unmatched sequences (see 
    * {@link #setIgnoreUnmatchedSequence(boolean)}. 
    */
   private int handleVariable(
      IPSTemplateDictionary dict,
      int pos,
      String template,
      StringBuffer rval) throws PSStringTemplateException
   {
      pos += m_startSequence.length();
      // Search for end
      int end = template.indexOf(m_endSequence, pos);
      if (end < 0)
      {
         if (!m_ignoreUnmatchedSequence)
         {
            throw new PSStringTemplateException(
               "No end sequence for variable reference starting at: "
                  + template.substring(pos));
         }
         
         // skip to the end so we'll be done
         return template.length();
      }
      String var = template.substring(pos, end);
      if (var.startsWith("escape "))
      {
         rval.append("{");
         rval.append(var);
         rval.append("}");
      } 
      else
      {
         Object val = dict.lookup(var);
         if (val != null)
         {
            rval.append(val.toString());
         }
      }
      return end + m_endSequence.length();
   }

   /**
    * The template to expand, will never be <code>null</code>
    * or empty after construction.
    */
   private String m_template = null;
   /**
    * This character causes the next character in the template to be 
    * taken as literal input.
    */
   private char m_quoteCharacter = '\\';
   /**
    * If this sequence is discovered in the template, it is used as the 
    * start delimiter for a variable reference.
    */
   private String m_startSequence = "{";
   /**
    * This sequence is used as the end sequence when scanning for a  variable
    * reference. If this sequence uses multiple characters, they must all 
    * match.
    */
   private String m_endSequence = "}";
   
   /**
    * Flag to indicate if a start sequence with no matching end sequence should
    * be ignored or if an exception should be thrown, <code>false</code> by
    * default.
    */
   private boolean m_ignoreUnmatchedSequence = false;
}
