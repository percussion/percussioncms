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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
* A private object to hold the mapping information. Initially, the 
* sourceDataSource is valid, and if a target datasource is mapped then this
* tuple can be used. 
*/


public class PSDatasourceMap implements IPSDeployComponent
{
   
   public PSDatasourceMap(String src, String tgt)
   {
      m_srcDataSource = src;
      m_tgtDataSource = tgt;
   }
   
   public PSDatasourceMap(Element src) throws PSUnknownNodeTypeException
   {
      fromXml(src);
   }

   
   /**
    * Copy ctor
    * 
    * @param source The source from which a shallow copy is made, may not be
    * <code>null</code>.
    */
   public PSDatasourceMap(PSDatasourceMap source)
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      copyFrom(source);
   }

   
   /**
    * set the sourceDataSource
    * @return may be <code>null</code>
    */
   public void setSrc(String datasrc)
   {
      m_srcDataSource = datasrc;
   }

   
   /**
    * get the sourceDataSource
    * @return may be <code>null</code>
    */
   public String getSrc()
   {
      return m_srcDataSource;
   }
  
   
   /**
    * set the targetDataSource
    * @return may be <code>null</code>
    */
   public void setTarget(String datasrc)
   {
      m_tgtDataSource = datasrc;
   }

   /**
    * get the targetDataSource
    * @return may be <code>null</code>
    */
   public String getTarget()
   {
      return m_tgtDataSource;
   }
   
   /** 
    * A convenience method which will return either the sourceDataSource or
    * targetDataSource depending on the index from the tabledata.
    * @param ix
    * @return
    */
   public String getColumnData(int ix)
   {
      if ( ix == SOURCE_INDEX )
         return getSrc();
      else if ( ix == TARGET_INDEX )
         return getTarget();
      return null; 
   }

   /**
    * Serialize this object's state to its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#toXml(Element)} for more info on method
    * signature.
    */

   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element   root = doc.createElement(XML_NODE_NAME);
      
      root.setAttribute(XML_ATTR_SOURCE_DATASRC, m_srcDataSource);
      root.setAttribute(XML_ATTR_TARGET_DATASRC, m_tgtDataSource);

      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #fromXml(Document)} for format of XML.  See 
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature, like this:
    * Get the <PSXDataSourceMap source=""  target="" />
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
      m_srcDataSource = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
            XML_ATTR_SOURCE_DATASRC);
      String tgtStr = sourceNode.getAttribute(XML_ATTR_TARGET_DATASRC);
      m_tgtDataSource = "";
      if ( tgtStr != null && tgtStr.length() > 0 )
         m_tgtDataSource = tgtStr;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSDatasourceMap)) return false;
      PSDatasourceMap that = (PSDatasourceMap) o;
      return Objects.equals(m_srcDataSource, that.m_srcDataSource) &&
              Objects.equals(m_tgtDataSource, that.m_tgtDataSource);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_srcDataSource, m_tgtDataSource);
   }

   /**
    * Creates a new instance of this object, performing a shallow copy of all
    * members.
    *
    * @param obj The object from which to copy values.
    *
    * @throws IllegalArgumentException if the supplied object is
    * <code>null</code> or of the wrong type.
    */
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      PSDatasourceMap src = (PSDatasourceMap)obj;
      
      m_srcDataSource = src.getSrc();
      m_tgtDataSource = src.getTarget();
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDatasourceMap";

   /**
    * The source datasource name as the attribute of the XML root node.
    */
   private static final String XML_ATTR_SOURCE_DATASRC = "source";
   /**
    * The target datasource name as the attribute of the XML root node.
    */
   private static final String XML_ATTR_TARGET_DATASRC = "target";
   
   /**
    * The source datasource as a string, initially <code>null</code>
    */
   private String m_srcDataSource = null;
   
   /**
    * Target datasource as a string, initially <code>null</code>
    */
   private String m_tgtDataSource = null;;

   private static final int SOURCE_INDEX = 0;
   private static final int TARGET_INDEX = 1;

}
