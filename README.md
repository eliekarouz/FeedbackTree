# FeedbackTree

Unidirectional data flow architecture for Android.\
The API is not stable yet as we are still experimenting with the concept.\
The idea is a combination of NoTests/RxFeedback.kt and Square/Workflows

### Installation

Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Add the dependency
```
dependencies {
  implementation 'com.github.eliekarouz:feedbacktree:0.5'
}
```
