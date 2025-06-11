output "ecs_task_execution_role_arn" {
  description = "ARN of the ECS task execution role"
  value       = aws_iam_role.ecs_task_execution_role.arn
}

output "ecs_task_role_arn" {
  description = "ARN of the ECS task role"
  value       = aws_iam_role.ecs_task_role.arn
}

output "lambda_execution_role_arn" {
  description = "ARN of the Lambda execution role"
  value       = aws_iam_role.lambda_execution_role.arn
}

output "cicd_user_name" {
  description = "Name of the CI/CD user"
  value       = aws_iam_user.cicd_user.name
}

output "cicd_user_arn" {
  description = "ARN of the CI/CD user"
  value       = aws_iam_user.cicd_user.arn
} 