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

package com.percussion.security;

import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.error.PSException;
import com.percussion.log.PSLogManager;
import com.percussion.log.PSLogServerWarning;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The PSSecurityProviderPool class pools instances of the security
 * providers for use by the ACL handlers and the security cataloger.
 * <P>
 * At this time, the same instance of the provider is returned to each
 * caller. Synchronization must be done within the security provider
 * implementation. We may change this in the future, but the caller should
 * always assume the returned object is thread-safe.
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSSecurityProviderPool
{
   /**
    * No external construction of this allowed.
    */
   private PSSecurityProviderPool()
   {
      super();
   }

   /**
    * Initialize the security provider pool with the specified configuration
    * objects.
    * <P>
    * <EM>Note: This should only be called by the server</EM>
    *
    * @param   config         the server configuration object containing
    *   *   the PSSecurityProviderInstance objects
    *
    * @see      com.percussion.design.objectstore.PSSecurityProviderInstance
    */
   public synchronized static void init(PSServerConfiguration config)
   {
      ms_instances = new HashMap<String, PSSecurityProvider>();
      ms_providers = new HashMap<Integer, List<PSSecurityProvider>>();
      ms_directoryCatalogers = new ArrayList<IPSDirectoryCataloger>();
      ms_roleCatalogers = new ArrayList<IPSInternalRoleCataloger>();

      if (config == null)
         return;

      // init security providers
      PSCollection pInstances = config.getSecurityProviderInstances();
      if ((pInstances == null) || (pInstances.size() == 0))
         return;

      PSSecurityProviderInstance spInst;
      for (int i = 0; i < pInstances.size(); i++) 
      {
         spInst = (PSSecurityProviderInstance)pInstances.get(i);
         addInstance(spInst, config);
      } 
      
      // init directory catalogers for all directory sets
      Iterator dirSets = config.getDirectorySets();
      while (dirSets.hasNext())
      {
         PSDirectorySet dirSet = (PSDirectorySet) dirSets.next();
         IPSDirectoryCataloger dirCat = initDirectoryCataloger(config, dirSet);
         if (dirCat != null)
            ms_directoryCatalogers.add(dirCat);
      }
      
      // instantiate backend dir provider and save it in collection as
      // well as in member
      ms_defaultDirCataloger = new PSBackEndDirectoryCataloger();
      ms_directoryCatalogers.add(ms_defaultDirCataloger);
      
      // init role catalogers for all role providers
      Iterator roleProviders = config.getRoleProviders();
      while (roleProviders.hasNext())
      {
         PSRoleProvider roleProvider = (PSRoleProvider) roleProviders.next();
         if (roleProvider != null)
            ms_roleCatalogers.add(initRoleCataloger(config, roleProvider));
      }
      
      // instantiate backend role provider and save it in collection as
      // well as in member
      ms_defaultRoleCataloger = new PSBackendRoleCataloger(null);
      ms_roleCatalogers.add(ms_defaultRoleCataloger);
   }


   /**
    * Instantiate a directory cataloger from the supplied directory set.
    * 
    * @param config The server config containing all directory services 
    * definitions, assumed not <code>null</code>.
    * @param dirSet The dierectory set to use, assumed not <code>null</code>.
    *  
    * @return the cataloger, never <code>null</code>.
    */
   private static IPSDirectoryCataloger initDirectoryCataloger(
      PSServerConfiguration config, PSDirectorySet dirSet)
   {
      PSReference directorySetReference = new PSReference(
         dirSet.getName(), 
         PSDirectorySet.class.getName());
      PSProvider directoryProvider = new PSProvider(
         PSDirectoryServerCataloger.class.getName(), 
         PSProvider.TYPE_DIRECTORY, directorySetReference);
      Properties props = new Properties();
      props.setProperty(PSSecurityProvider.PROVIDER_NAME, dirSet.getName());
      IPSDirectoryCataloger dirCat = 
         (IPSDirectoryCataloger) PSSecurityProvider.instantiateProvider(
            directoryProvider, props, config);
      if (dirCat != null)
         dirCat.setName(dirSet.getName());
      
      return dirCat;
   }

   /**
    * Instantiate a role cataloger from the supplied role provider.
    * 
    * @param config The server config containing all directory services 
    * definitions, assumed not <code>null</code>.
    * @param roleProvider The role provider to use, assumed not 
    * <code>null</code>.
    *  
    * @return the cataloger, never <code>null</code>.
    */
   private static IPSInternalRoleCataloger initRoleCataloger(
      PSServerConfiguration config, PSRoleProvider roleProvider)
   {
      PSReference roleProviderReference = new PSReference(
         roleProvider.getName(), PSRoleProvider.class.getName());
      PSProvider provider = new PSProvider(PSRoleCataloger.class.getName(), 
         PSProvider.TYPE_ROLE, roleProviderReference);      
      
      Properties props = new Properties();
      props.put(PSSecurityProvider.PROVIDER_NAME, roleProvider.getName());
      IPSInternalRoleCataloger roleCat = 
         (IPSInternalRoleCataloger) PSSecurityProvider.instantiateProvider(
         provider, props, config);
      
      return roleCat;
   }
   
   /**
    * Shutdown the security provider pool.
    * <P>
    * <EM>Note: This should only be called by the server</EM>
    */
   public synchronized static void shutdown()
   {
      ms_instances.clear();
      ms_providers.clear();
   }

   /**
    * Get the security provider instance of the specified name and type.
    *
    * @param inst the instance name to get
    *
    * @return the provider instance or <code>null</code> if not defined.
    */
   public synchronized static PSSecurityProvider getProvider(String inst)
   {
      return ms_instances.get(inst);
   }

   /**
    * Get the security provider meta data for the specified
    * provider type.
    *
    * @param   providerType   the provider type string. Must not be
    * <CODE>null</CODE>.
    *
    * @return The provider meta data or <code>null</code> if the provider
    * type string is not recognized.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public synchronized static IPSSecurityProviderMetaData getProviderMetaData(
      String providerType)
   {
      if (providerType == null)
         throw new IllegalArgumentException("providerType cannot be null");

      IPSSecurityProviderMetaData meta = null;
      providerType = providerType.toUpperCase(); // for fast comparison

      if (PSOdbcProvider.SP_NAME.toUpperCase().equals(providerType))
          meta = new PSOdbcProviderMetaData();
      else if (PSBackEndTableProvider.SP_NAME.toUpperCase().equals(providerType))
         meta = new PSBackEndTableProviderMetaData();
      else if (PSWebServerProvider.SP_NAME.toUpperCase().equals(providerType))
         meta = new PSWebServerProviderMetaData();
      else if (PSDirectoryConnProvider.SP_NAME.toUpperCase().equals(providerType))
         meta = new PSDirectoryConnProviderMetaData();

      return meta; // could be null
   }

   /**
    * Get all the security provider instances of the specified type.
    *
    * @param   type      the PSSecurityProvider.SP_TYPE_xxx type
    *
    * @return            an array of provider instances
    */
   public synchronized static PSSecurityProvider[] getAllProviders(int type)
   {
      if (type == PSSecurityProvider.SP_TYPE_ANY)
         return getAllProviders();

      List<PSSecurityProvider> entries = null;
      if (ms_providers != null)
         entries = ms_providers.get(new Integer(type));
      else
         entries = new ArrayList<PSSecurityProvider>();
      int size = (entries == null) ? 0 : entries.size();
      PSSecurityProvider[] ret = new PSSecurityProvider[size];
      if (size > 0)
         entries.toArray(ret);

      return ret;
   }

   /**
    * Get all the security provider instances.
    *
    * @return            an array of provider instances
    */
   public synchronized static PSSecurityProvider[] getAllProviders()
   {
      int size = ms_instances.size();
      PSSecurityProvider[] spList = new PSSecurityProvider[size];
      Iterator<PSSecurityProvider> ite = ms_instances.values().iterator();
      for (int i = 0; ite.hasNext(); i++) {
         spList[i] = ite.next();
      }

      return spList;
   }

   /**
    * Get a list of all instantiated directory catalogers.
    *  
    * @return The list, never <code>null</code>, may be empty.
    */
   public synchronized static List<IPSDirectoryCataloger> 
      getAllDirectoryCatalogers()
   {
      return new ArrayList<IPSDirectoryCataloger>(ms_directoryCatalogers);
   }
   
   /**
    * Get a list of all instantiated directory catalogers.
    *  
    * @return The list, never <code>null</code>, may be empty.
    */
   public synchronized static List<IPSInternalRoleCataloger> 
      getAllRoleCatalogers()
   {
      return ms_roleCatalogers == null ? new ArrayList<IPSInternalRoleCataloger>() : new ArrayList<IPSInternalRoleCataloger>(ms_roleCatalogers);
   }   
   
   /**
    * Get the role cataloger used to catalog backend role information.
    * 
    * @return The cataloger, never <code>null</code>.
    */
   public synchronized static IPSInternalRoleCataloger 
      getDefaultRoleCataloger()
   {
      return ms_defaultRoleCataloger;
   }
   
   
   /**
    * Get the directory cataloger used to catalog backend subject information.
    * 
    * @return The cataloger, never <code>null</code>.
    */
   public synchronized static IPSDirectoryCataloger 
      getDefaultDirectoryCataloger()
   {
      return ms_defaultDirCataloger;
   }
   
   /**
    * Sets up the security provider from its instance defintion.  Stores values
    * into HashMap ms_instances and ms_providers.
    *
    * @param spInst The security provider instance for which to create a
    *    provider and store the instance and provider. Assumed not
    *    <code>null</code>.
    * @param config the server configuration, may be <code>null</code>.
    */
   private synchronized static void addInstance(
      PSSecurityProviderInstance spInst, PSServerConfiguration config)
   {
      PSSecurityProvider sp = null;
      Properties props = spInst.getProperties();
      String spInstName = spInst.getName();
      int spType = spInst.getType();

      switch (spType)
      {
         case PSSecurityProvider.SP_TYPE_ODBC:
            sp = new PSOdbcProvider(props, spInstName);
            break;

         case PSSecurityProvider.SP_TYPE_BETABLE:
            sp = new PSBackEndTableProvider(props, spInstName);
            break;

         case PSSecurityProvider.SP_TYPE_WEB_SERVER:
            sp = new PSWebServerProvider(props, spInstName);
            break;
            
         case PSSecurityProvider.SP_TYPE_DIRCONN:
            try
            {
               sp = new PSDirectoryConnProvider(props, spInstName);
               PSDirectoryConnProvider dirConn = (PSDirectoryConnProvider) sp;

               // set directory provider
               Properties providerProps = props;
               dirConn.setDirectoryProvider(spInst.getDirectoryProvider(), 
                  providerProps, config);
               break;
            }
            catch (Exception e)
            {
               String spName = PSDirectoryConnProvider.SP_NAME;

               Object[] args = { spName, spInstName,
                  PSException.getStackTraceAsString(e) };

               PSLogManager.write(new PSLogServerWarning(
                  IPSSecurityErrors.PROVIDER_INIT_EXCEPTION, args, true,
                  "SecurityProviderPool"));

               return;
            }

         default:
            Object[] args = { String.valueOf(spType), spInstName };
            PSLogManager.write(new PSLogServerWarning(
               IPSSecurityErrors.PROVIDER_UNKNOWN, args,
               true, "SecurityProviderPool"));
            return;
      }   // end switch

      // add it to the instances hash
      PSSecurityProvider oldsp = ms_instances.put(spInstName, sp);
      if (oldsp != null) {   // we don't allow duplicate instance names!!!
         // log this!!
         Object[] args = { spInstName };
         PSLogManager.write(new PSLogServerWarning(
            IPSSecurityErrors.PROVIDER_INSTANCE_NAME_DUPLICATED, args,
            false, "SecurityProviderPool"));

         // keep the first instance as the stored instance
         ms_instances.put(spInstName, oldsp);
      }

      // and the providers array list within the hash
      Integer spKey = new Integer(spType);
      List<PSSecurityProvider> instList = ms_providers.get(spKey);
      if (instList == null)
         instList = new ArrayList<PSSecurityProvider>();
      instList.add(sp);
      ms_providers.put(spKey, instList);
      
      // add a directory provider if one is specified
      PSProvider provider = sp.getDefaultDirectoryProvider();
      if (provider != null)
      {
         IPSDirectoryCataloger dirCat = 
            (IPSDirectoryCataloger) PSSecurityProvider.instantiateProvider(
               provider, props, config); 
         if (dirCat != null)
         {
            dirCat.setName(sp.getInstance());
            ms_directoryCatalogers.add(dirCat);
         }
      }
   }

   /**
    * The provider objects.
    */
   private static Map<String, PSSecurityProvider> ms_instances;

   /**
    * The provider objects.
    *
    *  key = Integer({Provider Type})
    *  value = ArrayList of PSSecurityProvider objects
    */
   private static Map<Integer, List<PSSecurityProvider>> ms_providers;
   
   /**
    * Directory catalogers initialized during 
    * {@link #init(PSServerConfiguration)}, never <code>null</code> after that. 
    */
   private static List<IPSDirectoryCataloger> ms_directoryCatalogers;
   
   /**
    * Role catalogers initialized during 
    * {@link #init(PSServerConfiguration)}, never <code>null</code> after that. 
    */   
   private static List<IPSInternalRoleCataloger> ms_roleCatalogers;
   
   /**
    * The default directory cataloger initialized during 
    * {@link #init(PSServerConfiguration)}, never <code>null</code> after that. 
    */   
   private static IPSDirectoryCataloger ms_defaultDirCataloger;
   
   /**
    * The default role cataloger initialized during 
    * {@link #init(PSServerConfiguration)}, never <code>null</code> after that. 
    */   
   private static IPSInternalRoleCataloger ms_defaultRoleCataloger;
}
