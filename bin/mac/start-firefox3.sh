#!/bin/sh

# Starts Firefox3 when it is installed in Applications/Firefox3 (to coexist with Firefox 2 in Applications/Firefox). Use this instead of calling the AppleScripts directly.

osascript -e 'tell application "FireFox3" to quit without saving'
osascript -e "tell application \"FireFox3\" to open location \"$1\""
