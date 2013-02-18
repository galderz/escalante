Requirements
------------
* Java 1.6
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

Building
--------

Install the project using the provided settings.xml:

    mvn -s build/settings.xml install

If you will be building the project often, you'll want to
create/modify your own ~/.m2/settings.xml file.

If you're a regular JBoss developer, see:

* http://community.jboss.org/wiki/MavenGettingStarted-Developers

Otherwise, see:

* http://community.jboss.org/wiki/MavenGettingStarted-Users

Once your repositories are configured, simply type:

    mvn install

Layout
------

* `assembly/` Contains the instructions for creating the final binary
distributable in .zip format.

* `build/` Contains code related to building Escalante, including downloading
and unzipping a base JBoss Application Server used for testing.

* `modules/core` Includes common code use by several Escalante components.

* `modules/artifact` Includes code to resolve dependencies and build modules.
It also contains an integration test subproject that's executed via Maven
Invoker plugin which runs Arquillian tests to check this module works as
expected.

* `modules/lift` Includes code for Lift scala web framework integration.
It also contains an integration test subproject that's executed via Maven
Invoker plugin which runs Arquillian tests to check this module works as
expected.

* `modules/` Parent project for all Escalante modules.

Development Tips
----------------

Before you attempt to do any testing, whether it's via command line Maven,
or any IDE, please make sure `build/` project is built, so that base
JBoss Application Server is downloaded and unzipped in designated location.
If you want to avoid building the entire project, you can simply build the
individual module via:

    mvn install -pl build

Once the base JBoss Application Server has been installed, simply run your
tests via command line or the IDE.