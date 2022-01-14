pipeline {
    agent { label 'docker-slave' }
    
   
    
    stages {
        
        stage('Checkout from GitHub') {
            steps {
                echo '[>] Trying to checkout from git repository...'
                git branch: 'main',
                    credentialsId: 'matuszewski-gh-pat',
                    url: 'https://github.com/matuszewski/python-classifiers.git'
                //sh "python main.py"
                echo 'Build'
                sh "echo $ref"
            }
        }
        
        stage('Unit tests') {
            steps {
                echo '[>] Starting unit testing...'
            }
        }
        
        stage('Functional tests') {
            steps {
                echo '[>] Starting functional testing...'
            }
        }
        
    }
    post {
        always {
            echo '[>] ------------ DONE ------------ '
            
        }
        success {
            echo '[>] Result: successful'
            
            // clean up the workspace
            deleteDir()
        }
        unstable {
            echo '[!] Result: unstable'
        }
        failure {
            echo '[!] Result: failed'
            
            // send email notification
            //mail to: 'krzysiekmatuszewski@outlook.com',
            // subject: "Pipeline job failure: ${currentBuild.fullDisplayName}",
            // body: "Build URL: ${env.BUILD_URL}"
        }
        changed {
            echo '[>] Result: changed state'
        }
    }
}
