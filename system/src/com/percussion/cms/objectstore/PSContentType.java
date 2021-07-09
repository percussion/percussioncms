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

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;


/**
 * An object representation of a row in the CONTENTTYPES table. This object
 * is immutable. Note that 2 of the columns that are outdated are not
 * supported by this object.
 * <p>This is not in the objectstore because we want implementors to access
 * this information via the PSItemDefinition.
 */
public class PSContentType extends PSDbComponent
{
   /**
    * Creates an object representing a row in the CONTENTTYPES table. No
    * validation on the actual values is performed; i.e., it is not validated
    * that the id really exists, that the name is unique, or that the editorUrl
    * points to a real editor. These validations are performed by higher level
    * code. The following descriptions assume that that valid data is supplied.
    *
    * @param typeId A unique (across content types) numeric identifier for an
    *    item definition.
    *
    * @param name A unique (across content types) textual identifier for an
    *    item definition. Never <code>null</code> or empty.
    *    
    * @param label A descriptive label that should be shown in the UI for this
    *    item definition. If <code>null</code> or empty this defaults to the
    *    name.
    *
    * @param description An optional message that describes what this type is
    *    used for. May be <code>null</code> or empty.
    *
    * @param editorUrl A partial Url to access the html editor associated with
    *    this type. Of the form "../<appname>/<resource>". Back-slashes are
    *    normalized to /. Never <code>null</code> or empty.
    *
    * @param hideFromMenu A flag that indicates whether this type should be
    *    shown in the CA interface. (The presence of this property needs to be
    *    reviewed.)
    *
    * @param objectType An object type id, which point to a row in the object
    *    table, PSX_OBJECTS.
    */
   public PSContentType(int typeId, String name, String label, 
      String description, String editorUrl, boolean hideFromMenu, 
      int objectType)
   {
      super(createKey(typeId));
      
      if (null == name || name.trim().length() == 0)
         throw new IllegalArgumentException("non-empty name must be supplied");
      if (StringUtils.isBlank(label))
      {
         label = name;
      }
      if (null == editorUrl || editorUrl.trim().length() == 0)
      {
         throw new IllegalArgumentException(
               "non-empty editorUrl must be supplied");
      }
      else
      {
         //verify url format matches what we say below
         if (!verifyUrlFormat(editorUrl))
         {
            throw new IllegalArgumentException(
               "incorrect editorUrl format, expected ../appname/resource, got "
               + editorUrl);
         }
      }

      m_name = name;
      m_label = label;
      m_description = null == description ? "" : description;
      m_queryRequest = editorUrl.replace('\\', '/');
      m_hideFromMenu = hideFromMenu;
      m_objectType = objectType;
   }

   /**
    * See multi-paramed ctor for a description.
    * The source must conform to this dtd:
    * <p>The values found must conform to the descriptions in the other ctor.
    *
    * @param source An xml fragment that conforms to the dtd in the description.
    *    Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if expected nodes/attributes are not
    *    found or have invalid values.
    */
   public PSContentType( Element source )
      throws PSUnknownNodeTypeException
   {
      super(source);
      
      fromXml(source);
   }
   
   /**
    * The value of the CONTENTTYPEID column.
    *
    * @return The typeId supplied in the ctor.
    */
   public int getTypeId()
   {
      return (int) getKeyPartLong(KEY_CONTENTTYPEID, -1);
   }

   /**
    * The value of the CONTENTTYPENAME column.
    *
    * @return The name supplied in the ctor. Never <code>null</code> or
    *    empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * The value of the CONTENTTYPELABEL column.
    *
    * @return The name supplied in the ctor. Never <code>null</code> or
    *    empty.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * The value of the OBJECTTYPE column.
    *
    * @return The objectType supplied in the ctor.
    */
   public int getObjectType()
   {
      return m_objectType;
   }

   /**
    * The value of the CONTENTTYPEDESC column.
    *
    * @return The description supplied in the ctor. Maybe empty, never <code>
    *    null</code>.
    */
   @Override
   public String getDescription()
   {
      return m_description;
   }

   /**
    * The value of the CONTENTTYPEQUERYREQUEST column.
    *
    * @return The queryRequest URL supplied in the ctor. Never <code>null</code>
    * or empty. The format will be '../appname/resource'.
    */
   public String getEditorUrl()
   {
      return m_queryRequest;
   }


   /**
    * The value of the HIDEFROMMENU column.
    *
    * @return The hideFromMenu value supplied in the ctor.
    */
   public boolean isHiddenFromMenu()
   {
      return m_hideFromMenu;
   }
   
   
   /**
    * Returns a value of the respective column from the DB table.
    * @return may be <code>null</code> or <code>empty</code>.
    */
   public String getQueryRequest()
   {
      return m_queryRequest;
   }
   
   /**
    * <pre>
    * <!ELEMENT PSXContentType (PSXKey, NewRequest?, QueryRequest?, UpdateRequest?, Description? )>
    * <!ELEMENT Description (#PCDATA)>
    * <!ELEMENT UpdateRequest (#PCDATA)>
    * <!ELEMENT QueryRequest (#PCDATA)>
    * <!ELEMENT NewRequest (#PCDATA)>
    * <!ELEMENT CONTENTTYPEID (#PCDATA)>
    * <!ELEMENT PSXKey (CONTENTTYPEID )>
    * <!ATTLIST PSXKey needGenerateId (yes | no ) "no">
    * <!ATTLIST PSXKey isPersisted (yes | no ) "yes">
    * <!ATTLIST PSXContentType hideFromMenu (true | false ) "false">
    * <!ATTLIST  PSXContentType objectType CDATA #REQUIRED>
    * <!ATTLIST  PSXContentType name CDATA #REQUIRED>
    * <!ATTLIST  PSXContentType label CDATA #IMPLIED>
    * </pre> 
    */
   @Override
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = super.toXml(doc);
      
      root.setAttribute(XML_ATTR_name, m_name);
      root.setAttribute(XML_ATTR_label, m_label);
      root.setAttribute(XML_ATTR_hideFromMenu, m_hideFromMenu ? "1" : "0");
      root.setAttribute(XML_ATTR_objectType, "" + m_objectType);

      if (m_newRequest!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_NewRequest, m_newRequest);
         
      if (m_queryRequest!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_QueryRequest, m_queryRequest);
         
      if (m_updateRequest!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_UpdateRequest, m_updateRequest);
      
      if (m_description!=null)
         PSXmlDocumentBuilder.addElement(doc, root,
         XML_ELEM_Description, m_description);
         
      return root;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSCmsComponent#fromXml(org.w3c.dom.Element)
    */   
   @Override
   @SuppressWarnings("deprecation")
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");

      super.fromXml(sourceNode);
      
      m_name = PSXMLDomUtil.checkAttribute(sourceNode,
         XML_ATTR_name, true);      
      
      m_label = sourceNode.hasAttribute(XML_ATTR_label) 
         ? PSXMLDomUtil.checkAttribute(sourceNode, XML_ATTR_label, false)
         : m_name; // if no label attribute then label will be the
                   // same as the name.
         
      m_hideFromMenu = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_hideFromMenu, true) == 1;
      
      m_objectType = PSXMLDomUtil.checkAttributeInt(sourceNode,
         XML_ATTR_objectType, true);
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      
      Element el = walker.getNextElement(
         XML_ELEM_NewRequest, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_newRequest = PSXmlTreeWalker.getElementData(el);
      
      walker.setCurrent(sourceNode);
         
      el = walker.getNextElement(
         XML_ELEM_QueryRequest,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      if (el!=null)
         m_queryRequest = PSXmlTreeWalker.getElementData(el);
         
      if ( !verifyUrlFormat(m_queryRequest) )
      {
         Object[] args = { getNodeName(), XML_ELEM_QueryRequest, m_queryRequest};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args );
      }
      
      walker.setCurrent(sourceNode);

      el = walker.getNextElement(
         XML_ELEM_UpdateRequest,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_updateRequest = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);
         
      el = walker.getNextElement(
         XML_ELEM_Description,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (el!=null)
         m_description = PSXmlTreeWalker.getElementData(el);
         
      walker.setCurrent(sourceNode);
      
      el = walker.getNextElement(
         PSContentTypeVariantSet.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
   }
   
   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator,
    *     PSKey)}.
    * Since this is a read-only object, this is a not supported operation.
    * @throws UnsupportedOperationException always.
    */
   @Override
   @SuppressWarnings("unused")
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
      PSKey parent) throws PSCmsException
   {
      throw new UnsupportedOperationException("PSContentType is read-only.");
   }
  
   /**
    * Override to create our own Key which is {@link PSLocator}.
    */
   @Override
   protected PSKey createKey(Element el) throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("Source element cannot be null.");

      return new PSKey(el);
   }
   
   /**
    * Creates the correct key for this component.
    */
   public static PSKey[] createKeys(int[] contentTypeIds)
   {
      if (contentTypeIds == null || contentTypeIds.length < 1)
         throw new IllegalArgumentException("contentTypeIds may not be null or empty");

      PSKey keys[] = new PSKey[contentTypeIds.length];
      
      for (int i = 0; i < contentTypeIds.length; i++)
      {
         if (contentTypeIds[i] == 0)
            throw new IllegalArgumentException("contentTypeIds may not be = 0");
   
         keys[i] = new PSSimpleKey(KEY_CONTENTTYPEID, "" + contentTypeIds[i]); 
      }   

      return keys;  
   }

   /**
    * Creates the correct key for this component.
    */
   public static PSKey createKey(int contentTypeId)
   {
      return new PSSimpleKey(KEY_CONTENTTYPEID, "" + contentTypeId); 
   }
 

   /**
    * Overrides the base class to compare each of the member properties. All
    * members except the name are compared for exact matches. The name is
    * compared case insensitive.
    *
    * @param o The comparee. If null or not an instance of this class,
    *    <code>false</code> is returned.
    *
    * @return <code>true</code> if all members are equal as defined above,
    *    otherwise <code>false</code> is returned.
    */
   @Override
   public boolean equals( Object o )
   {
      if ( !(o instanceof PSContentType ))
         return false;

      PSContentType type = (PSContentType) o;

      if ( !m_queryRequest.equals(type.m_queryRequest))
         return false;
      if (!m_description.equals(type.m_description))
         return false;
      if (!m_name.equalsIgnoreCase(type.m_name))
         return false;

      return m_hideFromMenu == type.m_hideFromMenu;
   }

   /**
    * Must be overridden because we overrode equals.
    *
    * @return A value computed by concatenating all of the properties into one
    *    string and taking the hashCode of that. The name is lowercased before
    *    it is concatenated.
    */
   @Override
   public int hashCode()
   {
      return (m_description + m_queryRequest +
         m_hideFromMenu + getTypeId() + m_name.toLowerCase()).hashCode();
   }
   
   

   /**
    * Verifies that the supplied string has the following format:
    * <p>..\text\text<p>
    * <p>Either \ or / may appear as separators.
    *
    * @param editorUrl The text to check. May be <code>null</code>.
    *
    * @return If supplied text is <code>null</code> or it doesn't
    *    match the form above, <code>false</code> is returned. Otherwise,
    *    <code>true</code> is returned.
    */
   private boolean verifyUrlFormat( String editorUrl )
   {
      if ( null == editorUrl )
         return false;
      String normalized = editorUrl.replace('\\', '/');
      if ( !normalized.startsWith(CE_URL_PREFIX))
         return false;
      normalized = normalized.substring(3);
      int pos = normalized.indexOf("/");
      if ( pos < 0 )
         return false;
      normalized = normalized.substring(pos+1);
      pos = normalized.indexOf("/");
      if ( pos >= 0 )
         return false;
      return true;
   }
   
   /**
    * Get the name of the app from the request url
    * 
    * @param requestUrl The ce request url, assumed not <code>null</code> or 
    * empty and in the format "../<appName>/<resourceName>.html"
    * 
    * @return The app name, never <code>null</code> or empty.
    */
   public static String getAppName(String requestUrl)
   {
      String appName = StringUtils.substringAfter(
         requestUrl.replace('\\', '/'), CE_URL_PREFIX);
      appName = StringUtils.substringBefore(appName, "/");
      
      if (StringUtils.isBlank(appName))
         throw new IllegalArgumentException("invalid content editor url: " + 
            requestUrl);
      
      return appName;
   }

   /**
    * Creates the standard request url from the content type name.
    * 
    * @param name The content type name, may not be <code>null</code> or empty.
    * 
    * @return The request url, never <code>null</code> or empty, see
    * {@link #getAppName(String)} for the format used.
    */
   public static String createRequestUrl(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      return CE_URL_PREFIX + createAppName(name) + "/" + name + ".html";
   }
   
   /**
    * The request url is **always** of the form "../psx_ceXXXX/XXXX.html"
    * @param url the request url string from which the appname has to be
    * extracted.
    * @return <code>null</code> if no pattern is found
    */
   public static String getAppNameFromRequestUrl(String url)
   {
      int slash = 0x2F;
      int firstSlash = url.indexOf(slash);
      if ( firstSlash < 0 )
         return null;
      
      int secondSlash = url.indexOf(slash, firstSlash+1);
      if ( secondSlash > firstSlash )
         return url.substring(firstSlash+1, secondSlash);
      return null;
   }
   
   /**
    * Construct the standard application name from the supplied content type
    * name.
    * 
    * @param name The content type name, may not be <code>null</code> or empty.
    * 
    * @return The app name, never <code>null</code> or empty.
    */
   public static String createAppName(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      return CE_APP_PREFIX + name;
   }
   
   /**
    * See {@link #PSContentType(int, String, String, String, String, boolean, int) ctor}
    * for a description.
    */
   private int m_objectType;


   /**
    * See {@link #PSContentType(int,String, String,String,String,boolean, int) ctor}
    * for a description. Never <code>null</code> or empty.
    */
   private String m_name;
   
   /**
    * See {@link #PSContentType(int,String, String,String,String,boolean, int) ctor}
    * for a description. May be <code>null</code> or empty.
    */
   private String m_label;


   /**
    * See {@link #PSContentType(int,String, String,String,String,boolean, int) ctor}
    * for a description. Never <code>null</code> or empty.
    */
   private String m_description;

   
   /**
    * 
    */
   private String m_newRequest;
   
   /**
    * See {@link #PSContentType(int,String, String,String,String,boolean, int) ctor}
    * for a description. Never <code>null</code> or empty.
    */
   private String m_queryRequest;
   
   /**
    * 
    */
   private String m_updateRequest;
   
   /**
    * See {@link #PSContentType(int,String, String,String,String,boolean, int) ctor}
    * for a description. Never <code>null</code> or empty.
    */
   private boolean m_hideFromMenu;
   
   /**
    * Constant for the leading "../" url prefix.
    */
   public static final String CE_URL_PREFIX = "../";
   
   public final static String KEY_CONTENTTYPEID = "CONTENTTYPEID";
   
   public static final String CE_APP_PREFIX = "psx_ce";
   
   /*
    * Private XML element and attribute names. 
    */
   private final static String XML_ELEM_NewRequest = "NewRequest";
   private final static String XML_ELEM_QueryRequest = "QueryRequest";
   private final static String XML_ELEM_UpdateRequest = "UpdateRequest";
   private final static String XML_ELEM_Description = "Description";
   private final static String XML_ATTR_hideFromMenu = "hideFromMenu";
   private final static String XML_ATTR_objectType = "objectType";
   private final static String XML_ATTR_name = "name";
   private final static String XML_ATTR_label = "label";
   
}
