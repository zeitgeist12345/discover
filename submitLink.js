async function submitLink(event) {
    event.preventDefault();

    // Clear any existing errors
    hideModalError();
    clearAllInputErrors();

    // Check if form is valid
    const form = event.target;
    if (!form.checkValidity()) {
        // Trigger validation for the first invalid field
        const firstInvalid = form.querySelector(':invalid');
        if (firstInvalid) {
            firstInvalid.focus();
            handleInvalidInput({ target: firstInvalid, preventDefault: () => { } });
        }
        return;
    }

    const submitBtn = document.getElementById('submit-btn');
    const originalText = submitBtn.textContent;

    // Show loading state
    submitBtn.disabled = true;
    submitBtn.classList.add('btn-loading');
    submitBtn.textContent = 'Adding...';

    const tagsInput = document.getElementById('add-link-tags');
    selectedTags = tagsInput.value.split(",")
        .map(tag => tag.trim().replace(/\s+/g, "")) // remove internal spaces
        .filter(tag => tag.length > 0);

    try {
        const formData = new FormData(event.target);
        const linkData = {
            name: formData.get('name'),
            url: normalizeUrl(formData.get('url')),
            description: formData.get('description'),
            tags: selectedTags.length > 0 ? selectedTags : ['user-submitted'],
            views: 0,
            likesDesktop: 0,
            dislikesDesktop: 0
        };

        const response = await fetch(`${API_BASE_URL}/addlink`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(linkData)
        });

        const result = await response.json();

        if (response.ok) {
            // Success
            showSuccessMessage('Link submitted for spam review successfully! The link will be live globally after review and approval 🎉');
            hideAddLinkForm();

            // Reset tags
            selectedTags = [];
        } else {
            // Handle specific error cases
            if (response.status === 409) {
                showModalError('This link already exists in the database. Please try a different URL.');
            } else {
                showModalError(result.error || 'Failed to add link');
            }
        }

    } catch (error) {
        console.error('Error submitting link:', error);
        showModalError('Failed to add link. Please try again.');
    } finally {
        // Reset button state
        submitBtn.disabled = false;
        submitBtn.classList.remove('btn-loading');
        submitBtn.textContent = originalText;
    }
}