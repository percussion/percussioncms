/******************************************************************************
 *
 * [ PSDirectoryServicePerformance.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSStopwatch;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This exit measures the performance of directory services and returns an
 * error if the allowed time is exceeded.
 */
public class PSDirectoryServicePerformance extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * The following HTML parameters can be used to request specific 
    * functionality from this this exit:
    * 
    * <code>tst_performance</code> - through this parameter the user can 
    *    specify the test for which to measure the performance. Known values 
    *    are <code>getUserAttribute</code> and 
    *    <code>getEmailAddressesForRole</code>.
    * 
    * <code>tst_maximumTime</code> - a string that will be interpreted as 
    *    <code>double</code> containing the maximum request time allowed. If 
    *    the supplied maximal request time is exceeded, a 
    *    <code>PSExtensionProcessingException</code> will be thrown.
    * 
    * <code>tst_attribute</code> - this parameter is expected for the 
    *    performance method <code>getUserAttribute</code>.
    * 
    * <code>tst_role</code> - this parameter is expected for the performance 
    *    method <code>getEmailAddressesForRole</code>.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String test = request.getParameter("tst_performance");
      if (test == null)
         throw new PSParameterMismatchException(0, 
            "tst_performance cannot be null");
         
      String time = request.getParameter("tst_maximumTime");
      if (time == null)
         throw new PSParameterMismatchException(0, 
            "tst_maximumTime cannot be null");
      double maximumTime = Double.parseDouble(time);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("DirectoryServicesPerformanceResults");
      doc.appendChild(root);
      
      if (test.equals(TEST_GET_USER_ATTRIBUTE))
      {
         String attribute = request.getParameter("tst_attribute");
         if (attribute == null || attribute.trim().length() == 0)
            throw new PSParameterMismatchException(0, 
               "tst_attribute cannot be null or empty");
         
         getUserAttribute(request, attribute, maximumTime, doc);
      }
      else if (test.equals(TEST_GET_EMAIL_ADDRESSES_FOR_ROLE))
      {
         String role = request.getParameter("tst_role");
         if (role == null || role.trim().length() == 0)
            throw new PSParameterMismatchException(0, 
               "tst_role cannot be null or empty");
         
         getEmailAddressesForRole(request, role, maximumTime, doc);
      }
      else
         throw new PSParameterMismatchException(0, "unknown performance test");
      
      return doc;
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * Does a lookup of the specified attribute for the user making the request.
    * 
    * @param request the request use to make the lookup, assumed not 
    *    <code>null</code>.
    * @param attribute the name of the attribute to lookup, assumed not 
    *    <code>null</code> or empty.
    * @param maximumTime the maximum allowed time to make the lookup.
    * @param doc the document to which to append the lookup result, assumed
    *    not <code>null</code>.
    * @throws PSExtensionProcessingException if the lookup request time
    *    exceeds the maximum allowed time.
    */
   private void getUserAttribute(IPSRequestContext request, String attribute,
      double maximumTime, Document doc) throws PSExtensionProcessingException
   {
      String user = request.getUserName();
      
      PSStopwatch stopwatch = new PSStopwatch();
      stopwatch.start();
      List subjectList = request.getSubjectGlobalAttributes(user, 
         PSSubject.SUBJECT_TYPE_USER, null, attribute, true);
      stopwatch.stop();
      
      if (stopwatch.elapsed() > maximumTime)
         throw new PSExtensionProcessingException(0, "maximum time " + 
            maximumTime + " exceeded: " + stopwatch.elapsed());
      
      Element result = doc.createElement("GetUserAttribute");
      result.setAttribute("user", user);
      Iterator subjects = subjectList.iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         result.appendChild(subject.toXml(doc));
      }
      
      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * Does a lookup of all email addresses for the specified role.
    * 
    * @param request the request use to make the lookup, assumed not 
    *    <code>null</code>.
    * @param role the role for which to make the lookup, assumed not 
    *    <code>null</code> or empty.
    * @param maximumTime the maximum allowed time to make the lookup.
    * @param doc the document to which to append the lookup result, assumed
    *    not <code>null</code>.
    * @throws PSExtensionProcessingException if the lookup request time
    *    exceeds the maximum allowed time.
    */
   private void getEmailAddressesForRole(IPSRequestContext request, String role, 
      double maximumTime, Document doc) throws PSExtensionProcessingException
   {
      PSStopwatch stopwatch = new PSStopwatch();
      stopwatch.start();
      Set emailAddressSet = request.getRoleEmailAddresses(role, null, null);
      stopwatch.stop();
      
      if (stopwatch.elapsed() > maximumTime)
         throw new PSExtensionProcessingException(0, "maximum time " + 
            maximumTime + " exceeded: " + stopwatch.elapsed());
      
      Element result = doc.createElement("GetEmailAddressesForRole");
      result.setAttribute("role", role);
      Iterator emailAddresses = emailAddressSet.iterator();
      while (emailAddresses.hasNext())
      {
         String emailAddress = (String) emailAddresses.next();
         
         Element elem = doc.createElement("String");
         Text text = doc.createTextNode(emailAddress);
         elem.appendChild(text);

         result.appendChild(elem);
      }
      
      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * Constant used for the test method <code>getUserAttribute</code>.
    */
   private static final String TEST_GET_USER_ATTRIBUTE = "getUserAttribute";
   
   /**
    * Constant used for the test method <code>getEmailAddressesForRole</code>.
    */
   private static final String TEST_GET_EMAIL_ADDRESSES_FOR_ROLE = 
      "getEmailAddressesForRole";
}
