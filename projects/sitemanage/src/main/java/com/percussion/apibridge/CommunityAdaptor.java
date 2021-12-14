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

package com.percussion.apibridge;

import com.percussion.rest.GuidList;
import com.percussion.rest.ObjectTypeEnum;
import com.percussion.rest.communities.Community;
import com.percussion.rest.communities.CommunityList;
import com.percussion.rest.communities.CommunityVisibilityList;
import com.percussion.rest.communities.ICommunityAdaptor;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.system.IPSSystemWs;
import org.springframework.beans.factory.annotation.Autowired;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

@PSSiteManageBean
public class CommunityAdaptor implements ICommunityAdaptor {

    @Autowired
    private IPSSecurityDesignWs securityDesignWs;

    @Autowired
    private IPSSystemWs systemWs;

    /***
     * Create one or more communities by name and return the results
     * @param names
     * @return
     */
    @Override
    public CommunityList createCommunities(List<String> names) {
        CommunityList ret=null;
        ArrayList<Community> communities = new ArrayList<>();

        String session = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
        String user = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

        List<PSCommunity> ps_communities = securityDesignWs.createCommunities(names,session,user);

        for(PSCommunity c : ps_communities){
           communities.add(ApiUtils.convertPSCommunity(c));
        }

        ret = new CommunityList(communities);

        return ret;
    }

    @Override
    public CommunityList findCommunities(String name) {

        List<IPSCatalogSummary> ps_summaries = securityDesignWs.findCommunities(name);
        ArrayList<Community> communities = new ArrayList<>();
        for(IPSCatalogSummary s : ps_summaries){
            communities.add(new Community(s.getGUID().longValue(),
                    ApiUtils.convertGuid(s.getGUID()),
                    s.getName(),
                    s.getDescription(),
                    s.getLabel()));
        }
        return new CommunityList(communities);
    }

    @Override
    public CommunityList loadCommunities(GuidList ids, boolean lock, boolean overrideLock) throws PSErrorResultsException {


        String session = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
        String user = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

        List<PSCommunity> ps_communities = securityDesignWs.loadCommunities(ApiUtils.convertGuids(ids),lock,overrideLock,session,user);

        return ApiUtils.convertPSCommunities(ps_communities);

    }

    @Override
    public void saveCommunities(CommunityList communities, boolean release) {

        String session = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
        String user = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

        securityDesignWs.saveCommunities(ApiUtils.convertCommunityList(communities),release,session,user);
    }

    @Override
    public void deleteCommunities(GuidList ids, boolean ignoreDependencies) {

        String session = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
        String user = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

        securityDesignWs.deleteCommunities(ApiUtils.convertGuids(ids),ignoreDependencies,session,user);
    }

    @Override
    public CommunityVisibilityList getVisibilityByCommunity(GuidList ids, ObjectTypeEnum type) throws PSErrorResultsException, RemoteException {

        CommunityVisibilityList ret = null;

        String session = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
        String user = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);

        List<PSCommunityVisibility> ps_visibilities = securityDesignWs.getVisibilityByCommunity(ApiUtils.convertGuids(ids),
                    ApiUtils.convertObjectTypeEnum(type),
                session,
                user);

            return new CommunityVisibilityList(ApiUtils.convertPSCommunityVisibilities(ps_visibilities));
    }

    @Override
    public void switchCommunity(String name) {
        systemWs.switchCommunity(name);
    }
}
