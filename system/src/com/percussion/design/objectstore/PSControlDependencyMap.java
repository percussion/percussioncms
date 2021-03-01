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
package com.percussion.design.objectstore;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Manages input data extensions that are derived from control dependencies.
 * Once the map is constructed, any input data extensions referenced by the map
 * as control dependencies may be retrieved by calling
 * {@link #getInputDataExtensions()}. To get the control dependencies for an
 * existing mapping, call {@link #getControlDependencies(PSDisplayMapping, 
 * PSControlMeta)}.
 * <p>
 * To add a new control, first call
 * {@link #getControlDependencies(PSDisplayMapping, PSControlMeta)} with the new 
 * mapping and then for any dependency returned with an id of "0", assign a new 
 * id. Once the dependency has been modified (if required), then call
 * {@link #setControlDependencies(PSDisplayMapping, List)} for the new mapping.
 * <p>
 * To handle removals, first call
 * {@link #getControlDependencies(PSDisplayMapping, PSControlMeta)} for each
 * existing mapping, call it again for any new mappings, and when ready to save,
 * call {@link #clearControlDependencies()} followed by
 * {@link #setControlDependencies(PSDisplayMapping, List)} for each existing
 * mapping that was not removed as well for for any new mappings.
 */
public class PSControlDependencyMap
{
   /**
    * Construct a new, empty dependency map
    */
   public PSControlDependencyMap()
   {
   
   }
   
   /**
    * Use the supplied user properties and extension calls to create the control 
    * dependency map.
    * 
    * @param userProps The properties, may not be <code>null</code>.
    * @param inputDataExtensions The current input data extensions, from which
    * control dependency extensions will be removed, if <code>null</code> or 
    * empty the user properties will be ignored.
    */
   @SuppressWarnings("unchecked")
   public PSControlDependencyMap(Map<String, String> userProps, 
      PSExtensionCallSet inputDataExtensions)
   {  
      if (userProps == null)
         throw new IllegalArgumentException("userProps may not be null");
      
      if (inputDataExtensions == null || inputDataExtensions.isEmpty())
         return;
      
      // first pass for controls
      String ctrlRegEx = CONTROL + ".*" + DEP_IDS;
      for (Map.Entry<String, String> entry : userProps.entrySet())
      {
         String name = entry.getKey();
         if (!name.matches(ctrlRegEx))
            continue;
         int pos = name.indexOf(CONTROL, 0) + CONTROL.length();
         int sepPos = name.indexOf(DEP_IDS);
         if (pos == -1 || sepPos == -1 || pos >= sepPos)
            continue;
         String strCtrlId = name.substring(pos, sepPos);
         String[] strDepIds = entry.getValue().split(",");
         if (strDepIds.length == 0)
            continue;
         try
         {
            int ctrlId = Integer.parseInt(strCtrlId);
            List<Integer> depIds = new ArrayList<>();
            for (int i = 0; i < strDepIds.length; i++)
            {
               String strDepId = strDepIds[i];
               int depId = Integer.parseInt(strDepId);
               String depName = userProps.get(CONTROL + strCtrlId + DEP + 
                  strDepId);
               if (StringUtils.isBlank(depName))
                  continue;
               PSExtensionCall ext = findExtension(depId, depName, 
                  inputDataExtensions, true);
               if (ext == null)
               {
                  // may have already found it
                  ext = findExtension(depId, depName, 
                     m_ceInputDataExits.values(), false);
                  if (ext == null)
                     continue;
               }
               
               m_ceInputDataExits.put(ext.getId(), ext);
               depIds.add(ext.getId());
            }
            
            if (!depIds.isEmpty())
               m_controlDependencies.put(ctrlId, depIds);
         }
         catch (NumberFormatException e)
         {
            // continue
         }
      }
   }

   /** 
    * Set the dependencies for the specified control. Child mappings are not
    * handled automatically.
    * 
    * @param mapping Specifies the control for which dependencies are being set,
    * may not be <code>null</code>, and must specify a non-<code>null</code>
    * control in its ui set.
    * @param dependencies List of dependencies to set for the specified control.
    * May not be <code>null</code> or empty. New dependencies must have unique
    * ids assigned with {@link PSDependency#setId(int)} 
    * (see {@link #getControlDependencies(PSDisplayMapping, PSControlMeta)}).
    */
   @SuppressWarnings("unchecked")
   public void setControlDependencies(PSDisplayMapping mapping, 
      List<PSDependency> dependencies)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      PSControlRef control = mapping.getUISet().getControl();
      if (control == null)
         throw new IllegalArgumentException(
            "mapping must specify a non-null control");
      
      if (dependencies == null || dependencies.size() == 0)
      {
         throw new IllegalArgumentException(
            "dependencies may not be null or empty");
      }
      
      // resolve macros to fields, handle single dimension
      List<Integer> depIds = new ArrayList<>();
      for (PSDependency dependency : dependencies)
      {
         IPSDependentObject dependentObj = dependency.getDependent();

         // add the dependent object
         if (!(dependentObj instanceof PSExtensionCall))
         {
            throw new IllegalArgumentException(
               "Don't know how to process dependent object " +
               dependentObj.getClass() );
         }
         
         PSExtensionCall ext = (PSExtensionCall) dependentObj;
         int extId = ext.getId();
         if (extId == 0)
            throw new IllegalArgumentException(
               "dependencies may not have id=0");
         
         if (dependency.getOccurrence() == PSDependency.SINGLE_OCCURRENCE)
         {
            PSExtensionCall singleExt = findExtension(ext.getName(), 
               m_controlDepExits.values());
            if (singleExt != null)
            {
               // need to replace
               ext.setId(singleExt.getId());
               extId = ext.getId(); 
            }
            m_singleDepExits.put(ext.getName(), ext);
         }
         
         m_controlDepExits.put(extId, ext);
         ext = (PSExtensionCall) ext.clone();
         PSContentEditorDependencyMacroResolver.replaceMacroWithValue(mapping, 
            ext.getParameters().iterator());
         m_ceInputDataExits.put(extId, ext); 
         depIds.add(extId);
      }
      
      m_controlDependencies.put(control.getId(), depIds);      
   }
   
   /**
    * Get the dependencies for the control specified by the supplied mapping.
    * Child mappers are not checked.  Modifications to the dependencies returned 
    * by this method are not reflected in this object, you must call 
    * {@link #setControlDependencies(PSDisplayMapping, List)} to make changes.
    * <p>
    * For any dependency specified by the supplied control meta that does not 
    * already have an instance defined, a new dependency instance is created 
    * with an id of "0" and returned, but not added to this map.  The caller 
    * must call {@link PSDependency#setId(int)} with a new unique id before 
    * calling {@link #setControlDependencies(PSDisplayMapping, List)} in order 
    * to add the new dependency. Note that even for new mappings, if the 
    * dependency has a single occurence type, it may already have a defined 
    * instance, so a dependency id should only be set if it is "0".
    * 
    * @param mapping The mapping, may not be <code>null</code> and must specify
    * a non-<code>null</code> control in its ui set, and it's control ref must
    * have a non-zero id.
    * @param meta The control meta to use, may not be <code>null</code>, must
    * match the control specified by the supplied mapping.  
    * 
    * @return The list of dependencies, never <code>null</code>, may be empty.
    * Note that for dependencies with a single occurence type, the same instance
    * is returned for any mapping referencing that dependency.
    */
   @SuppressWarnings("unchecked")
   public List<PSDependency> getControlDependencies(PSDisplayMapping mapping, 
      PSControlMeta meta)
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      PSControlRef control = mapping.getUISet().getControl();
      if (control == null)
         throw new IllegalArgumentException(
            "mapping must specify a non-null control");
      
      if (control.getId() == 0)
         throw new IllegalArgumentException(
            "the mapping's control must have a non-zero id");
      
      if (!meta.getName().equals(control.getName()))
         throw new IllegalArgumentException(
               "mapping and control meta must specify the same control name");
      
      // handle single dimension, resolve fields to macros
      List<PSDependency> results = new ArrayList<>();
      List<PSDependency> metaDeps = meta.getDependencies();
      Collection<PSExtensionCall> controlExt = getExtensionsForControl(control); 
      for (PSDependency dependency : metaDeps)
      {
         PSDependency dep = (PSDependency) dependency.clone();
         IPSDependentObject depObj = dep.getDependent();
         if (!(depObj instanceof PSExtensionCall))
         {
            throw new IllegalArgumentException(
               "Don't know how to process dependent object " + 
               depObj.getClass());
         }
         
         PSExtensionCall template = (PSExtensionCall) depObj;
         PSExtensionCall ext = findExtension(template.getName(), controlExt);
         if (ext != null)
         {
            PSExtensionCall depExt = m_controlDepExits.get(ext.getId());
            if (depExt == null)
            {
               depExt = (PSExtensionCall) ext.clone();
               PSContentEditorDependencyMacroResolver.replaceValueWithMacro(
                  mapping, template.getParameters().iterator(), 
                  depExt.getParameters().iterator());
               m_controlDepExits.put(depExt.getId(), depExt);
            }
            dep.setId(depExt.getId());
            dep.setDependent(depExt);
         }
         else 
         {
            dep.setId(0);
            if (dep.getOccurrence() == PSDependency.SINGLE_OCCURRENCE)
            {
               PSExtensionCall single = m_singleDepExits.get(
                  template.getName());
               if (single != null)
               {
                  dep.setId(single.getId());
                  dep.setDependent(single);
               }
               else
               {
                  m_singleDepExits.put(template.getName(), 
                     (PSExtensionCall) dep.getDependent());
               }
            }
         }
         
         results.add(dep);
      }
      
      return results;
   }

   /**
    * Clears all control dependencies. Usually called when saving, so that
    * {@link #setControlDependencies(PSDisplayMapping, List)} can be called for
    * each control thus effectively handling removals.
    */
   public void clearControlDependencies()
   {
      m_controlDependencies.clear();
      m_ceInputDataExits.clear();
      m_controlDepExits.clear();
      m_singleDepExits.clear();
   }
   
   /**
    * Get the collection of all extension calls specified by this map.
    * 
    * @return The calls, never <code>null</code>, may be empty.
    */
   public Collection<PSExtensionCall> getInputDataExtensions()
   {
      return m_ceInputDataExits.values();
   }
   
   /**
    * Convert the contents of this map back into a set of user properties.
    * 
    * @param inputDataExts The set of input data extensions, may not be
    * <code>null</code>, control dependency extensions are inserted at the 
    * beginning of this collection.
    * 
    * @return The properties, never <code>null</code>, may be empty.
    */
   public Map<String, String> generateUserProperties(
      PSExtensionCallSet inputDataExts)
   {
      Map<String, String> userProps = new HashMap<>();
      Set<Integer> extIds = new HashSet<>();
      
      for (Map.Entry<Integer, List<Integer>> entry : 
         m_controlDependencies.entrySet())
      {
         int ctrlId = entry.getKey();
         List<Integer> depIds = entry.getValue();
         if (depIds.isEmpty())
            continue;
         List<PSExtensionCall> exts = new ArrayList<>();
         for (int callId : depIds)
         {
            PSExtensionCall call = m_ceInputDataExits.get(callId);
            if (call != null)
               exts.add(call);
         }
         
         if (!exts.isEmpty())
         {
            int ins = 0;
            String ctrlProp = CONTROL + ctrlId;
            String depIdStr = "";
            for (PSExtensionCall call : exts)
            {
               int extId = call.getId();
               userProps.put(ctrlProp + DEP + extId, call.getName());
               
               if (depIdStr.length() > 0)
                  depIdStr += ",";
               depIdStr += extId;
               
               // don't add dupes
               if (!extIds.contains(extId))
               {
                  inputDataExts.add(ins, call);
                  extIds.add(extId);
               }
            }
            
            userProps.put(ctrlProp + DEP_IDS, depIdStr);
         }
      }
      
      return userProps;
   }   

   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSControlDependencyMap))
         return false;
      
      PSControlDependencyMap other = (PSControlDependencyMap) obj;
      
      // only compare control deps and input data exits as other members are
      // transient runtime artifacts
      if (!m_controlDependencies.equals(other.m_controlDependencies))
         return false;
      else if (!m_ceInputDataExits.equals(other.m_ceInputDataExits))
         return false;
      
      return true;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      // only include control deps and input data exits as other members are
      // transient runtime artifacts
      return m_controlDependencies.hashCode() + m_ceInputDataExits.hashCode();
   }

   /**
    * Convience method that calls 
    * {@link #findExtension(String, Collection, boolean)
    * findExtension(name, extensions, false)}
    */
   private PSExtensionCall findExtension(String name, 
      Collection<PSExtensionCall> extensions)
   {
      return findExtension(-1, name, extensions, false);
   }
   
   /**
    * Checks for a matching extension in the supplied collection, optionally
    * removing it.
    * 
    * @param id The extension id, use -1 to ignore this property.
    * @param name The name of the extension to match, 
    * assumed not <code>null</code>.
    * @param extensions The collection to search, assumed not <code>null</code>.
    * @param remove <code>true</code> to remove the extension, 
    * <code>false</code> to leave it in the collection.
    * 
    * @return The matching extension, or <code>null</code> if not found.
    */
   private PSExtensionCall findExtension(int id, String name, 
      Collection<PSExtensionCall> extensions, boolean remove)
   {
      Iterator<PSExtensionCall> calls = extensions.iterator();
      while (calls.hasNext())
      {
         PSExtensionCall call = calls.next();
         if (id != -1 && id != call.getId())
            continue;
         
         if (call.getName().equals(name))
         {
            if (remove)
               extensions.remove(call);
            return call;
         }
      }
      
      return null;
   }       
   
   /**
    * Find all extension calls the specified control ref has as dependencies.
    * 
    * @param control The control ref, assumed not <code>null</code>.
    * 
    * @return The calls, never <code>null</code>, may be empty.
    */
   private Collection<PSExtensionCall> getExtensionsForControl(
      PSControlRef control)
   {
      List<PSExtensionCall> extList = new ArrayList<>();
      List<Integer> depIds = m_controlDependencies.get(control.getId());
      if (depIds != null)
      {
         for (int id : depIds)
         {
            extList.add(m_ceInputDataExits.get(id));
         }         
      }
      
      return extList;
   }
   
   /**
    * Constant for the property name prefix.
    */
   public static final String CONTROL = "Control";   
   
   /**
    * Constant for the property name suffix when specifying the extension call
    * name for a dependency.
    */
   public static final String DEP = "_Dependency";
   
   /**
    * Map of dependencies for each control, never <code>null</code> after 
    * contruction, may be empty, key is the control id, value is the dependency 
    * id.
    */
   private Map<Integer, List<Integer>> m_controlDependencies = 
      new HashMap<>();
   
   /**
    * Map of extension component id to exit, never <code>null</code> after 
    * construction, contains extensions specified by control dependencies with
    * parameters that do not specify macros. 
    */
   private Map<Integer, PSExtensionCall> m_ceInputDataExits = 
      new HashMap<>();
   
   /**
    * Map of extension component id to exit, never <code>null</code> after 
    * construction, contains extensions specified by control dependencies with
    * parameters that specify macros.
    */
   private Map<Integer, PSExtensionCall> m_controlDepExits = 
      new HashMap<>();
   
   /**
    * Map of extensions known to be specified by a control as "single
    * occurrence" (see {@link PSDependency#SINGLE_OCCURRENCE}. Never
    * <code>null</code>, may be empty, does not necessarily contain all such
    * dependencies, only those that have been checked. Key is the extension
    * name. Used to return the same extension call instance for such
    * dependencies.
    */
   private Map<String, PSExtensionCall> m_singleDepExits = 
      new HashMap<>();
   
   /**
    * Constant for the property name suffix when specifying control 
    * dependency ids.
    */
   private final String DEP_IDS = "_DependencyIds";
     
   
}
