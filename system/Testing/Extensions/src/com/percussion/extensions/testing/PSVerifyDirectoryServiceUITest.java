/* *****************************************************************************
 *
 * [ PSVerifyDirectoryServiceUITest.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.extensions.testing;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.util.PSProperties;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This exit is used to verify the directory service user interface test
 * results.
 */
public class PSVerifyDirectoryServiceUITest extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * Validate the directory service UI tests. Two HTML parameters are 
    * expected:
    * 
    * <code>tst_method</code> - the test method which should be performed.
    *    Valid methods are <code>validateExisting</code> and 
    *    <code>notExisting</code>.
    * 
    * <code>tst_directoryservicename</code> - the name of the directory service
    *    to be tested. This exit assumes that the names of all directory 
    *    service components (authenticarion, directory, directory set and role 
    *    provider) are the same.
    * 
    * The method <code>validateExisting</code> validates all directory service
    * components supplied though the given name against directory service
    * components assumed to be present with <code>/reference</code> appended
    * to the supplied name.
    * 
    * The method <code>notExisting</code> tests that no directory service 
    * component for the supplied name exists. To make sure that we are not 
    * fooled with spelling errors, the reference objects for the supplied name
    * must exist.
    */
   public Document processResultDocument(Object[] params,
      IPSRequestContext request, Document resultDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         FileInputStream fin = new FileInputStream(PSProperties.getConfig(
            PSServer.ENTRY_NAME, "config.xml", PSServer.getRxConfigDir()));
         
         PSServerConfiguration config = new PSServerConfiguration(
            PSXmlDocumentBuilder.createXmlDocument(fin, false));
         
         String method = request.getParameter("tst_method");
         if (method == null || method.trim().length() == 0)
            throw new PSParameterMismatchException(0, 
               "tst_method parameter cannot be null or empty");
         
         String name = request.getParameter("tst_directoryservicename");
         if (name == null || name.trim().length() == 0)
            throw new PSParameterMismatchException(0, 
               "tst_directoryservicename parameter cannot be null or empty");
         
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = doc.createElement("DirectoryServiceUiTestResults");
         root.setAttribute("method", method.toLowerCase());
         doc.appendChild(root);
         
         String referenceName = name + REFERENCE;
         String refNotFoundMessage = " reference found for name: " + name;
         
         // get all references we will compare with
         PSAuthentication authenticationRef = config.getAuthentication(
            referenceName);
         if (authenticationRef == null)
            throw new PSExtensionProcessingException(0, "No authentication" + 
               refNotFoundMessage);

         PSDirectory directoryRef = config.getDirectory(referenceName);
         if (directoryRef == null)
            throw new PSExtensionProcessingException(0, "No directory" + 
               refNotFoundMessage);

         PSDirectorySet directorySetRef = config.getDirectorySet(referenceName);
         if (directorySetRef == null)
            throw new PSExtensionProcessingException(0, "No direcctory set" + 
               refNotFoundMessage);

         PSRoleProvider roleProviderRef = config.getRoleProvider(referenceName);
         if (roleProviderRef == null)
            throw new PSExtensionProcessingException(0, "No role provider" + 
               refNotFoundMessage);
         
         if (method.equalsIgnoreCase("validateExisting"))
            existsAndIsValid(config, doc, name, authenticationRef, 
               directoryRef, directorySetRef, roleProviderRef);
         else if (method.equalsIgnoreCase("notExisting"))
            existsNot(config, doc, name);
         else
            throw new PSParameterMismatchException(0, 
               "unknown method found in tst_method");
            
         return doc;
      }
      catch (Exception e)
      {
         throw new PSExtensionProcessingException(0, e.getLocalizedMessage());
      }
   }
   
   /**
    * Tests if all directory service components exist for the supplied name and
    * validates them agains the supplied reference components.
    * For each component a result will be added with the value 
    * <code>valid</code> if the component does exist and has the same values
    * as the supplied reference components, <code>invalid</code> otherwise.
    * 
    * @param config the server configuration to test agains, assumed not 
    *    <code>null</code>.
    * @param doc to document to which to append the results, assumed not 
    *    <code>null</code>.
    * @param name the directory service name to do the test for, assumed not
    *    <code>null</code> or empty.
    * @param authenticationRef the authentication reference against which to
    *    validate, assumed not <code>null</code>.
    * @param directoryRef the directory reference against which to
    *    validate, assumed not <code>null</code>.
    * @param directorySetRef the directory set reference against which to
    *    validate, assumed not <code>null</code>.
    * @param roleProviderRef the role provider reference against which to
    *    validate, assumed not <code>null</code>.
    * @throws MalformedURLException for invalid LDAP url's.
    */
   private void existsAndIsValid(PSServerConfiguration config, Document doc, 
      String name, PSAuthentication authenticationRef, PSDirectory directoryRef,
      PSDirectorySet directorySetRef, PSRoleProvider roleProviderRef) 
      throws MalformedURLException
   {
      // get all testees than need to be comapred with our references
      PSAuthentication authentication = config.getAuthentication(name);
      if (authentication != null)
         authentication.setName(authentication.getName() + REFERENCE);

      PSReference ref = null;
      URL url = null;
      URL urlRef = null;
      PSDirectory directory = config.getDirectory(name);
      if (directory != null)
      {
         directory.setName(directory.getName() + REFERENCE);
         ref = directory.getAuthenticationRef();
         ref.setName(ref.getName() + REFERENCE);
         directory.setAuthenticationRef(ref);
         url = new URL(directory.getProviderUrl());
         urlRef = new URL(directoryRef.getProviderUrl());
      }

      PSDirectorySet directorySet = config.getDirectorySet(name);
      if (directorySet != null)
      {
         directorySet.setName(directorySet.getName() + REFERENCE);
         ref = directorySet.getDirectoryRef(name);
         ref.setName(ref.getName() + REFERENCE);
      }

      PSRoleProvider roleProvider = config.getRoleProvider(name);
      if (roleProvider != null)
      {
         roleProvider.setName(roleProvider.getName() + REFERENCE);
         ref = roleProvider.getDirectoryRef();
         ref.setName(ref.getName() + REFERENCE);
      }
      
      // perform the tests and set the results
      boolean authenticationOk = authentication != null && 
         authenticationRef.equals(authentication);
      Element result = createResultElement(doc, authenticationOk, 
         AUTHENTICATION);
      doc.getDocumentElement().appendChild(result);
      
      boolean directoryOk = directory != null;
      if (directoryOk)
      {
         if (!url.getProtocol().equals(urlRef.getProtocol()))
            directoryOk = false;
         else if (!url.getFile().equals(urlRef.getFile()))
            directoryOk = false;
         else
         {
            directory.setProviderUrl(urlRef.toString());
            directoryOk = directoryRef.equals(directory);
         }
      }
      result = createResultElement(doc, directoryOk, DIRECTORY);
      doc.getDocumentElement().appendChild(result);
      
      boolean directorySetOk = directorySet != null && 
         directorySetRef.equals(directorySet);
      result = createResultElement(doc, directorySetOk, DIRECTORY_SET);
      doc.getDocumentElement().appendChild(result);
      
      boolean roleProviderOk = roleProvider != null &&
         roleProviderRef.equals(roleProvider);
      result = createResultElement(doc, roleProviderOk, ROLE_PROVIDER);
      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * Tests if no directory service components exist for the supplied name.
    * For each component a result will be added with the value 
    * <code>valid</code> if the ccomponent does not exist and 
    * <code>invalid</code> if the component does exist.
    * 
    * @param config the server configuration to test agains, assumed not 
    *    <code>null</code>.
    * @param doc to document to which to append the results, assumed not 
    *    <code>null</code>.
    * @param name the directory service name to do the test for, assumed not
    *    <code>null</code> or empty.
    */
   private void existsNot(PSServerConfiguration config, Document doc, 
      String name)
   {
      Element result = createResultElement(doc, 
         config.getAuthentication(name) == null, AUTHENTICATION);
      doc.getDocumentElement().appendChild(result);

      result = createResultElement(doc, 
         config.getDirectory(name) == null, DIRECTORY);
      doc.getDocumentElement().appendChild(result);

      result = createResultElement(doc, 
         config.getDirectorySet(name) == null, DIRECTORY_SET);
      doc.getDocumentElement().appendChild(result);

      result = createResultElement(doc, 
         config.getRoleProvider(name) == null, ROLE_PROVIDER);
      doc.getDocumentElement().appendChild(result);
   }
   
   /**
    * Create a result element for the suppliedd parameters.
    * 
    * @param doc the document for which to create the result element,
    *    assumed not <code>null</code>.
    * @param valid <code>true</code> to create a valid results element,
    *    <code>false</code> to create an invalid result element.
    * @param test the name of the test for which to create the result element,
    *    assumed not <code>null</code> or empty.
    * @return the result element, never <code>null</code>.
    */
   private Element createResultElement(Document doc, boolean valid, String test)
   {
      Element result = doc.createElement(test);
      if (valid)
         result.setAttribute("result", "valid");
      else
         result.setAttribute("result", "invalid");
      
      return result;
   }

   /* (non-Javadoc)
    * @see IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }
   
   /**
    * The string appended to the directory service name to lookup the reference
    * objects. 
    */
   private static final String REFERENCE = "/reference";
   
   /**
    * The element name to hold the test result for authentications.
    */
   private static final String AUTHENTICATION = "Authentication";
   
   /**
    * The element name to hold the test result for directories.
    */
   private static final String DIRECTORY = "Directory";
   
   /**
    * The element name to hold the test result for directory sets.
    */
   private static final String DIRECTORY_SET = "DirectorySet";
   
   /**
    * The element name to hold the test result for role providers.
    */
   private static final String ROLE_PROVIDER = "RoleProvider";
}
