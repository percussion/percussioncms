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
package com.percussion.services.legacy;

import com.percussion.cms.objectstore.PSComponentSummary;

import java.util.Collection;
import java.util.List;

/**
 * This interface is provided as the public (Rx implementers) interface to this
 * service. This interface provides methods for loading legacy item summaries.
 *
 * @author paulhoward
 */
public interface IPSCmsContentSummaries
{
   /**
    * Load one or more component summaries by content id.
    * 
    * @param ids One or more content ids, never <code>null</code> or empty.
    * @return One or more component summaries, which may or may not be in the
    *         same order as the ids. The size of the returned list may not
    *         match the size of the content ids.
    */
   List<PSComponentSummary> loadComponentSummaries(Collection<Integer> ids);

   /**
    * Load a single component summary by content id.
    * 
    * @param id a content id
    * @return A valid summary or <code>null</code> if the summary is not
    * found.
    */
   PSComponentSummary loadComponentSummary(int id);
   PSComponentSummary loadComponentSummary(int id,boolean refresh);

}
