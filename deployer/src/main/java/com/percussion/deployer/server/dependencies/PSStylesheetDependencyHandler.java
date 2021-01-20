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

package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.util.PSIteratorUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a stylesheet
 */
public class PSStylesheetDependencyHandler 
   extends PSSupportFileDependencyHandler
{
   /**
    * Construct a dependency handler.
    * 
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSStylesheetDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>ContextVariable</li>
    * <li>Stylesheet</li>
    * <li>SupportFile</li>
    * </ol>
    * 
    * @return An iterator over zero or more types as <code>String</code> 
    * objects, never <code>null</code>, does not contain <code>null</code> or 
    * empty entries.
    */
   public Iterator getChildTypes()
   {
      Set childTypes = new HashSet(ms_childTypes);
      Iterator types = super.getChildTypes();
      while (types.hasNext())
         childTypes.add(types.next().toString());
         
      return childTypes.iterator();
   }

   
   // see base class
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      List deps = new ArrayList();

      return PSIteratorUtils.emptyIterator();
   }
   
   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }
   
   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id) != null;
   }
   
   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSDependency dep = null;
      
      if (isStylesheet(id))
      {
         dep = super.getDependency(tok, id);
      }
      
      return dep;
   }
   
   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep) 
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      // use set to ensure we don't add dupes
      Set childDeps = new HashSet();
      
      // get file deps and ensure it exists
      PSDependency fileDep = getDependencyHandler(
         super.DEPENDENCY_TYPE).getDependency(tok, dep.getDependencyId());
      if (fileDep != null)
      {
         Iterator fileDeps = super.getChildDependencies(tok, fileDep);
         while (fileDeps.hasNext())
            childDeps.add(fileDeps.next());
            
         // load stylesheet and check for imports and includes
         Document doc = getXmlDocumentFromFile(new File(dep.getDependencyId()));
         Iterator sheetDeps = getStylesheetDependencies(tok, doc);
         while (sheetDeps.hasNext())
            childDeps.add(sheetDeps.next());
      }
      
      return childDeps.iterator();      
   }
   
   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      return getDependencyFiles(tok, dep.getDependencyId());
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      installFileDependencyFiles(tok, archive, dep, ctx);
   }

   /**
    * Determine if the supplied file id should be treated as a stylesheet
    * 
    * @param id The id, may not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if it is a stylesheet, false otherwise.
    * 
    * @throws IllegalArgumentException if <code>id</code> is invalid.
    */
   static boolean isStylesheet(String id)
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");  
      
      return id.endsWith(".xsl");
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Stylesheet";
   
   /**
    * List of child types supported by this handler, never <code>null</code> or
    * empty.
    */
   private static List ms_childTypes = new ArrayList();
   
   static
   {
      ms_childTypes.add(DEPENDENCY_TYPE);
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
   }
   
}
