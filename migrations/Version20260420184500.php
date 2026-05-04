<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260420184500 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add userType column for USER accounts and initialize artist/investor types';
    }

    public function up(Schema $schema): void
    {
        $this->addSql("ALTER TABLE users ADD COLUMN IF NOT EXISTS userType ENUM('ARTIST','INVESTOR') DEFAULT NULL");
        $this->addSql("UPDATE users SET userType = 'ARTIST' WHERE role = 'USER' AND userType IS NULL AND (LOWER(email) LIKE '%artiste%' OR LOWER(nom) LIKE '%artiste%' OR LOWER(prenom) LIKE '%artiste%')");
        $this->addSql("UPDATE users SET userType = 'INVESTOR' WHERE role = 'USER' AND userType IS NULL");
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE users DROP COLUMN userType');
    }
}
