/**
 * REST API Backend - E-commerce / Marketplace
 * Arquitectura de Software Limpia y Modular
 * Desarrollado para conectar la App Android con PostgreSQL en Render
 */

const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const { Pool } = require('pg');

const app = express();
const PORT = process.env.PORT || 3000;
const JWT_SECRET = 'ecomers_super_secret_jwt_key_2026_antigravity';

// Middleware
app.use(cors());
app.use(express.json());

// Configuración de PostgreSQL
const connectionString = 'postgresql://ecomers_r7wm_user:EKqJU2OuPC9WsuZ1yXdFHLccxmYtqJKE@dpg-d8b77vl7vvec73ehh42g-a.virginia-postgres.render.com/ecomers_r7wm?ssl=true';
const pool = new Pool({
  connectionString: connectionString,
  ssl: { rejectUnauthorized: false }
});

// Middleware de Autenticación JWT
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) return res.status(401).json({ error: 'Acceso no autorizado: Token no proporcionado' });

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ error: 'Acceso denegado: Token inválido o expirado' });
    req.user = user;
    next();
  });
}

// Middleware de Validación de Roles
function requireRole(roles) {
  return (req, res, next) => {
    if (!roles.includes(req.user.rol)) {
      return res.status(403).json({ error: `Acceso restringido: Se requiere rol ${roles.join(' o ')}` });
    }
    next();
  };
}

// ==========================================
// 1. MÓDULO DE AUTENTICACIÓN
// ==========================================

// Registro de Usuario
app.post('/api/auth/register', async (req, res) => {
  const { email, password, fullName, rol } = req.body;
  if (!email || !password || !fullName || !rol) {
    return res.status(400).json({ error: 'Todos los campos son obligatorios' });
  }

  try {
    const passwordHash = await bcrypt.hash(password, 10);
    const result = await pool.query(
      'INSERT INTO usuarios (email, password_hash, full_name, rol) VALUES ($1, $2, $3, $4) RETURNING id, email, full_name, rol, created_at',
      [email.toLowerCase(), passwordHash, fullName, rol]
    );
    res.status(201).json({ message: 'Usuario registrado con éxito', user: result.rows[0] });
  } catch (error) {
    console.error('❌ Error en registro de usuario:', error);
    if (error.code === '23505') {
      res.status(400).json({ error: 'El correo electrónico ya está registrado' });
    } else {
      res.status(500).json({ error: 'Error del servidor al registrar usuario', details: error.message });
    }
  }
});

// Login de Usuario (Retorna JWT + Rol)
app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'Correo y contraseña requeridos' });
  }

  try {
    const result = await pool.query('SELECT * FROM usuarios WHERE email = $1', [email.toLowerCase()]);
    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Credenciales inválidas' });
    }

    const user = result.rows[0];
    const isPasswordMatch = await bcrypt.compare(password, user.password_hash);
    if (!isPasswordMatch) {
      return res.status(401).json({ error: 'Credenciales inválidas' });
    }

    // Generar Token JWT
    const token = jwt.sign(
      { id: user.id, email: user.email, rol: user.rol, fullName: user.full_name },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    res.json({
      message: 'Login exitoso',
      token: token,
      user: {
        id: user.id,
        email: user.email,
        fullName: user.full_name,
        rol: user.rol
      }
    });
  } catch (error) {
    res.status(500).json({ error: 'Error del servidor en login', details: error.message });
  }
});

// Recuperar contraseña (simulado)
app.post('/api/auth/recover', async (req, res) => {
  const { email } = req.body;
  if (!email) return res.status(400).json({ error: 'El email es requerido' });
  
  try {
    const result = await pool.query('SELECT email FROM usuarios WHERE email = $1', [email.toLowerCase()]);
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }
    
    // Simulación de envío
    res.json({ message: 'Se ha enviado un enlace de recuperación de contraseña a su correo electrónico.' });
  } catch (error) {
    res.status(500).json({ error: 'Error en recuperación', details: error.message });
  }
});


// ==========================================
// 2. MÓDULO CRUD DE USUARIOS (Administrador)
// ==========================================

// Listar todos los usuarios
app.get('/api/users', authenticateToken, requireRole(['admin']), async (req, res) => {
  try {
    const result = await pool.query('SELECT id, email, full_name, rol, created_at FROM usuarios ORDER BY id DESC');
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: 'Error al listar usuarios', details: error.message });
  }
});

// Crear usuario
app.post('/api/users', authenticateToken, requireRole(['admin']), async (req, res) => {
  const { email, password, fullName, rol } = req.body;
  try {
    const passwordHash = await bcrypt.hash(password, 10);
    const result = await pool.query(
      'INSERT INTO usuarios (email, password_hash, full_name, rol) VALUES ($1, $2, $3, $4) RETURNING id, email, full_name, rol, created_at',
      [email.toLowerCase(), passwordHash, fullName, rol]
    );
    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: 'Error al crear usuario', details: error.message });
  }
});

// Obtener detalle de usuario
app.get('/api/users/:id', authenticateToken, requireRole(['admin']), async (req, res) => {
  try {
    const result = await pool.query('SELECT id, email, full_name, rol, created_at FROM usuarios WHERE id = $1', [req.params.id]);
    if (result.rows.length === 0) return res.status(404).json({ error: 'Usuario no encontrado' });
    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: 'Error al obtener usuario', details: error.message });
  }
});

// Actualizar usuario
app.put('/api/users/:id', authenticateToken, requireRole(['admin']), async (req, res) => {
  const { email, fullName, rol } = req.body;
  try {
    const result = await pool.query(
      'UPDATE usuarios SET email = $1, full_name = $2, rol = $3 WHERE id = $4 RETURNING id, email, full_name, rol',
      [email.toLowerCase(), fullName, rol, req.params.id]
    );
    if (result.rows.length === 0) return res.status(404).json({ error: 'Usuario no encontrado' });
    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: 'Error al actualizar usuario', details: error.message });
  }
});

// Eliminar usuario
app.delete('/api/users/:id', authenticateToken, requireRole(['admin']), async (req, res) => {
  try {
    const result = await pool.query('DELETE FROM usuarios WHERE id = $1 RETURNING id', [req.params.id]);
    if (result.rows.length === 0) return res.status(404).json({ error: 'Usuario no encontrado' });
    res.json({ message: 'Usuario eliminado correctamente', id: req.params.id });
  } catch (error) {
    res.status(500).json({ error: 'Error al eliminar usuario', details: error.message });
  }
});


// ==========================================
// 3. MÓDULO CRUD DE PRODUCTOS (Vendedor)
// ==========================================

// Listar todos los productos (Público, catálogo del Comprador)
app.get('/api/products', async (req, res) => {
  try {
    const result = await pool.query(`
      SELECT p.*, u.full_name as seller_name 
      FROM productos p 
      JOIN usuarios u ON p.seller_id = u.id 
      ORDER BY p.id DESC
    `);
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: 'Error al listar productos', details: error.message });
  }
});

// Listar productos específicos de un vendedor
app.get('/api/products/seller/:sellerId', authenticateToken, requireRole(['seller']), async (req, res) => {
  try {
    const result = await pool.query('SELECT * FROM productos WHERE seller_id = $1 ORDER BY id DESC', [req.params.sellerId]);
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: 'Error al listar productos del vendedor', details: error.message });
  }
});

// Crear Producto (Vendedor)
app.post('/api/products', authenticateToken, requireRole(['seller']), async (req, res) => {
  const { nombre, descripcion, precio, stock, imagenUrl } = req.body;
  const sellerId = req.user.id;

  if (!nombre || !precio || stock === undefined) {
    return res.status(400).json({ error: 'Nombre, precio y stock son obligatorios' });
  }

  try {
    const result = await pool.query(
      'INSERT INTO productos (seller_id, nombre, descripcion, precio, stock, imagen_url) VALUES ($1, $2, $3, $4, $5, $6) RETURNING *',
      [sellerId, nombre, descripcion, precio, stock, imagenUrl]
    );
    res.status(201).json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: 'Error al crear producto', details: error.message });
  }
});

// Actualizar Producto (Vendedor)
app.put('/api/products/:id', authenticateToken, requireRole(['seller']), async (req, res) => {
  const { nombre, descripcion, precio, stock, imagenUrl } = req.body;
  const sellerId = req.user.id;

  try {
    // Validar propiedad del producto
    const checkProduct = await pool.query('SELECT seller_id FROM productos WHERE id = $1', [req.params.id]);
    if (checkProduct.rows.length === 0) return res.status(404).json({ error: 'Producto no encontrado' });
    if (checkProduct.rows[0].seller_id !== sellerId) {
      return res.status(403).json({ error: 'No está autorizado para editar este producto' });
    }

    const result = await pool.query(
      'UPDATE productos SET nombre = $1, descripcion = $2, precio = $3, stock = $4, imagen_url = $5 WHERE id = $6 RETURNING *',
      [nombre, descripcion, precio, stock, imagenUrl, req.params.id]
    );
    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: 'Error al actualizar producto', details: error.message });
  }
});

// Eliminar Producto (Vendedor)
app.delete('/api/products/:id', authenticateToken, requireRole(['seller']), async (req, res) => {
  const sellerId = req.user.id;

  try {
    // Validar propiedad
    const checkProduct = await pool.query('SELECT seller_id FROM productos WHERE id = $1', [req.params.id]);
    if (checkProduct.rows.length === 0) return res.status(404).json({ error: 'Producto no encontrado' });
    if (checkProduct.rows[0].seller_id !== sellerId) {
      return res.status(403).json({ error: 'No está autorizado para eliminar este producto' });
    }

    await pool.query('DELETE FROM productos WHERE id = $1', [req.params.id]);
    res.json({ message: 'Producto eliminado correctamente', id: req.params.id });
  } catch (error) {
    res.status(500).json({ error: 'Error al eliminar producto', details: error.message });
  }
});

// Subida de imagen simulada (Para retornar URLs aleatorias estéticas de Unsplash/LoremPicsum)
app.post('/api/products/upload', authenticateToken, requireRole(['seller']), (req, res) => {
  const randomId = Math.floor(Math.random() * 1000);
  const mockUrl = `https://picsum.photos/id/${randomId}/500/500`;
  res.json({ imageUrl: mockUrl });
});


// ==========================================
// 4. MÓDULO DE COMPRAS, ÓRDENES Y GEOLOCALIZACIÓN
// ==========================================

// Crear una Orden con Geolocalización y Detalles de Productos
app.post('/api/orders', authenticateToken, requireRole(['buyer']), async (req, res) => {
  const { total, latitud, longitud, items } = req.body;
  const buyerId = req.user.id;

  if (!items || items.length === 0 || !total) {
    return res.status(400).json({ error: 'Detalles de la orden y total requeridos' });
  }

  const client = await pool.connect();
  try {
    await client.query('BEGIN');

    // 1. Insertar en la tabla ordenes
    const orderResult = await client.query(
      'INSERT INTO ordenes (buyer_id, total, estado_pago, latitud, longitud) VALUES ($1, $2, $3, $4, $5) RETURNING *',
      [buyerId, total, 'Pendiente', latitud || null, longitud || null]
    );
    const orderId = orderResult.rows[0].id;

    // 2. Insertar cada producto en detalles_orden
    for (const item of items) {
      // item = { productId, cantidad, precioUnitario }
      await client.query(
        'INSERT INTO detalles_orden (order_id, product_id, cantidad, precio_unitario) VALUES ($1, $2, $3, $4)',
        [orderId, item.productId, item.cantidad, item.precioUnitario]
      );

      // 3. Descontar del stock del producto
      await client.query(
        'UPDATE productos SET stock = stock - $1 WHERE id = $2 AND stock >= $1',
        [item.cantidad, item.productId]
      );
    }

    await client.query('COMMIT');
    res.status(201).json({
      message: 'Orden creada con éxito',
      order: orderResult.rows[0]
    });
  } catch (error) {
    await client.query('ROLLBACK');
    res.status(500).json({ error: 'Error al procesar la orden', details: error.message });
  } finally {
    client.release();
  }
});

// Listar Historial de Órdenes del Comprador
app.get('/api/orders/buyer/:buyerId', authenticateToken, requireRole(['buyer']), async (req, res) => {
  // Garantizar seguridad de acceso a sus propios datos
  if (parseInt(req.params.buyerId) !== req.user.id) {
    return res.status(403).json({ error: 'Acceso no autorizado al historial de otro usuario' });
  }

  try {
    const result = await pool.query(`
      SELECT o.*, 
        (
          SELECT json_agg(json_build_object(
            'id', d.id,
            'productId', d.product_id,
            'nombre', p.nombre,
            'imagenUrl', p.imagen_url,
            'cantidad', d.cantidad,
            'precioUnitario', d.precio_unitario
          ))
          FROM detalles_orden d
          JOIN productos p ON d.product_id = p.id
          WHERE d.order_id = o.id
        ) as items
      FROM ordenes o
      WHERE o.buyer_id = $1
      ORDER BY o.id DESC
    `, [req.params.buyerId]);

    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: 'Error al obtener historial de órdenes', details: error.message });
  }
});


// ==========================================
// 5. INTEGRACIÓN DE PASARELA DE PAGOS (ePayco)
// ==========================================

// Confirmación de pago / Simulación de Webhook de ePayco
app.post('/api/orders/:id/pay', authenticateToken, requireRole(['buyer']), async (req, res) => {
  const orderId = req.params.id;
  const { transactionId, status } = req.body; // status: 'Aprobado', 'Rechazado'

  try {
    const result = await pool.query(
      'UPDATE ordenes SET estado_pago = $1, id_transaccion_epayco = $2 WHERE id = $3 RETURNING *',
      [status || 'Aprobado', transactionId || 'EP-' + Math.floor(Math.random()*100000), orderId]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Orden no encontrada' });
    }

    res.json({
      message: `Pago procesado con éxito en ePayco. Estado de orden: ${status || 'Aprobado'}`,
      order: result.rows[0]
    });
  } catch (error) {
    res.status(500).json({ error: 'Error al procesar pago', details: error.message });
  }
});


// Iniciar el Servidor REST API
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Servidor REST API corriendo en http://localhost:${PORT}`);
  console.log(`📡 Conectado a Render Postgres database: ecomers_r7wm`);
});
