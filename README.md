# SDP 2012/2013 Group 5 #

## Install dependencies on DICE machine ##
* Clone repository locally, e.g. 
  `cd ~/ && git clone git@github.com:c-w/ug3_SDP.git`
  (do not forget to add DICE ssh public key to your github account)
* Go to project directory and run the install script, e.g. 
  `cd ~/ug3_SDP && ./install.sh`
* Logout and login for new library paths to take effect, alternatively 
  `source ~/.bashrc`


## Setting up the code in Eclipse ##
* In eclipse, go to file -> new -> java project
* Set project name to whatever you like, untick "use default location", 
* For "location", select browse, navigate to ug3_SDP, click "OK", then "Finish"
* Go to the environment tab, click "new", then type LD_LIBRARY_PATH for the 
  name and ./lib for the value, click "ok" then "apply" then "run"

## Bluetooth ##
* Turn on NXT brick and make sure you are in the main menu
* Run the server `./bluetooth.sh`
* If needed accept bluetooth pairing with PIN `1337`
* NXT and server output should display "Connected!"
* In case you need to reprogram NXT brick, edit
  `src/BluetoothServer/BluetoothNXT.java` and use `./bluetooth.sh --program-nxt`
  to upload the program over bluetooth
* To return to the main menu press `orange` and `down` (the one right below 
  orange) buttons at the same time
* If using the dummy robot, add --dummy-robot to all the script calls
