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
