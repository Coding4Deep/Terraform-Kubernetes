# resource "aws_s3_bucket" "k8s_project_bucket" {
#   bucket = var.bucket_name

#   tags = {
#     Name = var.bucket_name
#   }
#   lifecycle {
#     prevent_destroy = true
#   }

# }

# resource "aws_s3_bucket_versioning" "versioning" {
#   bucket = aws_s3_bucket.k8s_project_bucket.id

#   versioning_configuration {
#     status = var.versioning_enabled ? "Enabled" : "Suspended"
#   }
# }

