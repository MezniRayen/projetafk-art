<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\PasswordResetToken;
use App\Form\ForgotPasswordRequestType;
use App\Form\ResetPasswordType;
use App\Form\RegistrationFormType;
use App\Repository\PasswordResetTokenRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;

final class SecurityController extends AbstractController
{
    #[Route('/register', name: 'app_register')]
    public function register(Request $request, UserPasswordHasherInterface $passwordHasher, EntityManagerInterface $entityManager): Response
    {
        $user = new User();
        $form = $this->createForm(RegistrationFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $plainPassword = (string) $form->get('plainPassword')->getData();
            if (!$user->getUserType()) {
                $user->setUserType('INVESTOR');
            }
            $user->setRole('USER');
            $user->setStatut('ACTIF');
            $user->setIsVerified(false);
            $user->setDateCreation(new \DateTime());
            $user->setPassword($passwordHasher->hashPassword($user, $plainPassword));
            $entityManager->persist($user);
            $entityManager->flush();

            $this->addFlash('success', 'User created. You can login now.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/register.html.twig', [
            'registrationForm' => $form,
        ]);
    }

    #[Route('/login', name: 'app_login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        return $this->render('security/login.html.twig', [
            'last_username' => $authenticationUtils->getLastUsername(),
            'error' => $authenticationUtils->getLastAuthenticationError(),
        ]);
    }

    #[Route('/forgot-password', name: 'app_forgot_password', methods: ['GET', 'POST'])]
    public function forgotPassword(Request $request, UserRepository $userRepository, PasswordResetTokenRepository $tokenRepository, EntityManagerInterface $entityManager, MailerInterface $mailer): Response
    {
        $form = $this->createForm(ForgotPasswordRequestType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $email = trim((string) $form->get('email')->getData());
            $user = $userRepository->findOneBy(['email' => $email]);

            if ($user instanceof User) {
                $tokenRepository->deleteExistingForUser($user);

                $rawToken = bin2hex(random_bytes(32));
                $token = (new PasswordResetToken())
                    ->setUser($user)
                    ->setTokenHash(hash('sha256', $rawToken))
                    ->setRequestedAt(new \DateTime())
                    ->setExpiresAt((new \DateTime())->modify('+1 hour'));

                $entityManager->persist($token);
                $entityManager->flush();

                $resetUrl = $this->generateUrl('app_reset_password', ['token' => $rawToken], \Symfony\Component\Routing\Generator\UrlGeneratorInterface::ABSOLUTE_URL);

                $emailMessage = (new Email())
                    ->from($_ENV['MAILER_FROM'] ?? 'no-reply@example.com')
                    ->to($user->getEmail())
                    ->subject('Reset your password')
                    ->html($this->renderView('security/emails/reset_password.html.twig', [
                        'resetUrl' => $resetUrl,
                    ]));

                $mailer->send($emailMessage);
            }

            $this->addFlash('success', 'If the email exists, a reset link has been sent.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/forgot_password.html.twig', [
            'form' => $form,
        ]);
    }

    #[Route('/reset-password/{token}', name: 'app_reset_password', methods: ['GET', 'POST'])]
    public function resetPassword(string $token, Request $request, PasswordResetTokenRepository $tokenRepository, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher): Response
    {
        $tokenEntity = $tokenRepository->findValidByHash(hash('sha256', $token));
        if (!$tokenEntity instanceof PasswordResetToken) {
            return $this->render('security/reset_password.html.twig', [
                'form' => $this->createForm(ResetPasswordType::class),
                'error' => 'This reset link is invalid or expired.',
            ]);
        }

        $form = $this->createForm(ResetPasswordType::class);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $plainPassword = (string) $form->get('plainPassword')->getData();
            $plainPasswordConfirm = (string) $form->get('plainPasswordConfirm')->getData();

            if ($plainPassword !== $plainPasswordConfirm) {
                return $this->render('security/reset_password.html.twig', [
                    'form' => $form,
                    'error' => 'Passwords do not match.',
                ]);
            }

            $user = $tokenEntity->getUser();
            if (!$user instanceof User) {
                return $this->render('security/reset_password.html.twig', [
                    'form' => $form,
                    'error' => 'User not found.',
                ]);
            }

            $user->setPassword($passwordHasher->hashPassword($user, $plainPassword));
            $tokenEntity->setUsedAt(new \DateTime());
            $entityManager->flush();

            $this->addFlash('success', 'Your password has been updated.');

            return $this->redirectToRoute('app_login');
        }

        return $this->render('security/reset_password.html.twig', [
            'form' => $form,
            'error' => null,
        ]);
    }

    #[Route('/logout', name: 'app_logout')]
    public function logout(): void
    {
        throw new \LogicException('Handled by firewall logout.');
    }
}
