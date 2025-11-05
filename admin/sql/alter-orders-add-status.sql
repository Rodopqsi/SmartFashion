-- Add status to orders table for Admin order state tracking
-- Values: 'PAGADO', 'PROCESANDO_ENVIO', 'ENTREGADO', 'CANCELADO'
ALTER TABLE orders ADD COLUMN status VARCHAR(32) NULL AFTER created_at;

-- Optionally initialize existing rows to 'PAGADO'
UPDATE orders SET status = 'PAGADO' WHERE status IS NULL;
