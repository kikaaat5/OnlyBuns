INSERT INTO role (name)
VALUES ('ROLE_ADMIN');
INSERT INTO role (name)
VALUES ('ROLE_CLIENT');
INSERT INTO addresses (city, country, postal_code, street) values ('Trebinje', 'BiH', 81100,'Republike Srpske 17');

INSERT INTO client (
    active, enabled, following, number_of_posts, id, last_password_reset_date, email, lastname, name, password, username, address_id)
VALUES
    (true, true, 0, 0, nextval('user_seq'), '2024-11-11 21:53:50.02', 'jana@gmail.com', 'jankovic', 'jana', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'ivana', 1);

INSERT INTO public.user_role(
    role_id, user_id)
VALUES (2, 1);