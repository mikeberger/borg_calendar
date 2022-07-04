# BORG Calendar

## Intro
BORG is a combination calendar and task tracking system. The calendar supports all sorts of appointments and also a simple todo list. The calendar functionality is similar to that of other PIMs, such as Microsoft Outlook, Mozilla Calendar, Palm Desktop, Yahoo Calendar, etc... The Task Tracker goes way beyond todo's and might better be called a project tracking system.
It is a standalone Desktop application.
## Why BORG?

A potential BORG user might agree with the following: 

* I use a calendar/task tracker to remind me to do everything in life that I have to do.
* I do not want to access the internet to see my calendar
* Some of my data is private. I don't trust putting it on an internet server.
* I work on Linux, Windows and other OS's and would like any of my machines to run my calendar program
* I like open source software. Even if the developer(s) die, I can still build and enhance my calendar program.

## Features
 
* Month/Week/Day/Year views
* To-Do list
* Repeating Appointments
* Private Appointments that do not show by default (for recording things like "rectal exam 2pm" that you don't need your friends/coworkers to see over your shoulder).
* Pop-up reminders
* Email reminders
* A Project/Task/Subtask Tracking System
* A simple address book
* XML import/export
* Memo book with a strong encryption option (for very private memos, passwords, etc..)
* Checklists
* more ...

## How to Get BORG
 
BORG downloads and releases are available on Github: https://github.com/mikeberger/borg_calendar/releases

## How to build BORG

* Some knowledge of Java and Apache Maven is assumed.
* Checkout the project from Github
* Have a JDK installed of version 17 or higher. openjdk-17 is currently being used.
* Import the project into any IDE as a Maven project (optional)
* Build using Maven from the IDE or by installing Apache Maven and running mvn from the command line (use clean and install targets)
* To build an executable installer with bundled Java, edit and run the appropriate script in the install folder - winpackage.bat for windows or linpackage.sh for linux
* The installer is built using the jpackage utility that is part of openjdk. An additional product called WiX Toolset needs to be installed for jpackage to build an msi installer on windows.
