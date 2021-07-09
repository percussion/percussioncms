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
package com.percussion.content;


import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * PSHtmlParser is a DOM-compliant HTML parser that yields
 * Document or Element trees from HTML files. It tries to deal
 * with the fact that HTML is not always well-formed from an
 * XML standpoint.
 * <P>
 * CURRENT LIMITATIONS
 * <UL>
 * <LI>Does not deal with entity references.</LI>
 * <LI>Does not deal with &lt;!DOCTYPE ... &gt;</LI>
 * <LI>Adds whitespace where it shouldn't in some situations.</LI>
 * <LI>Whitespace definition is less restrictive than HTML's</LI>
 * </UL>
 * <P>
 * <B>GUARANTEES:</B><BR>
 * This parser reads character by character and will always return
 * so that every char read is added to the literal buffer, and
 * you can use the combination of the literal buffer plus whatever's
 * left in the reader to reconstruct the exact sequence of characters
 * that came through the parser. Note that this guarantee may be
 * invalidated by the use of buffered readers or streams. If you want
 * the parser's buffer to bear this exact relation to the unread stream,
 * you must not use any buffering on input streams or readers.
 */
public class PSHtmlParser
{
   public PSHtmlParser()
   {
   }

   public void parse(Reader rdr)
      throws IOException
   {
      m_rdr = rdr;
      m_lineNum = 1;
      lex();
   }

   public void addStopElement(HTMLElement el, boolean caseInsensiteValues)
   {
      m_caseInsensitiveSearch = caseInsensiteValues;
      if (m_stopAt == null)
         m_stopAt = new ArrayList();
      m_stopAt.add(el);
   }

   public HTMLElement getStoppedElement()
   {
      return m_foundEl;
   }

   // gets the document fragment containing everything up to and
   // including the stopped-on element (see getStoppedElement)
   public HTMLDocumentFragment getDocFragment()
   {
      // TODO: do this as soon as parsing is complete
      HTMLDocumentFragment frag = new HTMLDocumentFragment();
      for (int i = 0; i < m_elStack.size(); i++)
      {
         HTMLNode node = (HTMLNode)m_elStack.get(i);
         // System.out.println("Adding " + node + " to doc frag.");
          frag.appendChild(node);
      }

      return frag;
   }

   public void dumpDocument()
   {
      for (int i = 0; i < m_elStack.size(); i++)
      {
         HTMLElement el = (HTMLElement)m_elStack.get(i);
         dumpElement(el);
      }
   }

   public void maintainLiteralBuffer()
   {
      m_literalBuf = new StringBuffer();
   }
   
   public String getLiteralBuffer()
   {
      return m_literalBuf.toString();
   }

   private void lex()
      throws HTMLException
   {
      try
      {
         int c = m_rdr.read();
         if (m_literalBuf != null)
            m_literalBuf.append((char)c);
         while (-1 != c && !m_stop)
         {
            //System.out.print((char)c);
            switch (c)
            {
            case '<':
               handleLT();
               break;
            case '>':
               handleGT();
               break;
            case '=':
               handleEQ();
               break;
            case '!':
               handleBang();
               break;
            case '-':
               handleDash();
               break;
            case '"':
               handleDQuote();
               break;
            case '\'':
               handleSQuote();
               break;
            case '[':
               handleOBracket();
               break;
            case ']':
               handleCBracket();
               break;
            default:
               handleChar((char)c);
            }

            if (!m_stop)
            {
               c = m_rdr.read();
               if (m_literalBuf != null)
                  m_literalBuf.append((char)c);
            }
         }
      }
      /*  Class  no longer exists
      catch (sun.io.MalformedInputException e)
      {
         StringBuffer msg = new StringBuffer(100);
         msg.append("Malformed character code. Perhaps the document specifies the wrong charset.");
         String superMsg = e.getMessage();
         if (superMsg != null)
         {
            msg.append("(");
            msg.append(superMsg);
            msg.append(")");
         }

         htmlException(msg.toString());
      }
      */
      catch (IOException e)
      {
         htmlException(
            "IO error while parsing: " + e.toString());
      }
   }

   /**
    * Handle a < character.
    */
   private void handleLT()
   {
      switch (m_lexState)
      {
      case ST_ONTEXT:
         // allow [TEXT]<TAG>
         //             ^
         m_lexState = ST_SAWOPENTAG;
         finishText();
         break;
      case ST_ONATVAL:
         // allow <TAG ATTR=foo<bar>
         //                    ^
         // fall through
      case ST_ONQATVAL:
         // this is not technically allowed (it should be
         // escaped as &lt; even in attribute values), but
         // this is how we deal with it
         // allow <TAG ATTR="aaaaaa<aaaa">
         //                        ^
         accumulate('<');
         break;
      case ST_SAWCLOSETAG:
         // allow <TAG>[WS]<TAG>
         //                ^
         m_lexState = ST_SAWOPENTAG;
         break;
      case ST_SAWCDATAEND_ONE:
      case ST_SAWCDATAEND_TWO:
         handleChar('<'); // m_lexState reset will be handled by handleChar()
         break;
      case ST_ONPROCINT:
         accumulate('<');
         break;
      case ST_ONCOMMENT:
      case ST_ONCDATA:
         accumulate('<');
         break;
      default:
         illegalToken('<');
         break;
      }
   }

   /**
    * Handle a > character.
    */
   private void handleGT()
   {
      switch (m_lexState)
      {
      case ST_SAWTAGNAME:
         // allow <TAG >
         //            ^
         // fall through
      case ST_ONTAGNAME:
         // allow <TAG>
         //           ^
         m_lexState = ST_SAWCLOSETAG;
         finishTagName();
         finishTag();
         break;
      case ST_ONATNAME:
         // allow <HR NOSHADE>
         //                  ^
         m_lexState = ST_SAWCLOSETAG;
         
         // HTML spec says this form of an attribute (without an
         // equals sign and value) should have the attribute name
         // as the value.
         m_attrVal.setLength(0);
         m_attrVal.append(m_attrName.toString());

         finishAtName();
         finishAtVal(); // empty value
         finishTag();
         break;
      case ST_ONCDATA:
      case ST_ONQATVAL:
         // allow <TAG ATTR="aaaaa>aaaa">
         //                       ^
         accumulate('>'); // allowed
         break;
      case ST_ONPROCINT:
         m_lexState = ST_SAWCLOSETAG;
         // finishProcInt();
         break;
      case ST_SAWATVAL_SEP:
         // allow <TAG ATTR=>
         // fall through
      case ST_ONATVAL:
         // allow <TAG ATTR=FOO>    and    <TAG ATTR="FOO">
         //                    ^                          ^
         m_lexState = ST_SAWCLOSETAG;
         finishAtVal();
         finishTag();
         break;
      case ST_SAWCOMMENTENDDASH_TWO:
         m_lexState = ST_SAWCLOSETAG;
         finishComment();
         break;
      case ST_SAWATVAL:
         // allow <TAG ATTR=FOO[ws]>    and    <TAG ATTR="FOO"[ws]>
         //                        ^                              ^
         m_lexState = ST_SAWCLOSETAG; // allowed
         finishTag();
         break;
      case ST_ONCOMMENT:
         // we allow these guys in comments (only --> ends a comment)
         accumulate('>');
         break;
      case ST_SAWPROCINTBANG:
         // allow <!>
         // TODO: real handling of p.i.'s
         m_lexState = ST_SAWCLOSETAG;
         break;
      case ST_SAWCDATAEND_ONE:
         // allow ]>
         //        ^
         handleChar('>'); // m_lexState reset will be handled by handleChar()
         break;
      case ST_SAWCDATAEND_TWO:
         // finishes ']]>' of CDATA statement
         m_lexState = ST_SAWCLOSETAG;
         finishCDATA();
         break;
      default:
         illegalToken('>');
         break;
      }
   }
   
   /**
    * Handle an = character.
    */
   private void handleEQ()
   {
      switch (m_lexState)
      {
      case ST_ONTEXT:
         // allow aaaaaa=aaaaaaaa
         //             ^
         accumulate('=');
         break;
      case ST_ONATNAME:
         // allow <TAG ATTR=aaaa>
         //                ^
         m_lexState = ST_SAWATVAL_SEP;
         finishAtName();
         break;
      case ST_ONATVAL:
         // allow <TAG ATTR=foo=bar baz=bar>
         //                    ^
         // fall through
      case ST_ONQATVAL:
         // allow <TAG ATTR="aaaaa=aaaa">
         //                       ^
         accumulate('=');
         break;
      case ST_SAWATTRNAME:
         // allow <TAG ATTR[ws]=aaaa>
         //                    ^
         m_lexState = ST_SAWATVAL_SEP;
         break;
      case ST_SAWCLOSETAG:
         // allow <TAG>=
         //            ^
         m_lexState = ST_ONTEXT;
         accumulate('=');
         break;
      case ST_SAWCDATAEND_ONE:
      case ST_SAWCDATAEND_TWO:
         // allows ]]=
         handleChar('='); // m_lexState reset will be handled by handleChar()
         break;
      case ST_ONCDATA:
      case ST_ONCOMMENT:
         accumulate('=');
         break;
      default:
         illegalToken('=');
      }
   }

   /**
    * Handle a ! character
    */
   private void handleBang()
   {
      // TODO: handle generic processing instructions that start with
      // ! (such as <!DOCTYPE HTML .... >
      switch (m_lexState)
      {
      case ST_SAWOPENTAG:
         // start of a processing instruction
         m_lexState = ST_SAWPROCINTBANG;
         break;
      case ST_SAWCLOSETAG:
         // handle <FOO>!</BAR>
         m_lexState = ST_ONTEXT;
         // fall through
         //CHECKSTYLE:OFF
      case ST_ONCOMMENT:
      case ST_ONQATVAL:
      case ST_ONTEXT:
      case ST_ONATVAL:
      case ST_ONCDATA:
         //CHECKSTYLE:ON
         accumulate('!');
         break;
      case ST_SAWCDATAEND_ONE:
      case ST_SAWCDATAEND_TWO:
         // allows ]]! or ]!
         handleChar('!'); // m_lexState reset will be handled by handleChar()
         break;
      case ST_ONPROCINT:
         // TODO: real handling of p.i.'s
         break;
      default:
         // do not allow bangs in tag names, attribute names, or
         // anywhere else
         illegalToken('!');
         break;
      }
   }

   /**
    * Handle a - character.
    */
   private void handleDash()
   {
      switch (m_lexState)
      {
      case ST_ONPROCINT:
         // TODO: real p.i. handling
         break;
      case ST_SAWPROCINTBANG:
         m_lexState = ST_SAWCOMMENTSTARTDASH;
         break;
      case ST_SAWCOMMENTSTARTDASH:
         // two dashes before a tag name suffice to start a comment
         m_lexState = ST_ONCOMMENT;
         break;
      case ST_ONCOMMENT:
         m_lexState = ST_SAWCOMMENTENDDASH_ONE;
         break;
      case ST_SAWCOMMENTENDDASH_ONE:
      case ST_SAWCOMMENTENDDASH_TWO:
         m_lexState = ST_SAWCOMMENTENDDASH_TWO;
         break;
      case ST_SAWATVAL_SEP:
         // allow <FONT SIZE=-1>
         //                  ^
         m_lexState = ST_ONATVAL;
         accumulate('-');
         break;
      case ST_SAWCDATAEND_ONE:
      case ST_SAWCDATAEND_TWO:
         // allows ]]-
         handleChar('-'); // m_lexState reset will be handled by handleChar()
         break;
      case ST_SAWCLOSETAG:
         // allow <foo>-
         m_lexState = ST_ONTEXT;
         // fall through
         //CHECKSTYLE:OFF
      default:
         //CHECKSTYLE:ON
         accumulate('-');
      }
   }

   /**
    * Handle a " character.
    */
   private void handleDQuote()
   {
      handleQuote('"');
   }

   /**
    * Handle a ' character.
    */
   private void handleSQuote()
   {
      handleQuote('\'');
   }

   /**
    * Handle a ' or " character.
    */
   private void handleQuote(char c)
   {
      switch (m_lexState)
      {
      case ST_ONTEXT:
         accumulate(c);
         break;
      case ST_ONQATVAL:
         if (c == m_quote)
         {
            m_lexState = ST_SAWATVAL;
            finishAtVal();
         }
         else
         {
            accumulate(c);
         }
         break;
      case ST_ONPROCINT:
         // TODO: add to processing instr
         break;
      case ST_SAWATVAL_SEP:
         m_lexState = ST_ONQATVAL;
         m_quote = c;
         break;
      case ST_SAWATVAL:
         // Fall through -- this means we forgive things like
         // <A HREF="foo"">
         //              ^
         // because we ignore any number of extra quotes at the end.
      case ST_SAWATVAL_QUOTE:
         m_lexState = ST_SAWATVAL;
         break;
      case ST_SAWCLOSETAG:
         // allow <FOO>"
         //            ^
         m_lexState = ST_ONTEXT;
         accumulate(c);
         break;
      case ST_ONCOMMENT:
         accumulate(c);
         break;
      case ST_SAWCDATAEND_ONE:
      case ST_SAWCDATAEND_TWO:
         // allows ]' or ]" or ]]' or ]]"
         handleChar(c); // lets handleChar() set the m_lexState
         break;
      case ST_ONCDATA: // allow CDATA[ str = "data"
      case ST_ONATVAL:
         // allow <A HREF=foobar'baz>
         //                     ^
         accumulate(c);
         break;
      default:
         illegalToken(c);
      }
   }

   /**
    * Handle a whitespace character.
    */
   private void handleWhitespace(char c)
   {
      if (c == '\r')
      {
         m_sawCR = true;
      }
      else if (c == '\n' || m_sawCR)
      {
         m_lineNum++;
         m_sawCR = false;
      }

      switch (m_lexState)
      {
      case ST_ONTEXT:
         accumulate(c);
         break;
      case ST_ONTAGNAME:
         m_lexState = ST_SAWTAGNAME;
         finishTagName();
         break;
      case ST_ONATNAME:
         m_lexState = ST_SAWATTRNAME;
         finishAtName();
         break;
      case ST_ONQATVAL:
         accumulate(c);
         break;
      case ST_ONATVAL:
         m_lexState = ST_SAWATVAL;
         finishAtVal();
         break;
      case ST_ONPROCINT:
         // TODO: real p.i. handling
         break;
      case ST_SAWOPENTAG:
         break; // skip ignorable whitespace between < and first char of tag name
      case ST_SAWTAGNAME:
         break; // skip ignorable whitespace between tag name and whatever
      case ST_SAWATTRNAME:
         break; // skip ignorable whitespace between attr name and =
      case ST_SAWATVAL_SEP:
         break; // skip ignorable whitespace between = and attr value
      case ST_SAWATVAL:
         break; // skip ignorable whitespace between attr value and whatever
      case ST_SAWATVAL_QUOTE:
         m_lexState = ST_ONQATVAL; // whitespace is first char inside quote
         accumulate(c);
         break;
      case ST_SAWCLOSETAG:
         // allow <FOO>[ws]
         //            ^
         m_lexState = ST_ONTEXT;
         accumulate(c);
         break;
      case ST_SAWCDATAEND_ONE:
            m_lexState = ST_ONCDATA;
            // append the ']' and the new character, since CDATA is not really
            // closing now
            accumulate(']');
            accumulate(c);
            break;
      case ST_SAWCDATAEND_TWO:
            m_lexState = ST_ONCDATA;
            // adding an additional ']' for the 2nd close bracket
            accumulate(']');
            // append the ']' and the new character, since CDATA is not really
            // closing now
            accumulate(']');
            accumulate(c);
            break;
      case ST_ONCDATA: // fall thru and accumulate for CDATA
      case ST_ONCOMMENT:
         accumulate(c);
         break;
      case ST_SAWPROCINTBANG:
         // skip ignorable whitespace
         break;
      case ST_SAWCOMMENTENDDASH_ONE:
      case ST_SAWCOMMENTENDDASH_TWO:
         m_lexState = ST_ONCOMMENT;
         accumulate(c);
         break;
      default:
         illegalToken(c);
      }
   }


   /**
    * Handles an open bracket character ([). NOTE: mention lookAheadForCDATA()
    */
   private void handleOBracket()
   {
      switch (m_lexState)
      {
         case ST_SAWPROCINTBANG:
            if ( lookAheadForCDATA() )
               m_lexState = ST_ONCDATA;
            else // invalid CDATA head characters; we throw exception immediately
               illegalToken('[');
            break;
         default:
            handleChar('['); // lets handleChar() set the m_lexState
      }
   }

   /**
    * Takes temporary control over the Reader member and looks ahead for valid
    * CDATA Section start.
    *
    * @return <CODE>true</CODE> if the six chars ahead is a valid CDATA section
    * head. <CODE>false</CODE> if the six chars is not "CDATA[".
    */
   private boolean lookAheadForCDATA()
   {
      char[] charBuffer = new char[ms_strCdataHead.length()];
      StringBuffer sbHead = new StringBuffer();

      try
      {
         m_rdr.read( charBuffer );
         sbHead.append( charBuffer );
         if ( null != m_literalBuf )
            m_literalBuf.append( charBuffer ); // appending to litBuffer
      }
      catch ( IOException e )
      {
         htmlException( "IO error while parsing: " + e.toString() );
      }

      return sbHead.toString().equals( ms_strCdataHead );
   }

   /**
    * Handles an close bracket character (]).
    */
   private void handleCBracket()
   {
      switch (m_lexState)
      {
         case ST_ONCDATA:
            //accumulate(']');
            m_lexState = ST_SAWCDATAEND_ONE;
            break;
         case ST_SAWCDATAEND_ONE:
            //accumulate(']');
            m_lexState = ST_SAWCDATAEND_TWO;
            break;
         case ST_SAWCDATAEND_TWO:
            // state remains; although the first close bracket is considered as
            // a char
            accumulate(']');
            break;
         default:
            handleChar(']');
      }
   }


   /**
    * Handle any other kind of character.
    */
   private void handleChar(char c)
   {
      if (Character.isWhitespace(c))
      {
         handleWhitespace(c);
      }
      else
      {
         switch (m_lexState)
         {
         case ST_ONPROCINT:
            // TODO: real p.i. handling
            break;
         case ST_ONTEXT:
         case ST_ONTAGNAME:
         case ST_ONATNAME:
         case ST_ONQATVAL:
         case ST_ONCOMMENT:
         case ST_ONATVAL:
         case ST_ONCDATA:
            accumulate(c);
            break; // still on same state
         case ST_SAWCDATAEND_ONE:
            m_lexState = ST_ONCDATA;
            // append the ']' and the new character, since CDATA is not really
            // closing now
            accumulate(']');
            accumulate(c);
            break;
         case ST_SAWCDATAEND_TWO:
            m_lexState = ST_ONCDATA;
            // adding an additional ']' for the 2nd close bracket
            accumulate(']');
            // append the ']' and the new character, since CDATA is not really
            // closing now
            accumulate(']');
            accumulate(c);
            break;
         case ST_SAWOPENTAG:
            m_lexState = ST_ONTAGNAME; // now on tag name
            accumulate(c);
            break;
         case ST_SAWTAGNAME:
            m_lexState = ST_ONATNAME;
            accumulate(c);
            break;
         case ST_SAWATVAL_SEP:
            m_lexState = ST_ONATVAL;
            accumulate(c);
            break;
         case ST_SAWATVAL_QUOTE:
            m_lexState = ST_ONQATVAL;
            accumulate(c);
            break; // now on quoted attr val
         case ST_SAWATVAL:
            m_lexState = ST_ONATNAME;
            accumulate(c);
            break;
         case ST_SAWCLOSETAG:
            m_lexState = ST_ONTEXT;
            accumulate(c);
            break;
         case ST_SAWATTRNAME:
            // allow <HR NOSHADE>
            //           ^
            m_lexState = ST_SAWATVAL;
            
            // HTML spec says this form of an attribute should
            // get a value of the attribute name
            m_attrVal.setLength(0);
            m_attrVal.append(m_attrName.toString());

            finishAtName();
            finishAtVal(); // empty value
            break;
         case ST_SAWCOMMENTENDDASH_ONE:
         case ST_SAWCOMMENTENDDASH_TWO:
            // the comment does not end here, so add this text to comment
            m_lexState = ST_ONCOMMENT;
            accumulate(c);
            break;
         case ST_SAWPROCINTBANG:
            // TODO: real handling of processing instructions
            //m_lexState = ST_ONPROCINT;
            accumulate(c);
            break;
         default:
            illegalToken(c);
         }
      }
   }


   /**
    * Adds the character to the appropriate node for the current state.
    */
   private void accumulate(char c)
   {
      //System.out.print(c);

      switch (m_lexState)
      {
      case ST_ONTEXT:
         m_text.append(c);
         break;
      case ST_ONTAGNAME:
         m_tagName.append(c);
         break;
      case ST_ONATNAME:
         m_attrName.append(c);
         break;
      case ST_ONQATVAL:
      case ST_ONATVAL:
         m_attrVal.append(c);
         break;
      case ST_ONCOMMENT:
         m_comment.append(c);
         break;
      case ST_ONCDATA:
         m_cdata.append(c);
         break;
      default:
         illegalToken(c);
      }
   }

   /**
    * Report an illegal token.
    */
   public void illegalToken(char c)
      throws HTMLException
   {
      StringBuffer buf = new StringBuffer(20);
      buf.append("Invalid token '");
      buf.append(c);
      buf.append("' (" + (int)c + ")");
      buf.append(" parseState(");
      buf.append(getStateName(m_lexState));
      buf.append("). ");

      // now for hints !
      if (c == '>' && ST_ONTEXT == m_lexState)
      {
         buf.append("Perhaps you meant &gt;?");
      }

      htmlException(buf.toString());
   }

   /**
    * Gets the state name for a state. For now just
    * returns the number, but should in the future return
    * things like "INSIDE ATTRIBUTE VALUE" and so on.
    */
   private static String getStateName(int state)
   {
      return "" +  state;
   }

   /**
    * Mark the end of a tag name (not necessarily the end
    * of a tag). This would occur after the tag name but
    * before the first attribute and certainly before the
    * closing >.
    */
   private void finishTagName()
      throws HTMLException
   {
      String tagName = m_tagName.toString();
      if (m_upperCaseTags)
         tagName = tagName.toUpperCase();
      else if (m_lowerCaseTags)
         tagName = tagName.toLowerCase();

      // System.out.println("ENCOUNTERED: " + tagName);
      m_tagName.setLength(0);

      if (tagName.startsWith("/"))
      {
         String realName = tagName.substring(1);
         if (m_elStack.empty())
         {
            htmlException("Wrong context for tag close");
         }
         else
         {
            // System.out.println("Trying to close " + realName);

            // iterate all the down to the most recent element with this name
            // but don't necessarily remove objects using pop() because we have to be
            // forgiving for HTML
            for (int i = m_elStack.size() - 1; i >= 0; i--)
            {
               HTMLElement el = (HTMLElement)m_elStack.get(i);
               
               if (el.getNodeName().equals(realName)) // found us on stack
               {
                  // pop all these guys off
                  // System.out.println("Found " + realName + " on stack.");
                  HTMLElement lastEl = el;
                  for (int j = m_elStack.size() - 1; j >= i; j--)
                  {
                     HTMLElement popEl = (HTMLElement)m_elStack.pop();
                     // popEl.setParentNode(el);
                     // System.out.println("Closing " + popEl.getNodeName()
                     //    + ", whose parent is " + popEl.getParentNode());

                     // we can't lose the root element!
                     if (m_elStack.empty())
                     {
                        m_elStack.push(popEl);
                     }
                  }
                  break;
               }
            }
         }
      }
      else
      {
         // create an element
         HTMLElement el = new HTMLElement(tagName);

         // System.out.println("Created new element " + el);

         // add the new element to the hierarchy
         HTMLElement parent = null;

         if (!m_elStack.empty())
         {
            // get the parent, automatically popping and bypassing noclose tags
            parent = (HTMLElement)m_elStack.peek();
            while (isAutoCloseElement(parent))
            {
               // System.out.println("Automatically closing noclose element " + parent.getNodeName());
               m_elStack.pop();
               if (m_elStack.isEmpty())
               {
                  // whoops, we went off the top of the stack
                  // for now, so that we don't have multiple root elements,
                  // we never go off the top (we push it back on and use
                  // it as the parent)
                  m_elStack.push(parent);
                  break;
               }
               parent = (HTMLElement)m_elStack.peek();
            }

            // now see if we can be contained in this parent
            while (!canContain(parent, el))
            {
               // System.out.println("Automatically closing parent: " + parent.getNodeName()
               //    + " which cannot contain a " + el.getNodeName());
               m_elStack.pop();
               if (m_elStack.isEmpty())
               {
                  // whoops, we went off the top of the stack
                  // for now, so that we don't have multiple root elements,
                  // we never go off the top (we push it back on and use
                  // it as the parent)
                  m_elStack.push(parent);
                  break;
               }
               parent = (HTMLElement)m_elStack.peek();
            }
         }

         if (parent == null)
         {
            // System.out.println("Top level tag " + el.getNodeName());
         }
         else
         {
            // System.out.println("Putting " + el.getNodeName() + " under " + parent.getNodeName());
            parent.appendChild(el);
         }

         m_elStack.push(el);
      }
   }

   /**
    * Reached the > at the end of a tag.
    */
   private void finishTag()
      throws HTMLException
   {
      if (m_elStack.empty())
         htmlException("Wrong context for finish tag");

      // see if this was a tag we're searching for
      if (m_stopAt != null)
      {
         // see if the search nodes match the finishing node
         HTMLElement el = (HTMLElement)m_elStack.peek();

         for (Iterator i = m_stopAt.iterator(); i.hasNext(); )
         {
            HTMLElement stopAt = (HTMLElement)i.next();
            // System.out.println("Seeing if " + el + " matches " + stopAt);
            if (matches(stopAt, el))
            {
               // System.out.println("Found it");
               m_foundEl = el;
               m_stop = true;

               // pop all the stuff off of the stack except for the top-most
               // element
               HTMLNode root = (HTMLNode)m_elStack.pop();
               while (!m_elStack.empty())
               {
                  root = (HTMLNode)m_elStack.pop(); // pull the element off the stack
               }
               m_elStack.push(root);
               break;
            }
         }
      }
   }

   private boolean matches(Element template, Element example)
   {
      if (example.getNodeName().equalsIgnoreCase(template.getNodeName()))
      {
         NamedNodeMap attrs = template.getAttributes();
         for (int i = 0; i < attrs.getLength(); i++)
         {
            Attr attr = (Attr)attrs.item(i);
            String attrName = attr.getName();
            String value = attr.getValue();
            String elVal = example.getAttribute(attrName);
            if (elVal == null)
               return false;

            if (m_caseInsensitiveSearch)
            {
               elVal = elVal.toUpperCase();
               value = value.toUpperCase();
            }

            // System.out.println(" Seeing if attr val " + attrName + " matches.");
            if (!elVal.equals(value))
               return false;
         }
         return true;
      }
      return false;
   }

   /**
    * Finished an attribute name. Called after an attribute name but before
    * the = sign, the beginning of another attribute, or the end of the tag.
    */
   private void finishAtName()
   {
      m_curAtName = m_attrName.toString();
      m_attrName.setLength(0);
   }

   /**
    * @author   chadloder
    * 
    * @version 1.11 1999/05/20
    * 
    * A private utility method to prepare an attribute value.
    *
    * Section 7.9.3 of the SGML standard says that attribute values should
    * be transformed by:
    * <UL>
    *   <LI>Removing quotes (done by the parser)
    *   <LI>Replacing character and entity references (we do only as much of this
    *  as is needed to make sure that our dquote delimiters don't get broken on
    *  output.
    *   <LI>Deleting character 10 (ASCII LF)
    *   <LI>Replacing character 9 and 13 (ASCII HT and CR) with character 32 (SPACE)
    * </UL>
    *
    * The characters will be converted according to this scheme:
    * <TABLE BORDER=1>
    * <TR>
    *   <TD>&lt;</TD>
    * <TD>&amp;lt;</TD>
    * </TR>
    * <TR>
    *   <TD>&gt;</TD>
    * <TD>&amp;gt;</TD>
    * </TR>
    * <TR>
    *   <TD>&apos;</TD>
    * <TD>&amp;apos;</TD>
    * </TR>
    * <TR>
    *   <TD>&quot;</TD>
    * <TD>&amp;quot;</TD>
    * </TR>
    * </TABLE>
    *
    * @param   input
    * 
    * @return   String A new String with all special characters transformed
    * into their entities.
    */
   public static void prepareAttribute(String input, Writer out)
      throws java.io.IOException
   {
      /* This implementation should be fairly efficient in that
       * it minimizes the number of function calls. Hence, we
       * operate on an array rather than a String object. We
       * collect the output in a string buffer, and here too
       * we try to minimize the number of function calls.
       *
       * We do not add each normal character to the output
       * buffer one at a time. Instead, we build a "run" of
       * normal characters and, upon encountering a special
       * character, we write the previous normal run before
       * we write the special replacement.
       */
      char[] chars = input.toCharArray();
      int len = chars.length;

      char c;

      // the start of the latest run of normal characters
      int startNormal = 0;
      
      int i = 0;
      while (true)
      {
         if (i == len)
         {
            if (startNormal != i)
               out.write(chars, startNormal, i - startNormal);
            break;
         }
         c = chars[i];
         switch (c)
         {
            // for now we ignore ampersands because they could
            // signal the start of entity reference
            case '<' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&lt;");
               break;
            case '>' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&gt;");
               break;
            case '\'' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&apos;");
               break;
            case '"' :
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write("&quot;");
               break;
            case '\n':
               // fall through to the \t case, because
               // we replace both \t and \n with a space
            case '\t':
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               out.write(' ');
               break;
            case '\r':
               if (startNormal != i)
                  out.write(chars, startNormal, i - startNormal);
               startNormal = i + 1;
               // delete this LF from the output
               break;
            default:
               // do nothing...this char becomes part of the normal run
         }
         i++;
      }
   }

   /**
    * @author   chadloder
    * 
    * @version 1.11 1999/05/20
    * 
    * A private utility method to prepare an attribute value.
    *
    * Section 7.9.3 of the SGML standard says that attribute values should
    * be transformed by:
    * <UL>
    *   <LI>Removing quotes (done by the parser)
    *   <LI>Replacing character and entity references (we do only as much of this
    *  as is needed to make sure that our dquote delimiters don't get broken on
    *  output.
    *   <LI>Deleting character 10 (ASCII LF)
    *   <LI>Replacing character 9 and 13 (ASCII HT and CR) with character 32 (SPACE)
    * </UL>
    *
    * The characters will be converted according to this scheme:
    * <TABLE BORDER=1>
    * <TR>
    *   <TD>&lt;</TD>
    * <TD>&amp;lt;</TD>
    * </TR>
    * <TR>
    *   <TD>&gt;</TD>
    * <TD>&amp;gt;</TD>
    * </TR>
    * <TR>
    *   <TD>&apos;</TD>
    * <TD>&amp;apos;</TD>
    * </TR>
    * <TR>
    *   <TD>&quot;</TD>
    * <TD>&amp;quot;</TD>
    * </TR>
    * </TABLE>
    *
    * @param   input
    * 
    * @return   String A new String with all special characters transformed
    * into their entities.
    */
   public static String prepareAttribute(String input)
   {
      try
      {
         java.io.StringWriter writer = new java.io.StringWriter(
            (int)(input.length() * 1.5));
         prepareAttribute(input, writer);
         return writer.toString();
      }
      catch (java.io.IOException e)
      {
         // this should never happen
         throw new RuntimeException(e.toString());
      }
   }

   /**
    * Finished an attribute value. Called after an attribute name but before
    * the name of the next attrbitue or the end of the tag.
    */
   private void finishAtVal()
      throws HTMLException
   {
      String atVal = prepareAttribute(m_attrVal.toString());
      m_attrVal.setLength(0);

      String atName = m_curAtName;
      if (m_upperCaseAttrs)
         atName = atName.toUpperCase();
      else if (m_lowerCaseAttrs)
         atName = atName.toLowerCase();

      if (m_elStack.empty())
      {
         htmlException("Wrong context for attribute=value");
      }
      else
      {
         if (m_curAtName == null)
         {
            htmlException("Wrong context for attribute=value");
         }
         else
         {
            HTMLElement curEl = (HTMLElement)m_elStack.peek();
            // System.out.println("Setting " + atName + " attribute of " + curEl.getNodeName() + " element to " + atVal);
            curEl.setAttribute(atName, atVal);
         }
      }

      m_curAtName = null;
   }

   /**
    * Finished a text node (encountered the start of a tag).
    */
   private void finishText()
      throws HTMLException
   {
      if (m_text.length() > 0)
      {
         String text = m_text.toString();
         m_text.setLength(0);
         if (m_elStack.empty())
         {
            // ignore whitespace outside of root, but don't ignore
            // other text
            if (text.trim().length() > 0)
               htmlException("Cannot have text \"" + text.trim() + "\" outside of root element.");
         }
         else
         {
            HTMLElement curEl = (HTMLElement)m_elStack.peek();
            curEl.appendChild(new HTMLText(text));
         }
      }
   }

   /**
    * Finished a comment node.
    */
   private void finishComment()
      throws HTMLException
   {
      String text = m_comment.toString();
      m_comment.setLength(0);
      if (m_elStack.empty())
      {
         // ignore this comment because it's outside the
         // root element
         // htmlException("Wrong context for comment");
      }
      else
      {
         HTMLElement curEl = (HTMLElement)m_elStack.peek();
         // TODO: make an HTMLComment node
         // curEl.appendChild(new HTMLText(text));
      }
   }

   /**
    * Finished a CDATA node.
    */
   private void finishCDATA() throws HTMLException
   {
      String text = m_cdata.toString();
      m_cdata.setLength(0);
      if (m_elStack.empty())
      {
         if (text.trim().length() > 0)
            htmlException("Cannot have CDATA \"" + text.trim() + "\" outside of root element.");
      }
      else
      {
         HTMLElement curEl = (HTMLElement)m_elStack.peek();
         curEl.appendChild( new HTMLCDATA( text ) );
      }
   }

   /**
    * Prints the given element to System.out via DOM printing.
    */
   private static void dumpElement(HTMLElement el)
   {
      try
      {
         PSXmlDocumentBuilder.write(el, System.out);
      }
      catch (IOException e)
      {
         // not possible
      }
   }


   // Tags like <META> and <KEYGEN> are forbidden to have end tags. Will
   // return true if these tags are forbidden to have end tags. This is
   // NOT the same as tags whose end tag is optional (like <LI>). For
   // these, use endsTag().
   private static boolean isAutoCloseElement(Element el)
   {
      return (null != ms_autoCloseElements.get(el.getNodeName().toUpperCase()));
   }

   private static boolean canContain(HTMLElement parent, HTMLElement child)
   {
      String parentName = parent.getNodeName().toUpperCase();
      String childName  = child.getNodeName().toUpperCase();

      // all of these elements are peers which really can't contain each other
      if (parentName.equals("P") && childName.equals("P"))
         return false;
      if (parentName.equals("LI") && childName.equals("LI"))
         return false;
      if (parentName.equals("DT") && childName.equals("DD"))
         return false;
      if (parentName.equals("TD") && childName.equals("TD"))
         return false;

      if (childName.equals("TD"))
      {
         if (parentName.equals("TR"))
            return false;
         if (parentName.equals("TH"))
            return false;
      }
      boolean parentIsText  = (null != ms_textElements.get(parentName));
      boolean childIsText   = (null != ms_textElements.get(childName)); 
      boolean parentIsBlock = (null != ms_blockElements.get(parentName));
      boolean childIsBlock  = (null != ms_blockElements.get(childName));

      // P is weird - ends at next block level element
      if (childIsBlock && parentName.equals("P"))
         return false;

      if ( (childIsBlock && parentIsBlock) ||   (childIsText  && parentIsBlock) )
         return true;

      if ( parentIsBlock && !childIsBlock )
         return true; // ignore special stuff for now

      // TODO: deal with "special" elements
      return true;
   }

   /**
    * Writes the HTML tree to the writer, starting with the start
    * node and through all its children (not siblings) up to
    * and including the start of the end node. If the end node is
    * an element, only its start tag (with attributes) will be written.
    * <P>
    * The end object must itself be a child of start,
    * otherwise all children of start will be printed.
    */
   public static boolean writeHtmlTree(Node start, Node end, Writer out)
      throws IOException
   {
      // System.out.println("Writing node " + start);

      boolean shouldCont = true;

      switch (start.getNodeType())
      {
      case Node.ATTRIBUTE_NODE:
         out.write(" ");
         out.write(((Attr)start).getName());
         out.write("=\"");
         out.write( ((Attr)start).getValue() );
         out.write("\"");
         if (start == end)
            shouldCont = false;
         break;
         
      case Node.CDATA_SECTION_NODE:
         out.write("<![CDATA[");
         out.write(((CDATASection)start).getData());
         out.write("]]>");
         if (start == end)
            shouldCont = false;
         break;
         
      case Node.COMMENT_NODE:
         out.write("<!-- ");
         out.write(((Comment)start).getData());
         out.write(" -->");
         if (start == end)
            shouldCont = false;
         break;
         
      case Node.DOCUMENT_NODE:
         Document dNode = (Document)start;
         
         /* go through the doc's children, which should be
          * the PI nodes, DTD nodes and then the root data node
          */
         for (Node kid = dNode.getFirstChild();
             kid != null;
             kid = kid.getNextSibling()
            )
         {
            if (!shouldCont)
               break;
            shouldCont = writeHtmlTree(kid, end, out);
            if (!shouldCont)
               break;
         }
         break;
         
      case Node.ELEMENT_NODE:
         Element eNode = (Element)start;
         out.write("<");
         out.write(eNode.getTagName());

         // write out all the attributes
         NamedNodeMap attrList = eNode.getAttributes();
         for (int i = 0; i < attrList.getLength(); i++)
         {
            Attr aNode = (Attr)attrList.item(i);
            writeHtmlTree(aNode, end, out);
         }
         
         // write all the child nodes (if this is not the stop
         // element)
         if (start == end)
            shouldCont = false;

         if (eNode.hasChildNodes() && shouldCont)
         {
            // close the tag
            out.write(">");
            for (Node kid = eNode.getFirstChild();
                kid != null;
                kid = kid.getNextSibling()
               )
            {
               if (!shouldCont)
                  break;
               shouldCont = writeHtmlTree(kid, end, out);
            }
         }
         else
         {
            out.write(">");
         }

         if (shouldCont && !PSHtmlParser.isAutoCloseElement(eNode))
         {
            out.write("</");
            out.write(eNode.getTagName());
            out.write(">");
         }

         break;
         
      case Node.PROCESSING_INSTRUCTION_NODE:
         ProcessingInstruction pi = (ProcessingInstruction)start;
         out.write("<?");
         out.write(pi.getTarget());
         out.write(" ");
         out.write(pi.getData());
         out.write("?>");
         if (start == end)
            shouldCont = false;
         break;
         
      case Node.TEXT_NODE:
         out.write( ((Text)start).getData() );
         if (start == end)
            shouldCont = false;
         break;

      default:
         for (Node kid = start.getFirstChild();
             kid != null;
             kid = kid.getNextSibling()
            )
         {
            if (!shouldCont)
               break;
            shouldCont = writeHtmlTree(kid, end, out);
         }
      }

      return shouldCont;
   }


   public static Node getRoot(Node n)
   {
      Node parent = null;
      while (n != null)
      {
         parent = n;
         n = n.getParentNode();
      }
      return parent;
   }

   public HTMLElement getRoot()
   {
      HTMLElement ret = null;
      if (!m_elStack.empty())
      {
         ret = (HTMLElement)m_elStack.peek();
      }
      return (HTMLElement)getRoot(ret);
   }

   private void htmlException(String message)
      throws HTMLException
   {
      HTMLException ex = new HTMLException((short)0, message);
      ex.setLineNumber(m_lineNum);
      throw ex;
   }

   // the reader
   private Reader m_rdr;

   // the current lexing state
   private int m_lexState = ST_ONTEXT;

   // the current line number of the input
   private int m_lineNum = 0;

   // we collapse \r\n to a single line number
   private boolean m_sawCR = false;

   // the quote char used to delimit the current quoted string
   private char m_quote = 0;

   // we stop if/when we reach an element with this name and with all of
   // its attributes
   // private HTMLElement m_stopAt = null;
   private ArrayList m_stopAt = null;

   // true if we should stop parsing
   private boolean m_stop = false;

   // if m_stopAt was specified and we found the element, this will be
   // set to the matching element, otherwise will be null
   private HTMLElement m_foundEl = null;

   // true if values should be compared case insensitive
   private boolean m_caseInsensitiveSearch = false;

   private static final String ms_strCdataHead = new String("CDATA[");

   // the current "scratch buffers" for accumulating different content
   private StringBuffer m_text = new StringBuffer();     // text data
   private StringBuffer m_tagName  = new StringBuffer(); // a tag name
   private StringBuffer m_attrName = new StringBuffer(); // an attribute name
   private StringBuffer m_attrVal  = new StringBuffer(); // an attribute value
   private StringBuffer m_comment  = new StringBuffer(); // a comment
   private StringBuffer m_procInstr= new StringBuffer(); // processing instruction
   private StringBuffer m_cdata = new StringBuffer(); // CDATA source

   // this is non-null if the caller wants us to maintain a literal buffer
   private StringBuffer m_literalBuf = null;

   private String m_curAtName = null;
 
   // TODO: use arraylist for performance reasons
   private Stack m_elStack = new Stack();

   private boolean m_upperCaseTags = true;
   private boolean m_lowerCaseTags = false;
   private boolean m_upperCaseAttrs = true;
   private boolean m_lowerCaseAttrs = false;

   // STATIC PARSING TABLES AND WHATNOT

   /*
    * Certain HTML elements that may appear in BODY are said to be "block-level"
    * while others are "inline" (also known as "text level"). The distinction is
    * founded on several notions:
    *
    * Content model 
    * Generally, block-level elements may contain inline elements and other block-level
    * elements. Generally, inline elements may contain only data and other inline elements.
    * Inherent in this structural distinction is the idea that block elements create
    * "larger" structures than inline elements.
    *
    * Formatting
    * By default, block-level elements are formatted differently than inline elements.
    * Generally, block-level elements begin on new lines, inline elements do not. For
    * information about white space, line breaks, and block formatting, please consult
    * the section on text. 
    */

   /**
    * noclose elements ALWAYS close immediately and never get pushed onto the stack,
    * because they are not allowed to have close tags.
    */
   private static Map ms_autoCloseElements = new HashMap();
   static
   {
      ms_autoCloseElements.put("AREA", Boolean.TRUE);
      ms_autoCloseElements.put("BASE", Boolean.TRUE);
      ms_autoCloseElements.put("BASEFONT", Boolean.TRUE);
      ms_autoCloseElements.put("BR", Boolean.TRUE);
      ms_autoCloseElements.put("COL", Boolean.TRUE);
      ms_autoCloseElements.put("FRAME", Boolean.TRUE);
      ms_autoCloseElements.put("HR", Boolean.TRUE);
      ms_autoCloseElements.put("IMG", Boolean.TRUE);
      ms_autoCloseElements.put("INPUT", Boolean.TRUE);
      ms_autoCloseElements.put("ISINDEX", Boolean.TRUE);
      ms_autoCloseElements.put("LINK", Boolean.TRUE);
      ms_autoCloseElements.put("KEYGEN", Boolean.TRUE);
      ms_autoCloseElements.put("META", Boolean.TRUE);
      ms_autoCloseElements.put("PARAM", Boolean.TRUE);
      ms_autoCloseElements.put("SPACER", Boolean.TRUE);
      ms_autoCloseElements.put("WBR", Boolean.TRUE);
   }

   /**
    * Block-level elements are "higher level" elements that can contain
    * other block-level and text-level elements.
    */
   private static Map ms_blockElements = new HashMap();
   static
   {
      ms_blockElements.put("ADDRESS", Boolean.TRUE);
      ms_blockElements.put("BLOCKQUOTE", Boolean.TRUE);
      ms_blockElements.put("CENTER", Boolean.TRUE);
      ms_blockElements.put("DIR", Boolean.TRUE);
      ms_blockElements.put("DIV", Boolean.TRUE);
      ms_blockElements.put("DL", Boolean.TRUE);
      ms_blockElements.put("FIELDSET", Boolean.TRUE);
      ms_blockElements.put("FORM", Boolean.TRUE);
      ms_blockElements.put("H1", Boolean.TRUE);
      ms_blockElements.put("H2", Boolean.TRUE);
      ms_blockElements.put("H3", Boolean.TRUE);
      ms_blockElements.put("H4", Boolean.TRUE);
      ms_blockElements.put("H5", Boolean.TRUE);
      ms_blockElements.put("H6", Boolean.TRUE);
      ms_blockElements.put("MENU", Boolean.TRUE);
      ms_blockElements.put("NOFRAMES", Boolean.TRUE);
      ms_blockElements.put("NOSCRIPT", Boolean.TRUE);
      ms_blockElements.put("OL", Boolean.TRUE);
      ms_blockElements.put("PRE", Boolean.TRUE);
      ms_blockElements.put("TABLE", Boolean.TRUE);
      ms_blockElements.put("UL", Boolean.TRUE);

      // these aren't "officially" block level, but we treat them as such
      ms_blockElements.put("DD", Boolean.TRUE);
      ms_blockElements.put("DT", Boolean.TRUE);
      ms_blockElements.put("FRAMESET", Boolean.TRUE);
      ms_blockElements.put("LI", Boolean.TRUE);
      ms_blockElements.put("TBODY", Boolean.TRUE);
      ms_blockElements.put("TD", Boolean.TRUE);
      ms_blockElements.put("TFOOT", Boolean.TRUE);
      ms_blockElements.put("TH", Boolean.TRUE);
      ms_blockElements.put("THEAD", Boolean.TRUE);
      ms_blockElements.put("TR", Boolean.TRUE);

      ms_blockElements.put("HTML", Boolean.TRUE);
      ms_blockElements.put("BODY", Boolean.TRUE);
   }

   /**
    * Text-level elements are "lower-level" and generally cannot contain
    * block-level elements. They can contain other text-level elements.
    */
   private static Map ms_textElements = new HashMap();
   static
   {
      ms_textElements.put("A", Boolean.TRUE);
      ms_textElements.put("ABBR", Boolean.TRUE);
      ms_textElements.put("ACRONYM", Boolean.TRUE);
      ms_textElements.put("B", Boolean.TRUE);
      ms_textElements.put("BDO", Boolean.TRUE);
      ms_textElements.put("BIG", Boolean.TRUE);
      ms_textElements.put("BR", Boolean.TRUE);
      ms_textElements.put("CITE", Boolean.TRUE);
      ms_textElements.put("CODE", Boolean.TRUE);
      ms_textElements.put("DFN", Boolean.TRUE);
      ms_textElements.put("EM", Boolean.TRUE);
      ms_textElements.put("FONT", Boolean.TRUE);
      ms_textElements.put("I", Boolean.TRUE);
      ms_textElements.put("IMG", Boolean.TRUE);
      ms_textElements.put("INPUT", Boolean.TRUE);
      ms_textElements.put("KBD", Boolean.TRUE);
      ms_textElements.put("LABEL", Boolean.TRUE);
      ms_textElements.put("Q", Boolean.TRUE);
      ms_textElements.put("S", Boolean.TRUE);
      ms_textElements.put("SAMP", Boolean.TRUE);
      ms_textElements.put("SELECT", Boolean.TRUE);
      ms_textElements.put("SMALL", Boolean.TRUE);
      ms_textElements.put("SPAN", Boolean.TRUE);
      ms_textElements.put("STRIKE", Boolean.TRUE);
      ms_textElements.put("STRONG", Boolean.TRUE);
      ms_textElements.put("SUB", Boolean.TRUE);
      ms_textElements.put("SUP", Boolean.TRUE);
      ms_textElements.put("TEXTAREA", Boolean.TRUE);
      ms_textElements.put("TT", Boolean.TRUE);
      ms_textElements.put("U", Boolean.TRUE);
      ms_textElements.put("VAR", Boolean.TRUE);      
   }

   /**
    * Some elements can act as either block- or text-level, depending on
    * their context. I call these "special".
    */
   private static Map ms_specialElements = new HashMap();
   static
   {
      ms_specialElements.put("APPLET", Boolean.TRUE);
      ms_specialElements.put("BUTTON", Boolean.TRUE);
      ms_specialElements.put("DEL", Boolean.TRUE);
      ms_specialElements.put("IFRAME", Boolean.TRUE);
      ms_specialElements.put("INS", Boolean.TRUE);
      ms_specialElements.put("MAP", Boolean.TRUE);
      ms_specialElements.put("OBJECT", Boolean.TRUE);
      ms_specialElements.put("SCRIPT", Boolean.TRUE);
   }

   private static final int ST_ONTEXT = 0;
   private static final int ST_ONTAGNAME = 1;
   private static final int ST_ONATNAME = 2;
   private static final int ST_ONQATVAL = 3;
   private static final int ST_ONATVAL = 4;
   private static final int ST_ONCOMMENT = 5;
   private static final int ST_ONPROCINT = 6;

   private static final int ST_SAWOPENTAG = 7;
   private static final int ST_SAWTAGNAME = 8;
   private static final int ST_SAWATTRNAME = 9;
   private static final int ST_SAWATVAL_SEP = 10;
   private static final int ST_SAWATVAL_QUOTE = 11;
   private static final int ST_SAWATVAL = 12;
   private static final int ST_SAWCLOSETAG = 13; 
   private static final int ST_SAWPROCINTBANG = 14;
   private static final int ST_SAWCOMMENTSTARTDASH = 15;
   private static final int ST_SAWCOMMENTENDDASH_ONE = 16;
   private static final int ST_SAWCOMMENTENDDASH_TWO = 17;

   /** new lex states: ONCDATA, SAWCDATACLOSEONE, and SAWCDATACLOSETWO */
   private static final int ST_ONCDATA = 18;
   private static final int ST_SAWCDATAEND_ONE = 19;
   private static final int ST_SAWCDATAEND_TWO = 20;
}




