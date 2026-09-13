Installers are now created using jpackage. IzPack is no longer used.

Installers need to be created manually by running either winpackage.bat on windows or linpackage.sh on linux.

A tar or zip can be created from the contents of target/installer for other OSes, like MAC.

** The installers bundle Java. A tar or ZIP does not.

-------------
Notes on Wix:

9/2026 - updated to wix v7 - a different beast from wix v3. v3 is not supported in Java 25.

Magic incantations needed to set up so that Java 25 jpackage calls Wix successfully when building an msi installer:
dotnet tool install --global wix
wix eula accept wix7
wix extension add --global WixToolset.Util.wixext/7.0.0
wix extension add --global WixToolset.UI.wixext/7.0.0

