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
package com.percussion.security;

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSSubject;

import java.util.Collection;
import java.util.Iterator;

/**
 * This interface is provided to provide access to user information such as
 * email address. It provides a simple search mechanism for finding collections
 * of users based on conditional logic.
 * <p>Methods are provided for specific properties that are widely used. The
 * methods are of the form get<Type> and get<Type>AttributeName. If such an
 * attribute is multi-valued, the first value found is returned.
 * <p>Most methods that require a user name take either a <code>String</code> 
 * that is the base user name (e.g. johnsmith) or a <code>PSSubject</code>.
 *
 * @author Paul Howard
 * @version  5
 */
public interface IPSDirectoryCataloger
{
   /**
    * Convenience method that calls {@link getEmailAddress(PSSubject)}. 
    * See that method for more details.
    *
    * @param userName the name of the user as known to Rhythmyx. Never 
    *    <code>null</code> or empty.
    */
   public String getEmailAddress(String userName);

   /**
    * Searches the directory catalog for the specified subject. It then gets
    * the email address based on the configured email attribute.
    *
    * @param user the subject as known to Rhythmyx. Never <code>null</code>.
    * @return the value associated with the configured email attribute, or
    *    <code>null</code> if no attribute is found. If the attribute holds
    *    multiple values, the first entry will be returned.
    * @throws UnsupportedOperationException if an email attribute has not been
    *    registered for this cataloger.
    */
   public String getEmailAddress(PSSubject user);

   /**
    * Each directory catalog can be configured with the name of the attribute
    * that contains the user's email address.
    *
    * @return either a non-empty string or <code>null</code> if the attribute
    *    has not been set. If <code>null</code> is returned, the 
    *    <code>getEmailAddress</code> methods will throw an exception if called.
    */
   public String getEmailAddressAttributeName();
   
   /**
    * Each directory catalog must be configured with the name for the object
    * attribute. This is required to search the object.
    * 
    * @return a non-empty <code>String</code> with the attribute name of the
    *    objects that this catloger searches for.
    */
   public String getObjectAttributeName();

   /**
    * Convenience method that calls {@link getAttribute(PSSubject, String)}. 
    * See that method for more details.
    *
    * @param userName the name of the user as known to Rhythmyx. Never 
    *    <code>null</code> or empty.
    */
   public String getAttribute(String userName, String attributeName);

   /**
    * Convenience method that calls {@link getAttributes(PSSubject, Collection)
    * getAttributes(user, new ArrayList().add(attributeName)}. See that method
    * for more details.
    *
    * @param user the subject as known to Rhythmyx. Never <code>null</code>.
    * @param attributeName the desired attribute, never <code>null</code> or
    *    empty.
    * @return the requested attribute or <code>null<code> if no attribute is
    *    found for the specified attribute name. If the attribute holds
    *    multiple values, the first entry will be returned.
    */
   public String getAttribute(PSSubject user, String attributeName);
   
   /**
    * Convenience method that calls {@link getAttributes(PSSubject)}. See that 
    * method for more details.
    * 
    * @param userName the name of the user as known to Rhythmyx. Never 
    *    <code>null</code> or empty.
    */
   public PSSubject getAttributes(String userName);
   
   /**
    * Get the attributes for the specified user from all configured directories.
    * 
    * @param user the user for which to get the attributes, not 
    *    <code>null</code>.
    * @return the supplied user with the attributes updated according to the    
    *    cataloger configuration, never <code>null</code>.
    */
   public PSSubject getAttributes(PSSubject user);

   /**
    * Convenience method that calls {@link getAttributes(PSSubject, Collection)}. 
    * See that method for more details.
    *
    * @param userName the name of the user as known to Rhythmyx. Never 
    *    <code>null</code> or empty.
    */
   public PSSubject getAttributes(String userName, Collection attributeNames);

   /**
    * Searches the directory catalog associated with the supplied subject. 
    * If it finds the user, it then gets the value for the specified attributes.
    *
    * @param user the subject as known to Rhythmyx. Never <code>null</code>.
    * @param attributeNames the set of attributes for which you want the
    *    value(s). If an attribute is not present for a user, <code>null</code> 
    *    is set for that attribute. If <code>null</code> or empty, all known
    *    attributes are returned for the user.
    * @return the supplied subject is returned. All requested attributes are
    *    set on this subject as attributes. Existing attributes on the supplied 
    *    subject will be overwritten. Attributes that are not found are added
    *    to the subject with <code>null<code> as value.
    */
   public PSSubject getAttributes(PSSubject user, Collection attributeNames);

   /**
    * Searches all registered directories for users that match the provided
    * search criteria. For each user that matches, the values for all requested
    * properties are returned.
    * 
    * @param criteria the conditions used to limit the returned set of users.
    * Each conditional is OR'd with the others in the array. If
    * <code>null</code> or empty, or if any entry is null, all users are
    * returned. This should be done with care as this could return a result of
    * many thousands of entries. Only <code>PSLiteral</code> types are allowed
    * for variable and value and only the <code>OPTYPE_EQUALS</code> and
    * <code>OPTYPE_LIKE</code> operators are allowed.
    * @param attributeNames the set of attributes for which you want the
    * value(s). If an attribute is not present for a user, <code>null</code>
    * is set for that attribute.
    * @return a valid collection of 0 or more <code>PSSubject</code> objects.
    * All attributes are set on the respective subject as attributes.
    */
   public Collection findUsers(PSConditional[] criteria,
         Collection attributeNames);

   /**
    * Get this provider's list of group provider objects.
    *
    * @return an iterator over zero or more group provider objects.  Never
    * <code>null</code>.
    */
   public Iterator<IPSGroupProvider> getGroupProviders();

   /**
    * Set the name used to identify this provider.
    * 
    * @param name The name, may not be <code>null</code> or empty.
    */
   public void setName(String name);
   
   /**
    * Get the name used to identify this provider.
    * 
    * @return The name, may be <code>null</code> if one has not been set, never
    * empty.
    */
   public String getName();

   
   /**
    * Get the name used to display this provider's type.
    * 
    * @return The name, may be <code>null</code> if one has not been set, never
    * empty.
    */   
   public String getCatalogerDisplayType();
   
   /**
    * Get the cataloger type
    * 
    * @return The cataloger type, never <code>null</code> or empty.
    */
   public String getCatalogerType();
}
