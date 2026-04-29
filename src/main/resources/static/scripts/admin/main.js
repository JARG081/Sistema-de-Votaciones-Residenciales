import { showSuervey, survey } from "../forms/survey.js";

document.addEventListener("DOMContentLoaded", () => {
    // Inicializar funcionalidad de encuestas
    survey('.btnAgregarRespuesta', '.respuestasContainer', 'mb-3');
    showSuervey('.showFormBtn', '.surveyCard', '.hideFormBtn', '.surveyForm form');
    
    // Validación y formateo de métricas
    formatMetrics();
});

/**
 * Formatea y valida las métricas mostradas en el dashboard
 * Asegura que no se muestren valores negativos o inválidos
 */
function formatMetrics() {
    const metricElements = document.querySelectorAll('.metric-value');
    
    metricElements.forEach(element => {
        const value = parseFloat(element.textContent);
        
        // Validar que el valor sea un número válido y mayor a 0
        if (isNaN(value) || value <= 0) {
            element.classList.add('metric-no-data');
            element.textContent = 'Sin datos disponibles';
        } else {
            // Formatear el número con máximo 2 decimales
            const formattedValue = value.toFixed(2);
            element.textContent = formattedValue;
        }
    });
}

/**
 * Función auxiliar para formatear números grandes con separadores
 * Ejemplo: 1000 -> 1,000
 */
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}
