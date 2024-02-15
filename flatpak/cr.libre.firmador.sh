#!/bin/sh

PROGRAM="firmador.jar"

# Figure out where the jar is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# create env variable required for the library to work fine
export LIBASEP11=/app/lib/libASEP11.so

# Move to the right folder / Run Firmador
cd "$SCRIPT_DIR"
exec ../jre/bin/java -jar "$PROGRAM"
