-- 初期データ投入（テスト用）
INSERT INTO employee (employee_code, first_name, last_name, email, department, position, salary, hire_date, status) VALUES
('EMP001', 'Taro', 'Yamada', 'taro.yamada@example.com', 'Engineering', 'Senior Engineer', 80000.00, '2020-04-01', 'ACTIVE'),
('EMP002', 'Hanako', 'Suzuki', 'hanako.suzuki@example.com', 'Sales', 'Sales Manager', 75000.00, '2019-09-15', 'ACTIVE'),
('EMP003', 'Ichiro', 'Tanaka', 'ichiro.tanaka@example.com', 'HR', 'HR Specialist', 60000.00, '2021-01-10', 'ACTIVE');

INSERT INTO product (product_code, product_name, category, price, stock_quantity, description, manufacturer, release_date, is_active) VALUES
('PRD001', 'Laptop Pro 15', 'Electronics', 1500.00, 50, 'High-performance laptop', 'TechCorp', '2023-01-15', true),
('PRD002', 'Wireless Mouse', 'Accessories', 35.00, 200, 'Ergonomic wireless mouse', 'Accessories Inc', '2022-06-01', true),
('PRD003', 'USB-C Hub', 'Accessories', 45.00, 150, 'Multi-port USB-C hub', 'ConnectTech', '2023-03-20', true);

INSERT INTO sales (transaction_id, product_code, customer_name, quantity, unit_price, total_amount, sale_date, payment_method, status) VALUES
('TRX001', 'PRD001', 'Customer A', 2, 1500.00, 3000.00, '2024-01-15 10:30:00', 'CREDIT_CARD', 'COMPLETED'),
('TRX002', 'PRD002', 'Customer B', 5, 35.00, 175.00, '2024-01-16 14:20:00', 'CASH', 'COMPLETED'),
('TRX003', 'PRD003', 'Customer C', 3, 45.00, 135.00, '2024-01-17 09:15:00', 'DEBIT_CARD', 'COMPLETED');