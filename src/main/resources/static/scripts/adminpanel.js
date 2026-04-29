const myChart= document.querySelector(".my-chart");
const chartData = {
labels: ["Lorem 1","Lorem 2", "Lorem 3", "Lorem 4"],
data: [30, 17, 10, 7],
};

new Chart(myChart,{
    type: "doughnut",
    data:{
        labels: chartData.labels,
        datasets: [
            {
                label:"Votos Registrados",
                data: chartData.data
            }
        ]
    }

})

document.getElementById('boton-crear-encuesta').addEventListener('click', function() {
    mostrarSweetAlertCrearEncuesta();
});

function mostrarSweetAlertCrearEncuesta() {
    Swal.fire({
        title: 'Crear Encuesta',
        html: `
            <input type="text" id="pregunta" class="swal2-input" placeholder="Pregunta">
            <input type="text" id="opcion1" class="swal2-input" placeholder="Opción de respuesta 1">
            <input type="text" id="opcion2" class="swal2-input" placeholder="Opción de respuesta 2">
            <input type="text" id="opcion3" class="swal2-input" placeholder="Opción de respuesta 3">
            <input type="text" id="opcion4" class="swal2-input" placeholder="Opción de respuesta 4">
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Crear encuesta',
        cancelButtonText: 'Cancelar',
        reverseButtons: true,
        preConfirm: () => {
            const pregunta = document.getElementById('pregunta').value;
            const opcion1 = document.getElementById('opcion1').value;
            const opcion2 = document.getElementById('opcion2').value;
            const opcion3 = document.getElementById('opcion3').value;
            const opcion4 = document.getElementById('opcion4').value;

            // Validar si algún campo está vacío
            if (!pregunta || !opcion1 || !opcion2 || !opcion3 || !opcion4) {
                Swal.showValidationMessage('Por favor, complete todos los campos');
                return false; // Evitar que se cierre el diálogo si algún campo está vacío
            }

            return { pregunta: pregunta, opciones: [opcion1, opcion2, opcion3, opcion4] };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const { pregunta, opciones } = result.value;
            // Aquí puedes agregar la lógica para crear la encuesta con los datos ingresados
            Swal.fire('Encuesta creada', `Se ha creado la siguiente encuesta:
Pregunta: ${pregunta}
Opciones de respuesta:
1. ${opciones[0]}
2. ${opciones[1]}
3. ${opciones[2]}
4. ${opciones[3]}`, 'success');
        }
    });
}


document.querySelectorAll('.boton-rojo').forEach((boton) => {
    boton.addEventListener('click', function() {
        var textoBoton = boton.textContent.trim();

        switch (textoBoton) {
            case 'Agregar usuario':
                mostrarSweetAlertAgregarUsuario();
                break;
            case 'Eliminar usuario':
                mostrarSweetAlertEliminarUsuario();
                break;
            case 'Actualizar usuario':
                mostrarSweetAlertActualizarUsuario();
                break;
            case 'Dar permisos a usuario':
                mostrarSweetAlertDarPermisos();
                break;
            default:
                break;
        }
    });
});


// Función para mostrar el SweetAlert al hacer clic en el botón "Agregar usuario"

function mostrarSweetAlertAgregarUsuario() {
    Swal.fire({
        title: 'Agregar Usuario',
        html: `
            <input type="text" id="nombre-usuario" class="swal2-input" placeholder="Nombre">
            <input type="email" id="correo-usuario" class="swal2-input" placeholder="Correo">
            <input type="number" style="-moz-appearance: textfield" id="documento-usuario" class="swal2-input" placeholder="Documento">
            <input type="tel" id="telefono-usuario" class="swal2-input" placeholder="Teléfono">
        `,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, agregar usuario',
        cancelButtonText: 'Cancelar',
        reverseButtons: true,
        preConfirm: () => {
            const nombreUsuario = document.getElementById('nombre-usuario').value;
            const correoUsuario = document.getElementById('correo-usuario').value;
            const documentoUsuario = document.getElementById('documento-usuario').value;
            const telefonoUsuario = document.getElementById('telefono-usuario').value;
            
            // Validar si algún campo está vacío
            if (!nombreUsuario || !correoUsuario || !documentoUsuario || !telefonoUsuario) {
                Swal.showValidationMessage('Por favor, complete todos los campos');
                return false; // Evitar que se cierre el diálogo si algún campo está vacío
            }
            
            return { nombre: nombreUsuario, correo: correoUsuario, documento: documentoUsuario, telefono: telefonoUsuario };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const { nombre, correo, documento, telefono } = result.value;
            // Aquí puedes agregar la lógica para agregar un usuario con los datos ingresados
            Swal.fire('Usuario agregado', `Se ha agregado el usuario:
Nombre: ${nombre}
Correo: ${correo}
Documento: ${documento}
Teléfono: ${telefono}`, 'success');
        }
    });
}



function mostrarSweetAlertEliminarUsuario() {
    Swal.fire({
        title: 'Eliminar Usuario',
        html: '<input type="number" style="-moz-appearance: textfield" id="documento-usuario" class="swal2-input" placeholder="Ingrese el documento del usuario">',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Eliminar usuario',
        cancelButtonText: 'Cancelar',
        reverseButtons: true,
        preConfirm: () => {
            const documentoUsuario = document.getElementById('documento-usuario').value;
            
            // Validar si el campo está vacío
            if (!documentoUsuario.trim()) {
                Swal.showValidationMessage('Por favor, complete el campo');
                return false; // Evitar que se cierre el diálogo si el campo está vacío
            }
            
            return documentoUsuario;
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const documentoUsuario = result.value;
            // Aquí puedes agregar la lógica para eliminar un usuario con el documento ingresado
            Swal.fire('Usuario eliminado', 'Se ha eliminado el usuario con documento: ' + documentoUsuario, 'success');
        }
    });
}

// Función para mostrar el SweetAlert al hacer clic en el botón "Eliminar usuario"


// Función para mostrar el SweetAlert al hacer clic en el botón "Editar usuario"
function mostrarSweetAlertActualizarUsuario() {
    Swal.fire({
        title: 'Editar Usuario',
        html: `
            <input type="text" id="nombre-usuario" class="swal2-input" placeholder="Nombre">
            <input type="email" id="correo-usuario" class="swal2-input" placeholder="Correo">
            <input type="number" style="-moz-appearance: textfield" id="documento-usuario" class="swal2-input" placeholder="Documento">
            <input type="tel" id="telefono-usuario" class="swal2-input" placeholder="Teléfono">
        `,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: 'Guardar',
        cancelButtonText: 'Cancelar',
        reverseButtons: true,
        preConfirm: () => {
            const nombreUsuario = document.getElementById('nombre-usuario').value;
            const correoUsuario = document.getElementById('correo-usuario').value;
            const documentoUsuario = document.getElementById('documento-usuario').value;
            const telefonoUsuario = document.getElementById('telefono-usuario').value;
            if (!nombreUsuario || !correoUsuario || !documentoUsuario || !telefonoUsuario) {
                Swal.showValidationMessage('Por favor, complete todos los campos');
                return false; // Evitar que se cierre el diálogo si los campos no están completos
            }
            return { nombre: nombreUsuario, correo: correoUsuario, documento: documentoUsuario, telefono: telefonoUsuario };
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const { nombre, correo, documento, telefono } = result.value;
            // Aquí puedes agregar la lógica para editar un usuario con los datos ingresados
            Swal.fire('Usuario actualizado', `Se han actualizado los datos del usuario:
Nombre: ${nombre}
Correo: ${correo}
Documento: ${documento}
Teléfono: ${telefono}`, 'success');
        }
    });
}



// Función para mostrar el SweetAlert al hacer clic en el botón "Dar permisos a usuario"
function mostrarSweetAlertDarPermisos() {
    Swal.fire({
        title: 'Dar Permisos a Usuario',
        html: '<input type="number" style="-moz-appearance: textfield" id="documento-usuario" class="swal2-input" placeholder="Ingrese el documento del usuario">',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, dar permisos',
        cancelButtonText: 'Cancelar',
        reverseButtons: true,
        preConfirm: () => {
            const documentoUsuario = document.getElementById('documento-usuario').value;
            if (!documentoUsuario.match(/^\d+$/)) {
                Swal.showValidationMessage('Por favor, ingrese solo números');
            }
            return documentoUsuario;
        }
    }).then((result) => {
        if (result.isConfirmed) {
            const documentoUsuario = result.value;
            // Aquí puedes agregar la lógica para dar permisos a un usuario con el documento ingresado
            Swal.fire('Permisos otorgados', 'Se han otorgado permisos al usuario con documento: ' + documentoUsuario, 'success');
        }
    });
}
