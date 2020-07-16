CREATE TABLE IF NOT EXISTS cart
(
    customer_id VARCHAR(40) NOT NULL,
    PRIMARY KEY (customer_id)
);

CREATE TABLE IF NOT EXISTS cart_item
(
    customer_id VARCHAR(40)   NOT NULL,
    item_id     VARCHAR(40)   NOT NULL,
    quantity    INT           NOT NULL,
    unit_price  DECIMAL(8, 2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES `cart` (customer_id) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, item_id)
);


