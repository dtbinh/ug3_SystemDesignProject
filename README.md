# SDP 2012/2013 Group 5 #

## To install all dependencies on DICE machine ##
* Clone repository locally, e.g. `cd ~/ && git clone git@github.com:c-w/ug3_SDP.git` (do not forget to add DICE ssh public key to your github account)
* cd to project directory and run the install script, e.g. `cd ~/ug3_SDP && ./install.sh`
* Logout and login for new library paths to take effect, alternatively `source ~/.bashrc`
* ??????
* Profit

## NXT brick programming over bluetooth on DICE ##
Look into `src/examples/bluetooth/run.sh` script. It compiles java to lejos byte code, links it and uploads it over bluetooth, code is run automatically. This works only on DICE machines with BT adapter (i.e. ones around the pitch) and after the all dependencies have been installed (see: To install all dependencies on DICE machine).

Alternatively, one could use USB cable, but DICE accounts do not have sufficient privileges for that, so it would work only on own machine. To see example of this look into `src/examples/nxt/HelloWorld/run.sh`.


## Setting up the Milestone 2 code in Eclipse ##

* In eclipse, go to file -> new -> java project
* set project name to whatever you like, untick "use default location", 
* For "location", select browse then navigate to ug3_SDP/src/Augsrc , then click "OK", then "Finish"
* In the package explorer, go to {YOUR PROJECT}/src/PC, right click Milestone2.java, go to "run as", then go to "run configurations"
* go to the environment tab, click "new", then type LD_LIBRARY_PATH for the name and ./lib for the value, click "ok" then "apply" then "run"
* the App will run, to run it in future, just right click Milestone2 then run as -> java application. 

 Have fun!


## Collaborators ##
* Duka
* Ironside
* McGillion (samtmcg)
* Moore (Zahzou)
* Osman (osozg)
* Pantov
* Rooney
* Sutas (eAndrius)
* Wolff (c-w)
