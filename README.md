
Prudence
========

Prudence is a platform on which you can build scalable web frontends and network services.
Server-side code can be written in JavaScript, Python, Ruby, PHP, Lua, Groovy, or Clojure.
Though minimalistic, Prudence addresses real-world, practical needs, from URI rewriting and
virtual hosting to state-of-the-art server- and client-side caching.

Please see the main Prudence site for comprehensive documentation:

http://threecrickets.com/prudence/


Building Prudence
----------------- 

To *completely* build Prudence you need Ant, Maven and Sincerity:

http://ant.apache.org/

http://maven.apache.org/

http://threecrickets.com/sincerity/

You may need to create a file named "/build/private.properties" (see below) and override
the default locations for Maven and Sincerity.

The, simply change to the "/build/" directory and run "ant".

During the build process, build and distribution dependencies will be downloaded from an
online repository at http://repository.threecrickets.com/, so you will need Internet access.

The result of the build will go into the "/build/distribution/" directory. Temporary
files used during the build process will go into "/build/cache/", which you are free to
delete.

To avoid the "bootstrap class path not set" warning during compilation (harmless),
configure the "compile.boot" setting in "private.properties".

If you *only* want to build the Prudence Jar, then you only need Ant (you don't need Maven
and Sincerity). Run the "libraries" Ant task instead of the default one.


Configuring the Build
---------------------

The "/build/custom.properties" file contains configurable settings, along with
some commentary on what they are used for. You are free to edit that file, however
to avoid git conflicts, it would be better to create your own "/build/private.properties"
instead, in which you can override any of the settings. That file will be ignored by git.


Building the Prudence Manual
----------------------------

To build the manual, as part of the standard build process, you will need to install
LyX and eLyXer, and configure their paths in "private.properties":

http://www.lyx.org/

http://elyxer.nongnu.org/


Packaging
---------

You can create distribution packages (zip, deb, rpm, IzPack) using the appropriate
"package-" Ant targets. They will go into the "/build/distribution/" directory.

If you wish to sign the deb and rpm packages, you need to install the "dpkg-sig" and
"rpm" tools, and configure their paths and your keys in "private.properties". 

In order to build the platform installers (for Windows and OS X), you will need to
install InstallBuilder and configure its path in "private.properties":

http://installbuilder.bitrock.com/

BitRock has generously provided the Prudence project with a free license, available
under "/build/installbuilder/license.xml". It will automatically be used by the build
process.
