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
package com.percussion.utils.string;

import com.percussion.utils.types.PSPair;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Methods that handle the selective removal or inclusion of processing
 * instructions from body and other fields. This is used to deal with
 * JSP/ASP/PHP embedded tags. This is done in three sections:
 * <ul>
 * <li>The first section replaces all <i>active</i> tags with a comment of the
 * form &lt;!--
 * 
 * @psx-activetag-NNNNN --!&gt; where <code>NNNNN</code> is a sequential
 *                      number allocated for each active tag.
 *                      <li>In the second step, run after the content has been
 *                      process through html tidy, the comments are replaced
 *                      with processing instructions.
 *                      <li>The third step is run when the content is being
 *                      sent back to ephox. In the third step the processing
 *                      instructions are stripped.
 *                      </ul>
 * 
 * @author dougrand
 * 
 */
public class PSXmlPIUtils
{
   public enum Action {
      QUOTE, PHP
   }

   private static final String PI_START = "<?psx-activetag ";

   private static final String PI_END = "?>";

   private static final String COMMENT_START = "<!-- ";

   private static final String COMMENT_END = " -->";

   private static final String COMMENT_TAG = "@psx_activetag_";

   // Start, action and end sequence vectors must be the same length
   private static final String[] START_SEQ =
   {"<%", "<?php"};

   private static final String[] END_SEQ =
   {"%>", "?>"};

   private static final Action[] ACTION =
   {Action.QUOTE, Action.PHP};

   private static final boolean[] START_FOLLOWED_BY_WS =
   {false, true};

   /**
    * If any of the strings are found, then escape the content between the start
    * and matching end in a processing instruction.
    * 
    * @param input input data string, never <code>null</code> or empty
    * @return a pair of the map holding the active content and a string with the
    *         appropriate substitutions
    */
   public static PSPair<Map<Integer, PSPair<Action, String>>, String> encodeTags(
         String input)
   {
      if (StringUtils.isBlank(input))
      {
         throw new IllegalArgumentException("input may not be null or empty");
      }

      StringBuilder rval = new StringBuilder(input.length());
      Action action;
      Map<Integer, PSPair<Action, String>> keys = new HashMap<Integer, PSPair<Action, String>>();
      int key = 0;

      for (int i = 0; i < input.length(); i++)
      {
         boolean match = false;
         for (int j = 0; j < START_SEQ.length; j++)
         {
            if (input.startsWith(START_SEQ[j], i))
            {
               if (START_FOLLOWED_BY_WS[j])
               {
                  char next = input.charAt(i + START_SEQ[j].length());
                  if (!Character.isWhitespace(next))
                  {
                     // Find next sequence that may match
                     continue;
                  }
                  else
                  {
                     match = true;
                  }
               }
               else
               {
                  match = true;
               }
               int l = input.indexOf(END_SEQ[j], i);
               if (l < 0)
               {
                  throw new IllegalStateException(
                        "Delimeters did not match, start delim: "
                              + START_SEQ[j]);
               }
               int e = l + END_SEQ[j].length();

               String value = input.substring(i, e);
               action = ACTION[j];
               keys.put(key, new PSPair<Action, String>(action, value));

               rval.append(COMMENT_START);
               rval.append(COMMENT_TAG);
               rval.append(key);
               rval.append(COMMENT_END);
               i = e - 1;
               key++;
               break; // End looping for sequence match
            }
         }
         if (!match)
         {
            if (input.startsWith(COMMENT_START, i))
            {
               // Find end comment
               int l = input.indexOf(COMMENT_END, i);
               if (l < 0)
               {
                  throw new IllegalStateException("Missing end comment");
               }
               int e = l + 3;
               rval.append(input.substring(i, e));
               i = e - 1;
            }
            else
            {
               rval.append(input.charAt(i));
            }
         }
      }

      return new PSPair<Map<Integer, PSPair<Action, String>>, String>(keys,
            rval.toString());
   }

   /**
    * Walk the document node structure and examine comments. If the content of
    * the comments matches the active tag sequence, then replace the comment
    * node with a processing instruction node with the corresponding active tag
    * information
    * 
    * @param doc the document, never <code>null</code>
    * @param keys the keys, never <code>null</code>
    */
   public static void substitutePIs(Document doc,
         Map<Integer, PSPair<Action, String>> keys)
   {
      if (doc == null)
      {
         throw new IllegalArgumentException("doc may not be null");
      }
      if (keys == null)
      {
         throw new IllegalArgumentException("keys may not be null");
      }
      doSubstitutePIs(doc, doc.getDocumentElement(), keys);
   }

   /**
    * Walk the nodes, recursing
    * 
    * @param doc the document, assumed not <code>null</code>
    * @param node current node, assumed not <code>null</code>
    * @param keys keys, assumed not <code>null</code>
    */
   private static void doSubstitutePIs(Document doc, Node node,
         Map<Integer, PSPair<Action, String>> keys)
   {
      NodeList l = node.getChildNodes();
      int len = l.getLength();
      for (int i = 0; i < len; i++)
      {
         Node n = l.item(i);

         if (n.getNodeType() == Node.COMMENT_NODE)
         {
            String text = n.getTextContent().trim();
            if (text.startsWith(COMMENT_TAG))
            {
               Node pi = null;
               int key = Integer.parseInt(text.substring(COMMENT_TAG.length()));
               PSPair<Action, String> active = keys.get(key);
               if (active.getFirst().equals(Action.QUOTE))
               {
                  pi = doc.createProcessingInstruction("psx-activetag", active
                        .getSecond());

               }
               else if (active.getFirst().equals(Action.PHP))
               {
                  String code = stripPI(active.getSecond());
                  pi = doc.createProcessingInstruction("php", code);
               }
               else
               {
                  throw new IllegalStateException("Unknown action "
                        + active.getFirst());
               }
               n.getParentNode().replaceChild(pi, n);
            }
         }
         else
         {
            doSubstitutePIs(doc, n, keys);
         }
      }
   }

   /**
    * Take the processing instruction source and return just the value
    * portion
    * @param pisource the pi source, never <code>null</code> or empty
    * @return the value portion
    */
   private static String stripPI(String pisource)
   {
      if (StringUtils.isBlank(pisource))
      {
         throw new IllegalArgumentException("pisource may not be null or empty");
      }
      
      String parts[] = pisource.split("\\s",2);
      if (parts.length < 2)
      {
         throw new IllegalStateException("Did not find separator in PI");
      }
      String value = parts[1];
      // Value should end in ?>
      value = value.trim();
      if (!value.endsWith("?>"))
      {
         throw new IllegalStateException("Invalid PI does not end in ?> :" + pisource);
      }
      return value.substring(0, value.length() - 2);
   }

   /**
    * Remove any pi that is followed by a value in the start sequences, and
    * which has the corresponding value in the end sequences at the other end.
    * So a string that consists of a start pi, followed by <% JSP code %> will
    * have the pi stripped if the <% and %> are in the sequences.
    * 
    * @param input input data string, never <code>null</code> or empty
    * @return a filtered string, where appropriate CData are removed
    */
   public static String removePI(String input)
   {
      if (StringUtils.isBlank(input))
      {
         throw new IllegalArgumentException("input may not be null or empty");
      }

      StringBuilder rval = new StringBuilder(input.length());

      for (int i = 0; i < input.length(); i++)
      {
         if (input.startsWith(PI_START, i))
         {
            int e = input.indexOf(PI_END, i);
            if (e < 0)
            {
               throw new IllegalStateException("Found no matching cdata end");
            }
            int s = i + PI_START.length();
            String content = input.substring(s, e).trim();
            boolean match = false;
            for (int j = 0; j < START_SEQ.length; j++)
            {
               if (content.startsWith(START_SEQ[j])
                     && content.endsWith(END_SEQ[j]))
               {
                  match = true;
                  break;
               }
            }
            if (match)
            {
               rval.append(content);
               i = e + PI_END.length() - 1;
            }
            else
            {
               e = e + PI_END.length();
               rval.append(input, i, e);
               i = e - 1;
            }
         }
         else
         {
            rval.append(input.charAt(i));
         }
      }

      return rval.toString();
   }
}
