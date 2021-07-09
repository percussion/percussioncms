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

package com.percussion.rest.displayformat;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorResultsException;


import java.util.List;

public interface IDisplayFormatAdaptor {

    public List<DisplayFormat> createDisplayFormats(List<String> names, String session, String user);
    public void deleteDisplayFormats(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user);
    public List<DisplayFormat> findAllDisplayFormats() throws PSCmsException, PSErrorResultsException, PSUnknownNodeTypeException;
    public DisplayFormat findDisplayFormat(IPSGuid id) throws PSCmsException, PSUnknownNodeTypeException;
    public DisplayFormat findDisplayFormat(String name) throws PSCmsException, PSUnknownNodeTypeException;
    public void saveDisplayFormats(List<DisplayFormat> displayFormats, boolean release, String session, String user);

}
