<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260420193000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create join table for user favorite projects';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE IF NOT EXISTS user_favorite_projects (idUser BIGINT NOT NULL, id_projet BIGINT NOT NULL, PRIMARY KEY(idUser, id_projet), INDEX IDX_FAVORITE_USER (idUser), INDEX IDX_FAVORITE_PROJECT (id_projet), CONSTRAINT FK_FAVORITE_USER FOREIGN KEY (idUser) REFERENCES users (idUser) ON DELETE CASCADE, CONSTRAINT FK_FAVORITE_PROJECT FOREIGN KEY (id_projet) REFERENCES projet_artistique (id_projet) ON DELETE CASCADE) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS user_favorite_projects');
    }
}
