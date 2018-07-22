set "JAVA_HOME=%1"
%JAVA_HOME%\bin\jlink --module-path %JAVA_HOME%\jmods --add-modules java.desktop,java.prefs,java.sql,java.xml,java.logging --output target/jre