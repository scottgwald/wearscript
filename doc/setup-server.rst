.. _server:

Server Setup
============

.. _our-server:

0a. Using Our Server
----------------
* Visit https://api.picar.us/wearscript/, then sign-in using your Google acccount
* Alternatively, we provide a server that tracks the 'dev' branch https://api.picar.us/wearscriptdev/

.. _your-server:

0b. Install Server
--------------
* Server code is located at https://github.com/wearscript/wearscript-server.git
* Playground webapp is located at https://github.com/wearscript/wearscript-playground.git (you need both)
* Linux is highly recommended, we have not tested this on OSX or Windows (feel free to try)
* A few "alternate" options are listed below that may be useful if you run into problems
* Tested on Ubuntu 13.04, if you want support it helps if you stick with this or a new Ubuntu if possible.
* Ubuntu packages: apt-get install golang git mercurial redis-server
* Setup the config.go file (look at config.go.example)
* Run /server/install.sh (this does basic dependencies and such)
* Start with ./server and continue with "Connecting the Client to the Server"

Alternate: Installing Go (manually)
------------------------
* wget https://go.googlecode.com/files/go1.1.1.linux-amd64.tar.gz
* tar -xzf go1.1.1.linux-amd64.tar.gz
* Put "export GOROOT=<yourpath>/go" and "export GOPATH=<yourpath>/gocode" in your .bashrc
* The "gocode" is where packages will be stored and "go" is the location of the extracted folder.

Alternate: Install Redis
------------------
* Follow instructions here http://redis.io/download (tested on 2.6.*)
