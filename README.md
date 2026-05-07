# Appsembly

Appsembly es una aplicación Spring Boot con frontend React/Vite y persistencia en PostgreSQL. El proyecto está preparado para ejecutarse con Docker Compose en tres servicios: base de datos, backend y proxy frontend.

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
- Validación de código de encuesta: [http://localhost:8080/inicio](http://localhost:8080/inicio)
- Pantalla de encuesta activa con registro de voto: [http://localhost:8080/pregunta](http://localhost:8080/pregunta)
- Pantalla de historial: [http://localhost:8080/historial](http://localhost:8080/historial)
- Pantalla de resultados: [http://localhost:8080/resultados](http://localhost:8080/resultados)
- Alta de usuario mínima para pruebas: [http://localhost:8080/user/save](http://localhost:8080/user/save)
- Padrón y administración de residentes: [http://localhost:8080/admin/padron](http://localhost:8080/admin/padron)
- Corrección de vivienda por residente: [http://localhost:8080/admin/padron/{userId}/housing](http://localhost:8080/admin/padron/{userId}/housing)
- Bloqueo / desbloqueo de residentes: [http://localhost:8080/admin/padron/{userId}/block](http://localhost:8080/admin/padron/{userId}/block)

### Rol Administrador

- Login de admin: [http://localhost:8080/index](http://localhost:8080/index)
- Redirección posterior al login: [http://localhost:8080/inicio](http://localhost:8080/inicio)
- Dashboard de administración con métricas reales: [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- Panel administrativo visible por ruta de vista: [http://localhost:8080/adminpanel](http://localhost:8080/adminpanel)
- Creación de encuestas: [http://localhost:8080/admin/prueba](http://localhost:8080/admin/prueba)
- Cierre, activación, borrador y archivado de encuestas: [http://localhost:8080/admin/surveys/{surveyId}/status](http://localhost:8080/admin/surveys/{surveyId}/status)
- Exportación de resultados por votación: [http://localhost:8080/admin/surveys/{surveyId}/export](http://localhost:8080/admin/surveys/{surveyId}/export)
- Detalle de votos por encuesta: [http://localhost:8080/admin/surveys/{surveyId}/votes](http://localhost:8080/admin/surveys/{surveyId}/votes)
- Participación por bloque, torre o unidad: [http://localhost:8080/admin/participation](http://localhost:8080/admin/participation)
- Alta de usuario: [http://localhost:8080/user/save](http://localhost:8080/user/save)
- Resultados de encuestas: [http://localhost:8080/resultados](http://localhost:8080/resultados)
- Historial: [http://localhost:8080/historial](http://localhost:8080/historial)

## Flujo Implementado

- El login devuelve JSON con redirección según el rol.
- El código de acceso valida que exista una encuesta activa antes de entrar al cuestionario.
- Las encuestas ahora tienen ciclo de vida: `DRAFT`, `OPEN`, `CLOSED` y `ARCHIVED`.
- Cada encuesta registra trazabilidad de acciones con quién la creó, activó, cerró o archivó.
- Las encuestas pueden restringirse por bloque, torre o ambos, usando la vivienda registrada del residente.
- Las encuestas pueden ser públicas o anónimas; en modo público se conserva el detalle de quién votó qué opción.
- Las encuestas creadas desde admin se guardan en base de datos con respuestas, fecha de expiración, alcance de audiencia y privacidad.
- El voto se registra en el backend en el endpoint `POST /vote/submit` y evita duplicados por código en la encuesta activa.
- El padrón de residentes guarda relación con bloque, torre y unidad, y permite bloquear usuarios o corregir su vivienda.
- Las métricas, historial, encuesta actual y resultados se calculan desde la base de datos.
- Los errores de validación y conflicto devuelven respuestas JSON controladas.

## Rutas Principales

- [http://localhost:8080/index](http://localhost:8080/index)
- [http://localhost:8080/inicio](http://localhost:8080/inicio)
- [http://localhost:8080/admin/dashboard](http://localhost:8080/admin/dashboard)
- [http://localhost:8080/adminpanel](http://localhost:8080/adminpanel)
- [http://localhost:8080/admin/padron](http://localhost:8080/admin/padron)
- [http://localhost:8080/admin/participation](http://localhost:8080/admin/participation)
- [http://localhost:8080/pregunta](http://localhost:8080/pregunta)
- [http://localhost:8080/resultados](http://localhost:8080/resultados)
- [http://localhost:8080/historial](http://localhost:8080/historial)
- [http://localhost:8080/user/save](http://localhost:8080/user/save)
- [http://localhost:8080/vote/submit](http://localhost:8080/vote/submit)
- [http://localhost:8080/admin/surveys/{surveyId}/export](http://localhost:8080/admin/surveys/{surveyId}/export)
- [http://localhost:8081/index](http://localhost:8081/index)
- [http://localhost:8081/admin/dashboard](http://localhost:8081/admin/dashboard)

## Patrón de arquitectura

- MVC para controladores y vistas.
- Service/Repository para la lógica de negocio y acceso a datos.
- Spring Security para autenticación y autorización.

## Validación Local

- Backend: `./mvnw.cmd test`
- Frontend: `npm run build` dentro de `frontend/`
