pluginManagement {
    repositories {
        // Repositorio de Google (Android, Firebase, Play Services)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Repositorios adicionales
        mavenCentral()
        gradlePluginPortal()
        // jcenter() // ⚠️ Usar solo si alguna dependencia antigua lo necesita
    }
}

dependencyResolutionManagement {
    // ✅ Permite repositorios en los módulos (corrige tu error)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()        // ✅ Siempre primero
        mavenCentral()  // ✅ Repositorio central
        // jcenter()    // ⚠️ Solo si es necesario
    }
}

rootProject.name = "SmartFashionEcommerce"
include(":app")
