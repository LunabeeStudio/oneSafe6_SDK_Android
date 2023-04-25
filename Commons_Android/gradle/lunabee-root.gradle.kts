import java.net.URI

val artifactoryUsername: String = project.findProperty("artifactory_consumer_username") as? String
    ?: "library-consumer-public"
val artifactoryPassword: String = project.findProperty("artifactory_consumer_api_key") as? String
    ?: "AKCp8k8PbuxYXoLgvNpc5Aro1ytENk3rSyXCwQ71BA4byg3h7iuMyQ6Sd4ZmJtSJcr7XjwMej"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI("https://artifactory.lunabee.studio/artifactory/libs-release-local")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            url = URI("https://artifactory.lunabee.studio/artifactory/libs-snapshot-local")
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
            mavenContent {
                snapshotsOnly()
            }
        }
    }
}
