document.addEventListener("DOMContentLoaded", function() {

    localStorage.removeItem("opcionSeleccionada");
    // Define la pregunta y opciones
    var pregunta = "¿Lorem ipsum dolor sit amet, consectetur adipiscing elit.?";
    var opciones = [
    "1. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis nec libero sed metus ultrices tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer lacinia augue at risus fringilla, nec aliquet enim aliquam. Ut condimentum sapien et sem aliquet, at hendrerit velit tincidunt. Nam scelerisque erat at nisi interdum feugiat. Nulla facilisi. Phasellus eleifend turpis sed tellus dapibus, ac vestibulum lectus ultrices. Pellentesque vitae mauris a nunc consequat commodo. Suspendisse volutpat ex et efficitur eleifend. Integer vehicula justo a velit fermentum, non cursus magna laoreet. In hac habitasse platea dictumst.", 
    "2. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis nec libero sed metus ultrices tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer lacinia augue at risus fringilla, nec aliquet enim aliquam. Ut condimentum sapien et sem aliquet, at hendrerit velit tincidunt. Nam scelerisque erat at nisi interdum feugiat. Nulla facilisi. Phasellus eleifend turpis sed tellus dapibus, ac vestibulum lectus ultrices. Pellentesque vitae mauris a nunc consequat commodo. Suspendisse volutpat ex et efficitur eleifend. Integer vehicula justo a velit fermentum, non cursus magna laoreet. In hac habitasse platea dictumst.", 
    "3. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis nec libero sed metus ultrices tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer lacinia augue at risus fringilla, nec aliquet enim aliquam. Ut condimentum sapien et sem aliquet, at hendrerit velit tincidunt. Nam scelerisque erat at nisi interdum feugiat. Nulla facilisi. Phasellus eleifend turpis sed tellus dapibus, ac vestibulum lectus ultrices. Pellentesque vitae mauris a nunc consequat commodo. Suspendisse volutpat ex et efficitur eleifend. Integer vehicula justo a velit fermentum, non cursus magna laoreet. In hac habitasse platea dictumst.", 
    "4. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis nec libero sed metus ultrices tristique. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer lacinia augue at risus fringilla, nec aliquet enim aliquam. Ut condimentum sapien et sem aliquet, at hendrerit velit tincidunt. Nam scelerisque erat at nisi interdum feugiat. Nulla facilisi. Phasellus eleifend turpis sed tellus dapibus, ac vestibulum lectus ultrices. Pellentesque vitae mauris a nunc consequat commodo. Suspendisse volutpat ex et efficitur eleifend. Integer vehicula justo a velit fermentum, non cursus magna laoreet. In hac habitasse platea dictumst."];

    // Muestra la pregunta y opciones en la página
    document.getElementById("pregunta").textContent = pregunta;
    var opcionesDiv = document.getElementById("opciones");
    var confirmButton = document.createElement("button");
    confirmButton.textContent = "Confirmar Elección";
    confirmButton.style.display = "none"; // Ocultamos el botón inicialmente

    opciones.forEach(function(opcion, index) {

        var button = document.createElement("button");
        button.textContent = opcion;
        button.addEventListener("click", function() {
            // Resalta la opción seleccionada
            opcionesDiv.querySelectorAll("button").forEach(function(btn) {
                btn.classList.remove("selected");
            });
            button.classList.add("selected");

            // Mostrar el botón de confirmación
            confirmButton.style.display = "block";
        });
        opcionesDiv.appendChild(button);
    });
    
    // Agregar evento al botón de confirmación

    confirmButton.addEventListener("click", function() {
        // Aquí puedes agregar el código para guardar el voto
        var opcionSeleccionada = opcionesDiv.querySelector(".selected").textContent;
        console.log("Has votado por la opción " + opcionSeleccionada);
        // Almacenar la opción seleccionada en localStorage
        localStorage.setItem("opcionSeleccionada", opcionSeleccionada);
        console.log("Opción seleccionada almacenada en localStorage: " + opcionSeleccionada);
        // Redirige a home.html
        window.location.href = "home.html";
    });

    // Agregar el botón de confirmación al final de las opciones
    opcionesDiv.appendChild(confirmButton);
    confirmButton.id = "confirmarButton";
});
