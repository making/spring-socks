INSERT INTO customer(customer_id, first_name, last_name, username, email)
VALUES ('5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        'John',
        'Doe',
        'jdoe',
        'jdoe@example.com');

INSERT INTO customer_password(customer_id, password)
VALUES ('5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        '{bcrypt}$2a$10$oxSJl.keBwxmsMLkcT9lPeAIxfNTPNQxpeywMrF7A3kVszwUTqfTK'); -- demo

INSERT INTO customer_address(address_id, customer_id, number, street, city, postcode,
                             country)
VALUES ('1fb1fe06-32a0-473f-97f2-4a7b70b1d6ca',
        '5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        '3401',
        'Hillview Ave',
        'Palo Alto, CA',
        '94304',
        'USA');

INSERT INTO customer_address(address_id, customer_id, number, street, city, postcode,
                             country)
VALUES ('afca2d00-a47f-48c2-af48-d5aaa175b35a',
        '5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        '501',
        'Second Street Suite 710',
        'San Francisco, CA',
        '94107',
        'USA');

INSERT INTO customer_card(card_id, customer_id, long_num, expires, ccv)
VALUES ('0d32799a-3aaf-446a-b8f2-bea0466a403c',
        '5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        '4111111111111111',
        '2025-06-30',
        '456');

INSERT INTO customer_card(card_id, customer_id, long_num, expires, ccv)
VALUES ('8d2b4918-ee2c-49bc-9cb9-625d8a76ad32',
        '5065e20d-e7e0-4dbe-9389-e1502f4563c1',
        '5555555555554444',
        '2023-10-31',
        '789');