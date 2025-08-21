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