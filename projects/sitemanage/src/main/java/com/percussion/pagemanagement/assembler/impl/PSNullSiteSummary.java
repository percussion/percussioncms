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
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.sitemanage.data.PSSiteSummary;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * A null site used for resource generation of assets during preview.
 * During preview of assets we do not have a site associated with them.
 * Instead of giving the assets a <code>null</code> value for sites
 * in the {@link PSResourceInstance resource instance} we use this object. 
 * <p>
 * This follows the Null Object pattern as we prefer to avoid null when we can.
 * 
 * @author adamgent
 *
 */
@XmlRootElement
public class PSNullSiteSummary extends PSSiteSummary
{

    private static final long serialVersionUID = 1L;
    private static PSNullSiteSummary siteSummary = new PSNullSiteSummary();
    static {
        siteSummary.setBaseUrl("http://localhost/");
        siteSummary.setFolderPath("//Sites/$NullSite$");
        siteSummary.setName("NullSite");
        siteSummary.setId(null);
    }
    
    private PSNullSiteSummary() {
    }
    
    public static PSNullSiteSummary getInstance() {
        return siteSummary;
    }

}

