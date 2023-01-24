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

package com.percussion.services.assembly.impl.finder;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.utils.guid.IPSGuid;

import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.Set;

public interface IPSAutoFinderUtils {

    Set<PSContentFinderBase.ContentItem> getContentItems(IPSAssemblyItem sourceItem,
                                                         long slotId, Map<String, Object> params, IPSGuid templateId) throws PSSiteManagerException, PSNotFoundException, RepositoryException;
}
