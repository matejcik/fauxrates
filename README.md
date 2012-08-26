Fauxrates refitted for Play framework
=====================================

The build is real simple now:

1. install Play framework http://www.playframework.org/
2. clone the git, navigate into fauxrates directory
3. run "play eclipsify", "play idea" or "play netbeans"
   according to your IDE preferences - or just edit it
   in vim
4. "play run" to start up the app with auto-reloader
5. navigate to http://localhost:9000/

It includes the database schema too! (in h2 database engine
for now, i'll get to setting up postgres later)
