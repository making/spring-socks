CREATE TABLE IF NOT EXISTS `order`
(
    order_id                 VARCHAR(40) NOT NULL,
    customer_id              VARCHAR(40) NOT NULL,
    customer_first_name      VARCHAR(64) NOT NULL,
    customer_last_name       VARCHAR(64) NOT NULL,
    customer_username        VARCHAR(64) NOT NULL,
    address_number           VARCHAR(64) NOT NULL,
    address_street           VARCHAR(64) NOT NULL,
    address_city             VARCHAR(64) NOT NULL,
    address_postcode         VARCHAR(32) NOT NULL,
    address_country          VARCHAR(32) NOT NULL,
    card_long_num            VARCHAR(64) NOT NULL,
    card_expires             DATE        NOT NULL,
    card_ccv                 VARCHAR(4)  NOT NULL,
    shipment_carrier         VARCHAR(10) NOT NULL,
    shipment_tracking_number VARCHAR(40) NOT NULL,
    shipment_delivery_date   DATE        NOT NULL,
    date                     TIMESTAMP   NOT NULL,
    PRIMARY KEY (order_id)
);

CREATE TABLE IF NOT EXISTS order_status
(
    order_id   VARCHAR(40) NOT NULL,
    status     INT         NOT NULL,
    updated_at TIMESTAMP   NOT NULL DEFAULT now(),
    FOREIGN KEY (order_id) REFERENCES `order` (order_id) ON DELETE CASCADE,
    INDEX (order_id, updated_at)
);


CREATE TABLE IF NOT EXISTS order_item
(
    order_id   VARCHAR(40)   NOT NULL,
    item_id    VARCHAR(40)   NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(8, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order` (order_id) ON DELETE CASCADE,
    PRIMARY KEY (order_id, item_id)
);


