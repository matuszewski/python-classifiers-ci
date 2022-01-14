pipeline {
    
    agent {
        label 'docker-slave'
    }
    
    parameters {
        string(name: 'BRANCH', defaultValue: "main", description: "Please provide the branch name (only for manual, ad-hoc builds)")
    }
    
    environment {
        BRANCH="${GITHUB_PR_SOURCE_BRANCH ?: params.BRANCH}"
    }
    
    stages {
        stage('Check config') {
            steps {
                // check python version
                sh 'python --version'
                sh 'python3 --version'

                // check pip version
                sh 'pip --version' 
                
                // display env variable GITHUB_PR_SOURCE_BRANCH
                echo "${GITHUB_PR_SOURCE_BRANCH}"
            }
        }
        
        stage('Checkout from GitHub') {
            steps {
                
                echo 'Trying to checkout from git repository...'
                git branch: 'main',
                    credentialsId: 'matuszewski-gh-pat',
                    url: 'https://github.com/matuszewski/python-classifiers.git'
                
                echo 'Trying to run classifiers.py...'
                sh "python3 classifiers.py"
            }
        }
        
        stage('Info') {
            steps {
                // send job pending status to github
                // -- with setGitHubPullRequestStatus plugin:
                setGitHubPullRequestStatus context: "docker",
                                           message: "${JOB_BASE_NAME} Build ${BUILD_NUMBER} build pending",
                                           state: 'PENDING'
                // NOTE! It must have some variable to link it with the PR, hence the environment section above^^^
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
            //script {
            //    try {
            //        echo "Trying to send the job status to GitHub"
            //        githubNotify context: 'Notification key', description: 'This is a shorted example',  status: 'SUCCESS'

            //    } catch (Exception e) {
            //        echo 'Exception occurred while trying to send the status: ' + e.toString()
            //    }
            //}
            
            //githubNotify account: 'raul-arabaolaza', context: 'Final Test', credentialsId: 'raul-github',
  //  description: 'This is an example', repo: 'acceptance-test-harness', sha: '0b5936eb903d439ac0c0bf84940d73128d5e9487'
  //  , status: 'SUCCESS', targetUrl: 'https://my-jenkins-instance.com'
    
            

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
