// formatte l'immatriculation en ajoutant des tirets en en mettant en majuscule, avec un format particulier
function formatImmatriculation(input) {
    let value = input.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (value.length > 2) {
        value = value.slice(0, 2) + '-' + value.slice(2);
    }
    if (value.length > 6) {
        value = value.slice(0, 6) + '-' + value.slice(6);
    }
    if (value.length > 8) {
        value = value.slice(0, 9);
    }
    input.value = value;
}

//verifie dynamiquement l'affichage d'erreur d'immatriculation et en empeche la saisie
function checkImmat(input) {
    let value = input.value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    let isValid = true;

    if (value.length > 2) {
        value = value.slice(0, 2) + '-' + value.slice(2);
    }
    if (value.length > 6) {
        value = value.slice(0, 6) + '-' + value.slice(6);
    }
    if (value.length > 8) {
        value = value.slice(0, 9);
    }
//check erreur de lettre
    for (let i = 0; i < value.length; i++) {
        if (i < 2 || (i > 6 && i <= 9)) {
            if (!/[A-Z]/.test(value[i])) {
                isValid = false;
                break;
            }
            //check erreur de chiffre
        } else if (i > 2 && i < 6) {
            if (!/[0-9]/.test(value[i])) {
                isValid = false;
                break;
            }
        }
    }

    if (isValid) {
        input.classList.remove('validation-champ-surimpression');
        let errorElement = document.getElementById('immatriculationError');
        if (errorElement) {
            errorElement.remove();
        }
    } else {
        input.classList.add('validation-champ-surimpression');
        if (!document.getElementById('immatriculationError')) {
            let errorDiv = document.createElement('div');
            errorDiv.id = 'immatriculationError';
            errorDiv.className = 'is-invalid validation-champ-incorrect';
            errorDiv.textContent = "Le format requis doit être de type 'AA-123-AA'. Merci de vérifier l'immatriculation saisie.";
            input.parentNode.appendChild(errorDiv);
        }
    }

    input.value = value;
}

// formatte le numero de tel
function formatTel(input) {
    // Remove all non-digit characters
    let cleaned = input.value.replace(/\D/g, '');

    // Limite à 10 vhiffres pouvant etre saisis
    if (cleaned.length > 10) {
        cleaned = cleaned.substring(0, 10);
    }

    // Formatte le numéro de téléphone de 2x5 chiffres
    // let formatted = cleaned.match(/.{1,2}/g)?.join(' ') || '';

    input.value = cleaned;
}

//formatte le code postal
function formatCp(input) {
    // Remove all non-digit characters
    let cleaned = input.value.replace(/\D/g, '');

    // Limite à 5 chiffres pouvant etre saisis
    if (cleaned.length > 5) {
        cleaned = cleaned.substring(0, 5);
    }
    input.value = cleaned;
}

//force les lettres en minuscule  pour les emails
function forcerminuscule(input) {
    input.value = input.value.toLowerCase();
}

// A n'utiliser que si le formulaire ne contient qu'une seule date à valider
function validationDeDate(input) {
    const saisieDate = new Date(input.value);
    const minDate = new Date('1950-01-01');
    const maxDate = new Date();

    // Validate creation date
    if (saisieDate < minDate || saisieDate > maxDate) {
        input.setCustomValidity('La date doit être comprise entre 1950 et aujourd\'hui.');
    } else {
        input.setCustomValidity('');
    }
}
