/**
 * Anime Girls Theme - Background Switcher & Particle Effects
 * 动漫美女主题 - 背景切换和粒子特效
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('Anime Girls Theme loaded ✨');
    
    // Initialize background switcher
    initBackgroundSwitcher();
    
    // Initialize particle effects if enabled
    if (document.body.classList.contains('particles-enabled')) {
        initParticles();
    }
    
    // Initialize glow effects
    initGlowEffects();
});

/**
 * Background Switcher
 */
function initBackgroundSwitcher() {
    const backgrounds = ['bg1', 'bg2', 'bg3', 'bg4', 'bg5'];
    let currentBgIndex = 0;
    let autoSwitchInterval = null;
    
    // Get saved background from localStorage
    const savedBg = localStorage.getItem('anime-theme-bg');
    if (savedBg && backgrounds.includes(savedBg)) {
        currentBgIndex = backgrounds.indexOf(savedBg);
    }
    
    // Apply initial background
    applyBackground(backgrounds[currentBgIndex]);
    
    // Create background switcher UI
    createSwitcherUI(backgrounds, currentBgIndex);
    
    // Check if auto-switch is enabled
    const autoSwitch = document.body.dataset.autoSwitch === 'true';
    const switchInterval = parseInt(document.body.dataset.switchInterval) || 30;
    
    if (autoSwitch) {
        startAutoSwitch(backgrounds, switchInterval);
    }
}

function createSwitcherUI(backgrounds, currentIndex) {
    const switcher = document.createElement('div');
    switcher.className = 'bg-switcher';
    switcher.innerHTML = `
        <button class="bg-btn bg-btn-1 ${currentIndex === 0 ? 'active' : ''}" data-bg="bg1" title="Sakura Girl"></button>
        <button class="bg-btn bg-btn-2 ${currentIndex === 1 ? 'active' : ''}" data-bg="bg2" title="Starry Fantasy"></button>
        <button class="bg-btn bg-btn-3 ${currentIndex === 2 ? 'active' : ''}" data-bg="bg3" title="Sunset Beach"></button>
        <button class="bg-btn bg-btn-4 ${currentIndex === 3 ? 'active' : ''}" data-bg="bg4" title="Enchanted Forest"></button>
        <button class="bg-btn bg-btn-5 ${currentIndex === 4 ? 'active' : ''}" data-bg="bg5" title="City Night"></button>
    `;
    
    document.body.appendChild(switcher);
    
    // Add click handlers
    switcher.querySelectorAll('.bg-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const bg = this.dataset.bg;
            applyBackground(bg);
            updateActiveButton(bg);
            localStorage.setItem('anime-theme-bg', bg);
        });
    });
}

function applyBackground(bgClass) {
    // Remove all background classes
    document.body.classList.remove('background-bg1', 'background-bg2', 'background-bg3', 'background-bg4', 'background-bg5');
    // Add new background class
    document.body.classList.add(`background-${bgClass}`);
}

function updateActiveButton(bg) {
    document.querySelectorAll('.bg-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.bg === bg);
    });
}

function startAutoSwitch(backgrounds, intervalSeconds) {
    let index = backgrounds.indexOf(localStorage.getItem('anime-theme-bg')) || 0;
    
    setInterval(() => {
        index = (index + 1) % backgrounds.length;
        const bg = backgrounds[index];
        applyBackground(bg);
        updateActiveButton(bg);
        localStorage.setItem('anime-theme-bg', bg);
    }, intervalSeconds * 1000);
}

/**
 * Particle Effects
 */
function initParticles() {
    const canvas = document.createElement('canvas');
    canvas.id = 'particles-canvas';
    document.body.insertBefore(canvas, document.body.firstChild);
    
    const ctx = canvas.getContext('2d');
    let particles = [];
    const particleCount = 50;
    
    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }
    
    function createParticle() {
        return {
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height,
            size: Math.random() * 4 + 1,
            speedX: (Math.random() - 0.5) * 0.5,
            speedY: (Math.random() - 0.5) * 0.5,
            opacity: Math.random() * 0.5 + 0.2,
            hue: Math.random() * 60 + 300 // Pink to purple range
        };
    }
    
    function initParticlesArray() {
        particles = [];
        for (let i = 0; i < particleCount; i++) {
            particles.push(createParticle());
        }
    }
    
    function animateParticles() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        particles.forEach(p => {
            p.x += p.speedX;
            p.y += p.speedY;
            
            // Wrap around edges
            if (p.x < 0) p.x = canvas.width;
            if (p.x > canvas.width) p.x = 0;
            if (p.y < 0) p.y = canvas.height;
            if (p.y > canvas.height) p.y = 0;
            
            // Draw particle
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
            ctx.fillStyle = `hsla(${p.hue}, 100%, 70%, ${p.opacity})`;
            ctx.fill();
            
            // Draw glow
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size * 2, 0, Math.PI * 2);
            ctx.fillStyle = `hsla(${p.hue}, 100%, 70%, ${p.opacity * 0.3})`;
            ctx.fill();
        });
        
        requestAnimationFrame(animateParticles);
    }
    
    resize();
    initParticlesArray();
    animateParticles();
    
    window.addEventListener('resize', () => {
        resize();
        initParticlesArray();
    });
}

/**
 * Glow Effects
 */
function initGlowEffects() {
    if (!document.body.classList.contains('glow-enabled')) return;
    
    // Add hover glow to interactive elements
    document.querySelectorAll('a, button, .card, .post-card').forEach(el => {
        el.addEventListener('mouseenter', function() {
            this.style.filter = 'drop-shadow(0 0 10px var(--glow-color))';
        });
        el.addEventListener('mouseleave', function() {
            this.style.filter = '';
        });
    });
}

/**
 * Smooth scroll for anchor links
 */
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({ behavior: 'smooth' });
        }
    });
});
