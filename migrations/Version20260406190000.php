<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Platforms\AbstractMySQLPlatform;
use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260406190000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Align users role values with app roles and enforce non-empty investment optional fields.';
    }

    public function up(Schema $schema): void
    {
        if (!$this->connection->getDatabasePlatform() instanceof AbstractMySQLPlatform) {
            return;
        }

        $schemaManager = $this->connection->createSchemaManager();
        if (!$schemaManager->tablesExist(['users']) || !$schemaManager->tablesExist(['investissement'])) {
            return;
        }

        $this->addSql("ALTER TABLE users MODIFY role ENUM('INVESTISSEUR','ARTISTE','ADMIN') NOT NULL DEFAULT 'INVESTISSEUR'");
        $this->addSql("UPDATE users SET role = 'INVESTISSEUR' WHERE role = 'USER'");
        $this->addSql('ALTER TABLE investissement MODIFY palier VARCHAR(100) NOT NULL');
        $this->addSql('ALTER TABLE investissement MODIFY message_soutien VARCHAR(255) NOT NULL');
    }

    public function down(Schema $schema): void
    {
        if (!$this->connection->getDatabasePlatform() instanceof AbstractMySQLPlatform) {
            return;
        }

        $schemaManager = $this->connection->createSchemaManager();
        if (!$schemaManager->tablesExist(['users']) || !$schemaManager->tablesExist(['investissement'])) {
            return;
        }

        $this->addSql("UPDATE users SET role = 'USER' WHERE role IN ('INVESTISSEUR','ARTISTE')");
        $this->addSql("ALTER TABLE users MODIFY role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER'");
        $this->addSql('ALTER TABLE investissement MODIFY palier VARCHAR(100) DEFAULT NULL');
        $this->addSql('ALTER TABLE investissement MODIFY message_soutien VARCHAR(255) DEFAULT NULL');
    }
}
