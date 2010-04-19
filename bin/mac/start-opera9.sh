#!/bin/sh

# Starts Opera9.

osascript -e 'tell application "Opera" to quit without saving'
osascript -e "tell application \"Opera\" to open location \"$1\""

