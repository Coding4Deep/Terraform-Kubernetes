resource "tls_private_key" "private_ssh_key" {
  algorithm = "RSA"
  rsa_bits  = 4096
}
resource "aws_key_pair" "public_ssh_key" {
  key_name   = var.pem_file_name
  public_key = tls_private_key.private_ssh_key.public_key_openssh
}
resource "local_file" "private_key_pem" {
  content         = tls_private_key.private_ssh_key.private_key_pem
  filename        = "${pathexpand("~")}/${var.pem_file_name}.pem"
  file_permission = "0400"
}

