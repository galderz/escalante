Test resource files are located under src/main/resources on purpouse, so that
they're not exported to projects depending on this project. This avoids issues
with arquillian configuration and multiple arquillian listener instantiation.
