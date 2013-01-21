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

## Collaborators ##
* Duka
* Ironside
* McGillion
* Moore (Zahzou)
* Osman (osozg)
* Pantov
* Rooney
* Sutas (eAndrius)
* Wolff (c-w)
