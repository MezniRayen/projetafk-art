<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260406200000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add image column to projet_artistique for local picture uploads';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE projet_artistique ADD image VARCHAR(255) DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE projet_artistique DROP image');
    }
}
