#!/bin/sh

# Starts Firefox.

osascript -e 'tell application "FireFox" to quit without saving'
osascript -e "tell application \"FireFox\" to open location \"$1\""

