/**
 * Main JavaScript file for the CV Generator application
 */

document.addEventListener('DOMContentLoaded', function() {
    // Load recent CVs in sidebar
    loadRecentCVs();
    
    // Initialize tooltips
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize any CV form handlers
    initCVFormHandlers();
});

/**
 * Fetches and displays recent CVs in the sidebar
 */
function loadRecentCVs() {
    const recentCvsList = document.getElementById('recentCvsList');
    if (!recentCvsList) return;
    
    // Make an AJAX call to get recent CVs
    fetch('/api/cv/recent')
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to load recent CVs');
            }
            return response.json();
        })
        .then(data => {
            recentCvsList.innerHTML = ''; // Clear loading spinner
            
            if (data.length === 0) {
                recentCvsList.innerHTML = '<li class="nav-item px-3"><p class="text-muted small">No CVs found</p></li>';
                return;
            }
            
            // Add recent CVs to the sidebar
            data.forEach(cv => {
                const li = document.createElement('li');
                li.className = 'nav-item';
                li.innerHTML = `
                    <a class="nav-link text-truncate" href="/cv/${cv.id}" title="${cv.title}">
                        <i class="fas fa-file-alt me-2"></i>
                        ${cv.title}
                    </a>
                `;
                recentCvsList.appendChild(li);
            });
        })
        .catch(error => {
            console.error('Error loading recent CVs:', error);
            recentCvsList.innerHTML = '<li class="nav-item px-3"><p class="text-danger small">Failed to load recent CVs</p></li>';
        });
}

/**
 * Initializes form handlers for CV creation/editing
 */
function initCVFormHandlers() {
    // Dynamic form fields for education
    const educationContainer = document.getElementById('educationContainer');
    const addEducationBtn = document.getElementById('addEducation');
    
    if (addEducationBtn) {
        addEducationBtn.addEventListener('click', function() {
            const index = document.querySelectorAll('.education-entry').length;
            const educationHtml = `
                <div class="education-entry card mb-3">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">Education #${index + 1}</h6>
                        <button type="button" class="btn btn-sm btn-outline-danger remove-education">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Institution</label>
                                <input type="text" class="form-control" name="educations[${index}].institution" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Degree</label>
                                <input type="text" class="form-control" name="educations[${index}].degree" required>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Start Date</label>
                                <input type="date" class="form-control" name="educations[${index}].startDate" required>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">End Date</label>
                                <input type="date" class="form-control" name="educations[${index}].endDate">
                                <div class="form-check mt-2">
                                    <input class="form-check-input" type="checkbox" id="currentEducation${index}">
                                    <label class="form-check-label" for="currentEducation${index}">
                                        Currently studying
                                    </label>
                                </div>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Description</label>
                            <textarea class="form-control" name="educations[${index}].description" rows="2"></textarea>
                        </div>
                    </div>
                </div>
            `;
            
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = educationHtml;
            educationContainer.appendChild(tempDiv.firstElementChild);
            
            // Add event listener to the new remove button
            tempDiv.firstElementChild.querySelector('.remove-education').addEventListener('click', function() {
                this.closest('.education-entry').remove();
                updateEducationIndices();
            });
        });
    }
    
    // Dynamic form fields for experience (similar to education)
    // Add similar code for experience, skills, etc.
}

/**
 * Updates the indices in education form fields after removal
 */
function updateEducationIndices() {
    const educationEntries = document.querySelectorAll('.education-entry');
    educationEntries.forEach((entry, index) => {
        // Update header
        entry.querySelector('h6').textContent = `Education #${index + 1}`;
        
        // Update input names
        const inputs = entry.querySelectorAll('input, textarea');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            if (name) {
                input.setAttribute('name', name.replace(/educations\[\d+\]/, `educations[${index}]`));
            }
        });
        
        // Update checkbox ID
        const checkbox = entry.querySelector('input[type="checkbox"]');
        if (checkbox) {
            checkbox.id = `currentEducation${index}`;
            const label = entry.querySelector(`label[for^="currentEducation"]`);
            if (label) {
                label.setAttribute('for', `currentEducation${index}`);
            }
        }
    });
}