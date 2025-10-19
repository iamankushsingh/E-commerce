-- Order Management Sample Data
-- Tables are automatically created by JPA from @Entity classes

-- Insert sample cart for testing (user_id = 1)
INSERT INTO carts (user_id, total_amount, created_at) VALUES (1, 0.00, NOW());

-- Sample data can be added manually through MySQL Workbench for testing 