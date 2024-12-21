rootProject.name = "gitiles"

pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.aliyun.com/repository/gradle-plugin")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenLocal()

        maven {
            url = uri("https://maven.aliyun.com/repository/public")
        }
        flatDir {
            dirs("libs")
        }

    }
}

include(":modules:java-prettify")