pipeline {
    agent { label 'docker-slave' }
    
   
    
    stages {
        stage('Check config') {
            steps {
                // check python version
                sh 'python --version'
                sh 'python3 --version'

                // check pip version
                sh 'pip --version'
                
            }
        }
        stage('Checkout from GitHub') {
            steps {
                echo '[>] Trying to checkout from git repository...'
                git branch: 'main',
                    credentialsId: 'matuszewski-gh-pat',
                    url: 'https://github.com/matuszewski/python-classifiers.git'
                sh "python3 classifiers.py"
                echo 'Build'
                //sh "echo $ref"
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
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: '.', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'HTML Report of successful job execution'])
            archiveArtifacts allowEmptyArchive: true, artifacts: 'index.html', followSymlinks: false
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
