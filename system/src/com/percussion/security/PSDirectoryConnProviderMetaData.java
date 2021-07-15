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

package com.percussion.security;

import com.percussion.data.PSResultSet;
import com.percussion.data.PSSqlException;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.server.PSServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.CompoundName;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;



/**
 * The PSDirectoryConnProviderMetaData class implements cataloging for
 * the PSDirectoryConnProvider security provider.
 * 
 * @author chadloder
 */
public class PSDirectoryConnProviderMetaData extends PSJndiProviderMetaData
{
   /**
    * Construct a meta data object for the specified provider
    * instance.
    *
    * @param inst The provider instance. Can be <CODE>null</CODE>,
    * in which case not all of the information will be available.
    */
   PSDirectoryConnProviderMetaData(PSDirectoryConnProvider inst)
   {
      super(inst);
   }

   /**
    * Default constructor to find connection properties, etc.
    */
   public PSDirectoryConnProviderMetaData()
   {
      this(null);
   }

   /**
    * Get the name of this security provider.
    *
    * @return      the provider's name
    */
   public String getName()
   {
      return PSDirectoryConnProvider.SP_NAME;
   }

   /**
    * Get the full name of this security provider.
    *
    * @return      the provider's full name
    */
   public String getFullName()
   {
      return "Directory Connection Security Provider";
   }

   /**
    * Get the descritpion of this security provider.
    *
    * @return      the provider's description
    */
   public String getDescription()
   {
      return "Directory server authentication proxy.";
   }
   
   /**
    * See {@link IPSSecurityProviderMetaData#getObjects(String[], String[])} for
    * description.  The following information is specific to this class:
    * <ul>
    * <li>This class does not support the <code>"_"</code> wildcard
    * in filter patterns, and will throw a SQLException if one is supplied.</li>
    * <li>If returning user names, they are not distiguished, and only contain
    * the value of the user entries principle attribute.</li>
    * <li>If returning group names, they fully qualified distiguished name.</li>
    * </ul>
    */
   public ResultSet getObjects(String[] objectTypes, String[] filterPattern)
      throws SQLException
   {
      List obType = new ArrayList();
      List obId = new ArrayList();
      List obName = new ArrayList();

      if (m_instance instanceof PSDirectoryConnProvider)
      {
         PSDirectoryConnProvider provider = 
            (PSDirectoryConnProvider) m_instance;
         
         PSServerConfiguration config = PSServer.getServerConfiguration();
         
         PSDirectorySet directorySet = config.getDirectorySet(
            provider.getDirectoryProvider().getReference().getName());

         // get the object attribute and ask for it in the results
         String objectAttributeName = 
            directorySet.getRequiredAttributeName(
               PSDirectorySet.OBJECT_ATTRIBUTE_KEY);
         String[] attrIDs = { objectAttributeName };

         Iterator references = directorySet.iterator();
         while (references.hasNext())
         {
            PSReference reference = (PSReference) references.next();
            PSDirectory directory = config.getDirectory(reference.getName());
            PSAuthentication authentication = config.getAuthentication(
               directory.getAuthenticationRef().getName());
            
            provider.setProviderProperties(directory, authentication);
            
            DirContext ctx = null;
            NamingEnumeration results = null;
            NamingEnumeration attrs = null;
            NamingEnumeration attVals = null;
            try
            {
               if (objectTypes == null)
               {
                  List types = getSupportedTypes();
                  objectTypes = new String[types.size()];
                  types.toArray(objectTypes);
               }

               for (int i = 0; i < objectTypes.length; i++)
               {
                  if (objectTypes[i].equalsIgnoreCase(OBJECT_TYPE_USER))
                  {
                     // catalog all users under this provider
                     ctx = m_instance.getCatalogContext();

                     SearchControls controls = new SearchControls();
                     controls.setReturningAttributes(attrIDs);
                     if (directory.isShallowCatalogOption())
                        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                     else
                        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

                     // search using standard person object class
                     String searchFilter = "(" + 
                        PSJndiProvider.OBJECT_CLASS_ATTR + "=" + 
                           PSJndiProvider.OBJECT_CLASS_PERSON_VAL + ")";
                     if (filterPattern != null)
                        searchFilter = PSJndiUtils.getFilterString(
                           filterPattern, objectAttributeName, searchFilter);

                     // search with empty name to search current context
                     results = ctx.search("", searchFilter, controls);
                        
                     // use a set to return unique results.
                     Set valSet = new HashSet();
                     while (results.hasMore())
                     {
                        SearchResult result = (SearchResult) results.next();
                        for (attrs = result.getAttributes().getAll(); 
                           attrs.hasMore();)
                        {
                           Attribute attr = (Attribute) attrs.next();
                           
                           attVals = attr.getAll();
                           while (attVals.hasMoreElements())
                           {
                              String name = attVals.nextElement().toString();
                              valSet.add(PSJndiUtils.unEscapeDnComponent(name));
                           }
                           attVals.close();
                           attVals = null;
                        }
                        attrs.close();
                        attrs = null;
                     }
                     
                     results.close();
                     results = null;
                     
                     ctx.close();
                     ctx = null;

                     Iterator values = valSet.iterator();
                     while (values.hasNext())
                     {
                        String name = (String) values.next();
                        obType.add(OBJECT_TYPE_USER);
                        obId.add(name);
                        obName.add(name);  // name and id are the same
                     }
                  }
                  else if (objectTypes[i].equalsIgnoreCase(OBJECT_TYPE_GROUP))
                  {
                     // catalog all groups through this provider
                     Set valSet = new HashSet();

                     Iterator groupProviders = m_instance.getGroupProviders();
                     while (groupProviders.hasNext())
                     {
                        IPSGroupProvider gp = 
                           (IPSGroupProvider) groupProviders.next();
                        if (gp instanceof PSJndiGroupProvider)
                           ((PSJndiGroupProvider)gp).setProviderUrl(
                              directory.getProviderUrl());
                           

                        if (filterPattern == null)
                           valSet.addAll(gp.getGroups(null));
                        else
                        {
                           for (int j = 0; j < filterPattern.length; j++)
                           {
                              valSet.addAll(gp.getGroups(filterPattern[i]));
                           }
                        }
                     }

                     Iterator values = valSet.iterator();
                     while (values.hasNext())
                     {
                        CompoundName name = (CompoundName) values.next();
                        obType.add(OBJECT_TYPE_GROUP);
                        obId.add(name.toString());
                        obName.add(name.toString()); // name and id are the same
                     }
                  }
               }
            }
            catch (NamingException e)
            {
               // convert to SQLException and re-throw
               throw new PSSqlException(
                  IPSSecurityErrors.DIR_GET_OBJECTS_FAILED,
                     e.toString(), "0");
            }
            catch (PSSecurityException e)
            {
               // convert to SQLException and re-throw
               throw new PSSqlException(
                  IPSSecurityErrors.DIR_GET_OBJECTS_FAILED,
                     e.getLocalizedMessage(), "0");
            }
            finally
            {
               if (attVals != null)
                  try { attVals.close();} catch (NamingException ex) {}

               if (attrs != null)
                  try { attrs.close();} catch (NamingException ex) {}

               if (results != null)
                  try { results.close();} catch (NamingException ex) {}

               if (ctx != null)
                  try { ctx.close();} catch (NamingException ex) {}
            }
         }
      }

      HashMap columnNames = new HashMap();
      columnNames.put("OBJECT_TYPE", new Integer(1));
      columnNames.put("OBJECT_ID",  new Integer(2));
      columnNames.put("OBJECT_NAME", new Integer(3));

      List[] results = new List[]
      { 
         obType, 
         obId, 
         obName 
      };

      return new PSResultSet(results, columnNames, ms_GetObjectsRSMeta);
   }
}

