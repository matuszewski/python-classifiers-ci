pipeline {
    agent { label 'docker-slave' }
  
    triggers {
        // trigger with github polling on cron: "at 02:00 on every day-of-week from Monday through Friday"
        pollSCM('0 2 * * 1-5')
    }
    
    stages {

        stage('Checkout from GitHub') {
            steps {
                echo '[>] Trying to checkout from git repository...'
                git branch: 'main',
                    credentialsId: 'matuszewski-gh-pat',
                    url: 'https://github.com/matuszewski/python-classifiers.git'
                sh "python3 classifiers.py"
                echo 'Build'
            }
        }
        
    }

    post {
        always {
            echo 'JOB FINISHED'
        }
        success {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as SUCCESS"

            // publish report and archive
            publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: '.', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'HTML Report of successful job execution'])
            archiveArtifacts allowEmptyArchive: true, artifacts: 'index.html', followSymlinks: false
            
            // clean up the workspace
            deleteDir()
        }
        unstable {
            echo "Pipeline job ${env.JOB_NAME} finished, marked as UNSTABLE"
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
