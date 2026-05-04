<?php

namespace App\Command;

use App\Entity\User;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(name: 'app:seed-test-users', description: 'Create or update test users for admin/artist/investor.')]
class SeedTestUsersCommand extends Command
{
    public function __construct(
        private readonly EntityManagerInterface $entityManager,
        private readonly UserRepository $userRepository,
        private readonly UserPasswordHasherInterface $passwordHasher,
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $users = [
            [
                'email' => 'admin@afkart.com',
                'nom' => 'System',
                'prenom' => 'Admin',
                'role' => 'ADMIN',
                'userType' => null,
                'password' => 'Admin123!',
            ],
            [
                'email' => 'artiste@test.com',
                'nom' => 'Test',
                'prenom' => 'Artiste',
                'role' => 'USER',
                'userType' => 'ARTIST',
                'password' => 'Artist123!',
            ],
            [
                'email' => 'invest@test.com',
                'nom' => 'Test',
                'prenom' => 'Investisseur',
                'role' => 'USER',
                'userType' => 'INVESTOR',
                'password' => 'Investor123!',
            ],
        ];

        foreach ($users as $data) {
            $user = $this->userRepository->findOneBy(['email' => $data['email']]);
            if (!$user instanceof User) {
                $user = new User();
                $user->setEmail($data['email']);
                $this->entityManager->persist($user);
            }

            $user->setNom($data['nom']);
            $user->setPrenom($data['prenom']);
            $user->setRole($data['role']);
            $user->setUserType($data['userType']);
            $user->setStatut('ACTIF');
            $user->setIsVerified(true);
            $user->setDateCreation(new \DateTime());
            $user->setPassword($this->passwordHasher->hashPassword($user, $data['password']));

            $output->writeln(sprintf('Seeded: %s (%s) / password: %s', $data['email'], $data['role'], $data['password']));
        }

        $this->entityManager->flush();

        $output->writeln('Done.');

        return Command::SUCCESS;
    }
}
