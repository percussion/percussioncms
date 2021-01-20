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


package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class encapsulates a list of database information mapping objects,
 * <code>PSDbmsMapping</code>, for a source server.
 */
public class PSDbmsMap  implements IPSDeployComponent
{
   /**
    * Creating an object from a source server name.
    *
    * @param server The name of the source server. It may not be empty or
    * <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>server</code> is
    * <code>null</code> or empty.
    */
   public PSDbmsMap(String server)
   {
      if ( server == null || server.trim().length() == 0 )
         throw new IllegalArgumentException(
            "server name may not be null or empty");

      m_srcServer = server;
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSDbmsMap(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the name of the source server.
    *
    * @return The name of source server, it will never be <code>null</code> or
    * empty.
    */
   public String getSourceServer()
   {
      return m_srcServer;
   }

   /**
    * Get a list of <code>PSDbmsMapping</code> objects.
    *
    * @return an Iterator over zero or more <code>PSDbmsMapping</code> objects,
    * never <code>null</code>, but might be empty.
    */
   public Iterator getMappings()
   {
      return m_mappingList.iterator();
   }

   /**
    * Add an <code>PSDbmsMapping</code> object.
    *
    * @param mapping The mapping to be added, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>mapping</code> is
    * <code>null</code>.
    */
   public void addMapping(PSDbmsMapping mapping)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");

      m_mappingList.add(mapping);
   }
   
   /**
    * Removes the supplied <code>PSDbmsMapping</code> object if it exists in the 
    * map. Uses {@link #getMapping(PSDbmsInfo) getMapping(sourceInfo)} for 
    * checking existance of mapping.
    *
    * @param mapping The <code>PSDbmsMapping</code> object to be removed, may
    * not be <code>null</code>
    *
    * @throws IllegalArgumentException If <code>mapping</code> is
    * <code>null</code>.
    */
   public void removeMapping(PSDbmsMapping mapping)
   {
      if ( mapping == null )
         throw new IllegalArgumentException("mappinge may not be null");

      mapping = getMapping(mapping.getSourceInfo());
      
      if(mapping != null)
         m_mappingList.remove(mapping);
   }

   /**
    * Gets the mapping with the specified source info.
    *
    * @param sourceInfo The source info to use to locate the mapping.  The
    * mapping with the matching source info will be returned.
    *
    * @return the mapping, may be <code>null</code> if none found.
    *
    * @throws IllegalArgumentException If <code>sourceInfo</code> is
    * <code>null</code>.
    */
   public PSDbmsMapping getMapping(String sourceInfo)
   {
      if (sourceInfo == null)
         throw new IllegalArgumentException("sourceInfo may not be null");

      PSDbmsMapping result = null;
      Iterator list = m_mappingList.iterator();

      while (list.hasNext() && result == null)
      {
         PSDbmsMapping currMap = (PSDbmsMapping) list.next();
         if ( sourceInfo.equals(currMap.getSourceInfo()) )
            result = currMap;
      }

      return result;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDbmsMap (PSXDbmsMapping*)>
    * &lt;!ATTLIST PSXDbmsMap
    *     sourceServer CDATA #REQUIRED
    * ...
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    * See {@link PSDbmsMapping#toXml(Document)} for the XML format of
    * PSXDbmsMapping element.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_SRC_SERVER, m_srcServer);

      Iterator maplist = m_mappingList.iterator();
      while (maplist.hasNext())
      {
         PSDbmsMapping mapping = (PSDbmsMapping) maplist.next();
         Element mappingXml = mapping.toXml(doc);
         root.appendChild(mappingXml);
      }
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      m_srcServer = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_SRC_SERVER);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      m_mappingList.clear();

      Element mappingEl = tree.getNextElement(PSDbmsMapping.XML_NODE_NAME,
         firstFlags);

      while (mappingEl != null)
      {
         PSDbmsMapping mapping = new PSDbmsMapping(mappingEl);
         m_mappingList.add(mapping);

         mappingEl = tree.getNextElement(PSDbmsMapping.XML_NODE_NAME,
            nextFlags);
      }
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSDbmsMap))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSDbmsMap");

      PSDbmsMap objSrc = (PSDbmsMap) obj;
      m_srcServer = objSrc.m_srcServer;
      m_mappingList.clear();
      m_mappingList.addAll(objSrc.m_mappingList);
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_srcServer.hashCode() + m_mappingList.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean bEqual = false;

      if ((obj instanceof PSDbmsMap))
      {
         if (obj == this) // compare to itself
         {
            bEqual = true;
         }
         else
         {
            PSDbmsMap objMap2 = (PSDbmsMap) obj;

            boolean b1 = m_mappingList.equals(objMap2.m_mappingList);
            boolean b2 = m_srcServer.equals(objMap2.m_srcServer);

            if (m_mappingList.equals(objMap2.m_mappingList) &&
               m_srcServer.equals(objMap2.m_srcServer) )
               bEqual = true;
         }
      }
      return bEqual;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDbmsMap";

   /**
    * The source server name as the attribute of the XML root node.
    */
   private static final String XML_ATTR_SRC_SERVER = "sourceServer";

   /**
    * The source server name. Initialized by the constructor, after that,
    * only modified by <code>fromXml() and copyFrom()</code>, never empty
    * or <code>null</code>.
    */
   private String m_srcServer;

   /**
    * A list of <code>PSDbmsMapping</code> objects. Initialized here, modified
    * by <code>fromXml(), addMapping() and copyFrom()</code> can never to
    * <code>null</code>.
    */
   private List m_mappingList =  new ArrayList();;

}
