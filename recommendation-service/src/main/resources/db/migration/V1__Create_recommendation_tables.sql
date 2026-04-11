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

CREATE TABLE IF NOT EXISTS user_activities (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    activity_type VARCHAR(50) NOT NULL,
    session_id VARCHAR(255),
    duration_seconds INTEGER,
    price DECIMAL(10,2),
    quantity INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    context TEXT
);

CREATE INDEX IF NOT EXISTS idx_user_activities_user_id ON user_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_product_id ON user_activities(product_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_activity_type ON user_activities(activity_type);
CREATE INDEX IF NOT EXISTS idx_user_activities_created_at ON user_activities(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_user_activities_user_product ON user_activities(user_id, product_id);
CREATE INDEX IF NOT EXISTS idx_user_activities_session_id ON user_activities(session_id);

CREATE TABLE IF NOT EXISTS user_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(500) NOT NULL,
    category VARCHAR(100),
    price DECIMAL(10,2),
    image_url VARCHAR(1000),
    recommendation_type VARCHAR(50) NOT NULL,
    recommendation_score DECIMAL(10,4) DEFAULT 0.0,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days'),
    clicked BOOLEAN DEFAULT FALSE,
    clicked_at TIMESTAMP,
    purchased BOOLEAN DEFAULT FALSE,
    purchased_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_recommendations_user_id ON user_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_product_id ON user_recommendations(product_id);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_type ON user_recommendations(recommendation_type);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_score ON user_recommendations(recommendation_score DESC);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_user_clicked ON user_recommendations(user_id, clicked);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_expires_at ON user_recommendations(expires_at);
CREATE INDEX IF NOT EXISTS idx_user_recommendations_user_type_score ON user_recommendations(user_id, recommendation_type, recommendation_score DESC);
