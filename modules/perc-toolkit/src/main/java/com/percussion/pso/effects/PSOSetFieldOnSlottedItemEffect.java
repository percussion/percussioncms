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

package com.percussion.pso.effects;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.pso.utils.PSOSlotContents;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/***
 * This effect is intended to be used on AA relationships
 * to set the property of an item when an item is added to
 * a given slot.
 *
 */
@PSHandlesEffectContext(required=PSEffectContext.ALL)
public class PSOSetFieldOnSlottedItemEffect implements IPSEffect{


	/**
	 * Logger for this class
	 */
	private static final Logger log = LogManager.getLogger(PSOSetFieldOnSlottedItemEffect.class);
	private static IPSContentWs mCws;  
	private static IPSGuidManager mGmgr; 

	/***
	 * Returns a valid ContentWS instance
	 * @return
	 */
	protected static IPSContentWs getContentService(){
		if(mCws == null)
			mCws = PSContentWsLocator.getContentWebservice();

		return mCws;		
	}
	
	/***
	 * Returns a valid Guid Manager instance.
	 * @return
	 */
	protected static IPSGuidManager getGuidManager(){
		if(mGmgr==null)
			mGmgr = PSGuidManagerLocator.getGuidMgr();
		
		return mGmgr;
	}
	
	/***
	 * Inner class for handling the user configured parameters for the extension
	 * on the Relationship Effect parameters dialog.
	 */
	private class ConfiguredParams{

		protected String fieldName;
		protected String valueIfEmpty;
		protected String valueIfNotEmpty;		
		protected String slotName;
		protected int slotId;
		
		/***
		 * Constructor to initialize a new parameter object
		 * @param params
		 */
		protected ConfiguredParams(Object[] params){
			
			if(params!=null){
				
				//Make sure that all the parameters have been supplied.
				if(params.length<4)
				{
					throw new IllegalArgumentException("All parameters are required!");
				}
						
				if(params[0]!=null){
					fieldName=params[0].toString();
					log.debug("fieldName=" + params[0]);
				}else{
					fieldName=null;
				}
				
				if(params[1]!=null){
					valueIfEmpty=params[1].toString();
					log.debug("valueIfEmpty=" + params[1]);
				}else{
					valueIfEmpty=null;
				}
				
				if(params[2]!=null){
					valueIfNotEmpty=params[2].toString();
					log.debug("valueIfNotEmpty=" + params[2]);
				}else{
					valueIfNotEmpty=null;
				}
				
				if(params[3]!=null){				
					slotName = params[3].toString().trim();
					log.debug("slotName=" + params[3]);
				}else{
					slotName=null;
				}
	
			}
			
			if(params == null || fieldName== null || slotName==null)
			{
				throw new IllegalArgumentException("Field Name, and SlotName parameters must be set.");
			}
			
			//Load the slot id
			slotId = PSOSlotContents.getSlot(slotName).getGUID().getUUID();
			
		}
	}

	
	/***
	 * Inner class for encapsulating the relationship parameters. 
	 * @author natechadwick
	 *
	 */
	private class RelationshipParams{
		
		private static final String LOCALDEP = "rs_islocaldependency";
		private static final String USEDEPREV = "rs_usedependentrevision";
		private static final String ALLOWCLONING = "rs_allowcloning";
		private static final String VARIANT_ID = "sys_variantid";
		private static final String SKIPPROMOTE = "rs_skippromotion";
		private static final String SLOTID= "sys_slotid";
		private static final String USEOWNERREV = "rs_useownerrevision";
		private static final String USESERVERID = "rs_useserverid";
		private static final String SORTRANK = "sys_sortrank";		
		private static final String YES = "yes";
		
		@SuppressWarnings("unused")
		protected boolean islocaldependency;///yes
		@SuppressWarnings("unused")
		protected boolean usedependentrevision;///no
		@SuppressWarnings("unused")
		protected boolean allowcloning;///yes
		@SuppressWarnings("unused")
		protected int variantid;///570
		@SuppressWarnings("unused")
		protected boolean skippromotion;///no
		protected int slotid;///518
		@SuppressWarnings("unused")
		protected boolean useownerrevision;///yes
		@SuppressWarnings("unused")
		protected boolean useserverid;///yes
		@SuppressWarnings("unused")
		protected int sortrank;//2
	
		
		/***
		 * Will return true if the supplied property exists and is set to true, false otherwise
		 * @param props map of properties, must not be null.
		 * @param name name of the property
		 * @return true or false
		 */
		private boolean getBooleanPropValue(Map<String,String> props, String name){
			if(props.containsKey(name)){
				if(props.get(name).equals(YES)){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}
		
		/***
		 * Will return the integer property value or 0 if the property is not set. 
		 * @param props Never null. Collection of properties.
		 * @param name Name of the property
		 * @return Integer value or 0 if the property is not set.
		 */
		private int getIntegerPropValue(Map<String,String> props, String name){
			if(props.containsKey(name)){
				return Integer.parseInt(props.get(name));
			}else{
				return 0;
			}
		}
		
		/***
		 * Convenience constructor
		 * @param props
		 */
		protected RelationshipParams(Map<String,String> props){
			
			islocaldependency = getBooleanPropValue(props, LOCALDEP);
			usedependentrevision = getBooleanPropValue(props, USEDEPREV);
			allowcloning = getBooleanPropValue(props, ALLOWCLONING);
			variantid = getIntegerPropValue(props, VARIANT_ID);
			skippromotion = getBooleanPropValue(props, SKIPPROMOTE);
			slotid = getIntegerPropValue(props,SLOTID);
			useownerrevision = getBooleanPropValue(props, USEOWNERREV);
			useserverid = getBooleanPropValue(props, USESERVERID);
			sortrank = getIntegerPropValue(props, SORTRANK);			
		}
	
	}

	public void init(IPSExtensionDef def, File f)
			throws PSExtensionException {
		log.debug("Initializing...");
		
		   for ( @SuppressWarnings("unchecked")
		Iterator<String> paramsIter = def.getInitParameterNames(); paramsIter.hasNext(); ) 
		   {
			   String p =paramsIter.next();
			   
			   log.debug(p + ":=" + def.getInitParameter(p));
		   }
		   
		
		
	}

	/***
	 * This is called when the Effect is being executed.  Note, this will be called for every item in a slot 
	 * during a save request.  
	 */
	public void attempt(Object[] params, IPSRequestContext request,
		      IPSExecutionContext context, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
			
		if(context.isPostConstruction() || context.isPostDestruction()) {
			//Get the configured params. 
			ConfiguredParams configParams = new ConfiguredParams(params);
			
			PSRelationship rel = context.getCurrentRelationship();
			if(rel.isActiveAssemblyRelationship()){
				log.debug("Loading relationship properties...");
				RelationshipParams relParams = new RelationshipParams(rel.getProperties());
				
				if(relParams.slotid == configParams.slotId){
						log.debug("Slot match.");
					
						IPSGuid guid = getGuidManager().makeGuid(
								rel.getOwner()
								);
						

						List<IPSGuid>guids = Collections.singletonList(guid);
						try{
							//Load the owner item. 
							List<PSCoreItem> items = getContentService().loadItems(
									guids, true, true, false,false);
							
							if(items!=null && items.size()>0){
								//Should only be one item. 
								PSCoreItem item = items.get(0);
								
								if(context.isPreDestruction())
									log.debug("Pre-Destruction");
								else
									log.debug("Pre-Construction");
								log.debug("Updating Content ID: {}", item.getContentId());
			
								//Determine if this is a destroy or a create. 
								if(context.isPostConstruction()){
									updateField(item, configParams.fieldName, configParams.valueIfNotEmpty);
									log.debug("Updating field for a construction event");
								}else if(context.isPostDestruction()){
									
									PSRelationshipFilter filter = new PSRelationshipFilter();
									
									filter.setOwner(rel.getOwner());
									filter.setName(PSRelationshipFilter.FILTER_NAME_ACTIVE_ASSEMBLY);
									filter.limitToEditOrCurrentOwnerRevision(true);
									
									//If this is destruction then we need to see if there are any more items in the slot. 
									boolean found = false;
									try{
										//Get all AA relationships and if we find one including the specified slot
										//set the specified field to the specified value;
										for (PSAaRelationship r : mCws.loadContentRelations(filter,true)) {
											if(r.getId()!= rel.getId()){
												if(r.getSlotName().equals(configParams.slotName)){
													updateField(item, configParams.fieldName, configParams.valueIfNotEmpty + "");
													found=true;
													log.debug("Found item in slot, setting {} to {}", configParams.fieldName, configParams.valueIfNotEmpty);
													break;
												}
											}
									  }
										if(!found){
											updateField(item, configParams.fieldName, configParams.valueIfEmpty + "");
											log.debug("No item found in slot, setting " + configParams.fieldName + " to " + (configParams.valueIfEmpty + ""));
										}
									} catch (PSErrorException e) {
										log.debug("Error processing slot relationships for item " + "" );
										log.error(PSExceptionUtils.getMessageForLog(e));
										log.debug(PSExceptionUtils.getDebugMessageForLog(e));
									}finally{} 
								}
								
								//Save the owner item.
								setProcessedFlag(request);
								saveItem((PSRequestContext)request, rel.getOwner(), item);
								clearProcessedFlag(request);
								log.debug("Item updated.");
							}else{
								log.error("Unable to locate owner for relationship. Content ID: {} not found!", rel.getOwner().getId());
							}
						} catch (PSErrorResultsException e) {
							log.error(PSExceptionUtils.getMessageForLog(e));
							log.debug(PSExceptionUtils.getDebugMessageForLog(e));
						} catch (FileNotFoundException e) {
							log.error(PSExceptionUtils.getMessageForLog(e));
							log.debug(PSExceptionUtils.getDebugMessageForLog(e));
						} catch (IOException e) {
							log.error(PSExceptionUtils.getMessageForLog(e));
							log.debug(PSExceptionUtils.getDebugMessageForLog(e));
						} catch (PSCmsException | PSInvalidContentTypeException e) {
							// TODO Auto-generated catch block
							log.error(PSExceptionUtils.getMessageForLog(e));
							log.debug(PSExceptionUtils.getDebugMessageForLog(e));
						} finally{}
				}
			
			
			}
			   
		}
		
		result.setSuccess();
	}

	@Override
	public void recover(Object[] params, IPSRequestContext request, IPSExecutionContext context, PSExtensionProcessingException e, PSEffectResult result) throws PSExtensionProcessingException {
		//TODO: Implement me
	}


	public void test(Object[] params, IPSRequestContext request, IPSExecutionContext context, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
		log.debug("test method called.");
		result.setSuccess(); 
	}

	
	private void updateField(PSCoreItem item, String name, Object value)
	throws FileNotFoundException, IOException {
		
		PSItemField fld = item.getFieldByName(name);
		if (fld != null) {
			updateFieldValue(fld, value);
		} else {
			log.error("Cannot find field {} Ignoring ",name
							);
		}
		
	}

	private void updateFieldValue(PSItemField field, Object value)
	throws FileNotFoundException, IOException{
		field.clearValues();
		log.debug("updating field " + field.getName());
		IPSFieldValue newValue = getFieldValue(field, value);
		if (newValue!=null) {
			field.addValue(newValue);
		}else{
			log.warn("Setting value for {} to null. ",field.getName());
		}
		 
	}

	private IPSFieldValue getFieldValue(PSItemField field, Object value){

		PSItemFieldMeta fieldMeta = field.getItemFieldMeta();
		IPSFieldValue newValue = null;
		String dataType = fieldMeta.getFieldDef().getDataType();
		log.debug("Field "+ field.getName()+"DataType is "+dataType);
	
		if (dataType.equals(PSField.DT_BOOLEAN)){
			log.debug("Found boolean fields value is ");
			if(value.equals("true") || value.equals("yes") || value.equals("1"))
			{
				newValue = new PSTextValue("true");
			}else{
				newValue = new PSTextValue("false");
			}
		}
		else if ((fieldMeta.getBackendDataType() == PSItemFieldMeta.DATATYPE_TEXT) 
				|| (fieldMeta.getBackendDataType()
						== PSItemFieldMeta.DATATYPE_NUMERIC)){
			newValue = new PSTextValue((String)value);
		}
		
		
		return newValue;
	}
	
	public static String PROCESSED_FLAG= "PSOSetFieldOnSlottedItemProcessedFlag";
	
	/***
	 * Sets the {@link #PROCESSED_FLAG} on the Request context so that the
	 * input transform doesn't overwrite the output of the effect when running
	 * in non Content Editor contexts. 
	 * 
	 * @param ctx
	 */
	private void setProcessedFlag(IPSRequestContext ctx){
		ctx.setParameter(PROCESSED_FLAG, true);
	}

	/***
	 * Clears the processed flag. 
	 */
	private void clearProcessedFlag(IPSRequestContext ctx){
		ctx.removeParameter(PROCESSED_FLAG);
	}
	
	
	private void saveItem(PSRequestContext request, PSLocator loc, PSCoreItem item) throws PSCmsException, PSInvalidContentTypeException{
			
		PSServerItem serverItem = new PSServerItem(item.getItemDefinition());

		serverItem.load(loc, request.getSecurityToken());

           // merge the new data into the current item
           serverItem.loadData(item);

           // insert or update the item
           serverItem.save(request.getRequest());


	}
}
