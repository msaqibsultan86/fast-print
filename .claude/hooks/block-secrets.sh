#!/bin/sh
payload=$(cat)
case "$payload" in
  *keystore.properties*|*.keystore*|*.jks*|*client_secret*)
    case "$payload" in
      *"git add"*|*"git commit"*)
        echo "Blocked: refusing to stage signing material or client secrets." >&2
        exit 2
        ;;
    esac
    ;;
esac
exit 0
