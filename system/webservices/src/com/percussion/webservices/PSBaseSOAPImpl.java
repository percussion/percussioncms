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
package com.percussion.webservices;

import com.percussion.security.PSAuthorizationException;
import com.percussion.services.security.PSServletRequestWrapper;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.system.RelationshipCategory;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.rmi.RemoteException;
import java.util.Iterator;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.Attachments;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

/**
 * This base class implements generic functionality available with every
 * axis SOAP implementation. 
 */
public class PSBaseSOAPImpl
{


    /**
     * The logger for this class.
     */
    private static Logger ms_logger = Logger.getLogger(PSBaseSOAPImpl.class);


    /**
    * Get the http servlet request associated with the current axis message.
    * 
    * @return the http servlet request, never <code>null</code>.
    */
   protected HttpServletRequest getServletRequest()
   {
      MessageContext context = MessageContext.getCurrentContext();
      return (HttpServletRequest) context.getProperty(
         HTTPConstants.MC_HTTP_SERVLETREQUEST);
   }
   
   /**
    * Get the http servlet response associated with the current axis message.
    * 
    * @return the http servlet response, never <code>null</code>.
    */
   protected HttpServletResponse getServletResponse()
   {
      MessageContext context = MessageContext.getCurrentContext();
      return (HttpServletResponse) context.getProperty(
         HTTPConstants.MC_HTTP_SERVLETRESPONSE);
   }
   
   /**
    * Get the rhythmyx session from the SOAP headers.
    * 
    * @return the rhythmyx session supplied with a message SOAP header,
    *    never <code>null</code> or empty.
    * @throws SOAPException for any error looking up the rhythmyx session
    *    SOAP header. 
    */
   protected String getRhythmyxSession() throws SOAPException
   {
      String session = null;
      
      MessageContext context = MessageContext.getCurrentContext();
      Message message = context.getCurrentMessage();
      SOAPHeader soapHeader = message.getSOAPHeader();

      Iterator headers = soapHeader.extractHeaderElements(
         SOAPConstants.URI_SOAP_ACTOR_NEXT);
      while (session == null && headers.hasNext())
      {
         SOAPElement header = (SOAPElement) headers.next();
         // try the header formate from Axis client in Java
         if (header.getElementName().getLocalName().equals("session"))
         {
            session = header.getValue();
         }
         // try the header format from Microsoft .NET client
         else if (header.getElementName().getLocalName().equals(
               "PSAuthenticationHeader"))
         {
            Iterator children = header.getChildElements();
            if (children.hasNext())
            {
               Node childEl = (Node) children.next();
               if (childEl instanceof SOAPElement)
               {
                  SOAPElement sessEl = (SOAPElement) childEl;
                  if (sessEl.getElementName().getLocalName().equals("Session")) 
                  {
                     session = sessEl.getValue();
                  }
               }
            }
         }
      }
      
      if (session == null)
         throw new SOAPException("Required rhythmyx session header not found.");
      
      return session;
   }
   
   /**
    * Get all message attachments.
    * 
    * @return an array over all message attachments, never <code>null</code>,
    *    may be empty.
    * @throws AxisFault for any error getting the attachments.
    */
   protected AttachmentPart[] getAttachments() throws AxisFault
   {
      MessageContext context = MessageContext.getCurrentContext();
      Message msg = context.getRequestMessage();
      Attachments attachments = msg.getAttachmentsImpl();
      if (null == attachments)
         return new AttachmentPart[0];
      
      int attachmentCount = attachments.getAttachmentCount();
      AttachmentPart attachmentParts[] = new AttachmentPart[attachmentCount];
      Iterator it = attachments.getAttachments().iterator();
      int count = 0;
      while (it.hasNext())
      {
         AttachmentPart part = (AttachmentPart) it.next();
         attachmentParts[count++] = part;
      }
      
      return attachmentParts;
   }
   
   /**
    * Authenticate the the current message context. This retrieves the 
    * required rhythmyx session header from the message context and 
    * authenticates that.
    * 
    * @return the authenticated rhythmyx session, never <code>null</code> or
    *    empty.
    * @throws PSInvalidSessionFault if the current message context does not
    *    contain a valid rhythmyx session. 
    */
   protected String authenticate() throws PSInvalidSessionFault
   {
      try
      {
         String sessionId = getRhythmyxSession();
         PSSecurityFilter.authenticate(getServletRequest(),sessionId);
         
         return sessionId;
      }
      catch (LoginException e)
      {
         int code = IPSWebserviceErrors.INVALID_SESSION;
         ms_logger.error("Authenication Error",e);
         throw new PSInvalidSessionFault(code, 
            PSWebserviceErrors.createErrorMessage(code, e.toString()), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (SOAPException e)
      {
         int code = IPSWebserviceErrors.MISSING_SESSION;
         ms_logger.error("Authenication Error",e);
         throw new PSInvalidSessionFault(code, 
            PSWebserviceErrors.createErrorMessage(code, e.toString()), 
            ExceptionUtils.getFullStackTrace(e));
      }
   }
   
   /**
    * Get the remote user.
    * 
    * @return the remote user, may be <code>null</code> if not authenticated,
    *    never empty.
    */
   protected String getRemoteUser()
   {
      HttpServletRequest request = getServletRequest();
      if (request instanceof PSServletRequestWrapper)
         return ((PSServletRequestWrapper) request).getRemoteUser();
      
      return request.getRemoteUser();
   }
   
   /**
    * Converts the supplied source object to the specified type.
    * 
    * @param type the class type to which to convert the supplied source, 
    *    not <code>null</code>.
    * @param source the object to convert, not <code>null</code>.
    * @return the transformed object, never <code>null</code>.
    */
   protected Object convert(Class type, Object source) 
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      Converter converter = factory.getConverter(type);
      
      return converter.convert(type, source);
   }
   
   /**
    * Extract the boolean value from the supplied <code>Boolean</code> object.
    * 
    * @param value the <code>Boolean</code> value from which to extract the 
    *    boolean value, may be <code>null</code> in which case the specified 
    *    default will be returned.
    * @param defaultValue the default value to be returned if the supplied 
    *    value is <code>null</code>.
    * @return the extracted boolean value or the specified default.
    */
   protected boolean extractBooleanValue(Boolean value, boolean defaultValue)
   {
      return value == null ? defaultValue : value.booleanValue();
   }

   /**
    * Convenience method. Converts {@link IllegalArgumentException} to 
    * {@link PSContractViolationFault}
    * 
    * @param e the exception, it may not be <code>null</code>.
    * @param serviceName the service name, it may not be <code>null</code>.
    * 
    * @throws PSContractViolationFault the converted exception. 
    */
   protected void handleInvalidContract(IllegalArgumentException e,
         String serviceName) throws PSContractViolationFault 
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null.");
      if (serviceName == null)
         throw new IllegalArgumentException("serviceName may not be null.");
      
      int code = IPSWebserviceErrors.INVALID_CONTRACT;

      ms_logger.error("SOAP Invalid Contract for service "+serviceName,e);

      throw new PSContractViolationFault(code,
            PSWebserviceErrors.createErrorMessage(code, serviceName, e.toString()), ExceptionUtils.getFullStackTrace(e));
   }   
   
   /**
    * Converts the received <code>RuntimeException</code> into the correct 
    * axis fault. If the cause was a <code>PSAuthorizationException</code> it 
    * is converted to a <code>PSNotAuthorizedFault</code>, otherwise it is 
    * converted to a <code>RemoteException</code>.
    * 
    * @param e the runtime exception to convert, not <code>null</code>.
    * @param serviceName the name of the service which caused the supplied 
    *    exception, not <code>null</code>.
    * @throws PSNotAuthorizedFault if the root cause was a 
    *    <code>PSAuthorizationException</code>.
    * @throws RemoteException for all other runtime exceptions.
    */
   protected void handleRuntimeException(RuntimeException e, String serviceName)
      throws PSNotAuthorizedFault, RemoteException
   {
      if (e == null)
         throw new IllegalArgumentException("a cannot be null");
      
      if (serviceName == null)
         throw new IllegalArgumentException("serviceName cannot be null");



      if (e.getCause() instanceof PSAuthorizationException)
      {
          ms_logger.debug("SOAP PSAuthorizationException for service "+serviceName,e);
         int code = IPSWebserviceErrors.NOT_AUTHORIZED;
         throw new PSNotAuthorizedFault(code,
            PSWebserviceErrors.createErrorMessage(code, getRemoteUser(), 
               serviceName, e.toString()),
               ExceptionUtils.getFullStackTrace(e));
      }
      else
          ms_logger.error("SOAP RuntimeException for service "+serviceName,e);
      
      throw new RemoteException(e.toString(), e);
   }

   /**
    * Convenience method. Converts {@link PSErrorsException} to 
    * {@link PSErrorsFault}
    *  
    * @param e the to be converted exception, it may not be <code>null</code>.
    * @param serviceName the service name, it may not be <code>null</code>.
    * 
    * @throws RemoteException if error occurred during conversion.
    */
   protected void handleErrorResultsException(PSErrorResultsException e, 
         String serviceName) throws RemoteException
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null.");
      if (serviceName == null)
         throw new IllegalArgumentException("serviceName may not be null.");

      ms_logger.debug("SOAP PSErrorResultsException for service "+serviceName,e);
      PSErrorResultsFault fault = (PSErrorResultsFault) convert(
         PSErrorResultsFault.class, e);
      fault.setService(serviceName);
      
      throw fault;
   }
   
   /**
    * Convenience method. Converts {@link PSErrorsException} to 
    * {@link PSErrorsFault}
    *  
    * @param e the to be converted exception, it may not be <code>null</code>.
    * @param serviceName the service name, it may not be <code>null</code>.
    * 
    * @throws RemoteException if error occurred during conversion.
    */
   protected void handleErrorsException(PSErrorsException e, String serviceName)
         throws RemoteException
   {
      if (e == null)
         throw new IllegalArgumentException("e may not be null.");
      if (serviceName == null)
         throw new IllegalArgumentException("serviceName may not be null.");

      ms_logger.debug("SOAP PSErrorsException for service "+serviceName,e);
      PSErrorsFault fault = (PSErrorsFault) convert(
         PSErrorsFault.class, e);
      fault.setService(serviceName);
      
      throw fault;
   }

   /**
    * Convenience method, converts {@link PSLockErrorException}
    * to {@link PSLockFault} and throws the converted exception.
    * 
    * @param e the to be converted exception, may not be <code>null</code>.
    * 
    * @throws PSLockFault if succesfully converted the exception.
    * @throws RemoteException if failed to convert the exception.
    */
   protected void handleLockError(PSLockErrorException e) 
      throws PSLockFault, RemoteException
   {
       ms_logger.debug("SOAP PSLockErrorException",e);
      PSLockFault fault = (PSLockFault) convert(
         PSLockFault.class, e);
      
      throw fault;
   }
   
   /**
    * Converts the value of relationship category from webservice to 
    * objectstore (which is defined in PSRelationshipConfig.CATEGORY_XXX).
    *  
    * @param cat the to be converted category, it may be <code>null</code>.
    *    If it is not <code>null</code>, then it has to be one of the 
    *    pre-defined values in {@link RelationshipCategory}.
    * 
    * @return the converted category, which is one of the values in
    *    PSRelationshipConfig.CATEGORY_XXX. It may be <code>null</code> if the
    *    supplied value is <code>null</code>.
    *    
    * @throws IllegalArgumentException if the supplied category is not 
    *    <code>null</code> and does not match any pre-defined values in 
    *    {@link RelationshipCategory}
    */
   protected String getRelationshipCategory(RelationshipCategory cat)
   {
      if (cat == null)
         return null;
      
      String scat = cat.getValue();
      if (RelationshipCategory.ActiveAssembly.getValue().equals(scat))
         return CATEGORY_ACTIVE_ASSEMBLY;

      if (RelationshipCategory.Folder.getValue().equals(scat))
         return CATEGORY_FOLDER;
         
      if (RelationshipCategory.Promotable.getValue().equals(scat))
         return CATEGORY_PROMOTABLE;
      
      if (RelationshipCategory.Copy.getValue().equals(scat))
         return CATEGORY_COPY;
      
      throw new IllegalArgumentException(
            "Relationship Category must match one of the pre-defined values in RelationshipCategory if not null.");
   }
   
   // category contants defined in com.percussion.design.objectstore.PSRelationshipConfig
   private final static String CATEGORY_ACTIVE_ASSEMBLY = com.percussion.design.objectstore.PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY;
   private final static String CATEGORY_FOLDER = com.percussion.design.objectstore.PSRelationshipConfig.CATEGORY_FOLDER;
   private final static String CATEGORY_PROMOTABLE = com.percussion.design.objectstore.PSRelationshipConfig.CATEGORY_PROMOTABLE;
   private final static String CATEGORY_COPY = com.percussion.design.objectstore.PSRelationshipConfig.CATEGORY_COPY;

}

