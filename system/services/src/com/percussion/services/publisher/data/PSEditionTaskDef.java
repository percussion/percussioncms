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
package com.percussion.services.publisher.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.persistence.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An edition task represents a single task to be run either before or after an
 * edition is run. Negative sequence numbers indicate tasks to be run before the
 * edition, and positive sequence numbers indicate tasks to be run after the
 * edition.
 * 
 * @author dougrand
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSEditionTaskDef")
@Table(name = "PSX_EDITION_TASK")
public class PSEditionTaskDef implements java.io.Serializable, IPSCatalogItem,
IPSEditionTaskDef
{
   /**
    * Required id for serialized objects.
    */
   private static final long serialVersionUID = -3018086015173301436L;

   /**
    * The unique id for the task. Not a GUID since tasks are not packaged stand
    * alone.
    */
   @Id
   private long taskId = -1L;

   /**
    * Hibernate version column, <code>null</code> for unsaved values and never
    * <code>null</code> for saved or loaded values.
    */
   @Version
   Integer version;

   /**
    * The foreign key reference for the containing edition.
    */
   @Basic
   private long editionId;

   /**
    * The sequence of this tasks amongst all the tasks to be performed. Negative
    * sequence numbers indicate the tasks should be performed before the edition
    * is run, positive after. No two tasks for a given edition should share a
    * sequence.
    */
   @Basic
   private int sequence;

   /**
    * The name of the extension to be loaded for the task to be run.
    */
   @Basic
   private String extensionName;

   /**
    * Continue on a failure?
    */
   @Basic
   private boolean continueOnFailure;

   /**
    * A rule can reference parameters that control how the rule will be invoked.
    * The parameters can be overridden when the rule is invoked.
    */
   @OneToMany(targetEntity = PSEditionTaskParam.class, cascade =
   {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
   @JoinColumn(name = "TASK_ID")
   @MapKey(name = "name")
   @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, 
         region = "PSEditionTask_params")
   @Fetch(FetchMode. SUBSELECT)
   private Map<String, PSEditionTaskParam> params = new HashMap<>();

   /**
    * Default constructor, required by hibernate implementation.
    */
   @SuppressWarnings("unused")
   private PSEditionTaskDef() 
   {
   }

   /**
    * Minimal constructor.
    * 
    * @param taskGuid the ID of the created object, never <code>null</code>.
    */
   public PSEditionTaskDef(IPSGuid taskGuid)
   {
      if (taskGuid == null)
         throw new IllegalArgumentException("taskGuid may not be null.");
      
      taskId = taskGuid.longValue();
   }
   
   /**
    * Creates an object from the given IDs.
    * @param taskGuid the ID of the created object, never <code>null</code>.
    * @param editionGuid the Edition ID of the created object, never 
    *    <code>null</code>.
    */
   public PSEditionTaskDef(IPSGuid taskGuid, IPSGuid editionGuid)
   {
      if (taskGuid == null)
         throw new IllegalArgumentException("taskGuid may not be null.");
      if (editionGuid == null)
         throw new IllegalArgumentException("editionGuid may not be null.");
      
      taskId = taskGuid.longValue();
      editionId = editionGuid.longValue(); 
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getTaskId()
    */
   public IPSGuid getTaskId()
   {
      return PSGuidUtils.makeGuid(this.taskId, PSTypeEnum.EDITION_TASK_DEF);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setTaskId(java.lang.Long)
    */
   public void setTaskId(IPSGuid taskId)
   {
      if (taskId == null)
         throw new IllegalArgumentException("taskId may not be null");

      this.taskId = taskId.longValue();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getEditionId()
    */
   public IPSGuid getEditionId()
   {
      return PSGuidUtils.makeGuid(this.editionId, PSTypeEnum.EDITION);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setEditionId(java.lang.Long)
    */
   public void setEditionId(IPSGuid editionId)
   {
      if (editionId == null)
         throw new IllegalArgumentException("editionId may not be null");

      this.editionId = editionId.longValue();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getSequence()
    */
   public int getSequence()
   {
      return this.sequence;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setSequence(java.lang.Integer)
    */
   public void setSequence(int sequence)
   {
      this.sequence = sequence;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getExtensionName()
    */
   public String getExtensionName()
   {
      return this.extensionName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setExtensionName(java.lang.String)
    */
   public void setExtensionName(String extensionName)
   {
      this.extensionName = extensionName;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getContinueOnFailure()
    */
   public boolean getContinueOnFailure()
   {
      return this.continueOnFailure;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setContinueOnFailure(java.lang.Boolean)
    */
   public void setContinueOnFailure(boolean continueOnFailure)
   {
      this.continueOnFailure = continueOnFailure;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#getParams()
    */
   public Map<String, String> getParams()
   {
      Map<String, String> rval = new HashMap<>();
      for (Map.Entry<String, PSEditionTaskParam> e : this.params.entrySet())
      {
         rval.put(e.getKey(), e.getValue().getValue());
      }
      return Collections.unmodifiableMap(rval);
   }

   /**
    * Get internal values, used by service only.
    * 
    * @return the values, never <code>null</code> but could be empty.
    */
   public Collection<PSEditionTaskParam> getInternalParams()
   {
      return params.values();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.services.publisher.data.IPSEditionTask#setParam(java.lang.String,
    *      java.lang.String)
    */
   public void setParam(String parameterName, String value)
   {
      if (StringUtils.isBlank(parameterName))
      {
         throw new IllegalArgumentException(
               "parameterName may not be null or empty");
      }
      if (StringUtils.isBlank(value))
      {
         removeParam(parameterName);
         return;
      }
      PSEditionTaskParam param = this.params.get(parameterName);
      if (param == null)
      {
         param = new PSEditionTaskParam();
         param.setTaskId(taskId);
         param.setName(parameterName);
         param.setValue(value);
         this.params.put(parameterName, param);
      }
      else
      {
         param.setValue(value);
      }
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEditionTaskData#removeParam(java.lang.String)
    */
   public void removeParam(String parameterName)
   {
      if (StringUtils.isBlank(parameterName))
      {
         throw new IllegalArgumentException(
               "parameterName may not be null or empty");
      }
      PSEditionTaskParam param = this.params.get(parameterName);
      if (param != null)
      {
         param.setTaskId(null);
         this.params.remove(parameterName);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.services.publisher.IPSEditionTaskDef#getVersion()
    */
   public Integer getVersion()
   {
      return version;
   }
   
   /**
    * Modifies the hibernate version information for this object.
    * 
    * @param version The version to set.
    * 
    * @throws IllegalStateException if an attempt is made to set a previously
    * set version to a non-<code>null</code> value.
    */
   public void setVersion(Integer version) 
   {
      if (this.version != null && version != null)
         throw new IllegalStateException("version can only be set once");
      
      this.version = version;
   }
   
   /**
    * Restores this object's state from its XML representation (string).  See
    * {@link #toXML()} for format of XML.  See
    * {@link IPSCatalogItem#fromXML(String)} for more info on method
    * signature.
    */
   public void fromXML(String xmlsource) throws IOException, SAXException,
      PSInvalidXmlException
   {
      PSStringUtils.notBlank(xmlsource, "xmlsource may not be null or empty");
      
      Reader r = new StringReader(xmlsource);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
      NodeList nodes = doc.getElementsByTagName(XML_NODE_NAME);
      if (nodes.getLength() == 0)
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
               XML_NODE_NAME);
      }
      
      Element elem = (Element) nodes.item(0);
      String cof = PSXmlUtils.checkAttribute(elem, XML_ATTR_COF_NAME, false);
      setContinueOnFailure(Boolean.valueOf(cof));
      
      String edtnId = PSXmlUtils.checkAttribute(elem, XML_ATTR_EDTN_ID_NAME,
            true);
      setEditionId(new PSGuid(edtnId));
      
      String extName = PSXmlUtils.checkAttribute(elem, XML_ATTR_EXT_NAME,
            true);
      setExtensionName(extName);
      
      int seq = PSXmlUtils.checkAttributeInt(elem, XML_ATTR_SEQ_NAME, true);
      setSequence(Integer.valueOf(seq).intValue());
      
      String tskId = PSXmlUtils.checkAttribute(elem, XML_ATTR_TASK_ID_NAME,
            true);
      setTaskId(new PSGuid(tskId));
      
      NodeList paramNodes = elem.getElementsByTagName(XML_PARAM_NODE_NAME);
      for (int i = 0; i < paramNodes.getLength(); i++)
      {
         Element paramNode = (Element) paramNodes.item(i);
         String name = PSXmlUtils.checkAttribute(paramNode, XML_ATTR_NAME,
               true);
         String value = PSXmlUtils.checkAttribute(paramNode, 
               XML_ATTR_VALUE_NAME, true);
         setParam(name, value);
      }
   }
   
   /**
    * Serializes this object's state to its XML representation as a string.  The
    * format is:
    * <pre><code>
    * &lt;!ELEMENT PSXEditionTaskDef (PSXEditonTaskParam*)>
    * &lt;!ATTLIST PSXEditionTaskDef
    *    continue-on-failure CDATA (true | false) "false" #REQUIRED
    *    edition-id CDATA #REQUIRED
    *    extension-name CDATA #REQUIRED
    *    sequence CDATA #REQUIRED
    *    task-id CDATA #REQUIRED
    * >
    * &lt;!ATTLIST PSXEdtionTaskParam>
    *    name CDATA #REQUIRED
    *    value CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSCatalogItem#toXML()} for more info.
    */
   public String toXML()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_COF_NAME, 
            continueOnFailure ? BOOL_TRUE : BOOL_FALSE);
      root.setAttribute(XML_ATTR_EDTN_ID_NAME, getEditionId().toString());
      root.setAttribute(XML_ATTR_EXT_NAME, extensionName);
      root.setAttribute(XML_ATTR_SEQ_NAME, String.valueOf(sequence));
      root.setAttribute(XML_ATTR_TASK_ID_NAME, getTaskId().toString());
           
      Map<String, String> taskParams = getParams();
      for (String param : taskParams.keySet())
      {
         Element child = doc.createElement(XML_PARAM_NODE_NAME);
         child.setAttribute(XML_ATTR_NAME, param);
         child.setAttribute(XML_ATTR_VALUE_NAME, params.get(param).getValue());
         root.appendChild(child);
      }
      
      doc.appendChild(root);
      
      return PSXmlDocumentBuilder.toString(doc);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#setGUID(com.percussion.utils.guid.IPSGuid)
    */
   public void setGUID(IPSGuid newguid)
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid may not be null");
      
      if (taskId != -1L)
         throw new IllegalStateException("guid can only be set once");
      
      taskId = newguid.longValue();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogItem#getGUID()
    */
   public IPSGuid getGUID()
   {
      return getTaskId();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSEditionTaskDef))
         return false;
      PSEditionTaskDef etd = (PSEditionTaskDef) obj;
      
      EqualsBuilder builder = new EqualsBuilder()
         .append(taskId, etd.taskId)
         .append(editionId, etd.editionId)
         .append(sequence, etd.sequence)
         .append(extensionName, etd.extensionName)
         .append(continueOnFailure, etd.continueOnFailure)
         .append(params, etd.params);
      
      return builder.isEquals();
   }
   
   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(extensionName).append(params).
         toHashCode();
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXEditionTaskDef";
   
   /**
    * Constant for the boolean true value.
    */
   private static final String BOOL_TRUE = "true";
   
   /**
    * Constant for the boolean false value.
    */
   private static final String BOOL_FALSE = "false";
   
   // private XML constants
   private static final String XML_PARAM_NODE_NAME = "PSXEditionTaskParam";
   private static final String XML_ATTR_COF_NAME = "continue-on-failure";
   private static final String XML_ATTR_EDTN_ID_NAME = "edition-id";
   private static final String XML_ATTR_EXT_NAME = "extension-name";
   private static final String XML_ATTR_SEQ_NAME = "sequence";
   private static final String XML_ATTR_TASK_ID_NAME = "task-id";
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_VALUE_NAME = "value";  
}
