CREATE TABLE IF NOT EXISTS sock
(
    sock_id     VARCHAR(40) NOT NULL,
    name        VARCHAR(20),
    description VARCHAR(200),
    price       DECIMAL(8, 2),
    count       INT,
    image_url_1 VARCHAR(40),
    image_url_2 VARCHAR(40),
    PRIMARY KEY (sock_id)
);

CREATE TABLE IF NOT EXISTS tag
(
    tag_id MEDIUMINT NOT NULL AUTO_INCREMENT,
    name   VARCHAR(20),
    PRIMARY KEY (tag_id)
);

CREATE TABLE IF NOT EXISTS sock_tag
(
    sock_id VARCHAR(40),
    tag_id  MEDIUMINT NOT NULL,
    FOREIGN KEY (sock_id)
        REFERENCES sock (sock_id),
    FOREIGN KEY (tag_id)
        REFERENCES tag (tag_id)
);