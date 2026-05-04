<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260420183000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Align users table with USER/ADMIN schema and add admin_actions table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql("UPDATE users SET role = 'USER' WHERE role IN ('INVESTISSEUR', 'ARTISTE')");
        $this->addSql("ALTER TABLE users MODIFY role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER'");
        $this->addSql("ALTER TABLE users MODIFY statut ENUM('ACTIF','SUSPENDU','BANNI') NOT NULL DEFAULT 'ACTIF'");
        $this->addSql("ALTER TABLE users ADD COLUMN IF NOT EXISTS lastLogin DATETIME DEFAULT NULL");
        $this->addSql("ALTER TABLE users ADD COLUMN IF NOT EXISTS isVerified TINYINT(1) DEFAULT 0");
        $this->addSql("ALTER TABLE users ADD COLUMN IF NOT EXISTS profilePicture VARCHAR(255) DEFAULT NULL");

        $this->addSql("CREATE TABLE IF NOT EXISTS admin_actions (idAction BIGINT AUTO_INCREMENT NOT NULL, idAdmin BIGINT NOT NULL, action VARCHAR(50) NOT NULL, dateAction DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, description LONGTEXT DEFAULT NULL, expirationDate DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL 30 DAY), status ENUM('VALIDE','EXPIREE') NOT NULL DEFAULT 'VALIDE', INDEX IDX_A6C054D29EA19F0D (idAdmin), PRIMARY KEY(idAction), CONSTRAINT FK_A6C054D29EA19F0D FOREIGN KEY (idAdmin) REFERENCES users (idUser) ON DELETE CASCADE) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB");
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE admin_actions DROP FOREIGN KEY FK_A6C054D29EA19F0D');
        $this->addSql('DROP TABLE admin_actions');

        $this->addSql('ALTER TABLE users DROP lastLogin');
        $this->addSql('ALTER TABLE users DROP isVerified');
        $this->addSql('ALTER TABLE users DROP profilePicture');
        $this->addSql("UPDATE users SET role = 'INVESTISSEUR' WHERE role = 'USER'");
        $this->addSql("ALTER TABLE users MODIFY role ENUM('INVESTISSEUR','ARTISTE','ADMIN') NOT NULL DEFAULT 'INVESTISSEUR'");
    }
}
