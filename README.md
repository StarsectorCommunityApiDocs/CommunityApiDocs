# Starsector Community API Docs

Online at <https://starsectorcommunityapidocs.github.io/CommunityApiDocs/>.

## Contributors

### Setup guide

This is for **IntelliJ**, but should be easy enough to adapt to other IDEs (or none). Please feel free to add
instructions for other IDEs.

1. Request to join the Team here: <https://github.com/orgs/StarsectorCommunityApiDocs/teams/contributor>.
   1. Unfortunately, there is no way to automatically add you to the team, so you will need to wait for a maintainer to
      approve your request (if someone knows a way, please let us know).
2. Add the git submodule to your mod:
    1. If you haven't already, connect IntelliJ to your GitHub account by going to `File -> Settings -> Version Control
       -> GitHub` and adding your account there.
    2. Within IntelliJ, open the Terminal (icon is in the bottom-left by default).
    3. Optionally, make a new folder for the docs and `cd` into it. Otherwise, it will go into a new folder called
       `CommunityApiDocs` in your project.
    4. Type `git submodule add https://github.com/StarsectorCommunityApiDocs/CommunityApiDocs`.
3. Add the git submodule to IntelliJ: 
    1. Open the IntelliJ settings and navigate to `Version Control -> Directory Mappings`.
    2. Click the `+` and add the `CommunityApiDocs` folder that was just created.
4. Set your mod's dependency to be on the new git submodule.
    1. Open the Project Structure (`File -> Project Structure`).
    2. Go to `Modules -> Dependencies`.
    3. Find your existing dependency on `starsector-core`.
    4. Edit it, leaving all _Classes_ the same, but adding/changing the _Sources_ to your new `CommunityApiDocs/src`
       folder.
        1. You should now have a _Sources_ entry that looks like `<your mod path>/CommunityApiDocs/src`.
5. All done!
    1. Now, when you navigate to vanilla source code, it will open up the Community API Docs instead, which you can
       edit.
    2. You'll also see the documentation from the Community API Docs in the tooltips and quick documentation.
    3. Make sure to update the submodule every so often to get the latest changes.
    4. When you make changes, you'll see them show up in the IntelliJ `Commit` tab (found on the left side of IntelliJ
       by default).

## Maintainers

### Tasks

#### Annual task: git submodule  <https://github.com/StarsectorCommunityApiDocs/StarsectorJarsPrivate.git> not found

When the Private Access Token (PAT) expires each year, it needs to be regenerated and updated.

StarsectorJarsPrivate is a private repo that contains Starsector's jar files, needed for `javadoc` to run. It requires a
PAT for the Actions runner to clone it.

1. Go to <https://github.com/settings/tokens>. Generate a new token:
    1. Resource owner: StarsectorCommunityApiDocs.
    2. Expiration: max
    3. Repository access: All repositories
    4. Permissions: Repository Permissions: Contents (read).
    5. Generate token. Copy it.
2. Go to <https://github.com/StarsectorCommunityApiDocs/CommunityApiDocs/settings/secrets/actions>.
3. Make a Repository Secret with the name `PRIVATE_REPO_PAT`.
4. Rerun the Actions script and it should work.

#### Game update task: Update game jars

1. Go to <https://github.com/StarsectorCommunityApiDocs/StarsectorJarsPrivate>.
2. Remove those and replace them with the ones in your own starsector-core folder (sort by: type, scroll to jars).
3. Re-run the Actions script to update the docs.