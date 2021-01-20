package com.percussion.fastforward;

import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;

import org.w3c.dom.Document;

/**
 * Generates the file name for the content item specified by the the HTML
 * parameters
 * {@link com.percussion.util.IPSHtmlParameters#SYS_CONTENTID contentid}and
 * {@link com.percussion.util.IPSHtmlParameters#SYS_REVISION revision}in the
 * request context. This is done by executing an internal request to the
 * Rhythmyx resource specified by the third parameter to the exit. Nothing will
 * be done if the second parameter is non-empty or if the html parameter
 * {@link com.percussion.util.IPSHtmlParameters#SYS_COMMAND} in the request is
 * not {@link com.percussion.cms.handlers.PSModifyCommandHandler#COMMAND_NAME}
 */
public class PSAutoGenerateFileName extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{

   /**
    * Required by the interface. Always return <code>false</code>.
    * 
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    * @return false
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Implementation of the interface method.
    * 
    * @param params parameters required for this exit. Three parameters are
    *           expected as described below:
    *           <p>
    *           if the second parameter is non-empty, the third parameter can be
    *           empty.
    *           </p>
    *           <p>
    *           otherwise, the third parameter must be non-empty and should
    *           represent the update resource that updates the filename in the
    *           database.
    * @param request The request context used to execute the internal request,
    *           must not be <code>null</code>.
    * @param doc The input document that is passed, never used.
    * @return The result document same as the input result document.
    * @throws PSParameterMismatchException
    * @throws PSExtensionProcessingException
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND, "");
      //Exit is effective only if the sys_command is "modify"
      if(!command.equals(PSModifyCommandHandler.COMMAND_NAME))
      {
         return doc;
      }
      if (params.length < 3)
      {
         throw new PSParameterMismatchException(0,
               "Atleast 3 parameter required");
      }
      if (params[0] == null || params[0].toString().trim().equals(""))

         throw new PSParameterMismatchException(0,
               "Default Filename should not be empty");

      if (params[1] == null || params[1].toString().trim().equals(""))
      {
         request.printTraceMessage(
               "Creating filename because it was not passed in ");
      }
      else
      {
         return doc;
      }

      if (params[2] == null || params[2].toString().trim().equals(""))
         throw new PSParameterMismatchException(0,
               "Update resource should not be empty");

      String defaultFilename = params[0].toString();
      String updateresource = params[2].toString();
      request.printTraceMessage("Default filename = " + defaultFilename);
      request.printTraceMessage("Update Resource = " + updateresource);

      String contentid = request.getParameter(IPSHtmlParameters.SYS_CONTENTID);
      String revisionid = request.getParameter(IPSHtmlParameters.SYS_REVISION);
      if (contentid == null)
      {
         request.printTraceMessage("The contentid is null. Return");
         return doc;
      }

      String filename = defaultFilename + contentid;
      HashMap paramMap = new HashMap();
      paramMap.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
      paramMap.put(IPSHtmlParameters.SYS_REVISION, revisionid);
      paramMap.put("defaultFilename", filename);
      paramMap.put("DBActionType", "UPDATE");

      try
      {
         IPSInternalRequest irq = request.getInternalRequest(updateresource,
               paramMap, false);
         irq.performUpdate();
      }
      catch (PSInternalRequestCallException irce)
      {
         irce.printStackTrace();
      }
      catch (PSAuthorizationException paex)
      {
         paex.printStackTrace();
      }
      catch (PSAuthenticationFailedException pafe)
      {
         pafe.printStackTrace();
      }
      return doc;
   }

}