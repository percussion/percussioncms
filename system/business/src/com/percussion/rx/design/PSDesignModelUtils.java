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
package com.percussion.rx.design;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.server.PSRequest;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSDependent;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Util class for design models. Consists of static methods.
 * @author bjoginipally
 *
 */
public class PSDesignModelUtils
{

   private static final Logger log = LogManager.getLogger(PSDesignModelUtils.class);
   
   /**
    * Helper method to convert a given list of objects to list of strings if the
    * object in the original list is an instance of String.
    * 
    * @param list list of objects must not be <code>null</code>.
    * @return The list of strings, may be empty never <code>null</code>, the
    * size of the returned list may be less than the size of the original list
    * if any of the object in the original list is not a String object.
    */
   @SuppressWarnings("unchecked")
   public static List<String> getStringList(List list)
   {
      if (list == null)
         throw new IllegalArgumentException("list must not be null");
      List<String> temp = new ArrayList<>();
      for (Object object : list)
      {
         if(object instanceof String)
            temp.add((String) object);
      }
      return temp;
   }
   
   /**
    * Helper method to set the request to internal user.
    * 
    * @param origReq The original request used to determine if the request info
    * needs to be reset.
    */
   @SuppressWarnings("unchecked")
   public static void setRequestToInternalUser(PSRequest origReq)
   {
      try
      {
         PSRequestInfo.resetRequestInfo();
      }
      catch(Exception ignore)
      {
         
      }
      PSRequest req = PSRequest.getContextForRequest();
      PSRequestInfo.initRequestInfo((Map) null);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
   }

   /**
    * Helper method to reset the request to original request.
    * 
    * @param origReq The original request to which the request info will be
    * set.
    * @param userName The original user name
    */
   @SuppressWarnings("unchecked")
   public static void resetRequestToOriginal(PSRequest origReq, String userName)
   {
      PSRequestInfo.resetRequestInfo();
      PSRequestInfo.initRequestInfo((Map) null);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, origReq);
      PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, userName);
   }
   
   /**
    * Get the processor proxy used for component crud operations.
    *  
    * @return a local component proxy, never <code>null</code>.
    */
   public static PSComponentProcessorProxy getComponentProxy()
   {
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      setRequestToInternalUser(req);

      try
      {
         PSRequest compReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         return new PSComponentProcessorProxy(
               PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, compReq);
      }
      catch (PSCmsException e)
      {
         // this is not possible
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
         throw new RuntimeException(
               "Failed to create PSComponentProcessorProxy.", e);
      }
      finally
      {
         resetRequestToOriginal(req, origUser);
      }
   }
   
   /**
    * Gets the current version of the supplied design object.
    * 
    * @param guid The id of the design object, may not be <code>null</code>.
    * 
    * @return The design object version.  May be <code>null</code> if a
    * design model is not available for the object or if the object does not
    * support version.
    */
   public static Long getVersion(IPSGuid guid)
   {
      if (guid == null)
         throw new IllegalArgumentException("guid may not be null");
      
      Long version = null;
      
      IPSDesignModel model = getDesignModel(PSTypeEnum.valueOf(guid.getType()));
      if (model != null)
      {   
         try
         {
            version = model.getVersion(guid);
         }
         catch (UnsupportedOperationException e)
         {
            // design model does not support version
         }
      }
    
      return version;
   }
   
   /**
    * Gets the current version of the supplied design object.
    * 
    * @param type The type of design object, may not be <code>null</code>.
    * @param name The name of the design object, may not be blank.
    * 
    * @return The design object version.  May be <code>null</code> if a
    * design model is not available for the object or if the object does not
    * support version.
    */
   public static Long getVersion(PSTypeEnum type, String name)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be blank");
      
      Long version = null;
      
      IPSDesignModel model = getDesignModel(type);
      if (model != null)
      {   
         try
         {
            version = model.getVersion(name);
         }
         catch (UnsupportedOperationException e)
         {
            // design model does not support version                  
         }
      }
     
      return version;
   }
   
   /**
    * Gets the name of the supplied design object.
    * 
    * @param guid The id of the design object, may not be <code>null</code>.
    * 
    * @return The design object name.  May be <code>null</code> if a design
    * model is not available for the object. 
    */
   public static String getName(IPSGuid guid) throws PSNotFoundException {
      String name = null;
      
      IPSDesignModel model = getDesignModel(PSTypeEnum.valueOf(guid.getType()));
      if (model != null)
         name = model.guidToName(guid);
      
      return name;
   }
   
   /**
    * Gets the design model for the supplied design object type.
    * 
    * @param type The design object type, assumed not <code>null</code>.
    * 
    * @return The design model used to access design objects of the given type.
    * May be <code>null</code> if a design model is not available for objects of
    * the specified type.
    */
   private static IPSDesignModel getDesignModel(PSTypeEnum type)
   {
      IPSDesignModel model = null;
      
      try
      {
         IPSDesignModelFactory factory = 
            PSDesignModelFactoryLocator.getDesignModelFactory();
         
         model = factory.getDesignModel(type);
      }
      catch (PSMissingBeanConfigurationException e)
      {
         // design model does not exist for object type
      }
      
      return model;
   }
   
   /**
    * Create a component key for the supplied id and object type.
    * 
    * @param id the id for which to create the component key, assumed not
    *    <code>null</code>.
    * @param objType the type of the object for which to create the component
    *    key, assumed not <code>null</code> or empty.
    * @return the component key for the specified id and object type, never 
    *    <code>null</code>.
    */
   public static PSKey getComponentKey(IPSGuid id, String objType)
   {
      PSKey key = null;

      if (objType.equals(PSAction.getComponentType(PSAction.class)))
         key = PSAction.createKey(String.valueOf(id.longValue()));
      else if (objType.equals(PSDisplayFormat
         .getComponentType(PSDisplayFormat.class)))
         key = PSDisplayFormat.createKey(new String[] { String.valueOf(id
            .longValue()) });
      else if (objType.equals(PSSearch.getComponentType(PSSearch.class)))
         key = PSSearch
            .createKey(new String[] { String.valueOf(id.longValue()) });
      else
         throw new RuntimeException(
            "Cannot create component key for object type: " + objType);

      return key;
   }
   
   /**
    * Checks to see if the supplied id has any dependents, and if so, returns
    * comma delimited dependent types.
    * 
    * @param id The id to check, may not be <code>null</code>.
    * 
    * @return The error, or <code>null</code> if there are no dependents.
    */
   public static String checkDependencies(IPSGuid id)
   {
      IPSSystemService sysService = PSSystemServiceLocator.getSystemService();
      List<IPSGuid> depIds = new ArrayList<>(1);
      depIds.add(id);
      
      List<PSDependency> deps = sysService.findDependencies(
         depIds);
      PSDependency dep = deps.iterator().next();
      String depTypes = dep.getDependentTypes();
      return depTypes;
   }
   
   /**
    * Checks to see if the supplied associations have any dependents, and if so, 
    * returns an appropriate error exception.  
    * 
    * @param parent The id of the owner of the associations to check, may not be
    * <code>null</code>.
    * @param children The child ids of the associations to check, may not be
    * <code>null</code> or empty.
    * 
    * @return The error, or <code>null</code> if there are no dependents.
    */
   public static PSPair<List<String>,String> checkAssociationDependencies(IPSGuid parent, 
      List<IPSGuid> children)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent may not be null");
      if (children == null || children.isEmpty())
         throw new IllegalArgumentException(
            "children may not be null or empty");
      
      IPSSystemService sysService = PSSystemServiceLocator.getSystemService();
      List<IPSGuid[]> compIds = new ArrayList<>(children.size());
      for (IPSGuid childId : children)
      {
         IPSGuid[] compid = new IPSGuid[2];
         compid[0] = childId;
         compid[1] = parent;
         compIds.add(compid);
      }
      
      List<PSDependency> deps = sysService.findCompositeDependencies(
         compIds);
      
      Set<PSDependent> depSet = new HashSet<>();
      List<String> depIdList = new ArrayList<>();
      for (PSDependency dep : deps)
      {
         if (dep.getDependents().isEmpty())
            continue;
         depIdList.add(String.valueOf(dep.getId()));
         depSet.addAll(dep.getDependents());
      }
      
      String depTypes = null;
      for (PSDependent dependent : depSet)
      {
         if (depTypes == null)
            depTypes = "";
         else
            depTypes += ", ";
         
         depTypes += dependent.getDisplayType();
      }
      PSPair<List<String>,String> pair = new PSPair<>();
      pair.setFirst(depIdList);
      pair.setSecond(depTypes);
      return pair;
   }
   
   /**
    * Remove slot associations that reference the supplied id. If a slot is to
    * be modified and it is not already locked by the specified session and
    * user, it is locked, modified, and then unlocked. Pre-existing locks are
    * not released.
    * 
    * @param id The guid to check for associations to. If it specifies a content
    * type or template to which a slot has any associations, the slot will be
    * modified to remove the association. May not be <code>null</code>.
    * @throws PSAssemblyException If there are any errors saving a modified
    * slot.
    */
   public static void removeSlotAssocations(IPSGuid id)
      throws PSAssemblyException
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");

      IPSAssemblyService service = PSAssemblyServiceLocator
            .getAssemblyService();
      List<IPSTemplateSlot> allSlots = service.findSlotsByName(null);
      List<IPSTemplateSlot> modSlots = new ArrayList<>();
      for (IPSTemplateSlot slot : allSlots)
      {
         Collection<PSPair<IPSGuid, IPSGuid>> slotAssociations = slot
               .getSlotAssociations();
         Iterator<PSPair<IPSGuid, IPSGuid>> iter = slotAssociations.iterator();
         boolean modified = false;
         while (iter.hasNext())
         {
            PSPair<IPSGuid, IPSGuid> assoc = iter.next();
            if (id.equals(assoc.getFirst()) || id.equals(assoc.getSecond()))
            {
               iter.remove();
               modified = true;
            }
         }
         if (modified)
         {
            slot.setSlotAssociations(slotAssociations);
            modSlots.add(slot);
         }
      }
      if (!modSlots.isEmpty())
      {
         // now save the slots
         for (IPSTemplateSlot slot : modSlots)
         {
            service.saveSlot(slot);
         }
      }
   }
   
}
