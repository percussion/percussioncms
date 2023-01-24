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
