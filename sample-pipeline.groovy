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
            
            // clean up the workspace
            // TODO : uncomment deleteDir()
            
            // publish HTML report and archive
            publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: "HTML Report Directory", reportFiles: 'html-file_name', reportName: 'HTML Report', reportTitles: 'HTML Report of successful job execution'])
            archiveArtifacts allowEmptyArchive: true, artifacts: 'index.html', followSymlinks: false

            // send status to github
            // TODO
            try {
                echo "Trying to send the job status to GitHub"
                githubNotify context: 'Notification key', description: 'This is a shorted example',  status: 'SUCCESS'

            } catch (Exception e) {
                echo 'Exception occurred while trying to send the status: ' + e.toString()
            }
            
        }
        
        unstable {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as UNSTABLE"
            
            // send status to github
            // TODO
        }
        
        failure {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as FAILURE"
            
            // send email notification
            // TODO : setup SMTP server on vm/docker
            //mail to: 'krzysiekmatuszewski@outlook.com',
            // subject: "Pipeline job failure: ${currentBuild.fullDisplayName}",
            // body: "Build URL: ${env.BUILD_URL}"
            
            // send status to github
            // TODO
        }
        
        changed {
            echo 'Job changed state'
        }
        
    }
}
