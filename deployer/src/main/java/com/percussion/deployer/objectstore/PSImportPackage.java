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
 * Represents a package included in an import descriptor.  Can contain the 
 * validation results for the package, as well as any other extra package
 * information related to installing a deployable element and its dependencies.
 */
public class PSImportPackage implements IPSDeployComponent
{

   /**
    * Construct an import package with a deployable element.
    * 
    * @param pkg The deployable element, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>pkg</code> is <code>null</code>.
    */
   public PSImportPackage(PSDeployableElement pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("pkg may not be null");
         
      m_pkg = pkg;   
   }

   /**
    * Construct this object from its Xml representation.  
    * 
    * @param src The element containing the Xml format of this object.  See 
    * {@link #toXml(Document)} for more info.  May not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>src</code> is <code>null</code>.
    * @throws PSUnknownNodeTypeException if <code>src</code> is malformed.
    */
   public PSImportPackage(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
   
      fromXml(src);
   }

   /**
    * Get the deployable element contained in this import package.
    * 
    * @return The element, never <code>null</code>.
    */
   public PSDeployableElement getPackage()
   {
      return m_pkg;
   }

   /**
    * Get the validation results if they have been set.
    * 
    * @return The results, may be <code>null</code>.
    */
   public PSValidationResults getValidationResults()
   {
      return m_validationResults;
   }

   /**
    * Set the validation results for this package.
    * 
    * @param results The validation results, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>results</code> is 
    * <code>null</code>.
    */
   public void setValidationResults(PSValidationResults results)
   {
      if (results == null)
         throw new IllegalArgumentException("results may not be null");
      
      m_validationResults = results;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXImportPackge (PSXDeployableElement, PSXValidationResults)>
    * </code></pre>
    * 
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(m_pkg.toXml(doc));
      if (m_validationResults != null)
         root.appendChild(m_validationResults.toXml(doc));

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
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS | 
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
         
      Element pkgEl = tree.getNextElement(PSDeployableElement.XML_NODE_NAME, 
         firstFlags);
      if (pkgEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, 
               PSImportPackage.XML_NODE_NAME);
      }
      m_pkg = new PSDeployableElement(pkgEl);
      
      Element valEl = tree.getNextElement(PSValidationResults.XML_NODE_NAME, 
         nextFlags);
      if (valEl != null)
         m_validationResults = new PSValidationResults(valEl);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      if (!(obj instanceof PSImportPackage))
         throw new IllegalArgumentException("obj wrong type");

      PSImportPackage other = (PSImportPackage)obj;
      
      m_pkg = other.m_pkg;
      m_validationResults = other.m_validationResults;

   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_pkg.hashCode() + (m_validationResults == null ? 0 : 
         m_validationResults.hashCode());
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSImportPackage))
         isEqual = false;
      else
      {
         PSImportPackage other = (PSImportPackage)obj;
         if (!m_pkg.equals(other.m_pkg))
            isEqual = false;
         else if (m_validationResults == null ^ 
            other.m_validationResults == null)
         {
            isEqual = false;
         }
         else if (m_validationResults != null && !m_validationResults.equals(
            other.m_validationResults))
         {
            isEqual = false;
         }
      }
      
      return isEqual;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXImportPackage";
   
   /**
    * The deployable element to be packaged, never <code>null</code> or 
    * modified after ctor.
    */
   PSDeployableElement m_pkg;
   
   /**
    * Validation results for the package set when validated by the server,
    * may be <code>null</code> if this has not been done.
    */
   PSValidationResults m_validationResults = null;
}
