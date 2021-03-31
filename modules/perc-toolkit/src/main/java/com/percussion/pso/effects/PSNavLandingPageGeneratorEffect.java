package com.percussion.pso.effects;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.fastforward.managednav.PSNavAbstractEffect;
import com.percussion.fastforward.managednav.PSNavConfig;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.fastforward.managednav.PSNavProxyFactory;
import com.percussion.fastforward.managednav.PSNavRelationshipInfo;
import com.percussion.fastforward.managednav.PSNavUtil;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.impl.PSGuidManager;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.impl.PSSystemService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;


/***
 * A relationship effect for handling the generation and association of Landing Pages for
 * newly created Navons designed for the <code>FolderContent</code> relationship.   
 * <p>
 * There is currently one event that that this effect must handle:
 * <ul>
 * <li>A new NavOn is added to a folder.</li>
 * </ul>
 * </p>
 * <p>
 * A new landing page of the configured content type 
 * </p>
 * @author natechadwick
 *
 */
@PSHandlesEffectContext(required = PSEffectContext.PRE_CONSTRUCTION)
public class PSNavLandingPageGeneratorEffect extends  PSNavAbstractEffect{

	public static String DEFAULT_LANDING_CONTENTTYPE = "navon.landingpage.default.ct";
	public static String DEFAULT_LANDING_TITLE_TEMPLATE = "navon.landingpage.default.title.template";
	public static String DEFAULT_LANDING_DISPLAYTITLE_FIELD = "navon.landingpage.default.displaytitle.field";
	public static String DEFAULT_LANDING_DISPLAYTITLE_FORMAT ="navon.landingpage.default.displaytitle.format";
	public static String DEFAULT_LANDING_REQUIRED_FIELD_NAMES="navon.landingpage.default.required.fields.names";
	public static String DEFAULT_LANDING_REQUIRED_FIELD_VALUES="navon.landingpage.default.required.fields.values";
	public static String DEFAULT_LANDING_COMMUNITYID="navon.landingpage.default.communityid";
	
	 private static Log log = LogFactory.getLog(PSNavLandingPageGeneratorEffect.class); 
	 private String m_defaultContentType=null;
	 private String m_defaultLandingTitleTemplate = "{0}-LP";
	 private String m_defaultLandingDisplayTitleField = null;
	 private String m_defaultLandingDisplayTitleFormat = "{0}";
	 private String[] m_defaultLandingRequiredFields = null;
	 private String[] m_defaultLandingRequiredValues = null;
	 private Integer m_defaultLandingCommunityId = 0;
	 private long m_contentTypeId=0;
	 
	/**
	 * @return the m_defaultLandingCommunityId
	 */
	public Integer getDefaultLandingCommunityId() {
		return m_defaultLandingCommunityId;
	}


	/**
	 * @param m_defaultLandingCommunityId the m_defaultLandingCommunityId to set
	 */
	public void setDefaultLandingCommunityId(Integer m_defaultLandingCommunityId) {
		this.m_defaultLandingCommunityId = m_defaultLandingCommunityId;
	}


	/**
	 * @return the m_defaultLandingRequiredValues
	 */
	public String[] getDefaultLandingRequiredValues() {
		return m_defaultLandingRequiredValues;
	}


	/**
	 * @param m_defaultLandingRequiredValues the m_defaultLandingRequiredValues to set
	 */
	public void setDefaultLandingRequiredValues(
			String[] m_defaultLandingRequiredValues) {
		this.m_defaultLandingRequiredValues = m_defaultLandingRequiredValues;
	}


	/**
	 * @return the m_defaultLandingRequiredFields
	 */
	public String[] getDefaultLandingRequiredFields() {
		return m_defaultLandingRequiredFields;
	}


	/**
	 * @param m_defaultLandingRequiredFields the m_defaultLandingRequiredFields to set
	 */
	public void setDefaultLandingRequiredFields(
			String[] m_defaultLandingRequiredFields) {
		this.m_defaultLandingRequiredFields = m_defaultLandingRequiredFields;
	}


	/**
	 * @return the m_defaultLandingDisplayTitleFormat
	 */
	public String getDefaultLandingDisplayTitleFormat() {
		return m_defaultLandingDisplayTitleFormat;
	}


	/**
	 * @param m_defaultLandingDisplayTitleFormat the m_defaultLandingDisplayTitleFormat to set
	 */
	public void setDefaultLandingDisplayTitleFormat(
			String m_defaultLandingDisplayTitleFormat) {
		this.m_defaultLandingDisplayTitleFormat = m_defaultLandingDisplayTitleFormat;
	}


	/**
	 * @return the m_defaultLandingDisplayTitleField
	 */
	public String getDefaultLandingDisplayTitleField() {
		return m_defaultLandingDisplayTitleField;
	}


	/**
	 * @param m_defaultLandingDisplayTitleField the m_defaultLandingDisplayTitleField to set
	 */
	public void setDefaultLandingDisplayTitleField(
			String m_defaultLandingDisplayTitleField) {
		this.m_defaultLandingDisplayTitleField = m_defaultLandingDisplayTitleField;
	}


	/**
	 * @return the m_defaultLandingTitleTemplate
	 */
	public String getDefaultLandingTitleTemplate() {
		return m_defaultLandingTitleTemplate;
	}


	/**
	 * @param m_defaultLandingTitleTemplate the m_defaultLandingTitleTemplate to set
	 */
	public void setDefaultLandingTitleTemplate(
			String m_defaultLandingTitleTemplate) {
		this.m_defaultLandingTitleTemplate = m_defaultLandingTitleTemplate;
	}


	/**
	 * @return the m_defaultContentType
	 */
	public String getDefaultContentType() {
		return m_defaultContentType;
	}


	/**
	 * @param m_defaultContentType the m_defaultContentType to set
	 */
	public void setDefaultContentType(String m_defaultContentType) {
		this.m_defaultContentType = m_defaultContentType;
	}


	/***
	 * Handle initilization parameters.  The Content Type configured 
	 * as the 
	 * 
	 */
	@Override public void init(com.percussion.extension.IPSExtensionDef extDef, java.io.File codeRoot) throws com.percussion.extension.PSExtensionException {
		super.init(extDef, codeRoot);
	};
	
	
	@Override
	public void attempt(Object[] params, IPSRequestContext req,
			IPSExecutionContext excontext, PSEffectResult result)
			throws PSExtensionProcessingException, PSParameterMismatchException {
		
		if(excontext.isPreConstruction()){
			initParams(params);
		
		
			PSRelationship curRel = excontext.getCurrentRelationship();
		
			PSNavRelationshipInfo info;
			try{
				info = new PSNavRelationshipInfo(curRel, req);
			}catch(Exception ex){
				log.debug("Unable to load relationship for RID:" + curRel.getId(),ex);
				result.setSuccess();
				return;
			}
		
		
			addNewLandingPage(req,info,result);
		}
		else{
			result.setSuccess();
			return;
		}
	}


	private void initParams(Object[] params) {
		
		if(params!=null){
		
			if(params[0]!=null)
				this.setDefaultContentType(params[0].toString());
	
			if(params[1]!=null)
				this.setDefaultLandingTitleTemplate(params[1].toString());
		
			if(params[2]!=null)
				this.setDefaultLandingDisplayTitleField(params[2].toString());
		
			if(params[3]!=null)
				this.setDefaultLandingDisplayTitleFormat(params[3].toString());
		
			if(params[4]!=null)
				this.setDefaultLandingRequiredFields(getCSVList(params[4].toString()));
		
			if(params[5]!=null)
				this.setDefaultLandingRequiredValues(getCSVList(params[5].toString()));
		
			if(params[6]!=null)
				this.setDefaultLandingCommunityId(Integer.parseInt("0" + params[6].toString()));	
		
			if(m_defaultLandingRequiredFields!=null && m_defaultLandingRequiredValues!=null){
				if(m_defaultLandingRequiredFields.length != m_defaultLandingRequiredValues.length){
					throw new IllegalArgumentException("Required fields and Required field value lists have a different number of enteries!");
				}
			}
		}
	}


	private void addNewLandingPage(IPSRequestContext req,
			PSNavRelationshipInfo info, PSEffectResult result) {
		
		PSComponentSummary dep = info.getDependent();
		if(dep.isFolder()){
			log.debug("Skipping effect as item is a folder");
			result.setSuccess();
			return;
		}
		
		try{
			
			PSNavConfig navConfig = PSNavConfig.getInstance();
			
			if(dep.getContentTypeId() == navConfig.getNavonType()){
				//This is a NavOn so we want to make sure it's landing
				//page slot is empty.  If it is not, then we want to skip out.
				//Otherwise we will create a new Landing Page and add it to the 
				//slot. 
			
				String landingSlot = navConfig.getProperty(PSNavConfig.NAVON_LANDING_SLOT, "rffNavLandingPage");
				PSAaRelationshipList slotContents = PSNavUtil.getSlotContents(req, info.getRelation().getDependent(), landingSlot);
				
				if(slotContents.isEmpty()){
					
					//If there is no default community set, then just take the community of the NavOn
					if(m_defaultLandingCommunityId==null || m_defaultLandingCommunityId.equals(0)){
						m_defaultLandingCommunityId = info.getDependent().getCommunityId();
					}
					
					//Generate the Landing Page
					log.debug("Creating Landing page for Navon...");
					PSLocator lpLoc=null;
					
					try{
						lpLoc = createLandingPage(req,info.getOwner(),info.getDependent(),m_defaultLandingCommunityId);
					}catch(Exception e){
						result.setError(e.getLocalizedMessage());
						return;
					}
				  
					//add the NavOn Landing Page link
		            try{
					createNavOnLandingPageRelationship(req, info.getDependent(), lpLoc, landingSlot);
		            }catch(Exception e){
		            	result.setError(e.getLocalizedMessage());
		            	return;
		            }
			         
					log.debug("Landing page generation complete.");
					result.setSuccess();
					return;
				}else{
					log.debug("Skipping adding Landing Page as one is already defined.");
					result.setSuccess();
					return;
				}
			
			}else{
				log.debug("Skipping adding Landing Page for non-navon type.");
				result.setSuccess();
				return;
			}
			
		} catch (PSNavException e) {
			log.error("Unable to process NavOn slots for NavOn:" + dep.getContentId(),e);
			result.setSuccess();
			return;
		}

	}
		

	/***
	 * Responsible for creating the landing page and adding it to 
	 * the same Folder as the NavOn.
	 * @throws Exception 
	 */
	private PSLocator createLandingPage(IPSRequestContext req, PSComponentSummary folder, PSComponentSummary navon, int communityId) throws Exception{
	
		   boolean changeCommunity = false;
	       String savedCommunity = null;
	       PSNavConfig config = null;
	       PSLocator lpLoc=null;
	       
		try{
		  config = PSNavConfig.getInstance(req);
		  
		  if (communityId != req.getSecurityToken().getCommunityId())
          { // the user is in a different community from the parent navon
            // we have to temporarily switch communities to save the new item
              changeCommunity = true;
              savedCommunity = PSNavUtil.getSessionCommunity(req);
              log.debug("Changing communities, old id was " + savedCommunity + ", new id is " + communityId);
              PSNavUtil.setSessionCommunity(req, communityId);
          }
			
		  
		 //Create the Landing Page type 
		   PSItemDefManager defMgr = PSItemDefManager.getInstance();
           String folderName = folder.getName();
           log.debug("adding new Landing Page to folder " + folderName);

           m_contentTypeId = defMgr.contentTypeNameToId(m_defaultContentType);
           
           PSItemDefinition lpDef = defMgr.getItemDef(m_contentTypeId, communityId);
           if (lpDef == null)
           {
               String errmsg = "Unable to find Itemdef for type {0} in community {1}. ";
               Object[] args = new Object[2];
               args[0] = config.getNavonType();
               args[1] = communityId;
               String sb = MessageFormat.format(errmsg, args);
               log.error(sb);
               throw new PSNavException(sb);
           }

           PSServerItem lp = new PSServerItem(lpDef, null, req.getSecurityToken());

           IPSFieldValue titleValue = new PSTextValue(makeLPTitle(req, folder,m_defaultLandingTitleTemplate));
           log.debug("New Landing Page name is " + titleValue.getValueAsString());
           setFieldValue(lp, "sys_title", titleValue);

           IPSFieldValue displaytitleValue = new PSTextValue(makeLPTitle(req, folder,m_defaultLandingDisplayTitleFormat));
           setFieldValue(lp, m_defaultLandingDisplayTitleField, displaytitleValue);

           setFieldValue(lp, "sys_contentstartdate", new PSDateValue(new Date()));
           log.debug("Landing Page community id " + String.valueOf(communityId));
           setFieldValue(lp, "sys_communityid", new PSTextValue(String.valueOf(communityId)));
           
           //Set the required fields with the configured default values
           if(m_defaultLandingRequiredFields != null && m_defaultLandingRequiredValues != null)
           {
        	   int i=0;
        	   for(String fld_name : m_defaultLandingRequiredFields){
        		   setFieldValue(lp, fld_name, new PSTextValue(m_defaultLandingRequiredValues[i]));        				   
        		   i++;
		       }
        	   
           }else{
        	   log.debug("No required fields configured, skipping them.");
           }
           
           
           log.debug("before new Landing Page save");
           lp.save(req.getSecurityToken());
           log.debug("after save");

           int contentId = lp.getContentId();
           log.debug("new content id is " + String.valueOf(contentId));
           int revision = lp.getRevision();
           log.debug("new revision is " + String.valueOf(revision));
           lpLoc = new PSLocator(contentId, revision);
           
           checkInItem(req, lpLoc);

           //Get the relationship proxy.
           PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
           PSRelationshipProcessorProxy relProxy = pf.getRelProxy();
           
           //add the relationship to to the folder.
           relProxy.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT, Collections.singletonList(lpLoc),
                 folder.getCurrentLocator());
         
         
         //Restore community
         if (changeCommunity)
         { // we changed communities, so we have to go back
             PSNavUtil.setSessionCommunity(req, savedCommunity);
             log.debug("Restored community to " + savedCommunity);
         }
         
		} catch (Exception e) {
			log.error("Error generating Landing Page", e);
			throw e;
		}
		 return lpLoc;
	}
	
	/***
	 * Handles create the slot relationship between the NavOn and the 
	 * @param req  Request Context
	 * @param navon  Locator for the NavOn
	 * @param lp Locator for the Landing Page
	 * @param landingSlotName Name of the NavOn Landing Page slot
	 * @throws PSNavException 
	 */
	private void createNavOnLandingPageRelationship(IPSRequestContext req,
			PSComponentSummary navon, PSLocator lp, String landingSlotName) throws PSNavException{
			
		  if (req == null)
	        {
	            throw new IllegalArgumentException("req must not be null");
	        }
	        if (navon == null)
	        {
	            throw new IllegalArgumentException("navon must not be null");
	        }
	        if (lp == null)
	        {
	            throw new IllegalArgumentException("landing page must not be null");
	        }
	        
	        log.debug("adding Landing Page to LandingPage Slot ");
	        PSNavConfig config = PSNavConfig.getInstance(req);
	        
	        PSRelationshipConfig aaConfig = config.getAaRelConfig();
	        
	        IPSAssemblyService asWs = PSAssemblyServiceLocator.getAssemblyService();
	        
	        IPSTemplateSlot lpSlot=null;
			try {
				lpSlot = asWs.findSlotByName(landingSlotName);
			} catch (PSAssemblyException e1) {
				log.debug("An exception occurred while looking up the landing page slot:" + landingSlotName,e1);
				throw new PSNavException(e1);
			}
	        
			
	        if(lpSlot == null){
	        	log.debug("Unable to locate the LandingPage slot:" + landingSlotName);
	        	throw new PSNavException("Unable to locate the LandingPage slot:" + landingSlotName);
	        }
	        log.debug("LandingPage slot is " + lpSlot.toString() + " " + landingSlotName);
	        Collection<PSPair<IPSGuid, IPSGuid>> slotTempsAndCTs = lpSlot.getSlotAssociations();
	        
	        IPSAssemblyTemplate t=null;
	        if(slotTempsAndCTs == null || slotTempsAndCTs.isEmpty()){
	        	throw new PSNavException("Unable to locate a default template for the landing page " + landingSlotName);
	        }else{
	        	IPSGuid ct;
	        	IPSGuid templateGUID = null;
	        	
	        	Iterator<PSPair<IPSGuid, IPSGuid>> it = slotTempsAndCTs.iterator();
	        	while(it.hasNext()){
	        		PSPair<IPSGuid, IPSGuid> p = it.next();
	        		
	        		    if(m_contentTypeId==p.getFirst().getUUID()){
	        		    	templateGUID = (IPSGuid)p.getSecond();
	        		    	break;
	        		    }
	        	}
	        	
	        	t = asWs.findTemplate(templateGUID);
	        }
	        
	        
	        log.debug("LP link template" + t.getName() + " " + t.toString());

	        PSLocator childLoc = (PSLocator) lp.clone();
	        
	        //Dependent of slot relationships are revisionless
	        childLoc.setRevision(-1);
	        try
	        {
	            PSAaRelationship aaRel = new PSAaRelationship(navon.getCurrentLocator(), childLoc, lpSlot, t);
	            PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
	            PSActiveAssemblyProcessorProxy aaProxy = pf.getAaProxy();

	            // aaProxy.validateAaRelationship(aaRel);
	            PSAaRelationshipList aaList = new PSAaRelationshipList();
	            log.debug("add to list " + aaList.toString());
	            aaList.add(aaRel);

	            aaProxy.addSlotRelationships(aaList, -1);
	            log.debug("Landing Page added to slot for NavOn:" + navon.getName());

	        }
	        catch (PSCmsException e)
	        {
	            throw new PSNavException("Error adding Landing Page to NavOn slot for NavOn: " + navon.getContentId(), e);
	        }
		
		
	}


	private static String makeLPTitle(final IPSRequestContext req, final PSComponentSummary folder, String template)
    {
        String pattern = template;
        if (pattern == null || pattern.trim().length() == 0)
        {
            return folder.getName() + "-LP";
        }

        Object[] parray = new Object[2];
        parray[0] = folder.getName();
        parray[1] = folder.getTipLocator().getPart(PSLocator.KEY_ID);

        return MessageFormat.format(pattern, parray);
    }


	/***
	  * Attempts a simple parse of the specified string to return CSV string represented as an array.
	  * @param arg
	  * @return Returns an array of strings holding the parameters. If no params are found, returns null.
	  */
	 public String[] getCSVList(String arg){
		 String ret[] = null;
		 
		 if(arg != null && arg.length()>0){
			 ret = arg.split(",");
			 for(int i=0;i< ret.length;i++){
				 ret[i] = ret[i].trim();
			 }
		 }
			 
		 return ret;
	 }
	 
	 /**
	     * Helper method to set a field value for a content item. Nothing happens if
	     * the specified field by name does not exist in the item.
	     * 
	     * @param item server item object must not be <code>null</code>.
	     * @param fieldName name of the field to set, must not be <code>null</code>
	     *            or empty.
	     * @param fieldValue value of the field to set, may be <code>null</code> or
	     *            empty.
	     */
	    private static void setFieldValue(PSServerItem item, String fieldName, IPSFieldValue fieldValue)
	    {
	    	//TODO:  Make this method public in NavFolderUtils so this method instance can be removed. 
	        if (item == null)
	        {
	            throw new IllegalArgumentException("item must not be null");
	        }
	        if (fieldName == null || fieldName.length() < 1)
	        {
	            throw new IllegalArgumentException("fieldName must not be null or empty");
	        }
	        PSItemField field = item.getFieldByName(fieldName);
	        if (field == null)
	        {
	            log.warn("Field " + fieldName + " not found ");
	            return;
	        }
	        field.clearValues();
	        field.addValue(fieldValue);
	    }
	    
	    /**
	     * Helper method to checkin and item specified by its locator. Makes an
	     * internal request to the content editor URL with appropriate htmnl
	     * parameters.
	     * 
	     * @param req request context object, must not be <code>null</code>.
	     * @param loc locator of the item to checkin, must nor be <code>null</code>.
	     * @throws PSNavException if it fails to check the item in.
	     */
	    public static void checkInItem(IPSRequestContext req, PSLocator loc) throws PSNavException
	    {
	    	
	    	//TODO: Expose this method in PSNavFolderUtils so it can be removed here. 
	        if (req == null)
	        {
	            throw new IllegalArgumentException("req must not be null");
	        }
	        if (loc == null)
	        {
	            throw new IllegalArgumentException("loc must not be null");
	        }
	        PSItemDefManager defMgr = PSItemDefManager.getInstance();
	        try
	        {
	            PSItemDefinition itemDef = defMgr.getItemDef(loc, req.getSecurityToken());
	            String editorURL = itemDef.getEditorUrl();
	            Map pMap = new HashMap();
	            pMap.put(IPSHtmlParameters.SYS_COMMAND, "workflow");
	            pMap.put("WFAction", "CheckIn");
	            pMap.put(IPSHtmlParameters.SYS_CONTENTID, loc.getPart(PSLocator.KEY_ID));
	            pMap.put(IPSHtmlParameters.SYS_REVISION, loc.getPart(PSLocator.KEY_REVISION));
	            IPSInternalRequest ir = req.getInternalRequest(editorURL, pMap, false);
	            ir.performUpdate();
	        }
	        catch (Exception ex)
	        {
	            throw new PSNavException(ex);
	        }
	    }

}
