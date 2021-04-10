package com.percussion.soln.jcr.data;

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

public class ValueData implements Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = -1441585584706585871L;
    
    Boolean booleanData;
    Calendar dateData;
    Double doubleData;
    Long longData;
    String stringData;
    /**
     * JCR property type.
     */
    int type;
    
    public ValueData() {
        // For general serializers that use the setters and getters.
        type = PropertyType.STRING;
        stringData = "";
    }
    
    public ValueData(boolean data) {
        booleanData = data;
        type = PropertyType.BOOLEAN;
    }

    public ValueData(String data) {
        stringData = data;
        type = PropertyType.STRING;
    }

    public ValueData(Calendar data) {
        dateData = data;
        type = PropertyType.DATE;
    }

    public ValueData(double data) {
        doubleData = data;
        type = PropertyType.DOUBLE;
    }

    public ValueData(long data) {
        longData = data;
        type = PropertyType.LONG;
    }
    
 
    
    public Boolean getBooleanData() {
        return booleanData;
    }
    
    public void setBooleanData(boolean booleanData) {
        this.booleanData = booleanData;
    }
    
    
    public Calendar getDateData() {
        return dateData;
    }
    
    public void setDateData(Calendar dateData) {
        this.dateData = dateData;
    }
    
    public Double getDoubleData() {
        return doubleData;
    }
    
    public void setDoubleData(double doubleData) {
        this.doubleData = doubleData;
    }
    
    public Long getLongData() {
        return longData;
    }
    
    public void setLongData(long longData) {
        this.longData = longData;
    }
    
    public String getStringData() {
        return stringData;
    }
    
    public void setStringData(String stringData) {
        this.stringData = stringData;
    }
    
    public int getType() {
        return type;
    }
    
    /**
     * Sets the type for this property.  See javax.jcr.PropertyType 
     * for a list of types.  Only STRING, DATE, LONG, DOUBLE, and BINARY are supported 
     * at this time. 
     * @param datatype The type to set. 
     * @throws RepositoryException 
     */
    public void setType(int datatype) throws RepositoryException
    {
       switch(datatype)
       {
          case PropertyType.STRING:
          case PropertyType.DATE:
          case PropertyType.LONG: 
          case PropertyType.DOUBLE:
          case PropertyType.BINARY:
          case PropertyType.BOOLEAN:
             this.type = datatype;  
             //log.debug("Setting type " + datatype); 
             break;
          default:
             throw new ValueFormatException("Unsupported Type " + datatype); 
       }
       
    }
    
    

}
