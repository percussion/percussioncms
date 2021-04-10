package com.percussion.soln.jcr;

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

public abstract class AbstractSimplyProperty {

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


    public String getString() throws ValueFormatException, RepositoryException {
        return getValue().getString();
    }

    public int getType() throws RepositoryException {
        return getValue().getType();
    }

    public abstract Value getValue() throws ValueFormatException, RepositoryException;

    public abstract Value[] getValues() throws ValueFormatException, RepositoryException;

}
