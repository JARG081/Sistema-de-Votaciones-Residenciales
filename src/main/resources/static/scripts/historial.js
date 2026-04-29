const nombreUsuario = "Juan";
const apellidoUsuario = "Pérez";

// Obtener referencias a los elementos de nombre y apellido
const nombreElemento = document.getElementById("nombre");
const apellidoElemento = document.getElementById("apellido");

// Actualizar el contenido de los elementos con los valores del usuario
nombreElemento.textContent = nombreUsuario;
apellidoElemento.textContent = apellidoUsuario;// Datos de ejemplo de las votaciones


const votaciones = [
    { titulo: "Votación 1", fecha: "25/04/2024", estado: "proximo" },
    { titulo: "Votación 2", fecha: "16/04/2024", estado: "en curso" },
    { titulo: "Votación 3", fecha: "10/03/2024", estado: "finalizado" },
    { titulo: "Votación 4", fecha: "23/03/2024", estado: "finalizado" },
    { titulo: "Votación 5", fecha: "03/02/2024", estado: "finalizado" },
    { titulo: "Votación 6", fecha: "03/01/2024", estado: "finalizado" }

];

// Función para crear los elementos HTML de las votaciones
// Función para crear los elementos HTML de las votaciones
function crearVotaciones() {
    const contenedor = document.getElementById("contenedor-votaciones");

    votaciones.forEach(votacion => {
        const divVotacion = document.createElement("div");
        divVotacion.classList.add("votacion");

        const estado = votacion.estado.toLowerCase();
        divVotacion.style.backgroundColor = estado === "en curso" ? "#5fde68" : estado === "proximo" ? "#dbc64b" : "#b0afac";

        const titulo = document.createElement("h3");
        titulo.textContent = votacion.titulo.toUpperCase(); // Convertir a mayúsculas

        const detalle = document.createElement("p");
        detalle.textContent = `Estado: ${votacion.estado}, Fecha: ${votacion.fecha}`;

        const botonDetalle = document.createElement("button");
        botonDetalle.textContent = "Ver Detalle"; // Texto del botón
        botonDetalle.addEventListener("click", function() {
            window.location.href = "resultados.html"; // Redirigir al detalle de la votación
        });

        divVotacion.appendChild(titulo);
        divVotacion.appendChild(detalle);
        divVotacion.appendChild(botonDetalle);

        contenedor.appendChild(divVotacion);
    });
}


// Llamar a la función para crear las votaciones cuando se cargue la página
window.addEventListener("DOMContentLoaded", crearVotaciones);
