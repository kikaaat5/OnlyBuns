-- Dodavanje novih uloga
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_CLIENT');

-- Dodavanje adresa
INSERT INTO addresses (city, country, postal_code, street) VALUES ('Trebinje', 'BiH', 81100, 'Republike Srpske 17');

-- Dodavanje klijenta
INSERT INTO client (
    active, enabled, following, number_of_posts, id, last_password_reset_date, email, lastname, name, password, username, address_id, followers)
VALUES
    (true, true, 0, 10, nextval('user_seq'), '2024-11-11 21:53:50.02', 'jana@gmail.com', 'jankovic', 'jana', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'ivana', 1, 0),
    (true, true, 0, 5, nextval('user_seq'), '2024-11-11 21:53:50.02', 'ana@gmail.com', 'petrovic', 'ana', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'ana', 1, 0),
    (true, true, 0, 7, nextval('user_seq'), '2024-11-11 21:53:50.02', 'nikola@gmail.com', 'jovanovic', 'nikola', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'nikola', 1, 0),
    (true, true, 0, 12, nextval('user_seq'), '2024-11-11 21:53:50.02', 'petar@gmail.com', 'milenkovic', 'petar', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'petar', 1, 0),
    (true, true, 0, 8, nextval('user_seq'), '2024-11-11 21:53:50.02', 'mila@gmail.com', 'markovic', 'mila', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'mila', 1, 0),
    (true, true, 0, 11, nextval('user_seq'), '2024-11-11 21:53:50.02', 'milica@gmail.com', 'jovanovic', 'milica', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'milica', 1, 0),
    (true, true, 0, 6, nextval('user_seq'), '2024-11-11 21:53:50.02', 'sanja@gmail.com', 'petrovic', 'sanja', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'sanja', 1, 0),
    (true, true, 0, 5, nextval('user_seq'), '2024-11-11 21:53:50.02', 'jelena@gmail.com', 'jankovic', 'jelena', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'jelena', 1, 0),
    (true, true, 0, 9, nextval('user_seq'), '2024-11-11 21:53:50.02', 'milena@gmail.com', 'jovanovic', 'milena', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'milena', 1, 0);

-- Dodavanje uloge korisnicima (klijenti)
INSERT INTO public.user_role(role_id, user_id)
VALUES (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9);

-- Dodavanje administratora
INSERT INTO administrator (
    enabled, id, last_password_reset_date, email, lastname, name, password, username, address_id)
VALUES
    (true, nextval('user_seq'), '2024-11-11 21:53:50.02', 'maja@gmail.com', 'jankovic', 'maja', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'maja', 1);

-- Dodavanje uloge administratoru
INSERT INTO public.user_role(role_id, user_id)
VALUES (1, 10);

ALTER TABLE post
ALTER COLUMN image_path TYPE TEXT;

-- Dodavanje postova
INSERT INTO public.post(
    id, latitude, likes_count, longitude, user_id, created_at, description, image_path)
VALUES
    (100, 45.2671, 25, 19.8335, 1, '2024-11-11T08:30:00', 'Post 1 description', '/assets/images/bunny1.jpeg'),
    (101, 44.8176, 15, 20.4633, 1, '2024-11-11T09:00:00', 'Post 2 description', '/assets/images/bunny2.jpeg'),
    (102, 43.8486, 50, 21.7555, 1, '2024-11-10T16:45:00', 'Post 3 description', '/assets/images/bunny3.jpeg'),
    (103, 40.6406, 10, 22.2287, 4, '2024-11-09T12:30:00', 'Post 4 description', '/assets/images/bunny4.jpeg'),
    (104, 46.0511, 100, 19.8484, 5, '2024-11-08T14:00:00', 'Post 5 description', '/assets/images/bunny3.jpeg'),
    (105, 44.8176, 60, 18, 3, '2024-12-12T10:00:00', 'Post 6 description', '/assets/images/bunny2.jpeg'),
    (106, 43.8486, 75, 20, 3, '2024-12-12T11:00:00', 'Post 7 description', '/assets/images/bunny1.jpeg'),
    (107, 40.6406, 35, 21, 6, '2024-12-12T09:30:00', 'Post 8 description', '/assets/images/bunny3.jpeg'),
    (108, 46.0511, 85, 22, 7, '2024-12-12T13:00:00', 'Post 9 description', '/assets/images/bunny4.jpeg'),
    (109, 44.8176, 95, 23, 9, '2024-12-12T14:00:00', 'Post 10 description', '/assets/images/bunny2.jpeg');

-- Dodavanje komentara (ID-evi će biti automatski generisani)
-- Komentari za poslednju nedelju (trenutni datum: 2025-06-23)
INSERT INTO public.comment(user_id, content, created_at, post_id) VALUES
                                                                      (2, 'Odličan post!', '2025-06-20T10:00:00', 100), -- Pre 3 dana
                                                                      (3, 'Slažem se!', '2025-06-21T11:30:00', 100), -- Pre 2 dana
                                                                      (4, 'Prelepo!', '2025-06-22T12:00:00', 101), -- Pre 1 dan
                                                                      (5, 'Super content!', '2025-06-23T09:00:00', 102), -- Danas
                                                                      (2, 'Komentar 5', '2025-06-19T15:00:00', 103); -- Pre 4 dana

-- Komentari za poslednji mesec (ali ne poslednju nedelju)
INSERT INTO public.comment(user_id, content, created_at, post_id) VALUES
                                                                      (6, 'Interesantno!', '2025-06-01T10:00:00', 104), -- Početak juna
                                                                      (7, 'Dobro rečeno.', '2025-05-25T14:00:00', 105), -- Krajem maja
                                                                      (8, 'Volim ovo.', '2025-05-15T08:00:00', 106), -- Sredina maja
                                                                      (9, 'Komentar 9', '2025-05-01T09:00:00', 107); -- Početak maja

-- Komentari za poslednju godinu (ali ne poslednji mesec)
INSERT INTO public.comment(user_id, content, created_at, post_id) VALUES
                                                                      (1, 'Komentar 10', '2025-04-10T16:00:00', 108), -- April
                                                                      (2, 'Komentar 11', '2025-03-20T11:00:00', 109), -- Mart
                                                                      (3, 'Komentar 12', '2025-02-14T12:00:00', 100), -- Februar
                                                                      (4, 'Komentar 13', '2025-01-05T13:00:00', 101), -- Januar
                                                                      (5, 'Komentar 14', '2024-12-01T10:00:00', 102), -- Decembar prošle godine
                                                                      (6, 'Komentar 15', '2024-11-20T14:00:00', 103), -- Novembar prošle godine (važno za godišnji test)
                                                                      (7, 'Komentar 16', '2024-10-15T09:00:00', 104); -- Oktobar prošle godine

-- Novi klijenti koji nemaju postove, samo komentare
INSERT INTO client (
    active, enabled, following, number_of_posts, id, last_password_reset_date, email, lastname, name, password, username, address_id, followers)
VALUES
    (true, true, 0, 0, nextval('user_seq'), '2024-11-11 21:53:50.02', 'samo.komentari1@gmail.com', 'komentar', 'prvi', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'samo_komentari1', 1, 0),
    (true, true, 0, 0, nextval('user_seq'), '2024-11-11 21:53:50.02', 'samo.komentari2@gmail.com', 'komentar', 'drugi', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'samo_komentari2', 1, 0),
    (true, true, 0, 0, nextval('user_seq'), '2024-11-11 21:53:50.02', 'neaktivni@gmail.com', 'neaktivni', 'neaktivni', '$2a$10$7kAwpIqahu78ZFuKhVyy4.sLpw5BND1QnohhK9RK/HLulUlvSSW1S', 'neaktivni', 1, 0);

-- Dodavanje uloga novim klijentima
-- Ažurirano: Naredni ID-evi nakon 10 (Maja) su 11, 12, 13.
INSERT INTO public.user_role(role_id, user_id)
VALUES (2, 11), (2, 12), (2, 13);

-- Komentari od klijenata koji nemaju postove (ID-evi 11 i 12)
INSERT INTO public.comment(user_id, content, created_at, post_id) VALUES
                                                                      (11, 'Super post!', '2025-06-23T10:00:00', 100),
                                                                      (12, 'Slažem se sa ovim.', '2025-06-22T11:00:00', 101);