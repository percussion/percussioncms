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
package com.percussion.rx.publisher.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSEditionTask;
import com.percussion.rx.publisher.IPSEditionTaskStatusCallback;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.sitemgr.IPSSite;

import java.io.File;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Edition task to asynchronously invoke another edition. It is typically used
 * in a post edition task to chain another edition
 * 
 * @author Bill Langlais
 */
public class PSEditionRunner implements IPSEditionTask
{

   public TaskType getType()
   {
      return TaskType.PREANDPOSTEDITION;
   }

   @SuppressWarnings("unused")
   public void perform(IPSEdition edition, IPSSite site, Date startTime,
         Date endTime, long jobId, long duration, boolean success,
         Map<String, String> params, IPSEditionTaskStatusCallback status)
      throws Exception
   {
      Validate.notNull(edition, "edition may not be null");

      Validate.notNull(site, "site may not be null");

      String nextEditionName = params.get("Edition");

      if (StringUtils.isBlank(nextEditionName))
      {
         throw new IllegalArgumentException("You must specify an Edition");
      }

      publish(nextEditionName);
   }

   /**
    * Publish the edition.
    * 
    * @param editionName - Name of the edition to be published assumed not
    * <code>null</code> or empty.
    */
   private void publish(String editionName)
   {
      IPSPublisherService ps = PSPublisherServiceLocator.getPublisherService();

      IPSEdition edition = ps.findEditionByName(editionName);

      if (edition == null)
      {
         throw new RuntimeException(editionName + "is not a valid Edition!");
      }

      IPSRxPublisherService rxPub = PSRxPublisherServiceLocator
            .getRxPublisherService();

      rxPub.startPublishingJob(edition.getGUID(), null);
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {
      // No init
   }
}
