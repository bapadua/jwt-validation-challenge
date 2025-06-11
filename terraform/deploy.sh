#!/bin/bash

# ========================================
# JWT Project Terraform Deployment Script
# ========================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if required tools are installed
check_prerequisites() {
    print_header "Checking Prerequisites"
    
    if ! command -v terraform &> /dev/null; then
        print_error "Terraform is not installed"
        echo "Install it from: https://www.terraform.io/downloads.html"
        exit 1
    fi
    print_success "Terraform is installed"
    
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed" 
        echo "Install it from: https://aws.amazon.com/cli/"
        exit 1
    fi
    print_success "AWS CLI is installed"
    
    # Check AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured"
        echo "Run: aws configure"
        exit 1
    fi
    print_success "AWS credentials configured"
}

# Validate terraform.tfvars
validate_config() {
    print_header "Validating Configuration"
    
    if [ ! -f "terraform.tfvars" ]; then
        print_error "terraform.tfvars file not found"
        echo "Copy terraform.tfvars.example to terraform.tfvars and configure it"
        exit 1
    fi
    
    # Check for default password
    if grep -q "ChangeMePlease123!" terraform.tfvars; then
        print_error "Default database password detected!"
        echo "Please change db_password in terraform.tfvars"
        exit 1
    fi
    
    print_success "Configuration validated"
}

# Initialize Terraform
init_terraform() {
    print_header "Initializing Terraform"
    
    terraform init
    print_success "Terraform initialized"
}

# Plan deployment
plan_deployment() {
    print_header "Planning Deployment"
    
    terraform plan -out=tfplan
    print_success "Deployment plan created"
}

# Apply deployment
apply_deployment() {
    print_header "Applying Deployment"
    
    read -p "Do you want to proceed with the deployment? (y/N): " confirm
    if [[ $confirm != [yY] ]]; then
        print_warning "Deployment cancelled"
        exit 0
    fi
    
    terraform apply tfplan
    print_success "Deployment completed successfully!"
}

# Show outputs
show_outputs() {
    print_header "Deployment Outputs"
    
    echo -e "${YELLOW}Backend API URL:${NC}"
    terraform output backend_api_url
    
    echo -e "${YELLOW}Database Endpoint:${NC}"
    terraform output database_endpoint
    
    echo -e "${YELLOW}Lambda Functions:${NC}"
    terraform output lambda_staging_name
    terraform output lambda_production_name
    
    echo -e "${YELLOW}GitHub Actions Secrets:${NC}"
    terraform output github_actions_secrets
}

# Cleanup function
cleanup() {
    if [ -f "tfplan" ]; then
        rm -f tfplan
    fi
}

# Main deployment function
deploy() {
    print_header "JWT Project Infrastructure Deployment"
    
    check_prerequisites
    validate_config
    init_terraform
    plan_deployment
    apply_deployment
    show_outputs
    
    print_success "🎉 Infrastructure deployed successfully!"
    echo ""
    echo -e "${BLUE}Next steps:${NC}"
    echo "1. Configure GitHub Actions secrets with the output values"
    echo "2. Push your backend-challenge code to trigger API deployment"
    echo "3. Push your aws-lambda-jwt code to trigger Lambda deployment"
    echo "4. Monitor the deployments in the AWS Console"
}

# Destroy function
destroy() {
    print_header "Destroying Infrastructure"
    
    print_warning "This will DESTROY ALL resources!"
    print_warning "This action cannot be undone!"
    echo ""
    read -p "Type 'destroy' to confirm: " confirm
    
    if [[ $confirm != "destroy" ]]; then
        print_warning "Destruction cancelled"
        exit 0
    fi
    
    terraform destroy
    print_success "Infrastructure destroyed"
}

# Status function
status() {
    print_header "Infrastructure Status"
    
    if [ ! -f "terraform.tfstate" ]; then
        print_warning "No Terraform state found"
        echo "Infrastructure may not be deployed"
        exit 0
    fi
    
    echo -e "${YELLOW}Current state:${NC}"
    terraform show -json | jq -r '.values.root_module.resources[] | select(.type != "random_id") | "\(.type): \(.name)"' 2>/dev/null || echo "jq not installed - showing raw state"
    
    echo ""
    echo -e "${YELLOW}Resource count:${NC}"
    terraform state list | wc -l
}

# Update function
update() {
    print_header "Updating Infrastructure"
    
    check_prerequisites
    validate_config
    
    terraform plan
    
    read -p "Apply these changes? (y/N): " confirm
    if [[ $confirm == [yY] ]]; then
        terraform apply
        print_success "Infrastructure updated successfully!"
    else
        print_warning "Update cancelled"
    fi
}

# Help function
show_help() {
    echo "JWT Project Terraform Management Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  deploy    - Deploy the complete infrastructure"
    echo "  destroy   - Destroy all infrastructure (DANGEROUS!)"
    echo "  status    - Show current infrastructure status"
    echo "  update    - Update existing infrastructure"
    echo "  plan      - Show what changes would be made"
    echo "  outputs   - Show current outputs"
    echo "  help      - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 deploy      # Deploy everything"
    echo "  $0 status      # Check current status"
    echo "  $0 destroy     # Remove everything"
}

# Trap to cleanup on exit
trap cleanup EXIT

# Main script logic
case "${1:-}" in
    "deploy")
        deploy
        ;;
    "destroy")
        destroy
        ;;
    "status")
        status
        ;;
    "update")
        update
        ;;
    "plan")
        check_prerequisites
        validate_config
        init_terraform
        terraform plan
        ;;
    "outputs")
        show_outputs
        ;;
    "help"|"-h"|"--help")
        show_help
        ;;
    "")
        show_help
        ;;
    *)
        print_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac 