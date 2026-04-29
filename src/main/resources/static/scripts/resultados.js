
// Define tus datos como variables globales
var opciones = [
'OPCION 1 -------------------------------------------------------', 
'OPCION 2 -------------------------------------------------------', 
'OPCION 3 -------------------------------------------------------',
'OPCION 4 -------------------------------------------------------'];
var data = [20, 39, 15, 32];
var totalVotos = data.reduce((a, b) => a + b, 0); // Suma total de votos

var porcentajes = data.map(votos => (votos / totalVotos * 100).toFixed(1));
var porcentajesConSimbolo = porcentajes.map(porcentaje => porcentaje + '%');

var tituloGrafica = 'ESTA USTED DE ACUERDO CON';
var backgroundColors = [
    'rgb(66, 134, 244,0.5)',
    'rgb(74, 135, 72,0.5)',
    'rgb(229, 89, 50,0.5)',
    'rgb(66, 34, 244,0.5)'
];
// Mostrar el título arriba del gráfico


var ctx = document.getElementById("myChart").getContext("2d");
var myChart = new Chart(ctx, {
    type: "pie",
    data: {
        labels: porcentajesConSimbolo,
        datasets: [{
            label: tituloGrafica,
            data: data,
            backgroundColor: backgroundColors
        }]
    },
    options: {
        title: {
            display: false // Deshabilitar el título predeterminado de Chart.js
        },/*
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true
                }
            }]
        }*/
    }
});
// resultados.js
var tituloEncuesta = "Encuesta de Ejemplo:";
var tituloNota = "Notas de la Encuesta";
var contenidoNota = "sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample text sample textsample text sample text sample text sample text sample text sample text sample text sample text sample textsample text sample text sample text sample text sample text sample text sample text sample text sample textsample text sample text sample text sample text sample text sample text sample text sample text sample text.";
var tituloResultado = "Resultado";
var sectionOpciones = document.getElementById("opciones");

// Crea las opciones y los colores al mismo tiempo
// Crea las opciones y los colores al mismo tiempo
for (var i = 0; i < opciones.length; i++) {
    var opcion = opciones[i];
    var color = backgroundColors[i];

    // Crea el div para la opción
    var divOpcion = document.createElement("div");
    divOpcion.classList.add("opcion");

    // Crea el div para el color
    var divColor = document.createElement("div");
    divColor.classList.add("opcion-color");
    divColor.style.backgroundColor = color;

    // Crea el span para el texto de la opción
    var spanOpcion = document.createElement("span");
    spanOpcion.textContent = opcion;

    // Agrega el color y el texto al div de la opción
    divOpcion.appendChild(divColor);
    divOpcion.appendChild(spanOpcion);

    // Agrega la opción al section de opciones
    sectionOpciones.appendChild(divOpcion);
    // Agrega un espacio entre cada opción
    sectionOpciones.appendChild(document.createElement("br"));
}

// Cargar datos en el HTML
document.getElementById("tituloEncuesta").textContent = tituloEncuesta;
document.getElementById("tituloNota").textContent = tituloNota;
document.getElementById("contenidoNota").textContent = contenidoNota;
document.getElementById("tituloResultado").textContent = tituloResultado;
