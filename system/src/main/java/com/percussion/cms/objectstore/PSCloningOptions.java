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

import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Object store implementation to the <code>PSXCloningOptions</code> object
 * as defined in schema sys_FolderParameters.xsd.
 */
public class PSCloningOptions extends PSComponent
{
   /**
    * Convenience constructor for copy site subfolders where no site name is
    * required.
    * 
    * @see #PSCloningOptions(int, String, String, int, int, Map) for 
    *    documentation
    */
   public PSCloningOptions(int type, String folderName, int copyOption, 
      int copyContentOption, Map communityMappings)
   {
      this(type, null, null, folderName, copyOption, copyContentOption, 
         communityMappings);
   }

   /**
    * Construct the cloning options for the supplied parameters.
    * 
    * @param type the type of cloning, one of the <code>TYPE_XXX</code> values.
    * @param siteToCopy the name of the site definition being copied, may be 
    *    <code>null</code> or empty.
    * @param siteName the new site name, may be <code>null</code> or empty.
    * @param folderName the new site folder name, not <code>null</code> or 
    *    empty.
    * @param copyOption the option of what should be cloned, one of the 
    *    <code>COPY_XXX</code> values.
    * @param copyContentOption the option how content should be cloned, one
    *    of the <code>COPYCONTENT_XXX</code> values.
    * @param communityMappings a map of communities that need to be changed
    *    from the source to the target, may be <code>null</code> or empty.
    *    Map key and value are expected as <code>Integer</code>, where the
    *    key is the source community id and the value the target community id.
    */
   public PSCloningOptions(int type, String siteToCopy, String siteName, 
      String folderName, int copyOption, int copyContentOption, 
      Map communityMappings)
   {
      if (!isValid(type, ms_typeEnum))
         throw new IllegalArgumentException("type must be one of TYPE_XXX");
      m_type = type;
      
      setSiteToCopy(siteToCopy);
      setSiteName(siteName);
      
      if (folderName == null)
         throw new IllegalArgumentException("folderName cannot be null");
      
      folderName = folderName.trim();
      if (folderName.length() == 0)
         throw new IllegalArgumentException("folderName cannot be empty");
      m_folderName = folderName;

      if (!isValid(copyOption, ms_copyOptionsEnum))
         throw new IllegalArgumentException(
            "copyOption must be one of COPY_XXX");
      m_copyOption = copyOption;

      if (!isValid(copyContentOption, ms_copyContentOptionsEnum))
         throw new IllegalArgumentException(
            "copyContentOption must be one of COPYCONTENT_XXX");
      m_copyContentOption = copyContentOption;
      
      m_communityMappings.clear();
      if (communityMappings != null)
         m_communityMappings.putAll(communityMappings);
   }
   
   /**
    * Constructs the cloning options from its XML representation.
    * 
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList) for parameter 
    *    descriptions.
    */
   public PSCloningOptions(Element source, IPSDocument parent,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(source, parent, parentComponents);
   }
   
   /* (non-Javadoc)
    * @see PSComponent#copyFrom(PSComponent) for documentation.
    */
   public void copyFrom(PSCloningOptions c)
   {
      super.copyFrom(c);
      
      m_type = c.m_type;
      m_siteToCopy = c.m_siteToCopy;
      m_siteName = c.m_siteName;
      m_folderName = c.m_folderName;
      m_copyOption = c.m_copyOption;
      m_copyContentOption = c.m_copyContentOption;
      m_communityMappings = c.m_communityMappings;
      m_siteMappings = c.m_siteMappings;
   }
   
   /* (non-Javadoc)
    * @see IPSComponent#clone() for documentation.
    */
   public Object clone()
   {
      PSCloningOptions clone = (PSCloningOptions) super.clone();
      clone.m_type = m_type;
      clone.m_siteToCopy = m_siteToCopy;
      clone.m_siteName = m_siteName;
      clone.m_folderName = m_folderName;
      clone.m_copyOption = m_copyOption;
      clone.m_copyContentOption = m_copyContentOption;
      clone.m_communityMappings.putAll(m_communityMappings);
      clone.m_siteMappings.putAll(m_siteMappings);
      
      return clone;
   }
   
   /**
    * Must be overridden to properly fulfill the contract.
    *
    * @return a value computed by adding the hash codes of all members.
    */
   public int hashCode()
   {
      int hash = m_type + 
         m_siteToCopy == null ? 0 : m_siteToCopy.hashCode() +
         m_siteName == null ? 0 : m_siteName.hashCode() +
         m_folderName.hashCode() +
         m_copyOption + 
         m_copyContentOption + 
         m_communityMappings.hashCode() +
         m_siteMappings.hashCode();

      return hash;
   }
   
   /**
    * Tests if the supplied object is equal to this one.
    * 
    * @param o the object to test, may be <code>null</code>.
    * @return <code>true</code> if the supplied object is equal to this one,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSCloningOptions))
         return false;

      PSCloningOptions t = (PSCloningOptions) o;
      if (t.m_type != m_type)
         return false;
      if (!compare(t.m_siteToCopy, m_siteToCopy))
         return false;
      if (!compare(t.m_siteName, m_siteName))
         return false;
      if (!compare(t.m_folderName, m_folderName))
         return false;
      if (t.m_copyOption != m_copyOption)
         return false;
      if (t.m_copyContentOption != m_copyContentOption)
         return false;
      if (!t.m_communityMappings.equals(m_communityMappings))
         return false;
      if (!t.m_siteMappings.equals(m_siteMappings))
         return false;

      return true;
   }
   
   /**
    * Get the cloning type.
    * 
    * @return the cloning type, one of the <code>TYPE_XXX</code> values.
    */
   public int getType()
   {
      return m_type;
   }
   
   /**
    * Are these options to clone a site?
    * 
    * @return <code>true</code> if they are <code>false</code> otherwise.
    */
   public boolean isCloneSite()
   {
      return m_type == TYPE_SITE;
   }
   
   /**
    * Are these options to clone a site subfolder?
    * 
    * @return <code>true</code> if they are <code>false</code> otherwise.
    */
   public boolean isCloneSiteSubfolder()
   {
      return m_type == TYPE_SITE_SUBFOLDER;
   }
   
   /**
    * Get the name of the site definition being copied.
    * 
    * @return the name of the site definition being copied, may be 
    *    <code>null</code> or empty.
    */
   public String getSiteToCopy()
   {
      return m_siteToCopy;
   }
   
   /**
    * Set the site definition being copied.
    * 
    * @param siteToCopy the new site definition being copied, may be 
    *    <code>null</code> or empty.
    */
   public void setSiteToCopy(String siteToCopy)
   {
      m_siteToCopy = null;

      if (siteToCopy != null && siteToCopy.trim().length() > 0)
         m_siteToCopy = siteToCopy.trim();
   }
   
   /**
    * Get the name for the new site.
    * 
    * @return the new site name, may be <code>null</code> or empty.
    */
   public String getSiteName()
   {
      return m_siteName;
   }
   
   /**
    * Set the site name.
    * 
    * @param siteName the new site name, may be <code>null</code> or empty.
    */
   public void setSiteName(String siteName)
   {
      m_siteName = null;

      if (siteName != null && siteName.trim().length() > 0)
         m_siteName = siteName.trim();
   }
   
   /**
    * Get the name for the new site folder or site subfolder.
    * 
    * @return the new site folder or site subfolder name, never 
    *    <code>null</code> or empty.
    */
   public String getFolderName()
   {
      return m_folderName;
   }
   
   /**
    * Get the option of what objects should be cloned.
    * 
    * @return the copy option, one of the <code>COPY_XXX</code> values.
    */
   public int getCopyOption()
   {
      return m_copyOption;
   }
   
   /**
    * Is the 'No Content' copy option selected?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isNoContent()
   {
      return m_copyOption == COPY_NO_CONTENT;
   }
   
   /**
    * Is the 'Navigation Content' copy option selected?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isNavigationContent()
   {
      return m_copyOption == COPY_NAVIGATION_CONTENT;
   }
   
   /**
    * Is the 'All Content' copy option selected?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isAllContent()
   {
      return m_copyOption == COPY_ALL_CONTENT;
   }
   
   /**
    * Get the option of how content should be cloned.
    * 
    * @return the copy content option, one of the <code>COPYCONTENT_XXX</code>
    *    values.
    */
   public int getCopyContentOption()
   {
      return m_copyContentOption;
   }
   
   /**
    * Is the 'As Link' copy content option selected?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isCopyContentAsLink()
   {
      return m_copyContentOption == COPYCONTENT_AS_LINK;
   }
   
   /**
    * Is the 'As New Copy' copy content option selected?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isCopyContentAsNewCopy()
   {
      return m_copyContentOption == COPYCONTENT_AS_NEW_COPY;
   }
   
   /**
    * Get the source to target community mappings.
    * 
    * @return the source - target community mappings, never <code>null</code>,
    *    may be empty.
    */
   public Map getCommunityMappings()
   {
      return m_communityMappings;
   }
   
   /**
    * Get the source to target site id mappings.
    * 
    * @return the source to target site id mappings, never <code>null</code>,
    *    may be empty.
    */
   public Map getSiteMappings()
   {
      return m_siteMappings;
   }
   
   /**
    * Add a new source to target site id mapping.
    * 
    * @param source the source site id, not <code>null</code>.
    * @param target the target site id, not <code>null</code>.
    */
   public void addSiteMapping(Integer source, Integer target)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");

      if (target == null)
         throw new IllegalArgumentException("target cannot be null");
      
      m_siteMappings.put(source, target);
   }

   /**
    * Set if the src item workflow should be used if a valid selection.  See
    * {@link #useSrcItemWorkflow()} for details.  Default is <code>false</code>
    * if it has not been set.
    * 
    * @param useSrcWorkflow <code>true</code> to use the src, <code>false</code> otherwise.
    */
   public void setUseSrcItemWorkflow(boolean useSrcWorkflow)
   {
      m_useSrcItemWorkflow = useSrcWorkflow;
   }
   
   /**
    * Determine if the source item's workflow should be used if it is a valid selection.  Default system
    * behavior is to calculate the workflow of the new item from the content type's workflow settings and
    * the new item's community.  A <code>true</code> value returned by this method indicates the source
    * item's workflow should be used as long as it is valid for the content type and community.  If it
    * is not valid, or if this method returns <code>false</code>, the default system behavior should be used.
    * 
    * @return <code>true</code> to use the source item worklfow, <code>false</code> otherwise.
    */
   public boolean useSrcItemWorkflow()
   {
      return m_useSrcItemWorkflow;
   }
   
   /* (non-Javadoc)
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList)
    */
   public void fromXml(Element source, IPSDocument parent,
      ArrayList parentComponents) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      m_type = toInteger(source.getAttribute(TYPE_ATTR), ms_typeNames, 
         TYPE_ATTR);
      
      setSiteToCopy(source.getAttribute(SITE_TO_COPY_ATTR));
      setSiteName(source.getAttribute(SITE_NAME_ATTR));
      
      String folderName = source.getAttribute(FOLDER_NAME_ATTR);
      if (folderName == null || folderName.trim().length() == 0)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            FOLDER_NAME_ATTR,
            folderName
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_folderName = folderName;
      
      m_copyOption = toInteger(source.getAttribute(COPY_OPTION_ATTR), 
         ms_copyOptionNames, COPY_OPTION_ATTR);
      
      m_copyContentOption = toInteger(
         source.getAttribute(COPYCONTENT_OPTION_ATTR), 
         ms_copyContentOptionNames, COPYCONTENT_OPTION_ATTR);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);
      
      walker.setCurrent(source);
      m_communityMappings.clear();
      Element communityMappings = walker.getNextElement(
         COMMUNITY_MAPPINGS_ELEM, true);
      if (communityMappings != null)
      {
         Element mapping = walker.getNextElement(true);
         while (mapping != null)
         {
            int sourceId = 0;
            String src = mapping.getAttribute(SOURCEID_ATTR);
            try
            {
               sourceId = Integer.parseInt(src);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME, SOURCEID_ATTR, src
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            int targetId = 0;
            String tgt = mapping.getAttribute(TARGETID_ATTR);
            try
            {
               targetId = Integer.parseInt(tgt);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME, TARGETID_ATTR, tgt
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            m_communityMappings.put(new Integer(sourceId),
               new Integer(targetId));

            mapping = walker.getNextElement(true);
         }
      }
      
      walker.setCurrent(source);
      m_siteMappings.clear();
      Element siteMappings = walker.getNextElement(SITE_MAPPINGS_ELEM, true);
      if (siteMappings != null)
      {
         Element mapping = walker.getNextElement(true);
         while (mapping != null)
         {
            int sourceId = 0;
            String src = mapping.getAttribute(SOURCEID_ATTR);
            try
            {
               sourceId = Integer.parseInt(src);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME, SOURCEID_ATTR, src
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            int targetId = 0;
            String tgt = mapping.getAttribute(TARGETID_ATTR);
            try
            {
               targetId = Integer.parseInt(tgt);
            }
            catch (NumberFormatException e)
            {
               Object[] args =
               {
                  XML_NODE_NAME, TARGETID_ATTR, tgt
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }

            m_siteMappings.put(new Integer(sourceId),
               new Integer(targetId));

            mapping = walker.getNextElement(true);
         }
      }
   }

   /* (non-Javadoc)
    * @see IPSComponent#toXml(Document)
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(TYPE_ATTR, ms_typeNames[m_type]);
      if (m_siteToCopy != null)
         root.setAttribute(SITE_TO_COPY_ATTR, m_siteToCopy);
      if (m_siteName != null)
         root.setAttribute(SITE_NAME_ATTR, m_siteName);
      root.setAttribute(FOLDER_NAME_ATTR, m_folderName);
      root.setAttribute(COPY_OPTION_ATTR, ms_copyOptionNames[m_copyOption]);
      root.setAttribute(COPYCONTENT_OPTION_ATTR, 
         ms_copyContentOptionNames[m_copyContentOption]);

      if (!m_communityMappings.isEmpty())
      {
         Element communityMappings = doc.createElement(COMMUNITY_MAPPINGS_ELEM);
         root.appendChild(communityMappings);
         
         Iterator sources = m_communityMappings.keySet().iterator();
         while (sources.hasNext())
         {
            Integer source = (Integer) sources.next();
            Integer target = (Integer) m_communityMappings.get(source);
            
            Element mapping = doc.createElement(MAPPING_ELEM);
            mapping.setAttribute(SOURCEID_ATTR, source.toString());
            mapping.setAttribute(TARGETID_ATTR, target.toString());
            communityMappings.appendChild(mapping);
         }
      }

      if (!m_siteMappings.isEmpty())
      {
         Element siteMappings = doc.createElement(SITE_MAPPINGS_ELEM);
         root.appendChild(siteMappings);
         
         Iterator sources = m_siteMappings.keySet().iterator();
         while (sources.hasNext())
         {
            Integer source = (Integer) sources.next();
            Integer target = (Integer) m_siteMappings.get(source);
            
            Element mapping = doc.createElement(MAPPING_ELEM);
            mapping.setAttribute(SOURCEID_ATTR, source.toString());
            mapping.setAttribute(TARGETID_ATTR, target.toString());
            siteMappings.appendChild(mapping);
         }
      }

      return root;
   }
   
   /**
    * Tests if the supplied value is defined in the supplied array.
    * 
    * @param value the value to test.
    * @param validValues an array with all valid values, assumed not
    *    <code>null</code>.
    * @return <code>true</code> if the supplied value is defined in the
    *    provided values array, <code>false</code> otherwise.
    */
   private boolean isValid(int value, int[] validValues)
   {
      boolean isValid = false;
      for (int i=0; i<validValues.length && !isValid; i++)
         isValid = validValues[i] == value;
      
      return isValid;
   }
   
   /**
    * Converts the supplied string value to its integer representation.
    * 
    * @param value the value to convert, may be <code>null</code> or empty.
    * @param validValues an array with all valid values in indexed order, 
    *    assumed not <code>null</code>.
    * @param attrName the attribute name to convert, assumed not 
    *    <code>null</code> or empty.
    * @return the integer representation for the supplied string value.
    * @throws PSUnknownNodeTypeException if the supplied is <code>null</code>
    *    or does not have a known value.
    */
   private int toInteger(String value, String[] validValues, String attrName)
      throws PSUnknownNodeTypeException
   {
      if (value != null)
      {
         for (int i=0; i<validValues.length; i++)
         {
            if (validValues[i].equals(value))
               return i;
         }
      }
      
      Object[] args =
      {
         XML_NODE_NAME,
         attrName,
         value
      };
      throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
   }
   
   /**
    * The type of cloning options, either this is to clone a site or to clone
    * a site subfolder. Initialized in constructor, always one of the 
    * <code>TYPE_XXX</code> values, never changed after that.
    */
   private int m_type = -1;
   
   /**
    * The type constant used if this cloning options are setup to clone a
    * site. 
    */
   public static final int TYPE_SITE = 0;
   
   /**
    * The type constant used if this cloning options are setup to clone a
    * site subfolder. 
    */
   public static final int TYPE_SITE_SUBFOLDER = 1;
   
   /**
    * An array with all valid type values.
    */
   public static final int[] ms_typeEnum =
   {
      TYPE_SITE, TYPE_SITE_SUBFOLDER
   };
   
   /**
    * An array that maps the type value to its XML representaion.
    */
   public static final String[] ms_typeNames =
   {
      "site", "siteSubfolder"
   };
   
   /**
    * Holds the name of the site definition being copied, initialized in 
    * constructor. May be <code>null</code> or empty in which case no site 
    * definition is copied.
    */
   private String m_siteToCopy = null;
   
   /**
    * Holds the name of the new site, initialized in constructor. May be
    * <code>null</code> or empty in which case no site definition is copied.
    */
   private String m_siteName = null;
   
   /**
    * Holds the name of the new site folder or site subfolder, initialized 
    * while constructed, never <code>null</code> or empty after that.
    */
   private String m_folderName = null;
   
   /**
    * This option defines what content is to be cloned. Initialized during
    * construction, always one of the <code>COPY_XXX</code> values after
    * that.
    */
   private int m_copyOption = -1;
   
   /**
    * The copy option constant used if no content will be cloned.
    */
   public static final int COPY_NO_CONTENT = 0;
   
   /**
    * The copy option constant used if only navigation content will be cloned.
    */
   public static final int COPY_NAVIGATION_CONTENT = 1;
   
   /**
    * The copy option constant used if all content will be cloned.
    */
   public static final int COPY_ALL_CONTENT = 2;
   
   /**
    * An array with all valid copy option values.
    */
   public static final int[] ms_copyOptionsEnum =
   {
      COPY_NO_CONTENT, COPY_NAVIGATION_CONTENT, COPY_ALL_CONTENT
   };
   
   /**
    * An array that maps the copy option value to its XML representaion.
    */
   public static final String[] ms_copyOptionNames =
   {
      "noContent", "navigationContent", "allContent"
   };
   
   /**
    * This option defines how content is to be cloned. Initialized during
    * construction, always one of the <code>COPYCONTENT_XXX</code> values after
    * that.
    */
   private int m_copyContentOption = -1;

   /**
    * The copy content option constant used if content will be cloned as link.
    */
   public static final int COPYCONTENT_AS_LINK = 0;

   /**
    * The copy content option constant used if content will be cloned as new 
    * copy.
    */
   public static final int COPYCONTENT_AS_NEW_COPY = 1;

   /**
    * An array with all valid copy content option values.
    */
   public static final int[] ms_copyContentOptionsEnum =
   {
      COPYCONTENT_AS_LINK, COPYCONTENT_AS_NEW_COPY
   };

   /**
    * An array that maps the copy content option value to its XML representaion.
    */
   public static final String[] ms_copyContentOptionNames =
   {
      "asLink", "asNewCopy"
   };
   
   /**
    * This maps source community ids to target community ids. Initialized during
    * construction, never <code>null</code>, may be empty. The map key is
    * an <code>Integer</code> representing the source community id while the 
    * map value is an <code>Integer</code> representing the target community id.
    * All folders and content that is cloned as new copy will be created for
    * the target mapping found in this map. If no mapping is found the original
    * community is used.
    */
   private Map m_communityMappings = new HashMap();
   
   /**
    * Maps source site ids as <code>Integer</code> to target site ids as 
    * <code>Integer</code>. Initialized to an empty map, updated through
    * {@link #addSiteMapping(Integer, Integer)}.
    */
   private Map m_siteMappings = new HashMap();
   
   /**
    * Indicates if source items workflow should be used as long as it is valid.  Transient
    * value that is not persisted.
    */
   private transient boolean m_useSrcItemWorkflow = false;
   
   /**
    * The XML document node name.
    */
   public static final String XML_NODE_NAME = "PSXCloningOptions";
   
   // XML element and attribute names
   private static final String TYPE_ATTR = "type";
   private static final String SITE_TO_COPY_ATTR = "siteToCopy";
   private static final String SITE_NAME_ATTR = "siteName";
   private static final String FOLDER_NAME_ATTR = "folderName";
   private static final String COPY_OPTION_ATTR = "copyOption";
   private static final String COPYCONTENT_OPTION_ATTR = "copyContentOption";
   private static final String COMMUNITY_MAPPINGS_ELEM = "CommunityMappings";
   private static final String SITE_MAPPINGS_ELEM = "SiteMappings";
   private static final String MAPPING_ELEM = "Mapping";
   private static final String SOURCEID_ATTR = "sourceId";
   private static final String TARGETID_ATTR = "targetId";
}
