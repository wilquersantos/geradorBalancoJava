-- Create items table if not exists
CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    coleta VARCHAR(255) NOT NULL DEFAULT 'GERAL',
    codigo_referencia VARCHAR(255) NOT NULL,
    quantidade INTEGER NOT NULL,
    descricao TEXT NOT NULL,
    data_recebimento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster searches
CREATE INDEX IF NOT EXISTS idx_codigo_referencia ON items(codigo_referencia);
CREATE INDEX IF NOT EXISTS idx_data_recebimento ON items(data_recebimento);
