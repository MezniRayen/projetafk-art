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
            ->add('email', EmailType::class, ['required' => true])
            ->add('role', ChoiceType::class, [
                'choices' => [
                    'Artiste' => 'ARTISTE',
                    'Investisseur' => 'INVESTISSEUR',
                    'Admin' => 'ADMIN',
                ],
                'required' => true,
            ])
            ->add('plainPassword', PasswordType::class, [
                'mapped' => false,
                'required' => true,
                'constraints' => [
                    new NotBlank(),
                    new Length(min: 6, max: 255),
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
