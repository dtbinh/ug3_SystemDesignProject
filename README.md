# SDP 2012/2013 Group 5 #

## To install all dependencies on DICE machine ##
* Clone repository locally, e.g. `cd ~/ && git clone git@github.com:c-w/ug3_SDP.git` (do not forget to add DICE ssh public key to your github account)
* cd to project directory and run the install script, e.g. `cd ~/ug3_SDP && ./install.sh`
* Logout and login for new library paths to take effect, alternatively `source ~/.bashrc`
* ??????
* Profit


## Setting up the code in Eclipse ##
* In eclipse, go to file -> new -> java project
* set project name to whatever you like, untick "use default location", 
* For "location", select browse then navigate to ug3_SDP/src/Augsrc , then click "OK", then "Finish"
* go to the environment tab, click "new", then type LD_LIBRARY_PATH for the name and ./lib for the value, click "ok" then "apply" then "run"
