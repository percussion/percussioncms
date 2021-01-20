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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.i18n.PSI18nUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.relationship.data.PSRelationshipData;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSComponentSummary contains some item information that is from a row in
 * the CONTENTSTATUS table. The value range may be different for different item
 * types.
 * <p>
 * Still supported is loading this through the component processor, but that use
 * is deprecated.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSComponentSummary")
@Table(name = "CONTENTSTATUS")
@NamedQueries(
{
      @NamedQuery(name = "summary.findPublicOrCurrent", query = "select m_contentId, m_currRevision, m_publicRevision"
            + " from PSComponentSummary where m_contentId in (:ids)"),
      @NamedQuery(name = "summary.loadComponentSummaries", query = " from PSComponentSummary where m_contentId in (:ids)")})
@DynamicInsert(true)
@DynamicUpdate(true)
public class PSComponentSummary extends PSDbComponent implements Serializable
{
   /**
    * Serial id identifies versions of serialized data
    */
   private static final long serialVersionUID = 1L;

   // TODO Make this class not a subclass of dbcomponent

   /**
    * Creates an instance from the input parameters. This CTOR should only be
    * used for Unit Tests that need to create a PSComponentSummary.
    *
    * @param contentId The content id, it may not be less than <code>0</code>.
    *
    * @param currRevision The current revision number, value of the
    *           CURRENTREVISION column.
    *
    * @param tipRevision The tip revision number, , value of the TIPREVISION
    *           column.
    *
    * @param editRevision The edit revision number, , value of the EDITREVISION
    *           column.
    *
    * @param objectType The type of the component. It must be either
    *           <code>TYPE_FOLDER</code> or <code>TYPE_ITEM</code>. It is
    *           the value of OBJECTTYPE column.
    *
    * @param name The name of the component. Never <code>null</code> or empty.
    *           It is the value of TITLE column.
    *
    * @param permissions access mask for determining the level of access for the
    *           user accessing this component. This should be used if the object
    *           is of type "folder". For other object types, specify a value of
    *           "-1". For objects of type "folder" this must be a non-negative
    *           value.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public PSComponentSummary(int contentId, int currRevision, int tipRevision,
         int editRevision, int objectType, String name, long contentTypeId,
         int permissions) {
      super(createKey(contentId, currRevision));

      if (contentId < 0)
         throw new IllegalArgumentException("contentId may not be < 0");
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      validateType(objectType);

      if (objectType == TYPE_FOLDER)
      {
         if (permissions < 0)
            throw new IllegalArgumentException(
                  "invalid permissions for folder : contentId = " + contentId);

         m_permissions = new PSFolderPermissions(permissions);
      }

      m_contentId = contentId;
      m_currRevision = currRevision;
      m_tipRevision = tipRevision;
      m_editRevision = editRevision;
      m_name = name;
      m_objectType = objectType;
      m_contentTypeId = contentTypeId;
   }

   /**
    * Validates a given type.
    *
    * @param type The to be validated type. It must be one of the
    *           <code>TYPE_XXX</code> values.
    *
    * @throws IllegalArgumentException if the type is invalid.
    */
   public static void validateType(int type)
   {
      if ((type != TYPE_FOLDER) && (type != TYPE_ITEM))
         throw new IllegalArgumentException("type must be either "
               + TYPE_NAMES[TYPE_FOLDER] + " or " + TYPE_NAMES[TYPE_ITEM]);
   }

   /**
    * No arg ctor for hibernate
    */
   public PSComponentSummary() {
      super(createKey(0, -1)); // Provide erzatz locator, fixed in loader
   }

   /**
    * Creates an instance from a previously serialized (using <code>toXml
    * </code>)
    * object.
    *
    * @param source A valid element that meets the dtd defined in the
    *           description of {@link #toXml(Document)}. Never
    *           <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException If the supplied source element does not
    *            conform to the dtd defined in the <code>fromXml<code> method.
    */
   public PSComponentSummary(Element source) throws PSUnknownNodeTypeException {
      super(source);
      fromXml(source);
   }

   /**
    * Get the locator that contains current revision number.
    *
    * @return The locator, never <code>null</code>.
    */
   public PSLocator getCurrentLocator()
   {
      if (m_currRevision == null)
         m_currRevision = new Integer(0);

      if (m_currentLocator == null)
         m_currentLocator = new PSLocator(m_contentId, m_currRevision);

      return m_currentLocator;
   }

   /**
    * @return the public revision or current revision depending on the state of
    *         the item
    */
   public int getPublicOrCurrentRevision()
   {
      if (getPublicRevision() == -1)
         return getCurrentLocator().getRevision();
      else
         return getPublicRevision();
   }

   /**
    * If the user is non-empty, then see if the item is checked out to the user
    * and possibly return the edit locator, otherwise return the current
    * revision.
    * @param user the user name, may be empty or <code>null</code>
    * @return the edit or current revision
    */
   public int getAAViewableRevision(String user)
   {
      if (StringUtils.isNotBlank(user) && user.equals(m_checkoutUserName))
      {
         return m_editRevision;
      }
      else
      {
         return m_currRevision;
      }
   }

   /**
    * @return the last public revision. It is <code>-1</code> if there is no
    *         public revision yet.
    */
   public int getPublicRevision()
   {
      if (m_publicRevision == null)
         return (int) -1;
      else
         return m_publicRevision.intValue();
   }

   /**
    * Get the locator that contains tip revision number.
    *
    * @return The locator, never <code>null</code>.
    */
   public PSLocator getTipLocator()
   {
      if (m_tipLocator == null)
         m_tipLocator = new PSLocator(m_contentId, m_tipRevision);

      return m_tipLocator;
   }

   /**
    * Get the locator that contains edit revision number.
    *
    * @return The locator, never <code>null</code>.
    */
   public PSLocator getEditLocator()
   {
      if (m_editLocator == null)
         m_editLocator = new PSLocator(m_contentId, m_editRevision);

      return m_editLocator;
   }

   /**
    * Gets the locator for the revision to which edits should be applied (the
    * edit revision if the item is checked out, otherwise the current revision).
    *
    * @return The locator described above, never <code>null</code>.
    *    The revision of the locator will be the edit revision if item is
    *    checked out, current revision otherwise.
    */
   public PSLocator getHeadLocator()
   {
      if (m_editRevision > 0)
         return getEditLocator();
      else
         return getCurrentLocator();
   }

   /**
    * Get the object type of the component.
    *
    * @return the object type of the component. It is one of the TYPE_xxx
    *         values.
    *
    */
   public int getType()
   {
      return m_objectType;
   }

   /**
    * Determines whether the object is of TYPE_FOLDER or not.
    *
    * @return <code>true</code> if the object is a folder; otherwise return
    *         <code>false</code>.
    */
   public boolean isFolder()
   {
      return (m_objectType == TYPE_FOLDER);
   }

   /**
    * Determines whether the object is of TYPE_ITEM or not.
    *
    * @return <code>true</code> if the object is a folder; otherwise return
    *         <code>false</code>.
    */
   public boolean isItem()
   {
      return (m_objectType == TYPE_ITEM);
   }

   /**
    * Get the name of the component.
    *
    * @return The name supplied in the ctor, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get the contentID for use in the PSRemoteAgent.getItemHistory()
    *
    * @return the contentid.
    */
   public int getContentId()
   {
      return m_contentId;
   }

   /**
    * Set the name of the component.
    *
    * @param name the new name, may not be <code>null</code> or empty.
    */
   public void setName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
   }

   /**
    * Get the content type id of the component.
    */
   public long getContentTypeId()
   {
      return m_contentTypeId;
   }

   /**
    * Gets the GUID-representation of the content type of the component.
    * @return a GUID-representation of the content type of the component, never
    * <code>null</code>.
    */
   public IPSGuid getContentTypeGUID()
   {
      return new PSGuid(PSTypeEnum.NODEDEF, m_contentTypeId);
   }

   /**
    * @return Locale of the Item, Never <code>null</code> or empty.
    */
   public String getLocale()
   {
      return m_locale;
   }

   /**
    * @return Item's checkout user name. May be empty.
    */
   public String getCheckoutUserName()
   {
      return StringUtils.defaultString(m_checkoutUserName);
   }

   /**
    * @return Contentid of the clone parent. -1 if this is not a clone of any
    *         item.
    */
   public int getClonedParentContentId()
   {
      return m_clonedParentContentId;
   }

   /**
    * @return CommunityId of the item. A valid communityid > 0. -1 if the item
    *         is visible to all communities.
    */
   public int getCommunityId()
   {
      return m_communityId;
   }

   /**
    * @return Item's creattion date. May be <code>null</code>.
    */
   public Date getContentCreatedDate()
   {
      return m_contentCreatedDate;
   }

   /**
    * @return Item's start date. May be <code>null</code>.
    */
   public Date getContentStartDate()
   {
      return m_contentStartDate;
   }

   /**
    * @return Item's expiry date. May be <code>null</code>.
    */
   public Date getContentExpiryDate()
   {
      return m_contentExpiryDate;
   }

   /**
    * @return Item's last modfied date. May be <code>null</code>.
    */
   public Date getContentLastModifiedDate()
   {
      return m_contentLastModifiedDate;
   }

   /**
    * @return Item's last modifed user name. May be <code>null</code>.
    */
   public String getContentLastModifier()
   {
      return m_contentLastModifier;
   }

   /**
    * @return Item's current workflow state id. Greater than 0 for a
    *         workflowable object.
    */
   public int getContentStateId()
   {
      return m_contentStateId;
   }

   /**
    * @return Item's workflow id. Greater than 0 for a workflowable object.
    */
   public int getWorkflowAppId()
   {
      return m_workflowAppId;
   }

   /**
    * @return content aging time in seconds. -1 if never set.
    */
   public int getContentAgingTime()
   {
      return m_contentAgingTime;
   }

   /**
    * @return Content creator's name. Never <code>null</code> or empty.
    */
   public String getContentCreatedBy()
   {
      return m_contentCreatedBy;
   }

   /**
    * @return Content publish path. May be <code>null</code> or empty.
    */
   public String getContentPathName()
   {
      return m_contentPathName;
   }

   /**
    * @return Content publish date. May be <code>null</code>.
    */
   public Date getContentPublishDate()
   {
      return m_contentPublishDate;
   }

   /**
    * @return Content post date. May be <code>null</code>.
    */
   public Date getContentPostDate()
   {
      return m_contentPostDate;
   }

   /**
    * @return Content post date timezone. May be <code>null</code>.
    */
   public String getContentPostDateTz()
   {
      return m_contentPostDateTz;
   }

   /**
    * @return Content path suffix. May be <code>null</code> or empty.
    */
   public String getContentSuffix()
   {
      return m_contentSuffix;
   }

   /**
    * @return Content next aging date. May be <code>null</code>.
    */
   public Date getNextAgingDate()
   {
      return m_nextAgingDate;
   }

   /**
    * @return Next aging transition id. -1 if not applicable.
    */
   public int getNextAgingTransition()
   {
      return m_nextAgingTransition;
   }

   /**
    * @return Aging reminder date. May be <code>null</code>.
    */
   public Date getReminderDate()
   {
      return m_reminderDate;
   }

   /**
    * @return Repeated aging transition date. May be <code>null</code>.
    */
   public Date getRepeatedAgingTransStartDate()
   {
      return m_repeatedAgingTransStartDate;
   }

   /**
    * @return <code>true</code> if the revsion is locked in he current state,
    *         <code>false</code> otherwise.
    */
   public boolean isRevisionLock()
   {
      return m_revisionLock != null && m_revisionLock == 'Y';
   }

   /**
    * @return Current state entered date. May be <code>null</code>.
    */
   public Date getStateEnteredDate()
   {
      return m_stateEnteredDate;
   }

   /**
    * Returns a <code>PSObjectPermissions</code> object which encapsulates an
    * access mask. This mask determines the level of access for the user
    * accessing this component. Currently this mask is relevant only for
    * "folders".
    *
    * @return the permissions set on the component encapsulated by this object,
    *         never <code>null</code> if the object is of type "folder",
    *         always <code>null</code> otherwise.
    */
   public PSObjectPermissions getPermissions()
   {
      return m_permissions;
   }


   /**
    * Get the parent folder information for this component summary. The parent
    * folder information is loaded lazily, so do not call this method outside
    * of a service method.
    *
    * @return the parent folder info, never <code>null</code> but may be empty.
    */
   public Set<PSRelationshipData> getParentFolderRelationships()
   {
      return parentFolders;
   }

   public boolean equals(Object o)
   {
      if (!(o instanceof PSComponentSummary))
         return false;
      PSComponentSummary obj2 = (PSComponentSummary) o;
      return new EqualsBuilder().append(m_name, obj2.m_name).append(
            m_contentId, obj2.m_contentId).append(m_currRevision,
            obj2.m_currRevision).append(m_tipRevision, obj2.m_tipRevision)
            .append(m_editRevision, obj2.m_editRevision).append(m_objectType,
                  obj2.m_objectType).append(m_contentTypeId,
                  obj2.m_contentTypeId).append(m_locale, obj2.m_locale).append(
                  m_checkoutUserName, obj2.m_checkoutUserName).append(
                  m_clonedParentContentId, obj2.m_clonedParentContentId)
            .append(m_communityId, obj2.m_communityId).append(
                  m_contentCreatedDate, obj2.m_contentCreatedDate).append(
                  m_contentExpiryDate, obj2.m_contentExpiryDate).append(
                  m_contentLastModifiedDate, obj2.m_contentLastModifiedDate)
            .append(m_contentLastModifier, obj2.m_contentLastModifier).append(
                  m_contentStartDate, obj2.m_contentStartDate).append(
                  m_contentStateId, obj2.m_contentStateId).append(
                  m_workflowAppId, obj2.m_workflowAppId).append(
                  m_nextAgingDate, obj2.m_nextAgingDate).append(
                  m_nextAgingTransition, obj2.m_nextAgingTransition).append(
                  m_stateEnteredDate, obj2.m_stateEnteredDate).append(
                  m_revisionLock, obj2.m_revisionLock).append(m_reminderDate,
                  obj2.m_reminderDate).append(m_repeatedAgingTransStartDate,
                  obj2.m_repeatedAgingTransStartDate).append(
                  m_contentCreatedBy, obj2.m_contentCreatedBy).append(
                  m_contentAgingTime, obj2.m_contentAgingTime).append(
                  m_contentPublishDate, obj2.m_contentPublishDate).append(
                  m_contentPathName, obj2.m_contentPathName).append(
                  m_contentSuffix, obj2.m_contentSuffix).isEquals();
   }

   public int hashCode()
   {
      return new HashCodeBuilder().append(m_name).append(m_contentId).append(
            m_currRevision).append(m_tipRevision).append(m_editRevision)
            .append(m_objectType).append(m_contentTypeId).append(m_locale)
            .append(m_checkoutUserName).append(m_clonedParentContentId) //
            .append(m_communityId).append(m_contentCreatedDate) //
            .append(m_contentExpiryDate).append(m_contentLastModifiedDate)
            .append(m_contentLastModifier).append(m_contentStartDate) //
            .append(m_contentStateId).append(m_workflowAppId) //
            .append(m_nextAgingDate).append(m_nextAgingTransition) //
            .append(m_stateEnteredDate).append(m_revisionLock) //
            .append(m_reminderDate).append(m_repeatedAgingTransStartDate)
            .append(m_contentCreatedBy).append(m_contentAgingTime) //
            .append(m_contentPublishDate).append(m_contentPathName) //
            .append(m_contentSuffix).toHashCode();
   }







   /**
    * @param contentId The contentId to set.
    */
   public void setContentId(Integer contentId)
   {
      m_contentId = contentId;
   }

   /**
    * @param checkoutUserName The checkoutUserName to set.
    */
   public void setCheckoutUserName(String checkoutUserName)
   {
      m_checkoutUserName = StringUtils.defaultString(checkoutUserName);
   }

   /**
    * @param clonedParentContentId The clonedParentContentId to set.
    */
   public void setClonedParentContentId(int clonedParentContentId)
   {
      m_clonedParentContentId = clonedParentContentId;
   }

   /**
    * @param communityId The communityId to set.
    */
   public void setCommunityId(int communityId)
   {
      m_communityId = communityId;
   }

   /**
    * @param contentAgingTime The contentAgingTime to set.
    */
   public void setContentAgingTime(int contentAgingTime)
   {
      m_contentAgingTime = contentAgingTime;
   }

   /**
    * @param contentCreatedBy The contentCreatedBy to set.
    */
   public void setContentCreatedBy(String contentCreatedBy)
   {
      m_contentCreatedBy = contentCreatedBy;
   }

   /**
    * @param contentCreatedDate The contentCreatedDate to set.
    */
   public void setContentCreatedDate(Date contentCreatedDate)
   {
      m_contentCreatedDate = contentCreatedDate;
   }

   /**
    * @param contentExpiryDate The contentExpiryDate to set.
    */
   public void setContentExpiryDate(Date contentExpiryDate)
   {
      m_contentExpiryDate = contentExpiryDate;
   }

   /**
    * @param contentLastModifiedDate The contentLastModifiedDate to set.
    */
   public void setContentLastModifiedDate(Date contentLastModifiedDate)
   {
      m_contentLastModifiedDate = contentLastModifiedDate;
   }

   /**
    * @param contentLastModifier The contentLastModifier to set.
    */
   public void setContentLastModifier(String contentLastModifier)
   {
      m_contentLastModifier = contentLastModifier;
   }

   /**
    * @param contentPathName The contentPathName to set.
    */
   public void setContentPathName(String contentPathName)
   {
      m_contentPathName = contentPathName;
   }

   /**
    * @param contentPublishDate The contentPublishDate to set.
    */
   public void setContentPublishDate(Date contentPublishDate)
   {
      m_contentPublishDate = contentPublishDate;
   }

   /**
    * @param contentPostDate The contentPostDate to set.
    */
   public void setContentPostDate(Date contentPostDate)
   {
      m_contentPostDate = contentPostDate;
   }

   /**
    * @param timezone The contentPostDate timezone to set.
    */
   public void setContentPostDateTz(String timezone)
   {
      m_contentPostDateTz = timezone;
   }

   /**
    * @param contentStartDate The contentStartDate to set.
    */
   public void setContentStartDate(Date contentStartDate)
   {
      m_contentStartDate = contentStartDate;
   }

   /**
    * @param contentStateId The contentStateId to set.
    */
   public void setContentStateId(int contentStateId)
   {
      m_contentStateId = contentStateId;
   }

   /**
    * @param contentSuffix The contentSuffix to set.
    */
   public void setContentSuffix(String contentSuffix)
   {
      m_contentSuffix = contentSuffix;
   }

   /**
    * @param contentTypeId The contentTypeId to set.
    */
   public void setContentTypeId(long contentTypeId)
   {
      m_contentTypeId = contentTypeId;
   }

   /**
    * @param currentLocator The currentLocator to set.
    */
   public void setCurrentLocator(PSLocator currentLocator)
   {
      m_currentLocator = currentLocator;
   }

   /**
    * @param locale The locale to set.
    */
   public void setLocale(String locale)
   {
      m_locale = locale;
   }

   /**
    * @param nextAgingDate The nextAgingDate to set.
    */
   public void setNextAgingDate(Date nextAgingDate)
   {
      m_nextAgingDate = nextAgingDate;
   }

   /**
    * @param nextAgingTransition The nextAgingTransition to set.
    */
   public void setNextAgingTransition(int nextAgingTransition)
   {
      m_nextAgingTransition = nextAgingTransition;
   }

   /**
    * @param reminderDate The reminderDate to set.
    */
   public void setReminderDate(Date reminderDate)
   {
      m_reminderDate = reminderDate;
   }

   /**
    * @param repeatedAgingTransStartDate The repeatedAgingTransStartDate to set.
    */
   public void setRepeatedAgingTransStartDate(Date repeatedAgingTransStartDate)
   {
      m_repeatedAgingTransStartDate = repeatedAgingTransStartDate;
   }

   /**
    * @param revisionLock The revisionLock to set.
    */
   public void setRevisionLock(boolean revisionLock)
   {
      m_revisionLock = revisionLock ? 'Y' : 'N';
   }

   /**
    * @param stateEnteredDate The stateEnteredDate to set.
    */
   public void setStateEnteredDate(Date stateEnteredDate)
   {
      m_stateEnteredDate = stateEnteredDate;
   }

   /**
    * @param workflowAppId The workflowAppId to set.
    */
   public void setWorkflowAppId(int workflowAppId)
   {
      m_workflowAppId = workflowAppId;
   }

   /**
    * @return Returns the objectType.
    */
   public int getObjectType()
   {
      return m_objectType;
   }

   /**
    * @param objectType The objectType to set.
    */
   public void setObjectType(int objectType)
   {
      m_objectType = objectType;
   }

   /**
    * @param permissions The permissions to set.
    */
   public void setPermissions(PSFolderPermissions permissions)
   {
      m_permissions = permissions;
   }

   /**
    * @return Returns the version.
    */
   public Integer getVersion()
   {
      return m_version;
   }

   /**
    * @param version The version to set.
    */
   public void setVersion(Integer version)
   {
      m_version = version;
   }

   /**
    * Serializes this object into an xml element that can be attached to the
    * supplied document. It will conform to the following dtd:
    *
    * <pre>
    *
    *
    *        &lt;!ELEMENT PSXComponentSummary EMPTY&gt;
    *        &lt;!ATTLIST PSXComponentSummary
    *           contentId CDATA #REQUIRED
    *           contentTypeId CDATA #REQUIRED
    *           currRevision CDATA #REQUIRED
    *           editRevision CDATA #REQUIRED
    *           name CDATA #REQUIRED
    *           objectType CDATA #REQUIRED
    *           tipRevision CDATA #REQUIRED
    *           clonedParent CDATA #IMPLIED
    *           communityId CDATA #IMPLIED
    *           contentAgingTime CDATA #IMPLIED
    *           contentCheckoutUserName CDATA #IMPLIED
    *           contentCreatedBy CDATA #IMPLIED
    *           contentCreatedDate CDATA #IMPLIED
    *           contentExpiryDate CDATA #IMPLIED
    *           contentLastModifiedDate CDATA #IMPLIED
    *           contentLastModifier CDATA #IMPLIED
    *           contentPathName CDATA #IMPLIED
    *           contentPublishDate CDATA #IMPLIED
    *           contentStartDate CDATA #IMPLIED
    *           contentStateId CDATA #IMPLIED
    *           contentSuffix CDATA #IMPLIED
    *           lastTransitionDate CDATA #IMPLIED
    *           locale CDATA #IMPLIED
    *           nextAgingDate CDATA #IMPLIED
    *           nextAgingTransition CDATA #IMPLIED
    *           permissions CDATA #IMPLIED
    *           reminderDate CDATA #IMPLIED
    *           repeatedAgingTransStartDate CDATA #IMPLIED
    *           revisionLock CDATA #IMPLIED
    *           state CDATA #IMPLIED
    *           stateEnteredDate CDATA #IMPLIED
    *           workflowAppId CDATA #IMPLIED
    *           &gt;
    *
    *
    * </pre>
    *
    * @param doc Used to generate the element. Never <code>null</code>.
    *
    * @return the generated element, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (null == doc)
         throw new IllegalArgumentException("doc must be supplied");

      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TYPE, "" + m_objectType);
      root.setAttribute(XML_ATTR_CONTENTTYPE_ID, longformat(m_contentTypeId));
      root.setAttribute(XML_ATTR_CONTENT_ID, intformat(m_contentId));
      root.setAttribute(XML_ATTR_HIB_VERSION, intformat(m_version));
      root.setAttribute(XML_ATTR_CURR_REV, intformat(m_currRevision));
      root.setAttribute(XML_ATTR_TIP_REV, intformat(m_tipRevision));
      root.setAttribute(XML_ATTR_EDIT_REV, intformat(m_editRevision));
      if (m_publicRevision != null)
         root.setAttribute(XML_ATTR_PUBLIC_REV, intformat(m_publicRevision));
      root.setAttribute(XML_ATTR_LOCALE, m_locale);
      root.setAttribute(XML_ATTR_CLONEDPARENT,
            intformat(m_clonedParentContentId));
      root.setAttribute(XML_ATTR_COMMUNITYID, intformat(m_communityId));
      root.setAttribute(XML_ATTR_CONTENTCHECKOUTUSERNAME, m_checkoutUserName);
      root.setAttribute(XML_ATTR_CONTENTCREATEDDATE,
            dateformat(m_contentCreatedDate));
      root.setAttribute(XML_ATTR_CONTENTEXPIRYDATE,
            dateformat(m_contentExpiryDate));
      root.setAttribute(XML_ATTR_CONTENTLASTMODIFIEDDATE,
            dateformat(m_contentLastModifiedDate));
      root.setAttribute(XML_ATTR_CONTENTLASTMODIFIER, m_contentLastModifier);
      root.setAttribute(XML_ATTR_CONTENTSTARTDATE,
            dateformat(m_contentStartDate));
      root.setAttribute(XML_ATTR_CONTENTSTATEID, intformat(m_contentStateId));
      root.setAttribute(XML_ATTR_WORKFLOWAPPID, intformat(m_workflowAppId));
      root.setAttribute(XML_ATTR_NEXTAGINGDATE, dateformat(m_nextAgingDate));
      root.setAttribute(XML_ATTR_CONTENTPUBLISHDATE,
            dateformat(m_contentPublishDate));
      root.setAttribute(XML_ATTR_CONTENTPOSTDATE,
            dateformat(m_contentPostDate));
      root.setAttribute(XML_ATTR_CONTENTPOSTDATETZ, m_contentPostDateTz);
      root.setAttribute(XML_ATTR_REMINDERDATE, dateformat(m_reminderDate));
      root.setAttribute(XML_ATTR_REPEATEDAGINGTRANSSTARTDATE,
            dateformat(m_repeatedAgingTransStartDate));
      root.setAttribute(XML_ATTR_STATEENTEREDDATE,
            dateformat(m_stateEnteredDate));
      root.setAttribute(XML_ATTR_NEXTAGINGTRANSITION,
            intformat(m_nextAgingTransition));
      root.setAttribute(XML_ATTR_REVISIONLOCK, isRevisionLock() ? "Y" : "N");
      root.setAttribute(XML_ATTR_CONTENTAGINGTIME,
            intformat(m_contentAgingTime));
      root.setAttribute(XML_ATTR_CONTENTCREATEDBY, m_contentCreatedBy);
      root.setAttribute(XML_ATTR_CONTENTPATHNAME, m_contentPathName);
      root.setAttribute(XML_ATTR_CONTENTSUFFIX, m_contentSuffix);

      if (m_permissions != null)
         root.setAttribute(XML_ATTR_PERMISSIONS, ""
               + m_permissions.getPermissions());

      return root;
   }

   // Use an accurate date format for interchange, this format
   // is compatible with the old application
   static final DateFormat FMT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

   /**
    * Format a date for the XML output
    *
    * @param date to format, may be <code>null</code>
    * @return the formatted date or an empty string if the date is
    *         <code>null</code>
    */
   private String dateformat(Date date)
   {
      if (date == null)
      {
         return "";
      }
      else
      {
         return FMT.format(date);
      }
   }

   /**
    * Format an Integer for toXML
    *
    * @param x the field to format
    * @return the integer value or zero if the value is <code>null</code>
    */
   private String intformat(Integer x)
   {
      if (x == null)
      {
         return "0";
      }
      else
      {
         return x.toString();
      }
   }

   /**
    * Format a Long for toXML
    *
    * @param x the field to format
    * @return the integer value or zero if the value is <code>null</code>
    */
   private String longformat(Long x)
   {
      if (x == null)
      {
         return "0";
      }
      else
      {
         return x.toString();
      }
   }

   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (null == sourceNode)
         throw new IllegalArgumentException("sourceNode must be supplied");

      super.fromXml(sourceNode);

      // Title
      m_name = PSXMLDomUtil.checkAttribute(sourceNode, XML_ATTR_NAME, true);

      // Object Type
      m_objectType = PSXMLDomUtil.checkAttributeInt(sourceNode, XML_ATTR_TYPE,
            true);

      // Content typeid
      m_contentTypeId = PSXMLDomUtil.checkAttributeLong(sourceNode,
            XML_ATTR_CONTENTTYPE_ID, true);

      // Contentid
      m_contentId = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_CONTENT_ID, true);

      // Hibernate version
      m_version = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_HIB_VERSION, false);

      // Current Revision
      m_currRevision = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_CURR_REV, true);

      // Tip revision
      m_tipRevision = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_TIP_REV, true);

      // Edit revision
      m_editRevision = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_EDIT_REV, true);

      int publicRevision = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_PUBLIC_REV, false);
      m_publicRevision = (publicRevision == -1) ? null : publicRevision;

      // Locale string
      m_locale = PSXMLDomUtil
            .checkAttribute(sourceNode, XML_ATTR_LOCALE, false);
      if (m_locale.length() < 1)
         m_locale = PSI18nUtils.DEFAULT_LANG;

      // Checkout User Name
      m_checkoutUserName = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTCHECKOUTUSERNAME, false);

      // Cloned parent contentid
      m_clonedParentContentId = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_CLONEDPARENT, false).trim());

      // Communityid
      m_communityId = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_COMMUNITYID, false).trim());

      // Created Date
      m_contentCreatedDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTCREATEDDATE, false);

      // Expiry Date
      m_contentExpiryDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTEXPIRYDATE, false);

      // Last Modfied Date
      m_contentLastModifiedDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTLASTMODIFIEDDATE, false);

      // Last modifer name
      m_contentLastModifier = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTLASTMODIFIER, false);

      // Content Start Date
      m_contentStartDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTSTARTDATE, false);

      // Current Stateid
      m_contentStateId = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_CONTENTSTATEID, false).trim());

      // Workflowid
      m_workflowAppId = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_WORKFLOWAPPID, false).trim());

      // Revision locked?
      setRevisionLock(PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_REVISIONLOCK, false).trim().equalsIgnoreCase("Y"));

      // Creator's name
      m_contentCreatedBy = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTCREATEDBY, false);

      // Content Path Name
      m_contentPathName = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTPATHNAME, false);

      // Content Suffix
      m_contentSuffix = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTSUFFIX, false);

      // Next Aging Date
      m_nextAgingDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_NEXTAGINGDATE, false);

      // Content Publish Date
      m_contentPublishDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTPUBLISHDATE, false);

      // Content Post Date
      m_contentPostDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_CONTENTPOSTDATE, false);

      // Content Path Name timezone
      m_contentPostDateTz = PSXMLDomUtil.checkAttribute(sourceNode,
            XML_ATTR_CONTENTPOSTDATETZ, false);

      // Reminder Date
      m_reminderDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_REMINDERDATE, false);

      // State Entry Date
      m_stateEnteredDate = PSXMLDomUtil.checkAttributeDate(sourceNode,
            XML_ATTR_STATEENTEREDDATE, false);

      // Repeating Aging Transition Date
      m_repeatedAgingTransStartDate = PSXMLDomUtil.checkAttributeDate(
            sourceNode, XML_ATTR_REPEATEDAGINGTRANSSTARTDATE, false);

      // Aging time
      m_contentAgingTime = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_CONTENTAGINGTIME, false).trim());

      // Aging Transitionid
      m_nextAgingTransition = parseIntegerOrNull(PSXMLDomUtil.checkAttribute(
            sourceNode, XML_ATTR_NEXTAGINGTRANSITION, false).trim());

      boolean permissionsRequired = false;
      if (m_objectType == TYPE_FOLDER)
         permissionsRequired = true;

      int permissions = PSXMLDomUtil.checkAttributeInt(sourceNode,
            XML_ATTR_PERMISSIONS, permissionsRequired);
      if (permissionsRequired)
         m_permissions = new PSFolderPermissions(permissions);
   }

   /**
    * Returns Integer parsed from the string. Returns <code>null</code> if
    * string is empty or equals to "0".
    */
   private Integer parseIntegerOrNull(String str)
   {
      return StringUtils.isBlank(str) || Integer.parseInt(str) == 0
            ? null
            : Integer.parseInt(str);
   }

   /**
    * See {@link IPSDbComponent#toDbXml(Document, Element, IPSKeyGenerator,
    * PSKey)}. Since this is a read-only object, this is a not supported
    * operation.
    *
    * @throws UnsupportedOperationException always.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent) throws PSCmsException
   {
      throw new UnsupportedOperationException(
            "PSComponentSummary is read-only.");
   }

   public Object clone()
   {
      PSComponentSummary copy = (PSComponentSummary) super.clone();

      copy.m_currentLocator = (PSLocator) getCurrentLocator().clone();
      copy.m_editLocator = (PSLocator) getEditLocator().clone();
      copy.m_permissions = m_permissions == null
            ? null
            : (PSFolderPermissions) m_permissions.clone();

      return copy;
   }



   @Override
public String toString()
{
    return "PSComponentSummary [m_contentId=" + m_contentId + ", m_version=" + m_version + ", m_currRevision="
            + m_currRevision + ", m_tipRevision=" + m_tipRevision + ", m_editRevision=" + m_editRevision
            + ", m_publicRevision=" + m_publicRevision + ", m_currentLocator=" + m_currentLocator + ", m_editLocator="
            + m_editLocator + ", m_tipLocator=" + m_tipLocator + ", m_name=" + m_name + ", m_objectType=" + m_objectType
            + ", m_locale=" + m_locale + ", m_clonedParentContentId=" + m_clonedParentContentId + ", m_communityId="
            + m_communityId + ", m_checkoutUserName=" + m_checkoutUserName + ", m_contentCreatedDate="
            + m_contentCreatedDate + ", m_contentExpiryDate=" + m_contentExpiryDate + ", m_contentLastModifiedDate="
            + m_contentLastModifiedDate + ", m_contentLastModifier=" + m_contentLastModifier + ", m_contentStartDate="
            + m_contentStartDate + ", m_contentStateId=" + m_contentStateId + ", m_workflowAppId=" + m_workflowAppId
            + ", m_nextAgingDate=" + m_nextAgingDate + ", m_contentCreatedBy=" + m_contentCreatedBy
            + ", m_contentAgingTime=" + m_contentAgingTime + ", m_contentPublishDate=" + m_contentPublishDate
            + ", m_contentPostDate=" + m_contentPostDate + ", m_contentPostDateTz=" + m_contentPostDateTz
            + ", m_contentPathName=" + m_contentPathName + ", m_contentSuffix=" + m_contentSuffix + ", m_revisionLock="
            + m_revisionLock + ", m_reminderDate=" + m_reminderDate + ", m_stateEnteredDate=" + m_stateEnteredDate
            + ", m_nextAgingTransition=" + m_nextAgingTransition + ", m_repeatedAgingTransStartDate="
            + m_repeatedAgingTransStartDate + ", m_contentTypeId=" + m_contentTypeId + ", parentFolders="
            + parentFolders + ", m_permissions=" + m_permissions + "]";
}

/**
    * Creates the correct key for this component. The PSDbComponent createKey is
    * not used because we need a PSLocator rather than a generic PSKey.
    */
   public static PSKey createKey(int contentid, int revisionid)
   {
      PSKey key = new PSLocator(contentid, revisionid);

      return key;
   }

   /**
    * Override to create our own Key which is {@link PSLocator}.
    */
   protected PSKey createKey(Element el) throws PSUnknownNodeTypeException
   {
      if (el == null)
         throw new IllegalArgumentException("Source element cannot be null.");

      return new PSLocator(el);
   }

   /**
    * The content id, init by ctor, never modified after that
    */
   @Id
   @Column(name = "CONTENTID")
   private Integer m_contentId;

   /**
    * The row version, used by hibernate to detect stale instances. A
    * <code>null</code> value means that this is a new object rather than an
    * update.
    */
   @Version
   @Column(name = "HIB_VER")
   private Integer m_version = null;

   /**
    * The current revision number, init by ctor, never modified after that
    */
   @Basic
   @Column(name = "CURRENTREVISION")
   private Integer m_currRevision;

   public Integer getCurrRevision()
   {
      return m_currRevision;
   }

   public void setCurrRevision(Integer currRevision)
   {
      m_currentLocator = new PSLocator(m_contentId, currRevision);
      m_currRevision = currRevision;
   }

   public Integer getTipRevision()
   {
      return m_tipRevision;
   }

   public void setTipRevision(Integer tipRevision)
   {
      m_tipLocator = new PSLocator(m_contentId, tipRevision);
      m_tipRevision = tipRevision;
   }

   public Integer getEditRevision()
   {
      return m_editRevision;
   }

   public void setEditRevision(Integer editRevision)
   {
      m_editLocator = new PSLocator(m_contentId, editRevision);
      m_editRevision = editRevision;
   }

   public void setPublicRevision(Integer publicRevision)
   {
      m_publicRevision = publicRevision;
   }

   /**
    * The tip revision number, init by ctor, never modified after that.
    */
   @Basic
   @Column(name = "TIPREVISION")
   private Integer m_tipRevision;

   /**
    * The edit revision number, init by ctor, never modified after that
    */
   @Basic
   @Column(name = "EDITREVISION")
   private Integer m_editRevision;

   /**
    * The last public revision number.
    */
   @Basic
   @Column(name = "PUBLIC_REVISION")
   private Integer m_publicRevision = null;

   /**
    * Current locator,
    *
    * @see {@link #getTipLocator()} for descrption. It is (lazy) initialized by
    *      getCurrentLocator(), never <code>null</code> after that.
    */
   @Transient
   private PSLocator m_currentLocator = null;

   /**
    * Edit locator,
    *
    * @see {@link #getEditLocator()} for descrption. It is (lazy) initialized by
    *      getEditLocator(), never <code>null</code> after that.
    */
   @Transient
   private PSLocator m_editLocator = null;

   /**
    * Tip locator,
    *
    * @see {@link #getTipLocator()} for descrption. It is (lazy) initialized by
    *      getTipLocator(), never <code>null</code> after that.
    */
   @Transient
   private PSLocator m_tipLocator = null;

   /**
    * It is the value of TITLE column, for example a name of the folder.
    * Initialized by the constructor, never <code>null</code> or empty after
    * that.
    */
   @Basic
   @Column(name = "TITLE")
   private String m_name = "";

   /**
    * The object type of the component. It is defined in the object table.
    * Initialized by the constructor. It can only be one of the
    * <code>TYPE_XXX</code> values after that.
    */
   @Basic
   @Column(name = "OBJECTTYPE")
   private int m_objectType;

   /**
    * Locale identifier string that is from LOCALE column of the CONTENTSTATUS
    * table. It corresponds to one of the deployed locales in the system.
    * Default is {@link com.percussion.i18n.PSI18nUtils#DEFAULT_LANG}.
    */
   @Basic
   @Column(name = "LOCALE")
   private String m_locale = PSI18nUtils.DEFAULT_LANG;

   /**
    * Content Id of the cloned parent that is from CLONEDPARENT column of the
    * CONTENTSTATUS table. It will be set to -1 if there is no clone parent
    * exists for this item.
    */
   @Basic
   @Column(name = "CLONEDPARENT")
   private Integer m_clonedParentContentId;

   /**
    * Community Id of the item from COMMUNITYID column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "COMMUNITYID")
   private Integer m_communityId;

   /**
    * Item's checkout user name from CONTENTCHECKOUTUSERNAME column of the
    * CONTENTSTATUS table. Will be empty if not checked out to anybody.
    */
   @Basic
   @Column(name = "CONTENTCHECKOUTUSERNAME")
   private String m_checkoutUserName = "";

   /**
    * Item's creation date from CONTENTCREATEDDATE column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "CONTENTCREATEDDATE")
   private Date m_contentCreatedDate = null;

   /**
    * Item's expiry date from CONTENTEXPIRYDATE column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "CONTENTEXPIRYDATE")
   private Date m_contentExpiryDate = null;

   /**
    * Item's last modifed date from CONTENTLASTMODIFIEDDATE column of the
    * CONTENTSTATUS table.
    */
   @Basic
   @Column(name = "CONTENTLASTMODIFIEDDATE")
   private Date m_contentLastModifiedDate = null;

   /**
    * Item's last modified user name from CONTENTLASTMODIFIER column of the
    * CONTENTSTATUS table. Never <code>null</code> may be empty.
    */
   @Basic
   @Column(name = "CONTENTLASTMODIFIER")
   private String m_contentLastModifier = "";

   /**
    * Item's start date from CONTENTSTARTDATE column of the CONTENTSTATUS table.
    * Never <code>null</code> may be empty.
    */
   @Basic
   @Column(name = "CONTENTSTARTDATE")
   private Date m_contentStartDate = null;

   /**
    * Item's current state id from CONTENTSTATEID column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "CONTENTSTATEID")
   private Integer m_contentStateId;

   /**
    * Item's workflow id from WORKFLOWAPPID column of the CONTENTSTATUS table.
    */
   @Basic
   @Column(name = "WORKFLOWAPPID")
   private Integer m_workflowAppId;

   /**
    * Item's next aging date from NEXTAGINGDATE column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "NEXTAGINGDATE")
   private Date m_nextAgingDate = null;

   /**
    * Item's creator's name from CONTENTCREATEDBY column of the CONTENTSTATUS
    * table. Never <code>null</code> may be empty.
    */
   @Basic
   @Column(name = "CONTENTCREATEDBY")
   private String m_contentCreatedBy = "";

   /**
    * Item's aging time from CONTENTAGINGTIME column of the CONTENTSTATUS table.
    */
   @Basic
   @Column(name = "CONTENTAGINGTIME")
   private Integer m_contentAgingTime;

   /**
    * Item's publish date from CONTENTPUBLISHDATE column of the CONTENTSTATUS
    * table
    */
   @Basic
   @Column(name = "CONTENTPUBLISHDATE")
   private Date m_contentPublishDate = null;

   /**
    * Item's publish date from CONTENTPOSTDATE column of the CONTENTSTATUS
    * table
    */
   @Basic
   @Column(name = "CONTENTPOSTDATE")
   private Date m_contentPostDate = null;

   /**
    * Item's ContentPostDate Timezone from CONTENTPOSTDATETZ column of the CONTENTSTATUS
    * table
    */
   @Basic
   @Column(name = "CONTENTPOSTDATETZ")
   private String m_contentPostDateTz = "";

   /**
    * Item's content path name from CONTENTPATHNAME column of the CONTENTSTATUS
    * table. May be <code>null</code> or empty.
    */
   @Basic
   @Column(name = "CONTENTPATHNAME")
   private String m_contentPathName = "";

   /**
    * Item's file path name suffix from CONTENTSUFFIX column of the
    * CONTENTSTATUS table. May be <code>null</code> or empty.
    */
   @Basic
   @Column(name = "CONTENTSUFFIX")
   private String m_contentSuffix = "";

   /**
    * Item's revision lock flag from REVISIONLOCK column of the CONTENTSTATUS
    * table.
    */
   @Basic
   @Column(name = "REVISIONLOCK")
   private Character m_revisionLock = 'N';

   /**
    * Item's reminder date from REMINDERDATE column of the CONTENTSTATUS table
    */
   @Basic
   @Column(name = "REMINDERDATE")
   private Date m_reminderDate = null;

   /**
    * Item's curent state entered date from STATEENTEREDDATE column of the
    * CONTENTSTATUS table. Never <code>null</code> may be empty.
    */
   @Basic
   @Column(name = "STATEENTEREDDATE")
   private Date m_stateEnteredDate = null;

   /**
    * Item's next aging transitionid from NEXTAGINGTRANSITION column of the
    * CONTENTSTATUS table.
    */
   @Basic
   @Column(name = "NEXTAGINGTRANSITION")
   private Integer m_nextAgingTransition;

   /**
    * Item's repeated aging transition start date from
    * REPEATEDAGINGTRANSSTARTDATE column of the CONTENTSTATUS table
    */
   @Basic
   @Column(name = "REPEATEDAGINGTRANSSTARTDATE")
   private Date m_repeatedAgingTransStartDate = null;

   /**
    * The content type id of the component.
    */
   @Basic
   @Column(name = "CONTENTTYPEID")
   private Long m_contentTypeId = (long) UNKNOWN_CONTENTTYPE_ID;

   /**
    * Owning relationship information, used for folder restrictions in JCR
    * searches. Lazy loaded to avoid issues with performance and transient to
    * avoid issues with lazy loading and any serialization.
    */
   @SuppressWarnings("unused")
   @OneToMany(targetEntity = PSRelationshipData.class, fetch = FetchType.LAZY)
   @JoinColumn(name = "DEPENDENT_ID")
   @Filter(name="relationshipConfigFilter",
         condition="CONFIG_ID = " + PSRelationshipConfig.ID_FOLDER_CONTENT)
   private Set<PSRelationshipData> parentFolders =
      new HashSet<PSRelationshipData>();

   /**
    * Specifies the permissions set on the item encapsulated by this object for
    * the user accessing the item. Currently this has relevance only if the
    * encapsulated object is of type "folder". Initialized in the constructor or
    * <code>fromXml()</code> method if the object type is "folder". Never
    * <code>null</code> if this summary is for a folder object, otherwise
    * <code>null</code>. Never modified after initialization.
    */
   @Transient
   private PSFolderPermissions m_permissions = null;

   // Private constants for XML attribute and element name
   public final static String XML_ATTR_NAME = "name";

   public final static String XML_ATTR_ID = "id";

   public final static String XML_ATTR_TYPE = "objectType";

   public final static String XML_ATTR_FOLDER_TYPE = "folderType";

   public final static String XML_ATTR_CONTENTTYPE_ID = "contentTypeId";

   public final static String XML_ATTR_CONTENT_ID = "contentId";

   public final static String XML_ATTR_HIB_VERSION = "hibVersion";

   public final static String XML_ATTR_CURR_REV = "currRevision";

   public final static String XML_ATTR_TIP_REV = "tipRevision";

   public final static String XML_ATTR_EDIT_REV = "editRevision";

   public final static String XML_ATTR_PUBLIC_REV = "publicRevision";

   public final static String XML_ATTR_PERMISSIONS = "permissions";

   public final static String XML_NODE_DESCRIPTION = "Description";

   public final static String XML_ATTR_LOCALE = "locale";

   public final static String XML_ATTR_CLONEDPARENT = "clonedParent";

   public final static String XML_ATTR_COMMUNITYID = "communityId";

   public final static String XML_ATTR_CONTENTCHECKOUTUSERNAME = "contentCheckoutUserName";

   public final static String XML_ATTR_CONTENTCREATEDDATE = "contentCreatedDate";

   public final static String XML_ATTR_CONTENTEXPIRYDATE = "contentExpiryDate";

   public final static String XML_ATTR_CONTENTLASTMODIFIEDDATE = "contentLastModifiedDate";

   public final static String XML_ATTR_CONTENTLASTMODIFIER = "contentLastModifier";

   public final static String XML_ATTR_CONTENTSTARTDATE = "contentStartDate";

   public final static String XML_ATTR_CONTENTSTATEID = "contentStateId";

   public final static String XML_ATTR_WORKFLOWAPPID = "workflowAppId";

   public final static String XML_ATTR_NEXTAGINGDATE = "nextAgingDate";

   public final static String XML_ATTR_CONTENTCREATEDBY = "contentCreatedBy";

   public final static String XML_ATTR_CONTENTAGINGTIME = "contentAgingTime";

   public final static String XML_ATTR_CONTENTPUBLISHDATE = "contentPublishDate";

   public final static String XML_ATTR_CONTENTPOSTDATE = "contentPostDate";

   public final static String XML_ATTR_CONTENTPOSTDATETZ = "contentPostDateTz";

   public final static String XML_ATTR_CONTENTPATHNAME = "contentPathName";

   public final static String XML_ATTR_CONTENTSUFFIX = "contentSuffix";

   public final static String XML_ATTR_REVISIONLOCK = "revisionLock";

   public final static String XML_ATTR_REMINDERDATE = "reminderDate";

   public final static String XML_ATTR_STATEENTEREDDATE = "stateEnteredDate";

   public final static String XML_ATTR_NEXTAGINGTRANSITION = "nextAgingTransition";

   public final static String XML_ATTR_REPEATEDAGINGTRANSSTARTDATE = "repeatedAgingTransStartDate";

   public final static String XML_ATTR_LASTTRANSITIONDATE = "lastTransitionDate";

   // Constants to represent the parts of the component summary
   /**
    * Get full component summary info
    */
   public final static int GET_SUMMARY = 1;

   /**
    * Get the locator. This locator will always have revision id=-1
    */
   public final static int GET_LOCATOR = 2;

   /**
    * Get name of the component
    */
   public final static int GET_NAME = 3;

   /**
    * Get current locator. The revision in this locator will be the current
    * revisionid.
    */
   public final static int GET_CURRENT_LOCATOR = 4;

   /**
    * Get current locator. The revision in this locator will be the tip
    * revisionid.
    */
   public final static int GET_TIP_LOCATOR = 5;

   /**
    * Unknown content type id
    */
   public final static int UNKNOWN_CONTENTTYPE_ID = -1;

   /**
    * The constant used for objects of type 'item'.
    */
   public final static int TYPE_ITEM = PSCmsObject.TYPE_ITEM;

   /**
    * The constant used for objects of type 'folder'.
    */
   public final static int TYPE_FOLDER = PSCmsObject.TYPE_FOLDER;

   /**
    * String represetation for the numeric values of TYPE_XXX. Use the type id
    * as the index into this array.
    */
   public final static String[] TYPE_NAMES = new String[]
   {"", "item", "folder"};
}
