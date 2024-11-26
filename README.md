# Starsector Community API Docs

Online at <https://starsectorcommunityapidocs.github.io/CommunityApiDocs/>.

## Maintainers

### Tasks

#### Annual task: git submodule  <https://github.com/StarsectorCommunityApiDocs/StarsectorJarsPrivate.git> not found

When the Private Access Token (PAT) expires each year, it needs to be regenerated and updated.

StarsectorJarsPrivate is a private repo that contains Starsector's jar files, needed for `javadoc` to run. It requires a PAT for the Actions runner to clone it.


1. Go to <https://github.com/settings/tokens>. Generate a new token:
   1. Resource owner: StarsectorCommunityApiDocs.
   2. Expiration: max
   3. Repository access: All repositories
   4. Permissions: Repository Permissions: Contents (read).
   5. Generate token. Copy it.
   6. Go to <https://github.com/StarsectorCommunityApiDocs/CommunityApiDocs/settings/secrets/actions>.
   7. Make a Repository Secret with the name `PRIVATE_REPO_PAT`.
   8. Rerun the Actions script and it should work.

#### Game update task: Update game jars

1. Go to <https://github.com/StarsectorCommunityApiDocs/StarsectorJarsPrivate>.
2. Remove those and replace them with the ones in your own starsector-core folder (sort by: type, scroll to jars).
3. Re-run the Actions script to update the docs.