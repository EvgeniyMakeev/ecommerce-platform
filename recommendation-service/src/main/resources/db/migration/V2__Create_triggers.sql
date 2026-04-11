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

CREATE OR REPLACE FUNCTION update_clicked_at_column()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.clicked = TRUE AND OLD.clicked = FALSE THEN
        NEW.clicked_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_recommendations_clicked_at 
    BEFORE UPDATE ON user_recommendations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_clicked_at_column();

CREATE OR REPLACE FUNCTION update_purchased_at_column()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.purchased = TRUE AND OLD.purchased = FALSE THEN
        NEW.purchased_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_recommendations_purchased_at 
    BEFORE UPDATE ON user_recommendations 
    FOR EACH ROW 
    EXECUTE FUNCTION update_purchased_at_column();
