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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.apibridge;

import com.percussion.rest.contenttypes.ContentType;
import com.percussion.rest.contenttypes.IContentTypesAdaptor;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.util.PSSiteManageBean;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.PSContentWsLocator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
public class ContentTypeAdaptor implements IContentTypesAdaptor {

    private IPSContentDesignWs designSvc;
    public ContentTypeAdaptor(){
        designSvc = PSContentWsLocator.getContentDesignWebservice();
    }

    /***
     * List all content types available to the System
     * @param baseUri Requesting URI
     * @return A list of all available Content Types
     */
    @Override
    public List<ContentType> listContentTypes(URI baseUri) {
        List<ContentType> ret = new ArrayList<ContentType>();

            List<IPSCatalogSummary> types = designSvc.findContentTypes("*");
            for(IPSCatalogSummary s : types){
                ret.add(ApiUtils.convertContentType(s));
            }
        return ret;
    }

    /***
     * List ContentTypes available for the specified Site
     * @param baseUri Originating URI
     * @param siteId Site Id for Site to filter Types by
     * @return An array of ContentTypes
     */
    @Override
    public List<ContentType> listContentTypes(URI baseUri, int siteId) {
        return null;
    }
}
