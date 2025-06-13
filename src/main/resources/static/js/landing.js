document.addEventListener('DOMContentLoaded', function() {
    // Theme toggle
    const themeToggle = document.getElementById('theme-toggle');
    const themeIcon = themeToggle ? themeToggle.querySelector('i') : null;
    
    // Initialize theme from localStorage
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.body.setAttribute('data-bs-theme', savedTheme);
    document.body.setAttribute('data-theme', savedTheme);
    
    // Update icon based on current theme
    if (themeIcon) {
        themeIcon.className = savedTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
    }
    
    // Toggle theme on button click
    if (themeToggle) {
        themeToggle.addEventListener('click', function() {
            const currentTheme = document.body.getAttribute('data-bs-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            
            // Update theme attributes
            document.body.setAttribute('data-bs-theme', newTheme);
            document.body.setAttribute('data-theme', newTheme);
            
            // Update icon
            themeIcon.className = newTheme === 'dark' ? 'fas fa-sun' : 'fas fa-moon';
            
            // Save preference
            localStorage.setItem('theme', newTheme);
        });
    }
    
    // Mobile menu toggle (if exists)
    const mobileMenuBtn = document.getElementById('mobile-menu-btn');
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', function() {
            const navLinks = document.querySelector('.nav-links');
            navLinks.classList.toggle('show');
        });
    }
});
