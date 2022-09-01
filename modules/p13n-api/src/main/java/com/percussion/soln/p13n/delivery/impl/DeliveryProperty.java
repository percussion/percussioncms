package com.percussion.soln.p13n.delivery.impl;

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.value.ValueFactoryImpl;

import com.percussion.soln.p13n.delivery.IDeliveryProperty;

public class DeliveryProperty implements IDeliveryProperty {

    private Value data;
    private String name;
    private static ValueFactory valueFactory = ValueFactoryImpl.getInstance();
    
    public DeliveryProperty(String name, String data) {
        super();
        this.name = name;
        this.data = valueFactory.createValue(data);
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        return getValue().getBoolean();
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        return getValue().getDate();
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        return getValue().getDouble();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        return getValue().getLong();
    }

    public String getName() throws RepositoryException {
        return name;
    }

    public String getString() throws ValueFormatException, RepositoryException {
        return getValue().getString();
    }

    public int getType() throws RepositoryException {
        if (! isMultiple() ) return getValue().getType();
        return getValues()[0].getType();
    }

    public Value getValue() throws ValueFormatException, RepositoryException {
        if (isMultiple()) throw new ValueFormatException("Cannot get single value for multi value.");
        return data;
    }

    public Value[] getValues() throws ValueFormatException, RepositoryException {
        if ( ! isMultiple() ) throw new ValueFormatException("Not Multi value.");
        throw new UnsupportedOperationException("Multi Values is not supported right now");
    }

    public boolean isMultiple() throws RepositoryException {
        //Multiple is not supported right now.
        return false;
    }

}
