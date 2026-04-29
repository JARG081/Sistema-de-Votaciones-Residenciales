 function sidebarMenu(sidebarBtn, sidebar, menuLink) {
    const d = document;
    d.addEventListener("click", e => {
        if (e.target.matches(sidebarBtn) || e.target.matches(`${sidebarBtn} *`)) {
            d.querySelector(sidebar).classList.toggle("active");
            d.querySelector(sidebarBtn).classList.toggle("active");
            d.getElementById("sidebar-icon").style.display="visible";
        }
        if (e.target.matches(menuLink)) {
            d.querySelector(sidebar).classList.remove("active");
            d.querySelector(sidebarBtn).classList.remove("active");
        }
    });
}

export{sidebarMenu}