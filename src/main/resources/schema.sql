-- 従業員テーブル
DROP TABLE IF EXISTS employee;
CREATE TABLE employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    department VARCHAR(100),
    position VARCHAR(100),
    salary DECIMAL(10, 2),
    hire_date DATE,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 商品テーブル
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10, 2),
    stock_quantity INT,
    description TEXT,
    manufacturer VARCHAR(100),
    release_date DATE,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 売上テーブル
DROP TABLE IF EXISTS sales;
CREATE TABLE sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(50) NOT NULL UNIQUE,
    product_code VARCHAR(50),
    customer_name VARCHAR(200),
    quantity INT,
    unit_price DECIMAL(10, 2),
    total_amount DECIMAL(10, 2),
    sale_date DATETIME,
    payment_method VARCHAR(50),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX idx_employee_code ON employee(employee_code);
CREATE INDEX idx_employee_email ON employee(email);
CREATE INDEX idx_product_code ON product(product_code);
CREATE INDEX idx_product_category ON product(category);
CREATE INDEX idx_sales_transaction ON sales(transaction_id);
CREATE INDEX idx_sales_date ON sales(sale_date);

-- JasperReports用のemployeesテーブル（既存のemployeeテーブルとは別）
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
    employee_id VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    salary DECIMAL(10,2),
    hire_date DATE
);

-- サンプルデータ挿入
INSERT INTO employees (employee_id, first_name, last_name, email, department, salary, hire_date) VALUES
('EMP001', 'John', 'Doe', 'john.doe@example.com', 'Engineering', 85000.00, '2020-01-15'),
('EMP002', 'Jane', 'Smith', 'jane.smith@example.com', 'Marketing', 75000.00, '2019-03-22'),
('EMP003', 'Michael', 'Johnson', 'michael.j@example.com', 'Sales', 70000.00, '2021-06-10'),
('EMP004', 'Emily', 'Brown', 'emily.brown@example.com', 'Engineering', 95000.00, '2018-11-05'),
('EMP005', 'David', 'Wilson', 'david.wilson@example.com', 'HR', 65000.00, '2020-08-17'),
('EMP006', 'Sarah', 'Davis', 'sarah.davis@example.com', 'Finance', 80000.00, '2019-12-01'),
('EMP007', 'Robert', 'Miller', 'robert.m@example.com', 'Engineering', 90000.00, '2017-09-20'),
('EMP008', 'Lisa', 'Garcia', 'lisa.garcia@example.com', 'Marketing', 72000.00, '2021-02-14'),
('EMP009', 'James', 'Martinez', 'james.m@example.com', 'Sales', 68000.00, '2020-05-30'),
('EMP010', 'Jennifer', 'Anderson', 'jennifer.a@example.com', 'HR', 70000.00, '2019-07-12');