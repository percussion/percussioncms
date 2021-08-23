
# Change Log

This file documents changes that have been made to API's / public interfaces.

## Design / Development Changes
### Test / Debug Tools
The Test / Debug tools have been disabled by default, and will all require user membership in the Admin role when enabled for all tool scripts. 
To re-enable Test / Debug tools, change the enableDebugTools=false property in the rxconfig/Server/server.properties file to true and restart the CMS instance. 

The tools are accessible from the /test url.

This was a security change.

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

## Obsolete Server Properties

The following properties have been replaced by the enableDebugTools property:

* enableLogTool
* enableSQLTool
* enableJCRTool

The optimizePublishWithMD5Check property has been replaced by the optimizePublishWithChecksum property as MD5 has been deprecated and has been replaced by SHA-256.

##  XML Catalog

Percussion CMS's origin was as an XML Application server, as such it has a long history with XML.  Current OWASP security best practices recommend restrictions on Entity, DTD, and namespace processing to help block a class of XXE vulnerabilities.  In order to comply with these best practices, starting with the 8.0.3 release of Percussion CMS, the system will only allow external entity / dtd references that are pre-registered in either the <InstallDir>/PercussionXMLCatalog.xml file or in the <InstallDir>/var/config/CustomXMLCatalog.xml file. 
 
User's with custom XML applications, XSL style sheets, DTD, or entity references will need to register those uri's in the <InstallDir>/var/config/CustomXMLCatalog.xml file.  Please review the <InstallDir>/PercussionXMLCatalog.xml file for examples.  XML Applications are typically handled with a **rewriteURI** element:
 
 ```
    <rewriteURI
            uriStartString="file:myxmlapp/"
            rewritePrefix="file://(/ on Windows)<InstallDir>/myxmlapp/" />
```
 
### Tips
 * Backslashes are not allowed in these url's, forward slashes only.  
 * Windows requires 3 slashes before the driver letter e.g. file:///C:/.
 
Developer's adding new XML applications to the main distribution will want to submit a Pull Request for the PercussionXMLCatalog.xml.  We should be providing a local resource for all Entity/DTD/XSD references.  These should be added to the system/DTD folder.  For Unit Tests, please also add the dtd/ent/xsd to the modules/perc-xml-security/src/test/resources/DTD folder and update the TestingXMLCatalog.xml with the relative uri. 
 
The XML Catalog follows the OASIS 1.0 XML Catalog standard.  
  
## The **var** directory

As part of our containerization efforts, we are pushing new configuration to the <InstallDir>/var folder. When running via a container, this will be a mount point to permanent storage.    
