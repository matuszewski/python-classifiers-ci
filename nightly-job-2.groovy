def updatePRStatus(String status) { // uses GitHub Integration Plugin https://www.jenkins.io/doc/pipeline/steps/github-pullrequest/     (gitHubPRStatus : )
  if (!env.GITHUB_PR_COND_REF) {
    echo "manually triggered"
    return;
  }
  try {
  switch(status) {
        case 'running':
            script {
                gitHubPRStatus githubPRMessage("Jenkins job is running...")
            } // end of script
            break
        
        case 'success':
            script {
                // Add a comment to GitHub PR
                githubPRComment comment: githubPRMessage("<h2>Jenkins job ${JOB_NAME} ended with SUCCESS. Build: ${BUILD_URL}")

                // Set status to GitHub PR check
                githubPRStatusPublisher (
                    statusMsg: [
                        content: "jenkins build succeed"
                        ],
                    unstableAs: 'FAILURE',
                    buildMessage: [
                        failureMsg: [content: 'build failed!'],
                        successMsg: [content: 'build succeeded'],
                        ],
                    statusVerifier: [buildStatus: 'SUCCESS']
                )
            } // end of script
            break;
    
        default:
            break;
  } // end of switch
  } catch (Exception e) {
    echo 'Exception occured on GitHub PR request status' + e.toString()
  }
} // end of updatePRStatus()


pipeline {
    agent { label 'docker-slave' }
    
    // removed trigerring from jenkinsfile
    
    
    
    stages {
        
        stage('Set running status on GitHub') {
            steps {
                script {
                    if(env.GITHUB_PR_COND_REF) {
                        echo '[!] triggered by polling'
                        updatePRStatus('running')
                    }
                    else {
                        echo '[!] manually trigered'
                    }
                }
            }
        }
        
        stage('Checkout from GitHub') {
            steps {
                echo '[>] Trying to checkout from git repository...'
                git branch: 'main',
                    credentialsId: 'matuszewski-gh-pat',
                    url: 'https://github.com/matuszewski/python-classifiers.git'
                sh "python3 classifiers.py"
                echo 'Build done!'
            }
        }
        
        // TODO: SonarQube scan
        //stage('SonarQube Analysis') {
        //    def scannerHome = tool 'SONAR_QUBE';
        //    withSonarQubeEnv() {
        //        sh "${scannerHome}/bin/sonar-scanner"
        //    }
        //}
        
    }

    post {
        always {
            echo 'JOB FINISHED'
        }
        success {
            echo "Pipeline job ${env.JOB_NAME} failed, marked as SUCCESS"

            // publish report and archive
            //publishHTML([allowMissing: true, alwaysLinkToLastBuild: false, keepAll: false, reportDir: '.', reportFiles: 'index.html', reportName: 'HTML Report', reportTitles: 'HTML Report of successful job execution'])
            //archiveArtifacts allowEmptyArchive: true, artifacts: 'index.html', followSymlinks: false
            
            // clean up the workspace
            //deleteDir()
            
            // set status on gh pr
            updatePRStatus('success')
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
            
            // set status on gh pr
            updatePRStatus('failure')
        }
        changed {
            echo 'Job changed state'
        }
    }
}
