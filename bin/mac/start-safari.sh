#!/bin/sh

# Starts Safari.

osascript -e 'tell application "Safari" to quit without saving'
osascript -e "tell application \"Safari\" to open location \"$1\""