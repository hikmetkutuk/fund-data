> If a record has been added manually from the outside 
and the order of the auto-incrementing primary key (e.g. SERIAL or BIGSERIAL) is mixed up, 
PostgreSQL may give a duplicate key value violates unique constraint error 
when you try to add a new record because it conflicts with an existing primary key value. 
This is usually caused by the sequence not being set correctly.

``SELECT setval('currency_price_id_seq', (SELECT MAX(id) FROM currency_price))``

---

> If you want to find records in the fund_list table that are not matched with the query result in the fund_prices table 
(that is, records in the fund_list table but not in the fund_prices query), 
you can use the following method:

``SELECT fl.* FROM public.funds fl LEFT JOIN public.fund_prices fp ON fl.id = fp.id AND fp.date = '2024-12-24' WHERE fp.id IS NULL``
