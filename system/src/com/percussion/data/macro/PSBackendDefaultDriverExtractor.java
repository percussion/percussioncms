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
package com.percussion.data.macro;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.extension.services.PSDatabasePool;
import com.percussion.util.PSSqlHelper;

/**
 * Macro extractor to get the database defaut driver name. 
 */
public class PSBackendDefaultDriverExtractor implements IPSMacroExtractor
{
   /**
    * This macro extractor extracts the driver name of the default backend
    * database. The name returned is always uppercased.
    */
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      PSDatabasePool dbPool = PSDatabasePool.getDatabasePool();
      return dbPool.getDefaultDriver().toUpperCase();
   }
}
