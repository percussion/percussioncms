/******************************************************************************
 *
 * [ PSDirectoryServicesPublicAPI.java ]
 * 
 * COPYRIGHT (c) 1999 - 2004 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.community.PSAuthenticateUser;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * This exit is used to test all public directory service functionality
 * supplied through the <code>IPSRequestContext</code> interface.
 */
public class PSDirectoryServicesPublicAPI extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * Calls the requested public directory service methods through the 
    * supplied request. The  results are returned in a document conforming 
    * to the DTD described in Directory Service Test Plan 
    * under III - Public API tests.
    * 
    * This method takes one optional HTML parameter named 
    * <code>tst_method</code>. If the parameter is not supplied, all methods
    * are called. Otherwise just the requested method is called. Valid entries
    * are:
    * <ol>
    * <li>getRoleAttributes</li>
    * <li>getRoleEmailAddresses</li>
    * <li>getRoles</li>
    * <li>getRoleSubjects</li>
    * <li>getSubjectEmailAddresses</li>
    * <li>getSubjectGlobalAttributes</li>
    * <li>getSubjectRoleAttributes</li>
    * <li>getSubjectRoles</li>
    * <li>getSubjects</li>
    * </ol>
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement("DirectoryServicesPublicAPIResults");
      doc.appendChild(root);
      
      String method = request.getParameter("tst_method");
      
      if (method == null || method.equals("getRoleAttributes"))
         testGetRoleAttributes(request, doc);

      if (method == null || method.equals("getRoleEmailAddresses"))
         testGetRoleEmailAddresses(request, doc);

      if (method == null || method.equals("getRoles"))
         testGetRoles(request, doc);

      if (method == null || method.equals("getRoleSubjects"))
         testGetRoleSubjects(request, doc);

      if (method == null || method.equals("getSubjectEmailAddresses"))
         testGetSubjectEmailAddresses(request, doc);

      if (method == null || method.equals("getSubjectGlobalAttributes"))
         testGetSubjectGlobalAttributes(request, doc);

      if (method == null || method.equals("getSubjectRoleAttributes"))
         testGetSubjectRoleAttributes(request, doc);

      if (method == null || method.equals("getSubjectRoles"))
         testGetSubjectRoles(request, doc);

      if (method == null || method.equals("getSubjects"))
         testGetSubjects(request, doc);
      
      return doc;
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getRoleAttributes(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetRoleAttributes</code> elements.
    * 
    * The tests are performed for all roles in which the request user is a
    * member.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetRoleAttributes(IPSRequestContext request, Document doc)
   {
      Iterator roles = request.getSubjectRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         if (role != null)
         {
            Element result = doc.createElement("GetRoleAttributes");
            result.setAttribute("role", role);

            Iterator attributes = request.getRoleAttributes(role).iterator();
            while (attributes.hasNext())
            {
               PSAttribute attribute = (PSAttribute) attributes.next();
               result.appendChild(attribute.toXml(doc));
            }
            
            doc.getDocumentElement().appendChild(result);
         }
      }
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getRoleEmailAddresses(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetRoleEmailAddresses</code> elements.
    * 
    * The tests are performed for all roles in which the request user is a
    * member.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetRoleEmailAddresses(IPSRequestContext request, 
      Document doc)
   {
      Iterator roles = request.getSubjectRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         if (role != null)
         {
            Element result = doc.createElement("GetRoleEmailAddresses");
            result.setAttribute("role", role);

            Iterator emailAddresses = request.getRoleEmailAddresses(
               role, null, null).iterator();
            while (emailAddresses.hasNext())
            {
               String emailAddress = (String) emailAddresses.next();
               result.appendChild(createStringElement(emailAddress, doc));
            }
            
            doc.getDocumentElement().appendChild(result);
         }
      }
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getRoles(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetRoles</code> elements.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetRoles(IPSRequestContext request, Document doc)
   {
      Element result = doc.createElement("GetRoles");
      
      Iterator roles = request.getRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         result.appendChild(createStringElement(role, doc));
      }
      
      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getRoleSubjects(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetRoleSubjects</code> elements.
    * 
    * The tests are performed for the <code>Admin</code> role and for the
    * <code>Default</code>.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetRoleSubjects(IPSRequestContext request, Document doc)
   {
      String role = "Admin";
      
      // get role subjects of all types, no filter set
      doc.getDocumentElement().appendChild(getRoleSubjects(request, doc, 
         role, 0, null));

      // get role subjects of type user, no filter set
      doc.getDocumentElement().appendChild(getRoleSubjects(request, doc, 
         role, PSSubject.SUBJECT_TYPE_USER, null));

      // get role subjects of type group, no filter set
      doc.getDocumentElement().appendChild(getRoleSubjects(request, doc, 
         role, PSSubject.SUBJECT_TYPE_GROUP, null));

      // get role subjects of all types, with filter set
      doc.getDocumentElement().appendChild(getRoleSubjects(request, doc, 
         role, 0, "a%"));
      
      role ="Default";

      // get role subjects of all types, with filter set
      doc.getDocumentElement().appendChild(getRoleSubjects(request, doc, 
         role, 0, "a%"));
   }
   
   /**
    * Calls the appropriate <code>IPSRequestContext.getRoleSubjects(...)</code>
    * method depending on the supplied parameters and returns the result.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    * @param role the role for which to get the subjects, assumed not
    *    <code>null</code> or empty.
    * @param type the subject type by which to filter, assumed one of 0 or
    *    <code>PSSubject.SUBJECT_TYPE_xxx</code>
    * @param filter a string by which to filter the subjects name, may be 
    *    <code>null</code>, assumed not empty.
    * @return a <code>GetRoleSubjects</code> element with all results,
    *    never <code>null</code>.
    */
   private Element getRoleSubjects(IPSRequestContext request, Document doc, 
      String role, int type, String filter)
   {
      Element result = doc.createElement("GetRoleSubjects");
      result.setAttribute("role", role);
      if (type != 0)
         result.setAttribute("type", "" + type);
      if (filter != null)
         result.setAttribute("filter", filter);

      Iterator subjects = null;
      if (type == 0 && filter == null)
         subjects = request.getRoleSubjects(role).iterator();
      else
         subjects = request.getRoleSubjects(role, type, filter).iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         result.appendChild(subject.toXml(doc));
      }
      
      return result;
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getSubjectEmailAddresses(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetSubjectEmailAddresses</code> elements.
    * 
    * The tests are performed for the current user and community.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetSubjectEmailAddresses(IPSRequestContext request, 
      Document doc)
   {
      String subject = request.getUserName();
      
      doc.getDocumentElement().appendChild(getSubjectEmailAddresses(request, 
         doc, subject, null, null));
      
      doc.getDocumentElement().appendChild(getSubjectEmailAddresses(request, 
         doc, subject, SYS_EMAIL_ATTRIBUTE_NAME, null));

      doc.getDocumentElement().appendChild(getSubjectEmailAddresses(request, 
         doc, subject, SYS_EMAIL_ATTRIBUTE_NAME, getDefaultCommunity(request)));
   }
   
   /**
    * Get the current users default community.
    * 
    * @param request the request for which to get the default community,
    *    assumed not <code>null</code>.
    * @return the default community for the current user, may be 
    *    <code>null</code> if not found.
    */
   private String getDefaultCommunity(IPSRequestContext request)
   {
      String community = null;
      
      try
      {
         community = PSAuthenticateUser.getUserDefaultCommunity(request);
      }
      catch (Exception e)
      {
         // ignore
      }
      
      return community;
   }
   
   /**
    * Calls the appropriate 
    * <code>IPSRequestContext.getSubjectEmailAddresses(...)</code>
    * method depending on the supplied parameters and returns the result.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    * @param subject the subject for which to get the email addresses,
    *    assumed not <code>null</code> or empty.
    * @param attributeName the attribute name to lookup the backend email
    *    attribute, may be <code>null</code>, assumed not empty.
    * @param community the community for which to filter the email addresses,
    *    may be <code>null</code>, assumed not empty.
    * @return a <code>GetSubjectEmailAddresses</code> element with all results,
    *    never <code>null</code>.
    */
   private Element getSubjectEmailAddresses(IPSRequestContext request, 
      Document doc, String subject, String attributeName, String community)
   {
      Element result = doc.createElement("GetSubjectEmailAddresses");

      result.setAttribute("subject", subject);
      if (community != null)
         result.setAttribute("community", community);
      
      Iterator emailAddresses = request.getSubjectEmailAddresses(
         subject, attributeName, community).iterator();
      while (emailAddresses.hasNext())
      {
         String emailAddress = (String) emailAddresses.next();
         result.appendChild(createStringElement(emailAddress, doc));
      }
      
      return result;
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getSubjectGlobalAttributes(...)</code> method. The
    * test results will be appendend to the supplied document as 
    * <code>GetSubjectGlobalAttributes</code> elements.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetSubjectGlobalAttributes(IPSRequestContext request, 
      Document doc)
   {
      doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(request, 
         doc, null, 0, null, null, false, null));

      try
      {
         String user = (String) request.getUserContextInformation(
            "User/Name", "").toString();
         if (user != null)
            doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(
               request, doc, user, 0, null, null, true, null));
      }
      catch (PSException e)
      {
         // ignore
      }

      doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(request, 
         doc, "%nested%", PSSubject.SUBJECT_TYPE_GROUP, null, null, false, 
         null));

      doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(request, 
         doc, null, 0, "Q%", null, false, null));

      doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(request, 
         doc, null, 0, null, "street", false, null));

      String community = getDefaultCommunity(request);
      doc.getDocumentElement().appendChild(getSubjectGlobalAttributes(request, 
         doc, "%level%", 0, null, null, false, community));
   }

   /**
    * Calls the appropriate 
    * <code>IPSRequestContext.getSubjectGlobalAttributes(...)</code>
    * method depending on the supplied parameters and returns the result.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    * @param subjectNameFilter the subject name to filter by, may be 
    *    <code>null</code>, assumed not empty.
    * @param type the subject type to filter by, assumed one of 0 or 
    *    <code>PSSubject.SUBJECT_TYPE_xxx</code>.
    * @param roleNameFilter the role name to filter by, may be 
    *    <code>null</code>, assumed not empty.
    * @param attributeNameFilter the attribute name to filter by, may be
    *    <code>null</code>, assumed not empty. 
    * @param includeEmpty <code>true</code> to include empty subjects,
    *    <code>false</code> otherwise.
    * @param community the community to filter by, may be 
    *    <code>null</code>, assumed not empty.
    * @return a <code>GetSubjectGlobalAttributes</code> element with all 
    *    results, never <code>null</code>.
    */
   private Element getSubjectGlobalAttributes(IPSRequestContext request, 
      Document doc, String subjectNameFilter, int type, String roleNameFilter,
      String attributeNameFilter, boolean includeEmpty, String community)
   {
      Element result = doc.createElement("GetSubjectGlobalAttributes");
      result.setAttribute("includeEmpty", includeEmpty ? "yes" : "no");
      if (subjectNameFilter != null)
         result.setAttribute("subjectNameFilter", subjectNameFilter);
      if (type != 0)
         result.setAttribute("type", "" + type);
      if (roleNameFilter != null)
         result.setAttribute("roleNameFilter", roleNameFilter);
      if (attributeNameFilter != null)
         result.setAttribute("attributeNameFilter", attributeNameFilter);
      if (community != null)
         result.setAttribute("community", community);
      
      Iterator subjects = null;
      if (!includeEmpty && subjectNameFilter == null && type == 0 && 
         roleNameFilter == null && attributeNameFilter == null && 
         community == null)
         subjects = request.getSubjectGlobalAttributes().iterator();
      else
         subjects = request.getSubjectGlobalAttributes(subjectNameFilter, type, 
            roleNameFilter, attributeNameFilter, includeEmpty, 
            community).iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         result.appendChild(subject.toXml(doc));
      }
      
      return result;
   }

   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getSubjectRoleAttributes(...)</code> method. 
    * The test results will be appendend to the supplied document as 
    * <code>GetSubjectRoleAttributes</code> elements.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetSubjectRoleAttributes(IPSRequestContext request, 
      Document doc)
   {
      Iterator roles = request.getRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         if (role != null)
         {
            doc.getDocumentElement().appendChild(getSubjectRoleAttributes(
               request, doc, null, 0, role, null));

            doc.getDocumentElement().appendChild(getSubjectRoleAttributes(
               request, doc, "admin%", 0, role, null));

//          enable code with bug fix Rx-04-08-0030
//            doc.getDocumentElement().appendChild(getSubjectRoleAttributes(
//               request, doc, null, PSSubject.SUBJECT_TYPE_GROUP, role, null));

            doc.getDocumentElement().appendChild(getSubjectRoleAttributes(
               request, doc, null, 0, role, "given%"));
         }
      }
   }

   /**
    * Calls the appropriate 
    * <code>IPSRequestContext.getSubjectRoleAttributes(...)</code>
    * method depending on the supplied parameters and returns the result.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    * @param subjectNameFilter the subject name to filter by, may be 
    *    <code>null</code>, assumed not empty.
    * @param type the subject type to filter by, assumed one of 0 or 
    *    <code>PSSubject.SUBJECT_TYPE_xxx</code>.
    * @param role the role name to filter by, assumed not 
    *    <code>null</code> or empty.
    * @param attributeNameFilter the attribute name to filter by, may be
    *    <code>null</code>, assumed not empty. 
    * @return a <code>GetSubjectRoleAttributes</code> element with all 
    *    results, never <code>null</code>.
    */
   private Element getSubjectRoleAttributes(IPSRequestContext request, 
      Document doc, String subjectNameFilter, int type, String role, 
      String attributeNameFilter)
   {
      Element result = doc.createElement("GetSubjectRoleAttributes");
      result.setAttribute("role", role);
      if (subjectNameFilter != null)
         result.setAttribute("subjectNameFilter", subjectNameFilter);
      if (type != 0)
         result.setAttribute("type", "" + type);
      if (attributeNameFilter != null)
         result.setAttribute("attributeNameFilter", attributeNameFilter);
      
      Iterator subjects = null;
      if (subjectNameFilter == null && type == 0 && attributeNameFilter == null)
         subjects = request.getSubjectRoleAttributes(role).iterator();
      else
         subjects = request.getSubjectRoleAttributes(subjectNameFilter, type, 
            role, attributeNameFilter).iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         result.appendChild(subject.toXml(doc));
      }
      
      return result;
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getSubjectRoles(...)</code> method. 
    * The test results will be appendend to the supplied document as 
    * <code>GetSubjectRoles</code> elements.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetSubjectRoles(IPSRequestContext request, Document doc)
   {
      Element result = doc.createElement("GetSubjectRoles");
      Iterator roles = request.getSubjectRoles().iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         result.appendChild(createStringElement(role, doc));
      }

      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * This method tests all signatures of the 
    * <code>IPSRequestContext.getSubjects(...)</code> method. 
    * The test results will be appendend to the supplied document as 
    * <code>GetSubjects</code> elements.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    */
   private void testGetSubjects(IPSRequestContext request, Document doc)
   {
      doc.getDocumentElement().appendChild(getSubjects(request, doc, "admin1"));

      doc.getDocumentElement().appendChild(getSubjects(request, doc, "a%"));

      doc.getDocumentElement().appendChild(getSubjects(request, doc, "%level%"));
   }

   /**
    * Calls the appropriate 
    * <code>IPSRequestContext.getSubjects(...)</code>
    * method depending on the supplied parameters and returns the result.
    * 
    * @param request the request used to perform the tests, assumed not 
    *    <code>null</code>.
    * @param doc the document to which to append the result, assumed not
    *    <code>null</code>.
    * @param subjectNameFilter the subject name to filter by, may be 
    *    <code>null</code>, assumed not empty.
    * @return a <code>GetSubjects</code> element with all 
    *    results, never <code>null</code>.
    */
   private Element getSubjects(IPSRequestContext request, Document doc, 
      String subjectNameFilter)
   {
      Element result = doc.createElement("GetSubjects");
      
      Iterator subjects = request.getSubjects(subjectNameFilter).iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         result.appendChild(subject.toXml(doc));
      }
      
      return result;
   }
   
   /**
    * Create a string element used to document test results.
    * 
    * @param value the value that will be added to the string element, 
    *    may be <code>null</code> or empty.
    * @param doc the document for which to create the string element, assumed
    *    not <code>null</code>.
    * @return the string element, never <code>null</code>.
    */
   private Element createStringElement(String value, Document doc)
   {
      Element elem = doc.createElement("String");
      Text text = doc.createTextNode(value);
      elem.appendChild(text);
      
      return elem;
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * The attribute name used to lookup email addresses from the backend.
    */
   private static final String SYS_EMAIL_ATTRIBUTE_NAME = "sys_email"; 
}
