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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDFColumns;
import com.percussion.cms.objectstore.PSDFProperties;
import com.percussion.cms.objectstore.PSDisplayColumn;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.rest.Guid;
import com.percussion.rest.displayformat.*;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.ui.IPSUiDesignWs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Provides the API implementation for the Display Format Resource
 */
@PSSiteManageBean
@Lazy
public class DisplayFormatAdaptor implements IDisplayFormatAdaptor {


    IPSUiDesignWs designWs;


    @Autowired
    public DisplayFormatAdaptor(IPSUiDesignWs designWs){
        this.designWs = designWs;
    }

    @Override
    public List<DisplayFormat> createDisplayFormats(List<String> names, String session, String user) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void deleteDisplayFormats(List<IPSGuid> ids, boolean ignoreDependencies, String session, String user) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public List<DisplayFormat> findAllDisplayFormats() throws PSCmsException, PSErrorResultsException, PSUnknownNodeTypeException {

        List<DisplayFormat> ret = new ArrayList<DisplayFormat>();
        List<IPSCatalogSummary> displayFormats = designWs.findDisplayFormats(null, null);
        List<IPSGuid> guids = new ArrayList<IPSGuid>();

        for(IPSCatalogSummary c: displayFormats){
            guids.add(c.getGUID());
        }

        String currentUser = (String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
        String currentSession  =(String)PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_JSESSIONID);

        List<PSDisplayFormat> dfs = designWs.loadDisplayFormats(guids,false,false,currentSession, currentUser);

        for(PSDisplayFormat f : dfs){
            ret.add(copyDisplayFormat(f));
        }

        return ret;
    }

    private DisplayFormatPropertyList copyDisplayFormatProps(PSDFProperties props){
        DisplayFormatPropertyList ret = new DisplayFormatPropertyList(new ArrayList<DisplayFormatProperty>());


        return ret;
    }
    private DisplayFormat copyDisplayFormat(PSDisplayFormat f) throws PSCmsException, PSUnknownNodeTypeException {
        DisplayFormat ret = new DisplayFormat();

        if(f.getPropertyContainer()!= null)
            ret.setProperties(copyDisplayFormatProps(f.getPropertyContainer()));

        ret.setInternalName(f.getInternalName());

        if(f.getColumnContainer() != null)
            ret.setColumns(copyDisplayFormatColumns(f.getColumnContainer()));


        if(f.getAllowedCommunities() != null)
            ret.setAllowedCommunities(copyAllowedCommunities(f.getAllowedCommunities()));

        ret.setAscendingSort(f.isAscendingSort());
        ret.setDescendingSort(f.isDescendingSort());
        ret.setInvalidFolderFieldNames(f.getInvalidFolderFieldNames());
        ret.setDisplayId(f.getDisplayId());
        ret.setName(f.getName());
        ret.setLabel(f.getLabel());
        ret.setDescription(f.getDescription());
        ret.setDisplayName(f.getDisplayName());
        ret.setGuid(copyGuid(f.getGUID()));
        return ret;
    }

    private Guid copyGuid(IPSGuid guid) {

        Guid g = new Guid();

        g.setHostId(guid.getHostId());
        g.setLongValue(guid.longValue());
        g.setStringValue(guid.toString());
        g.setType(guid.getType());
        g.setUuid(guid.getUUID());
        g.setUntypedString(guid.toStringUntyped());

        return g;
    }

    private Map<Guid, String> copyAllowedCommunities(Map<IPSGuid, String> allowedCommunities) {

        Map<Guid,String> ret = new HashMap<Guid,String>();

        for(Map.Entry<IPSGuid,String>  e : allowedCommunities.entrySet()){
            IPSGuid g = e.getKey();
            String s = e.getValue();
            ret.put(copyGuid(g),s);
        }
        return ret;
    }

    private DisplayFormatColumnList copyDisplayFormatColumns(PSDFColumns columnContainer) {

        DisplayFormatColumnList ret = new DisplayFormatColumnList(new ArrayList<DisplayFormatColumn>());

        for(int i=0;i<columnContainer.size();i++){
            PSDisplayColumn col = (PSDisplayColumn)columnContainer.get(i);
            DisplayFormatColumn dfc = new DisplayFormatColumn();
            dfc.setAscendingSort(col.isAscendingSort());
            dfc.setCategorized(col.isCategorized());
            dfc.setDateType(col.isDateType());
            dfc.setDescendingSort(col.isDescendingSort());
            dfc.setDescription(col.getDescription());
            dfc.setDisplayId(col.getDisplayId());
            dfc.setDisplayName(col.getDisplayName());
            dfc.setImageType(col.isImageType());
            dfc.setNumberType(col.isNumberType());
            dfc.setPosition(col.getPosition());
            dfc.setWidth(col.getWidth());
            dfc.setRenderType(col.getRenderType());
            dfc.setTextType(col.isTextType());
            dfc.setSource(col.getSource());
            dfc.setSortOrder(dfc.isAscendingSort());
            ret.add(dfc);
        }

        return ret;
    }

    @Override
    public DisplayFormat findDisplayFormat(IPSGuid id) throws PSCmsException, PSUnknownNodeTypeException {
        return copyDisplayFormat(designWs.findDisplayFormat(id));
    }

    @Override
    public DisplayFormat findDisplayFormat(String name) throws PSCmsException, PSUnknownNodeTypeException {
        return copyDisplayFormat(designWs.findDisplayFormat(name));
    }

    @Override
    public void saveDisplayFormats(List<DisplayFormat> displayFormats, boolean release, String session, String user) {
        //TODO
        throw new RuntimeException("Not yet implemented");
    }
}
