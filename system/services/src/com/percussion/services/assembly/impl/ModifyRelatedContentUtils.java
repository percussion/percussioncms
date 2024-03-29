/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.services.assembly.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSContentTypeTemplate;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.server.PSActiveAssemblerProcessor;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.util.IPSHtmlParameters;
import org.w3c.dom.Document;

public class ModifyRelatedContentUtils {
    /**
     * Convenience method that calls {@link move(int, IPSRequestContext,
     * boolean) move(rid, request, true)}.
     */
    public static void moveUp(int rid, IPSRequestContext request)
            throws PSException
    {
        move(rid, request, true);
    }

    /**
     * Convenience method that calls {@link move(int, IPSRequestContext,
     * boolean) move(rid, request, false)}.
     */
    public static void moveDown(int rid, IPSRequestContext request)
            throws PSException
    {
        move(rid, request, false);
    }

    /**
     * Move the supplied relationship by one position into the given direction.
     *
     * @param rid the id of the relationship to be moved.
     * @param request the request used to perform the action, assumed not
     *    <code>null</code>.
     * @param up the moving direction, <code>true</code> to move up/right,
     *    <code>false</code> to move down/left.
     * @throws PSException for any error.
     */
    public static void move(int rid, IPSRequestContext request, boolean up)
            throws PSException
    {
        PSRelationship relationship = getRelationship(rid, request);
        if (relationship != null)
        {
            String slotid = relationship.getProperty(IPSHtmlParameters.SYS_SLOTID);
            PSSlotType slot = getSlotType(slotid, request);
            if (slot == null)
                throw new PSExtensionProcessingException(0,
                        "No slot found for slotid: " + slotid);

            String variantid = relationship.getProperty(
                    IPSHtmlParameters.SYS_VARIANTID);
            IPSAssemblyTemplate template = getTemplate(variantid);
            if (template == null)
                throw new PSExtensionProcessingException(0,
                        ERROR_MSG_NO_VARIANT_FOUND + variantid);

            PSAaRelationshipList relationshipList = new PSAaRelationshipList();
            PSAaRelationship aarelationship = new PSAaRelationship(relationship, slot,
                    new PSContentTypeTemplate(template));
            //Sets the siteid and folderid of the original relationship to aarelationship
            String siteid = relationship.getProperty(
                    IPSHtmlParameters.SYS_SITEID);
            if(siteid != null)
                aarelationship.setProperty(IPSHtmlParameters.SYS_SITEID,siteid);
            String folderid = relationship.getProperty(
                    IPSHtmlParameters.SYS_FOLDERID);
            if(folderid != null)
                aarelationship.setProperty(IPSHtmlParameters.SYS_FOLDERID,folderid);
            relationshipList.add(aarelationship);

            String sortrank = relationship.getProperty(
                    IPSHtmlParameters.SYS_SORTRANK);
            if (sortrank == null)
                throw new PSExtensionProcessingException(0,
                        "No sortrank property found for relationship: " + rid);

            int index = -1;
            try
            {
                index = Integer.parseInt(sortrank);
            }
            catch (NumberFormatException e)
            {
                throw new PSExtensionProcessingException(0,
                        "Found invalid sortrank for relationship: " + rid);
            }
            //We need to avoid processing when up is true and the item is already at the top.
            PSActiveAssemblyProcessorProxy aaProc = getAaProcessor(request);
            boolean isZeroBased = aaProc.isZeroBased(relationshipList);
            if(!up || (!isZeroBased && index != 1) || (isZeroBased && index != 0))
            {
                if (up)
                    index--;
                else
                    index++;

                aaProc.reArrangeSlotRelationships(relationshipList, index);
            }

            /*
             * Add the contentid if not already there. This is required for the
             * touch-parents exit.
             */
            if (request.getParameter(IPSHtmlParameters.SYS_CONTENTID) == null)
                request.setParameter(IPSHtmlParameters.SYS_CONTENTID,
                        Integer.toString(relationship.getOwner().getId()));
        }
    }
    /**
     * Load a template using a string id
     * @param variantid the string id, assumed not <code>null</code> or empty
     * @return the template
     * @throws PSCmsException
     */
    private static IPSAssemblyTemplate getTemplate(String variantid)
            throws PSCmsException
    {
        try
        {
            IPSAssemblyService assembly =
                    PSAssemblyServiceLocator.getAssemblyService();
            try
            {
                return assembly.loadTemplate(variantid, true);
            }
            catch (PSAssemblyException e)
            {
                throw new PSCmsException(0,
                        "Invalid variantid : " + variantid);
            }
        }
        catch (NumberFormatException e)
        {
            throw new PSCmsException(0, e.getLocalizedMessage());
        }
    }


    /**
     * @param rid
     * @param index
     */
    public static void reorder(int rid, int index, IPSRequestContext request)
            throws PSException
    {
        PSRelationship relationship = getRelationship(rid, request);
        if (relationship == null)
        {
            throw new PSException("Could not find a relationship with rid=" + rid);
        }
        if(index == -1)
            index = Integer.MAX_VALUE;
        String slotid = relationship.getProperty(IPSHtmlParameters.SYS_SLOTID);
        PSSlotType slot = getSlotType(slotid, request);
        if (slot == null)
            throw new PSExtensionProcessingException(0,
                    "No slot found for slotid: " + slotid);

        String variantid = relationship.getProperty(
                IPSHtmlParameters.SYS_VARIANTID);
        IPSAssemblyTemplate template = getTemplate(variantid);
        if (template == null)
            throw new PSExtensionProcessingException(0,
                    "No variant type found for variantid: " + variantid);

        PSAaRelationshipList relationshipList = new PSAaRelationshipList();
        PSAaRelationship aarelationship = new PSAaRelationship(relationship, slot,
                new PSContentTypeTemplate(template));
        //Sets the siteid and folderid of the original relationship to aarelationship
        String siteid = relationship.getProperty(
                IPSHtmlParameters.SYS_SITEID);
        if(siteid != null)
            aarelationship.setProperty(IPSHtmlParameters.SYS_SITEID,siteid);
        String folderid = relationship.getProperty(
                IPSHtmlParameters.SYS_FOLDERID);
        if(folderid != null)
            aarelationship.setProperty(IPSHtmlParameters.SYS_FOLDERID,folderid);
        relationshipList.add(aarelationship);

        PSActiveAssemblyProcessorProxy aaProc = getAaProcessor(request);
        aaProc.reArrangeSlotRelationships(relationshipList, index);
    }

    /**
     * Convenience method that calls {@link getSlotType(int, request)}.
     */
    public static PSSlotType getSlotType(String slotid, IPSRequestContext request)
            throws PSCmsException
    {
        try
        {
            int id = Integer.parseInt(slotid);
            return getSlotType(id, request);
        }
        catch (NumberFormatException e)
        {
            throw new PSCmsException(0, e.getLocalizedMessage());
        }
    }

    /**
     * Get the slot type for the supplied parameters.
     *
     * @param slotid the slotid for which to get the slot type object.
     * @param request the request used to lookup the slots, assumed not
     *    <code>null</code>.
     * @return the slot type object or <code>null</code> if none
     *    was found for the supplied slotid.
     * @throws PSCmsException for any error looking up the slot types.
     */
    public static PSSlotType getSlotType(int slotid, IPSRequestContext request)
            throws PSCmsException
    {
        IPSAssemblyService service = PSAssemblyServiceLocator
                .getAssemblyService();
        IPSTemplateSlot slot = service.findSlot(new PSGuid(PSTypeEnum.SLOT,(long)slotid));
        if (slot!=null)
        {
            PSSlotType st = new PSSlotType(slot);
            return st;
        }
        return null;
         /*
      try
      {
         PSComponentDefProcessorProxy processor =
            new PSComponentDefProcessorProxy(
               PSComponentDefProcessorProxy.PROCTYPE_SERVERLOCAL, request);
         Element[] elements = processor.load(PSDbComponent.getComponentType(
            PSSlotTypeSet.class), new PSKey[] { });

         return new PSSlotTypeSet(elements).getSlotTypeById(slotid);
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSCmsException(e);
      }
      */
    }


    /**
     *
     * @param rid
     * @param slotId
     * @param index
     * @param request
     * @throws PSException
     */
    public static void moveToSlot(int rid, int slotId, int index, int newVariantId,
                                  IPSRequestContext request) throws PSException
    {
        PSRelationship relationship = getRelationship(rid, request);
        if (relationship == null)
        {
            throw new PSException("Could not find a relationship with rid=" + rid);
        }
        if(index == -1)
            index = Integer.MAX_VALUE;
        PSSlotType slot = getSlotType(String.valueOf(slotId), request);
        if (slot == null)
            throw new PSExtensionProcessingException(0,
                    "No slot found for slotid: " + slotId);

        String variantid = relationship.getProperty(
                IPSHtmlParameters.SYS_VARIANTID);
        if(newVariantId != -1)
            variantid = String.valueOf(newVariantId);
        IPSAssemblyTemplate template = getTemplate(variantid);
        if (template == null)
            throw new PSExtensionProcessingException(0,
                    "No variant type found for variantid: " + variantid);

        PSAaRelationshipList relationshipList = new PSAaRelationshipList();
        PSAaRelationship aarelationship = new PSAaRelationship(relationship, slot,
                new PSContentTypeTemplate(template));
        aarelationship.setProperty(IPSHtmlParameters.SYS_VARIANTID, variantid);
        //Sets the siteid and folderid of the original relationship to aarelationship
        String siteid = relationship.getProperty(
                IPSHtmlParameters.SYS_SITEID);
        if(siteid != null)
            aarelationship.setProperty(IPSHtmlParameters.SYS_SITEID,siteid);
        String folderid = relationship.getProperty(
                IPSHtmlParameters.SYS_FOLDERID);
        if(folderid != null)
            aarelationship.setProperty(IPSHtmlParameters.SYS_FOLDERID,folderid);
        relationshipList.add(aarelationship);

        PSActiveAssemblyProcessorProxy aaProc = getAaProcessor(request);
        aaProc.reArrangeSlotRelationships(relationshipList, index);
    }
    /**
     * This is a helper function that gets the XML document that gives the
     * contentid and revision of the parent item from the relateditemid in the
     * request. This function makes an internal request to a Rhythmyx resource
     * in sys_rcSupport application.
     *
     * @param request must not be <code>null</code>.
     * @return the result document that has contentid and revisionid of the
     *    parent item, may be <code>null</code>.
     * @throws PSExtensionProcessingException for any error.
     */
    public static Document getActiveItemInfo(IPSRequestContext request)
            throws PSExtensionProcessingException
    {
        Document doc = null;
        IPSInternalRequest iReq = null;
        try
        {
            String appResource = request.getCurrentApplicationName() +
                    "/activeitem";
            if (appResource.startsWith("/"))
                appResource = appResource.substring(1);
            iReq = request.getInternalRequest(appResource);
            doc = iReq.getResultDoc();
        }
        catch (Exception e)
        {
            throw new PSExtensionProcessingException(0, e.getLocalizedMessage());
        }

        return doc;
    }
    /**
     * Insert all slot items supplied through the HTML request parameters.
     *
     * @param request the request used to perform the action, assumed not
     *    <code>null</code>.
     * @throws PSException for any error.
     */
    public static void insertSlotItems(IPSRequestContext request)
            throws PSException
    {
        String contentid = null;
        String revision = null;

        /*
         * If the active item id is present we use it to get the contentid
         * and revision. This is the case when the active item is not the
         * parent item.
         */
        String activeitemid = request.getParameter(
                IPSHtmlParameters.SYS_ACTIVEITEMID);
        if (activeitemid != null && activeitemid.trim().length() != 0)
        {
            Document doc = getActiveItemInfo(request);
            if (doc != null)
            {
                contentid = doc.getDocumentElement().getAttribute(ATTR_CONTENTID);
                revision = doc.getDocumentElement().getAttribute(ATTR_REVISION);
            }
        }
        else
        {
            contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
            revision = request.getParameter(IPSHtmlParameters.SYS_REVISION);
        }

        if (contentid == null)
            throw new PSExtensionProcessingException(0,
                    "No owner contentid found.");

        if (revision == null)
            throw new PSExtensionProcessingException(0,
                    "No owner revision found.");

        String slotid = request.getParameter(IPSHtmlParameters.SYS_SLOTID);
        if (slotid == null)
            throw new PSExtensionProcessingException(0,
                    "No slotid found.");
        PSSlotType slot = getSlotType(slotid, request);
        if (slot == null)
            throw new PSExtensionProcessingException(0,
                    "No slot found for slotid: " + slotid);

        Object[] cidVidArray = request.getParameterList(PARAM_CONIDVARID);
        if (cidVidArray == null || cidVidArray.length == 0)
            throw new PSExtensionProcessingException(0,
                    "No contentid / variantid list found.");

        PSLocator owner = new PSLocator(contentid, revision);
        PSRelationshipConfig config =
                PSActiveAssemblerProcessor.getConfigForSlot(request, slotid);
        if (config == null)
            config = PSRelationshipCommandHandler.getRelationshipConfig(
                    PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);

        PSAaRelationshipList relationshipList = new PSAaRelationshipList();
        for (int i=0; i<cidVidArray.length; i++)
        {
            String[] array = cidVidArray[i].toString().split(";");

            //Required parameters
            String dependentid = "";
            if (array.length > 0)
                dependentid = array[0];
            String variantid = "";
            if (array.length > 1)
                variantid = array[1];
            /*
             * Starting from Rx 5.6, the cidVidArray element may have the optional
             * parameters siteid and folderid too.
             */
            String siteid = "";
            if(array.length>2)
                siteid = array[2];

            String folderid = "";
            if(array.length>3)
                folderid = array[3];

            PSLocator dependent = new PSLocator(dependentid);

            IPSAssemblyTemplate template = getTemplate(variantid);
            if (template == null)
                throw new PSExtensionProcessingException(0,
                        "No variant type found for variantid: " + variantid);

            PSAaRelationship relationship = new PSAaRelationship(owner,
                    dependent, slot, new PSContentTypeTemplate(template), config);
            if (siteid.length() > 0)
                relationship.setProperty(IPSHtmlParameters.SYS_SITEID, siteid);
            if (folderid.length() > 0)
                relationship.setProperty(IPSHtmlParameters.SYS_FOLDERID, folderid);
            relationshipList.add(relationship);

            /*
             * Append the dependentid to the httpcaller parameter. It is only
             * added if the HTML parameter 'sys_contentid' does not already
             * exist.
             */
            String httpcaller = request.getParameter(PARAM_HTTPCALLER);
            if (httpcaller != null)
            {
                int pos = httpcaller.indexOf('?');
                if (pos == -1 || httpcaller.substring(pos).indexOf(
                        IPSHtmlParameters.SYS_CONTENTID) == -1)
                {
                    String delimiter = pos == -1 ? "?" : "&";
                    httpcaller += delimiter + IPSHtmlParameters.SYS_CONTENTID +
                            "=" + dependentid;

                    request.setParameter(PARAM_HTTPCALLER, httpcaller);
                }
            }
        }

        PSActiveAssemblyProcessorProxy aaProc = getAaProcessor(request);
        aaProc.addSlotRelationships(relationshipList, Integer.MAX_VALUE);
    }

    /**
     * Get the active assembly processor.
     *
     * @param request the request for which to get the processor, assumed not
     *    <code>null</code>.
     * @return the active assembly processor proxy, never <code>null</code>.
     * @throws PSCmsException for any errors creating the processor.
     */
    public static PSActiveAssemblyProcessorProxy getAaProcessor(
            IPSRequestContext request) throws PSCmsException
    {
        return new PSActiveAssemblyProcessorProxy(
                PSActiveAssemblyProcessorProxy.PROCTYPE_SERVERLOCAL, request);
    }

    public static PSRelationshipSet getRelationships(int rid, IPSRequestContext request)
            throws PSCmsException
    {
        PSRelationshipFilter filter = new PSRelationshipFilter();
        filter.setRelationshipId(rid);

        PSRelationshipProcessor processor =  PSRelationshipProcessor.getInstance();
        return processor.getRelationships(filter);
    }

    public static PSRelationship getRelationship(int rid, IPSRequestContext request)
            throws PSCmsException
    {
        PSRelationshipSet relationships = getRelationships(rid, request);

        PSRelationship relationship = null;
        if (!relationships.isEmpty())
            relationship = (PSRelationship) relationships.get(0);

        return relationship;
    }

    /**
     * Delete the relationship addressed through the supplied relationship
     * id.
     *
     * @param rid the id of the relationship to be deleted.
     * @param request the request used to perform the action, assumed not
     *    <code>null</code>.
     * @throws PSException for any error.
     */
    public static void deleteSlotItem(int rid, IPSRequestContext request)
            throws PSException
    {
        PSRelationshipSet relationships = getRelationships(rid, request);
        getRsProcessor().delete(relationships);

        if (request.getParameter(IPSHtmlParameters.SYS_CONTENTID) == null &&
                !relationships.isEmpty())
        {
            PSRelationship relationship = (PSRelationship) relationships.get(0);
            request.setParameter(IPSHtmlParameters.SYS_CONTENTID,
                    String.valueOf(relationship.getOwner().getId()));
        }
    }

    /**
     * Change the slot and/or variant for the supplied relationship.
     *
     * @param rid the id of the relationship to be modified.
     * @param request the request used to perform the action, assumed not
     *    <code>null</code>.
     * @throws PSException for any error.
     */
    public static void modifySlotVariant(int rid, IPSRequestContext request)
            throws PSException
    {
        PSRelationship relationship = getRelationship(rid, request);
        if (relationship != null)
        {
            String newSlotid = request.getParameter(PARAM_NEWSLOTID);
            String newVariantid = request.getParameter(PARAM_NEWVARIANTID);

            if (newSlotid != null)
            {
                int slotid = -1;
                try
                {
                    slotid = Integer.parseInt(newSlotid);
                }
                catch (NumberFormatException e)
                {
                    throw new PSExtensionProcessingException(0,
                            "Found invalid new slotid for relationship: " + rid);
                }

                if (getSlotType(slotid, request) == null)
                    throw new PSExtensionProcessingException(0,
                            "Invalid new slotid : " + slotid);

                relationship.setProperty(IPSHtmlParameters.SYS_SLOTID, newSlotid);
            }

            if (newVariantid != null)
            {
                try
                {
                    getTemplate(newVariantid);
                }
                catch (PSCmsException e)
                {
                    throw new PSExtensionProcessingException(0,
                            "Invalid new variantid : " + newVariantid);
                }

                relationship.setProperty(IPSHtmlParameters.SYS_VARIANTID,
                        newVariantid);
            }

            if (newSlotid != null || newVariantid != null)
            {
                PSRelationshipSet relationships = new PSRelationshipSet();
                relationships.add(relationship);

                getRsProcessor().save(relationships);
            }
        }
    }
    /**
     * Get the relationship processor.
     *
     * @param request the request for which to get the processor, assumed not
     *    <code>null</code>.
     * @return the relationship processor proxy, never <code>null</code>.
     * @throws PSCmsException for any errors creating the processor.
     */
    public static PSRelationshipProcessor getRsProcessor() throws PSCmsException
    {
        return PSRelationshipProcessor.getInstance();
    }

    /**
     * Name of the attribute of the item element representing the
     * contentid of the parent item.
     */
    private static final String ATTR_CONTENTID = "contentid";

    /**
     * Name of the attribute of the item element representing the
     * revisionid of the parent item.
     */
    private static final String ATTR_REVISION = "revision";
    /**
     * HTML parameter representing the combination of item contentid
     * and variantid in the format 'contentid;variantid'.
     */
    public static final String PARAM_CONIDVARID = "conidvarid";


    /**
     * HTML parameter representing the new slotid to move the item.
     */
    private static final String PARAM_NEWSLOTID = "newslotid";

    /**
     * HTML parameter representing the new variantid for an item.
     */
    private static final String PARAM_NEWVARIANTID = "newvariantid";

    /**
     * HTML parameter representing the httpcaller return URL
     */
    public static final String PARAM_HTTPCALLER = "httpcaller";
    /**
     * Constant for no variant found error message.
     */
    public static final String ERROR_MSG_NO_VARIANT_FOUND =
            "No variant type found for variantid: ";
}
