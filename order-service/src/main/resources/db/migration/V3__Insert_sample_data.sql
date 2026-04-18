INSERT INTO orders (order_number, user_id, status, total_amount, currency, shipping_address, billing_address, created_at, updated_at, saga_id, saga_started_at, saga_completed_at) VALUES
('ORD-2024-001', 'user-123', 'CONFIRMED', 1999.98, 'USD', '123 Main St, New York, NY 10001', '123 Main St, New York, NY 10001', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days', 'saga-001', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('ORD-2024-002', 'user-456', 'PENDING', 249.99, 'USD', '456 Oak Ave, Los Angeles, CA 90001', '456 Oak Ave, Los Angeles, CA 90001', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day', 'saga-002', CURRENT_TIMESTAMP - INTERVAL '1 day', NULL),
('ORD-2024-003', 'user-789', 'CANCELLED', 899.99, 'USD', '789 Pine Rd, Chicago, IL 60601', '789 Pine Rd, Chicago, IL 60601', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days', 'saga-003', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days');

INSERT INTO order_items (order_id, product_id, product_name, price, quantity, image_url) VALUES
(1, 'prod-1', 'iPhone 15 Pro', 999.99, 2, 'https://example.com/iphone15.jpg'),
(2, 'prod-3', 'AirPods Pro', 249.99, 1, 'https://example.com/airpods.jpg'),
(3, 'prod-4', 'Samsung Galaxy S24', 899.99, 1, 'https://example.com/galaxy.jpg');
