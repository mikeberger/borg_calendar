The BORG palm conduits can only be built and run under Java 1.3 due to limitations of the 
abandoned Palm JSync project. When Palm Hotsync invokes a Jsync conduit, use of a Java 1.3 JVM is 
forced by the windows DLL. This cannot be altered.

Therefore the conduits cannot run using the BORG 1.7 source, which requires Java 1.5+.

The BORG code in the borg_src directory is a Java 1.3 compatible hacking of the BORG 
1.6.1 source. It contains the minimum logic needed for the conduits, running under Java 1.3,
to communicate with BORG 1.7 running Java 1.5 or Java 6. This communication is via
XML over sockets.

There is no way to avoid having to maintain a separate source tree for BORG 1.7 and the borg_src folder 
in this project.