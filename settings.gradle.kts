pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HustleHub"
include(":app")
include(":core_ui")
include(":core_network")
include(":core_database")
include(":feature_auth")
include(":feature_onboarding")
include(":feature_jobs")
include(":feature_wallet")
