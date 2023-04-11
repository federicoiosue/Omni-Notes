## Contributing

Due to the fact that I'm using [gitflow](https://github.com/nvie/gitflow) as code versioning methodology, you as developer should **always** start working on [develop branch](https://github.com/federicoiosue/Omni-Notes/tree/develop) that contains the most recent changes.

There are many features/improvements that are not on **my** roadmap but someone else could decide to work on them anyway: hunt for issues tagged as [Help Wanted](https://github.com/federicoiosue/Omni-Notes/issues?utf8=✓&q=label%3A"Help+wanted") to find them!

Feel free to add yourself to [contributors.md](https://github.com/federicoiosue/Omni-Notes/blob/develop/CONTRIBUTORS.md) file.

### New feature or improvements contributions

This kind of contributions **must** have screenshots or screencast as demonstration of the new additions.

### Code style

If you plan to manipulate the code then you'll have to do it by following a [specific code style](https://gist.github.com/federicoiosue/dee53e882b3c70d544f8608769eb02fc).
Also pay attention if you're using any plugin that automatically formats/cleans/rearrange your code and set it to only change that code that you touched and not the whole files.

### Test your code contributions!

All code changes and additions **must** be tested.
See the [related section](#test) for more informations or this two pull requests comments: [one](https://github.com/federicoiosue/Omni-Notes/pull/646#pullrequestreview-187973443) and [two](https://github.com/federicoiosue/Omni-Notes/pull/683#issuecomment-506206689)

### Forking project

When forking the project you'll have to modify some files that are strictly dependent from my own development / build / third-party-services environment. Files that need some attention are the following:

  - *gradle.properties*: this is overridden by another file with the same name inside the *omniNotes* module. You can do the same or leave as it is, any missing property will let the app gracefully fallback on a default behavior.

## Code quality

A public instance of SonarQube is available both to encourage other developers to improve their code contributions (and existing code obviously) and to move the project even further into transparency and openness.

Checkout for it [here](https://sonarcloud.io/dashboard?id=omni-notes)

Pull requests will be automatically analyzed and rejected if they'll rise the code technical debt.
