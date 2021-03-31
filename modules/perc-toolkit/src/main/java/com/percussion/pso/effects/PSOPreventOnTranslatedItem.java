/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.effects;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSEffectContext;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

/**
 * A Percussion CMS relationship effects that prevents operations on 
 * content items which are Translated.
 * <p>
 * It can be used with any relationship (you must add it to the relationship configuration), 
 * but it is intended to prevent creating a Translation of a Translation
 * or a Promotable Version of a Translation. 
 * </p> 
 * @author DavidBenua
 *
 */
@PSHandlesEffectContext(optional=
{PSEffectContext.PRE_CONSTRUCTION,PSEffectContext.PRE_DESTRUCTION,
		PSEffectContext.PRE_UPDATE,PSEffectContext.PRE_CHECKIN,PSEffectContext.PRE_CLONE,
		PSEffectContext.PRE_WORKFLOW})
public class PSOPreventOnTranslatedItem implements IPSEffect
{

	/**
	 * Logger for this class
	 */
	private static final Log log = LogFactory.getLog(PSEffectLoggingEffect.class);

	protected static IPSContentWs cws = null; 
	protected static IPSSystemWs sws = null; 
	protected static IPSGuidManager gmgr = null; 
	

	/**
	 * Initialize service pointers. 
	 */
	protected static void initServices()
	{
		if(cws == null)
		{
			cws = PSContentWsLocator.getContentWebservice();
			sws = PSSystemWsLocator.getSystemWebservice();
			gmgr = PSGuidManagerLocator.getGuidMgr();
			
		}
	}


	/**
	 * Tests if this relationship owner is a translation of some
	 * other item. 
	 */
	public void test(Object[] params, IPSRequestContext req,
			IPSExecutionContext exCtx, PSEffectResult result)
	throws PSExtensionProcessingException, PSParameterMismatchException
	{
		initServices();
	
		if(!exCtx.isConstruction() && !exCtx.isDestruction())
		{         
			return; 
		}
		try
		{

			int owner = exCtx.getCurrentRelationship().getOwner().getId();

			int transownerId = findTranslationOwner(owner);

			if (transownerId > 1) {
				log.debug("Item is a translation of "+transownerId+" preventing relationship");
				result.setError(MSG_TRANSLATED_ITEM); 
				return;
			}

		} catch (Exception e)
		{
			log.error("unexpected exception", e); 
			throw new PSExtensionProcessingException(this.getClass().getName(), e); 
		} 

		result.setSuccess();
	}

	private static final String MSG_TRANSLATED_ITEM = 
		"This operation is not valid on translated items";

	public void attempt(Object[] params, IPSRequestContext request,
			IPSExecutionContext context, PSEffectResult result)
	throws PSExtensionProcessingException, PSParameterMismatchException {
		result.setSuccess();
	}


	public void recover(Object[] params, IPSRequestContext request,
			IPSExecutionContext context, PSExtensionProcessingException e,
			PSEffectResult result) throws PSExtensionProcessingException {
		result.setSuccess();
	}


	public void init(IPSExtensionDef def, File codeRoot)
	throws PSExtensionException {
		
	} 

	public int findTranslationOwner(int id) throws PSErrorException {
		
		
		PSRelationshipFilter filter = new PSRelationshipFilter();
		filter.setCategory(PSRelationshipFilter.FILTER_CATEGORY_TRANSLATION);
		PSLocator dependent= new PSLocator(id,-1); 
		IPSGuid guid = gmgr.makeGuid(dependent);
		filter.limitToEditOrCurrentOwnerRevision(true);
		List<IPSGuid> parents = sws.findOwners(guid, filter);
		

		if (parents.size()>1) {
			log.error("Item "+id+" has more than one translation parent");
			return -1;
		} else if(parents.size()==1){
			return parents.get(0).getUUID();
		} else {
			return -1;
		}

	}
}
