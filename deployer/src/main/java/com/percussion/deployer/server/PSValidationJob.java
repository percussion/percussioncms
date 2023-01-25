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
 
package com.percussion.deployer.server;

import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSDeployException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Job to validate all packages in an import descriptor.  Results are saved on
 * the server and may be retrieved using the archive ref.
 */
public class PSValidationJob extends PSDeployJob
{
   /**
    * Restores the import descriptor from the supplied document, and validates
    * that the user is authorized to perform this job.  Saves the security token
    * from the request to use for subsequent operations during the run method.
    * <br>
    * See Base class for more info.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void init(int id, Document descriptor, PSRequest req, 
      Properties initParams) 
      throws PSAuthenticationFailedException, PSAuthorizationException, 
         PSJobException
   {
      if (descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null");
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      super.init(id, req, initParams);      
      
      try 
      {
         m_descriptor = new PSImportDescriptor(descriptor.getDocumentElement());
         List pkgList = new ArrayList();
         Iterator<PSImportPackage> importPkgs = m_descriptor.getImportPackageList().iterator();
         while (importPkgs.hasNext())
         {
            PSImportPackage importPkg = importPkgs.next();
            pkgList.add(importPkg.getPackage());
         }
         initDepCount(pkgList.iterator(), false);
      }
      catch (PSUnknownNodeTypeException | PSDeployException e)
      {
         throw new PSJobException(IPSJobErrors.INVALID_JOB_DESCRIPTOR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Runs this validation job.  Validates all packages and saves the results.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void doRun() 
   {
      try 
      {
         validate(m_descriptor, this, getSecurityToken());
         
         if (!isCancelled())
         {
            // write out the desciptor with the results using the archive ref
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(doc, m_descriptor.toXml(doc));
               
            File resultsFile = new File(
               PSDeploymentHandler.getValidationDir(), m_descriptor.getArchiveInfo().getArchiveRef() 
               + ".xml");
            resultsFile.getParentFile().mkdirs();
            resultsFile.deleteOnExit();

            try(FileOutputStream out = new FileOutputStream(resultsFile)){
               PSXmlDocumentBuilder.write(doc, out);
            }

            setStatus(100);  
            setStatusMessage(PSDeploymentManager.getBundle().getString("completed"));       
         }
      }
      catch (Exception ex) 
      {
         // getLocalizedMessage often returns empty string
         setStatusMessage("error: " + ex.toString());
         setStatus(-1);
         LogManager.getLogger(getClass()).error("Error validating Deployer "
               + "package", ex);
      }
      finally
      {
         setCompleted();
      }
      
   }
   
   /**
    * Standalone validation method used by server-side services
    * 
    * @param descriptor The import descriptor to validate, not <code>null</code> 
    * @param jobHandle Job handle to record status, not <code>null</code>
    * @param tok Security token repesenting current user session, not <code>null</code>.
    * 
    * @throws PSDeployException If there are any errors.
    */
   public void validate(PSImportDescriptor descriptor, IPSJobHandle jobHandle, PSSecurityToken tok) throws PSDeployException, PSNotFoundException {
       Validate.notNull(descriptor);
       Validate.notNull(jobHandle);
       Validate.notNull(tok);
       
       PSDbmsHelper dbmsHelper = null;
       PSDependencyManager dm = null;
       
       try 
       {
          ResourceBundle bundle = PSDeploymentManager.getBundle();
          setStatusMessage(bundle.getString("init"));
       
          // enable cache for non-system schema
          dbmsHelper = PSDbmsHelper.getInstance();
          dbmsHelper.enableSchemaCache();
          
          // get the archive ref
          PSArchiveInfo info = descriptor.getArchiveInfo();
          
          // walk the packages and validate
          PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
          dm = (PSDependencyManager) dh.getDependencyManager();
          
          // enable dependency cache
          dm.setIsDependencyCacheEnabled(true);
          
          // generate the id map
          PSDbmsInfo sourceDb = info.getRepositoryInfo();
          List<PSImportPackage> importList = descriptor.getImportPackageList();
                   
          PSTransformsHandler th = new PSTransformsHandler(tok,
                sourceDb.getDbmsIdentifier(), importList);

          setStatusMessage(bundle.getString("generatingIdMap"));

          // get the transformed id map
          PSIdMap idMap = th.getIdMap();

          // save the transformed id map
          dh.getIdMapMgr().saveIdMap(idMap);
          
          PSValidationCtx valCtx = new PSValidationCtx(jobHandle, descriptor,
             idMap);
          valCtx.setValidateAncestors(
                  descriptor.isAncestorValidationEnabled());

         Iterator pkgs = descriptor.getImportPackageList().iterator();
          while (pkgs.hasNext() && !isCancelled())
          {
             PSImportPackage pkg = (PSImportPackage)pkgs.next();
             PSDeployableElement de = pkg.getPackage();
             valCtx.addPackage(pkg);
             String msg = MessageFormat.format(bundle.getString("processing"), 
                new Object[] {de.getDisplayIdentifier()});
             setStatusMessage(msg);
             PSDependencyValidator dv = new PSDependencyValidator(
                   tok, de, valCtx, descriptor.getName());
             pkg.setValidationResults(dv.validate());            
          }

         if (!isCancelled())
         {
            // write out the desciptor with the results using the archive ref
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(doc, descriptor.toXml(doc));

            File resultsFile = new File(
               PSDeploymentHandler.VALIDATION_RESULTS_DIR, info.getArchiveRef()
               + ".xml");
            resultsFile.getParentFile().mkdirs();
            resultsFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(resultsFile))
            {
               PSXmlDocumentBuilder.write(doc, out);
            }

            setStatus(100);
            setStatusMessage(bundle.getString("completed"));
         }
      }
      catch (Exception ex)
      {
         // getLocalizedMessage often returns empty string
         setStatusMessage("error: " + ex.toString());
         setStatus(-1);
         LogManager.getLogger(getClass()).error("Error validating archive",
            ex);
       }
       finally
       {
          // disable non-system schema cache before releasing job lock
          if (dbmsHelper != null)
             dbmsHelper.disableSchemaCache();

          // disable dependency cache before releasing job lock
          if (dm != null)
             dm.setIsDependencyCacheEnabled(false);

         setCompleted();
       }
   }
   
   // see base class
   protected String getJobType()
   {
      return "Validate Import Descriptor";
   }
    
   /** 
    * The import descriptor supplied to the <code>init()</code> method, never
    * <code>null</code> or modified after that.
    */
   private PSImportDescriptor m_descriptor;
}
