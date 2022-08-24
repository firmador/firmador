pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }
    stage('Archive') {
      steps {
        archiveArtifacts(artifacts: 'target/firmador.jar', onlyIfSuccessful: true)
      }
    }
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '1'))
  }
}
