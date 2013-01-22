## Bluetooth server ##
* Start bluetooth server, e.g. `./run.sh`
* It will create a IPC socket on `/tmp/nxt_bluetooth_robot`

## Comms protocol ##
Send a string to the IPC socket with required opcode and parameters. Bluetooth server will always reply with current server/robot status (e.g. disconnected, no errors, etc).

All opcodes and return values are defined in `bluetooth_pc.java` file.

## Opcodes and params ##
* 0x01 - set motor speed individually.
** [0x01 m1 m2 m3 m4] - m[1..4] are individual motor speeds and vary between [-255..255], where 0 means free spinning (might be changed to braking in the future.

