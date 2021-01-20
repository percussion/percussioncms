
SOURCE_EXT_TESTING = com$(DIRSEP)percussion$(DIRSEP)extensions$(DIRSEP)testing

PACKAGEDIRS = $(SOURCE_EXT_TESTING) 

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

PACKAGEFILES    = $(SOURCE_EXT_TESTING:+"$(DIRSEP)*.java") 

.IF $(OS) == unix
   DIRSEP= /
.ELSE
   DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)..$(DIRSEP)builtins.mak

# Overrides of builtins.mak settings
DOCTITLE  = "E2 1.0 Testing API"
