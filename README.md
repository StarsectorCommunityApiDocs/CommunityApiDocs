# CommunityApiDocs

## Maintainers

### Annual: git submodule  https://github.com/StarsectorCommunityApiDocs/StarsectorJarsPrivate.git not found

StarsectorJarsPrivate is a private repo. It requires a Private Access Token (PAT) for the Actions runner to clone it.

When the PAT expires each year, it needs to be regenerated and updated.

1. Go to <https://github.com/settings/tokens>. Generate a new token:
   1. Resource owner: StarsectorCommunityApiDocs.
   2. Expiration: max
   3. Repository access: All repositories
   4. Permissions: Repository Permissions: Contents (read).
   5. Generate token. Copy it.
   6. Go to <https://github.com/StarsectorCommunityApiDocs/CommunityApiDocs/settings/secrets/actions>.
   7. Make a Repository Secret with the name `PRIVATE_REPO_PAT`.
   8. Rerun the Actions script and it should work.