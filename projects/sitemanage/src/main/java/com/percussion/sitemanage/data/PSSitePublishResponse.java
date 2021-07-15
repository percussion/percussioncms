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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;


/**
 * This response object stores the information returned from a publish request.
 */
@XmlRootElement(name = "SitePublishResponse")
@JsonRootName("SitePublishResponse")
public class PSSitePublishResponse
{
   /**
    * See {@link #getSiteName()}.
    */
   String siteName;

   /**
    * See {@link #getStatus()}.
    */
   @NotBlank
   @NotNull
   String status;

   /**
    * See {@link #getDelivered()}.
    */
   @NotBlank
   @NotNull
   String delivered;

   /**
    * See {@link #getFailures()}.
    */
   @NotBlank
   @NotNull
   String failures;
   
   String warningMessage;
   
    long jobid;

    public long getJobid()
    {
        return jobid;
    }

    public void setJobid(long jobid)
    {
        this.jobid = jobid;
    }

/**
    * @return the name of the site to be published.
    */
   public String getSiteName()
   {
      return siteName;
   }

   /**
    * @param siteName the name of the site to be published.
    */
   public void setSiteName(String siteName)
   {
      this.siteName = siteName;
   }

   /**
    * @return the publishing status, never blank.
    */
   public String getStatus()
   {
      return status;
   }

   /**
    * @param status the publishing status. May not be blank.
    */
   public void setStatus(String status)
   {
      this.status = status;
   }

   /**
    * @return the number of items delivered, never blank.
    */
   public String getDelivered()
   {
      return delivered;
   }

   /**
    * @param delivered the number of items delivered. May not be blank.
    */
   public void setDelivered(String delivered)
   {
      this.delivered = delivered;
   }
   
   /**
    * @return the number of failures, never blank.
    */
   public String getFailures()
   {
      return failures;
   }

   /**
    * @param failures the number of failures. May not be blank.
    */
   public void setFailures(String failures)
   {
      this.failures = failures;
   }
   
    /**
     * @return the warning message.
     */
    public String getWarningMessage()
    {
        return warningMessage;
    }

    /**
     * @param warningMessage the warning message.
     */
    public void setWarningMessage(String warningMessage)
    {
        this.warningMessage = warningMessage;
    }

}
