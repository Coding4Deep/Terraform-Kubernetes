module "PemFile" {
  source = "./PemFile"
  pem_file_name = var.pem_file_name
}

module "PublicEC2" {
  source = "./PublicEC2"

  public_instances = var.public_instances
  pem_file_name    = var.pem_file_name
  public_subnet_id = var.public_subnet_id
  public_sg_id     = var.public_sg_id
  volume_size      = var.volume_size
  volume_type      = var.volume_type
}


module "PrivateEC2" {
  source = "./PrivateEC2"

  private_instances = var.private_instances
  pem_file_name     = var.pem_file_name
  private_subnet_id = var.private_subnet_id
  private_sg_id     = var.private_sg_id
  volume_size       = var.volume_size
  volume_type       = var.volume_type
} 