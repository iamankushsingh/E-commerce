-- Migration script to add discount fields to orders table
-- Run this in MySQL Workbench or your database tool

-- Add discount_amount column
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 AFTER shipping_amount;

-- Add coupon_code column  
ALTER TABLE orders ADD COLUMN coupon_code VARCHAR(50) NULL AFTER discount_amount;

-- Update existing final_amount calculation to account for discount
UPDATE orders SET final_amount = total_amount + tax_amount + shipping_amount - discount_amount WHERE discount_amount > 0; 