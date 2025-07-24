INSERT INTO categories (name)
VALUES ('Ramos'),
       ('Arreglos para eventos'),
       ('Plantas'),
       ('Accesorios'),
       ('Flores sueltas');

INSERT INTO products (name, price, description, category_id)
VALUES ('Ramo Clásico de Rosas Rojas', 250.00,
        'Doce rosas rojas frescas con follaje, envueltas en papel decorativo y listón.', 1),
       ('Ramo Mixto de Temporada', 320.50,
        'Combinación de flores variadas de la temporada, ideal para cualquier ocasión.', 1),
       ('Centro de Mesa Elegante', 115.00,
        'Arreglo floral bajo con lirios, gerberas y follaje para decoración de mesas.', 2),
       ('Arreglo para Boda con Orquídeas', 450.00,
        'Diseño elegante con orquídeas blancas, rosas y follaje tropical para bodas.', 2),
       ('Suculenta en Maceta de Barro', 59.99, 'Suculenta pequeña en maceta artesanal de barro, fácil de cuidar.', 3),
       ('Orquídea Phalaenopsis Blanca', 75.00, 'Orquídea de interior en maceta blanca, incluye instructivo de cuidado.',
        3),
       ('Florero de Cristal Transparente', 99.99, 'Florero cilíndrico de cristal ideal para arreglos altos o ramos.',
        4),
       ('Cinta Decorativa de Satín (10m)', 25.50, 'Rollo de cinta de satín color rojo, perfecto para envolver ramos.',
        4),
       ('Rosa Roja Premium (por tallo)', 22.00, 'Rosa roja de tallo largo, fresca y de calidad premium.', 5),
       ('Girasol Fresco (por tallo)', 29.99, 'Girasol brillante de gran tamaño, ideal para arreglos campestres.', 5);

