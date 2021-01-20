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


/**
 * This class encapsulates the mapping of source and target datasources,
 * <code>PSXDatasourceMap</code>, from a source to a target.
 */
public class PSDbmsMapping implements IPSDeployComponent
{

   /**
    * Construct the object from a source database information.
    *
    * @param datasrcMap The datasource mapping information, it may not be
    * <code>null</code>
    */
   public PSDbmsMapping(PSDatasourceMap datasrcMap)
   {
      if (datasrcMap == null)
         throw new IllegalArgumentException("datasource map may not be null");

      m_datasrcMap = datasrcMap;
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
   public PSDbmsMapping(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Copy ctor.
    * 
    * @param source The mapping from which a shallow copy is constructed, may
    * not be <code>null</code>.
    */
   public PSDbmsMapping(PSDbmsMapping source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      m_datasrcMap = new PSDatasourceMap(source.getDataSourceMap());      
   }

   /**
    * Get the database information of the source.
    *
    * @return The source <code>PSDatasourceMap</code> object, it can never be
    * <code>null</code>.
    */
   public PSDatasourceMap getDataSourceMap()
   {
      return m_datasrcMap;
   }


   
   /**
    * Get the database information of the source.
    *
    * @return The source datasource as a string, it can never be
    * <code>null</code>.
    */
   public String getSourceInfo()
   {
      return m_datasrcMap.getSrc();
   }

   /**
    * Set the source database information.
    *
    * @param srcDataSource The new source datasource information, it may not be
    * <code>null</code>.
    */
   public void setSourceInfo(String srcDataSource)
   {
      if (srcDataSource == null)
         throw new IllegalArgumentException("srcDataSource may not be null");

      m_datasrcMap.setSrc(srcDataSource);
   }

   /**
    * Get the target database information.
    *
    * @return The target info, or <code>null</code> if it has not been set.
    */
   public String getTargetInfo()
   {
      return m_datasrcMap.getTarget();
   }

   /**
    * Set the target database info.
    *
    * @param tgtDatasrc The target datasource info, it may be <code>null</code>
    * to clear the target info.
    */
   public void setTargetInfo(String tgtDatasrc)
   {
      m_datasrcMap.setTarget(tgtDatasrc);
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    %lt;!ELEMENT PSXDbmsMapping (PSXDatasourceMap)
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    * See {@link PSXDatasourceMap#toXml(Document)} for info regarding
    * <code>PSXDatasourceMap</code> format.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(m_datasrcMap.toXml(doc));
      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    *
    * @throws PSUnknownNodeTypeException <code>sourceNode</code> is malformed.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

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

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element srcEl = tree.getNextElement(PSDatasourceMap.XML_NODE_NAME, firstFlags);
      // need to have at least one source element
      if (srcEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDatasourceMap.XML_NODE_NAME);
      }

      m_datasrcMap = new PSDatasourceMap(srcEl);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSDbmsMapping))
         throw new IllegalArgumentException("obj is not be PSDbmsMapping");

      PSDbmsMapping mapObj = (PSDbmsMapping) obj;
      m_datasrcMap = mapObj.m_datasrcMap;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
         return m_datasrcMap.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      if ((obj instanceof PSDbmsMapping))
      {
         PSDbmsMapping obj2 = (PSDbmsMapping) obj;

         if ( m_datasrcMap.equals(obj2.m_datasrcMap) )
           return true;
         return false;
      }
      return false;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDbmsMapping";

   /**
    * The datasource mapping : sourceDataSource <==> targetDataSource
    */
   private PSDatasourceMap m_datasrcMap;
   
}
