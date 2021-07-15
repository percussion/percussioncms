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

package com.percussion.sitemanage.service.impl;

import com.percussion.share.service.PSAbstractSimpleDataService;
import com.percussion.sitemanage.dao.IPSSiteArchitectureDao;
import com.percussion.sitemanage.data.PSSiteArchitecture;
import com.percussion.sitemanage.service.IPSSiteArchitectureDataService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Component("siteArchitectureDataService")
@Lazy
public class PSSiteArchitectureDataService extends
      PSAbstractSimpleDataService<PSSiteArchitecture, String> implements
      IPSSiteArchitectureDataService
{
   @Autowired
   public PSSiteArchitectureDataService(IPSSiteArchitectureDao dao)
   {
      super(dao);
   }
}
