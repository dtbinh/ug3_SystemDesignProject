* Turn on NXT brick and make sure you are in the main menu
* Run the server `./run_server.sh`
* Accept bluetooth pairing with PIN `1337` (if needed, might have been already paired)
* NXT and server output should display "Connected!"
* Then run `python kick.py` or `python drive.py`

* In case you need to reprogram NXT brick, edit `bluetooth_nxt.java` and use `./program_nxt.sh` to upload the program over bluetooth
