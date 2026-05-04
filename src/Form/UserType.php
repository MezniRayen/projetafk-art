<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class UserType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('prenom', TextType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('email', EmailType::class, ['required' => true])
            ->add('role', ChoiceType::class, [
                'required' => true,
                'choices' => [
                    'Admin' => 'ADMIN',
                    'User' => 'USER',
                ],
            ])
            ->add('userType', ChoiceType::class, [
                'required' => false,
                'placeholder' => 'Select type for USER',
                'choices' => [
                    'Artist' => 'ARTIST',
                    'Investor' => 'INVESTOR',
                ],
            ])
            ->add('plainPassword', PasswordType::class, [
                'mapped' => false,
                'required' => $options['password_required'],
                'attr' => ['pattern' => '^[^<>]+$'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
            'password_required' => true,
        ]);

        $resolver->setAllowedTypes('password_required', 'bool');
    }
}
