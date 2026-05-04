<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\File;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\Regex;

class ProfileFormType extends AbstractType
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
            ->add('email', EmailType::class, [
                'required' => true,
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('newPassword', PasswordType::class, [
                'mapped' => false,
                'required' => false,
                'label' => 'New password (optional)',
                'constraints' => [
                    new Length(min: 8, max: 255),
                    new Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.'),
                ],
                'empty_data' => '',
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('profilePictureFile', FileType::class, [
                'mapped' => false,
                'required' => false,
                'label' => 'Profile picture',
                'constraints' => [
                    new File([
                        'maxSize' => '5M',
                        'mimeTypes' => ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
                        'mimeTypesMessage' => 'Please upload a valid image (jpg, png, webp, gif).',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}
