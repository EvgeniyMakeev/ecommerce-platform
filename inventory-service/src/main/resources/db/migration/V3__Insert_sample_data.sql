INSERT INTO inventory (product_id, product_name, quantity, location, sku, created_at, updated_at) VALUES
('prod-1', 'iPhone 15 Pro', 50, 'warehouse-1', 'APPL-IP15PRO-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-2', 'MacBook Pro 16"', 30, 'warehouse-1', 'APPL-MBP16-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-3', 'AirPods Pro', 100, 'warehouse-2', 'APPL-APP-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-4', 'Samsung Galaxy S24', 45, 'warehouse-1', 'SAMS-GS24-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-5', 'Sony WH-1000XM5', 25, 'warehouse-2', 'SONY-WH1000XM5-001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO inventory_reservations (product_id, order_id, quantity, status, created_at, updated_at) VALUES
('prod-1', 'order-test-001', 2, 'CONFIRMED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('prod-2', 'order-test-002', 1, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
