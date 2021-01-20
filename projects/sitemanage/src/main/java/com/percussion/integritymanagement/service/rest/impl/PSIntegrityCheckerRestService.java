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

package com.percussion.integritymanagement.service.rest.impl;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.integritymanagement.data.PSIntegrityStatusList;
import com.percussion.integritymanagement.service.IPSIntegrityCheckerService.IntegrityTaskType;
import com.percussion.integritymanagement.service.impl.PSIntegrityCheckerService;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("pSIntegrityCheckerRestService")
@Path("/integritycheck")
public class PSIntegrityCheckerRestService
{
    private PSIntegrityCheckerService integrityCheckerService;
    
    @Autowired
    public PSIntegrityCheckerRestService(PSIntegrityCheckerService integrityCheckerService){
        this.integrityCheckerService = integrityCheckerService;
    }
    
    @POST
    @Produces(
    {MediaType.TEXT_HTML})
    public String start(@QueryParam("type") String type)
    {
        IntegrityTaskType tasktype = IntegrityTaskType.all;
        try{
            tasktype = IntegrityTaskType.valueOf(StringUtils.defaultString(type));
        }
        catch(Exception e){
            //default it to all
        }
        return integrityCheckerService.start(tasktype);
    }
    
    @POST
    @Path("/stop")
    public void stop()
    {
        integrityCheckerService.stop();
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/token/{id}")
    public PSIntegrityStatus status(@PathParam("id") String id)
    {
        return integrityCheckerService.getStatus(id);
    }

    @GET
    @Produces(
    {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/history")
    public List<PSIntegrityStatus> history(@QueryParam("type") String type)
    {
        Status st = null;
        try{
            st = Status.valueOf(StringUtils.defaultString(type));
        }
        catch(Exception e){
            //if the supplied string is a non valid status, then return all the history.
        }
        return new PSIntegrityStatusList(integrityCheckerService.getHistory(st));
    }

    @DELETE
    @Path("/token/{id}")
    public void delete(@PathParam("id") String id)
    {
        integrityCheckerService.delete(id);
    }
    
}
