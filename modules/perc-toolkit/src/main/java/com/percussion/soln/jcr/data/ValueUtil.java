package com.percussion.soln.jcr.data;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.value.ValueFactoryImpl;



public class ValueUtil {
    public static ValueFactory valueFactory = ValueFactoryImpl.getInstance();
    
    public static Value dataToJCRValue(ValueData data) {
        if (data == null) throw new IllegalArgumentException("Data cannot be null");
        switch(data.getType()) {
        case PropertyType.STRING:
            return valueFactory.createValue(data.getStringData());
        case PropertyType.BOOLEAN:
            return valueFactory.createValue(data.getBooleanData());
        case PropertyType.DATE:
            return valueFactory.createValue(data.getDateData());
        case PropertyType.DOUBLE:
            return valueFactory.createValue(data.getDoubleData());
        case PropertyType.LONG:
            return valueFactory.createValue(data.getLongData());
        default:
            throw new IllegalArgumentException("ValueData has a bad type.");
        }
    }
    
    public static Value[] dataToJCRValues(List<ValueData> data) {
        if (data == null && data.isEmpty()) throw new IllegalArgumentException("Data cannot be null or empty");
        Value[] values = new Value[data.size()];
        Iterator<ValueData> it = data.iterator();
        int i = 0;
        while(it.hasNext()) {
            values[i] = dataToJCRValue(it.next());
            i++;
        }
        return values;
    }
    
    public static ValueData jcrValueToData(Value data) {
        if (data == null) throw new IllegalArgumentException("Data cannot be null");
        try {
            switch (data.getType()) {
            case PropertyType.STRING:
                return new ValueData(data.getString());
            case PropertyType.BOOLEAN:
                return new ValueData(data.getBoolean());
            case PropertyType.DATE:
                return new ValueData(data.getDate());
            case PropertyType.DOUBLE:
                return new ValueData(data.getDouble());
            case PropertyType.LONG:
                return new ValueData(data.getLong());
            default:
                throw new IllegalArgumentException("ValueData has a bad type.");
            }

        } catch (ValueFormatException e) {
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public static ValueData createValueData(Object obj) {
        
        if (obj == null) {
            throw new IllegalArgumentException("Cannot be null");
        }
        else if(obj instanceof Boolean) {
            return new ValueData((Boolean)obj);
        }
        else if (obj instanceof String) {
            return new ValueData((String)obj);
        }
        else if (obj instanceof Long || obj instanceof Integer) {
            Long newValue = ((Number)obj).longValue();
            return new ValueData(newValue);
        }
        else if (obj instanceof Number) {
            return new ValueData(((Number) obj).doubleValue());
        }
        else if (obj instanceof Calendar) {
            return new ValueData((Calendar) obj);
        }
        else {
            throw new IllegalArgumentException("Obj: " + obj + " is not supported");
        }
    }

}
