
// Static analysis configuration
detekt {
    config = files("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}

ktlint {
    version = "1.0.1"
    debug = false
    verbose = true
    android = true
    outputToConsole = true
    outputColorName = "ALWAYS"
    ignoreFailures = false
    enableExperimentalRules = true
}
