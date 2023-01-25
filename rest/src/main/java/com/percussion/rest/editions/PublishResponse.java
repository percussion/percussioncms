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
package com.percussion.rest.editions;

import com.fasterxml.jackson.annotation.JsonRootName;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * This response object stores the information returned from a publish request.
 */
@XmlRootElement(name = "EditionPublishResponse")
@JsonRootName("EditionPublishResponse")
public class PublishResponse
{
   /**
    * See {@link #getSiteName()}.
    */
   String siteName;

   /**
    * See {@link #getStatus()}.
    */
   String status;

   /**
    * See {@link #getDelivered()}.
    */
   String delivered;

   /**
    * See {@link #getFailures()}.
    */
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
