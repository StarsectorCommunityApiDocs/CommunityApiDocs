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

#### Gradle

This part is only relevant if you are using **gradle** as a build system.

In order for your IDE to find the source files and allow you to navigate to them, you can add them as a dependency.
To do so, in the dependencies section of your, add the following:

```kotlin
    compileOnly(fileTree("$projectDir/CommunityApiDocs/src/com/fs/starfarer/api"){
        include("*.java")
    })
```

Double-check the path to make sure it matches the submodule path.

Note that this might not be the optimal or most canonical solution, but it did work for me.

#### Git CLI

This part is only relevant if you use the **git command line interface**.

To easily be able to use the git CLI, make sure to add the submodule via SSH rather than HTTP:

```shell
git submodule add git@github.com:StarsectorCommunityApiDocs/CommunityApiDocs.git
```

If you haven't already, generate an [ssh-key for Github](https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent)
to be able to push without needing to enter credentials.

If you want to commit/push your changes, simply navigate into the CommunityApiDocs folder and follow your 
usual workflow for commiting/pushing. No additional steps are required.

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

## Attributions

Dracula-javadoc was used to achieve a dark theme

Dracula-javadoc is licensed unter the MIT License

### MIT License

MIT License

Copyright (c) 2019 Allan Im

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
