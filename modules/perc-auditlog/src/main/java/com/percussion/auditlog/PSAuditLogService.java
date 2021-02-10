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

package com.percussion.auditlog;

import com.ibm.cadf.middleware.AuditContext;
import com.ibm.cadf.middleware.AuditMiddleware;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.util.Constants;
import com.percussion.auditlog.util.AuditPropertyLoader;
import com.percussion.auditlog.util.FileCreator;
import com.percussion.utils.io.PathUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Properties;

public class PSAuditLogService  implements IPSAuditLogService {

    private static AuditMiddleware middleware;
    private static Properties properties;
    private static final String CONFIG_FILE_BASE = "rxconfig/Server/audit-log.properties";
    private static Boolean isGenerateLog=false;


    /***
     * Creates an Audit Log Entry for a ContentEvent
     *
     * @param event A fully populated ContentEvent
     */
    public void logContentEvent(PSContentEvent event){
        Event ae = createEvent((AuditContext)event, event.getAction().name(),event.getOutcome());

        if(event.getContentId()!=null)
            ae.addTag(PSContentEvent.CONTENTID_TAG, String.valueOf(event.getContentId()));

        if(StringUtils.isNotEmpty(event.getGuid()))
            ae.addTag(PSContentEvent.GUID_TAG, event.getGuid());

        ae.setId("percussion:"+event.getGuid());


        auditLog(ae);
    }

    /***
     * Logs a Workflow Event
     * @param event
     */
    public void logWorkflowEvent(PSWorkflowEvent event){
        Event ae = createEvent((AuditContext)event, event.getAction().name(),event.getOutcome());

        if(event.getContentId()!=0)
            ae.addTag(PSWorkflowEvent.CONTENTID_TAG, String.valueOf(event.getContentId()));

        if(StringUtils.isNotEmpty(event.getGuid()))
            ae.addTag(PSWorkflowEvent.GUID_TAG, event.getGuid());

        if(StringUtils.isNotEmpty(event.getTransitionFrom()))
            ae.addTag(PSWorkflowEvent.TRANSITIONFROM_TAG, event.getTransitionFrom());

        if(StringUtils.isNotEmpty(event.getTransitionTo()))
            ae.addTag(PSWorkflowEvent.TRANSITIONTO_TAG, event.getTransitionTo());

        ae.setId("percussion:"+event.getGuid());
        auditLog(ae);
    }

    /***
     * Logs an Authentication Event
     * @param event
     */
    public void logAuthenticationEvent(PSAuthenticationEvent event) {

        Event ae = createEvent((AuditContext) event, event.getAction().name(), event.getOutcome());
        auditLog(ae);

    }

    /***
     * Logs an event for User Management
     * @param event
     */
    public void logUserManagementEvent(PSUserManagementEvent event){

        Event ae = createEvent((AuditContext)event, event.getAction().name(),event.getOutcome());
        auditLog(ae);


    }

    public void auditLog(Event ae){
        try{
            if(isGenerateLog() && properties!=null && properties.size()>0) {
                generateLogFile(properties);
                    middleware.audit(ae);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

    public Event createEvent(AuditContext event, String action, String outcome){
        return middleware.createEvent(action, outcome, event);
    }
    private PSAuditLogService()  {
        try {
            middleware = new AuditMiddleware(Constants.AUDIT_FORMAT_TYPE_JSON);

            properties = AuditPropertyLoader.loadProperties(PathUtils.getRxDir(null)+ File.separator+CONFIG_FILE_BASE);
            middleware.setProperties(properties);
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private static PSAuditLogService instance;

    synchronized public static PSAuditLogService getInstance(){
        if (instance == null)
        {
            // if instance is null, initialize
            instance = new PSAuditLogService();
            // AuditPropertyLoader.loadConfig();
        }

        return instance;
    }

    public static void generateLogFile(Properties properties ) {

            String fileName=  FileCreator.generateFile(properties.getProperty("filePath"),properties.getProperty("fileName"),properties.getProperty("filePattern"),properties.getProperty("extension"));

                middleware.setOutputFilePath(fileName);

    }

    public static Boolean isGenerateLog(){
        if("true".equalsIgnoreCase(properties.getProperty("generateLog"))){
            isGenerateLog=true;
        }
        return isGenerateLog;
    }


}
