# API Change Log

This file documents changes that have been made to API's / public interfaces.

## Java API Changes

### IPSPasswordFilter
This interface has been updated in order to handle upgrade of security algorithms used in password hashing between versions. Developers that implement this interface will need to make the folloing changes post upgrade:

#### getAlgorithm()
The current algorithm used for encryption.  For example PBKDF2WithHmacSHA512 will be returned by the system default password filter. 

#### getLegacyAlgorithm()
The legacy algorithm that was used for password hashing in the prior version.   For example, SHA-1 will be returned by the system default password filter.

#### legacyEncrypt (String clearTextPassword)
This new method returns a hashed password string using the legacyAlgorithm.

Users of Backend Table authenticator (PSBackEndTableProvider) will be affected by this change. 

As password hashes are not intended to be reversible, the system performs the following checks on Login with this provider post-upgrade.

* Use PSPasswordHandler.checkHashedPassword to see if the password is valid.
* If the password is not valid, validate the password by encrypting it with the legacy algorithm provided by the filter and compare against the stored password.
* If the password is valid using the legacy algorithm, encrypt the password using the new algorithm, and update the stored password in the database with the encrypted string.
* If the password is not valid using the legacy algorithm, and a password filter is not configured, check if the clear text password matches what is stored in the database.
* If the clear text password matches, the password will be encrypted with the new algorithm, and the database updated with the encrypted password.
* A PSAuthenticationFailedException is thrown by the Backend Table provider if all attempts at authentication fail.

Post upgrade, the DefaultPasswordFilter will be used for the PSBackendTable provider by default.  This can be changed using the rxconfig/Server/config.xml file or the Server Admin tool. 

