// Sidebar state management across pages
(function() {
    // Save sidebar state to localStorage
    function saveSidebarState(isOpen) {
        localStorage.setItem('sidebarState', isOpen ? 'open' : 'closed');
    }
    
    // Get sidebar state from localStorage
    function getSidebarState() {
        return localStorage.getItem('sidebarState') || 'closed';
    }
    
    // Apply sidebar state
    function applySidebarState() {
        const sidebar = document.getElementById('sidebar');
        const state = getSidebarState();
        
        if (!sidebar) return;
        
        if (state === 'open') {
            sidebar.classList.remove('inactive');
            document.body.classList.remove('sidebar-inactive');
        } else {
            sidebar.classList.add('inactive');
            document.body.classList.add('sidebar-inactive');
        }
    }
    
    // Override toggleSidebar function to save state
    const originalToggleSidebar = window.toggleSidebar;
    window.toggleSidebar = function() {
        if (originalToggleSidebar) {
            originalToggleSidebar();
        }
        
        const sidebar = document.getElementById('sidebar');
        if (sidebar) {
            const isOpen = !sidebar.classList.contains('inactive');
            saveSidebarState(isOpen);
        }
    };
    
    // Apply state on page load
    document.addEventListener('DOMContentLoaded', function() {
        // Special case for index.html - always start open
        if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/') {
            saveSidebarState(true);
        }
        applySidebarState();
    });
})();
