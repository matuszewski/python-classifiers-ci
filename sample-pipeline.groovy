pipeline {
    
    agent {
        label 'docker-slave'
    }
    
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
            }
        }
        
        stage('Unit tests') {
            steps {
                echo '[>] Starting unit testing...'
            }
        }
        
        stage('PyLint Test') {
            steps {
                echo '[>] Starting PyLint testing...'
                
                sh "pylint classifiers.py"
            }
        }
        
    }
    
    
    post {
        
        always {
            echo "JOB FINISHED"
        }
        
        success {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as SUCCESS"
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: '.', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'HTML Report of successful job execution'])
            archiveArtifacts allowEmptyArchive: true, artifacts: 'index.html', followSymlinks: false
            // clean up the workspace
            deleteDir()
        }
        
        unstable {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as UNSTABLE"
        }
        
        failure {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as FAILURE"
            
            // send email notification
            //mail to: 'krzysiekmatuszewski@outlook.com',
            // subject: "Pipeline job failure: ${currentBuild.fullDisplayName}",
            // body: "Build URL: ${env.BUILD_URL}"
        }
        
        changed {
            echo 'Job changed state'
        }
        
    }
}
