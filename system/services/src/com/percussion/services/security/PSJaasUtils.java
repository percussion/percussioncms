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
package com.percussion.services.security;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.security.PSGroupEntry;
import com.percussion.security.PSRoleEntry;
import com.percussion.security.PSUserAttributes;
import com.percussion.security.PSUserEntry;
import com.percussion.services.security.loginmods.data.PSGroup;
import com.percussion.services.security.loginmods.data.PSPrincipal;
import com.percussion.utils.security.IPSPrincipalAttribute;
import com.percussion.utils.security.IPSPrincipalAttribute.PrincipalAttributes;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.utils.security.PSSecurityCatalogException;

import java.security.Principal;
import java.security.acl.Group;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.apache.commons.lang.StringUtils;

/**
 * Handy utility functions for use with the Java Authorization and
 * Authentication Service
 * @author dougrand
 */
public class PSJaasUtils
{
   /**
    * Name of the JAAS group that contains the list of role names.
    */
   public static final String ROLE_GROUP_NAME = "Roles";

   public static final String ROLE_DEFAULT = "Default";
   
   /**
    * Predicate for filtering an iterater to return only group entries.
    */
   private static class PSGroupNamePredicate implements Predicate
   {
      /**
       * The name of the principal that contains groups, never 
       * <code>null</code>.
       */
      String mi_name = null;
      
      /**
       * Ctor
       * @param n name, assumed not <code>null</code>
       */
      PSGroupNamePredicate(String n)
      {
         mi_name = n;
      }
      
      public boolean evaluate(Object principal)
      {
         if (principal instanceof Principal)
         {
            return principal instanceof Group && 
               ((Principal) principal).getName().equals(mi_name);
         }
         else
            return false;
      }

   }

   /**
    * Find the group that represents the roles for the subject, or create a new
    * group with the appropriate name. If a group is created, it will be
    * appended to the collection of principals.
    * 
    * @param coll
    *           the collection of principals for the subject, must never be
    *           <code>null</code>
    * @param name
    *          the name of the group to find, never <code>null</code>
    * @return the group, either found or created, never <code>null</code>
    */
   @SuppressWarnings(value={"unchecked"})
   public static Group findOrCreateGroup(Collection coll, String name)
   {
      if (StringUtils.isEmpty(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      Iterator roleGroups =
         new FilterIterator(coll.iterator(), new PSGroupNamePredicate(name));
      if (! roleGroups.hasNext())
      {
         Group roleGroup = new PSGroup(name);
         coll.add(roleGroup);
         return roleGroup;
      }
      else
      {
         return (Group) roleGroups.next();
      }
   }

   /**
    * Find a principal of the class <code>PSPrincipal</code>
    * 
    * @param coll
    *           the collection of principals for the subject, must never be
    *           <code>null</code>
    * @return the first principal that matches or <code>null</code> if none
    *         match
    */
   @SuppressWarnings(value={"unchecked"})
   public static Principal findFirstPSPrincipal(Collection coll)
   {
      Iterator iter = new FilterIterator(coll.iterator(), new Predicate()
      {
         public boolean evaluate(Object principal)
         {
            return principal instanceof PSPrincipal;
         }
      });
      
      if (iter.hasNext())
         return (Principal) iter.next();
      else
         return null;
   }

   /**
    * Convert the supplied subject to a user entry.
    * 
    * @param subject The subject to convert, may not be <code>null</code>.
    * @param username The user name of the subject, may not be <code>null</code> 
    * or empty.
    * @param pw The user's password, may be <code>null</code> or empty.
    * 
    * @return The user entry, never <code>null</code>.
    */
   public static PSUserEntry subjectToUserEntry(Subject subject, 
      String username, String pw)
   {
      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");
      
      if (StringUtils.isBlank(username))
         throw new IllegalArgumentException(
            "username may not be null or empty");
      
      int accessLvl = 0;
   
      // Handle Groups Roles and attributes
      Collection<PSGroupEntry> gentries = new ArrayList<PSGroupEntry>();
      Collection<PSRoleEntry> roles = new ArrayList<PSRoleEntry>();
      PSAttributeList attList = new PSAttributeList();
      for (Principal p : subject.getPrincipals())
      {
         if (p instanceof IPSPrincipalAttribute)
         {
            IPSPrincipalAttribute pAttr = (IPSPrincipalAttribute) p;
            attList.setAttribute(pAttr.getName(), pAttr.getValues());
         }
         else if (p instanceof IPSTypedPrincipal)
         {
            IPSTypedPrincipal typedPrincipal  = (IPSTypedPrincipal) p;
            if (typedPrincipal.getPrincipalType().equals(
               IPSTypedPrincipal.PrincipalTypes.GROUP))
            {
               gentries.add(new PSGroupEntry(p.getName(), 0));
            }
         }
         else if (p instanceof Group)
         {
            Group g = (Group)p;
            String pname = g.getName();
            if (pname.equals(PSJaasUtils.ROLE_GROUP_NAME))
            {
               roles.addAll(getRoleEntries(g));
            }
         }
      }
      PSGroupEntry grouparr[] = new PSGroupEntry[gentries.size()];
      gentries.toArray(grouparr);
      PSUserAttributes userAttributes = null;
      if (attList.size() > 0)
         userAttributes = new PSUserAttributes(attList);
      PSRoleEntry[] rolearr = roles.toArray(new PSRoleEntry[roles.size()]);
      
      PSUserEntry entry = new PSUserEntry(username, accessLvl, grouparr, 
         rolearr, userAttributes, PSUserEntry.createSignature(username, pw));
      return entry;
   }

   /**
    * Recursively creates and returns role entries for each principal member of
    * the supplied group.
    * 
    * @param g The group, assumed not <code>null</code>.
    * 
    * @return The role entries, never <code>null</code>, may be empty.
    */
   private static Collection<PSRoleEntry> getRoleEntries(Group g)
   {
      Collection<PSRoleEntry> roles = new ArrayList<PSRoleEntry>();
      Enumeration<? extends Principal> groups = g.members();
      while (groups.hasMoreElements())
      {
         Principal p = groups.nextElement();
         if (p instanceof Group)
            roles.addAll(getRoleEntries((Group)p));
         else
            roles.add(new PSRoleEntry(p.getName(), 0));
      }

      return roles;
   }

   /**
    * Convert the supplied user entry to a JAAS Subject.
    * 
    * @param userEntry The entry, may not be <code>null</code>.
    * @param pwd The password, may be <code>null</code> or empty.
    * 
    * @return The subject, never <code>null</code>.
    */
   @SuppressWarnings(value={"unchecked"})
   public static Subject userEntryToSubject(PSUserEntry userEntry, String pwd)
   {
      if (userEntry == null)
         throw new IllegalArgumentException("userEntry may not be null");
      
      Subject subject = new Subject();
      // add subject name      
      Principal userprincipal = PSTypedPrincipal.createSubject(
         userEntry.getName());
      Set<Principal> principals = subject.getPrincipals();
      principals.add(userprincipal);
      Group rolegrp = findOrCreateGroup(principals, 
         PSJaasUtils.ROLE_GROUP_NAME);
      
      // add caller principal
      Group credgrp = findOrCreateGroup(principals,
            "CallerPrincipal");
      credgrp.addMember(userprincipal);
      
      // add groups
      PSGroupEntry[] groups = userEntry.getGroups();
      if (groups != null)
      {
         for (int i = 0; i < groups.length; i++)
         {
            IPSTypedPrincipal group = new PSTypedPrincipal(groups[i].getName(), 
               IPSTypedPrincipal.PrincipalTypes.GROUP);
            principals.add(group);
         }
      }
   
      // add roles
      PSRoleEntry[] roles = userEntry.getRoles();
      if (roles != null)
      {
         for (int i = 0; i < roles.length; i++)
         {
            String role = roles[i].getName();
            Principal roleprincipal = new PSPrincipal(role);
            rolegrp.addMember(roleprincipal);
         }
      }
   
      // add attributes
      PSUserAttributes attrs = userEntry.getAttributes();
      if (attrs != null)
      {
         Iterator entries = attrs.entrySet().iterator();
         while (entries.hasNext())
         {
            Map.Entry entry = (Map.Entry)entries.next();
            List<String> values = new ArrayList<String>();
            values.add((String) entry.getValue());
            IPSPrincipalAttribute attr = new PSPrincipalAttribute(
               (String) entry.getKey(), PrincipalAttributes.ANY, values);
            principals.add(attr);
         }
      }
      
      subject.getPrivateCredentials().add(pwd);
      subject.getPublicCredentials().add(userEntry.getName());
      
      return subject;
   }

   /**
    * Convenience method that calls 
    * {@link #convertSubject(PSSubject, String) convertSubject(psSub, null)}.
    */
   @SuppressWarnings("unchecked")
   public static Subject convertSubject(PSSubject psSub)
   {
      return convertSubject(psSub, null);
   }
   
   /**
    * Converts the supplied {@link PSSubject} to a JAAS {@link Subject}.
    * 
    * @param psSub The subject to convert, may not be <code>null</code>.
    * @param emailAttrName The name of the attribute designating the email
    * address, may be <code>null</code> or empty if not known.
    * 
    * @return The converted subject, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static Subject convertSubject(PSSubject psSub, String emailAttrName)
   {
      if (psSub == null)
         throw new IllegalArgumentException("psSub may not be null");
      
      Subject sub = new Subject();
      sub.getPublicCredentials().add(psSub.getName());
      
      Set<Principal> principals = sub.getPrincipals();
      principals.add(subjectToPrincipal(psSub));
      
      Iterator attrs = psSub.getAttributes().iterator();
      while (attrs.hasNext())
         principals.add(PSJaasUtils.convertAttribute(
            (PSAttribute) attrs.next(), emailAttrName));
      
      return sub;
   }   

   /**
    * Convenience method that calls 
    * {@link #convertAttribute(PSAttribute, String) 
    * convertAttribute(attr, null)}.
    */
   @SuppressWarnings("unchecked")
   public static IPSPrincipalAttribute convertAttribute(PSAttribute attr)
   {
      return convertAttribute(attr, null);
   }
   
   /**
    * Convert the {@link PSAttribute} to an {@link IPSPrincipalAttribute}
    * 
    * @param attr The attribute to convert, may not be <code>null</code>.
    * @param emailAttrName The name of the attribute designating the email
    * address, may be <code>null</code> or empty if not known.  Used to set the
    * type of the returned attribute.
    * 
    * @return The converted attribute, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   public static IPSPrincipalAttribute convertAttribute(PSAttribute attr, 
      String emailAttrName)
   {
      if (attr == null)
         throw new IllegalArgumentException("attr may not be null");
      
      List<String> values = new ArrayList<String>();
      Iterator valItr = attr.getValues().iterator();
      while (valItr.hasNext())
      {
         Object val = valItr.next();
         if (val != null)
            values.add(val.toString());
      }
      
      PrincipalAttributes attrType = attr.getName().equals(emailAttrName) ? 
         PrincipalAttributes.EMAIL_ADDRESS : PrincipalAttributes.ANY;
      
      PSPrincipalAttribute principalAttr = new PSPrincipalAttribute(
         attr.getName(), attrType, attr.getValues());
      return principalAttr;
   }
   
   /**
    * Converts the supplied subject to a typed principal
    * 
    * @param subject The subject to convert, may not be <code>null</code>.
    * 
    * @return The principal, never <code>null</code>.
    */
   public static IPSTypedPrincipal subjectToPrincipal(PSSubject subject)
   {
      return subjectToPrincipal(subject.getName(), subject.getType());
   }

   /**
    * Converts the supplied subject to a typed principal
    * 
    * @param subjectName The name of the subject, may not be <code>null</code> 
    * or empty. 
    * @param subjectType The type of subject, one of the 
    * <code>PSSubject.SUBJECT_TYPE_XXX</code> values.  If 0 is supplied, a type
    * of user is assumed. 
    * 
    * @return The principal, never <code>null</code>.
    */
   public static IPSTypedPrincipal subjectToPrincipal(String subjectName, 
      int subjectType)
   {
      if (StringUtils.isBlank(subjectName))
         throw new IllegalArgumentException(
            "subjectName may not be null or empty");
      
      return new PSTypedPrincipal(subjectName, getPrincipalType(subjectType));
   }   
   
   /**
    * Converts the supplied subject to a typed principal
    * 
    * @param subject The subject to convert, may not be <code>null</code>.
    * 
    * @return The principal, may be <code>null</code> if the name cannot be
    * determined.
    */
   public static IPSTypedPrincipal subjectToPrincipal(Subject subject)
   {
      boolean isUser = true;
      
      String userName = getUserNameFromTypedPrincipals(subject.getPrincipals(
         IPSTypedPrincipal.class), IPSTypedPrincipal.PrincipalTypes.SUBJECT);
      
      // now try as group
      if (StringUtils.isBlank(userName))
      {
         userName = getUserNameFromTypedPrincipals(subject.getPrincipals(
            IPSTypedPrincipal.class), IPSTypedPrincipal.PrincipalTypes.GROUP);
         isUser = false;
      }

      // perhaps from external cataloger
      if (StringUtils.isBlank(userName))
      {
         userName = getUserNameFromPrincipalAttributes(subject.getPrincipals(
            IPSPrincipalAttribute.class));
      }
      
      // try to find CallerPrincipal group
      if (StringUtils.isBlank(userName))
      {
         userName = getUserNameFromCallerPrincipal(subject.getPrincipals());
      }
      
      IPSTypedPrincipal principal = null;
      
      if (!StringUtils.isBlank(userName))
      {
         if (isUser)
            principal = PSTypedPrincipal.createSubject(userName);
         else
            principal = PSTypedPrincipal.createGroup(userName);
      }
      
      return principal;
   }
   
   /**
    * Tries to determine the user name from the supplied principal using the
    * "CallerPrincipal" group.
    * 
    * @param principals The principals to check, assumed not <code>null</code>.
    * 
    * @return The user name, may be <code>null</code> or empty.
    */
   private static String getUserNameFromCallerPrincipal(
      Set<Principal> principals)
   {
      String username = null;
      Iterator principalGroups = new FilterIterator(principals.iterator(), 
         new PSGroupNamePredicate("CallerPrincipal"));
      if (principalGroups.hasNext())
      {
         Group group = (Group) principalGroups.next();
         if (group.members().hasMoreElements())
         {
            Principal userPrincipal = group.members().nextElement();
            username = userPrincipal.getName();
         }
      }
      
      return username;
   }

   /**
    * Get the appropriate principal type from the specified subject type.
    * 
    * @param subjectType The type of subject, one of the 
    * <code>PSSubject.SUBJECT_TYPE_XXX</code> values.  If 0 is supplied, a type
    * of user is assumed.
    *  
    * @return The principal type, never <code>null</code>.
    */
   public static IPSTypedPrincipal.PrincipalTypes 
      getPrincipalType(int subjectType)
   {
      if (subjectType == PSSubject.SUBJECT_TYPE_GROUP)
         return IPSTypedPrincipal.PrincipalTypes.GROUP;
      else
         return IPSTypedPrincipal.PrincipalTypes.SUBJECT;
   }
   
   /**
    * Gets the principals representing the subjects groups.
    * 
    * @param subject The subject to check, may not be <code>null</code>.
    * 
    * @return The list of groups, never <code>null</code>, may be empty.
    */
   public static List<IPSTypedPrincipal> getSubjectGroups(Subject subject)
   {
      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");
      
      List<IPSTypedPrincipal> groups = new ArrayList<IPSTypedPrincipal>();
      for (Principal principal : subject.getPrincipals(
         IPSTypedPrincipal.class))
      {
         IPSTypedPrincipal typedPrincipal = (IPSTypedPrincipal)principal;
         if (typedPrincipal.getPrincipalType().equals(
            IPSTypedPrincipal.PrincipalTypes.GROUP))
         {
            groups.add(typedPrincipal);
         }
      }
      return groups;
   }

   /**
    * Converts the supplied JAAS {@link Subject} to a {@link PSSubject}.
    * 
    * @param subject The subject to convert, may not be <code>null</code>.
    * 
    * @return The subject, or <code>null</code> if the user's name cannot be
    * determined from the subjects principals.
    */
   public static PSSubject convertSubject(Subject subject)
   {
      // first try for typed principal
      boolean isUser = true;
      String userName = null;
      
      userName = getUserNameFromTypedPrincipals(subject.getPrincipals(
         IPSTypedPrincipal.class), IPSTypedPrincipal.PrincipalTypes.SUBJECT);
      
      // now try as group
      if (StringUtils.isBlank(userName))
      {
         userName = getUserNameFromTypedPrincipals(subject.getPrincipals(
            IPSTypedPrincipal.class), IPSTypedPrincipal.PrincipalTypes.GROUP);
         isUser = false;
      }

      // perhaps from external cataloger
      if (StringUtils.isBlank(userName))
      {
         userName = getUserNameFromPrincipalAttributes(subject.getPrincipals(
            IPSPrincipalAttribute.class));
      }
         
      if (StringUtils.isBlank(userName))
         return null;
      
      PSAttributeList attList = convertPrincipalAttributes(
         subject.getPrincipals(IPSPrincipalAttribute.class));
      
      int subType = isUser ? PSSubject.SUBJECT_TYPE_USER : 
         PSSubject.SUBJECT_TYPE_GROUP;
      
      return new PSGlobalSubject(userName, subType, attList);
   }
   
   /**
    * Determine if the supplied subject represents a group.
    * 
    * @param subject The subject to check, may not be <code>null</code>.
    *  
    * @return <code>true</code> if the subject's principals contains an
    * {@link IPSTypedPrincipal} of type <code>GROUP</code>, <code>false</code>
    * otherwise.
    */
   public static boolean isGroup(Subject subject)
   {
      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");
      
      return !StringUtils.isBlank(getUserNameFromTypedPrincipals(
         subject.getPrincipals(IPSTypedPrincipal.class), 
         IPSTypedPrincipal.PrincipalTypes.GROUP));
   }

   /**
    * Convert the supplied principals attributes to an attribute list, sorted by
    * attribute name, case-insensitive, ascending.
    * 
    * @param principals The set of principals to convert, assumed not 
    * <code>null</code>.
    * 
    * @return The attribute list, never <code>null</code>, will not include
    * the principal attribute of type <code>SUBJECT_NAME</code>.
    */
   private static PSAttributeList convertPrincipalAttributes(
      Set<IPSPrincipalAttribute> principals)
   {
      Set<IPSPrincipalAttribute> sorted = new TreeSet<IPSPrincipalAttribute>(
         new Comparator<IPSPrincipalAttribute>() {

            public int compare(IPSPrincipalAttribute o1, 
               IPSPrincipalAttribute o2)
            {
               Collator c = Collator.getInstance();
               return c.compare(o1.getName(), o2.getName());
            }});
      sorted.addAll(principals);
      
      PSAttributeList attList = new PSAttributeList();
      for (IPSPrincipalAttribute pAttr : sorted)
      {
         // skip subject
         if (pAttr.getAttributeType().equals(PrincipalAttributes.SUBJECT_NAME))
         {
            continue;
         }
         attList.setAttribute(pAttr.getName(), pAttr.getValues());
      }
      return attList;
   }

   /**
    * Check the supplied principals for a principal attribute of type 
    * <code>SUBJECT_NAME</code> and return it's first value if found.
    * 
    * @param principals The set of principals to check, assumed not 
    * <code>null</code>.
    * 
    * @return The name, may be <code>null</code> or empty.
    */
   private static String getUserNameFromPrincipalAttributes(
      Set<IPSPrincipalAttribute> principals)
   {
      String userName = null;
      for (IPSPrincipalAttribute attribute : principals)
      {
         if (attribute.getAttributeType().equals(
            PrincipalAttributes.SUBJECT_NAME))
         {
            List<String> vals = attribute.getValues();
            if (!vals.isEmpty())
            {
               userName = vals.get(0);
               break;
            }
         }
      }
      
      return userName;
   }

   /**
    * Check the supplied principals for a typed principal of type
    * <code>SUBJECT</code> and return the name of the first one found.
    * 
    * @param principals The set of principals to check, assumed not 
    * <code>null</code>.
    * @param type The type of principal to locate, assumed not 
    * <code>null</code>.
    * 
    * @return The name, may be <code>null</code> or empty.
    */
   private static String getUserNameFromTypedPrincipals(
      Set<IPSTypedPrincipal> principals, IPSTypedPrincipal.PrincipalTypes type)
   {
      String userName = null;
      for (IPSTypedPrincipal principal : principals)
      {
         if (principal.getPrincipalType().equals(type))
         {
            userName = principal.getName();
            break;
         }
      }
      
      return userName;
   }

   /**
    * Get the appropriate subject type from the supplied principal type
    *  
    * @param principalType The type to convert, may not be <code>null</code>.
    * 
    * @return The subject type.  Undefined principal types are treated as users.
    */
   public static int getSubjectType(PrincipalTypes principalType)
   {
      if (principalType == null)
         throw new IllegalArgumentException("principalType may not be null");
      
      if (principalType.equals(IPSTypedPrincipal.PrincipalTypes.GROUP))
         return PSSubject.SUBJECT_TYPE_GROUP;
      else
         return PSSubject.SUBJECT_TYPE_USER;
   }

   /**
    * Convert the supplied principal attribute to a {@link PSAttribute}
    * 
    * @param attribute The attribute to convert, may not be <code>null</code>.
    * 
    * @return The converted attribute, never <code>null</code>.
    */
   public static PSAttribute convertAttribute(IPSPrincipalAttribute attribute)
   {
      PSAttribute attr = new PSAttribute(attribute.getName());
      List<String> vals = new ArrayList<String>(attribute.getValues());
      attr.setValues(vals);
      
      return attr;
   }
   
   /**
    * Get the first value of the specified attribute from the supplied subject.
    * 
    * @param subject The subject to check, may not be <code>null</code>.
    * @param name The attribute name to find, may not be <code>null</code> or
    * empty.
    * 
    * @return The value, may be <code>null</code> or empty if not found or if
    * the attribute value is <code>null</code> or empty.
    */
   public static String getSubjectAttributeValue(Subject subject, String name)
   {
      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      String value = null;
      Set<IPSPrincipalAttribute> attrs = subject.getPrincipals(
         IPSPrincipalAttribute.class);
      for (IPSPrincipalAttribute attribute : attrs)
      {
         if (attribute.getName().equals(name))
         {
            List<String> values = attribute.getValues();
            if (!values.isEmpty())
               value = values.get(0);
         }
      }
      
      return value;
   }

   /**
    * Get the first value of the specified attribute from the supplied subject.
    * 
    * @param subject The subject to check, may not be <code>null</code>.
    * @param type The attribute type to find, may not be <code>null</code>.
    * 
    * @return The value, may be <code>null</code> or empty if not found or if
    * the attribute value is <code>null</code> or empty.
    */   
   public static String getSubjectAttributeValue(Subject subject, 
      PrincipalAttributes type)
   {
      if (subject == null)
         throw new IllegalArgumentException("subject may not be null");
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      String value = null;
      
      Set<IPSPrincipalAttribute> attrs = subject.getPrincipals(
         IPSPrincipalAttribute.class);
      for (IPSPrincipalAttribute attribute : attrs)
      {
         if (attribute.getAttributeType().equals(type))
         {
            List<String> values = attribute.getValues();
            if (!values.isEmpty())
               value = values.get(0);
         }
      }
      
      return value;
   }

   /**
    * Converts the supplied principal to a subject.
    * 
    * @param principal The principal to convert, may not be <code>null</code>.
    * 
    * @return The subject, never <code>null</code>, will have empty attribute
    * list.
    */
   public static PSSubject principalToSubject(IPSTypedPrincipal principal)
   {
      int subjectType;
      if (principal.getPrincipalType().equals(PrincipalTypes.GROUP))
         subjectType = PSSubject.SUBJECT_TYPE_GROUP;
      else
         subjectType = PSSubject.SUBJECT_TYPE_USER;
      
      return new PSGlobalSubject(principal.getName(), subjectType, null);
   }
   
   /**
    * Loads the supplied subject's roles.  If that subject has had roles set on 
    * it, only roles defined in the Rhythmyx backend are added, otherwise roles 
    * are added from all catalogers.
    * 
    * @param subject The subject, assumed not <code>null</code>. 
    * @param username The username of the subject, may be <code>null</code> 
    * or empty in which case the user's name is retrieved from the supplied
    * subject.
    *   
    * @throws LoginException If there are any errors.
    */
   public static void loadSubjectRoles(Subject subject, String username) 
      throws LoginException
   {
      IPSRoleMgr roleMgr = PSRoleMgrLocator.getRoleManager();
      
      IPSTypedPrincipal user;
      if (StringUtils.isBlank(username))
      {
         user = PSJaasUtils.subjectToPrincipal(subject);
         if (user == null)
            return;
      }
      else
         user = PSTypedPrincipal.createSubject(username);
      
      Group rolegrp = PSJaasUtils.findOrCreateGroup(
         subject.getPrincipals(), PSJaasUtils.ROLE_GROUP_NAME);
      List<IPSTypedPrincipal> groups = PSJaasUtils.getSubjectGroups(subject);
      
      try
      {
         Set<String> roles;
         if (rolegrp.members().hasMoreElements())
         {
            // only add backend roles
            roles = roleMgr.getDefaultUserRoles(user);
            for (IPSTypedPrincipal group : groups)
            {
               roles.addAll(roleMgr.getDefaultUserRoles(group));
            }
         }
         else
         {
            // query for all roles
            roles = roleMgr.getUserRoles(user);

            //CMS-5033 : Clearing the roles set if the user has only one role and that Role is "Default" to prevent user without any valid Role to log in into the system.
            //This is done as user with no valid role was able to log in into the system as the only available role was default role and that gave access to the system.
            if(roles.size() == 1 && roles.contains(PSJaasUtils.ROLE_DEFAULT)){
               roles.clear();
            }

            for (IPSTypedPrincipal group : groups)
            {
               roles.addAll(roleMgr.getUserRoles(group));
            }         
         }
         
         for (String roleName : roles)
         {
            rolegrp.addMember(new PSPrincipal(roleName));
         }
      }
      catch (PSSecurityCatalogException e)
      {
         throw new LoginException(e.getLocalizedMessage());
      }
   }   
}
