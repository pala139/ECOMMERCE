CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
    phone_number VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    zip_code VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);
CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL
);
CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INT NOT NULL,
    category_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT ck_stock CHECK (stock_quantity >= 0),
    CONSTRAINT ck_price CHECK (price > 0),
    INDEX idx_product_category (category_id),
    INDEX idx_product_price (price)
);
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(255) NOT NULL,
    idempotency_key VARCHAR(128) COLLATE utf8mb4_bin,
    request_hash VARCHAR(64),
    shipping_address VARCHAR(255),
    shipping_city VARCHAR(255),
    shipping_state VARCHAR(255),
    shipping_zip_code VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_order_request UNIQUE (user_id,idempotency_key)
) DEFAULT CHARSET=utf8mb4;
CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT ck_item_qty CHECK (quantity > 0)
);
