resource "aws_subnet" "private_subnet" {
  vpc_id                  = var.vpc_id
  cidr_block              = var.private_subnet_cidr_block
  availability_zone       = var.private_subnet_az
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.vpc_name}-private-subnet"
  }
}

resource "aws_eip" "private_nat_eip" {
  tags = {
    Name = "${var.vpc_name}-private-nat-eip"
  }
}

resource "aws_nat_gateway" "private_nat_gateway" {
  allocation_id = aws_eip.private_nat_eip.id
  subnet_id     = var.public_subnet_id

  tags = {
    Name = "${var.vpc_name}-private-nat-gateway"
  }
}

resource "aws_route_table" "private_nat_rt" {
  vpc_id = var.vpc_id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.private_nat_gateway.id
  }
  tags = {
    Name = "${var.vpc_name}-private-nat-rt"
  }
}

resource "aws_route_table_association" "private_subnet_association" {
  subnet_id      = aws_subnet.private_subnet.id
  route_table_id = aws_route_table.private_nat_rt.id
}
