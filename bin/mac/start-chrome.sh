#!/bin/sh

osascript -e 'tell application "Google Chrome" to quit without saving'
osascript -e "tell application \"Google Chrome\" to open location \"$1\""