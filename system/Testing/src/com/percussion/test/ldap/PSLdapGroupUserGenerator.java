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
package com.percussion.test.ldap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * This class was created to generate an LDIF file containing a set of groups
 * and users. The caller specifies how many groups and users to create. The
 * following structure is created:
 * 
 * <pre>
 * com
 *    percussion
 *       group_performance_test
 *          groups
 *             group0
 *             ...
 *             groupN
 *          users
 *             user0
 *             ...
 *             userM
 * </pre>
 * 
 * Where M and N is the specified # of users or groups, minus 1, respectively.
 * <p>
 * The number of users added to each group is a random # between 0 and 199 (or
 * the # of users specified-1 if less than 200,) inclusive. It is guaranteed
 * that there will be 1 group w/ all users, 1 group w/ 1 user (if there are at
 * least 3 groups) and 1 group with 0 users (if there are at least 4 groups.)
 * <p>
 * A random number is used to find the beginning of a block of users within the
 * group of all users.
 * <p>
 * An example output is as follows:
 * <pre>
version: 1
 
# entry-id: 10
dn: ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: organizationunit
description: Directory structure used for testing performance when there are a large quantity of groups.
ou: group_performance_test

# entry-id: 11
dn: ou=groups,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: organizationunit
description: node that contains all groups
ou: groups

# entry-id: 12
dn: ou=users,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: organizationunit
description: node that contains all users
ou: users

# entry-id: 13
dn: cn=group0,ou=groups,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: groupofuniquenames
cn: group0
uniqueMember: uid=user0,ou=users,ou=group_performance_test,dc=percussion,dc=com

# entry-id: 14
dn: cn=group1,ou=groups,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: groupofuniquenames
cn: group1

# entry-id: 23
dn: uid=user0,ou=users,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: person
objectClass: oganizationalPerson
objectClass: inetorgperson
objectClass: rhythmyxperson
givenName: First0
sn: Last0
mail: user0@test.com
uid: user0
cn: First0 Last0
userPassword: {SSHA}c7ODY/Rpeb06gLee4icS+XxqTQjoPBNYIQrFYw==

# entry-id: 24
dn: uid=user1,ou=users,ou=group_performance_test,dc=percussion,dc=com
objectClass: top
objectClass: person
objectClass: oganizationalPerson
objectClass: inetorgperson
objectClass: rhythmyxperson
givenName: First1
sn: Last1
mail: user1@test.com
uid: user1
cn: First1 Last1
userPassword: {SSHA}c7ODY/Rpeb06gLee4icS+XxqTQjoPBNYIQrFYw==
 * </pre>
 * 
 * Note that the generated output contains object classes and attributes that
 * are somewhat specific to our test environment.
 * <p>
 * See {@link PSLdapGroupUserGenerator#main(String[])} for usage.
 * 
 * @author paulhoward
 */
public class PSLdapGroupUserGenerator
{
   
   /**
    * Usage: com.percussion.test.ldap.PSLdapGroupUserGenerator filename #users #groups
    * <p>
    * Groups must be >= 2.
    * Users must be > 0;
    * <p>
    * Hasn't been thoroughly tested w/ all allowed input variations.
    * @param args
    */
   public static void main(String[] args)
      throws IOException
   {
      File ldifFile = new File(args[0]);
      Writer output = new FileWriter(ldifFile);
      PSLdapGroupUserGenerator gen = new PSLdapGroupUserGenerator();
      int groupCount = Integer.parseInt(args[2]);
      if (groupCount < 0)
         throw new IllegalArgumentException("Groups must be >= 2");
      int userCount = Integer.parseInt(args[1]);
      if (userCount < 0)
         throw new IllegalArgumentException("Users must be >= 0");
      System.out.println("Generating ldif file " + ldifFile.getAbsolutePath() 
            + " with " + groupCount + " groups and " + userCount + " users.");
      gen.generateOutput(output, groupCount, userCount); 
      output.close();
   }


   public PSLdapGroupUserGenerator()
   {}
   
   public void generateOutput(Writer output, int groupCount, int userCount)
      throws IOException
   {
      writeHeader(output);
      createContainers(output);
      createGroups(groupCount, userCount, output);
      createUsers(userCount, output);
   }

   private void writeHeader(Writer output)
      throws IOException
   {
      output.write("version: 1\r\n");
   }


   private void createGroups(int groupCount, int userCount, Writer output)
      throws IOException
   {
      if (groupCount < 2)
         throw new IllegalArgumentException("Must have 2 or more groups.");
      
      int[] idDistribution = new int[groupCount];
      idDistribution[0] = userCount;   //all users in group 0
      int groupIdx = 1;
      if (groupCount > 2)
      {
         //guarantee 1 group w/ exactly 1 user;
         idDistribution[groupIdx++] = 1;
      }
      if (groupCount > 3)
      {
         //guarantee 1 empty group;
         idDistribution[groupIdx++] = 0;
      }
      final int MAX_USERS_IN_GROUP = 200;
      final int actualMaxUsersInGroup = 
         MAX_USERS_IN_GROUP < userCount-1 ? MAX_USERS_IN_GROUP : userCount-1;
      SecureRandom r = new SecureRandom();
      for (; groupIdx < idDistribution.length; groupIdx++)
      {
         idDistribution[groupIdx] = r.nextInt(actualMaxUsersInGroup);
      }
      
      //debug
      int totalGroupMembers = 0;
      for (int ct : idDistribution)
      {
         System.out.print(ct + ", ");
         totalGroupMembers += ct;
      }
      System.out.println();
      System.out.println("Total members: " + totalGroupMembers);
      //end debug

      //guarantee group0 contains all users
      GroupLdapNode group = new GroupLdapNode(0);
      for (int ct = 0; ct < userCount; ct++)
         group.addMember(ct);
      writeNode(group, output);
      
      for (int i = 1; i < groupCount; i++)
      {
         group = new GroupLdapNode(i);
         int nextUserId = r.nextInt(userCount-idDistribution[i]);
         for (int ct = idDistribution[i]; ct > 0; ct--)
            group.addMember(nextUserId++);
         writeNode(group, output);
      }
   }

   private void createUsers(int userCount, Writer output)
      throws IOException
   {
      for (int i = 0; i < userCount; i++)
      {
         UserLdapNode user = new UserLdapNode(i);
         writeNode(user, output);
      }
   }

   /**
    * Creates the container nodes that form the hierarchy and will contain the
    * user and group nodes. The names of the nodes for the groups and users are
    * are stored in {@link #groupDn} and {@link #userDn}. 
    * 
    * @param output The ldif data will be written to this object. Assumed not
    * <code>null</code>.
    * 
    * @throws IOException
    */
   private void createContainers(Writer output)
      throws IOException
   {
      String rootNodeName = "ou=group_?performance_test,dc=percussion,dc=com";
      LdapNode parentNode = new OuLdapNode(rootNodeName, 
            "Directory structure used for testing performance when there are a large quantity of groups.");
      writeNode(parentNode, output);
      LdapNode groupsNode = new OuLdapNode("ou=groups," + rootNodeName, 
            "node that contains all groups");
      writeNode(groupsNode, output);
      groupDn = groupsNode.dn;
      LdapNode usersNode = new OuLdapNode("ou=users," + rootNodeName, 
            "node that contains all users");
      writeNode(usersNode, output);
      userDn = usersNode.dn;
   }
   
   
   private void writeNode(LdapNode node, Writer output)
      throws IOException
   {
      output.write("\r\n# entry-id: " + (entryId++) + "\r\n");
      node.write(output);
   }

   /**
    * The next number to use for entry ids. When a value is used, the user
    * must increment it so it is ready for use next time.
    */
   private int entryId = 10;
   
   /**
    * The fully qualified name (dn) of the node that contains all the group
    * nodes. Set in <code>createContainers</code> then never empty or modified
    * further.
    */
   private String groupDn;
   
   /**
    * The fully qualified name (dn) of the node that contains all the user
    * nodes. Set in <code>createContainers</code> then never empty or modified
    * further.
    */
   private String userDn;
   
   /**
    * The root of the name for all generated groups. A unique id is appended to
    * this name for each unique group.
    */
   private static final String GROUP_BASE_NAME = "group";

   /**
    * The root of the name for all generated users. A unique id is appended to
    * this name for each unique user.
    */
   private static final String USER_BASE_NAME = "user";

   private static List<String> tokenize(String value)
   {
      StringTokenizer toker = new StringTokenizer(value, ",");
      List<String> results = new ArrayList<String>();
      while (toker.hasMoreTokens())
         results.add(toker.nextToken().trim());
      return results;
   }
   

   /**
    * Contains the properties that are common to all ldap nodes.
    *
    * @author paulhoward
    */
   private abstract class LdapNode
   {
      /**
       * 
       * @param name The distinguished name: e.g. ou=foo,ou=bar,dc=perc,dc=com.
       * 
       * @param description May be <code>null</code> or empty.
       */
      public LdapNode(String name, String description)
      {
         dn = name;
         this.description = StringUtils.isBlank(description) ? "" : description;
         objectClass.add("top");
      }
      
      /**
       * Format the data in a way needed for an ldif file and write it to the
       * supplied stream.
       * 
       * @param stream Never <code>null</code>.
       */
      public void write(Writer stream)
         throws IOException
      {
         stream.write("dn: " + dn);
         stream.write("\r\n");
         for (String objClass : objectClass)
         {
            stream.write("objectClass: " + objClass);
            stream.write("\r\n");
         }
         if (description.length() > 0)
         {
            stream.write("description: " + description);
            stream.write("\r\n");
         }
      }
            
      
      /**
       * Never <code>null</code> after ctor.
       */
      public String dn;

      /**
       * Never empty after ctor.
       */
      public List<String> objectClass = new ArrayList<String>();
      
      /**
       * Never <code>null</code> after ctor.
       */
      public String description;
   }

   private class OuLdapNode extends LdapNode
   {
      public OuLdapNode(String name, String description)
      {
         super(name, description);
         List<String> nameParts = tokenize(name);
         String org = nameParts.get(0);
         ou = org.substring(org.indexOf("=")+1);
         objectClass.add("organizationalunit");
      }
      
      /**
       * Format the data in a way needed for an ldif file and write it to the
       * supplied stream.
       * 
       * @param stream Never <code>null</code>.
       */
      @Override
      public void write(Writer stream)
         throws IOException
      {
         super.write(stream);
         stream.write("ou: " + ou);
         stream.write("\r\n");
      }
            
      /**
       * Never <code>null</code> after ctor.
       */
      public String ou;
   }

   /**
    * Contains all the information needed for a group node in an ldif file. All
    * that is needed to create one is the name of the group, w/o the dc
    * components.
    */
   private class GroupLdapNode extends LdapNode
   {
      public GroupLdapNode(int groupId)
      {
         super("cn=" + GROUP_BASE_NAME + groupId + "," + groupDn, null);
         cn = GROUP_BASE_NAME + groupId;
         objectClass.add("groupofuniquenames");
      }
      
      /**
       * 
       * @param userId The id of the user to add as a member of this group. 
       * Assumed to be in the range of user ids present in the system.
       */
      public void addMember(int userId)
      {
         uniqueMember.add("uid=" + USER_BASE_NAME + userId + "," + userDn);
      }
      
      /**
       * Format the data in a way needed for an ldif file and write it to the
       * supplied stream.
       * 
       * @param stream Never <code>null</code>.
       */
      @Override
      public void write(Writer stream)
         throws IOException
      {
         super.write(stream);
         stream.write("cn: " + cn);
         stream.write("\r\n");
         for (String member : uniqueMember)
         {
            stream.write("uniqueMember: " + member);
            stream.write("\r\n");
         }
      }
            
      private String cn;
      
      /**
       * All members of this group. Never <code>null</code>, may be empty.
       */
      public List<String> uniqueMember = new ArrayList<String>();
   }
   
   /**
    * Contains all the information needed for a user node in an ldif file. All
    * that is needed to create one is a unique number that is appended to
    * several of the names within the object.
    */
   private class UserLdapNode extends LdapNode
   {
      public UserLdapNode(int userId)
      {
         super("uid=" + USER_BASE_NAME + userId + "," + userDn, null);
         objectClass.add("person");
         objectClass.add("organizationalPerson");
         objectClass.add("inetorgperson");
         objectClass.add("rhythmyxperson");
         this.userId = userId;
      }
      
      /**
       * Format the data in a way needed for an ldif file and write it to the
       * supplied stream.
       * @param stream Never <code>null</code>.
       */
      @Override
      public void write(Writer stream)
         throws IOException
      {
         super.write(stream);
         String givenName = "First" + userId;
         String sn = "Last" + userId;
         stream.write("givenName: " + givenName);
         stream.write("\r\n");
         stream.write("sn: " + sn);
         stream.write("\r\n");
         stream.write("mail: " + USER_BASE_NAME + userId + "@test.com");
         stream.write("\r\n");
         stream.write("uid: " + USER_BASE_NAME + userId);
         stream.write("\r\n");
         stream.write("cn: " + givenName + " " + sn);
         stream.write("\r\n");
         // pw is demo
         stream.write(
               "userPassword: {SSHA}c7ODY/Rpeb06gLee4icS+XxqTQjoPBNYIQrFYw==");
         stream.write("\r\n");
      }
      
      public int userId;
   }
}
