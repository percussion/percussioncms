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

package com.percussion.assetmanagement.forms.service.impl;

import com.percussion.assetmanagement.forms.data.PSFormSummary;
import com.percussion.assetmanagement.forms.data.PSFormSummaryList;
import com.percussion.assetmanagement.forms.service.IPSFormDataService;
import com.percussion.delivery.client.IPSDeliveryClient.HttpMethodType;
import com.percussion.delivery.client.IPSDeliveryClient.PSDeliveryActionOptions;
import com.percussion.delivery.client.PSDeliveryClient;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.service.exception.PSValidationException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.web.service.PSRestServicePathConstants.FIND_ALL_PATH;
import static org.apache.commons.lang.Validate.notNull;

/**
 * @author peterfrontiero
 * 
 */
@Path("/form")
@Component("formDataService")
public class PSFormDataService implements IPSFormDataService
{

    /**
     * The delivery service initialized by constructor, never <code>null</code>.
     */
    IPSDeliveryInfoService deliveryService;

    /**
     * The form data joiner initialized by constructor. Used to merge forms data
     * and generate a CSV file. Never <code>null</code>.
     */
    PSFormDataJoiner formDataJoiner;

    @Autowired
    @Lazy
    private IPSPubServerService pubServerService;
    /**
     * Create an instance of the service.
     * 
     * @param deliveryService the delivery service, not <code>null</code>.
     */
    @Autowired
    public PSFormDataService(IPSDeliveryInfoService deliveryService)
    {
        notNull(deliveryService);
        this.deliveryService = deliveryService;
        this.formDataJoiner = new PSFormDataJoiner();
    }

    /**
     * Finds a server with the forms service.
     * 
     * @return the server, it may be <code>null</code> if cannot find the
     *         server.
     */
    private PSDeliveryInfo findServer(String site) throws IPSPubServerService.PSPubServerServiceException, PSNotFoundException {
        String adminURl= pubServerService.getDefaultAdminURL(site);

        PSDeliveryInfo server = deliveryService.findByService(PSDeliveryInfo.SERVICE_FORMS,null,adminURl);
        if (server == null)
            log.debug("Cannot find server with service of: " + PSDeliveryInfo.SERVICE_FORMS);

        return server;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.assetmanagement.service.IPSFormDataService#getAllFormData
     * ()
     */
    @Override
    @GET
    @Path(FIND_ALL_PATH)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSFormSummary> getAllFormData(String site)
    {
        String procUrl = null;

        try {
            PSDeliveryInfo server = findServer(site);

            if (server == null)
                return Collections.emptyList();

            Map<String, PSFormSummary> formDataMap = new HashMap<>();
            procUrl = server.getUrl() + FORM_INFO_URL;

            List<PSFormSummary> result = new ArrayList<>();

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            JSONObject getJson = new JSONObject();
            getJson = deliveryClient.getJsonObject(new PSDeliveryActionOptions(server, FORM_INFO_URL + "list",
                    HttpMethodType.GET, true));
            JSONArray formInfo = (JSONArray) getJson.get("formsInfo");

            for (int i = 0; i < formInfo.size(); i++)
            {
                PSFormSummary sum;
                JSONObject formObj = formInfo.getJSONObject(i);
                String name = formObj.getString(NAME_FIELD);
                if (!formDataMap.containsKey(name))
                {
                    sum = new PSFormSummary();
                    sum.setName(name);
                }
                else
                {
                    sum = formDataMap.get(name);
                }

                mergeFormData(formObj, sum);
                formDataMap.put(name, sum);
            }
            result.addAll(formDataMap.values());
            CompareFormSummary compare = new CompareFormSummary();
            Collections.sort(result, compare);
            return new PSFormSummaryList(result);
        }
        catch (Exception e)
        {
            log.warn("Error getting all form data from processor at : {} Error: {}",
                    procUrl, e.getMessage());
            throw new WebApplicationException(e, Response.serverError().build());
        }

    }

    /**
     * Utility class, used to sort {@link PSFormSummary}
     */
    class CompareFormSummary implements Comparator<PSFormSummary>
    {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(PSFormSummary s1, PSFormSummary s2)
        {
            return s1.getName().compareToIgnoreCase(s2.getName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.assetmanagement.service.IPSFormDataService#getFormData
     * (java.lang.String)
     */
    @Override
    @GET
    @Path("/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSFormSummary getFormData(@PathParam("name") String name)
    {
        try {
            rejectIfBlank("getFormData", "name", name);

            PSFormSummary sum = null;

            PSDeliveryInfo processor = findServer("");
            if (processor == null)
                return sum;

            String url = processor.getUrl() + FORM_INFO_URL + name;
            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            JSONObject getJson = new JSONObject();

                getJson = deliveryClient.getJsonObject(new PSDeliveryActionOptions(processor, FORM_INFO_URL + name,
                        HttpMethodType.GET, true));
                JSONArray formInfo = (JSONArray) getJson.get("formsInfo");

                if (!formInfo.isEmpty()) {
                    sum = new PSFormSummary();
                    sum.setName(name);

                    mergeFormData(formInfo.getJSONObject(0), sum);
                }


            return sum;
        } catch (PSValidationException | IPSPubServerService.PSPubServerServiceException | PSNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Deletes the given form in all delivery servers.
     * 
     * @param name Form name.
     */
    @Override
    @DELETE
    @Path("/{name}/{site}")
    public void clearFormData(@PathParam("name") String name,@PathParam("site") String site) throws PSFormDataServiceException
    {
        try {
            PSDeliveryInfo deliveryServer = findServer(site);
            if (deliveryServer == null)
                throw new RuntimeException("Cannot find service of: " + PSDeliveryInfo.SERVICE_FORMS);

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            deliveryClient.push(new PSDeliveryActionOptions(deliveryServer, FORM_INFO_URL + name, HttpMethodType.DELETE,
                    true), null);
        } catch (IPSPubServerService.PSPubServerServiceException | PSNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
           throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Returns all forms data from all delivery servers in the configuration
     * 
     * @param name Form name
     * @return Forms data in CSV format. Never <code>null</code>, may be empty.
     */
    @Override
    @GET
    @Path("submissions/{site}/{name}")
    @Produces("text/csv")
    public String exportFormData(@PathParam("site") String site,
                                 @PathParam("name") String name)
    {
        try {
            rejectIfBlank("exportFormData", "name", name);

            PSDeliveryInfo deliveryServer = findServer(site);
            if (deliveryServer == null)
                throw new WebApplicationException("Cannot find service of: " + PSDeliveryInfo.SERVICE_FORMS);

            String formPath = createFormNamePath(name);
            List<String> formsData = new ArrayList<>();

            PSDeliveryClient deliveryClient = new PSDeliveryClient();
            String response = deliveryClient.getString(new PSDeliveryActionOptions(deliveryServer,
                    FORM_INFO_URL + formPath, HttpMethodType.GET, true));

            if (response != null)
                formsData.add(response);


            return formDataJoiner.joinFormData(formsData.toArray(new String[0]));
        } catch (PSValidationException | IPSPubServerService.PSPubServerServiceException | PSNotFoundException e) {
           throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * Merges the submission data from a form json object into a form summary.
     * 
     * @param obj form json object.
     * @param sum form summary.
     */
    private void mergeFormData(JSONObject obj, PSFormSummary sum)
    {
        int totalSubmissions = obj.getInt(TOTALFORMS_FIELD);
        sum.setNewSubmissions(sum.getNewSubmissions() + (totalSubmissions - obj.getInt(EXPORTEDFORMS_FIELD)));
        sum.setTotalSubmissions(sum.getTotalSubmissions() + totalSubmissions);
    }

    /**
     * Helper method to create a form name path.
     * 
     * @param formName Assumed not blank.
     * @return A path of the form "formName/formName.csv". Never empty.
     */
    private String createFormNamePath(String formName)
    {
        String formNewName = formName;
        String csvName = "";
        if (formName.endsWith(".csv"))
        {
            formNewName = formName.substring(0, formName.length() - 4);
            csvName = formName;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(formNewName);
        sb.append("/");
        if (csvName.isEmpty())
        {
            sb.append(formNewName);
            sb.append(".csv");
        }
        else
        {
            sb.append(csvName);
        }
        return sb.toString();
    }

    /**
     * Field names of the formInfo json object.
     */
    private static final String NAME_FIELD = "name";

    private static final String TOTALFORMS_FIELD = "totalForms";

    private static final String EXPORTEDFORMS_FIELD = "exportedForms";

    /**
     * Url to get form information. Contains leading forward slash, but no
     * trailing forward slash.
     */
    private static final String FORM_INFO_URL = "/perc-form-processor/forms/form/";

    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSFormDataService.class);

}
