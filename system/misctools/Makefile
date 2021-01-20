#
# Makefile for the tools component of E2. It specifies all the packages.
#

JARFILENAME = rxmisctools.jar
VERSIONFILENAME = 

PROPERTY_FILES	=  \
	com$(DIRSEP)percussion$(DIRSEP)tools$(DIRSEP)*.properties

IMAGE_FILES = \
	com$(DIRSEP)percussion$(DIRSEP)tools$(DIRSEP)images$(DIRSEP)*.gif

SOURCE_TOOLS = com$(DIRSEP)percussion$(DIRSEP)tools
SOURCE_HTTPCLIENT = HTTPClient

PACKAGEDIRS = $(SOURCE_TOOLS) \
              $(SOURCE_HTTPCLIENT)

IMAGEDIR = $(SOURCE_TOOLS)$(DIRSEP)images

PACKAGES = $(PACKAGEDIRS:s~$(DIRSEP)~.~)

IMAGES = $(IMAGEDIR:s~$(DIRSEP)~.~)

PACKAGEFILES    = $(SOURCE_TOOLS:+"$(DIRSEP)*.java") \
                  $(SOURCE_HTTPCLIENT:+"$(DIRSEP)*.java")


.IF $(OS) == unix
   DIRSEP= /
.ELSE
   DIRSEP= \ 
.END

.INCLUDE : ..$(DIRSEP)builtins.mak

# Overrides of builtins.mak settings
DOCTITLE  = "E2 tools 1.0 Internal API"
