pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh 'xvfb-run mvn clean package'
      }
    }
    stage('Archive') {
      steps {
        sh 'firmar-jar target/firmador.jar'
        archiveArtifacts artifacts: 'target/firmador.jar', onlyIfSuccessful: true
      }
    }
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '1'))
  }
}
