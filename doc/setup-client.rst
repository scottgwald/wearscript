.. _client-setup:

Client Setup (source)
=====================


0: Get WearScript Source
------------------------
#. with one click: `Click to Download ZIP <https://github.com/wearscript/wearscript-android/archive/master.zip>`_
#. using git: git clone https://github.com/wearscript/wearscript-android.git

1: Setup Your Device
--------------------
* Put the device in debug mode (see "Turning on debug mode" here https://developers.google.com/glass/gdk)
* Connect your Glass to your computer with a USB cable

.. _connecting-client-to-server:

2: Install Client
-------------------
.. _client-setup-source:

2: Get Android Studio and Install Client
----------------------------------------
First we install/setup Android Studio

* Download/unpack the canary version of Android Studio with Android SDK bundle http://tools.android.com/download/studio/canary/0-3-2 note that this is the latest version WITH the sdk bundled, you can update once it is installed but this simplifies the install
* Locate the "sdk/platform-tools" and "sdk/tools" directories and add them to your system path (this lets you use the "adb and android" commands)
* (OSX): echo "export PATH=$PATH:/Applications/Android\ Studio.app/sdk/platform-tools/:/Applications/Android\ Studio.app/sdk/tools/" >> ~/.profile
* (Windows) from the command prompt(Without quotes) "set PATH=%PATH%;C:\YourFolderPath\whereadbIsLocated\sdk\platform-tools\"
* NOTE: If it can't find the SDK then follow these steps

  * If you need a new version of the SDK get the ADT Bundle here http://developer.android.com/sdk/index.html
  * Open Android Studio, click on Configure->Project Defaults->Project Structure and under Project SDK click New...->Android SDK and select the "sdk" folder inside of the ADT Bundle
  * If you have changed your SDK path you may need to remove local.properties (it retains the path to use for the project, it'll be reset to default on import)


Now we build/install the client

* The gdk.jar is already included for you in the libraries folder.  The steps we ran to get it are:  run the "android" command, under Android 4.0.3 (API 15) install Glass Developer Sneak Peek to get the extra library (see https://developers.google.com/glass/develop/gdk/quick-start) and then copy the gdk.jar into the WearScript/libs folder.
* In the WearScript source go to thirdparty and run the command "bash install.sh"
* Start Android Studio (Linux: bash android-studio/bin/studio.sh)
* Click "Import Project" and select wearscript/glass  (NOTE: make sure you use the "wearscript/glass" directory, if you select "wearscript" it won't work)
* Select "Import project from external model" and use Gradle
* Select "Use default gradle wrapper" (it is the default), after this it will say Building 'glass' and take some time
* Build using Run->Run 'WearScript'
* After it is built, Select your device and install.
* Keep the screen on while it is installing or it will be started in the background.


3: Configure Device for Server
------------------------------
* WearScript allows you to control Glass via a WebApp, to do this you have to select a server (see :ref:`server`)
* Go to the server in Chrome (please use Chrome, it is easier for us to support)
* Click "authenticate", then sign-in using your Google acccount (if you are using our server you must be whitelisted before continuing)
* Click QR, then either

  * Paste the adb command while Glass is plugged connected to USB
  * Select WearScript (setup) and scan the QR code (the first time you start the barcode scanner it has a setup menu, just downswipe to dismiss it)

.. _starting-the-client:

4: Starting the Client
-----------------------
While you have the webapp open, start the client using one of the following methods
* To start with adb use "adb shell am start -n com.dappervision.wearscript/.MainActivity"
* To start with Android Studio after the project has been imported (see Install Client (Source)) select Run->Run 'WearScript'. 
* To start with "Ok Glass" say "wear a script"
