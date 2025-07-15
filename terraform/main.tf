# provider "aws" {
#   region = "us-east-1"
# }

# # Reference existing default VPC
# data "aws_vpc" "default" {
#   default = true
# }

# # Reference existing k8s-project VPC
# data "aws_vpc" "k8s_project" {
#   filter {
#     name   = "tag:Name"
#     values = ["k8s-project-vpc"]
#   }
# }

# # Get route tables for both VPCs
# data "aws_route_tables" "default_routes" {
#   vpc_id = data.aws_vpc.default.id
# }

# data "aws_route_tables" "k8s_routes" {
#   vpc_id = data.aws_vpc.k8s_project.id
# }

# # Create VPC peering connection
# resource "aws_vpc_peering_connection" "peering" {
#   vpc_id      = data.aws_vpc.default.id
#   peer_vpc_id = data.aws_vpc.k8s_project.id
#   auto_accept = true

#   tags = {
#     Name = "default-k8s-peering"
#   }
# }

# # Add routes from default VPC to k8s VPC
# resource "aws_route" "default_to_k8s" {
#   for_each = toset(data.aws_route_tables.default_routes.ids)
  
#   route_table_id            = each.value
#   destination_cidr_block    = data.aws_vpc.k8s_project.cidr_block
#   vpc_peering_connection_id = aws_vpc_peering_connection.peering.id
# }

# # Add routes from k8s VPC to default VPC
# resource "aws_route" "k8s_to_default" {
#   for_each = toset(data.aws_route_tables.k8s_routes.ids)
  
#   route_table_id            = each.value
#   destination_cidr_block    = data.aws_vpc.default.cidr_block
#   vpc_peering_connection_id = aws_vpc_peering_connection.peering.id
# }





provider "aws" {
  alias  = "requester"
  region = "us-east-1"
  access_key = var.requester_access_key
  secret_key = var.requester_secret_key
}

provider "aws" {
  alias  = "accepter"
  region = "us-east-1"
  access_key =  var.acceptor_access_key
  secret_key = var.acceptor_secret_key
}


data "aws_vpc" "default" {
  provider = aws.requester
  default  = true
}

data "aws_vpc" "k8s_project" {
  provider = aws.accepter
  filter {
    name   = "tag:Name"
    values = [var.acceptor_vpc_name] 
  }
}


resource "aws_vpc_peering_connection" "peering" {
  provider      = aws.requester
  vpc_id        = data.aws_vpc.default.id
  peer_vpc_id   = data.aws_vpc.k8s_project.id
  peer_owner_id = var.acceptor_account_id # Replace with actual Account B ID
  auto_accept   = false

  tags = {
    Name = "default-to-k8s-peering"
  }
}



resource "aws_vpc_peering_connection_accepter" "accept" {
  provider                  = aws.accepter
  vpc_peering_connection_id = aws_vpc_peering_connection.peering.id
  auto_accept               = true

  tags = {
    Name = "accept-default-to-k8s"
  }
}


data "aws_route_tables" "default_routes" {
  provider = aws.requester
  vpc_id   = data.aws_vpc.default.id
}

resource "aws_route" "default_to_k8s" {
  provider = aws.requester

  for_each = toset(data.aws_route_tables.default_routes.ids)

  route_table_id            = each.value
  destination_cidr_block    = data.aws_vpc.k8s_project.cidr_block
  vpc_peering_connection_id = aws_vpc_peering_connection.peering.id
}



data "aws_route_tables" "k8s_routes" {
  provider = aws.accepter
  vpc_id   = data.aws_vpc.k8s_project.id
}

resource "aws_route" "k8s_to_default" {
  provider = aws.accepter

  for_each = toset(data.aws_route_tables.k8s_routes.ids)

  route_table_id            = each.value
  destination_cidr_block    = data.aws_vpc.default.cidr_block
  vpc_peering_connection_id = aws_vpc_peering_connection.peering.id
}
