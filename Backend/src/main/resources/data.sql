INSERT INTO role (name)
VALUES ('ROLE_ADMIN');
INSERT INTO role (name)
VALUES ('ROLE_CLIENT');

INSERT INTO client (
    active, enabled, following, number_of_posts, id, last_password_reset_date, email, lastname, name, password, username)
VALUES
    (true, true, 0, 0, nextval('user_seq'), '2024-11-11 21:53:50.02', 'jana', 'jankovic', 'jana', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'ivana');

INSERT INTO public.user_role(
    role_id, user_id)
VALUES (2, 1);