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
package com.percussion.fastforward.sfp;

import com.percussion.server.IPSRequestContext;

import java.util.Set;

/**
 * This Exit is used to generate the content list for a specified site folder.
 * 
 * @deprecated This Exit may have poor performance with large amount of items. 
 *             Use {@link PSSiteFolderContentListBulkExit} instead.
 */
public class PSSiteFolderContentListExit extends
      PSSiteFolderContentListBaseExit
{
   // implements the abstract method getSiteFolderCListObject()
   protected PSSiteFolderCListBase getSiteFolderCListInstance(
         IPSRequestContext request, boolean isIncremental,
         String publishableContentValidValues, String contentResourceName,
         int maxRowsPerPage, String protocol, String host, String port,
         Set paramSetToPass)
   {
      return new PSSiteFolderContentList(request, isIncremental,
            publishableContentValidValues, contentResourceName, maxRowsPerPage,
            protocol, host, port,
            paramSetToPass);
   }
}
