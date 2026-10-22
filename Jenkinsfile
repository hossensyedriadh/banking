pipeline {
    agent any

    environment {
        NEXUS_OSS_REPO = 'cbs'
        GITHUB_REPO = 'cbs'
        GITHUB_API_URL = "https://api.github.com/repos/hossensyedriadh/${GITHUB_REPO}/deployments"
        DEPLOYMENT_ID = ''
        ENVIRONMENT = 'staging'
        K8S_NAMESPACE = 'banking-cbs-staging'
        NEXUS_REGISTRY = "nexus.cerberus.internal:7081"
        NEXUS_REGISTRY_IP = "192.168.59.10"
        NEXUS_REGISTRY_DNS = "nexus.cerberus.internal"
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    cleanWs()

                    withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                        env.GIT_TERMINAL_PROMPT = 0
                        sh """
                            git config --global credential.helper store
                            echo "https://x-access-token:${GITHUB_PAT}@github.com" > ~/.git-credentials
                        """

                        if (!fileExists(".git")) {
                            sh "git clone --branch staging https://github.com/hossensyedriadh/${GITHUB_REPO}.git ."
                        } else {
                            sh """
                                git fetch origin
                                git reset --hard origin/staging
                            """
                        }
                    }
                }
            }
        }

        stage('Create GitHub Deployment') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                        def payload = [
                            ref: "staging",
                            task: "deploy",
                            description: "Deployment from Cerberus Jenkins",
                            auto_merge: false,
                            environment: env.ENVIRONMENT,
                            transient_environment: true
                        ]

                        def jsonBody = groovy.json.JsonOutput.toJson(payload)

                        def jsonResponse = sh(script: """
                            curl --fail -s -X POST \
                            -H "Authorization: Bearer ${GITHUB_PAT}" \
                            -H "Accept: application/vnd.github+json" \
                            -H "Content-Type: application/json" \
                            -H "X-GitHub-Api-Version: 2026-03-10" \
                            -d '${jsonBody}' \
                            ${GITHUB_API_URL}
                        """, returnStdout: true).trim()

                        echo "RAW RESPONSE: ${jsonResponse}"

                        def json = new groovy.json.JsonSlurper().parseText(jsonResponse)
                        DEPLOYMENT_ID = json.id.toString()
                        echo "Deployment ID: ${DEPLOYMENT_ID}"
                    }
                }
            }
        }

        stage('Update GitHub Deployment to in_progress') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                        sh """
                            curl -X POST -H "Authorization: Bearer ${GITHUB_PAT}" \
                            -H "Accept: application/vnd.github+json" \
                            -H "Content-Type: application/json" \
                            -H "X-GitHub-Api-Version: 2026-03-10" \
                            -d '{
                                "state": "in_progress",
                                "deployment_id": "${DEPLOYMENT_ID}",
                                "description": "Deployment is in progress",
                                "environment": "${ENVIRONMENT}",
                                "auto_inactive": true
                            }' \
                            ${GITHUB_API_URL}/${DEPLOYMENT_ID}/statuses
                        """
                    }
                }
            }
        }

        stage('Prepare Internal CA') {
            steps {
                withCredentials([file(credentialsId: 'cerberus-root-ca', variable: 'CA_FILE')]) {
                    sh "cp $CA_FILE cerberus-root-ca.crt"
                }
            }
        }

        stage('Build Docker Image') {
            environment {
                ORG_GRADLE_PROJECT_nexusBuildUser = credentials('nexus-oss-build-user')
                ORG_GRADLE_PROJECT_nexusBuildPassword = credentials('nexus-oss-build-pwd')
            }

            steps {
                script {
                    def commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def imageName = "${env.NEXUS_REGISTRY}/${env.NEXUS_OSS_REPO}/${env.NEXUS_OSS_REPO}-${commit}"

                    sh """
                        pwd
                        ls -la
                        docker build \
                            --add-host="${env.NEXUS_REGISTRY_DNS}:${env.NEXUS_REGISTRY_IP}" \
                            --build-arg ORG_GRADLE_PROJECT_nexusBuildUser \
                            --build-arg ORG_GRADLE_PROJECT_nexusBuildPassword \
                            -t ${imageName} .
                    """
                }
            }
        }

        stage('Push Docker Image to Nexus OSS') {
            steps {
                script {
                    def commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def imageName = "${env.NEXUS_REGISTRY}/${env.NEXUS_OSS_REPO}/${env.NEXUS_OSS_REPO}-${commit}"

                    withCredentials([string(credentialsId: 'nexus-docker-pwd', variable: 'NEXUS_OSS_TOKEN'),
                                    string(credentialsId: 'nexus-docker-user', variable: 'NEXUS_OSS_USER')]) {
                        sh """
                            echo "\$NEXUS_OSS_TOKEN" | docker login ${env.NEXUS_REGISTRY} -u "\$NEXUS_OSS_USER" --password-stdin
                            docker push ${imageName}
                        """
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    def commit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    def imageName = "${env.NEXUS_REGISTRY}/${env.NEXUS_OSS_REPO}/${env.NEXUS_OSS_REPO}-${commit}"

                    withCredentials([file(credentialsId: 'cerberus-microk8s-kubeconfig', variable: 'KUBECONFIG')]) {
                        sh """
                            echo "Deploying image: ${imageName}"

                            sed "s|__IMAGE__|${imageName}|g" k8s/staging/deployment.yaml | microk8s kubectl apply -n ${K8S_NAMESPACE} -f -
                            microk8s kubectl apply -n ${K8S_NAMESPACE} -f k8s/staging/service-internal.yaml
                            microk8s kubectl apply -n ${K8S_NAMESPACE} -f k8s/staging/service-external.yaml

                            echo "Waiting for rollout to complete..."

                            if ! microk8s kubectl rollout status deployment/cbs -n ${K8S_NAMESPACE} --timeout=120s; then
                                echo "Rollout failed."

                                echo "Fetching debug info..."
                                microk8s kubectl get pods -n ${K8S_NAMESPACE} -o wide
                                microk8s kubectl describe deployment cbs -n ${K8S_NAMESPACE}

                                echo "Checking rollout history..."
                                if microk8s kubectl rollout history deployment/cbs -n ${K8S_NAMESPACE} | grep -q "REVISION"; then
                                    echo "Previous revision found. Rolling back..."
                                    microk8s kubectl rollout undo deployment/cbs -n ${K8S_NAMESPACE}
                                else
                                    echo "No previous revision found. Skipping rollback."
                                fi

                                exit 1
                            fi
                            echo "Deployment successful."
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                sh """
                    curl -X POST -H "Authorization: Bearer ${GITHUB_PAT}" \
                    -H "Accept: application/vnd.github+json" \
                    -H "Content-Type: application/json" \
                    -H "X-GitHub-Api-Version: 2026-03-10" \
                    -d '{
                        "state": "success",
                        "deployment_id": "${DEPLOYMENT_ID}",
                        "description": "Deployment finished successfully",
                        "environment": "${ENVIRONMENT}",
                        "auto_inactive": true
                    }' \
                    ${GITHUB_API_URL}/${DEPLOYMENT_ID}/statuses
                """
            }
        }

        failure {
            withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                sh """
                    curl -X POST -H "Authorization: Bearer ${GITHUB_PAT}" \
                    -H "Accept: application/vnd.github+json" \
                    -H "Content-Type: application/json" \
                    -H "X-GitHub-Api-Version: 2026-03-10" \
                    -d '{
                        "state": "failure",
                        "deployment_id": "${DEPLOYMENT_ID}",
                        "description": "Deployment failed",
                        "environment": "${ENVIRONMENT}",
                        "auto_inactive": true
                    }' \
                    ${GITHUB_API_URL}/${DEPLOYMENT_ID}/statuses
                """
            }
        }

        aborted {
            withCredentials([string(credentialsId: 'github_hossensyedriadh', variable: 'GITHUB_PAT')]) {
                sh """
                    curl -X POST -H "Authorization: Bearer ${GITHUB_PAT}" \
                    -H "Accept: application/vnd.github+json" \
                    -H "Content-Type: application/json" \
                    -H "X-GitHub-Api-Version: 2026-03-10" \
                    -d '{
                        "state": "inactive",
                        "deployment_id": "${DEPLOYMENT_ID}",
                        "description": "Deployment aborted",
                        "environment": "${ENVIRONMENT}",
                        "auto_inactive": true
                    }' \
                    ${GITHUB_API_URL}/${DEPLOYMENT_ID}/statuses
                """
            }
        }
    }
}
