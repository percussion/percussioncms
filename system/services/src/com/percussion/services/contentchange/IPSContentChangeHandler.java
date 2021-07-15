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
package com.percussion.services.contentchange;

import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSDataServiceException;

/**
 * Handle changes to items in the system
 * 
 * @author JaySeletz
 *
 */
public interface IPSContentChangeHandler
{

   /**
    * Handle the supplied change to a content item
    * 
    * @param e The event, not <code>null</code>.
    */
   public void handleEvent(PSEditorChangeEvent e) throws PSDataServiceException, PSNotFoundException ;

   /**
    * Handle the supplied change to a relationship
    *  
    * @param e The event, not <code>null</code>.
    */
   public void handleEvent(PSRelationshipChangeEvent e) throws PSDataServiceException, PSNotFoundException;

}
