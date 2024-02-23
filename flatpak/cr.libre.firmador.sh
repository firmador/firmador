#!/bin/sh

PROGRAM="firmador.jar"

# Figure out where the jar is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# create env variables required for it to work fine
export LIBASEP11=/app/lib/libASEP11.so
export FIRMADORINFLATPAK=true
export FLATPAKSOFFICEPATH=/app/libreoffice/opt/libreoffice7.5/program/soffice

# Move to the right folder / Run Firmador
cd "$SCRIPT_DIR"
exec ../jre/bin/java -jar "$PROGRAM" "$@"
