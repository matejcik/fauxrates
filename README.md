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

It includes the database schema too! It's in PostgreSQL.
Playframework will take care of installing the schema into
database, but you have to do the boring stuff such as
install the db server and modify app parameters
so that it connects. It's in conf/application.conf.

Or better, set up postgres so that you don't need to modify
the parameters. (Hint: to connect without user/pw filled-in,
grant database privileges to the user under which you run
the app, and modify postgres's pg_hba.conf to authenticate
users from localhost by "trust" instead of "ident".

This used to run on h2 in-memory database, but unfortunately
postgres's SQL dialect is too different from all the toy databases
and you'd have to rewrite parts of the evolution script to
make it work again. (see git revisions on how, if you really want to)
