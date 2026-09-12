@echo off
title Fast Print - local preview
cd /d "%~dp0www"
echo Serving Fast Print at http://localhost:8777
start "" http://localhost:8777/index.html
python -m http.server 8777
