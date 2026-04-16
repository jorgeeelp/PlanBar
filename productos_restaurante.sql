-- ============================================================
-- SCRIPT DE INSERCIÓN DE PRODUCTOS - PlanBar
-- Categorías: entrantes, sopas, carnes, pescados, postres, bebidas, cafeteria
-- ============================================================

-- ENTRANTES
INSERT INTO productos (nombre_producto, precio_producto, categoria_producto, observaciones_producto, foto_producto) VALUES
('Pan con Tomate', 2.50, 'entrantes', 'Con aceite de oliva virgen extra', 'default.jpg'),
('Croquetas de Jamón', 7.50, 'entrantes', 'Ración de 6 unidades, caseras', 'default.jpg'),
('Patatas Bravas', 5.50, 'entrantes', 'Con salsa brava y alioli', 'default.jpg'),
('Tabla de Ibéricos', 16.90, 'entrantes', 'Jamón, salchichón y lomo ibérico', 'default.jpg'),
('Pimientos de Padrón', 6.50, 'entrantes', 'Fritos con sal maldon', 'default.jpg'),
('Ensalada Mixta', 7.00, 'entrantes', 'Lechuga, tomate, cebolla, atún y aceitunas', 'default.jpg'),
('Jamón Serrano', 9.50, 'entrantes', 'Media ración con pan', 'default.jpg'),
('Gambas al Ajillo', 11.90, 'entrantes', 'Con guindilla y aceite de oliva', 'default.jpg'),

-- SOPAS
('Gazpacho Andaluz', 5.50, 'sopas', 'Frío, con guarnición de verduras', 'default.jpg'),
('Sopa de Cebolla', 6.00, 'sopas', 'Gratinada con queso emmental', 'default.jpg'),
('Caldo de Pollo', 4.50, 'sopas', 'Con fideos y verduras de temporada', 'default.jpg'),
('Crema de Verduras', 5.50, 'sopas', 'Calabacín, zanahoria y puerro', 'default.jpg'),
('Sopa de Marisco', 9.90, 'sopas', 'Con gambas, mejillones y almejas', 'default.jpg'),

-- CARNES
('Entrecot de Ternera', 19.90, 'carnes', '250g, con patatas fritas', 'default.jpg'),
('Solomillo de Cerdo', 14.50, 'carnes', 'Con salsa de champiñones', 'default.jpg'),
('Pollo Asado', 11.90, 'carnes', 'Medio pollo con guarnición', 'default.jpg'),
('Chuletón a la Brasa', 24.90, 'carnes', '300g de chuletón de vaca madurado', 'default.jpg'),
('Hamburguesa Gourmet', 13.50, 'carnes', 'Con queso cheddar, cebolla caramelizada y bacon', 'default.jpg'),
('Costillas BBQ', 16.90, 'carnes', 'Marinadas y asadas al horno lentamente', 'default.jpg'),
('Albóndigas en Salsa', 9.90, 'carnes', 'En salsa de tomate casera con arroz', 'default.jpg'),

-- PESCADOS
('Merluza a la Plancha', 14.90, 'pescados', 'Con salsa verde y almejas', 'default.jpg'),
('Dorada al Horno', 16.50, 'pescados', 'Con patatas panadera y limón', 'default.jpg'),
('Calamares a la Romana', 10.50, 'pescados', 'Con alioli y rodajas de limón', 'default.jpg'),
('Bacalao al Pil-Pil', 15.90, 'pescados', 'Receta tradicional vasca', 'default.jpg'),
('Paella de Marisco', 14.90, 'pescados', 'Mínimo 2 personas. Precio por persona', 'default.jpg'),
('Pulpo a la Gallega', 13.90, 'pescados', 'Con pimentón y aceite de oliva', 'default.jpg'),
('Lubina a la Sal', 17.90, 'pescados', 'Entera, con guarnición de verduras', 'default.jpg'),

-- POSTRES
('Tarta de Queso', 5.50, 'postres', 'Casera con coulis de frutos rojos', 'default.jpg'),
('Coulant de Chocolate', 6.00, 'postres', 'Con helado de vainilla', 'default.jpg'),
('Flan Casero', 4.50, 'postres', 'Con nata y caramelo', 'default.jpg'),
('Crema Catalana', 5.00, 'postres', 'Quemada en el momento', 'default.jpg'),
('Helado Artesano', 4.50, 'postres', 'Selección de 3 bolas', 'default.jpg'),
('Tiramisú', 5.50, 'postres', 'Receta original italiana', 'default.jpg'),
('Brownie con Helado', 6.00, 'postres', 'Brownie caliente con helado de vainilla', 'default.jpg'),

-- BEBIDAS
('CocaCola', 2.50, 'bebidas', 'Botellín 25cl', 'default.jpg'),
('CocaCola Zero', 2.50, 'bebidas', 'Botellín 25cl sin azúcar', 'default.jpg'),
('Agua Mineral', 1.50, 'bebidas', 'Botella 50cl', 'default.jpg'),
('Agua con Gas', 1.80, 'bebidas', 'Botella 50cl', 'default.jpg'),
('Cerveza Nacional', 2.50, 'bebidas', 'Botellín 25cl o caña', 'default.jpg'),
('Cerveza Artesana', 3.50, 'bebidas', 'Botella 33cl', 'default.jpg'),
('Vino Tinto (copa)', 3.00, 'bebidas', 'Denominación de origen Rioja', 'default.jpg'),
('Vino Blanco (copa)', 3.00, 'bebidas', 'Albariño gallego', 'default.jpg'),
('Vino Rosado (copa)', 2.80, 'bebidas', 'Navarra rosado', 'default.jpg'),
('Vino Tinto (botella)', 15.00, 'bebidas', 'Rioja Crianza', 'default.jpg'),
('Refresco de Naranja', 2.50, 'bebidas', 'Fanta naranja botellín 25cl', 'default.jpg'),
('Refresco de Limón', 2.50, 'bebidas', 'Fanta limón botellín 25cl', 'default.jpg'),
('Zumo Natural', 3.50, 'bebidas', 'Naranja, zanahoria o piña', 'default.jpg'),
('Tónica', 2.80, 'bebidas', 'Botellín Schweppes', 'default.jpg'),

-- CAFETERÍA
('Café Solo', 1.50, 'cafeteria', '', 'default.jpg'),
('Café con Leche', 2.00, 'cafeteria', '', 'default.jpg'),
('Cortado', 1.80, 'cafeteria', '', 'default.jpg'),
('Café con Hielo', 2.00, 'cafeteria', '', 'default.jpg'),
('Cappuccino', 2.50, 'cafeteria', '', 'default.jpg'),
('Infusión', 2.00, 'cafeteria', 'Manzanilla, poleo, tila o té', 'default.jpg'),
('Colacao', 2.50, 'cafeteria', 'Caliente o frío', 'default.jpg'),
('Carajillo', 3.00, 'cafeteria', 'Café con licor a elegir', 'default.jpg');
