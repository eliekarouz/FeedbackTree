plugins {
    id("com.eden.orchidPlugin") version "0.21.0"
}

// 2. Include Orchid dependencies
dependencies {
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidDocs:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidEditorial:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidKotlindoc:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidPluginDocs:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidGithub:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSyntaxHighlighter:0.21.0")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidWiki:0.21.0")
}

// 3. Get dependencies from JCenter and Kotlinx Bintray repo
repositories {
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx/")
}

// 4. Use the 'Editorial' theme, and set the URL it will have on Github Pages
orchid {
    version = "0.20.0"
}