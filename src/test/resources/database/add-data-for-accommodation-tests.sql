INSERT INTO users(id, email, first_name, last_name, password)
VALUES (1, 'admin@gmail.com', 'Jhon', 'Doe', '@g_sJ''#_$ks%1Nq');

INSERT INTO users_roles(user_id, role_id)
VALUES (1, 1),
       (1, 2);

INSERT INTO accommodations(id, type, location, size, daily_rate, availability)
VALUES (1, 'HOUSE', 'Lviv, Shevchenko St. 12', 'Studio, 1 Bedroom', 15, 10),
       (2, 'APARTMENT', 'Lviv, Perekopska St. 1', 'Studio, 1 Bedroom', 30, 5);

INSERT INTO accommodations_amenities(accommodation_id, amenities)
VALUES (1, 'golf courses'),
       (2, 'health club facilities');

INSERT INTO bookings (id, check_in_date, check_out_date, accommodation_id, user_id, status)
VALUES (1, '2024-04-22', '2024-04-23', 1, 1, 'CONFIRMED'),
       (2, '2025-01-01', '2025-01-02', 1, 1, 'PENDING');
