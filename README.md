wtw-man
=======

Whatever The Weather (WTW) - Weather report request manager service sub system components.

##What is it
A multi-module maven project that provides a Weather report request manager service within the WTW SOA application system. 

##Component modules
1. wtw-man-service: Weather report request manager service API. 
2. wtw-man-client: JAXRS service proxy for client side access to Weather report request manager service API. 
3. wtw-man-rs: JAXRS Weather report request manager service access.
4. wtw-man-director: Weather report request manager service API implementation.

##Architectural principles
1. Modularity.
2. Encapsulation, separation of concerns, Loose coupling.
3. Flexible, Extensible.
4. Distributed processing
5. Asynchronous processing
6. Variety of front ends, web, mobile, desktop
7. SOA, Web Services
8. Cloud deployment.
9. Continuous Integration, build, test, (unit, integration, UAT, performance, smoke test,) deploy.
10. Source code management with GIT, branches and master development.
11. Architect for OSGi.

## What does it look like?
wtw will be deployed on linode with UI currently prototyping on www.jimomulloy.co.uk:4000