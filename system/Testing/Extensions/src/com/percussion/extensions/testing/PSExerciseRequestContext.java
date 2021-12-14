/*[ PSExerciseRequestContext.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSSubject;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Rhythmyx extension that tests the new methods in version 4.0 of
 * the IPSRequestContext interface.
 *
 * <ul>
 * <li>getSubjects
 * <li>getSubjectRoles
 * <li>getRoleSubjects
 * <li>getRoleAttributes
 * <li>getSubjectGlobalAttributes
 * <li>getSubjectRoleAttributes
 * </ul>
 *
 * @since 4.0
 */
public class PSExerciseRequestContext extends PSDefaultExtension
      implements IPSRequestPreProcessor, IPSResultDocumentProcessor
{
   // see IPSResultDocumentProcessor
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   // see IPSResultDocumentProcessor
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resultDoc)
         throws PSParameterMismatchException,
         PSExtensionProcessingException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);
      String roleName = null;
      String subjectName = null;

      // try to get our request parameters
      HashMap paramMap = request.getParameters();
      if (null != paramMap)
      {
         Object o = paramMap.get("roleName");
         if (null != o) roleName = o.toString();
         o = paramMap.get("subjectName");
         if (null != o) subjectName = o.toString();
      }

      if (null != roleName)
      {
         buildRoleTree(request, roleName, root, doc);
      }
      else if (null != subjectName)
      {
         buildSubjectTree(request, subjectName, root, doc);
      }
      else
      {
         // if no rolename is provided, return a list of roles and users
         List roleList = request.getRoles();
         // remove those roles that weren't defined for this test
         for (Iterator iter = roleList.iterator(); iter.hasNext();)
         {
            roleName = (String) iter.next();
            if (!roleName.startsWith("QA"))
               iter.remove();
         }
         
         root.appendChild(iteratorToElement(roleList.iterator(), doc,
               "Roles", "RoleName"));
         List subjectList = request.getSubjects("QA%");
         root.appendChild(iteratorToElement(subjectList.iterator(), doc,
               "Users", "Subject"));

         // and info about globalattributes
         Element groupAttrRoot = doc.createElement("GlobalAttributesNoEmpty");
         subjectList = request.getSubjectGlobalAttributes(null, 0, null, null, 
            false);
         Iterator subjectIter = subjectList.iterator();
         while (subjectIter.hasNext())
         {
            Element subjectRoot = doc.createElement("Subject");
            PSSubject s = (PSSubject) subjectIter.next();
            PSXmlDocumentBuilder.addElement(doc, subjectRoot, "Name", s.toString());
            subjectRoot.appendChild(iteratorToElement(s.getAttributes().iterator(),
                  doc, "SubjectGlobalAttributes", "Attribute"));
            groupAttrRoot.appendChild(subjectRoot);
         }
         root.appendChild(groupAttrRoot);
         
         groupAttrRoot = doc.createElement("GlobalAttributesYesEmpty");
         subjectList = request.getSubjectGlobalAttributes(null, 0, null, null, 
            true);
         subjectIter = subjectList.iterator();
         while (subjectIter.hasNext())
         {
            Element subjectRoot = doc.createElement("Subject");
            PSSubject s = (PSSubject) subjectIter.next();
            PSXmlDocumentBuilder.addElement(doc, subjectRoot, "Name", s.toString());
            subjectRoot.appendChild(iteratorToElement(s.getAttributes().iterator(),
                  doc, "SubjectGlobalAttributes", "Attribute"));
            groupAttrRoot.appendChild(subjectRoot);
         }
         root.appendChild(groupAttrRoot);

         // and info about the requestor
         Element requestorRoot = doc.createElement("Requestor");
         roleList = request.getSubjectRoles();
         requestorRoot.appendChild(iteratorToElement(roleList.iterator(), doc,
               "Roles", "RoleName"));

         List singleSubjectList = request.getSubjectGlobalAttributes();
         Iterator iter = singleSubjectList.iterator();
         if (iter.hasNext())
         {
            PSSubject s = (PSSubject) iter.next();
            requestorRoot.appendChild(iteratorToElement(
                  s.getAttributes().iterator(), doc,
                  "GlobalAttributes", "Attribute"));
         }
         root.appendChild(requestorRoot);
      }

      return doc;
   }


   /**
    * Appends information about the specified subject to the provided XML 
    * document.
    */
   private void buildSubjectTree(IPSRequestContext request, String subjectName,
                                 Element root, Document doc)
   {
      List roleList;
      List subjectList;

      subjectList = request.getSubjects(subjectName);
      Iterator subjectIter = subjectList.iterator();
      Element subjectGroupRoot = doc.createElement("Subjects");
      while (subjectIter.hasNext())
      {
         Element subjectRoot = doc.createElement("Subject");
         PSSubject subject = (PSSubject) subjectIter.next();
         PSXmlDocumentBuilder.addElement(doc, subjectRoot, "Name", subject.toString());

         roleList = request.getSubjectRoles(subject.getName());
         subjectRoot.appendChild(iteratorToElement(roleList.iterator(), doc,
               "RolesByName", "Role"));

         roleList = request.getSubjectRoles(subject);
         subjectRoot.appendChild(iteratorToElement(roleList.iterator(), doc,
               "RolesBySubject", "Role"));

         List singleSubjectList = request.getSubjectGlobalAttributes(subject);
         Iterator iter = singleSubjectList.iterator();
         if (iter.hasNext())
         {
            PSSubject s = (PSSubject) iter.next();
            subjectRoot.appendChild(iteratorToElement(
                  s.getAttributes().iterator(), doc,
                  "SubjectGlobalAttributes", "Attribute"));
         }

         subjectGroupRoot.appendChild(subjectRoot);
      }
      root.appendChild(subjectGroupRoot);
   }


   /**
    * Appends information about the specified role to the provided XML document.
    */
   private void buildRoleTree(IPSRequestContext request, String roleName,
                              Element root, Document doc)
   {
      List attrList;
      List subjectList;
      attrList = request.getRoleAttributes(roleName);
      root.appendChild(iteratorToElement(attrList.iterator(), doc,
            "RoleAttributes", "Attribute"));

      subjectList = request.getRoleSubjects(roleName);
      Element subjectGroupRoot = doc.createElement("Subjects");
      Iterator subjectIter = subjectList.iterator();
      while (subjectIter.hasNext())
      {
         Element subjectRoot = doc.createElement("Subject");
         PSSubject subject = (PSSubject) subjectIter.next();
         PSXmlDocumentBuilder.addElement(doc, subjectRoot, "Name", subject.toString());

         List singleSubjectList = request.getSubjectRoleAttributes(subject, roleName);
         Iterator iter = singleSubjectList.iterator();
         if (iter.hasNext())
         {
            PSSubject s = (PSSubject) iter.next();
            subjectRoot.appendChild(iteratorToElement(
                  s.getAttributes().iterator(), doc,
                  "SubjectRoleAttributes", "Attribute"));
         }
         subjectGroupRoot.appendChild(subjectRoot);
      }
      root.appendChild(subjectGroupRoot);

      subjectList = request.getRoleMembers(roleName, 0, 0);
      root.appendChild(iteratorToElement(subjectList.iterator(), doc,
            "Members", "Member"));

      subjectList = request.getRoleSubjects(roleName,
            PSSubject.SUBJECT_TYPE_USER, null);
      root.appendChild(iteratorToElement(subjectList.iterator(), doc,
            "Users", "Subject"));

      subjectList = request.getRoleSubjects(roleName,
            PSSubject.SUBJECT_TYPE_GROUP, null);
      root.appendChild(iteratorToElement(subjectList.iterator(), doc,
            "Groups", "Subject"));

      // get all the subjects that contain the letter "a" for this role
      subjectList = request.getRoleSubjects(roleName, 0, "%e%");
      root.appendChild(iteratorToElement(subjectList.iterator(), doc,
            "E-Subjects", "Subject"));

      // role attributes for 'QA' provider instance from subjects containing 'h'
      Element groupRoot = doc.createElement("H-RoleAttributes");
      subjectList = request.getSubjectRoleAttributes("%h%", 0, roleName, "");
      subjectIter = subjectList.iterator();
      while (subjectIter.hasNext())
      {
         PSSubject s = (PSSubject) subjectIter.next();
         groupRoot.appendChild(iteratorToElement(s.getAttributes().iterator(),
               doc, "SubjectRoleAttributes", "Attribute"));
      }
      root.appendChild(groupRoot);

      // role attributes containing 'b' for BackEnd from subjects containing 'a'
      groupRoot = doc.createElement("AB128-RoleAttributes");
      subjectList = request.getSubjectRoleAttributes("%A%", 0, roleName, "%b%");
      subjectIter = subjectList.iterator();
      while (subjectIter.hasNext())
      {
         PSSubject s = (PSSubject) subjectIter.next();
         groupRoot.appendChild(iteratorToElement(s.getAttributes().iterator(),
               doc, "SubjectRoleAttributes", "Attribute"));
      }
      root.appendChild(groupRoot);
   }


   /**
    * Handles the pre-exit request by testing several getter methods of the
    * request to see if they return the expected values.
    *
    * @param params An array of objects representing the parameters to this
    *    method.  Expects this array to be empty.
    * @param request The request context for this request.
    *
    * @throws PSExtensionProcessingException when a run time error is detected.
    * @throws PSParameterMismatchException if any parameters have been passed
    *    in the params array.
    *
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
         throws PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException
   {
      List roleList = null;
      List subjectList = null;
      List attrList = null;

      // we do not expect to receive any parameters
      if (null != params && params.length != ms_EXPECTED_PARAMS)
      {
         throw new PSParameterMismatchException(ms_EXPECTED_PARAMS,
               params.length);
      }

      if (null == request)
      {
         throw new PSExtensionProcessingException(0, "request cannot be null");
      }

      /* TEST ROLES */

      // get all the system roles
      roleList = request.getRoles();
      printList(roleList, "All roles");

      subjectList = request.getSubjectRoleAttributes("%i%", 0, "fluffy", "");
      compare("getSubjectRoleAttributes for non-existant role", "",
            iteratorToString(subjectList.iterator()), true);

      // step through each role and list its subjects
      Iterator roleIter = roleList.iterator();
      while (roleIter.hasNext())
      {
         String roleName = (String) roleIter.next();
         System.out.println("========================================");
         System.out.println("== ROLE == " + roleName);

         attrList = request.getRoleAttributes(roleName);
         printList(attrList, "All my role attributes");

         subjectList = request.getRoleSubjects(roleName);
         printList(subjectList, "All my subjects");

         subjectList = request.getRoleMembers(roleName, 0, 0);
         printList(subjectList, "All my *members*");

         // list each subject's role attributes
         subjectList = request.getRoleSubjects(roleName);
         Iterator subjectIter = subjectList.iterator();
         while (subjectIter.hasNext())
         {
            PSSubject subject = (PSSubject) subjectIter.next();
            List singleSubjectList =
                  request.getSubjectRoleAttributes(subject, roleName);
            printSubjectAttributes(singleSubjectList, "Role specific attributes");
         }

         // get just the users for this role
         subjectList = request.getRoleSubjects(roleName,
               PSSubject.SUBJECT_TYPE_USER, null);
         printList(subjectList, "My user subjects");

         // get just the groups for this role
         subjectList = request.getRoleSubjects(roleName,
               PSSubject.SUBJECT_TYPE_GROUP, null);
         printList(subjectList, "My group subjects");

         // get all the subjects that contain the letter "a" for this role
         subjectList = request.getRoleSubjects(roleName, 0, "%a%");
         printList(subjectList, "My subjects containing with 'a'");

         subjectList = request.getSubjectRoleAttributes("%i%", 0, roleName, "");
         printSubjectAttributes(subjectList,
               "Role attributes for 'rxmaster' subjects containing 'i'");
         subjectList = request.getSubjectRoleAttributes("%a%", 0, roleName, 
            "%b%");
         printSubjectAttributes(subjectList,
               "Role attr. containing 'b' for BackEnd subjects containing 'a'");

         System.out.println("========================================");
      }

      testSubjectMethods(request);

      testRequestorMethods(request);

      testEmailMethods(request);
   }
   
   private void testEmailMethods(IPSRequestContext request)
   {
      List roleList = request.getSubjectRoles();
      System.out.println("========================================");
      printList(roleList, "Requestor Roles");
      String emailAttributeName = "mail";
      String community = request.getParameter(IPSHtmlParameters.SYS_COMMUNITY);
      System.out.println("Email Attribute = " + emailAttributeName + 
         ", Community= " + community);

      Iterator roles = roleList.iterator();
      while (roles.hasNext())
      {
         String role = (String) roles.next();
         
         Set emails = request.getRoleEmailAddresses(role, emailAttributeName, 
            community);
         printList(new ArrayList(emails), "Role emails: ");
      }

      List subjectList = request.getSubjects("Admin2");
      System.out.println("========================================");
      printList(subjectList, "Admin1 Subjects");
      
      Iterator subjects = subjectList.iterator();
      while (subjects.hasNext())
      {
         PSSubject subject = (PSSubject) subjects.next();
         Set emails = request.getSubjectEmailAddresses(subject.getName(), 
            emailAttributeName, community);
         printList(new ArrayList(emails), "Subject emails: ");
      }
   }

   /**
    * Tests the methods of IPSRequestContext that access role and attribute
    * information about the subject that made the current request.  This
    * information is only available if the user has authenticated (is not
    * anonymous).  The information is printed to the console.
    *
    * @param request the current request object; never <code>null</code>
    */
   private void testRequestorMethods(IPSRequestContext request)
   {
      List roleList;
      List attrList;

      // get the roles of the subject that made the request
      roleList = request.getSubjectRoles();
      printList(roleList, "Roles for requestor");

      // get the attributes of the subject that made the request
      // (returns a subject object if it has attributes -- or returns nothing)?
      attrList = request.getSubjectGlobalAttributes();
      printSubjectAttributes(attrList, "Global attributes for requestor");
   }


   /**
    * Tests the methods of IPSRequestContext that access subject information
    * (including subjects' roles and attributes).  The information is printed
    *  to the console.
    *
    * @param request the current request object; never <code>null</code>
    */
   private void testSubjectMethods(IPSRequestContext request)
   {
      List subjectList;
      List roleList;

      // operations on subjects in any role
      subjectList = request.getSubjects(null);
      printList(subjectList, "All subjects");

      subjectList = request.getSubjects("A%");
      printList(subjectList, "Subjects starting with 'A'");
      subjectList = request.getSubjects("a%");
      printList(subjectList, "Subjects starting with 'a'");
      subjectList = request.getSubjects("%i%");
      printList(subjectList, "Subjects containing 'i'");
      subjectList = request.getSubjects("X%");
      printList(subjectList, "Subjects starting with 'X'");
      subjectList = request.getSubjects("Admin1");
      printList(subjectList, "Admin1 Subjects");

      subjectList = request.getSubjectGlobalAttributes("%i%", 0, "", "", false);
      printSubjectAttributes(subjectList,
            "Global attributes for 'rxmaster' subjects containing 'i'");
      subjectList = request.getSubjectGlobalAttributes("%a%", 0, "", "%b%", 
         false);
      printSubjectAttributes(subjectList,
            "Global attr. containing 'b' for BackEnd subjects containing 'a'");

      // step through each subject and list its attributes
      subjectList = request.getSubjects(null);
      Iterator subjectIter = subjectList.iterator();
      while (subjectIter.hasNext())
      {
         PSSubject subject = (PSSubject) subjectIter.next();
         System.out.println("========================================");
         System.out.println("== SUBJECT == " + subject.toString());

         roleList = request.getSubjectRoles(subject.getName());
         printList(roleList, "All my roles");

         roleList = request.getSubjectRoles(subject);
         printList(roleList, "My Provider-specific roles");

         List singleSubjectList = request.getSubjectGlobalAttributes(subject);
         printSubjectAttributes(singleSubjectList, "My global attributes");

         System.out.println("========================================");
      }
      
      PSSubject originalSubject = request.getOriginalSubject();
      subjectList = new ArrayList();
      subjectList.add(originalSubject);
      printList(subjectList, "Original subject");
   }


   /**
    * Determines if a test was successful or not by comparing the string
    * output of the test from the expected result.  If the actual result
    * does not match the expected, a message is printed to the console.
    *
    * @param testName included with the failure message to indicate which
    *    test failed; must not be <code>null</code>; can be empty
    * @param expectedResult contains the string that will occur when the test
    *    is successful; must not be <code>null</code>; can be empty
    * @param actualResult contains the string that the test actually generated;
    *    will be compared (case-sensitive) to expectedResult; must not be
    *    <code>null</code>; can be empty
    * @param printIfSuccess determines if this method will generate output
    *    when the test is successful
    * @return an int, 0 if the test failed or 1 if the test succeeded.  This
    *    result can be accumulated into a number-of-successful-tests counter.
    */
   private static int compare(String testName, String expectedResult,
                              String actualResult, boolean printIfSuccess)
   {
      int success = 0;
      if (actualResult.equals(expectedResult))
      {
         success = 1;
         if (printIfSuccess)
            System.out.println("SUCCESS: " + testName);
      }
      else
      {
         success = 0;
         System.out.println("FAIL: " + testName);
         System.out.println("\tExpected: " + expectedResult);
         System.out.println("\tActual  : " + expectedResult);
      }
      return success;
   }


   /**
    * Prints the contents of a List to System.out as a comma-
    * delimited string, preceeded by a string message.
    *
    * @param theList Contains the list of objects to print.  Can be
    *    <code>null</code>
    * @param theMessage A message to be printed before the list.  Should not
    *    be <code>null</code>, can be empty.
    */
   private static void printList(List theList, String theMessage)
   {
      System.out.print(theMessage + ": ");
      if (null != theList)
         System.out.println(iteratorToString(theList.iterator()));
   }


   /**
    * Prints the attributes of a List of PSSubjects to System.out as a comma-
    * delimited string, preceeded by a string message.
    *
    * @param subjectList the subjects whose attributes will be printed.  If
    *    <code>null</code>, no attributes will be printed.
    * @param theMessage A message to be printed before the list.  Should not
    *    be <code>null</code>, can be empty.
    */
   private static void printSubjectAttributes(List subjectList,
                                              String theMessage)
   {
      System.out.print(theMessage + ": ");
      if (null != subjectList)
      {
         StringBuilder buf = new StringBuilder();

         // print out all the attributes of each subject in the list
         Iterator subjectIter = subjectList.iterator();
         while (subjectIter.hasNext())
         {
            PSSubject subject = (PSSubject) subjectIter.next();
            buf.append(subject.toString());
            buf.append("[");
            buf.append(iteratorToString(subject.getAttributes().iterator()));
            buf.append("]");
            if (subjectIter.hasNext())
            {
               buf.append(", ");
            }
         }
         System.out.println(buf.toString());
      }
   }


   /**
    * Turns a list into a comma-delimited string.
    *
    * @param iter collection of objects to add to the string;
    *    Cannot be <code>null</code>
    * @return A comma-delimited string
    */
   private static String iteratorToString(Iterator iter)
   {
      StringBuilder buf = new StringBuilder();
      while (iter.hasNext())
      {
         Object o = iter.next();
         buf.append(o.toString());
         if (iter.hasNext())
         {
            buf.append(", ");
         }
      }
      return buf.toString();
   }


   /**
    * Builds an XML tree from the string values of objects in the supplied
    * Iterator.
    *
    * <rootName><elementName>stringvalue</elementName> ... </rootName>
    *
    * @param iter objects to populate the element data; never <code>null</code>
    * @param doc the XML document this tree will be a part of; never <code>null</code>
    * @param rootName name of the element returned (parent);
    *    never <code>null</code> or empty
    * @param elementName name of the node that holds the string value (child);
    *    never <code>null</code> or empty
    * @return the rootName element; never <code>null</code>, but may be
    *    an empty node
    */
   private static Element iteratorToElement(Iterator iter,
                                            Document doc,
                                            String rootName,
                                            String elementName)
   {
      Element root = doc.createElement(rootName);

      while (iter.hasNext())
      {
         Object o = iter.next();
         PSXmlDocumentBuilder.addElement(doc, root, elementName, o.toString());
      }
      return root;
   }


   /**
    * Indicates how many parameters the pre-exit expects to receive
    */
   private static final int ms_EXPECTED_PARAMS = 0;

   /**
    * Indicates the root element name in the post-exit result document
    */
   private static final String ms_NodeType = "PSExerciseRequestContext";
}
