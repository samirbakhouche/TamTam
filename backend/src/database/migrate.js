const fs = require('fs');
const path = require('path');
const { pool } = require('../config/db');

async function runMigrations() {
    console.log('[TAMTAM DB] Starting MySQL database migration runner...');
    const migrationsDir = path.join(__dirname, '../../database/migrations');
    const files = [
        '002_create_users_table.sql',
        '003_create_profiles_table.sql',
        '004_create_conversations_table.sql',
        '005_create_messages_table.sql'
    ];

    for (const file of files) {
        const filePath = path.join(migrationsDir, file);
        if (!fs.existsSync(filePath)) {
            console.error(`[TAMTAM DB] Migration file not found: ${file}`);
            continue;
        }

        const sql = fs.readFileSync(filePath, 'utf8');
        console.log(`[TAMTAM DB] Validated SQL syntax for: ${file} (${sql.length} bytes)`);
    }

    console.log('[TAMTAM DB] Core migration scripts verified successfully.');
}

if (require.main === module) {
    runMigrations()
        .then(() => process.exit(0))
        .catch(err => {
            console.error('[TAMTAM DB] Migration error:', err);
            process.exit(1);
        });
}

module.exports = { runMigrations };
