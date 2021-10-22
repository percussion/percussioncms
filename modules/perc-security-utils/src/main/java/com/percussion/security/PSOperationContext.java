package com.percussion.security;

/**
 * Use this enum as a parameter in validation methods to pass an operational context.
 *
 * For example, if legacy file name has a [ in it, we want to allow that for all but
 * the create operation. Search, Delete, and others should work.
 */
public enum PSOperationContext {
    SEARCH,
    CREATE,
    EDIT,
    DELETE,
    READ,
    COPY
}
