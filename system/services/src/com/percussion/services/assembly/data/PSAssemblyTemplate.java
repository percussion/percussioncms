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
package com.percussion.services.assembly.data;

import com.percussion.extension.IPSExtension;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.SortComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An assembly template object represents a single template. Each template has
 * optional bindings. The bindings map expressions using the JEXL expression
 * syntax to variable bindings. In addition the template contains a reference to
 * a specific assembler for use in evaluating the template's source. The
 * template is a reference to one of two things:
 * <ul>
 * <li>An XSL stylesheet in the filesystem (legacy assembler)
 * <li>A Velocity template contained in a content item (velocity assembler)
 * </ul>
 * <p>
 * This object replaces <code>PSContentTypeVariant</code>.
 * 
 * @author dougrand
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSAssemblyTemplate")
@NaturalIdCache
@Table(name = "PSX_TEMPLATE")
@NamedQueries(
{
      @NamedQuery(name = "template.findByNameAndType", query = "select t from PSAssemblyTemplate t, "
            + "PSContentTemplateDesc d "
            + "where lower(t.name) = :name "
            + "and d.m_contenttypeid = :typeid and d.m_templateid = t.id"),
      @NamedQuery(name = "template.findByType", query = "select d.m_templateid "
            + "from PSContentTemplateDesc d where :ctype = d.m_contenttypeid "),
      @NamedQuery(name = "template.findTemplateNameToTypeInfo",
            query = "select d.m_contenttypeid, t.id, t.name "
            + "from PSAssemblyTemplate t, PSContentTemplateDesc d "
            + "where d.m_templateid = t.id")})
public class PSAssemblyTemplate
      implements
         IPSAssemblyTemplate,
         IPSCatalogSummary,
         IPSCatalogItem,
         Serializable
{

    private static PSExecutionOrderComparator bindingComparator = new PSExecutionOrderComparator();

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger ms_log = Logger.getLogger(PSAssemblyTemplate.class);

   /**
    * 
    */
   private static final long serialVersionUID = -1240365481092237620L;

   /**
    * The template's id
    */
   @Id
   @Column(name = "TEMPLATE_ID")
   private long id;

   /**
    * The hibernate object version
    */
   @Version
   private Integer version;

   /**
    * The template name
    */
   @Basic
   @NaturalId
   private String name;

   /**
    * The template displayed label
    */
   @Basic
   private String label;

   /**
    * The location prefix
    */
   @Basic
   @Column(name = "LOCATIONPREFIX")
   private String locationPrefix;

   /**
    * The location suffix
    */
   @Basic
   @Column(name = "LOCATIONSUFFIX")
   private String locationSuffix;

   /**
    * The assembler
    */
   @Basic
   private String assembler;

   /**
    * The assembly url - may reference the template content item or the
    * application depending on the kind of assembler
    */
   @Basic
   @Column(name = "ASSEMBLYURL")
   private String assemblyUrl;

   /**
    * The stylesheet name
    */
   @Basic
   @Column(name = "STYLESHEETNAME")
   private String styleSheet;

   /**
    * The aa type
    */
   @Basic
   @Column(name = "AATYPE")
   private int aaType;

   /**
    * The output format
    */
   @Basic
   @Column(name = "OUTPUTFORMAT")
   private int outputFormat;

   /**
    * Publish when value
    */
   @Basic
   @Column(name = "PUBLISHWHEN")
   private Character publishWhen = PublishWhen.Unspecified.getValue();

   @Basic
   @Column(name = "TEMPLATE_TYPE")
   private Integer templateType = TemplateType.Shared.ordinal();

   @Basic
   private String description;

   @Lob
   @Basic(fetch = FetchType.EAGER)
   private String template;

   @Basic()
   @Column(name="MIME_TYPE")
   private String mimeType;

   @Basic
   private String charset;

   @OneToMany(targetEntity = PSTemplateBinding.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
   @OrderColumn(name = "EXECUTION_ORDER")
   @ListIndexBase(1)
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSAssemblyTemplate_Bindings")
   @Fetch(FetchMode.SUBSELECT)
   @JoinColumn(name="TEMPLATE_ID", nullable=false)
   @SortComparator(PSExecutionOrderComparator.class)
   private List<PSTemplateBinding> bindings =new ArrayList<>();

   @ManyToMany(targetEntity = PSTemplateSlot.class, cascade =
   {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
   @JoinTable(name = "RXVARIANTSLOTTYPE", joinColumns =
   {@JoinColumn(name = "VARIANTID")}, inverseJoinColumns =
   {@JoinColumn(name = "SLOTID")})
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "PSAssemblyTemplate_Slots")
   private Set<IPSTemplateSlot> slots = new HashSet<>();

   @Basic
   @Column(name = "GLOBAL_TEMPLATE_USAGE")
   private Integer globalTemplateUsage = GlobalTemplateUsage.None
           .ordinal();

   @Basic
   @Column(name = "GLOBAL_TEMPLATE")
   private Long globalTemplate;

   static
   {
      // Register types with XML serializer for read creation of objects
      PSXmlSerializationHelper.addType("binding", PSTemplateBinding.class);
      PSXmlSerializationHelper.addType("slot", PSTemplateSlot.class);
   }

   /**
    * Ctor
    */
   public PSAssemblyTemplate() {
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return name;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      return description;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getAssembler()
    */
   public String getAssembler()
   {
      if (StringUtils.isBlank(assembler))
      {
         return IPSExtension.LEGACY_ASSEMBLER;
      }
      else
      {
         return assembler;
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getAssemblyUrl()
    */
   public String getAssemblyUrl()
   {
      return assemblyUrl;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getStyleSheetPath()
    */
   public String getStyleSheetPath()
   {
      return styleSheet;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getLocationPrefix()
    */
   public String getLocationPrefix()
   {
      return locationPrefix;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getLocationSuffix()
    */
   public String getLocationSuffix()
   {
      return locationSuffix;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getActiveAssemblyType()
    */
   public AAType getActiveAssemblyType()
   {
      return AAType.valueOf(aaType);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getOutputFormat()
    */
   public OutputFormat getOutputFormat()
   {
      return OutputFormat.valueOf(outputFormat);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getBindings()
    */
   public List<PSTemplateBinding> getBindings()
   {
      // hibernate can return null placeholders in lists that have order field.  We will filter these out.
      return bindings.stream().filter(Objects::nonNull).collect(Collectors.toList());
   }

   @Override
   public Object clone()
   {
      try
      {
         PSAssemblyTemplate cloneTemplate = new PSAssemblyTemplate();
         String xml = this.toXML();
         cloneTemplate.fromXML(xml);
         return cloneTemplate;
      }
      catch (Exception e)
   {
         throw new RuntimeException("Failed to clone template", e);
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#fromXML(java.lang.String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      id = 0; // Avoid problems during restore
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.TEMPLATE, id);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid newguid)
   {

      if (newguid == null)
      {
         throw new IllegalArgumentException("newguid may not be null");
      }

      if (id!=newguid.getUUID())
       ms_log.debug("Template guid being changed from "+id+ " to "+newguid.getUUID(), new Throwable());

      id = newguid.getUUID();

   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getPublishWhen()
    */
   public PublishWhen getPublishWhen()
   {
      if (publishWhen != null)
         return PublishWhen.valueOf(publishWhen);
      else
         return PublishWhen.Unspecified;
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
         throw new IllegalStateException("version can only be initialized once");

         if (version != null && version < 0)
         throw new IllegalArgumentException("version must be >= 0");

      this.version = version;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setAssembler(java.lang.String)
    */
   public void setAssembler(String a)
   {
      if (StringUtils.isBlank(a))
      {
         throw new IllegalArgumentException(
               "assembler may not be null or empty");
      }
      assembler = a;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setAssemblyUrl(java.lang.String)
    */
   public void setAssemblyUrl(String aurl)
   {
      // commented temporarily to be able to create new ones without worrying
      // about validation
      // if (StringUtils.isBlank(aurl))
      // {
      // throw new IllegalArgumentException(
      // "assemblyUrl may not be null or empty");
      // }
      assemblyUrl = aurl;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setBindings(java.util.List)
    */
   public void setBindings(List<PSTemplateBinding> newbindings)
   {
      Collections.sort(newbindings,bindingComparator);

      bindings.clear();
      int count=1;
      for (PSTemplateBinding b : newbindings)
   {
         if (!bindings.contains(b))
      {
            b.setExecutionOrder(count++);
            bindings.add(b);
      }
      }
   }

   /**
    * Add a new binding to the list of bindings
    * 
    * @param binding the binding to add, never <code>null</code>
    */
   public void addBinding(PSTemplateBinding binding)
   {
      if (binding == null)
      {
         throw new IllegalArgumentException("binding may not be null");
      }
      //binding.setTemplate(this);
      binding.setExecutionOrder(bindings.size()+1);
      bindings.add(binding);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#removeBinding(com.percussion.services.assembly.IPSTemplateBinding)
    */
   public void removeBinding(PSTemplateBinding binding)
   {
      if (binding == null)
      {
         throw new IllegalArgumentException("binding may not be null");
      }
      bindings.remove(binding);
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setDescription(java.lang.String)
    */
   public void setDescription(String d)
   {
      description = d;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setLocationPrefix(java.lang.String)
    */
   public void setLocationPrefix(String lp)
   {
      locationPrefix = lp;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setLocationSuffix(java.lang.String)
    */
   public void setLocationSuffix(String ls)
   {
      locationSuffix = ls;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setName(java.lang.String)
    */
   public void setName(String n)
   {
      if (StringUtils.isBlank(n))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      this.name = n;
   }

   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      if (StringUtils.isBlank(label))
      {
         throw new IllegalArgumentException("label may not be null or empty");
      }
      this.label = label;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setOutputFormat(com.percussion.services.assembly.IPSAssemblyTemplate.OutputFormat)
    */
   public void setOutputFormat(OutputFormat of)
   {
      if (of == null)
      {
         throw new IllegalArgumentException("outputFormat may not be null");
      }
      outputFormat = of.ordinal();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setPublishWhen(com.percussion.services.assembly.IPSAssemblyTemplate.PublishWhen)
    */
   public void setPublishWhen(PublishWhen pw)
   {
      if (pw == null)
      {
         throw new IllegalArgumentException("publishWhen may not be null");
      }
      publishWhen = pw.getValue();
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setStyleSheetPath(java.lang.String)
    */
   public void setStyleSheetPath(String styleSheetPath)
   {
      this.styleSheet = styleSheetPath;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setActiveAssemblyType(com.percussion.services.assembly.IPSAssemblyTemplate.AAType)
    */
   public void setActiveAssemblyType(AAType aat)
   {
      if (aat == null)
      {
         throw new IllegalArgumentException("aaType may not be null");
      }
      aaType = aat.ordinal();
   }

   /**
    * These accessors for serialization only
    * @return a list of slotids to serialize
    */
   public List<Long> getTemplateSlotIds()
   {
      return slots.stream()
              .map(IPSTemplateSlot::getGUID)
              .map(IPSGuid::longValue)
              .collect(Collectors.toList());
   }

   /**
    * These accessors for serialization only
    * @param slotid the slot ID to add
    */
   public void addTemplateSlotId(Long slotid)
   {
      if (slotid == null)
         throw new IllegalArgumentException("slotid may not be null.");
      
      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      IPSTemplateSlot slot = service.loadSlot(new PSGuid(PSTypeEnum.SLOT,
            slotid));
      Hibernate.initialize(slot);
      addSlot(slot);
   }

   /**
    * @return Returns the mimeType.
    */
   public String getMimeType()
   {
      return mimeType;
   }

   /**
    * @param mt The mimeType to set.
    */
   public void setMimeType(String mt)
   {
      mimeType = mt;
   }

   /**
    * @return Returns the template.
    */
   public String getTemplate()
   {
      return template;
   }

   /**
    * @param t The template to set.
    */
   public void setTemplate(String t)
   {
      template = t;
   }

   /**
    * @return Returns the charset.
    */
   public String getCharset()
   {
      return charset;
   }

   /**
    * @param c The charset to set.
    */
   public void setCharset(String c)
   {
      charset = c;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAssemblyTemplate)) return false;
      PSAssemblyTemplate that = (PSAssemblyTemplate) o;
      return Objects.equals(name, that.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this,
            ToStringStyle.MULTI_LINE_STYLE);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getGlobalTemplateUsage()
    */
   public GlobalTemplateUsage getGlobalTemplateUsage()
   {
      if (globalTemplateUsage != null)
         return GlobalTemplateUsage.valueOf(globalTemplateUsage);
      else
         return GlobalTemplateUsage.Legacy;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getGlobalTemplate()
    */
   public IPSGuid getGlobalTemplate()
   {
      if (globalTemplate != null)
         return new PSGuid(PSTypeEnum.TEMPLATE, globalTemplate);
      else
         return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setGlobalTemplateUsage(com.percussion.services.assembly.IPSAssemblyTemplate.GlobalTemplateUsage)
    */
   public void setGlobalTemplateUsage(GlobalTemplateUsage usage)
   {
      if (usage != null)
         globalTemplateUsage = usage.ordinal();
      else
         globalTemplateUsage = GlobalTemplateUsage.Legacy.ordinal();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setGlobalTemplate(com.percussion.utils.guid.IPSGuid)
    */
   public void setGlobalTemplate(IPSGuid guid)
   {
      if (guid != null)
         globalTemplate = guid.longValue();
      else
         globalTemplate = null;
   }

   /**
    * @return Returns the slots.
    */
   @IPSXmlSerialization(suppress = true)
   public Set<IPSTemplateSlot> getSlots()
   {
      return slots;
   }

   /**
    * Add a single slot to the template.
    * 
    * @param s the slot to add, never <code>null</code>
    */
   public void addSlot(IPSTemplateSlot s)
   {
      if (s == null)
      {
         throw new IllegalArgumentException("slot may not be null");
      }
      s.getSlotTemplates().remove(this);
      slots.add(s);
   }

   /**
    * Remove a single slot from the template
    * 
    * @param s the slot to remove, never <code>null</code>
    */
   public void removeSlot(IPSTemplateSlot s)
   {
      if (s == null)
      {
         throw new IllegalArgumentException("slot may not be null");
      }
      s.getSlotTemplates().remove(this);
      slots.remove(s);
   }


   /**
    * @param s The slots to set.
    */
   public void setSlots(Set<IPSTemplateSlot> s)
   {
      slots = s;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      if (StringUtils.isBlank(label))
         return getName();

      return label;
   }

   /**
    * Get the type of this object for cataloging
    * @return the type, never <code>null</code>
    */
   public PSTypeEnum getType()
   {
      return PSTypeEnum.TEMPLATE;
   }

   /*
    *  (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#getTemplateType()
    */
   public TemplateType getTemplateType()
   {
      if (templateType == null)
         return TemplateType.Shared;
      else
         return TemplateType.valueOf(templateType);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#setTemplateType(com.percussion.services.assembly.IPSAssemblyTemplate.TemplateType)
    */
   public void setTemplateType(TemplateType newTemplateType)
   {
      if (newTemplateType == null)
      {
         throw new IllegalArgumentException("newTemplateType may not be null");
      }
      templateType = newTemplateType.ordinal();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.assembly.IPSAssemblyTemplate#isVariant()
    */
   public boolean isVariant()
   {
      return assembler == null
            || assembler.equals(IPSExtension.LEGACY_ASSEMBLER);
   }


   /**
    * This method does the following: 1. creates a XML document 2. from the
    * document, extract the slot ids and build a list of guids 3. returns a set
    * of guids for the slots
    * 
    * @param tmpStr the original template as a XML string representation from
    *           which the slots are extracted, tmpStr is never <code>null</code>
    *           or empty
    * @return the slots as a guid collection may be empty, but never
    *         <code>null</code>
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    */
   public static Set<IPSGuid> getSlotIdsFromTemplate(String tmpStr)
         throws IOException, SAXException
   {
      if (StringUtils.isBlank(tmpStr))
         throw new IllegalArgumentException("tmpStr may not be null or empty");
      
      Set<IPSGuid> slotGuids = new HashSet<>();
      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            tmpStr), false);
      Element root = doc.getDocumentElement();
      NodeList nTmpList = root.getElementsByTagName(XML_SLOTIDS_NAME);
      if (nTmpList != null && nTmpList.getLength() > 0)
      {
         Element tmpList = (Element) nTmpList.item(0);
         NodeList nl = tmpList.getElementsByTagName(XML_SLOTID_NAME);
         for (int i = 0; (nl != null) && (i < nl.getLength()); i++)
         {
            Element tmpId = (Element) nl.item(i);
            String tmp = PSXMLDomUtil.getElementData(tmpId);
            if (StringUtils.isBlank(tmp))
               continue;
            IPSGuid g = new PSGuid(PSTypeEnum.SLOT, tmp);
            slotGuids.add(g);
         }
      }
      return slotGuids;
   }
   
   /**
    * This method is specifically used by MSM to replace the slot ids from
    * the serialized data with the new ids back into the serialized data 
    * @param tmpStr the original template as XML string representation, 
    * never <code>null</code> or empty
    * @param newSlots the list of new slots that need to be added may
    * not be <code>null</code>, may or may not be empty
    * @return the original template with replaced template GUIDS as a an XML string
    * representation 
    * @throws IOException if an I/O error occurs
    * @throws SAXException if a parsing error occurs
    */
   public static String replaceSlotIdsFromTemplate(String tmpStr,
         Set<IPSGuid> newSlots) throws IOException, SAXException
   {
      if (StringUtils.isBlank(tmpStr))
         throw new IllegalArgumentException("siteStr may not be null or empty");
      if (newSlots == null)
         throw new IllegalArgumentException("template list may not be null");
   
      Document doc = PSXmlDocumentBuilder.createXmlDocument(new StringReader(
            tmpStr), false);
      Element root = doc.getDocumentElement();
      NodeList slotIdsElem = root.getElementsByTagName(XML_SLOTIDS_NAME);
      Element oldSlotList = (Element)slotIdsElem.item(0);
      Element newSlotList = doc.createElement(XML_SLOTIDS_NAME);
      for (IPSGuid g : newSlots)
      {
         PSXmlDocumentBuilder.addElement(doc, newSlotList, XML_SLOTID_NAME,
               String.valueOf(g.getUUID()));
      }
      oldSlotList.getParentNode().replaceChild(newSlotList, oldSlotList);
      return PSXmlDocumentBuilder.toString(doc);
   }
   
   /**
    * Node name for the slotids list representation 
    */
   private static final String XML_SLOTIDS_NAME  = "template-slot-ids";
   
   /**
    * Node name for the slotid that is a child of templateids list 
    */
   private static final String XML_SLOTID_NAME   = "template-slot-id";

}
