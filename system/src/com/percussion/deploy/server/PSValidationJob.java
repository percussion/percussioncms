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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
 
package com.percussion.deploy.server;

import com.percussion.deploy.client.PSDeploymentManager;
import com.percussion.deploy.objectstore.PSArchiveInfo;
import com.percussion.deploy.objectstore.PSDbmsInfo;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSImportDescriptor;
import com.percussion.deploy.objectstore.PSImportPackage;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

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
         Iterator importPkgs = m_descriptor.getImportPackageList().iterator();
         while (importPkgs.hasNext())
         {
            PSImportPackage importPkg = (PSImportPackage)importPkgs.next();
            pkgList.add(importPkg.getPackage());
         }
         initDepCount(pkgList.iterator(), false);
      }
      catch (PSUnknownNodeTypeException e) 
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
         PSArchiveInfo info = m_descriptor.getArchiveInfo();
         
         // walk the packages and validate
         PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
         dm = dh.getDependencyManager();
         
         // enable dependency cache
         dm.setIsDependencyCacheEnabled(true);
         
         PSDbmsInfo sourceDb = info.getRepositoryInfo();
         PSIdMap idMap = null;
         if (!sourceDb.isSameDb(dbmsHelper.getServerRepositoryInfo()))
            idMap = dh.getIdMapMgr().getIdmap(sourceDb.getDbmsIdentifier());
         PSValidationCtx valCtx = new PSValidationCtx(this, m_descriptor,
            idMap);
         valCtx.setValidateAncestors(
            m_descriptor.isAncestorValidationEnabled());
         Iterator pkgs = m_descriptor.getImportPackageList().iterator();
         while (pkgs.hasNext() && !isCancelled())
         {
            PSImportPackage pkg = (PSImportPackage)pkgs.next();
            PSDeployableElement de = pkg.getPackage();
            valCtx.addPackage(pkg);
            String msg = MessageFormat.format(bundle.getString("processing"), 
               new Object[] {de.getDisplayIdentifier()});
            setStatusMessage(msg);
            pkg.setValidationResults(dm.validate(getSecurityToken(), de, 
            valCtx));            
         }

         if (!isCancelled())
         {
            // write out the desciptor with the results using the archive ref
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(doc, m_descriptor.toXml(doc));
               
            File resultsFile = new File(
               PSDeploymentHandler.VALIDATION_RESULTS_DIR, info.getArchiveRef() 
               + ".xml");
            resultsFile.getParentFile().mkdirs();
            resultsFile.deleteOnExit();
            
            FileOutputStream out = null;
            try 
            {
               out = new FileOutputStream(resultsFile);
               PSXmlDocumentBuilder.write(doc, out);
            }
            finally 
            {
               if (out != null)
                  try {out.close();} catch (IOException e){}
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
         LogManager.getLogger(getClass()).error("Error validating MSM archive",
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
