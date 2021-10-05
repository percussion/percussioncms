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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The CMS objectstore represents CMS concepts such as folders and slots as
 * Java objects. These objects know they are persisted in a database and they
 * can generate an Xml representation that can be passed to a Rhythmyx
 * resource to perform the updates ({@link #toDbXml(Document, Element, 
 * IPSKeyGenerator, PSKey) toDbXml}). Every such object must implement this 
 * interface.
 * <p>Although the objects know about the database, they don't know how to
 * reach the database. This is where the <code>PSProcessorProxy</code> and
 * <code>PSCmsProcessor</code> objects come in. They are responsible for making
 * sure the data gets to the server and is passed to the appropriate resource. 
 * See that interface for more details.
 * <p>The purpose of this design is to allow these objects to be used locally
 * on the server and remotely on clients (e.g. web service or workbench).
 * Therefore, any object implementing this interface cannot make any requests
 * to the server. If it must have a server dependency, it should be placed in
 * the server sub-package.
 * <p>Every derived class must implment a ctor that takes an element so it can
 * be constructed from an element that it generated via its toXml method.
 * <p>The clone, hashCode and equals methods treat the key and state specially.
 * It is believed that this behavior will be the most commonly desired.
 * Additional methods (xxxFull) have been added that consider the key values
 * and state if that should be required for special circumstances.
 * <p>Following is a description of how these classes are expected to be used
 * in various cases:
 * <table border="1">
 *    <tr>
 *       <th>Case</th>
 *       <th>Procedure</th>
 *    </tr>
 *    <tr>
 *       <td>New instance</td>
 *       <td>
 *          <ol>
 *             <li>Create the object using the new operator. The state will
 *                be DBSTATE_NEW and the key will be unassigned and not
 *                persisted.</li>
 *             <li>When finished changing it, pass it to the processor's save
 *                method.</li>
 *             <li>After the processor has finished, the key will be assigned
 *                and persisted and the state will be DBSTATE_UNMODIFIED.</li>
 *          </ol>
 *       </td>
 *    </tr>
 *    <tr>
 *       <td>Modify existing</td>
 *       <td>
 *          <ol>
 *             <li>Create the object using the new operator.</li>
 *             <li>Call the processor's load method and pass the returned
 *                element to the <code>fromXml</code> method. The key will
 *                be assigned and persisted and the state will be
 *                DBSTATE_UNMODIFIED.</li>
 *             <li>When finished changing it (after the first change, the state
 *                changes to DBSTATE_MODIFIED), pass it to the processor's save
 *                method.</li>
 *             <li>After the processor has finished, the state will be
 *                DBSTATE_UNMODIFIED.</li>
 *          </ol>
 *       </td>
 *    </tr>
 *    <tr>
 *       <td>Delete</td>
 *       <td>
 *          <ol>
 *             <li>If you have a persisted object, call the processor's
 *                delete method. Upon return, the key will be unassigned and
 *                the state will be DBSTATE_MARKEDFORDELETE, (which will be
 *                treated as DBSTATE_NEW if saved again).</li>
 *             <li>If your object has not been persisted, just stop using
 *                it, no need to call the processor (if you do, it is a noop.
 *                </li>
 *          </ol>
 *       </td>
 *    </tr>
 * </table>
 * <p>All 'top-level' components (those that are not used only as a child
 * component) must implement a method that returns the key for the object
 * that conforms to the following prototype:
 * <p>public static PSKey createKey(String value)
 * <p>If a value is supplied, an assigned key is created, otherwise, just a
 * key definition is returned.
 */
public interface IPSDbComponent extends IPSCmsComponent
{
   /**
    * A component can be in 1 of 4 states,
    * <ol>
    *    <li>This component represents an object that doesn't exist in the
    *       database.</li>
    *    <li>This component exactly represents an object as it exists in the
    *       database.</li>
    *    <li>This component represents an object in the db but has been
    *       changed in some way.</li>
    *    <li>The db object represented by this component no longer exists in the
    *       database. This is equivalent to being in the new state.</li>
    * </ol>
    * This value indicates that this object has never been saved to the db.
    */
   public static final int DBSTATE_NEW = 1;

   /**
    * See {@link #DBSTATE_NEW} for a full description.
    * This value indicates that this object started out representing a db
    * object, but has been modified. When it is saved, the db object will be
    * modified to match this component.
    */
   public static final int DBSTATE_MODIFIED = 2;

   /**
    * See {@link #DBSTATE_NEW} for a full description.
    * This value indicates that this object started out representing a db
    * object and still does. Performing a save on such a component is a no-op.
    */
   public static final int DBSTATE_UNMODIFIED = 3;

   /**
    * See {@link #DBSTATE_NEW} for a full description.
    * This value indicates that this object started out representing a db
    * object, but it is desired to delete it. If it is saved, the processor
    * will permanently remove the component from the database. This is
    * equivalent to passing the component's key to the processor's delete
    * method.
    */
   public static final int DBSTATE_MARKEDFORDELETE = 4;

   /**
    * Labels for each of the DBSTATE_xxx numeric values. The label for the
    * id is stored at the index of the value, e.g. the label for DBSTATE_NEW
    * is STATE_LABELS[DBSTATE_NEW].
    */
   public static final String[] STATE_LABELS =
   {
      "",
      "db_new",
      "db_modified",
      "db_unmodified",
      "db_marked_for_deletion"
   };

   /**
    * Every component has a unique type name. Be default this should be the
    * base class name.
    *
    * @return The type of this component, never <code>null</code> or empty.
    */
   public String getComponentType();


   /**
    * Gets the key that uniquely identifies this instance. Return of
    * a key does not mean that this instance has been persisted. To make
    * that determination, call {@link PSKey#isPersisted()}.
    *
    * @return A key in 1 of 3 states, just the definition (this is what you
    *    would get if you instantiated a new instance), assigned but not
    *    persisted, or persisted. The returned key is a clone, so changes to
    *    it will not affect this object.
    */
   public PSKey getLocator();

   /**
    * Whether or not this component is persisted to the database. In most
    * cases checking if the components key is persisted is sufficent. However,
    * in certain cases, collection components, which define their persistence in
    * terms of their contained children, need to provide their own implementation.
    *
    * @return <code>true</code> if this component is persisted to a db,
    *    otherwise <code>false</code>
    */
   public boolean isPersisted();

   /**
    * Whether or not this component's key is assigned values.
    *
    * @return <code>true</code> if this component contains a locator that
    *    has assigned values, otherwise <code>false</code>
    */
   public boolean isAssigned();

   /**
    * Sets the key that uniquely identifies this instance. Typically, this is
    * only used by the system, as setting the wrong key could produce
    * unexpected results. Setting a key may cause the state of this object
    * to change to maintain consistency.
    * <p>The following table shows all possible combinations between the
    * assigned, persisted and state variables of the key and this component,
    * indicating which are allowed and not. The 4 possibilities w/ assigned
    * <code>false</code> and persisted <code>true</code> are left out because
    * the key doesn't allow such a condition.
    * <p>The various methods that affect these 3 properties maintain the
    * contract as defined in the following table.
    * <table>
    *    <tr>
    *       <th>isAssigned</th>
    *       <th>isPersisted</th>
    *       <th>State</th>
    *       <th>Allowed</th>
    *    </tr>
    *    <tr>
    *       <td>true or false</td>
    *       <td>false</td>
    *       <td>new</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>true or false</td>
    *       <td>false</td>
    *       <td>modified</td>
    *       <td>no</td>
    *    </tr>
    *    <tr>
    *       <td>true or false</td>
    *       <td>false</td>
    *       <td>unmodified</td>
    *       <td>no</td>
    *    </tr>
    *    <tr>
    *       <td>true or false</td>
    *       <td>false</td>
    *       <td>deleted</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>true</td>
    *       <td>true</td>
    *       <td>new</td>
    *       <td>no</td>
    *    </tr>
    *    <tr>
    *       <td>true</td>
    *       <td>true</td>
    *       <td>modified</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>true</td>
    *       <td>true</td>
    *       <td>unmodified</td>
    *       <td>yes</td>
    *    </tr>
    *    <tr>
    *       <td>true</td>
    *       <td>true</td>
    *       <td>deleted</td>
    *       <td>no</td>
    *    </tr>
    * </table>
    *
    * @param locator An id appropriate for this object within the system.
    *    Never <code>null</code>. The definition of the supplied key must
    *    match the definition of the current key or an exception is thrown.
    *    A clone of the supplied key is stored locally, so subsequent changes
    *    to locator will not affect this object.
    */
   public void setLocator(PSKey locator);


   /**
    * If the key for this component has not been assigned, then the supplied
    * generator is used to assign the key. This can be useful if you are
    * creating linked objects.
    *
    * @param gen May be <code>null</code> if the key for this component is
    *    assigned from values in the object.
    *
    * @param parentKey The key of the parent of this object. If this object
    *    doesn't have a parent, it should be <code>null</code>. If one is
    *    supplied, it must be assigned.
    *
    * @throws PSCmsException  If any errors occur trying to allocate the
    *    ids.
    */
   public void assignKey(IPSKeyGenerator gen, PSKey parentKey)
      throws PSCmsException;


   /**
    * Creates an xml fragment properly formatted for use by the processor
    * assigned to this component type which contains information about what
    * has changed in this component since the last time it was persisted.
    * After the database is successfully updated, the
    * handler should call this object's {@link #setPersisted()} method.
    * <p>If the state of this object is DBSTATE_UNMODIFIED, <code>null</code>
    * is returned.
    *
    * @param doc  This document will be used to create the elements in the
    *    result xml fragment. Never <code>null</code>.
    *
    * @param root  The generated element will be appended to this node. Never
    *    <code>null</code>. If the component doesn't need updating, it won't
    *    be appended.
    *
    * @param keyGen  This object is used to create new keys when a component
    *    has a key that hasn't been assigned yet. Never <code>null</code>.
    *
    * @param parent  This param is used for managing foreign key relationships.
    *    If the component is a child and this is not supplied, an exception
    *    will be thrown.
    *
    * @throws PSCmsException If any of the components of the specified type
    *    have unassigned keys.
    */
   public void toDbXml(Document doc, Element root, IPSKeyGenerator keyGen,
         PSKey parent)
      throws PSCmsException;


   /**
    * Certain components may have associated descriptions.
    *
    * @return The text of the description, or the empty string if there is
    *    none. Never <code>null</code>
    */
   public String getDescription();


   /**
    * This method is called by the system after a component is successfully
    * modified, whether saving or removing. If the state was new or modified,
    * it must be set to unmodified. If it was deleted, it must be set to new.
    * <p>Must set the key to persisted or clear the key respectively. If
    * setting the key to persisted, if the key is not assigned, an
    * exception will be thrown. The key must be assigned before the
    * {@link #toDbXml(Document, Element, IPSKeyGenerator, PSKey) toDbXml} 
    * method is called.
    * <p>Generally, only called by system code.
    *
    * @throws PSCmsException If this instance's key does not have an assigned
    *    value.
    */
   public void setPersisted()
      throws PSCmsException;


   /**
    * If it is desired to remove a component from the database, call this
    * method, then save the component. This is equivalent to calling the
    * processor's delete method with this object's key.
    * <p>If the object has not been persisted yet, this is a no-op.
    */
   public void markForDeletion();


   /**
    * See {@link IPSCmsComponent#fromXml(Element) interface} for complete
    * details. This doc describes additional behaviors.
    * This method makes this object a duplicate of the object represented in
    * serialized form in the xml. More formally, if you have 2 different
    * components, A and B, and you call A.fromXml(B.toXml()). After this
    * operation completes, A.equalsFull(B) will return <code>true</code>.
    */
   public void fromXml(Element src)
      throws PSUnknownNodeTypeException;


   /**
    * Every component maintains a state indicating whether this instance of
    * the object has changed since it was instantiated from its database
    * representation. This state is always one of the DBSTATE_xxx values.
    * Typically, end users of the components do not need to concern themselves
    * with this value, it is managed by the implementation.
    * <p>Implementing classes are responsible for changing the state as the
    * object is modified (e.g., after re-serializing an object by passing
    * the element returned from the load method of the processor to the
    * <code>fromXml</code> method of this class, the state will be
    * DBSTATE_UNMODIFIED. As soon as an attribute is changed, the setDirty
    * method should be called.)
    *
    * @return One of the DBSTATE_xxx values.
    */
   public int getState();


   /**
    * See {@link #getState()} for a description. In general, this method
    * should not be called by client code.
    * <p>Sets the state to the one supplied, if it is allowed according to
    * the doc for {@link #setLocator(PSKey) setLocator}, otherwise an
    * exception is thrown.
    *
    * @param newState  One of the DBSTATE_xxx values.
    *
    * @throws IllegalStateException If setting the state of this object to
    *    the supplied one would result in an illegal combination between
    *    the state and key.
    */
   public void setState(int newState);


   /**
    * See the {@link IPSCmsComponent#clone()} for complete details. 
    * This interface differs as follows:
    * the clone behaves slightly differently for data components. It is still
    * a deep copy, but the clone may differ from the object it was cloned from
    * by the state and key. The values of the key in the clone will be cleared
    * and the state will be set to DBSTATE_NEW. Given an object A, it is
    * guaranteed that A.equals( A.clone()) will return <code>true</code>.
    */
   public Object clone();


   /**
    * See the {@link IPSCmsComponent#equals(Object) interface} for complete
    * details. This interface differs as follows: the key values and current
    * state (DBSTATE_xxx) are not considered in the comparison.
    */
   public boolean equals(Object obj);


   /**
    * See the {@link IPSCmsComponent#hashCode() interface} for complete
    * details. This interface differs as follows: the key values and current
    * state (DBSTATE_xxx) are not considered in the calculation. This is to
    * be consistent w/ the equals method.
    */
   public int hashCode();


   /**
    * Just like {@link #clone()}, except it considers the key values and state.
    */
   public Object cloneFull();


   /**
    * Just like {@link #equals(Object)}, except it considers the key values and
    * state.
    */
   public boolean equalsFull(Object obj);


   /**
    * Just like {@link #hashCode()}, except it considers the key values and
    * state.
    */
   public int hashCodeFull();
}
