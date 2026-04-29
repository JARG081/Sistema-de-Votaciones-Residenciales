import { showSuervey, survey } from "./forms/survey.js";
import { sidebarMenu } from "./menu/sideBar.js";

document.addEventListener("DOMContentLoaded", () => {
    
    sidebarMenu('.sidebar-toggler', '.sidebar');
    survey('.btnAgregarRespuesta','.respuestasContainer','mb-3')
    showSuervey('.showFormBtn','.surveyCard','.hideFormBtn','.surveyForm form')
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebarIconActive = document.querySelector('.sidebar-icon-active');
    const sidebarIconInactive = document.querySelector('.sidebar-icon-inactive');

    sidebarToggle.addEventListener('click', () => {
        const sidebar = document.querySelector('.sidebar');
        const isActive = sidebar.classList.contains('active');
        
        // Muestra el SVG activo y oculta el SVG inactivo según el estado del aside
        if (isActive) {
            sidebarIconActive.style.display = 'none';
            sidebarIconInactive.style.display = 'inline'; // O cambia 'inline' por 'block' si prefieres
        } else {
            sidebarIconActive.style.display = 'inline'; // O cambia 'inline' por 'block' si prefieres
            sidebarIconInactive.style.display = 'none';
        }
    });
});
