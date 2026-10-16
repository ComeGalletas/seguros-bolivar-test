INSERT INTO poliza (id, tipo, estado, canon, prima) VALUES
  (1,   'INDIVIDUAL', 'ACTIVA',    1200000.00, 85000.00),
  (2,   'COLECTIVA',  'ACTIVA',    5400000.00, 320000.00),
  (3,   'INDIVIDUAL', 'CANCELADA',  900000.00, 60000.00),
  (555, 'COLECTIVA',  'RENOVADA',  2500000.00, 150000.00);

INSERT INTO riesgo (id, descripcion, estado, poliza_id) VALUES
  (1, 'Apartamento Calle 100 #15-20',  'ACTIVO',    1),
  (2, 'Local comercial Av. Boyacá',     'ACTIVO',    2),
  (3, 'Bodega Zona Franca',             'ACTIVO',    2),
  (4, 'Casa Chía',                      'CANCELADO', 3),
  (5, 'Oficina Torre Norte piso 8',     'ACTIVO',    555);

ALTER TABLE poliza ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE riesgo ALTER COLUMN id RESTART WITH 1000;
