CREATE TABLE IF NOT EXISTS shipment
(
    order_id        VARCHAR(40) NOT NULL,
    carrier         VARCHAR(20) NOT NULL,
    tracking_number VARCHAR(40) NOT NULL UNIQUE,
    shipment_date   DATE        NOT NULL,
    PRIMARY KEY (order_id),
    INDEX (shipment_date)
);