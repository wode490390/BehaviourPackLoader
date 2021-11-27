pipeline {
    agent any
    tools {
        maven 'Maven 3'
        jdk 'Java 8'
    }
    options {
        buildDiscarder(logRotator(artifactNumToKeepStr: '1'))
    }
    stages {
        stage ('Build') {
            steps {
                sh 'mvn -B clean package jar:test-jar'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        stage ('Deploy') {
            when {
                branch "master"
            }
            steps {
                rtMavenDeployer (
                        id: "maven-deployer",
                        serverId: "wode",
                        releaseRepo: "maven-releases",
                        snapshotRepo: "maven-snapshots"
                )
                rtMavenResolver (
                        id: "maven-resolver",
                        serverId: "wode",
                        releaseRepo: "maven-deploy-release",
                        snapshotRepo: "maven-deploy-snapshot"
                )
                rtMavenRun (
                        pom: 'pom.xml',
                        goals: 'source:jar javadoc:javadoc javadoc:jar install -DskipTests',
                        deployerId: "maven-deployer",
                        resolverId: "maven-resolver"
                )
                step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])
                rtPublishBuildInfo (
                        serverId: "wode"
                )
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
