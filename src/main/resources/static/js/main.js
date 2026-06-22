/**
 * main.js — Frontend JavaScript for LinkSnip URL Shortener
 *
 * Handles:
 * - Form UX enhancements (URL validation feedback)
 * - Clipboard copy functionality
 * - Dynamic UI updates
 */

// ── Initialize on DOM Ready ──────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    initUrlInput();
    initExpiryDateMin();
    highlightCurrentNav();
});

/**
 * Real-time URL validation feedback.
 * Shows a green/red indicator as the user types their URL.
 */
function initUrlInput() {
    const urlInput = document.getElementById('originalUrl');
    if (!urlInput) return;

    urlInput.addEventListener('input', function () {
        const value = this.value.trim();

        if (value === '') {
            this.style.borderColor = '';  // Reset to default
            return;
        }

        // Check if it looks like a valid URL
        const isValid = value.startsWith('http://') || value.startsWith('https://');
        this.style.borderColor = isValid ? '#10b981' : '#ef4444';
    });
}

/**
 * Set the minimum datetime for the expiry field to now.
 * Prevents users from selecting past expiry dates.
 */
function initExpiryDateMin() {
    const expiryInput = document.getElementById('expiresAt');
    if (!expiryInput) return;

    // Format current date as datetime-local input expects: "2024-01-15T10:30"
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');

    expiryInput.min = `${year}-${month}-${day}T${hours}:${minutes}`;
}

/**
 * Add "active" class to the current nav link.
 */
function highlightCurrentNav() {
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-links a').forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.style.color = 'var(--primary)';
            link.style.fontWeight = '700';
        }
    });
}

/**
 * Copy the short URL to clipboard.
 * Called from the result.html page.
 */
function copyToClipboard() {
    const urlInput = document.getElementById('shortUrlValue');
    if (!urlInput) return;

    navigator.clipboard.writeText(urlInput.value)
        .then(() => {
            const btn = document.getElementById('copyBtn');
            if (btn) {
                btn.textContent = '✅ Copied!';
                btn.classList.add('copied');
                setTimeout(() => {
                    btn.textContent = '📋 Copy';
                    btn.classList.remove('copied');
                }, 2000);
            }
        })
        .catch(err => {
            // Fallback for older browsers
            console.error('Clipboard copy failed:', err);
            alert('Copy this URL: ' + urlInput.value);
        });
}

/**
 * Auto-dismiss alerts after 5 seconds.
 */
(function autoDismissAlerts() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });
})();
