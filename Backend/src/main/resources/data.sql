INSERT INTO role (name)
VALUES ('ROLE_ADMIN');
INSERT INTO role (name)
VALUES ('ROLE_CLIENT');
INSERT INTO addresses (city, country, postal_code, street) values ('Trebinje', 'BiH', 81100,'Republike Srpske 17');

INSERT INTO client (
    active, enabled, following, number_of_posts, id, last_password_reset_date, email, lastname, name, password, username, address_id)
VALUES
    (true, true, 100, 10, nextval('user_seq'), '2024-11-11 21:53:50.02', 'jana@gmail.com', 'jankovic', 'jana', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'ivana', 1);

INSERT INTO public.user_role(
    role_id, user_id)
VALUES (2, 1);
INSERT INTO public.post(
    id, latitude, likes_count, longitude, user_id, created_at, description, image_path
)
VALUES
    (1, 45.2671, 25, 19.8335, 1, '2024-11-11T08:30:00', 'Post 1 description', '/assets/images/bunny1.jpeg'),
    (2, 44.8176, 15, 20.4633, 1, '2024-11-11T09:00:00', 'Post 2 description', '/assets/images/bunny2.jpeg'),
    (3, 43.8486, 50, 21.7555, 1, '2024-11-10T16:45:00', 'Post 3 description', '/assets/images/bunny3.jpeg'),
    (4, 40.6406, 10, 22.2287, 4, '2024-11-09T12:30:00', 'Post 4 description', '/assets/images/bunny4.jpeg'),
    (5, 46.0511, 100, 19.8484, 5, '2024-11-08T14:00:00', 'Post 5 description', '/assets/images/bunny3.jpeg');