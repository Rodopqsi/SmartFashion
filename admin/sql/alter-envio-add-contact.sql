ALTER TABLE Envio
  ADD COLUMN email_destino VARCHAR(255) NULL AFTER direccion,
  ADD COLUMN telefono_destino VARCHAR(64) NULL AFTER email_destino;
