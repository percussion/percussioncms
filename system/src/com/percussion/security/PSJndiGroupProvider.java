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

import com.percussion.design.objectstore.PSJndiGroupProviderInstance;
import com.percussion.design.objectstore.PSJndiObjectClass;
import com.percussion.services.security.PSTypedPrincipal;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * Provides support for cataloging groups for a {@link PSJndiProvider}.  Also
 * responsible for determine if a particular subject is a member of one of the
 * groups supported by this group provider.
 */
public class PSJndiGroupProvider implements IPSGroupProvider
{
   /**
    * Constucts an instance of this class using the supplied security provider
    * def and group provider def.
    *
    * @param groupProviderDef The group provider instance definition to use.
    * May not be <code>null</code>.
    * @param directoryDef The directory definition to provide group
    * information for.  Used to get the specific settings used to
    * connect to the directory.  May not be <code>null</code>.
    * @param userObjectAttrName The attribute name that identifies a user, may
    * not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws NamingException if any group location specified in the
    * groupProviderDef is invalid.
    */
   public PSJndiGroupProvider(PSJndiGroupProviderInstance
      groupProviderDef, PSDirectoryDefinition directoryDef, 
      String userObjectAttrName) throws NamingException
   {
      if (groupProviderDef == null)
         throw new IllegalArgumentException("groupProviderDef may not be null");

      if (directoryDef == null)
         throw new IllegalArgumentException("directoryDef may not be null");

      if (StringUtils.isBlank(userObjectAttrName))
         throw new IllegalArgumentException(
            "userObjectAttrName may not be null or empty");
      
      m_groupProviderInstance = groupProviderDef;
      m_directoryDef = directoryDef;
      m_userObjectAttrName = userObjectAttrName;

      Iterator nodes = m_groupProviderInstance.getGroupNodes();
      while (nodes.hasNext())
      {
         String node = (String)nodes.next();
         m_groupLocationInfo.add(new GroupLocationInfo(node));
      }

      // parse out the base provider url (<prot>://server[:<port>]/)
      String providerUrl = m_directoryDef.getDirectory().getProviderUrl();
      setProviderUrl(providerUrl);
      m_dirBaseCtx = PSJndiUtils.getBaseContext(providerUrl);
   }

   /**
    * Determines if the specified user is a member of the specified group.
    * Nested groups are supported up to the number of levels defined by
    * {@link #MAX_NESTED_GROUPS}. Checks in the following manner:
    * <ul>
    * <li>Determines in which defined group location the specified group is
    * located.</li>
    * <li>Searches that location for the group entry.</li>
    * <li>Checks for each supported objectclass and corresponding memberlist
    * attribute.  For each memberlist:</li>
    * <ul>
    * <li>If static, check to see if the user's dn is in the list.  If not,
    * search for each member's entry, adding to the search filter each of the
    * possible group objectclass values, so as to retrieve that entry only if it
    * is a group.  for each group returned, recursively check up to 5 levels to
    * see if the user is a member.</li>
    * <li>If dynamic, check to see if the user is a member by parsing the
    * memberlist value into a base search dn and a filter. Add to the filter the
    * condition that the common name equals the user's name and perform the
    * search.  If any result is returned, the user is a member.  If not, perform
    * the search again, adding to the original filter the possible group
    * objectclass values.  This will return all members of the group that are
    * themselves groups.  Recursively check each entry to see if the user is a
    * member up to the number of levels defined by {@link #MAX_NESTED_GROUPS}.
    * </li>
    * </ul>
    * </ul>
    *
    * @param user The distinguished name of the user, may not be
    * <code>null</code> or empty.
    * @param group The distinguished name of the group, may not be
    * <code>null</code> or empty. Must be an existing group in the directory
    * this group provider represents, and must be an entry in one of the group
    * locations specified by this group provider.  Use
    * {@link #isGroupSupported(String) isGroupSupported} to check.
    *
    * @return <code>true</code> if the user is a member of the group,
    * <code>false</code> if not.
    *
    * @throws IllegalArgumentException if user or group is inavlid.
    * @throws PSSecurityException any errors occur.
    */
   public boolean isMember(String user, String group) throws PSSecurityException
   {
      if (user == null || user.trim().length() == 0)
         throw new IllegalArgumentException("user may not be null or empty");

      if (group == null || group.trim().length() == 0)
         throw new IllegalArgumentException("group may not be null or empty");

      if (!isGroupSupported(group))
         throw new IllegalArgumentException(
            "group is not supported by this provider");

      return isMember(user, group, 0);
   }


   /**
    * Get the collection of groups specified by this provider that match the
    * supplied filter if provided.  The steps taken are:<p>
    * <ol>
    * <li>Get the list of supported objectClasses and locations from the
    * provider instance member.</li>
    * <li>Search each location for entries with any of the supported objectClass
    * attribute values. Apply the filter if provided.</li>
    * <li>Build a collection of Strings, each containing the fully qualified
    * distinguished names of each group located.</li>
    * </ol>
    *
    * @param filter A valid LDAP filter to match on.  If <code>null</code>, all
    * groups are returned.  May not be empty.
    *
    * @return A collection of fully qualified distinguished group names as
    * Strings.  Never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if filter is invalid.
    * @throws PSSecurityException if any errors occur.
    */
   public Collection getGroups(String filter) throws PSSecurityException
   {
      if (filter != null && filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be empty");

      String searchFilter;
      if (filter != null)
      {
         searchFilter = PSJndiUtils.getFilterString(new String[] {filter},
            PSJndiGroupProvider.PRINCIPAL_GROUP_ATTR, 
            getGroupsSearchFilter());    
      }
      else
         searchFilter = getGroupsSearchFilter();
      
      Collection groups = new ArrayList();

      // if empty, no objectClasses defined, return empty collection
      if (searchFilter.trim().length() == 0)
         return groups;

      DirContext ctx = null;
      NamingEnumeration results = null;
      try
      {
         // walk group locations
         for (GroupLocationInfo locationInfo : m_groupLocationInfo)
         {
            // search context applying filter, getting back "cn" attribute
            CompoundName node = locationInfo.mi_groupLocation;

            // get the context for this location
            ctx = getDirContext(node);

            // ask for the name attribute back
            String[] attrIDs = {PRINCIPAL_GROUP_ATTR};
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(attrIDs);

            // search with empty name to search current context
            String fullSearchFilter;
            if (locationInfo.mi_groupFilter != null)
            {
               fullSearchFilter = "(& " + locationInfo.mi_groupFilter + " " + searchFilter + ")"; 
            }
            else
               fullSearchFilter = searchFilter;
            results = ctx.search("", fullSearchFilter, ctls);
            while (results.hasMore())
            {
               /* add the group, appending the current context to the relative
                * name returned
                */
               SearchResult result = (SearchResult)results.next();
               CompoundName cn = PSJndiUtils.getCompoundName(
                  result.getName(), node.toString());
               groups.add(cn.toString());
            }
         }
      }
      catch (NamingException e)
      {
         Object[] args = {m_groupProviderInstance.getName(),
            m_directoryDef.getDirectory().getName(), e.toString()};
         throw new PSSecurityException(IPSSecurityErrors.GET_GROUPS_FAILURE,
            args);
      }
      finally
      {
         if (results != null)
            try{ results.close();} catch (NamingException ex){}
            
         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }

      return groups;
   }

   // see IPSGroupProvider interface
   public Collection<String> getUserGroups(String userName)
   {
      Collection<String> userGroups = new ArrayList<String>();
      
      String fullUserName = getFullUserName(userName);
      if (fullUserName != null)
      {
         Iterator groups = getGroups(null).iterator();
         while (groups.hasNext())
         {
            String groupName = groups.next().toString();
            if (isMember(fullUserName, groupName))
               userGroups.add(groupName);
         }
      }
      
      return userGroups;
   }
   
   private String getFullUserName(String userName)
   {
      NamingEnumeration<SearchResult> results = null;
      DirContext context = null;
      try
      {
         String fullName = null;
         
         context = getDirContext(m_dirBaseCtx);
         SearchControls searchControls = PSJndiUtils.createSearchControls(
            m_directoryDef.getDirectory(), null); 
         searchControls.setCountLimit(1);
      
         Map filter = new HashMap();
         filter.put(m_userObjectAttrName, userName);

         results = context.search("", 
            PSJndiUtils.buildFilter(filter), searchControls);
         if (results.hasMore())
         {
            SearchResult result = results.next();
            
            fullName = result.getName();
            
            if (result.isRelative())
            {
               fullName = PSJndiUtils.getCompoundName(
                  fullName, m_dirBaseCtx).toString();
            }
         }
         
         return fullName;
      }
      catch(NamingException e)
      {
         Object[] args = {m_groupProviderInstance.getName(),
            m_directoryDef.getDirectory().getName(), e.toString()};
         throw new PSSecurityException(IPSSecurityErrors.GET_GROUPS_FAILURE,
            args);
      }
      finally
      {
         if (results != null)
            try{ results.close();} catch (NamingException ex){}

         if (context != null)
            try{ context.close();} catch (NamingException ex){}
      }
   }

   // see IPSGroupProvider interface
   public Collection<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups)
   {
      if (groups == null)
         throw new IllegalArgumentException("groups may not be null");
      
      List<IPSTypedPrincipal> members = new ArrayList<IPSTypedPrincipal>();
      
      // make copy to walk so we can do removals
      List<Principal> groupList = new ArrayList<Principal>(groups);
      for (Principal group : groupList)
      {
         String groupName = group.getName();
         if (!isGroupSupported(groupName))
            continue;
         
         List<IPSTypedPrincipal> groupMembers = getGroupMembers(groupName, 0);
         if (!groupMembers.isEmpty())
         {
            members.addAll(groupMembers);
            groups.remove(group);
         }
      }
      
      return members;
   }   
   
   /**
    * Recursively gets the supplied group's membership up to 5 levels deep.
    * 
    * @param groupName The name of the group to get members for, assumed not 
    * <code>null</code>.
    * @param level The current level, used to stop recursing after 5 levels.
    * 
    * @return A List of group members a principals, never <code>null</code>, may 
    * be empty.
    */
   private List<IPSTypedPrincipal> getGroupMembers(String groupName, int level)
   {
      List<IPSTypedPrincipal> members = new ArrayList<IPSTypedPrincipal>();

      DirContext ctx = null;
      NamingEnumeration results = null;
      NamingEnumeration ae = null;
      NamingEnumeration attVals = null;
      try
      {
         // get group entry
         CompoundName groupCn = PSJndiUtils.getCompoundName(groupName);
         ctx = getDirContext(groupCn.getSuffix(1).toString());

         String searchFilter = "(" + groupCn.get(0) + ")";

         // ask for the objectClass and memberlist attributes back
         m_groupProviderInstance.getObjectClassesNames();

         String[] memberListAttributes = getMemberListAttributes();
         int size = memberListAttributes.length;
         String[] memberAttrs = new String[size + 1];
         memberAttrs[0] = PSJndiProvider.OBJECT_CLASS_ATTR;
         System.arraycopy(memberListAttributes, 0, memberAttrs, 1,
            size);

         SearchControls ctls = new SearchControls();
         ctls.setReturningAttributes(memberAttrs);

         // search with empty name to search current context
         results = ctx.search("", searchFilter, ctls);

         /* walk the results - attributes don't come back in the order in which
          * they are specified, so we need to build a map of each entry's
          * attributes and values.
          */
         List<Map<String, List<String>>> resultList = 
            new ArrayList<Map<String, List<String>>>();
         while (results.hasMore())
         {
            Map<String, List<String>> attrMap = new HashMap();
            SearchResult result = (SearchResult)results.next();
            Attributes attrs = result.getAttributes();
            for (ae = attrs.getAll(); ae.hasMore();)
            {
               Attribute attr = (Attribute)ae.next();
               String attrKey = attr.getID().toLowerCase();
               List valList = attrMap.get(attrKey);
               if (valList == null)
                  valList = new ArrayList();
               attVals = attr.getAll();
               while (attVals.hasMoreElements())
                  valList.add(attVals.nextElement().toString());
               
               attrMap.put(attrKey, valList);
               attVals.close();
               attVals = null;
            }
            if (ae != null)
            {
               ae.close();
               ae = null;
            }

            resultList.add(attrMap);
         }
         results.close();
         results = null;
         ctx.close();
         ctx = null;
         
         // set up lists of members that may need to be recursively searched
         List<String> staticMemberList = new ArrayList<String>();
         List<String> dynamicMemberList = new ArrayList<String>();         

         for (Map attrMap : resultList)
         {
            members.addAll(filterMemberList(attrMap, staticMemberList,
               dynamicMemberList));
         }
         
         // see if we should recurse
         if (++level > MAX_NESTED_GROUPS)
         {
            // add any static members to the results
            for (String staticMember : staticMemberList)
            {
               IPSTypedPrincipal principal = dnToPrincipal(staticMember); 
               members.add(principal);
            }
            return members;
         }
         

         /*
          * Now need to recurse any returned members that are groups.
          * Start with static groups.
          */
         for (String staticMember : staticMemberList)
         {
            List<IPSTypedPrincipal> staticGroupMembers = 
               getGroupMembers(staticMember, level);
            
            // if no "members" returned, just add it to the return list as a 
            // member
            if (staticGroupMembers.isEmpty())
               members.add(dnToPrincipal(staticMember));
            else
               members.addAll(staticGroupMembers);
         }

         // now check dynamic groups
         for (String dynamicMember : dynamicMemberList)
         {
            // add all users returned by the query
            members.addAll(getDynamicUsers(dynamicMember));
            
            // get all groups returned by the query and recurse
            Iterator groupMembers = getDynamicGroupMembers(
               dynamicMember).iterator();
            while (groupMembers.hasNext())
            {
               String member = (String)groupMembers.next();
               members.addAll(getGroupMembers(member, level));               
            }
         }
      }
      catch (NameNotFoundException e)
      {
         // allow this to return current list
      }
      catch (NamingException e)
      {
         Object[] args = {m_groupProviderInstance.getName(),
            m_directoryDef.getDirectory().getName(), e.toString()};
         throw new PSSecurityException(IPSSecurityErrors.GET_GROUPS_FAILURE,
            args);
      }
      finally
      {
         if (attVals != null)
            try{ attVals.close();} catch (NamingException ex){}

         if (ae != null)
            try{ ae.close();} catch (NamingException ex){}            
            
         if (results != null)
            try{ results.close();} catch (NamingException ex){}

         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }
      
      return members;
   }

   /**
    * Convenience method that calls 
    * {@link #shouldTreatAsGroup(String, boolean) shouldTreatAsGroup(dn, true)} 
    * and then calls {@link #dnToPrincipal(String, boolean)} with the result.
    */
   private IPSTypedPrincipal dnToPrincipal(String dn) throws NamingException
   {
      return dnToPrincipal(dn, shouldTreatAsGroup(dn, true));
   }

   /**
    * Converts the supplied distinguished name to a principal.
    * 
    * @param dn The dn, assumed not <code>null</code> or empty.
    * @param treatAsGroup <code>true</code> to treat the dn as a group, 
    * <code>false</code> to treat it as a user.  Group names are fully 
    * qualified, whereas user names are only the principal object attribute
    * value.
    * 
    * @return The principal, never <code>null</code>.
    * 
    * @throws NamingException If the dn cannot be parsed.
    */   
   private IPSTypedPrincipal dnToPrincipal(String dn, boolean treatAsGroup) 
      throws NamingException
   {
      String member = PSJndiUtils.unEscapeDnComponent(
         PSJndiUtils.getCompoundName(dn).get(0));
      IPSTypedPrincipal principal;
      if (!treatAsGroup)
      {
         String user = StringUtils.substringAfter(member, "=");
         if (!StringUtils.isBlank(member))
            principal = PSTypedPrincipal.createSubject(user);
         else
            principal = PSTypedPrincipal.createUndefined(member);
      }
      else
         principal = PSTypedPrincipal.createGroup(member);
      
      return principal;
   }   

   /**
    * Executes the query specified by the supplied dynamic group member for
    * users only and returns the results.
    * 
    * @param searchUrl The base query to execute, assumed not <code>null</code>
    * or empty.
    * 
    * @return The users found in the group membership.
    * 
    * @throws NamingException If there are any errors. 
    */
   private Collection<IPSTypedPrincipal> getDynamicUsers(String searchUrl) 
      throws NamingException
   {
      StringBuffer baseBuf = new StringBuffer();
      String filter = parseSearchUrl(searchUrl, baseBuf);
      String base = baseBuf.toString();

      Collection<IPSTypedPrincipal> users = new ArrayList<IPSTypedPrincipal>();
      
      DirContext ctx = null;
      NamingEnumeration results = null;
      try
      {
         // get entries matching the filter and not the group objectclasses
         String groupCond = getGroupsSearchFilter();
         filter = "(& (" + filter + ") (!(" + groupCond + ")))";

         // perform a sub-tree search from the context, applying the filter
         SearchControls ctls = new SearchControls();
         String[] returnAtts = {m_userObjectAttrName};
         ctls.setReturningAttributes(returnAtts);
         ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

         ctx = getDirContext(base);
         results = ctx.search("", filter, ctls);
         while (results.hasMore())
         {
            SearchResult result = (SearchResult)results.next();
            CompoundName cn = PSJndiUtils.getCompoundName(result.getName(),
               base);
            users.add(dnToPrincipal(cn.toString(), false));
         }

         return users;
      }
      finally
      {
         if (results != null)
            try{results.close();} catch (NamingException ex){}
         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }
   }

   /**
    * Filters the members specified by the supplied attributes returning a list
    * of principles for each non-group member, and adding the remaining members
    * to one of the supplied lists.
    * 
    * @param attrMap The map of attributes returned, the key is the attribute
    * name, the value is a list of attribute values, assumed not
    * <code>null</code>.
    * @param staticMemberList List to which static group members are added,
    * assumed not <code>null</code>.
    * @param dynamicMemberList List to which dynamic group members are added,
    * assumed not <code>null</code>.
    * 
    * @return The list of non-group members as principals, never
    * <code>null</code>, may be empty.
    * 
    * @throws NamingException If there are any errors.
    */
   private Collection<IPSTypedPrincipal> filterMemberList(
      Map<String, List<String>> attrMap, List<String> staticMemberList, 
      List<String> dynamicMemberList) throws NamingException
   {
      List<IPSTypedPrincipal> groupMembers = new ArrayList<IPSTypedPrincipal>();
      
      List foundOcs = getObjectClasses(attrMap);
      if (foundOcs.isEmpty())
         return groupMembers;

      // walk supported object classes and see if in the list
      Iterator defOcs = m_groupProviderInstance.getObjectClassesNames();
      while (defOcs.hasNext())
      {
         String defOc = (String)defOcs.next();
         if (foundOcs.contains(defOc.toLowerCase()))
         {
            // found a supported objectClass, so get its memberlist attribute
            // values
            String memberAttrName =
               m_groupProviderInstance.getMemberAttribute(defOc);
            int memberAttrType =
               m_groupProviderInstance.getMemberAttributeType(defOc);
            List memberList = attrMap.get(memberAttrName.toLowerCase());

            // group may have no members
            if (memberList == null)
               continue;

            Iterator members = memberList.iterator();
            while (members.hasNext())
            {
               String member = (String)members.next();
               if (memberAttrType == PSJndiObjectClass.MEMBER_ATTR_STATIC)
               {
                  // check for group, but don't search, as the returned member
                  // list is searched on anyhow
                  if (shouldTreatAsGroup(member, false))
                     staticMemberList.add(member);
                  else
                     groupMembers.add(dnToPrincipal(member, false));
               }
               else
               {
                  dynamicMemberList.add(member);
               }
            }
         }
      }
      
      return groupMembers;
   }

   /**
    * Determine if the specified dn should be treated as a group or a user.
    * 
    * @param dn The dn to check, may not be <code>null</code> or empty.
    * @param doSearch <code>true</code> to search for the entry in case the
    * result is ambiguous, <code>false</code> to skip the search.
    * 
    * @return <code>true</code> if the dn should be treated as a group, 
    * <code>false</code> if we can treat it as a user.  If the results of the
    * check are ambiguous, the dn is treated as a group.
    * 
    * @throws NamingException If there is an error parsing the dn or searching
    * for a match. 
    */
   public boolean shouldTreatAsGroup(String dn, boolean doSearch) 
      throws NamingException
   {
      if (StringUtils.isBlank(dn))
         throw new IllegalArgumentException("dn may not be null or empty");
      
      // assume true and try to disprove.
      boolean treatAsGroup = true;
      if (!m_userObjectAttrName.equalsIgnoreCase(PRINCIPAL_GROUP_ATTR))
      {
         // we can be sure if the object attributes are different
         if (dn.startsWith(PRINCIPAL_GROUP_ATTR) && isGroupSupported(dn))
            treatAsGroup = true;
         else
            treatAsGroup = false;
      }
      else if (!isGroupSupported(dn))
         treatAsGroup = false;
      else if (doSearch)
      {
         // search for it as a group
         DirContext ctx = null;
         NamingEnumeration results = null;
         try
         {
            // get entries matching the filter and the supported objectClasses
            CompoundName cn = PSJndiUtils.getCompoundName(dn);
            ctx = getDirContext(cn.getSuffix(1).toString());
            
            String filter = cn.get(0);
            String groupCond = getGroupsSearchFilter();
            filter = "(& (" + filter + ") (" + groupCond + "))";

            // perform a sub-tree search from the context, applying the filter
            SearchControls ctls = new SearchControls();
            String[] returnAtts = {PRINCIPAL_GROUP_ATTR};
            ctls.setReturningAttributes(returnAtts);
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            results = ctx.search("", filter, ctls);
            if (!results.hasMore())
               treatAsGroup = false;
         }
         finally
         {
            if (results != null)
               try{results.close();} catch (NamingException ex){}
            if (ctx != null)
               try{ ctx.close();} catch (NamingException ex){}
         }         
      }
      
      return treatAsGroup;
   }

   /**
    * Checks to see if the node containing the group is one of the locations
    * supported by this group provider.
    *
    * @param group The name of the group, may not be <code>null</code> or empty.
    *
    * @return <code>true</code> if the group is supported, <code>false</code> if
    * not or if the group name is invalid.
    *
    * @throws IllegalArgumentException if group is <code>null</code> or empty.
    */
   public boolean isGroupSupported(String group)
   {
      if (group == null || group.trim().length() == 0)
         throw new IllegalArgumentException("group may not be null or empty");

      boolean isValid = false;
      try
      {
         CompoundName groupName = PSJndiUtils.getCompoundName(group);

         Iterator<GroupLocationInfo> nodes = m_groupLocationInfo.iterator();
         while (nodes.hasNext() && !isValid)
         {
            if (groupName.endsWith(nodes.next().mi_groupLocation))
               isValid = true;
         }
      }
      catch (InvalidNameException e)
      {
         // they gave an invalid name, allow to return false
      }

      return isValid;
   }

   // see IPSGroupProvider interface
   public int getType()
   {
      return m_groupProviderInstance.getType();
   }

   /**
    * Private version of {@link #isMember(String, String) isMember} used to
    * recursively check nested group membership, and track number of levels
    * checked.
    *
    * @param user The distinguished name of the user, assumed not
    * <code>null</code> or empty.
    * @param group The distinguished name of the group, assumed not
    * <code>null</code> or empty, and an existing group in the directory
    * this group provider represents.
    * @param level Represents the level of nested groups being checked.  Should
    * be <code>0</code> for the first call.  The value is incremented at the
    * beginning of each call and if it is greater than the number of levels
    * defined by {@link #MAX_NESTED_GROUPS}, the method will immediately
    * return <code>false</code>.
    *
    * @return <code>true</code> if the user is a member of the group,
    * <code>false</code> if not or if the number of levels defined by
    * {@link #MAX_NESTED_GROUPS} have been exceeded.
    *
    * @throws PSSecurityException any errors occur.
    */
   private boolean isMember(String user, String group, int level)
      throws PSSecurityException
   {
      if (++level > MAX_NESTED_GROUPS)
         return false;

      DirContext ctx = null;
      NamingEnumeration results = null;
      NamingEnumeration ae = null;
      NamingEnumeration attVals = null;
      try
      {
         // get group entry
         CompoundName groupName = PSJndiUtils.getCompoundName(group);
         ctx = getDirContext(groupName.getSuffix(1).toString());

         String searchFilter = "(" + groupName.get(0) + ")";

         // ask for the objectClass and memberlist attributes back
         m_groupProviderInstance.getObjectClassesNames();

         String[] memberListAttributes = getMemberListAttributes();
         int size = memberListAttributes.length;
         String[] memberAttrs = new String[size + 1];
         memberAttrs[0] = PSJndiProvider.OBJECT_CLASS_ATTR;
         System.arraycopy(memberListAttributes, 0, memberAttrs, 1,
            size);

         SearchControls ctls = new SearchControls();
         ctls.setReturningAttributes(memberAttrs);

         // search with empty name to search current context
         results = ctx.search("", searchFilter, ctls);

         // set up lists of members that may need to be recursively searched
         List staticMemberList = new ArrayList();
         List dynamicMemberList = new ArrayList();

         /* walk the results - attributes don't come back in the order in which
          * they are specified, so we need to build a map of each entry's
          * attributes and values.
          */
         while (results.hasMore())
         {
            Map attrMap = new HashMap();
            SearchResult result = (SearchResult)results.next();
            Attributes attrs = result.getAttributes();
            for (ae = attrs.getAll(); ae.hasMore();)
            {
               Attribute attr = (Attribute)ae.next();
               String attrKey = attr.getID().toLowerCase();
               List valList = (List)attrMap.get(attrKey);
               if (valList == null)
                  valList = new ArrayList();
               attVals = attr.getAll();
               while (attVals.hasMoreElements())
                  valList.add(attVals.nextElement().toString());
               
               attrMap.put(attrKey, valList);
               attVals.close();
               attVals = null;
            }
            if (ae != null)
            {
               ae.close();
               ae = null;
            }

            if (isUserInMemberList(user, attrMap, staticMemberList,
               dynamicMemberList))
            {
               return true;
            }
         }
         results.close();
         results = null;
         ctx.close();
         ctx = null;

         /*
          * not an immediate member, need to check any memberlists for group
          * entries - first check static groups
          */
         Iterator staticMembers = staticMemberList.iterator();
         while (staticMembers.hasNext())
         {
            String member = (String)staticMembers.next();
            if (isMember(user, member, level))
               return true;
         }

         // now check dynamic groups
         Iterator dynamicMembers = dynamicMemberList.iterator();
         while (dynamicMembers.hasNext())
         {
            String dynamicMember = (String)dynamicMembers.next();
            List groupMembers = getDynamicGroupMembers(dynamicMember);
            Iterator members = groupMembers.iterator();
            while (members.hasNext())
            {
               String member = (String)members.next();
               if (isMember(user, member, level))
               {
                  return true;
               }
            }
         }
      }
      catch (NameNotFoundException e)
      {
         // allow this to return false
      }
      catch (NamingException e)
      {
         Object[] args = {m_groupProviderInstance.getName(),
            m_directoryDef.getDirectory().getName(), e.toString()};
         throw new PSSecurityException(IPSSecurityErrors.GET_GROUPS_FAILURE,
            args);
      }
      finally
      {
         if (attVals != null)
            try{ attVals.close();} catch (NamingException ex){}

         if (ae != null)
            try{ ae.close();} catch (NamingException ex){}            
            
         if (results != null)
            try{ results.close();} catch (NamingException ex){}

         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }

      return false;
   }

   /**
    * Determines if the user is an immediate member of the group entry
    * represented by the supplied map of attributes.
    *
    * @param user The distinguished name of the user to check, assumed not
    * <code>null</code> or empty.
    * @param attrMap A Map of attributes representing the group entry.  The
    * keys are the attribute names as lowercase Strings, and the values are
    * List objects containing each value of that attribute the group entry
    * contained, as Strings.  Assumed not <code>null</code> and to contain at
    * least the <code>objectClass</code> key.
    * @param checkedStaticMembers A list to which this method will add the name
    * of any group member values checked from a static member list.  Assumed
    * not <code>null</code>.  Values are appended to the list as Strings,
    * preserving any preexisting values.
    * @param checkedDynamicMembers The same as <code>checkedStaticMembers</code>
    * except that it contains group member values checked from a dynamic member
    * list.
    *
    * @return <code>true</code> if the user is a member of the group,
    * <code>false</code> otherwise.
    *
    * @throws NamingException if any errors occur parsing names or searching
    * the directory.
    */
   private boolean isUserInMemberList(String user, Map attrMap,
      List checkedStaticMembers, List checkedDynamicMembers)
         throws NamingException
   {
      List foundOcs = getObjectClasses(attrMap);
      if (foundOcs.isEmpty())
         return false;

      // walk supported object classes and see if in the list
      Iterator defOcs = m_groupProviderInstance.getObjectClassesNames();
      while (defOcs.hasNext())
      {
         String defOc = (String)defOcs.next();
         if (foundOcs.contains(defOc.toLowerCase()))
         {
            // found a supported objectClass, so get its memberlist attribute
            // values
            String memberAttrName =
               m_groupProviderInstance.getMemberAttribute(defOc);
            int memberAttrType =
               m_groupProviderInstance.getMemberAttributeType(defOc);
            List memberList = (List)attrMap.get(memberAttrName.toLowerCase());

            // group may have no members
            if (memberList == null)
               continue;

            Iterator members = memberList.iterator();
            while (members.hasNext())
            {
               String member = (String)members.next();
               if (memberAttrType == PSJndiObjectClass.MEMBER_ATTR_STATIC)
               {
                  // static list - check for match
                  CompoundName memberName = PSJndiUtils.getCompoundName(
                     member);
                  CompoundName userName = PSJndiUtils.getCompoundName(
                     user);
                  if (memberName.equals(userName))
                     return true;

                  // not a match, add to the list
                  checkedStaticMembers.add(member);
               }
               else
               {
                  // dynamic list - need to search to verify
                  if (isUserInDynamicList(user, member))
                     return true;

                  // not a match, add to the list
                  checkedDynamicMembers.add(member);
               }
            }
         }
      }

      return false;
   }
   
   /**
    * Get the list of ObjectClass attribute values from the supplied attribute
    * map
    * 
    * @param attrMap Key is the attribute name, value is a list of attribute
    * values, assumed not <code>null</code>.
    * 
    * @return List of found object class values, never <code>null</code>, may
    * be empty.
    */
   private List<String> getObjectClasses(Map<String, List<String>> attrMap)
   {
      // build lowercase list of objectclass names from the directory
      List<String> foundOcs = new ArrayList<String>();
      
      List dirObjClasses = attrMap.get(
         PSJndiProvider.OBJECT_CLASS_ATTR.toLowerCase());
      if (dirObjClasses != null)
      {
         Iterator dirOcs = dirObjClasses.iterator();
         while (dirOcs.hasNext())
         {
            String dirOc = (String)dirOcs.next();
            foundOcs.add(dirOc.toLowerCase());
         }         
      }

      return foundOcs;
   }

   /**
    * Appends the user onto the search filter and performs the query.
    *
    * @param user The name of the user to check, assumed not <code>null</code>
    * or empty.
    * @param searchUrl The dynamic member value, assumed not <code>null</code>
    * or empty.
    *
    * @return <code>true</code> if user is a member, <code>false</code> if not.
    *
    * @throws NamingException if any errors occur
    */
   private boolean isUserInDynamicList(String user, String searchUrl)
      throws NamingException
   {
      StringBuffer baseBuf = new StringBuffer();
      String filter = parseSearchUrl(searchUrl, baseBuf);
      String base = baseBuf.toString();

      DirContext ctx = null;
      NamingEnumeration results = null;
      try
      {
         // get entries matching the filter and the member's common name
         CompoundName memberName = PSJndiUtils.getCompoundName(user);
         String memberCond = memberName.get(0);
         filter = "(& (" + filter + ") (" + memberCond + ") (" +
            PSJndiProvider.OBJECT_CLASS_ATTR + "=" +
            PSJndiProvider.OBJECT_CLASS_PERSON_VAL + "))";

         // perform a sub-tree search from the context, applying the filter
         SearchControls ctls = new SearchControls();
         ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         ctx = getDirContext(base);
         results = ctx.search("", filter, ctls);

         /* need to be sure that we got back our exact user, not just a user
          * with the same common name, but in a different node of the directory
          */
         boolean hasMatch = false;
         while(results.hasMoreElements() && !hasMatch)
         {
            SearchResult result = (SearchResult)results.next();
            CompoundName cn = PSJndiUtils.getCompoundName(result.getName(),
               base); // name returned is relative to search context
            if (cn.equals(memberName))
               hasMatch = true;
         }

         return hasMatch;
      }
      finally
      {
         // we may not have reached end of enumeration so explicitly close it
         if (results != null)
            try{ results.close();} catch (NamingException ex){}

         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }
   }

   /**
    * Gets any group members specified by the supplied ldap search URL.
    *
    * @param searchUrl The url, assumed not <code>null</code> or empty.
    *
    * @return A list of distinguished names as Strings whose entries contain
    * group objectClass atrribute values supported by this provider.  Never
    * <code>null</code>, may be empty if none are found.
    *
    * @throws NamingException if any errors occur
    */
   private List getDynamicGroupMembers(String searchUrl) throws NamingException
   {
      StringBuffer baseBuf = new StringBuffer();
      String filter = parseSearchUrl(searchUrl, baseBuf);
      String base = baseBuf.toString();

      List members = new ArrayList();
      DirContext ctx = null;
      NamingEnumeration results = null;
      try
      {
         // get entries matching the filter and the supported objectClasses
         String groupCond = getGroupsSearchFilter();
         filter = "(& (" + filter + ") (" + groupCond + "))";

         // perform a sub-tree search from the context, applying the filter
         SearchControls ctls = new SearchControls();
         String[] returnAtts = {PRINCIPAL_GROUP_ATTR};
         ctls.setReturningAttributes(returnAtts);
         ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

         ctx = getDirContext(base);
         results = ctx.search("", filter, ctls);
         while (results.hasMore())
         {
            SearchResult result = (SearchResult)results.next();
            CompoundName cn = PSJndiUtils.getCompoundName(result.getName(),
               base);
            members.add(cn.toString());
         }

         return members;
      }
      finally
      {
         if (results != null)
            try{results.close();} catch (NamingException ex){}
         if (ctx != null)
            try{ ctx.close();} catch (NamingException ex){}
      }
   }

   /**
    * Gets the initial context for the specified node.
    *
    * @param node The entry to bind the initial context to.  Assumed not
    * <code>null</code>.
    *
    * @return The context, never <code>null</code>.
    *
    * @throws NamingException if there are any errors.
    */
   private DirContext getDirContext(CompoundName node)
      throws NamingException
   {
      return getDirContext(node.toString());
   }

   /**
    * Gets the initial context for the specified distinguished name.
    *
    * @param dn The distinguished name of the entry to bind the intial context
    * to.  Assumed not <code>null</code> or empty.
    *
    * @return The context, never <code>null</code>.
    *
    * @throws NamingException if there are any errors.
    */
   private DirContext getDirContext(String dn)
      throws NamingException
   {
      return PSJndiUtils.createContext(m_directoryDef, m_providerUrl + dn);
   }
   
   /**
    * Set the new provider URL for this group provider.
    * 
    * @param providerUrl the new provider URL to be set, may be 
    *    <code>null</code>, must be a valid URL if provided.
    */
   public void setProviderUrl(String providerUrl)
   {
      if (providerUrl == null)
      {
         m_providerUrl = "";
      }
      else
      {
         try
         {
            m_providerUrl = getBaseUrl(providerUrl);
         }
         catch (MalformedURLException e)
         {
            throw new IllegalArgumentException(
               "providerUrl must be a valid URL");
         }
      }
   }

   /**
    * Get the base url from the provided string.  This consists of the
    * following pieces:<p>
    * <code>
    * <protocol>://<server>[:port]/
    * </code>
    *
    * @param strUrl The Url to parse and from which to extract the base.  May
    * be <code>null</code> or empty.
    *
    * @return The base Url, never <code>null</code>.
    *
    * @throws MalformedURLException if strUrl is <code>null</code>, empty, or
    * does not contain a valid Url.
    */
   private static String getBaseUrl(String strUrl) throws MalformedURLException
   {
      String protocol = PSJndiUtils.getProtocol(strUrl);
      URL url = PSJndiUtils.getURL(strUrl);

      int port = url.getPort();
      String strPort = (port == -1 ? "" : ":" + String.valueOf(port));
      String baseUrl = protocol + "://" + url.getHost() + strPort + "/";

      return baseUrl;
   }

   /**
    * Creates and returns a search filter that will find any entry that contains
    * a supported objectClass attribute value.  Filter is built once from the
    * supported object classes and cached.
    *
    * @return The filter string, never <code>null</code>, empty if no group
    * objecctClasses are defined.
    */
   private String getGroupsSearchFilter()
   {
      if (m_groupsSearchFilter == null)
      {
         StringBuffer buf = new StringBuffer();
         Iterator ocs = m_groupProviderInstance.getObjectClassesNames();
         while (ocs.hasNext())
         {
            String oc = (String)ocs.next();
            if (buf.length() == 0)
               buf.append("(|");

            buf.append(" (");
            buf.append(PSJndiProvider.OBJECT_CLASS_ATTR);
            buf.append("=");
            buf.append(oc);
            buf.append(")");
         }
         if (buf.length() != 0)
            buf.append(")");

         m_groupsSearchFilter = buf.toString();
      }

      return m_groupsSearchFilter;
   }

   /**
    * Returns an array of the attribute names the member lists are stored in for
    * each of the objectClasses.  Array is built once from the
    * supported object classes and cached.
    *
    * @return The array, never <code>null</code>, might be emtpy.
    */
   private String[] getMemberListAttributes()
   {
      if (m_memberListAttributes == null)
      {
         List attrs = new ArrayList();
         Iterator ocs = m_groupProviderInstance.getObjectClassesNames();
         while (ocs.hasNext())
         {
            String oc = (String)ocs.next();
            attrs.add(m_groupProviderInstance.getMemberAttribute(oc));
         }

         m_memberListAttributes = (String[])attrs.toArray(
            new String[attrs.size()]);
      }

      return m_memberListAttributes;
   }

   /**
    * Parses an LDAP url, returning the query filter portion, and also appending
    * the base portion to the supplied StringBuffer.  For example, if the url
    * is <p>
    * <code>ldap:///ou=percussion,c=us ??sub?(cn=m*)</code>
    *
    * <p>then<p>
    * <code>
    * base="ou=percussion,c=us"<br>
    * filter="(cn=m*)"
    *</code>
    *
    * @param url The url to parse, assumed not <code>null</code> or empty.
    * @param baseBuffer The StringBuffer onto which the base is appended.
    * Assumed not <code>null</code>.  If no base is supplied, nothing is
    * appended.
    *
    * @return The filter portion of the url, never <code>null</code>, may be
    * empty if no filter is supplied.
    */
   private static String parseSearchUrl(String url, StringBuffer baseBuffer)
   {
      String filter = "";
      String delim = "??sub?";

      // see if filter supplied
      int iDelim = url.lastIndexOf(delim);
      if (iDelim == -1)
         iDelim = url.length();
      else
         filter = url.substring(iDelim + delim.length());

      // trim off filter if there is one
      String base = url.substring(0, iDelim);

      // trim off protocol and server if supplied
      int iPrefix = base.lastIndexOf("/");
      if (iPrefix != -1)
      {
         if (base.length() == iPrefix + 1)
            base = "";
         else
            base = base.substring(iPrefix + 1);
      }

      baseBuffer.append(base);
      return filter;
   }

   /**
    * Name of the attribute used to identify a group entry by name.
    */
   public static final String PRINCIPAL_GROUP_ATTR = "cn";

   /**
    * The number of levels of nested groups we will search.  Currently this
    * value is <code>5</code>.
    */
   public static final int MAX_NESTED_GROUPS = 5;

   /**
    * The definition used by this group provider.  Never <code>null</code> or
    * modified after construction.
    */
   private PSJndiGroupProviderInstance m_groupProviderInstance;

   /**
   * The jndi directory def used to retrieve settings for binding
   * to the server in order to search and read group entries.  Initialized
   * during construction, never <code>null</code> after that.
   */
   private PSDirectoryDefinition m_directoryDef;

   /**
    * Attribute name to use for a user, not <code>null</code> or empty or
    * modified after construction.
    */
   private String m_userObjectAttrName;
   
   /**
    * List of group locations from the group provider instance. Never
    * <code>null</code>, could be empty.
    */
   private List<GroupLocationInfo> m_groupLocationInfo = 
      new ArrayList<GroupLocationInfo>();

   /**
    * A class to store a pair of related data objects. A small amount of
    * normalization is done to them.
    * <p>Instances should be treated as immutable.
    * 
    * @author paulhoward
    */
   private class GroupLocationInfo
   {
      /**
       * Pass in the value entered into the UI. It is processed and parsed into
       * its 2 pieces. Any ?'s in the DN must be escaped using the URL character
       * escaping mechanism (i.e. replace the ? with %3f.)
       * 
       * @param entry The name and optional filter. Of the form <em>name</em>
       * [?<em>filter</em>]. Assumed not <code>null</code> or empty.
       */
      public GroupLocationInfo(String entry)
         throws InvalidNameException
      {
         try
         {
            String node;
            String filter = null;
            if (entry.indexOf('?') > 0)
            {
               //there is a filter appended
               int pos = entry.indexOf('?');
               node = entry.substring(0, pos);
               filter = entry.substring(pos+1).trim();
               if (filter.length() > 0 && filter.charAt(0) != '(')
                  filter = "(" + filter + ")";
            }
            else
               node = entry;
            mi_groupLocation = PSJndiUtils.getCompoundName(URLDecoder.decode(
                  node, "UTF8"));
            mi_groupFilter = StringUtils.isBlank(filter) ? null : filter;
         }
         catch (UnsupportedEncodingException e)
         {
            //should never happen as UTF8 is always supported
            throw new RuntimeException(e);
         }
      }
      
      /**
       * The distinguished name supplied in the ctor.
       */
      public CompoundName mi_groupLocation;
      
      /**
       * The filter supplied in the ctor, normalized. Guaranteed to be of the
       * form <code>(<em>filter</em>)</code> or <code>null</code>,
       * never empty.
       */
      public String mi_groupFilter;
   }
   
   /**
    * Base url of server that this provider will use. Intialized during
    * construction, never <code>null</code> or modified after that.
    */
   private String m_providerUrl = null;
   
   /**
    * Base context of the directory, used to build user compound names, never
    * <code>null</code> or empty or modified after construction.
    */
   private String m_dirBaseCtx;

   /**
    * A filter that will return any entry that contains a supported objectClass
    * attribute value.  <code>null</code> until first call to
    * {@link #getGroupsSearchFilter()}, never <code>null</code> or modified
    * after that.
    */
   private String m_groupsSearchFilter = null;

   /**
    * An array of the attribute names used by each objectClass to store the
    * member lists.  <code>null</code> until the first call to
    * {@link #getMemberListAttributes()}, never <code>null</code> or modified
    * after that.
    */
   private String[] m_memberListAttributes = null;
}
