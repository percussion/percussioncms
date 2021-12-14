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
package com.percussion.cms.objectstore;


import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;


/**
 * The PSContentTypeVariant represents one row of the CONTENTVARIANTS table.
 * This is a read-only implementation of the PSDbComponent.
 * @deprecated Use the assembly service to load and manipulate variant
 * information
 */
public class PSContentTypeVariant extends PSDbComponent
{
  /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>) object.
    *
    * @param source A valid element that meets the dtd defined in the
    *    description of {@link #toXml(Document)}. Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does
    *    not conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSContentTypeVariant(Element source)
      throws PSUnknownNodeTypeException
   {
      super(source);
      fromXml(source);
   }

   /**
    * Create a content type variant from an assembly template
    * @param template the template, never <code>null</code>
    */
   public PSContentTypeVariant(IPSAssemblyTemplate template) {
      super(new PSKey(new String[] { KEY_VARIANTID }, 
            new int[] { (int) template.getGUID().longValue() }, 
            false));
      if (template == null)
      {
         throw new IllegalArgumentException("template may not be null");
      }
      m_aaType = template.getActiveAssemblyType().ordinal();
      m_assembyUrl = template.getAssemblyUrl();
      m_description = template.getDescription();
      m_locationPrefix = template.getLocationPrefix();
      m_name = template.getName();
      m_outputFormat = template.getOutputFormat().ordinal();
      m_publishWhen = template.getPublishWhen().name();
      m_styleSheetName = template.getStyleSheetPath();
      m_variantSlots = 
         new PSVariantSlotTypeSet(template.getGUID(), template.getSlots());
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      try
      {
         List<IPSNodeDefinition> defs = 
            mgr.findNodeDefinitionsByTemplate(template.getGUID());
         m_contentTypes = new ArrayList<>();
         for(IPSNodeDefinition def : defs)
         {
            m_contentTypes.add(def.getGUID().longValue());
         }
         
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @return aaType value.
    */
   public int getActiveAssemblyType()
   {
      return m_aaType;
   }

   /**
    * @return Variant's assembly url, never <code>null</code> or empty.
    */
   public String getAssemblyUrl()
   {
      return m_assembyUrl;
   }

   /**
    * @return ContentTypeId value.
    * @deprecated
    */
   public int getContentTypeId()
   {
      if (m_contentTypes == null || m_contentTypes.size() == 0)
      {
         return getKeyPartInt(KEY_CONTENTTYPEID, -1);
      }
      else
      {
         return getKeyPartInt(KEY_CONTENTTYPEID, 
               (int) (long) m_contentTypes.get(0));
      }
   }
   
   /**
    * Does this variant support the given content type
    * @param contenttypeid the content type to check for
    * @return <code>true</code> if the type is supported
    */
   public boolean supportsContentType(int contenttypeid)
   {
      if (m_contentTypes == null || m_contentTypes.size() == 0)
      {
         return false;
      }
      return m_contentTypes.contains(new Long(contenttypeid));
   }
   
   /**
    * Retrieve the supported content types
    * @return a list of content types, never <code>null</code> and never empty
    * for a valid template.
    */
   public List<Long> getContentTypes()
   {
      return m_contentTypes;
   }
   
   
   /**
    * @return variant id.
    */
   public int getVariantId()
   {
      return getKeyPartInt(KEY_VARIANTID, -1);
   }
   
   /**
    * @return variantDescription, may be <code>null</code>.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * @return variantDescription, may be <code>null</code>.
    */
   @Override
   public String getDescription()
   {
      return m_description;
   }

   /**
    * @return publishWhen, may be <code>null</code> or empty.
    */
   public String getPublishWhen()
   {
      return m_publishWhen;
   }

   /**
    * @return locationPrefix, may be <code>null</code> or empty.
    */
   public String getLocationPrefix()
   {
      return m_locationPrefix;
   }

   /**
    * @return output format id.
    */
   public int getOutputFormat()
   {
      return m_outputFormat;
   }

   /**
    * @return stylesheet name.
    */
   public String getStyleSheetName()
   {
      return m_styleSheetName;
   }
   
   /**
    * Returns variant slots set.
    * @return variant slots as a PSSlotTypeContentTypeVariantSet,
    * never <code>null</code>, may be <code>empty</code>.
    */
   public PSVariantSlotTypeSet getVariantSlots()
   {
      return m_variantSlots;
   }
      
   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    * <pre>
    * <!ELEMENT PSXContentTypeVariant (PSXKey, VariantDescription, LocationPrefix?, PublishWhen?, StyleSheetName, AssembyUrl, Description?, PSXVariantSlotTypeSet )>
    * <!ELEMENT PSXVariantSlotTypeSet (#PCDATA)>
    * <!ELEMENT Description (#PCDATA)>
    * <!ELEMENT AssembyUrl (#PCDATA)>
    * <!ELEMENT StyleSheetName (#PCDATA)>
    * <!ELEMENT VariantDescription (#PCDATA)>
    * <!ELEMENT LocationPrefix (#PCDATA)>
    * <!ELEMENT PublishWhen (#PCDATA)>
    * <!ELEMENT PSXKey (CONTENTTYPEID, VARIANTID )>
    * <!ELEMENT VARIANTID (#PCDATA)>
    * <!ELEMENT CONTENTTYPEID (#PCDATA)>
    * <!ATTLIST PSXKey needGenerateId (yes | no ) "no">
    * <!ATTLIST PSXKey isPersisted (yes | no ) "yes">
    * <!ATTLIST  PSXContentTypeVariant aaType CDATA #REQUIRED>
    * <!ATTLIST  PSXContentTypeVariant outputFormat CDATA #REQUIRED>
    * </pre>
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   @Override
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = super.toXml(doc);

      root.setAttribute(XML_ATTR_outputFormat, "" + m_outputFormat);
      root.setAttribute(XML_ATTR_aaType, "" + m_aaType);
      
      if (m_assembyUrl!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_AssemblyUrl, m_assembyUrl);
         
      if (m_styleSheetName!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_StyleSheetName, m_styleSheetName);
         
      if (m_name!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_VariantDescription, m_name);
         
      if (m_description!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_Description, m_description);
         
      if (m_publishWhen!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_PublishWhen, m_publishWhen);
         
      if (m_locationPrefix!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_LocationPrefix, m_locationPrefix);
         
      if (m_variantSlots != null)
         m_variantSlots.toXml(doc);
      
      return root;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSCmsComponent#fromXml(org.w3c.dom.Element)
    */
   @Override
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");

      super.fromXml(sourceNode);

      m_outputFormat = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_outputFormat, true);
            
      m_aaType = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_aaType, false);
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      
      Element el = walker.getNextElement(
         XML_ELEM_AssemblyUrl, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_assembyUrl = PSXmlTreeWalker.getElementData(el);
      
      walker.setCurrent(sourceNode);
      
      el = walker.getNextElement(
         XML_ELEM_StyleSheetName,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_styleSheetName = PSXmlTreeWalker.getElementData(el);
      
      walker.setCurrent(sourceNode);
      
      el = walker.getNextElement(
         XML_ELEM_VariantDescription,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_name = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);
      
      el = walker.getNextElement(
         XML_ELEM_Description,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_description = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);

      el = walker.getNextElement(
         XML_ELEM_PublishWhen,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_publishWhen = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);

      el = walker.getNextElement(
         XML_ELEM_LocationPrefix,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_locationPrefix = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);

      // get slot variants collection element from which we create
      // PSVariantSlotTypeSet
      el = walker.getNextElement(
         PSVariantSlotTypeSet.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      if(m_variantSlots == null)
         m_variantSlots = new PSVariantSlotTypeSet();

      if (el != null)
         m_variantSlots.fromXml(el);
   }

   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator,
    *     PSKey)}.
    * Since this is a read-only object, this is a not supported operation.
    * @throws UnsupportedOperationException always.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
      PSKey parent) throws PSCmsException
   {
      throw new UnsupportedOperationException("PSContentTypeVariant is read-only.");
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSContentTypeVariant copy = (PSContentTypeVariant)super.clone();

      copy.m_name = m_name;
      copy.m_styleSheetName = m_styleSheetName;
      copy.m_assembyUrl = m_assembyUrl;
      copy.m_outputFormat = m_outputFormat;
      copy.m_aaType = m_aaType;
      copy.m_description = m_description;
      copy.m_locationPrefix = m_locationPrefix;
      copy.m_publishWhen = m_publishWhen;
      
      copy.m_variantSlots = m_variantSlots; 
   
      return copy;
   }

   /**
    * Override to create our own Key which is {@link PSLocator}.
    */
   protected PSKey createKey(Element el) throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("Source element cannot be null.");

      return new PSKey(el);
   }

   /**
    * Overrides the base class to compare each of the member properties. All
    * string members'omparison is case insensitive.
    * The CONTENTVARIANT table is a variant definition table and as well
    * as a mapping table for Content Types and Variants. The Variantid
    * is used as data and as well key. That is the reason for adding the
    * variantid check in the equals method.
    *
    * @param o The comparee. If null or not an instance of this class,
    *    <code>false</code> is returned.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSContentTypeVariant ))
         return false;

      PSContentTypeVariant type = (PSContentTypeVariant) o;
      if(getVariantId()!=type.getVariantId())
         return false;
      if ( m_aaType!=type.m_aaType)
         return false;
      if (!compare(m_assembyUrl, type.m_assembyUrl))
         return false;
      if (!compare(m_description, type.m_description))
         return false;
      if (!compare(m_publishWhen, type.m_publishWhen))
         return false;
      if (!compare(m_locationPrefix, type.m_locationPrefix))
         return false;
      if (!compare(m_name, type.m_name))
         return false;
      if (m_outputFormat!=type.m_outputFormat)
         return false;
      if (!compare(m_styleSheetName, type.m_styleSheetName))
         return false;
      if (!compare(m_variantSlots, type.m_variantSlots))
         return false;

      return true;
   }

   /**
    * Must be overridden because we overrode equals.
    *
    * @return A value computed by concatenating all of the properties into one
    *    string and taking the hashCode of that. The name is lowercased before
    *    it is concatenated.
    */
   public int hashCode()
   {
      return (
         getVariantId()
            + ("AATYPE" + m_aaType).hashCode()
            + hashString(m_assembyUrl)
            + hashString(m_description)
            + hashString(m_publishWhen)
            + hashString(m_locationPrefix)
            + hashString(m_name)
            + ("OUTPUTFORMAT" + m_outputFormat).hashCode()
            + hashString(m_styleSheetName)
            + m_variantSlots.hashCode());
   }
   
   /**
    * Creates the correct key for this component.
    */
   public static PSKey[] createKeys(int[] variantIds)
   {
      if (variantIds == null || variantIds.length < 1)
         throw new IllegalArgumentException("variantIds may not be null or empty");
      
      PSKey keys[] = new PSKey[variantIds.length];
            
      for (int i = 0; i < variantIds.length; i++)
      {
         if (variantIds[i]< 1)
            throw new IllegalArgumentException("variantIds may not be < 0");
         
         keys[i] = new PSSimpleKey(KEY_VARIANTID, "" + variantIds[i]); 
      }   
      
      return keys;  
   }

   /**
    * Creates the correct key for this component according to the specified
    * content type and variant id. 
    * 
    * @param contentTypeId the content type id. It may be less than
    *    <code>-1</code>, then the variant id must be greater than
    *    <code>0</code>.  
    * @param variantId the variant id. It may be less than
    *    <code>-1</code>, then the content type id must be greater than
    *    <code>0</code>.  
    * 
    * @return the created key, never <code>null</code>.
    */
   public static PSKey createKey(long contentTypeId, int variantId)
   {
      if (contentTypeId < 0 && variantId < 0)
         throw new IllegalArgumentException("contentTypeId and variantId both may not be < 0");

      if(contentTypeId > 0 && variantId > 0)
      {               
         String[] keyNames = {KEY_CONTENTTYPEID, KEY_VARIANTID};
      
         String[] keyValues = {"" + contentTypeId, "" + variantId};
         return new PSKey(keyNames, keyValues, true);
      }
      else if(contentTypeId > 0)
      {
         return new PSSimpleKey(KEY_CONTENTTYPEID, "" + contentTypeId); 
      }
      else
      {
         return new PSSimpleKey(KEY_VARIANTID, "" + variantId); 
      }
   }
   
   /**
    * Constant for variant output form page 
    */
   static public int OUTPUTFORMAT_PAGE = 1;

   /**
    * Constant for variant output form snippet
    */
   static public int OUTPUTFORMAT_SNIPPET = 2;
   
   /**
    * ContentVariants Table Key Name.
    */
   private final static String KEY_CONTENTTYPEID = "CONTENTTYPEID";
   private final static String KEY_VARIANTID = "TEMPLATE_ID";
   
   
   /*
    * All the attributes for content variant variant. 
    */
   private String m_name = null;
   private String m_styleSheetName = null;
   private String m_assembyUrl = null;
   private int m_outputFormat = 0;
   private int m_aaType = 0;
   private String m_description = null;
   private String m_locationPrefix = null;
   private String m_publishWhen = null;
   private List<Long> m_contentTypes = null;
   
   /**
    * Variant Slots collection. Initialized by fromXml,
    * may be <code>null</code>.
    */
   private PSVariantSlotTypeSet m_variantSlots = null;
      
   // Private constants for XML attribute and element names
   private final static String XML_ATTR_outputFormat = "outputFormat";
   private final static String XML_ATTR_aaType = "aaType";
      
   private final static String XML_ELEM_VariantDescription = "VariantDescription";
   private final static String XML_ELEM_StyleSheetName = "StyleSheetName";
   private final static String XML_ELEM_AssemblyUrl = "AssemblyUrl";
   private final static String XML_ELEM_Description = "Description";
   private final static String XML_ELEM_LocationPrefix = "LocationPrefix";
   private final static String XML_ELEM_PublishWhen = "PublishWhen";
}
