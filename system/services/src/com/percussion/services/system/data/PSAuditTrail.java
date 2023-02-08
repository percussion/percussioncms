/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.services.system.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single audit trail.
 */
public class PSAuditTrail implements Serializable
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -8037995317473435726L;
   
   private int currentRevision;
   
   private int editRevision;
   
   private List<PSAudit> audits = new ArrayList<>();

   /**
    * Default constructor.
    */
   public PSAuditTrail()
   {
   }
   
   /**
    * Get the current revision of the item for which this defines the audit
    * trail.
    * 
    * @return the current revision of the item for which this defines the 
    *    audit trail.
    */
   public int getCurrentRevision()
   {
      return currentRevision;
   }
   
   /**
    * Set the current revision of the item for which this defines the audit
    * trail.
    * 
    * @param currentRevision the new current revision of the item for which
    *    this defines the audit trail.
    */
   public void setCurrentRevision(int currentRevision)
   {
      this.currentRevision = currentRevision;
   }
   
   /**
    * Get the edit revision of the item for which this defines the audit
    * trail.
    * 
    * @return the edit revision of the item for which this defines the 
    *    audit trail.
    */
   public int getEditRevision()
   {
      return editRevision;
   }
   
   /**
    * Set the edit revision of the item for which this defines the audit
    * trail.
    * 
    * @param editRevision the new edit revision of the item for which
    *    this defines the audit trail.
    */
   public void setEditRevision(int editRevision)
   {
      this.editRevision = editRevision;
   }
   
   /**
    * Get the list with all audits.
    * 
    * @return the list with all audits, never <code>null</code>, may
    *    be empty.
    */
   public List<PSAudit> getAudits()
   {
      return audits;
   }
   
   /**
    * Set a new list of audits.
    * 
    * @param audits the new list of audits, may be <code>null</code> or
    *    empty.
    */
   public void setAudits(List<PSAudit> audits)
   {
      if (audits == null)
         this.audits = new ArrayList<>();
      else
         this.audits = audits;
   }
   
   /**
    * Add a new audit.
    * 
    * @param audit the new audit to add, not <code>null</code>.
    */
   public void addAudit(PSAudit audit)
   {
      if (audit == null)
         throw new IllegalArgumentException("audit cannot be null");
      
      audits.add(audit);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAuditTrail)) return false;
      PSAuditTrail that = (PSAuditTrail) o;
      return getCurrentRevision() == that.getCurrentRevision() && getEditRevision() == that.getEditRevision() && Objects.equals(getAudits(), that.getAudits());
   }

   @Override
   public int hashCode() {
      return Objects.hash(getCurrentRevision(), getEditRevision(), getAudits());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSAuditTrail{");
      sb.append("currentRevision=").append(currentRevision);
      sb.append(", editRevision=").append(editRevision);
      sb.append(", audits=").append(audits);
      sb.append('}');
      return sb.toString();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

