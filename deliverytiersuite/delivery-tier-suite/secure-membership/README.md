# secure-membership
This modules contains all the backend support required by DTS for 
Secure published pages which needs Authentication. It Provides AuthenticationProviders that can be LDAP or Internal DB Users.

If a page is set to be secure, then this service takes care of Authentication/Autherization before DTS presents the page to user
using LDAP Authentication or internal user Authentication.
## Building

```
mvn clean install
```