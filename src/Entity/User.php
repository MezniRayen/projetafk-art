<?php

namespace App\Entity;

use App\Repository\UserRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Bridge\Doctrine\Validator\Constraints\UniqueEntity;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\Table(name: 'users')]
#[UniqueEntity(fields: ['email'], message: 'This email is already used.')]
class User implements UserInterface, PasswordAuthenticatedUserInterface
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(name: 'idUser', type: Types::BIGINT)]
    private ?string $id = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank]
    #[Assert\Length(max: 50)]
    #[Assert\Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.')]
    private string $nom = '';

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank]
    #[Assert\Length(max: 50)]
    #[Assert\Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.')]
    private string $prenom = '';

    #[ORM\Column(length: 100, unique: true)]
    #[Assert\NotBlank]
    #[Assert\Email]
    private string $email = '';

    #[ORM\Column(name: 'motDePasse', length: 255)]
    private string $password = '';

    #[ORM\Column(length: 20, options: ['default' => 'INVESTISSEUR'])]
    #[Assert\Choice(choices: ['ADMIN', 'ARTISTE', 'INVESTISSEUR'])]
    private string $role = 'INVESTISSEUR';

    #[ORM\Column(name: 'dateCreation', type: Types::DATETIME_MUTABLE)]
    private \DateTimeInterface $dateCreation;

    #[ORM\Column(length: 20, options: ['default' => 'ACTIF'])]
    private string $statut = 'ACTIF';

    #[ORM\OneToMany(mappedBy: 'artiste', targetEntity: ProjetArtistique::class)]
    private Collection $projets;

    #[ORM\OneToMany(mappedBy: 'investisseur', targetEntity: Investissement::class)]
    private Collection $investissements;

    public function __construct()
    {
        $this->projets = new ArrayCollection();
        $this->investissements = new ArrayCollection();
        $this->dateCreation = new \DateTimeImmutable();
    }

    public function getId(): ?string
    {
        return $this->id;
    }

    public function getNom(): string
    {
        return $this->nom;
    }

    public function setNom(string $nom): self
    {
        $this->nom = $nom;

        return $this;
    }

    public function getPrenom(): string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): self
    {
        $this->prenom = $prenom;

        return $this;
    }

    public function getEmail(): string
    {
        return $this->email;
    }

    public function setEmail(string $email): self
    {
        $this->email = $email;

        return $this;
    }

    public function getUserIdentifier(): string
    {
        return $this->email;
    }

    public function getPassword(): string
    {
        return $this->password;
    }

    public function setPassword(string $password): self
    {
        $this->password = $password;

        return $this;
    }

    public function getRole(): string
    {
        return $this->role;
    }

    public function setRole(string $role): self
    {
        $this->role = $role;

        return $this;
    }

    public function getRoles(): array
    {
        $base = match ($this->role) {
            'ADMIN' => 'ROLE_ADMIN',
            'ARTISTE' => 'ROLE_ARTIST',
            default => 'ROLE_INVESTOR',
        };

        return array_values(array_unique([$base, 'ROLE_USER']));
    }

    public function eraseCredentials(): void {}

    public function getDateCreation(): \DateTimeInterface
    {
        return $this->dateCreation;
    }

    public function setDateCreation(\DateTimeInterface $dateCreation): self
    {
        $this->dateCreation = $dateCreation;

        return $this;
    }

    public function getStatut(): string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;

        return $this;
    }

    public function getDisplayName(): string
    {
        return trim($this->prenom . ' ' . $this->nom);
    }

    /**
     * @return Collection<int, ProjetArtistique>
     */
    public function getProjets(): Collection
    {
        return $this->projets;
    }

    /**
     * @return Collection<int, Investissement>
     */
    public function getInvestissements(): Collection
    {
        return $this->investissements;
    }
}
