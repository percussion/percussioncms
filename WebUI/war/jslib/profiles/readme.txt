The profiles are broken out loosely on jquery version.  This structure is temporary, intended to get us through migration to
jquery 3 from 1.4/1.7, once that migration is complete we should look at putting in place a real dependency
management system / bundler like webpack, npm, and yarn.

Profiles
-----
profiles/1x = jquery 1.x
profiles/2x = jquery 2.x
profiles/3x = jquery 3.x

package.json
------------
Each profile has a package.json file that is currently only used to track dependencies.  The intent is that
this can be used in the future.  Please update this file when adding new dependencies.

Layout
----------
jquery: Contains jquery
jquery->plugins: contains a set of folders with each folder containing a jquery plugin
jquery->libraries: contains a set of jquery reliant libraries that are not plugins
libraries: contains a set of javascript libraries that do not depend on jquery

Css / Image files
----------------
Allot of plugins include CSS and images.  The CSS files generally reference those images on relative paths.
For this reason any CSS/images needed by plugins/libraries are added here and not into the seperate WEBUI/css folder.

Plugins with no NPM package - No Active Maintainer
---------------------------
There are some plugins in use that have no NPM packages. These plugins are typically not maintained.
Currently these are added to teh package.json file in a mythical perc-retiredjs package.
they live under jquery/plugins/perc-retired folder. Our goal should be to replace these
with maintained alternatives as soon as possible.
