---
title: Setup
category: Installation
order: 1
---

Add it in your root build.gradle:

```
allprojects {
  repositories {
    mavenCentral()
  }
}
```

Add the dependency to your build.gradle:

```
dependencies {
  implementation "com.github.eliekarouz.feedbacktree:feedbacktree:0.9.1"
}
```