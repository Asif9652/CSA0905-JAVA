CREATE DATABASE IF NOT EXISTS csa09_assignment5
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE csa09_assignment5;

CREATE TABLE IF NOT EXISTS bus_pass (
    pass_id VARCHAR(30) PRIMARY KEY,
    reg_no VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    route_no INT NOT NULL,
    validity_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    INDEX idx_bus_pass_route(route_no),
    INDEX idx_bus_pass_reg(reg_no)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS student (
    id INT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS wallet (
    wallet_id VARCHAR(40) PRIMARY KEY,
    balance DECIMAL(12,2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS demo_users (
    id VARCHAR(80) PRIMARY KEY,
    pwd VARCHAR(80) NOT NULL
) ENGINE=InnoDB;

INSERT INTO wallet(wallet_id,balance) VALUES
('HOSTEL',1000.00),('CANTEEN',500.00)
ON DUPLICATE KEY UPDATE wallet_id=VALUES(wallet_id);
