CREATE DATABASE IF NOT EXISTS ecommerce_recommendations;

CREATE TABLE IF NOT EXISTS popular_products (
    id BIGSERIAL PRIMARY KEY,
    product_id VARCHAR(255) UNIQUE NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    product_description TEXT,
    category VARCHAR(100),
    price DECIMAL(10,2),
    image_url VARCHAR(1000),
    view_count BIGINT DEFAULT 0,
    purchase_count BIGINT DEFAULT 0,
    rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0.0 AND rating <= 5.0),
    popularity_score DECIMAL(10,4) DEFAULT 0.0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_popular_products_product_id ON popular_products(product_id);
CREATE INDEX IF NOT EXISTS idx_popular_products_category ON popular_products(category);
CREATE INDEX IF NOT EXISTS idx_popular_products_popularity_score ON popular_products(popularity_score DESC);
CREATE INDEX IF NOT EXISTS idx_popular_products_view_count ON popular_products(view_count DESC);
CREATE INDEX IF NOT EXISTS idx_popular_products_purchase_count ON popular_products(purchase_count DESC);
CREATE INDEX IF NOT EXISTS idx_popular_products_rating ON popular_products(rating DESC);
CREATE INDEX IF NOT EXISTS idx_popular_products_last_updated ON popular_products(last_updated);

CREATE OR REPLACE FUNCTION update_last_updated_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_popular_products_last_updated 
    BEFORE UPDATE ON popular_products 
    FOR EACH ROW 
    EXECUTE FUNCTION update_last_updated_column();
