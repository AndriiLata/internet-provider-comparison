-- mapping: address → product id
CREATE TABLE servusspeed_address_product (
                                             id           BIGSERIAL PRIMARY KEY,
                                             street       TEXT NOT NULL,
                                             house_number TEXT NOT NULL,
                                             postal_code  TEXT NOT NULL,
                                             city         TEXT NOT NULL,
                                             product_id   VARCHAR(255) NOT NULL,
                                             UNIQUE (street, house_number, postal_code, city, product_id)
);

-- immutable product details
CREATE TABLE servusspeed_product (
                                     product_id               VARCHAR(255) PRIMARY KEY,
                                     provider                 TEXT,
                                     connection_type          TEXT,
                                     speed                    INT,
                                     speed_limit_from         INT,
                                     contract_duration        INT,
                                     max_age                  INT,
                                     discounted_monthly_cost  INT,
                                     monthly_cost             INT,
                                     monthly_cost_after24m    INT,
                                     monthly_discount_value   INT,
                                     max_discount             INT,
                                     installation_service     BOOLEAN,
                                     tv_included              BOOLEAN,
                                     tv_brand                 TEXT,
                                     last_updated             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
