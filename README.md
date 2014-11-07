[![Stories in Ready](https://badge.waffle.io/federicoiosue/Omni-Notes.png?label=ready&title=Ready)](https://waffle.io/federicoiosue/Omni-Notes)
[![Stories in In Progress](https://badge.waffle.io/federicoiosue/Omni-Notes.png?label=In%20Progress&title=In Progress)](https://waffle.io/federicoiosue/Omni-Notes)
Omni-Notes
==========

Note taking <b>open-source</b> application aimed to have both a <b>simple interface</b> but keeping <b>smart</b> behavior.

The project was created by the absence of such applications compatible with old phones and old versions of Android that would propose, however, an attractive look and aligned with the most recent design of the Google operating system


**Follow the developments and post your comments and advice on Google+ Beta Comunity at http://goo.gl/eF6qqF**

*Help to keep translations updated is always welcome, if you want give an hand checkout the translation project at [Crowdin][2]*

##Features


Actually the following functions are implemented:

* Android KitKat-ready appearance
* Manage active, archived, trashed notes and filter the ones with reminder
* Add, modify, share notes
* Use categories to organize notes and add them some color
* Mask notes to avoid others to look at that without having the password
* Note searching by text or tags and optimized search-as-you-type mode
* Batch operations choosing multiple notes: merge, archive, trash, delete
* Export/import notes and settings to safe backup folder
* Multimedia attachments: all file types are supported!
* Sketch mode to add hand made drawings or notes
* To-do list with customizable behavior
* Gesture actions to delete and create notes in a faster way


Further developments will include:

* Notes sychronization
* Web interface to manage notes

You can find a complete changelog inside the application settings menu!

If you need some help on how to use the application you'll find everything you need into the [Help Online](etc/help/help.md)

[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_00.png">][3]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_01.png">][4]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_02.png">][5]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_03.png">][6]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_04.png">][7]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_05.png">][8]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_06.png">][9]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_07.png">][10]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_08.png">][11]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_09.png">][12]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_10.png">][13]
[<img src="https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/thumb_11.png">][14]


[<img src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png">][1]


##Build the app

Due to the fact that I'm using [gitflow](https://github.com/nvie/gitflow) as code versioning methodology (even before it was formalized) you, as developer should **always** start working on [develop branch](https://github.com/federicoiosue/Omni-Notes/tree/develop) that contains the most recent changes.

To simplify the approach with the app code I switched to the new Android Studio IDE (BETAs are ready to be daily used) and I created a personal [Maven repository](https://github.com/federicoiosue/repository) to avoid you to download all the libraries and sub-projects Omni Notes depends on.

Short story long: you have to clone from Omni Notes' develop branch on GitHub the code into Android Studio, wait for all dependencies have been downloaded and start coding!

**Don't forget to contribute to original code! Don't be selfish or lazy!**

####Dependences

They're all listed into the [build.gradle](https://github.com/federicoiosue/Omni-Notes/blob/develop/omniNotes/build.gradle) file but due to the fact that many of the dependences have been customized by me I'd like to say thanks here to the original developers of these great libraries:

* https://github.com/derekbrameyer/android-betterpickers
* https://github.com/gabrielemariotti/changeloglib
* https://github.com/LarsWerkman/HoloColorPicker
* https://github.com/keyboardsurfer
* https://github.com/neopixl/PixlUI


##Developed with love and passion by


* Federico Iosue - [Website](http://www.iosue.it/federico)



##License


    Copyright 2014 Federico Iosue

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [1]: https://play.google.com/store/apps/details?id=it.feio.android.omninotes
 [2]: https://crowdin.net/project/omni-notes/
 [3]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/01.png
 [4]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/02.png
 [5]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/03.png
 [6]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/04.png
 [7]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/05.png
 [8]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/06.png
 [9]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/07.png
 [10]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/08.png
 [11]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/09.png
 [12]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/10.png
 [13]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/11.png
 [14]: https://raw.githubusercontent.com/federicoiosue/Omni-Notes/master/etc/play_store_pics/12.png
