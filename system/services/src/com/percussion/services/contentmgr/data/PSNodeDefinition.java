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
package com.percussion.services.contentmgr.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper for content type definitions in Rhythmyx. Additional methods are
 * provided here for Rhythmyx specific information. Most of the JSR-170
 * information is not provided at this time.
 * 
 * @author dougrand
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSNodeDefinition")
@NaturalIdCache
@Table(name = "CONTENTTYPES")
public class PSNodeDefinition implements IPSNodeDefinition
{
   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("variant-guid", PSGuid.class);
   }

   @Id
   @Column(name = "CONTENTTYPEID")
   private Long m_contenttypeid;

   @SuppressWarnings("unused")
   @Version
   @Column(name = "VERSION")
   private Integer m_version = -1;

   @NaturalId
   @Column(name = "CONTENTTYPENAME")
   private String m_name;

   @Basic
   @Column(name = "CONTENTTYPELABEL")
   private String m_label;

   @Basic
   @Column(name = "CONTENTTYPEDESC")
   private String m_description;

   @Basic
   @Column(name = "CONTENTTYPENEWREQUEST")
   private String m_newRequest;

   @Basic
   @Column(name = "CONTENTTYPEQUERYREQUEST")
   private String m_queryRequest;

   @Basic
   @Column(name = "CONTENTTYPEUPDATEREQUEST")
   private String m_updateRequest;

   @Basic
   @Column(name = "OBJECTTYPE")
   private Integer m_objectType;

   @Basic
   @Column(name = "HIDEFROMMENU")
   private Boolean m_hideFromMenu = Boolean.FALSE;

   @OneToMany(targetEntity = PSContentTemplateDesc.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
   @JoinColumn(name = "CONTENTTYPEID")
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "object")
   @Fetch(FetchMode. SUBSELECT)
   private Set<PSContentTemplateDesc> m_cvDescriptors;

   @OneToMany(targetEntity = PSContentTypeWorkflow.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
   @JoinColumn(name = "CONTENTTYPEID")
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "object")
   @Fetch(FetchMode. SUBSELECT)
   private Set<PSContentTypeWorkflow> m_ctWfRels;

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.NodeDefinition#getRequiredPrimaryTypes()
    */
   @IPSXmlSerialization(suppress = true)
   public NodeType[] getRequiredPrimaryTypes()
   {
      return new NodeType[] { getDefaultPrimaryType() };
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.NodeDefinition#getDefaultPrimaryType()
    */
   @IPSXmlSerialization(suppress = true)
   public NodeType getDefaultPrimaryType()
   {
      // Note that this looks up the primary type on demand to avoid
      // the cost
      IPSContentRepository repository =
           PSContentInternalLocator.getLegacyRepository();
      try
      {
         return repository.findNodeType(this);
      }
      catch (NoSuchNodeTypeException e)
      {
         return null;
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.NodeDefinition#allowsSameNameSiblings()
    */
   public boolean allowsSameNameSiblings()
   {
      return false;
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#getDeclaringNodeType()
    */
   @IPSXmlSerialization(suppress = true)
   public NodeType getDeclaringNodeType()
   {
      return getDefaultPrimaryType();
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#getName()
    */
   public String getName()
   {
      if (m_name != null)
         return PSContentUtils.externalizeName(m_name).replace(' ', '_');
      else
         return null;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#getInternalName()
    */
   public String getInternalName()
   {
      return m_name;
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#isAutoCreated()
    */
   public boolean isAutoCreated()
   {
      return false;
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#isMandatory()
    */
   public boolean isMandatory()
   {
      return false;
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#getOnParentVersion()
    */
   @IPSXmlSerialization(suppress = true)
   public int getOnParentVersion()
   {
      throw new UnsupportedOperationException("Not yet supported");
   }

   /**
    * (non-Javadoc)
    * 
    * @see javax.jcr.nodetype.ItemDefinition#isProtected()
    */
   public boolean isProtected()
   {
      return false;
   }

   /**
    * @return Returns the contenttypeid.
    */
   @IPSXmlSerialization(suppress = true)
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_contenttypeid);
   }

   /**
    * @param guid The contenttypeid to set.
    */
   public void setGUID(IPSGuid guid)
   {
      m_contenttypeid = guid.longValue();
   }

   /**
    * Get the raw content type id, required for some operations
    * 
    * @return the raw content type id
    */
   public long getRawContentType()
   {
      return m_contenttypeid;
   }

   /**
    * Get id as long, only used for serialization
    * 
    * @return get the id
    */
   public long getId()
   {
      return m_contenttypeid;
   }

   /**
    * Set the new id, only used for serialization
    * 
    * @param id the new id
    */
   public void setId(long id)
   {
      m_contenttypeid = id;
   }

   /**
    * Get the label, which is the value that is shown in the user interface.
    * 
    * @return Returns the label.
    */
   public String getLabel()
   {
      return m_label;
   }

   /**
    * A new label
    * 
    * @param label The label to set, may be <code>null</code> or empty
    */
   public void setLabel(String label)
   {
      m_label = label;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#getDescription()
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#setDescription(java.lang.String)
    */
   public void setDescription(String description)
   {
      m_description = description;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#getHideFromMenu()
    */
   public Boolean getHideFromMenu()
   {
      return m_hideFromMenu;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#setHideFromMenu(java.lang.Boolean)
    */
   public void setHideFromMenu(Boolean hideFromMenu)
   {
      if (hideFromMenu == null)
      {
         throw new IllegalArgumentException("hideFromMenu may not be null");
      }
      m_hideFromMenu = hideFromMenu;
   }

   /**
    * @return Returns the newRequest.
    */
   public String getNewRequest()
   {
      return m_newRequest;
   }

   /**
    * @param newRequest The newRequest to set.
    */
   public void setNewRequest(String newRequest)
   {
      m_newRequest = newRequest;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#getObjectType()
    */
   public Integer getObjectType()
   {
      return m_objectType;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#setObjectType(java.lang.Integer)
    */
   public void setObjectType(Integer objectType)
   {
      m_objectType = objectType;
   }

   /**
    * @return Returns the queryRequest.
    */
   public String getQueryRequest()
   {
      return m_queryRequest;
   }

   /**
    * @param queryRequest The queryRequest to set.
    */
   public void setQueryRequest(String queryRequest)
   {
      m_queryRequest = queryRequest;
   }

   /**
    * @return Returns the updateRequest.
    */
   public String getUpdateRequest()
   {
      return m_updateRequest;
   }

   /**
    * @param updateRequest The updateRequest to set.
    */
   public void setUpdateRequest(String updateRequest)
   {
      m_updateRequest = updateRequest;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#setName(java.lang.String)
    */
   public void setName(String name)
   {
      m_name = PSContentUtils.internalizeName(name);
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#setInternalName(java.lang.String)
    */
   public void setInternalName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_name = name;
   }

   /**
    * Get the actual descriptors that associate a given content type with the
    * templates. Don't use this method directly, use {@link #getVariantGuids()}
    * and related methods instead.
    * 
    * @return the descriptors, may be empty but not <code>null</code>
    */
   @IPSXmlSerialization(suppress = true)
   public Set<PSContentTemplateDesc> getCvDescriptors()
   {
      return m_cvDescriptors;
   }

   /**
    * Set the descriptors. See {@link #getCvDescriptors()} for more information.
    * 
    * @param cvDescriptors The cvDescriptors to set.
    */
   public void setCvDescriptors(Set<PSContentTemplateDesc> cvDescriptors)
   {
      m_cvDescriptors = cvDescriptors;
   }


   /**
    * Get the actual relationships that associate a given content type with the
    * workflow. Don't use this method directly, use {@link #getWorkflowGuids()}
    * and related methods instead.
    * 
    * @return the contenttype workflow relationships, may be empty but not
    * <code>null</code>
    */
   @IPSXmlSerialization(suppress = true)
   public Set<PSContentTypeWorkflow> getCtWfRels()
   {
      return m_ctWfRels;
   }

   /**
    * Set the content type workflow relations. See {@link #getCtWfRels()} for
    * more information.
    * 
    * @param cTWfRels The cTWfRels to set.
    */
   public void setCtWfRels(Set<PSContentTypeWorkflow> cTWfRels)
   {
      m_ctWfRels = cTWfRels;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      m_version = 0;
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /**
    * Get the associated templates as guids, similar to
    * {@link #getTemplateIds()} but returning guids.
    * 
    * @return a set of guids, never <code>null</code>, unmodifiable set is 
    * returned
    * 
    */
   @IPSXmlSerialization(suppress = true)
   public Set<IPSGuid> getVariantGuids()
   {
      Set<IPSGuid> guids = new HashSet<>();
      if (m_cvDescriptors != null)
      {
         for (PSContentTemplateDesc desc : m_cvDescriptors)
         {
            guids.add(desc.getTemplateId());
         }
      }
      return Collections.unmodifiableSet(guids);
   }

   /**
    * Get the associated workflows as guids.
    * 
    * @return a set of guids, never <code>null</code>, unmodifiable set is 
    * returned
    * 
    */
   @IPSXmlSerialization(suppress = true)
   public Set<IPSGuid> getWorkflowGuids()
   {
      Set<IPSGuid> guids = new HashSet<>();
      if (m_ctWfRels != null)
      {
         for (PSContentTypeWorkflow rel : m_ctWfRels)
         {
            guids.add(rel.getWorkflowId());
         }
      }
      return Collections.unmodifiableSet(guids);
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#addVariantGuid(com.percussion.utils.guid.IPSGuid)
    */
   public void addVariantGuid(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      if (m_cvDescriptors == null)
      {
         m_cvDescriptors = new HashSet<>();
      }
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      
      for (PSContentTemplateDesc desc : m_cvDescriptors)
      {
         if (guid.equals(desc.getTemplateId()))
         {
            return;
         }
      }

      // dont always create a new one, if an association exists, use it
      PSContentTemplateDesc cvDesc = null;
      try
      {
         cvDesc = cmgr.findContentTypeTemplateAssociation(guid, this
               .getGUID());
      }
      catch (RepositoryException e)
      {
      }

      if (cvDesc != null)
         m_cvDescriptors.add(cvDesc);
      else
      {
         // association not found, so add a new one
         PSContentTemplateDesc desc = new PSContentTemplateDesc();
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         desc.setId(gmgr.createGuid(PSTypeEnum.INTERNAL).longValue());
         desc.setContentTypeId(getGUID());
         desc.setTemplateId(guid);
         m_cvDescriptors.add(desc);
      }
   }

   /** (non-Javadoc)
    * @see com.percussion.services.contentmgr.IPSNodeDefinition#removeVariantGuid(com.percussion.utils.guid.IPSGuid)
    */
   public void removeVariantGuid(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      PSContentTemplateDesc found = null;
      for (PSContentTemplateDesc desc : m_cvDescriptors)
      {
         if (guid.equals(desc.getTemplateId()))
         {
            found = desc;
            break;
         }
      }
      if (found != null)
      {
         m_cvDescriptors.remove(found);
      }
   }

   /**
    * Set the version. This method is explicitly not exposed in the interface as
    * there are only limited cases where this needs to be used, such as with web
    * services.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (version==null || version==-1) version = 0;
      else if (version < 0)
         throw new IllegalArgumentException("version must be >= 0");

      m_version = version;
   }

   /**
    * Get the version. This method is explicitly not exposed in the interface as
    * there are only limited cases where this needs to be used, such as with web
    * services.
    * 
    * @return The version, may be <code>null</code> if it has not been set.
    */
   @IPSXmlSerialization(suppress=true)
   public Integer getVersion()
   {
      return m_version;
   }

   /**
    * Method that is used only as part of the internal implementation. Call this
    * method to clear all related descriptors before a deletion.
    * 
    */
   void removeAllVariants()
   {
      m_cvDescriptors.clear();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object arg0)
   {
      EqualsBuilder builder = new EqualsBuilder();

      PSNodeDefinition b = (PSNodeDefinition) arg0;

      return builder.append(getGUID(), b.getGUID()).append(getVariantGuids(),
            b.getVariantGuids()).append(getWorkflowGuids(),
            b.getWorkflowGuids()).append(getDescription(), b.getDescription())
            .append(getHideFromMenu(), b.getHideFromMenu()).append(getName(),
                  b.getName()).append(getLabel(), b.getLabel()).append(
                  getNewRequest(), b.getNewRequest()).append(getObjectType(),
                  b.getObjectType()).append(getQueryRequest(),
                  b.getQueryRequest()).append(getUpdateRequest(),
                  b.getUpdateRequest()).isEquals();
   }

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return m_name != null ? m_name.hashCode() : 0;
   }

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /**
    * Get a string representation of GUIDs of the template associtations
    * 
    * @return set of Guid Strings may be empty never <code>null</code>
    */
   public Set<String> getTemplateIds()
   {
      Set<String> ids = new HashSet<>();
      if (m_cvDescriptors != null && !m_cvDescriptors.isEmpty())
      {
         for (PSContentTemplateDesc desc : m_cvDescriptors)
            ids.add(desc.getTemplateId().toString());
      }
      return ids;
   }

   /**
    * Get a string representation of GUIDs of the workflow associtations
    * 
    * @return set of Guid Strings may be empty never <code>null</code>
    */
   public Set<String> getWorkflowIds()
   {
      Set<String> ids = new HashSet<>();
      if (m_ctWfRels != null && !m_ctWfRels.isEmpty())
      {
         for (PSContentTypeWorkflow ctwf : m_ctWfRels)
            ids.add(ctwf.getWorkflowId().toString());
      }
      return ids;
   }

   /**
    * Add the Template Guid, represented by a string to the template association
    * aka cvDescriptors
    * 
    * @param tmpId the string form of the guid, never <code>null</code>
    */
   public void addTemplateId(String tmpId)
   {
      if (StringUtils.isBlank(tmpId))
         throw new IllegalArgumentException("template guid may not be null");
      addVariantGuid(new PSGuid(tmpId));
   }

   /**
    * Add the given template to the cv_descriptors, if not found dont add it.
    * During de-serialization, if we donot do this, we end up creating a new
    * association and give it a new guid. The other side effect ( again, if we
    * dont do it this way...) is that the table gets corrupted with the deleted
    * associations namely the templateid column is set to <NULL> BAAAAD
    * 
    * @param g the template guid that needs to be added to the collection of
    *           cv_descriptors
    */
   private void addTemplateGuidToCollection(IPSGuid g)
   {
      if (g == null)
         throw new IllegalArgumentException("template guid may not be null");
      PSContentTemplateDesc desc = null;
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      try
      {
         desc = cmgr.findContentTypeTemplateAssociation(g, this.getGUID());
      }
      catch (RepositoryException e)
      {
      }

      if (desc != null)
         m_cvDescriptors.add(desc);
   }

   /**
    * Given a Collection of template ids as strings, sync them with the existing
    * list of template associations for this NodeDef
    * 
    * @param newT set of string template ids never <code>null</code>, may be
    *           empty
    */
   public void mergeTemplateIds(Set<String> newT)
   {
      if (newT.isEmpty())
         return;
      Set<IPSGuid> newTmps = new HashSet<>();
      for (String t : newT)
         newTmps.add(new PSGuid(t));

      // if the current template set is empty
      if (m_cvDescriptors.isEmpty())
      {
         for (IPSGuid guid : newTmps)
         {
            addTemplateGuidToCollection(guid);
         }
         return;
      }
      // get all existing tmp guids associated with this site
      Set<IPSGuid> curTmps = new HashSet<>();
      for (PSContentTemplateDesc desc : m_cvDescriptors)
         curTmps.add(desc.getTemplateId());

      /**
       * 1. commonTmps = intersection of curTmps, newTmps 2. removeTmps =
       * curTmps - newTmps 3. delete removeTmps from curTmps 4. delete
       * commonTmps from newTmps
       */
      Collection common = CollectionUtils.intersection(curTmps, newTmps);
      Collection remove = CollectionUtils.subtract(curTmps, newTmps);
      curTmps.removeAll(remove);
      newTmps.removeAll(common);
      curTmps.addAll(newTmps);
      m_cvDescriptors.clear();

      for (IPSGuid guid : curTmps)
         addTemplateGuidToCollection(guid);
   }
}
