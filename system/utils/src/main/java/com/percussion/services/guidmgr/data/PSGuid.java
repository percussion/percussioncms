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
package com.percussion.services.guidmgr.data;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class encapsulates guids and converts them to and from user readable and
 * machine formats. Guids are represented internally as 64 bit long values
 * that contain several fields.
 * <p>
 * GUIDs are intended as globally unique ids. They are kept global by
 * incorporating a host id, which varies per installation, and a unique id,
 * which is simply an allocated number that is managed as part of the
 * installation. The type allows components that receive a GUID to determine if
 * the GUID is valid and what the GUID references. For example, the content
 * manager can use the type and other information to decide what repository
 * contains the item referenced by the GUID.
 * <p>
 * The data in the 64 bit value is laid out as described in this table: <table>
 * <tr>
 * <th>Bits</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>0-31</td>
 * <td>UUID</td>
 * </tr>
 * <tr>
 * <td>32-39</td>
 * <td>Type ID</td>
 * </tr>
 * <tr>
 * <td>40-63</td>
 * <td>Host ID</td>
 * </tr>
 * </table>
 * <p>
 * Set methods are included here for XML serialization. Guid values should not
 * be modified after creation. Calling the set methods if the given field is not
 * zeroed will results in an exception.
 * <p>
 * Although this object is derived from the abstract class {@link Number} and
 * implements the methods, only {@link #longValue()} should be used for 
 * operations that rely on the full data contained. The other methods may 
 * only return part of the original value, especially on true GUID values that
 * use all 64 bits of the long.
 * 
 * @author dougrand
 */
public class PSGuid extends Number implements IPSGuid
{
   /**
    * 
    */
   private static final long serialVersionUID = 3134898757562243271L;

   /**
    * Guid value
    */
   protected long m_guid;

   static final long BIT8 = 0xFFL;

   static final long BIT24 = 0xFFFFFFL;

   static final long BIT32 = 0xFFFFFFFFL;

   static final short HOST_POS = 40;

   static final short TYPE_POS = 32;

   private static final String TYPE_NOT_NULL = "type may not be null";
   /**
    * General constructor for a guid.
    * 
    * @param hostid the host id, defined from the system configuration and
    *    issued by tech support. Host ids below 1000 are reserved for internal 
    *    use.
    * @param type the type of the GUID being created, not <code>null</code>.
    * @param uuid the unique id of the object. Unique ids may be repeated 
    *    across different types.
    */
   public PSGuid(long hostid, PSTypeEnum type, long uuid)
   {
      assemble(hostid, type, uuid);
   }

   /**
    * Constructor for a guid from string form without a type.
    * 
    * @param type a type value, not <code>null</code>.
    * @param representation the string, must be in the following format, where
    * parts in [] are optional:
    * <pre>
    *    [hostid-[typeid-]]uuid
    * </pre>.
    */
   public PSGuid(PSTypeEnum type, String representation)
   {
      assemble(representation, type);
   }

   /**
    * Public ctor, use in very specific cases for serialization, i.e. fromXML
    * method and such. Not for general use.
    */
   public PSGuid()
   {
      this(PSTypeEnum.INTERNAL, 0);
   }

   /**
    * Allows creation of a guid from a numeric representation. The type must be
    * a type present in the {@link PSTypeEnum} class. The hostid is not
    * validated.
    * 
    * @param guid The value returned from the {@link #longValue()} method. If
    * the type is not found in the enumeration class, an
    * <code>IllegalArgumentException</code> is thrown
    */
   public PSGuid(long guid)
   {
      long uuid = doGetUUID(guid);
      long host = doGetHostId(guid);
      short typeVal = doGetType(guid);
      
      PSTypeEnum type = PSTypeEnum.valueOf(typeVal);
      if (type == null)
      {
         throw new IllegalArgumentException(
               "guid does not contain a valid type");
      }
      assemble(host, type, uuid);
   }
   
   /**
    * Constructor for a guid from string form.
    * 
    * @param representation the string, must be in the same format that
    *    {@link #toString()}uses to format a GUID.
    */
   public PSGuid(String representation)
   {
      assemble(representation, null);
   }

   /**
    * Constructor for a guid from a direct value, e.g. the database.
    * 
    * @param type the type of the guid to be constructed, never
    *    <code>null</code> and must match the type if the type is present in 
    *    the value passed in.
    * @param value the guid value.
    */
   public PSGuid(PSTypeEnum type, long value)
   {
      if (type == null)
         throw new IllegalArgumentException(TYPE_NOT_NULL);

      m_guid = value;
      if (getType() == 0)
         setType(type.getOrdinal());
      else if (getType() != type.getOrdinal())
         throw new IllegalArgumentException("Type does not match");
   }

   /**
    * Assemble a GUID from the component information.
    * 
    * @param hostid the host id, defined from the system configuration and
    *    issued by tech support. Host ids below 1000 are reserved for internal 
    *    use.
    * @param type the type of the GUID being created, not <code>null</code>.
    * @param uuid the unique id of the object. Unique ids may be repeated 
    *    across different types. Only the lower 32 bits are used.
    */
   protected void assemble(long hostid, PSTypeEnum type, long uuid)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");
      
      long hostval = hostid << HOST_POS;
      long typeval = ((long) type.getOrdinal()) << TYPE_POS;
      long idval = uuid & BIT32;
      m_guid = hostval + typeval + idval;
   }

   /**
    * Assemble a GUID from a string representation. Uses
    * {@link #assemble(long, PSTypeEnum, long)} to actually do the work after
    * tokenizing the string. There may be one, two or three components. If there
    * is one component, then the conversion to a guid must be consistent, either
    * with a simple number or with a consistent type in the type passed as
    * compared with the "middle" number in the string representation. The passed
    * string may also be a 64 bit number, in which case the type must match the
    * type that is specified in the number.
    * 
    * @param guid the string, may have one, two or three components, separated
    *    by hyphens, never <code>null</code> or empty.
    * @param type the type of the GUID being created, may be <code>null</code>,
    *    in which case there must be three components.
    */
   protected void assemble(String guid, PSTypeEnum type)
   {
      if (StringUtils.isBlank(guid))
         throw new IllegalArgumentException("guid may not be null or empty");

      String[] tokens;
      if (guid.startsWith("-") && guid.lastIndexOf("-") == 0)
      {
         // one negative component
         tokens = new String[]{guid};
      }
      else
      {
         tokens = StringUtils.split(guid, "-");
      }
         
      if (tokens.length > 3)
         throw new IllegalArgumentException(
            "guid string must have no more than three components");

      switch (tokens.length)
      {
         case 1:
         {
            m_guid = Long.parseLong(tokens[0]);
            if (getType() == 0)
            {
               if (type == null)
               {
                  throw new IllegalArgumentException(
                        "Type is undetermined, expecting \"type\" argument");
               }
               setType(type.getOrdinal());
            }
            else if ((type != null) && (getType() != type.getOrdinal()))
            {
               throw new IllegalArgumentException("Type does not match");
            }
         }
         break;

         case 2:
         {
            if (type == null)
               throw new IllegalArgumentException(
                  "Type is required for two component string representations");

            long hostid = Long.parseLong(tokens[0]);
            long uuid = Long.parseLong(tokens[1]);
            assemble(hostid, type, uuid);
         }
         break;

         default:
         {
            long hostid = Long.parseLong(tokens[0]);
            long typeid = Long.parseLong(tokens[1]);
            long uuid = Long.parseLong(tokens[2]);
            if ((type != null) && (typeid != type.getOrdinal()))
               throw new IllegalArgumentException("Type did not match");

            if (type == null)
               type = PSTypeEnum.valueOf((int) typeid);

            assemble(hostid, type, uuid);
         }
      }
   }

   /**
    * Convert a numeric guid into a user readable form
    * 
    * @return a formatted string
    */
   @Override
   public String toString()
   {
      long hostid = getHostId();
      int type = getType();
      long uuid = getUUID();

      return hostid + "-" + type + "-" + uuid;

   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.guid.IPSGuid#toStringUntyped()
    */
   public String toStringUntyped()
   {
      long hostid = getHostId();
      long uuid = getUUID();

      return hostid + "-" + uuid;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.guid.IPSGuid#getHostId()
    */
   public long getHostId()
   {
      return doGetHostId(m_guid);
   }
   
   /**
    * Set a new host id value. The current host id must be empty.
    * 
    * @param hostid a new host id value, must fit in 24 bits and be a positive
    *    number.
    */
   public void setHostId(long hostid)
   {
      if (hostid > BIT24 || hostid < 0)
         throw new IllegalArgumentException(
            "hostid must be a non-negative number that fits in 24 bits");

      if (getHostId() != 0)
         throw new IllegalStateException("The hostid must not already be set");

      m_guid += hostid << HOST_POS;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.guid.IPSGuid#getType()
    */
   public short getType()
   {
      return doGetType(m_guid);
   }

   /**
    * Set a new type value. The current type must be empty.
    * 
    * @param type a new type value, must fit in 8 bits and be a positive number.
    */
   public void setType(short type)
   {
      if (type > BIT8 || type < 0)
         throw new IllegalArgumentException(
            "Type must be a non-negative number that fits in 8 bits");

      if (getType() != 0)
         throw new IllegalStateException("The type must not already be set");

      m_guid += ((long) type) << TYPE_POS;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.guid.IPSGuid#getUUID()
    */
   public int getUUID()
   {
      return doGetUUID(m_guid);
   }

   /**
    * Set a new uuid value. The uuid must be unset when this method is called.
    * 
    * @param uuid the new uuid value, must fit in 32 bits and be a positive
    *    number.
    */
   public void setUUID(int uuid)
   {
      if (uuid < 0)
         throw new IllegalArgumentException(
            "UUID must be a non-negative number");

      if (getUUID() != 0)
         throw new IllegalStateException("The UUID must not already be set");

      m_guid += uuid;
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.utils.guid.IPSGuid#longValue()
    */
   @Override
   public final long longValue()
   {
      if (getHostId() == 0)
         return getUUID();
      else
         return m_guid;
   }
   
   /** (non-Javadoc)
    * @see java.lang.Number#intValue()
    */
   @Override
   public int intValue()
   {
      return (int) longValue();
   }

   
   /** (non-Javadoc)
    * @see java.lang.Number#floatValue()
    */
   @Override
   public float floatValue()
   {
      return longValue();
   }

   /** (non-Javadoc)
    * @see java.lang.Number#doubleValue()
    */
   @Override
   public double doubleValue()
   {
      return longValue();
   }   

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSGuid))
         return false;

      PSGuid b = (PSGuid) obj;

      return m_guid == b.m_guid;
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_guid).toHashCode();
   }
   
   /**
    * Extracts the bits that correspond to the host id and shifts them to the
    * low bit position.
    * 
    * @param guid A fully built guid.
    * 
    * @return The extracted value.
    */
   private long doGetHostId(long guid)
   {
      return BIT24 & (guid >> HOST_POS);
   }
   
   /**
    * Extracts the bits that correspond to the object id and shifts them to the
    * low bit position.
    * 
    * @param guid A fully built guid.
    * 
    * @return The extracted value.
    */
   private int doGetUUID(long guid)
   {
      return (int) (BIT32 & guid);
   }
   
   /**
    * Extracts the bits that correspond to the type id and shifts them to the
    * low bit position.
    * 
    * @param guid A fully built guid.
    * 
    * @return The extracted value.
    */
   private static short doGetType(long guid)
   {
      long t = BIT8 & (guid >> TYPE_POS);
      return (short) t;
   }
   
   /**
    * Returns if the corresponding bits in the passed value do indeed correspond
    * to a type. If those bits have 0 value, it is also valid 
    * @param type the type of the guid to be checked, never <code>null</code>
    * @param value the guid value.
    */
   public static boolean isValid(PSTypeEnum type, long value)
   {
      if (type == null)
         throw new IllegalArgumentException(TYPE_NOT_NULL);
    
      short t = doGetType(value);

      return t == 0 || t == type.getOrdinal();
   }
   
   
   /**
    * Returns if the corresponding bits in the passed value do indeed correspond
    * to a type. If those bits have 0 value, it is also valid 
    * @param type the type of the guid to be checked, never <code>null</code>
    * @param value the guid value as a string
    */
   public static boolean isValid(PSTypeEnum type, String value)
   {
      if (type == null)
         throw new IllegalArgumentException(TYPE_NOT_NULL);
      if ( StringUtils.isBlank(value))
         throw new IllegalArgumentException("value may not be null");
      long val = Long.parseLong(value);
      return isValid(type, val);
   }

}
