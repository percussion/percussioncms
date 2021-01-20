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
package com.percussion.services.content.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.xml.sax.SAXException;

/**
 * This object represents a single keyword.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSKeyword")
@Table(name = "RXLOOKUP")
public class PSKeyword implements Serializable, IPSCatalogSummary,
   IPSCatalogItem, IPSCloneTuner
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -647694540051253253L;

   @Id
   @Column(name = "LOOKUPID", nullable = false)
   private long m_id;

   @Basic
   @Column(name = "LOOKUPTYPE", nullable = true, length = 50)
   private String keywordType;

   @Basic
   @Column(name = "LOOKUPVALUE", nullable = true, length = 100)
   private String value;

   @Basic
   @Column(name = "LOOKUPDISPLAY", nullable = true, length = 100)
   private String label;

   @Basic
   @Column(name = "DESCRIPTION", nullable = true, length = 255)
   private String description;

   @Basic
   @Column(name = "LOOKUPSEQUENCE", nullable = true)
   private Integer sequence = 0;

   @Version
   @Column(name = "VERSION")
   private Integer version;
   
   @Transient
   private List<PSKeywordChoice> m_choices = new ArrayList<PSKeywordChoice>();
   
   /**
    * Constant for all objects of type keyword.
    */
   public static String KEYWORD_TYPE = "1";

   /**
    * Bean pattern requires the default constructor. Do not use this to create
    * new objects.
    */
   public PSKeyword()
   {
   }

   /**
    * Create a new keyword for the specified parameters with an empty choice
    * list.
    * 
    * @param label the display label for the new keyword, not <code>null</code>
    * or empty.
    * @param description a description for the new keyword, may be
    * <code>null</code> or empty.
    * @param value the value for the new keyword, not <code>null</code>. This
    * value will be used as the type for all choices.
    */
   public PSKeyword(String label, String description, String value)
   {
      setKeywordType(KEYWORD_TYPE);
      setValue(value);
      setLabel(label);
      setDescription(description);
   }

   /**
    * Create a new keyword for the supplied id and choice. This is needed
    * because keywords and choices are stored in the same table and just
    * interpret the content differently.
    * 
    * @param id the guid of the keyword, not <code>null</code>.
    * @param choice the choice for which to create a keyword, not
    * <code>null</code>.
    * @return the new created keyword for the supplied parameters, never
    * <code>null</code>.
    */
   public PSKeyword createKeyword(IPSGuid id, PSKeywordChoice choice)
   {
      if (id == null)
         throw new IllegalArgumentException("id cannot be null");

      if (choice == null)
         throw new IllegalArgumentException("choice cannot be null");

      PSKeyword keyword = new PSKeyword();
      keyword.setGUID(id);
      keyword.setKeywordType(getValue());
      keyword.setLabel(choice.getLabel());
      keyword.setDescription(choice.getDescription());
      keyword.setValue(choice.getValue());
      keyword.setSequence(choice.getSequence());

      return keyword;
   }

   /**
    * Get the keyword type.  For keywords, this value will be equivalent to
    * {@link #KEYWORD_TYPE}.  For keyword choices, this value will be
    * equivalent to the value of the parent keyword.
    * 
    * @return the keyword type, never <code>null</code> or empty.
    */
   public String getKeywordType()
   {
      return keywordType;
   }

   /**
    * Set the keyword type, this can only be done once in the lifetime of this
    * object.
    * 
    * @param keywordType the new keyword type, not <code>null</code> or empty.
    */
   public void setKeywordType(String keywordType)
   {
      if (StringUtils.isBlank(keywordType))
         throw new IllegalArgumentException(
            "keywordType cannot be null or empty");

      if (this.keywordType != null)
         throw new IllegalStateException("cannot change keyword type");

      this.keywordType = keywordType;
   }

   /**
    * Get the keyword value. This value is used as the keyword type for all
    * choices.
    * 
    * @return the keyword value, never <code>null</code>, may be empty.
    */
   public String getValue()
   {
      return (value != null) ? value : "";
   }

   /**
    * Set the value, this can only be done once in the lifetime of this object.
    * 
    * @param value the new keyword value, not <code>null</code>.
    */
   public void setValue(String value)
   {
      if (value == null)
         throw new IllegalArgumentException("value cannot be null");

      if (!m_choices.isEmpty() && this.value != null)
         throw new IllegalStateException(
            "cannot change value if keyword defines choices");

      this.value = value;
   }

   /**
    * Get the keyword label.
    * 
    * @return the keyword label, never <code>null</code> or empty.
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * Calls {@link #setLabel(String)}.
    * 
    * @see #setLabel(String)
    */
   public void setName(String name)
   {
      setLabel(name);
   }

   /**
    * Set a new keyword label.
    * 
    * @param label the new label, not <code>null</code> or empty.
    */
   public void setLabel(String label)
   {
      if (StringUtils.isBlank(label))
         throw new IllegalArgumentException("label cannot be null or empty");

      this.label = label;
   }

   /**
    * Get the keyword description.
    * 
    * @return the keyword description, may be <code>null</code> or empty.
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * Set a new keyword description.
    * 
    * @param description the new keyword description, may be <code>null</code>
    * or empty.
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * Get the display sequence for this keyword.
    * 
    * @return the 0 based display sequence.
    */
   public Integer getSequence()
   {
      return sequence;
   }

   /**
    * Set a new display sequence.
    * 
    * @param sequence the new 0 based display sequence, may be <code>null</code>,
    * must be >= 0 if provided.
    */
   public void setSequence(Integer sequence)
   {
      if (sequence != null && sequence < 0)
         throw new IllegalArgumentException("sequence must be >= 0");

      this.sequence = sequence;
   }

   /**
    * Get a list with all defined keyword choices.
    * 
    * @return all keyword choices, never <code>null</code>, may be empty.
    */
   public List<PSKeywordChoice> getChoices()
   {
      return m_choices;
   }

   /**
    * Set new keyword choices.
    * 
    * @param choices a list with the new keyword choices, may be
    * <code>null</code> or empty.
    */
   public void setChoices(List<PSKeywordChoice> choices)
   {
      if (choices == null)
         m_choices = new ArrayList<PSKeywordChoice>();
      else
         m_choices = choices;
   }

   /**
    * Set a keyword choice, either inserts a new one or updated an existing one
    * based on the label case insensitive.
    * 
    * @param choice the keyword choice to set, not <code>null</code>.
    */
   public void setChoice(PSKeywordChoice choice)
   {
      if (choice == null)
         throw new IllegalArgumentException("choice cannot be null");

      for (PSKeywordChoice existingChoice : m_choices)
      {
         if (existingChoice.getLabel().equalsIgnoreCase(choice.getLabel()))
         {
            existingChoice.setDescription(choice.getDescription());
            existingChoice.setValue(choice.getValue());
            existingChoice.setSequence(choice.getSequence());

            return;
         }
      }

      m_choices.add(choice);
   }

   /**
    * Necessary for betwixt serialization
    * 
    * @param choice
    */
   public void addChoice(PSKeywordChoice choice)
   {
      if (choice == null)
         throw new IllegalArgumentException("choice cannot be null");

      for (PSKeywordChoice existingChoice : m_choices)
      {
         if (existingChoice.getLabel().equalsIgnoreCase(choice.getLabel()))
         {
            existingChoice.setDescription(choice.getDescription());
            existingChoice.setValue(choice.getValue());
            existingChoice.setSequence(choice.getSequence());

            return;
         }
      }

      m_choices.add(choice);
   }

   /**
    * Get the object version.
    * 
    * @return the object version, <code>null</code> if not initialized yet.
    */
   @IPSXmlSerialization(suppress = true)
   public Integer getVersion()
   {
      return version;
   }

   /**
    * Set the object version. The version can only be set once in the life cycle
    * of this object.
    * 
    * @param version the version of the object, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (this.version != null && version != null)
      {
         throw new IllegalStateException("version can only be initialized "
               + "once");
      }
      
      if (version != null && version.intValue() < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.version = version;
   }
   
   /**
    * Get the lookup id for the keyword.
    * 
    * @return The lookup id, <code>null</code> if not initialized yet.
    */
   public long getId()
   {
      return this.m_id;
   }
   
   /**
    * Set the lookup id for the keyword.
    * 
    * @param id The lookup id.
    */
   public void setId(long id)
   {
      this.m_id = id;
   }
   
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.KEYWORD_DEF, m_id);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return getLabel();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogSummary#getType()
    */
   public PSTypeEnum getType()
   {
      return PSTypeEnum.KEYWORD_DEF;
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");

      // if (m_id != 0)
      // throw new IllegalStateException("cannot change existing guid");

      m_id = newguid.longValue();
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(java.lang.Object,
    * long)
    */
   public Object tuneClone(long newId)
   {
      m_id = newId;
      value = newId + "";
      return this;
   }
   
   /**
    * Performs a deep copy of the data in the supplied keyword to this
    * keyword.  All properties are copied except for id and version.
    *
    * @param other a valid {@link PSKeyword}.  Cannot be <code>null</code>.
    */
   public void copy(PSKeyword other)
   {
      if (other == null)
         throw new IllegalArgumentException("other may not be null.");
           
      description = other.description;
      keywordType = other.keywordType;
      label = other.label;
      m_choices = new ArrayList<PSKeywordChoice>(other.getChoices());
      sequence = other.sequence;
      value = other.value;
   }
}
