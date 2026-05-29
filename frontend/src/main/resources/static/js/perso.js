// Helper: normalise une immatriculation (MAJ, retire caractères non alphanumériques,
// ajoute des tirets au bon endroit et tronque si trop longue)
function normalizeImmatString(raw) {
    let v = (raw || '').toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (v.length > 2) v = v.slice(0, 2) + '-' + v.slice(2);
    if (v.length > 6) v = v.slice(0, 6) + '-' + v.slice(6);
    if (v.length > 8) v = v.slice(0, 9);
    return v;
}

// formatte l'immatriculation en ajoutant des tirets en en mettant en majuscule
function formatImmatriculation(input) {
    input.value = normalizeImmatString(input.value);
}

// vérifie dynamiquement l'affichage d'erreur d'immatriculation et empêche la saisie
function checkImmat(input) {
    let value = normalizeImmatString(input.value);
    let isValid = true;

    for (let i = 0; i < value.length; i++) {
        if (i < 2 || (i > 6 && i <= 9)) {
            if (!/[A-Z]/.test(value[i])) { isValid = false; break; }
        } else if (i > 2 && i < 6) {
            if (!/[0-9]/.test(value[i])) { isValid = false; break; }
        }
    }

    if (isValid) {
        input.classList.remove('validation-champ-surimpression');
        let errorElement = input.parentNode.querySelector('.immatriculationError');
        if (errorElement) { errorElement.remove(); }
    } else {
        input.classList.add('validation-champ-surimpression');
        if (!input.parentNode.querySelector('.immatriculationError')) {
            let errorDiv = document.createElement('div');
            errorDiv.className = 'immatriculationError is-invalid validation-champ-incorrect';
            errorDiv.textContent = "Le format requis doit être de type 'AA-123-AA'. Merci de vérifier l'immatriculation saisie.";
            input.parentNode.appendChild(errorDiv);
        }
    }

    input.value = value;
}

// variante utilisée par le formulaire SIV: supprime l'affichage d'erreur si champ vide ou format ok
function checkImmatError(input) {
    let value = input.value || '';
    const err = input.parentNode.querySelector('.immatriculationError');
    if (value.length === 0) {
        input.classList.remove('validation-champ-surimpression');
        if (err) err.remove();
        return;
    }
    if (/^[A-Z]{2}-\d{3}-[A-Z]{2}$/.test(value)) {
        input.classList.remove('validation-champ-surimpression');
        if (err) err.remove();
    }
}

// grise le bouton de validation si l'immat existe
function checkImmatExists(input) {
    let immatriculation = input.value;
    fetch(`/queries/vehiculeExists/${immatriculation}`)
        .then(response => response.json())
        .then(data => {
            const existsEl = input.parentNode.querySelector('.immatriculationExisteError');
            const form = input.closest('form');
            const submitBtn = form ? form.querySelector('button[type=submit]') : null;
            if (data) {
                if (existsEl) existsEl.style.display = 'block';
                if (submitBtn) submitBtn.disabled = true;
            } else {
                if (existsEl) existsEl.style.display = 'none';
                if (submitBtn) submitBtn.disabled = false;
            }
        })
        .catch(error => { console.error('Error:', error); });
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
