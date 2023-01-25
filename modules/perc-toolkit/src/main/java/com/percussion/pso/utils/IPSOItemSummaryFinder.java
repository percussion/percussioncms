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
package com.percussion.pso.utils;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.utils.guid.IPSGuid;
public interface IPSOItemSummaryFinder
{
   public PSLocator getCurrentOrEditLocator(IPSGuid guid) throws PSException;
   public PSLocator getCurrentOrEditLocator(String contentId)
         throws PSException;
   public PSLocator getCurrentOrEditLocator(int id) throws PSException;
   public int getCheckoutStatus(String contentId, String userName)
         throws PSException;
   /**
    * Gets the component summary for an item.
    * @param contentId the content id
    * @return the component summary. Never <code>null</code>.
    * @throws PSException when the item does not exist.
    */
   public PSComponentSummary getSummary(String contentId) throws PSException;
   public PSComponentSummary getSummary(IPSGuid guid) throws PSException;
   public PSComponentSummary getSummary(int id) throws PSException;
}