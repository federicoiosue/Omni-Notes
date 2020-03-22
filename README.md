 ![icon](assets/logo.png)

Omni-Notes
==========

![SLicense](https://img.shields.io/badge/License-GPLv3-red.svg)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/omni-notes/localized.png)](https://crowdin.com/project/omni-notes)
[![Build Status](https://travis-ci.org/federicoiosue/Omni-Notes.svg?branch=develop)](https://travis-ci.org/federicoiosue/Omni-Notes)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8ade707d00ef468fa79d3f6b622444b5)](https://www.codacy.com/app/federico-iosue/Omni-Notes?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=federicoiosue/Omni-Notes&amp;utm_campaign=Badge_Grade)
[![GitHub release](https://img.shields.io/github/release/federicoiosue/omni-notes.svg)](https://github.com/federicoiosue/Omni-Notes/releases/latest)

Note taking <b>open-source</b> application aimed to have both a <b>simple interface</b> but keeping <b>smart</b> behavior.

The project was inspired by the absence of such applications compatible with old phones and old versions of Android. It aims to provide an attractive look and follow the most recent design guidelines of the Google operating system.

**Follow the developments and post your comments and advice on Facebook Community at https://www.facebook.com/OmniNotes**

*Help to keep translations updated is always welcome, if you want give a hand checkout the translation project on **https://translate.omninotes.app.** *

<a href="https://f-droid.org/repository/browse/?fdid=it.feio.android.omninotes.foss" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="90"/></a>
<a href="https://play.google.com/store/apps/details?id=it.feio.android.omninotes" target="_blank">
<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="90"/></a>

## Features

Currently the following functions are implemented:

* Material Design interface
  *Basic add, modify, archive, trash and delete notes actions
* Share, merge and search notes
* Image, audio and generic file attachments
* Manage your notes using tags and categories
* To-do list
* Sketch-note mode
* Notes shortcut on home screen
* Export/import notes to backup
* Google Now integration: just tell "write a note" followed by the content
* Multiple widgets, DashClock extension, Android 4.2 lockscreen compatibility
* Multilanguage: 30+ languages supported: https://crowdin.com/project/omni-notes


Further developments will include:

* Notes sychronization
* Web interface to manage notes ([stub project](https://github.com/federicoiosue/omni-notes-desktop))

You can find a complete changelog inside the application settings menu!

If you need some help on how to use the application you'll find everything you need in the [Help Online](assets/help/help.md) section.

![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/02.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/03.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/04.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/05.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/06.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/07.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/08.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/09.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/10.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/11.png)
![](https://raw.githubusercontent.com/federicoiosue/Omni-Notes/develop/assets/play_store_pics/12.png)

## User guide

Look into the wiki for GIFs-based tutorials: [LINK](https://github.com/federicoiosue/Omni-Notes/wiki)

## Build

Watch the following terminal session recording on how to compile distributable files
[![asciicast](https://asciinema.org/a/102898.png)](https://asciinema.org/a/102898)

To be sure that build environment is fully compliant with the project the following command creates a container with all the needed tools to compile the code:

```
cd {project-folder}; rm local.properties; docker rm android-omninotes; docker run -v $PWD:/workspace --name android-omninotes tabrindle/min-alpine-android-sdk:latest bash -c "yes | sdkmanager --update && yes | sdkmanager --licenses && cd /workspace && ./gradlew build --stacktrace -Dorg.gradle.daemon=true -Pandroid.useDeprecatedNdk=true"

```

## Test

To execute all tests included into the project connect a device or emulator, then run the following command:

```shell
./gradlew testAll
```

### Testing Pyramid

To speedup the development more levels of testing are available following the [testing pyramid approach](https://martinfowler.com/articles/practical-test-pyramid.html), each type test requires more time than the previous one.

### Unit Tests
```shell
./gradlew --stacktrace test
```

### Integration Tests
```shell
./gradlew --stacktrace -Pandroid.testInstrumentationRunnerArguments.notAnnotation=androidx.test.filters.LargeTest connectedAndroidTest
```

### UI Tests
```shell
./gradlew --stacktrace -Pandroid.testInstrumentationRunnerArguments.annotation=androidx.test.filters.LargeTest connectedPlayDebugAndroidTest
```
Notice that in this case I specified a single flavor to run tests on. This could be a useful and faster approach when you're testing specific flavor features.  

## Contributing

Due to the fact that I'm using [gitflow](https://github.com/nvie/gitflow) as code versioning methodology, you as developer should **always** start working on [develop branch](https://github.com/federicoiosue/Omni-Notes/tree/develop) that contains the most recent changes.

There are many features/improvements that are not on **my** roadmap but someone else could decide to work on them anyway: hunt for issues tagged as [Help Wanted](https://github.com/federicoiosue/Omni-Notes/issues?utf8=✓&q=label%3A"Help+wanted") to find them!

Feel free to add yourself to [contributors.md](https://github.com/federicoiosue/Omni-Notes/blob/develop/contributors.md) file.

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
  - *.travis.yml*: if you use [TravisCI](https://travis-ci.org/) as continuous integration platform and a SonarQUBE instance for code quality analysis you'll have to modify this file according to your needs.

## Code quality

A public instance of SonarQube is available both to encourage other developers to improve their code contributions (and existing code obviously) and to move the project even further into transparency and openness.

Checkout for it [here](https://sonarcloud.io/dashboard?id=omni-notes)

Pull requests will be automatically analyzed and rejected if they'll rise the code technical debt.

## Dependencies

They're all listed into the [build.gradle](https://github.com/federicoiosue/Omni-Notes/blob/develop/omniNotes/build.gradle) file but due to the fact that many of the dependences have been customized by me I'd like to say thanks here to the original developers of these great libraries:

* https://github.com/RobotiumTech/robotium
* https://github.com/LarsWerkman/HoloColorPicker
* https://github.com/keyboardsurfer/Crouton
* https://github.com/romannurik/dashclock/wiki/API
* https://github.com/ACRA/acra
* https://github.com/Shusshu/Android-RecurrencePicker
* https://github.com/gabrielemariotti/changeloglib
* https://github.com/greenrobot/EventBus
* https://github.com/futuresimple/android-floating-action-button
* https://github.com/keyboardsurfer
* https://github.com/nhaarman/ListViewAnimations
* https://github.com/bumptech/glide
* https://github.com/neopixl/PixlUI
* https://github.com/afollestad/material-dialogs
* https://github.com/JakeWharton/butterknife
* https://github.com/ical4j
* https://github.com/square/leakcanary
* https://github.com/pnikosis/materialish-progress
* https://github.com/apl-devs/AppIntro
* https://github.com/ReactiveX/RxAndroid
* https://github.com/artem-zinnatullin/RxJavaProGuardRules
* https://github.com/tbruyelle/RxPermissions
* https://github.com/ocpsoft/prettytime
* https://github.com/piwik/piwik-sdk-android
* https://github.com/mrmans0n/smart-location-lib


## Mentioned on

[XDA](https://www.xda-developers.com/omni-notes-the-open-source-note-app/)
[Android Authority](https://www.androidauthority.com/best-note-taking-apps-for-android-205356/)
[Droid Advisor](https://droidadvisor.com/omni-notes-note-taking-app/)
[Addictive Tips](https://www.addictivetips.com/android/note-taking-apps-for-android/)
[Techalook](https://techalook.com/apps/best-sticky-notes-android-iphone/)
[DZone](https://dzone.com/articles/amazing-open-source-android-apps-written-in-java)
[Slash Gear](https://www.slashgear.com/best-note-taking-apps-for-android-phones-and-tablets-04529297/)
[quaap.com](https://quaap.com/D/use-fdroid)

## Developed with love and passion by


* Federico Iosue - [Website](https://federico.iosue.it)
* [Other contributors](https://github.com/federicoiosue/Omni-Notes/blob/master/https://github.com/federicoiosue/Omni-Notes/blob/master/CONTRIBUTORS.md)



## License


    Copyright 2013-2019 Federico Iosue
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

