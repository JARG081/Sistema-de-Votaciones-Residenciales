# Appsembly

Appsembly es una aplicación Spring Boot con vistas Thymeleaf y persistencia en PostgreSQL. El proyecto está preparado para ejecutarse con Docker Compose en tres servicios: base de datos, backend y proxy frontend.

## Dependencias

Consulta [DEPENDENCIAS.md](DEPENDENCIAS.md) para ver el stack y los módulos usados.

## Ejecución con Docker

1. Levanta todo el stack:

```bash
docker-compose up -d --build
```

2. Abre la aplicación en:

```text
http://localhost:8080
```

3. Si quieres probar el backend directo:

```text
http://localhost:8081
```

## Puertos

- `8080`: frontend Nginx, punto de entrada recomendado para revisar la app completa.
- `8081`: backend Spring Boot directo, útil para depuración y pruebas de endpoints.
- `5432`: PostgreSQL, solo si necesitas conectar una herramienta de BD.

## Funcionalidades Por Rol

### Rol Usuario

- Login de usuario: [http://localhost:8080/index](http://localhost:8080/index)
- Acceso a la pantalla principal tras autenticarse: [http://localhost:8080/inicio](http://localhost:8080/inicio)
- Pantalla de historial: [http://localhost:8080/historial](http://localhost:8080/historial)
- Pantalla de resultados: [http://localhost:8080/resultados](http://localhost:8080/resultados)
- Pantalla de pregunta/encuesta: [http://localhost:8080/pregunta](http://localhost:8080/pregunta)
- Alta de usuario mínima para pruebas: [http://localhost:8080/user/save](http://localhost:8080/user/save)

### Rol Administrador

- Login de admin: [http://localhost:8080/index](http://localhost:8080/index)
- Redirección posterior al login: [http://localhost:8080/inicio](http://localhost:8080/inicio)
- Dashboard de administración: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- Panel administrativo visible por ruta de vista: [http://localhost:8080/adminpanel](http://localhost:8080/adminpanel)
- Gestión de encuestas: [http://localhost:8080/pregunta](http://localhost:8080/pregunta)
- Resultados de encuestas: [http://localhost:8080/resultados](http://localhost:8080/resultados)
- Historial: [http://localhost:8080/historial](http://localhost:8080/historial)

## Rutas Principales

- [http://localhost:8080/index](http://localhost:8080/index)
- [http://localhost:8080/inicio](http://localhost:8080/inicio)
- [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- [http://localhost:8080/adminpanel](http://localhost:8080/adminpanel)
- [http://localhost:8080/pregunta](http://localhost:8080/pregunta)
- [http://localhost:8080/resultados](http://localhost:8080/resultados)
- [http://localhost:8080/historial](http://localhost:8080/historial)
- [http://localhost:8080/user/save](http://localhost:8080/user/save)
- [http://localhost:8081/index](http://localhost:8081/index)
- [http://localhost:8081/admin/dashboard](http://localhost:8081/admin/dashboard)

## Patrón de arquitectura

- MVC para controladores y vistas.
- Service/Repository para la lógica de negocio y acceso a datos.
- Spring Security para autenticación y autorización.
