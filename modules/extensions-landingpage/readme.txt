
Landing Page Solution README and Installation
________________________________________________

**THIS VERSION REQUIRES RHYTHMYX 6.7 WITH THE LATEST PATCHES** 

.. sectnum::
.. contents:: Table of Contents

Packages
========

 * soln.landingpage.ppkg
 * soln.segment.ppkg

CM System Installation
======================

Install the CM System Landing Page package and its dependency the Segmentation package.

Usage
=====

On your site folders that have navons you can specify 
a content type template pair through folder properties (right click on a folder and select properties):

============================== ===================================
Property Name                  Property Value
============================== ===================================
soln.landingpage.contenttype   your landing page content type.
soln.landingpage.template      the template for the landing page.
============================== ===================================

The folder properties can be overridden or inherited by descendant folders
so if you only have one landing page / template combination you can specify it on the your
Sites root folder.

**When you create a folder the landing page is not automatically created.** *You must create the landing page content item
in a desired folder that has a Navon and it will automatically be associated to the Navon in the same folder.*



