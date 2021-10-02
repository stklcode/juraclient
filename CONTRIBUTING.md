# How to contribute

As for all great Open Source projects, contributions in form of bug reports and code are welcome and important to keep the project alive.

In general, this project follows the [GitHub Flow](https://guides.github.com/introduction/flow/). 
Fork the project, commit your changes to your branch, open a pull request and it will probably be merged.
However, to ensure maintainability and quality of the code, there are some guidelines you might be more or less familiar with. 
For that purpose, this document describes the important points.


## Opening an Issue

If you experience any issues with the plugin or the code, don't hesitate to file an issue.

### Bug Reports

Think you found a bug?
Please clearly state what happens and describe your environment to help tracking down the issue.
 
* Which version of the project are you running?
* Which version of Java?
* Which API do you try to query?

### Feature Requests

Missing a feature or like to have certain functionality enhanced?
No problem, please open an issue and describe what and why you think this change is required.


## Pull Requests

If you want to contribute your code to solve an issue or implement a desired feature yourself, you might open a pull request.
If the changes introduce new functionality or affect major parts of existing code, please consider opening an issue for discussion first.

For adding new functionality a new test case the corresponding JUnit test would be nice (no hard criterion though).

### Branches

The `master` branch represents the current state of development.
Please ensure your initial code is up to date with it at the time you start development.
The `master` should also be target for most pull requests.

In addition, this project features a `develop` branch, which holds bleeding edge developments, not necessarily considered stable or even compatible.
Do not expect this code to run smoothly, but you might have a look into the history to see if some work on an issue has already been started there.

For fixes and features, there might be additional branches, likely prefixed by `feature/` `fix/` followed by an issue number (if applicable) and/or a title.
Feel free to adapt these naming scheme to your forks.

### Merge Requirements

To be merged into the master branch, your code has to pass the automated continuous integration tests, to ensure compatibility.
In addition, your code has to be approved by a project member.

#### What if my code fails the tests?

Don't worry, you can submit your PR anyway. 
The reviewing process might help you to solve remaining issues.

### Commit messages

Please use speaking titles and messages for your commits, to ensure a transparent history.
If your patch fixes an issue, reference the ID in the first line.
If you feel like you have to _briefly_ explain your changes, do it (for long explanations and discussion, consider opening an issue or describe in the PR).

**Example commit:**
```text
Fix nasty bug (#1337)

This example commit fixes the issue that some people write non-speaking
commit messages like 'done magic'.
A short description is helpful sometimes.
```

You might sign your work, although that's no must.


### When will it be merged?

Short answer: When it makes sense.

Bug fixes should be merged in time - assuming they pass the above criteria.
New features might be assigned to a certain milestone and as a result of this be scheduled according to the planned release cycle.


## Versioning

This projects tries to adapt the [Semantic Versioning](https://semver.org).
In short, bug fixes without do not affect any compatibility will raise the third number only, new features will be reflected in the second number and any change breaking compatibility with the public API require raising the first number.

If you have to make a decision for which version to go please keep this in mind.
However, for most non-member commits this is mostly informative, as the decision will be made by the project team later.


## Build Environment

All you need to start off - besides your favorite IDE of course - is Java and Maven.
The project requires Java 8 or higher, both OpenJDK and Oracle JDK are supported.
All build steps are executed by calling e.g. `mvn clean package` in the project's root directory,


## Unit Tests

The Java code is tested by a set of JUnit tests.
All test files are located in the `src/test` directory. 
Files ending with `Test.java` will be automatically included into the test suite.


## Continuous Integration

Automated tests are run using [GitHub Actions](https://github.com/stklcode/juraclient/actions/) for every commit including pull requests.

There is also a code quality analysis pushing results to [SonarCloud](https://sonarcloud.io/dashboard?id=de.stklcode.pubtrans%3Ajuraclient).
Keep in mind that the ruleset is not yet perfect, so not every minor issue has to be fixed immediately.


## Still Open Questions?

If anything is still left unanswered, and you're unsure if you got it right, don't hesitate to contact a team member.
In any case you might submit your request/issue anyway, we won't refuse good code only for formal reasons.
