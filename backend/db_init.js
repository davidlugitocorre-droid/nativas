/**
 * Script de Inicialización de la Base de Datos PostgreSQL en Render
 * Arquitectura y Diseño de Software - Lugo, E-commerce
 */

const { Pool } = require('pg');

const connectionString = 'postgresql://ecomers_r7wm_user:EKqJU2OuPC9WsuZ1yXdFHLccxmYtqJKE@dpg-d8b77vl7vvec73ehh42g-a.virginia-postgres.render.com/ecomers_r7wm?ssl=true';

const pool = new Pool({
  connectionString: connectionString,
  ssl: {
    rejectUnauthorized: false // Requerido para servidores de bases de datos como Render / Heroku
  }
});

async function initDatabase() {
  console.log('⚡ Iniciando conexión con PostgreSQL en Render...');
  
  const client = await pool.connect();
  try {
    console.log('✅ Conexión establecida con éxito.');
    
    // Iniciar una transacción
    await client.query('BEGIN');

    // 1. Crear tabla de Usuarios
    console.log('⏳ Creando tabla "usuarios"...');
    await client.query(`
      CREATE TABLE IF NOT EXISTS usuarios (
        id SERIAL PRIMARY KEY,
        email VARCHAR(100) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        full_name VARCHAR(100) NOT NULL,
        rol VARCHAR(20) NOT NULL CHECK (rol IN ('admin', 'seller', 'buyer')),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);

    // 2. Crear tabla de Productos
    console.log('⏳ Creando tabla "productos"...');
    await client.query(`
      CREATE TABLE IF NOT EXISTS productos (
        id SERIAL PRIMARY KEY,
        seller_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
        nombre VARCHAR(150) NOT NULL,
        descripcion TEXT,
        precio DECIMAL(12, 2) NOT NULL CHECK (precio >= 0),
        stock INT NOT NULL CHECK (stock >= 0),
        imagen_url VARCHAR(500),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);

    // 3. Crear tabla de Órdenes
    console.log('⏳ Creando tabla "ordenes"...');
    await client.query(`
      CREATE TABLE IF NOT EXISTS ordenes (
        id SERIAL PRIMARY KEY,
        buyer_id INT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
        total DECIMAL(12, 2) NOT NULL,
        estado_pago VARCHAR(30) DEFAULT 'Pendiente' CHECK (estado_pago IN ('Pendiente', 'Aprobado', 'Rechazado')),
        latitud DOUBLE PRECISION,
        longitud DOUBLE PRECISION,
        id_transaccion_epayco VARCHAR(100),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      );
    `);

    // 4. Crear tabla de Detalles de Orden
    console.log('⏳ Creando tabla "detalles_orden"...');
    await client.query(`
      CREATE TABLE IF NOT EXISTS detalles_orden (
        id SERIAL PRIMARY KEY,
        order_id INT NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
        product_id INT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
        cantidad INT NOT NULL CHECK (cantidad > 0),
        precio_unitario DECIMAL(12, 2) NOT NULL
      );
    `);

    await client.query('COMMIT');
    console.log('🎉 ¡Estructura de Base de Datos inicializada con éxito!');

  } catch (error) {
    await client.query('ROLLBACK');
    console.error('❌ Error inicializando la base de datos:', error);
  } finally {
    client.release();
    await pool.end();
  }
}

initDatabase();
