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

    #[ORM\Column(length: 20, options: ['default' => 'USER'])]
    #[Assert\Choice(choices: ['USER', 'ADMIN'])]
    private string $role = 'USER';

    #[ORM\Column(name: 'userType', length: 20, nullable: true)]
    #[Assert\Choice(choices: ['ARTIST', 'INVESTOR'])]
    private ?string $userType = null;

    #[ORM\Column(name: 'dateCreation', type: Types::DATETIME_MUTABLE)]
    private \DateTimeInterface $dateCreation;

    #[ORM\Column(length: 20, options: ['default' => 'ACTIF'])]
    #[Assert\Choice(choices: ['ACTIF', 'SUSPENDU', 'BANNI'])]
    private string $statut = 'ACTIF';

    #[ORM\Column(name: 'lastLogin', type: Types::DATETIME_MUTABLE, nullable: true)]
    private ?\DateTimeInterface $lastLogin = null;

    #[ORM\Column(name: 'isVerified', type: Types::BOOLEAN, nullable: true, options: ['default' => false])]
    private ?bool $isVerified = false;

    #[ORM\Column(name: 'profilePicture', length: 255, nullable: true)]
    private ?string $profilePicture = null;

    #[ORM\OneToMany(mappedBy: 'artiste', targetEntity: ProjetArtistique::class)]
    private Collection $projets;

    #[ORM\OneToMany(mappedBy: 'investisseur', targetEntity: Investissement::class)]
    private Collection $investissements;

    #[ORM\ManyToMany(targetEntity: ProjetArtistique::class, inversedBy: 'favoritedByUsers')]
    #[ORM\JoinTable(name: 'user_favorite_projects')]
    #[ORM\JoinColumn(name: 'idUser', referencedColumnName: 'idUser', onDelete: 'CASCADE')]
    #[ORM\InverseJoinColumn(name: 'id_projet', referencedColumnName: 'id_projet', onDelete: 'CASCADE')]
    private Collection $favoriteProjects;

    public function __construct()
    {
        $this->projets = new ArrayCollection();
        $this->investissements = new ArrayCollection();
        $this->favoriteProjects = new ArrayCollection();
        $this->dateCreation = new \DateTime();
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
        if ($this->role === 'ADMIN') {
            return ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_ARTIST', 'ROLE_INVESTOR'];
        }

        if ($this->userType === 'ARTIST') {
            return ['ROLE_USER', 'ROLE_ARTIST'];
        }

        if ($this->userType === 'INVESTOR') {
            return ['ROLE_USER', 'ROLE_INVESTOR'];
        }

        return ['ROLE_USER'];
    }

    public function eraseCredentials(): void {}

    public function getDateCreation(): \DateTimeInterface
    {
        return $this->dateCreation;
    }

    public function getUserType(): ?string
    {
        return $this->userType;
    }

    public function setUserType(?string $userType): self
    {
        $this->userType = $userType;

        return $this;
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

    public function getLastLogin(): ?\DateTimeInterface
    {
        return $this->lastLogin;
    }

    public function setLastLogin(?\DateTimeInterface $lastLogin): self
    {
        $this->lastLogin = $lastLogin;

        return $this;
    }

    public function isVerified(): ?bool
    {
        return $this->isVerified;
    }

    public function setIsVerified(?bool $isVerified): self
    {
        $this->isVerified = $isVerified;

        return $this;
    }

    public function getProfilePicture(): ?string
    {
        return $this->profilePicture;
    }

    public function setProfilePicture(?string $profilePicture): self
    {
        $this->profilePicture = $profilePicture;

        return $this;
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

    /**
     * @return Collection<int, ProjetArtistique>
     */
    public function getFavoriteProjects(): Collection
    {
        return $this->favoriteProjects;
    }

    public function addFavoriteProject(ProjetArtistique $project): self
    {
        if (!$this->favoriteProjects->contains($project)) {
            $this->favoriteProjects->add($project);
        }

        return $this;
    }

    public function removeFavoriteProject(ProjetArtistique $project): self
    {
        $this->favoriteProjects->removeElement($project);

        return $this;
    }
}
