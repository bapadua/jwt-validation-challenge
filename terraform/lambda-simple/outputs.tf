# ========================================
# Outputs do JWT Lambda
# ========================================

output "lambda_function_name" {
  description = "Nome da função Lambda"
  value       = aws_lambda_function.jwt_lambda.function_name
}

output "lambda_function_arn" {
  description = "ARN da função Lambda"
  value       = aws_lambda_function.jwt_lambda.arn
}

output "lambda_function_url" {
  description = "URL pública da função Lambda"
  value       = aws_lambda_function_url.jwt_lambda_url.function_url
}

output "lambda_invoke_arn" {
  description = "ARN para invocar a função"
  value       = aws_lambda_function.jwt_lambda.invoke_arn
}

output "instructions" {
  description = "Instruções para usar o Lambda"
  value = <<EOF

🎉 JWT Lambda criado com sucesso!

📡 URL da Função: ${aws_lambda_function_url.jwt_lambda_url.function_url}

🧪 Para testar:
curl -X POST ${aws_lambda_function_url.jwt_lambda_url.function_url} \
  -H "Content-Type: application/json" \
  -d '{"test": "hello"}'

📋 Para fazer deploy do código real:
1. Compile seu JAR
2. aws lambda update-function-code --function-name ${aws_lambda_function.jwt_lambda.function_name} --zip-file fileb://seu-arquivo.jar

EOF
} 