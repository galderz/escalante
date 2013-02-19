Requirements
------------
* Java 1.6
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

Building
--------

Install the project using the provided settings.xml:

    mvn -s settings.xml install

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

* `assembly/` Contains the instructions for creating the final server
distribution structure.

* `dist/` Takes the distribution created by `assembly` and creates a .zip
file and Maven artifact that it can be published. You must use execute the
`release` profile for the ZIP to be generated.

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
