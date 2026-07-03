#!/bin/bash
set -e

# Configuration
AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID="603831738253"
ECR_REPO_NAME="monew/backend"
ECS_CLUSTER_NAME="monew-cluster"
ECS_SERVICE_NAME="monew-backend-service"
ECS_TASK_FAMILY="monew-backend-task"
VPC_ID="vpc-025a832e7c43cd4a2"
SUBNETS="subnet-0ef477548902c2587,subnet-0a2db7ca548c40824,subnet-01217f7d6e5fef34b,subnet-0c2615dc162672222"

echo "================================================="
echo "🚀 Monew ECS Backend Initial Setup & Deploy"
echo "================================================="

# 1. Create a dedicated Security Group for port 8080
echo "1️⃣ Creating Security Group for port 8080..."
SG_NAME="monew-backend-sg"
SG_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=$SG_NAME" "Name=vpc-id,Values=$VPC_ID" --query "SecurityGroups[0].GroupId" --output text)

if [ "$SG_ID" = "None" ] || [ -z "$SG_ID" ]; then
    SG_ID=$(aws ec2 create-security-group \
        --group-name "$SG_NAME" \
        --description "Security group for Monew Spring Boot backend" \
        --vpc-id "$VPC_ID" \
        --query "GroupId" \
        --output text)
    echo "Created new Security Group: $SG_ID"
    
    # Authorize port 8080 ingress
    aws ec2 authorize-security-group-ingress \
        --group-id "$SG_ID" \
        --protocol tcp \
        --port 8080 \
        --cidr 0.0.0.0/0
    echo "Allowed port 8080 ingress from anywhere (0.0.0.0/0)"
else
    echo "Security Group already exists: $SG_ID"
fi

# 2. Build local Spring Boot jar
echo "2️⃣ Building Spring Boot app..."
chmod +x gradlew
./gradlew bootJar -x test

# 3. Authenticate Docker with ECR
echo "3️⃣ Authenticating Docker with ECR..."
aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# 4. Build and Push Docker image
echo "4️⃣ Building and Pushing Docker Image..."
IMAGE_URI="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:latest"
docker build -t "$IMAGE_URI" .
docker push "$IMAGE_URI"
echo "Image pushed successfully: $IMAGE_URI"

# 5. Register Task Definition
echo "5️⃣ Registering Task Definition..."
aws ecs register-task-definition \
    --cli-input-json file://.aws/task-definition.json \
    --region "$AWS_REGION" > /dev/null
echo "Task Definition registered successfully."

# 6. Create ECS Service
echo "6️⃣ Creating ECS Service..."
SERVICE_ARN=$(aws ecs describe-services --cluster "$ECS_CLUSTER_NAME" --services "$ECS_SERVICE_NAME" --region "$AWS_REGION" --query "services[0].serviceArn" --output text)

if [ "$SERVICE_ARN" = "None" ] || [ -z "$SERVICE_ARN" ]; then
    aws ecs create-service \
        --cluster "$ECS_CLUSTER_NAME" \
        --service-name "$ECS_SERVICE_NAME" \
        --task-definition "$ECS_TASK_FAMILY" \
        --desired-count 1 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
        --region "$AWS_REGION" > /dev/null
    echo "ECS Service '$ECS_SERVICE_NAME' created successfully!"
else
    echo "ECS Service '$ECS_SERVICE_NAME' already exists. We will let GitHub Actions update it."
fi

echo "================================================="
echo "✅ Initial Setup Completed successfully!"
echo "Now you can push changes to GitHub to trigger Actions!"
echo "================================================="
