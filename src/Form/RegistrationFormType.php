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
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Regex;

class RegistrationFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $noHtmlAttr = [
            'required' => true,
            'attr' => ['pattern' => '^[^<>]+$', 'title' => 'HTML tags are not allowed.'],
        ];

        $builder
            ->add('nom', TextType::class, $noHtmlAttr)
            ->add('prenom', TextType::class, $noHtmlAttr)
            ->add('email', EmailType::class, [
                'required' => true,
                'constraints' => [
                    new NotBlank(),
                    new Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.'),
                ],
                'attr' => ['pattern' => '^[^<>]+$'],
            ])
            ->add('userType', ChoiceType::class, [
                'required' => true,
                'choices' => [
                    'Artist' => 'ARTIST',
                    'Investor' => 'INVESTOR',
                ],
                'constraints' => [
                    new NotBlank(),
                ],
            ])
            ->add('plainPassword', PasswordType::class, [
                'mapped' => false,
                'required' => true,
                'constraints' => [
                    new NotBlank(),
                    new Length(min: 8, max: 255),
                    new Regex(pattern: '/^[^<>]+$/u', message: 'HTML tags are not allowed.'),
                ],
                'attr' => ['pattern' => '^[^<>]+$'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}
