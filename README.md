# gradle-google-play-service-plugin

A Gradle plugin for adding customized stripped Google Play Service library.

64K method limit is a hard problem while developing a growing Android app, especially 
including Google Play Service in the app because it contributes over 20k methods to your app.

This gradle plugin gives you a easy way to use DSL to specify which components you need in the app.

You can use this plugin offine by checkout this repo and run `gradle install` to install in your local Maven repo.
Or use it with Central Maven Repository (WIP, waiting for Sonatype review)

## Basic usage

Add to your build.gradle

```
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.github.david402:playservice-plugin:0.0.7'
  }
}

apply plugin: 'playservice-plugin'

playServices {
    version '5.0.89'
}
```

## Advanced usage

You can strip Google Play service lib with components only needed by your project.

Example of stripping play service only with modules used by `ads` and `analytics`
```
playServices {
    version '5.0.89'
    components 'ads', 'common', 'dynamic', 'internal', 'analytics'
}
```

Here is the list of components in Google Play Service:
- actions
- ads
- analytics
- appindexing
- appstate
- auth
- cast
- common
- drive
- dynamic
- games
- gcm
- identity
- internal
- location
- maps
- panorama
- plus
- security
- tagmanager
- wallet
- wearable

## License

```
Copyright 2013 David Liu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
