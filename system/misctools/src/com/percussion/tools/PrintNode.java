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

package com.percussion.tools;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Writer;

public class PrintNode
{

  public PrintNode()
  {
  }

	/**
	 * Prints the supplied node to the supplied writer. Empty elements are written
	 * using the empty element tag format. See the method by the same name that
	 * takes more parameters for a more detailed description.
	**/
	public static void printNode( Node node, String indent, Writer out )
			throws IOException
	{
		printNode( node, indent, out, true );
	}

	/**
	 * Writes the supplied node and all its children as a nicely formatted XML
	 * document to the supplied writer.
	 *
	 * @param node The node to print. To print a whole document, pass in the
	 * document. If null, nothing is done.
	 *
	 * @param indent A String containing white space. Comments and elements
	 * are prepended with this string when they are being written. Children are
	 * automatically indented from their parents. <p/>
	 * When passing in a document, this should typically be the empty string.
	 * This may be null to mean the empty string.
	 *
	 * @param out The target for the output. If null, nothing is done.
	 *
	 * @param useEmptyElementTag If <code>true</code>, when an element has no
	 * data, the empty element tag form is used (example: <foo/>. If <code>false
	 * </code>, a start and end tag is used, even when no content is present
	 * (example: <foo></foo>). This flag has no effect if an element has content.
	 *
	 * @throws IOException If a failure occurs while navigating the node.
	**/
	public static void printNode(Node node, String indent, Writer out,
			boolean useEmptyElementTag )
			throws IOException
	{
		if (node == null || null == out )
			return;

		if ( null == indent )
			indent = "";

		switch (node.getNodeType())
		{
		case Node.ATTRIBUTE_NODE:
			out.write(" " + ((Attr)node).getName() + "=\"" +
				((Attr)node).getValue() + "\"");
			break;

		case Node.CDATA_SECTION_NODE:
			out.write("<![CDATA[" +
				((CDATASection)node).getData() +
//				convertToXmlEntities(((CDATASection)node).getData()) +
				"]]>" + NEWLINE );
			break;

		case Node.COMMENT_NODE:
			out.write(indent + "<!-- " + ((Comment)node).getData() + " -->" + NEWLINE );
			break;

      case Node.DOCUMENT_NODE:
         Document dNode = (Document)node;
         out.write(XML_HEADER + NEWLINE);
         /* go through the doc's children, which should be
          * the PI nodes, DTD nodes and then the root data node
          */
         for ( Node kid = dNode.getFirstChild(); kid != null; kid = kid.getNextSibling())
            printNode(kid, indent, out, useEmptyElementTag );
         break;

		case Node.ELEMENT_NODE:
			Element eNode = (Element)node;
//      out.write("\n");
			String strName = eNode.getTagName();
			out.write(indent + "<" + eNode.getTagName());

			/* print any attributes in the tag */
			NamedNodeMap attrList = eNode.getAttributes();
			if (attrList != null)
			{
				Attr aNode;
				for (int i = 0; i < attrList.getLength(); i++)
				{
					aNode = (Attr)attrList.item(i);
					out.write(" " + aNode.getName() + "=\"" +
						aNode.getValue() + "\"");
				}
			}

			/* if there are children, close the tag, print the kids
			 * and print the end tag
			 */
			if (eNode.hasChildNodes())
			{
				/* close the start tag */
				out.write(">");

				/* print the kids */
				Node kid = null;
				Text text = null;
				String str = "";
				boolean bElementPresent = false, bTextPresent = false;
				for (kid = eNode.getFirstChild();	kid != null; kid = kid.getNextSibling())
				{
					if(kid instanceof Element)
					{
						bElementPresent = true;
						out.write( NEWLINE );
					}
					if(kid instanceof Text)
					{
						text = (Text)kid;
						str = text.getNodeValue();
						if(null != str && !(str.trim().equals("")))
							bTextPresent = true;
					}
					printNode(kid, indent + "   ", out, useEmptyElementTag );
				}

				/* and the end tag */
				if(bElementPresent)
						out.write ( NEWLINE + indent );

				out.write("</" + eNode.getTagName() + ">");
				if(!bTextPresent)
					out.write( NEWLINE );
			}
			else
			{
				/* close this tag with the end tag */
				if ( useEmptyElementTag )
					out.write(" />");
				else
					out.write("></" + eNode.getTagName() + ">");
			}
			break;

		case Node.PROCESSING_INSTRUCTION_NODE:
			ProcessingInstruction pi = (ProcessingInstruction)node;
			out.write("<?" + pi.getTarget() + " " + pi.getData() + "?>" + NEWLINE );
			break;

		case Node.TEXT_NODE:
			String str = node.getNodeValue();
      if(null != str)
        str = str.trim();
			if(null!= str && !str.equals (""))
				out.write(convertToXmlEntities(str));
			break;
		case Node.ENTITY_REFERENCE_NODE:
			EntityReference er = (EntityReference)node;
			out.write ("&"+er.getNodeName()+";" + NEWLINE );
			return;
		case Node.DOCUMENT_TYPE_NODE:
		{
			DocumentType dt = (DocumentType)node;
			/* Since we are not supporting embedded DOCTYPE's currently, we can just
				add the default entity defs that were added to the HTML doc before
				processing. This will have to be addressed later. The commented code
				below writes out all entities in the document. */
			out.write( "<!DOCTYPE "+dt.getName() + " [" + /*supported later*/ "]>" + NEWLINE );

			/* We want to build entries in the output of the form
				<!ENTITY ent '&#nnn;'>
				for each character entity in the map,
				where ent is the name of the entity and nnn is the character code for the
				entity.
				In order to get the value, we must use the object in the NamedNodeMap
				as an EntityDecl. This ties us to the IBM parser because it is not documented.
				The following few lines check this assumption and complain if it isn't
				valid.
				We expect nearly all (if not all) entities to be character entities
				(I don't know if HTML allows entity defs now, but it probably will when
				XHTML comes into use). If the value is a single char, we treat it as a
				char entity, otherwise we treat it as a regular entity. */
/*
			NamedNodeMap map = dt.getEntities();

			int entities = map.getLength();
			if ( entities > 0 )
				out.write( "<!DOCTYPE "+dt.getName() + " [\n" );

			Node ent = map.item(0);
			if ( !(ent instanceof com.ibm.xml.parser.EntityDecl ))
				throw new RuntimeException( "Expected class com.ibm.xml.parser.EntityDecl, found "
					+ ent.getClass().getName() + ". This new class is not supported." );

			for(int i=0; i < entities; i++)
			{
				EntityDecl entity =  (com.ibm.xml.parser.EntityDecl) map.item(i);
				String name = entity.getNodeName();
				String value = entity.getValue();
				if ( 1 == value.length())
				{
					// for regular character entities, the node value is the character
					int charVal = (value.toCharArray())[0];
					value = Integer.toString( charVal );
					out.write( "\t<!ENTITY " + name + " '&#" + value + ";'>\n" );
				}
				else if ( name.equals( "amp" ) || name.equals( "lt" ) || name.equals( "gt" )
					|| name.equals( "quot" ) || name.equals( "apos" ))
				{
					// for the 5 XML special entities, the value is the entity value,
					// in the form &#nnn, where nnn is the character value
					out.write( "\t<!ENTITY " + name + " '" + value + "'>\n" );
				}
			}
			if ( entities > 0 )
				out.write( "]>\n" );
*/
			return;
		}
		}
		return;
	}

	private static String convertToXmlEntities(String input)
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

		StringBuilder buf = new StringBuilder((int)(chars.length * 1.5));

		char c;

		// the start of the latest run of normal characters
		int startNormal = 0;

		int i = 0;
		while (true)
		{
			if (i == len)
			{
				if (startNormal != i)
					buf.append(chars, startNormal, i - startNormal);
				break;
			}
			c = chars[i];
			switch (c)
			{
				case '&' :
					if (startNormal != i)
						buf.append(chars, startNormal, i - startNormal);
					startNormal = i + 1;
					buf.append("&amp;");
					break;
				case '<' :
					if (startNormal != i)
						buf.append(chars, startNormal, i - startNormal);
					startNormal = i + 1;
					buf.append("&lt;");
					break;
				case '>' :
					if (startNormal != i)
						buf.append(chars, startNormal, i - startNormal);
					startNormal = i + 1;
					buf.append("&gt;");
					break;
				case '\'' :
					if (startNormal != i)
						buf.append(chars, startNormal, i - startNormal);
					startNormal = i + 1;
					buf.append("&apos;");
					break;
				case '"' :
					if (startNormal != i)
						buf.append(chars, startNormal, i - startNormal);
					startNormal = i + 1;
					buf.append("&quot;");
					break;
				default:
					// do nothing...this char becomes part of the normal run
			}
			i++;
		}
		return buf.toString();
	}
  private static final String NEWLINE = "\r\n";

  /**
   * The document's XML header string
   */
  private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";
}
