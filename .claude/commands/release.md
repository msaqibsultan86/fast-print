Cut a release.

1. Confirm the working tree is clean and pushed.
2. Bump the version if the user named one.
3. Tag `v<version>` and push the tag.
4. Watch the Release workflow.
5. Confirm the published asset exists AND that `apksigner verify` passed in the log.
6. Give the user the direct download link.

Never report success from a green tick alone. An unsigned APK builds green and will not install.
