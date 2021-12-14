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

package com.percussion.xml;


import com.percussion.utils.xml.PSSaxHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Writer;

/**
 * This class is separated from PSXmlTreeWaker.java to fix a specific bug for
 * xmldom package. The idea is to not jeopardise other areas (as treewalker is
 * used everywhere) by making this change for xmldom. The only difference is
 * that the pretty print via indentation is completely disabled. This is to
 * avoid problems with  mixing the content between xml (html) nodes which is
 * very common with any HTML editor control like Ektron control.
 */
public class PSNodePrinter
{
   /**
    * Only constructor. Takes the print writer as the argument.
    * @param out must not be <code>null</code>.
    * @throws IllegalArgumentException
    */
   public PSNodePrinter(Writer out)
   {
      if(out==null)
      {
         throw new IllegalArgumentException("output writer must not be null");
      }
      m_out = out;
   }

   /**
    * This method works in conjunction with {@link #printElement(Element,
    * String) printElement}.
    * For the passed in node and all of its children, either the node is
    * written to the writer associated with this object during construction,
    * or <code>printElement<code> is called to handle this writing. No
    * indentation is done. This method calls itself recursively, so only the
    * root-most element that needs to be printed is passed in.
    *
    * @param node The node to print. If <code>null</code>, the method returns
    * immediately.
    *
    * @throws IOException when it fails to print a node
    */
   public void printNode(Node node)
      throws IOException
   {
      if (node == null)
         return;
      String ent = "";
      switch (node.getNodeType())
      {
         case Node.ATTRIBUTE_NODE:
            m_out.write(" ");
            m_out.write(((Attr)node).getName());
            m_out.write("=\"");
            // #RVAI-4CDKN3 : now we handle entities in attribute values
            ent = PSXmlTreeWalker.convertToXmlEntities(
               ((Attr)node).getValue());
            m_out.write(ent);
            m_out.write("\"");
            break;

         case Node.CDATA_SECTION_NODE:
            m_out.write("<![CDATA[");
            ent = PSXmlTreeWalker.convertToXmlEntities(
               ((CDATASection)node).getData());
            m_out.write(ent);
            m_out.write("]]>");
            break;

         case Node.COMMENT_NODE:
            m_out.write("<!--");
            m_out.write(((Comment)node).getData());
            m_out.write("-->");
            break;

         case Node.DOCUMENT_NODE:
            Document dNode = (Document)node;

         /* go through the doc's children, which should be
          * the PI nodes, DTD nodes and then the root data node
          */
            Node kid = dNode.getFirstChild();
            if (kid != null)
            {
               if (kid.getNodeType() == Node.ELEMENT_NODE)
                  printElement((Element)kid);
               else
                  printNode(kid);

               while (null != (kid = kid.getNextSibling()))
               {
                  printNode(kid);
               }
            }
            break;

         case Node.DOCUMENT_TYPE_NODE:
            break;

         case Node.ELEMENT_NODE:
            Element eNode = (Element)node;
            printElement(eNode);
            break;

   /*
    *New code for entity references.  When "printing" the XML document,
    *we must include the "Name" of any entities we encounter.  This will
    *only happen in Element data, never in attribute data.
    */
         case Node.ENTITY_REFERENCE_NODE:
            m_out.write("&"+node.getNodeName()+";");
            break;

         case Node.PROCESSING_INSTRUCTION_NODE:
            ProcessingInstruction pi = (ProcessingInstruction)node;
            // tidy returns null for pi.getTarget()
            String pitarget = pi.getTarget();
            String pidata = pi.getData();
            m_out.write("<?");
            if (pitarget != null)
            {
               pitarget = pitarget.trim();
               m_out.write(pitarget);
               m_out.write(" ");
            }
            if (pidata != null)
            {
               pidata = pidata.trim();
               m_out.write(pidata);
            }
            String tmp = m_out.toString();
            if (tmp.endsWith("?"))
               m_out.write(">");
            else
               m_out.write("?>");
            break;

         case Node.TEXT_NODE:
            String data = ((Text)node).getData();
            if (data != null)
               ent = PSXmlTreeWalker.convertToXmlEntities(data);
            m_out.write(ent);
            break;
      }
   }

   /**
    * Method to print the Element node
    * @param eNode must not be <code>null</code>
    * @throws IOException when it fails to print a node
    */
   void printElement(Element eNode)
      throws IOException
   {
      m_out.write("<");
      String tagName = eNode.getTagName();
      m_out.write(tagName);

      /* print any attributes in the tag */
      // #RVAI-4CDKN3 : now we handle entities in attribute values
      NamedNodeMap attrList = eNode.getAttributes();
      if (attrList != null) {
         for (int i = 0; i < attrList.getLength(); i++) {
            printNode(attrList.item(i));
         }
      }

      /* if there are children, close the tag, print the kids
       * and print the end tag
       */
      if (eNode.hasChildNodes())
      {
         /* close the tag */
         m_out.write(">");
         //Deal <script> elements differently in that we just copy the script
         //as it is and try not to resolve entity references and maintain the
         //line breaks so that the javascript works that way it should.
         if(tagName.equalsIgnoreCase("script"))
         {
            // We must preserve xsl tags that may be used to add/modify
            // the script tags. So we check the first child node and
            // if it is an xsl element then we print it.
            Node fChild = eNode.getFirstChild();
            String name = fChild.getNodeName();
            if(name.toLowerCase().startsWith("xsl:"))
               printNode(fChild);
            
            StringBuilder script = new StringBuilder(80);
            NodeList nodes = eNode.getChildNodes();
            int len = nodes.getLength();
            for(int i = 0; i < len; i++)
            {
               Node node = nodes.item(i);
               switch (node.getNodeType())
               {
                  case Node.TEXT_NODE :
                  case Node.COMMENT_NODE :                     
                     script.append(((CharacterData) node).getData().trim());                     
                     break;
                  case Node.CDATA_SECTION_NODE :
                     script.append(CDATAOPEN);
                     script.append(((CharacterData) node).getData().trim());
                     script.append(CDATACLOSE);
                     break;
                  default :
                     script.append(";");
                     break;
               }
            }
            String filteredScript = cleanupJavaScript(script.toString());
            m_out.write(filteredScript);
         }
         else
         {
            /* print the kids */
            for ( Node kid = eNode.getFirstChild(); kid != null;
               kid = kid.getNextSibling())
            {
                  printNode(kid);
            }
         }
         m_out.write("</");
         m_out.write(eNode.getTagName());
         m_out.write(">");
      }
      else if(canBeSelfClosed(eNode.getTagName()))
      {
         /* close this tag with the end tag */
         m_out.write(" />");
      }
      else
      {
         /* close the tag */
         m_out.write(">");
         /* This element must have a closing tag and not self close */
         m_out.write("</");
         m_out.write(eNode.getTagName());
         m_out.write(">");
      }
   }

   /**
    * Cleanup the javascript so that the end result is something like this:
    * &lt;script language="javascript"&gt;&lt;![CDATA[
    * function test()
    * {
    *    alert("test");
    * }
    * ]]&gt;
    * &lt;/script&gt;
    * @param script text data of the script element, assumed not <code>null</code>.
    * @return cleanedup script text, never <code>null</code>.
    */
   private String cleanupJavaScript(String script)
   {
      //Chop the XML comment open if present
      if(script.startsWith(COMMENTOPEN))
         script = script.substring(COMMENTOPEN.length()).trim();
      //Chop the XML comment close if present
      if (script.endsWith(COMMENTCLOSE))
      {
         script = script.substring(0, script.length() - COMMENTCLOSE.length())
               .trim();
      }

      //Ektron puts "//" at the end of javacript chop that
      if(script.endsWith("//"))
         script = script.substring(0, script.length()-2).trim();

      //Add CDATA open tag if not present to preserve javascript structure
      if(!script.startsWith(JSCRIPTCOMMENTOPEN + CDATAOPEN + JSCRIPTCOMMENTCLOSE))
      {
         if(script.startsWith(CDATAOPEN))
         {
            script =  script.substring(CDATAOPEN.length());
         }
        
         String nline = script.startsWith(NEWLINE) ? "" : NEWLINE;
         script = JSCRIPTCOMMENTOPEN + CDATAOPEN + 
            JSCRIPTCOMMENTCLOSE + nline + script;   
         
         
      }

      //Close CDATA tag if required
      if(!script.endsWith(JSCRIPTCOMMENTOPEN + CDATACLOSE + JSCRIPTCOMMENTCLOSE))
      {
         if(script.endsWith(CDATACLOSE))
         {
            script = script.substring(0, script.length() - CDATACLOSE.length());
         }
         
         String nline = script.endsWith(NEWLINE) ? "" : NEWLINE;
         script = script + nline + JSCRIPTCOMMENTOPEN + 
            CDATACLOSE + JSCRIPTCOMMENTCLOSE;   
         
      }
         

      return script;
   }
   
   /**
    * Helper method to determine if an empty element is allowed to be self
    * closed.
    * @param name assumed not <code>null</code>.
    * @return <code>true</code> if the element can be self
    * closed.
    */
   public static boolean canBeSelfClosed(String name)
   {
      return PSSaxHelper.canBeSelfClosedElement(name);
   }

   /**
    * writer object to write the node tree. Never <code>null</code> after
    * the object is created.
    */
   private Writer m_out;

   //String constants used in cleaning <script> element value
   private static final String CDATAOPEN = "<![CDATA[";
   private static final String CDATACLOSE = "]]>";
   private static final String COMMENTOPEN = "<!--";
   private static final String COMMENTCLOSE = "-->";
   private static final String JSCRIPTCOMMENTOPEN = "/*";
   private static final String JSCRIPTCOMMENTCLOSE = "*/";
   private static final String NEWLINE = "\r\n";   
}
