CREATE DATABASE IF NOT EXISTS java_assignment5;
USE java_assignment5;

CREATE TABLE IF NOT EXISTS bus_pass (
    pass_id VARCHAR(30) PRIMARY KEY,
    reg_no VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    route_no INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    pwd VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet (
    wallet_id INT PRIMARY KEY,
    balance DECIMAL(12,2) NOT NULL
);

-- Demo data only. Do not store real plain-text passwords in production.
INSERT IGNORE INTO users(id, pwd) VALUES ('student1', 'pass123');
INSERT IGNORE INTO wallet(wallet_id, balance) VALUES (1, 5000.00), (2, 2000.00);
