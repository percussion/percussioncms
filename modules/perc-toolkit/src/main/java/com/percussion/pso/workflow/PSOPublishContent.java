/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.workflow PSOPublishContent.java
 *  
 * @author DavidBenua
 *
 */
package com.percussion.pso.workflow;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class PSOPublishContent extends PSDefaultExtension
      implements
         IPSWorkflowAction
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOPublishContent.class);

   
   private static PublishEditionService svc = null; 
   /**
    * 
    */
   public PSOPublishContent()
   {
      super();
   }
   
   private static void initServices()
   {
      if(svc == null)
      {
         svc = PSOPublishEditionServiceLocator.getPublishEditionService();
      }
   }
   /**
    * @see com.percussion.extension.IPSWorkflowAction#performAction(com.percussion.extension.IPSWorkFlowContext, com.percussion.server.IPSRequestContext)
    */
   public void performAction(IPSWorkFlowContext wfContext,
         IPSRequestContext request) throws PSExtensionProcessingException
   {
       initServices();
       
       int workflowId = wfContext.getWorkflowID();
       int transitionId = wfContext.getTransitionID();
       String community = (String)request.getSessionPrivateObject(IPSHtmlParameters.SYS_COMMUNITY);
       if(StringUtils.isBlank(community))
       {
          String errmsg = "Community must not be blank"; 
          log.error(errmsg); 
          throw new PSExtensionProcessingException(0, errmsg);         
       }
       int communityid = Integer.parseInt(community); 
       
       int editionId = svc.findEdition(workflowId, transitionId, communityid); 
       log.debug("found edition " + editionId);
       svc.runEdition(String.valueOf(editionId));
       
   }
}
