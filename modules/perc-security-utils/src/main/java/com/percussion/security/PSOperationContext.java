package com.percussion.security;

/**
 * Use this enum as a parameter in validation methods to pass an operational context.
 *
 * For example, if legacy file name has a [ in it, we want to allow that for all but
 * the create operation. Search, Delete, and others should work.
 */
public enum PSOperationContext {
    /**
     * This indicates that the call is part of a Search operation.
     */
    SEARCH,
    /**
     * Create indicates that the call is part of a Create operation like for a new item
     */
    CREATE,
    /**
     * Edit indicates an update or edit operation for an existing item
     */
    EDIT,
    /**
     * Delete indicates that the call is part of a Delete operation.
     */
    DELETE,
    /**
     * Read indicates that the call is part of a Read operation.
     */
    READ,
    /**
     * Copy indicates that the call is part of a Copy oepration.
     */
    COPY,
    /**
     * Authentication indicates that the call is part of an authentication operation.
     */
    AUTHENTICATION,
    /**
     * Use this as a general default when the operation is generic and the validation applies to
     * any operation.
     */
    ANY
}
