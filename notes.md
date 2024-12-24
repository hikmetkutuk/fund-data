If a record has been added manually from the outside 
and the order of the auto-incrementing primary key (e.g. SERIAL or BIGSERIAL) is mixed up, 
PostgreSQL may give a duplicate key value violates unique constraint error 
when you try to add a new record because it conflicts with an existing primary key value. 
This is usually caused by the sequence not being set correctly.

#### SELECT setval('currency_price_id_seq', (SELECT MAX(id) FROM currency_price));