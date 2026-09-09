# Code style

## No comments
Ship code without comments. This is a standing instruction from the project owner.
Name things well enough that they are not needed.

## Naming
- APK output: `FastPrint-<version>.apk`
- CI artifact: `FastPrint`

## Editing www/index.html
It is one 3.6 MB file whose JS lives as escaped text. Patch it with anchored string
replacement, never by hand-editing offsets. Verify the replacement landed before committing.

## Shell
Never author files containing backslashes or regexes through a bash heredoc on this machine:
it eats one backslash. Use the Write tool. This shipped two broken builds.
