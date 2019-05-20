def CONTAINER_NAME="slotbook-telegram-bot"

node {
    stage('Initialize'){
        def dockerHome = tool 'myDocker'
        def sbtHome = tool 'mySbt'
        env.PATH = "${dockerHome}/bin:${sbtHome}/bin:${env.PATH}"
    }

    stage('Checkout') {
        checkout scm
    }

    stage("Image Prune"){
        imagePrune(CONTAINER_NAME)
    }

    stage('Build image'){
      sbtPublishLocal()
    }

    stage('Run App'){
        runApp()
    }
}

def imagePrune(containerName){
    try {
        sh "docker image prune -f"
        sh "docker stop $containerName"
    } catch(error){}
}

def sbtPublishLocal() {
  sh "sbt docker:publishLocal"
}

def runApp(){
    sh '/opt/services/slotbook-notification-service/restart.sh'
}
