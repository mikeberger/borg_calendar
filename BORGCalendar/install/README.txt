Installers are now created using jpackage. IzPack is no longer used.

The program is built in target/installer. 

Installers need to be created manually by running either winpackage.bat on windows or linpackage.sh on linux.

A tar or zip can be created from the contents of target/installer for other OSes, like MAC.


** The installers bundle Java 15. A tar or ZIP does not. Java 15 or higher must be installed separately if using a tar or ZIP to install.
