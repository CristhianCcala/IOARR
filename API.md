# API IOARR - Documentación de Endpoints

Aplicación de registro de asistencia (IOARR). API REST construida con Spring Boot 4.1.0 y Java 17.

- **Base URL:** `http://localhost:8080`
- **Autenticación:** JWT Bearer Token (excepto `POST /api/auth/login` y `OPTIONS`)
- **Formato:** JSON

---

## Autenticación

Todos los endpoints (excepto el login) requieren el header:

```
Authorization: Bearer <token>
```

### Usuarios por defecto

| Rol | Usuario |
| --- | --- |
| ADMINISTRACION | `admin` |
| JEFE | `jefe` |
| DIGITALIZADOR | `digitalizador` |

### Roles disponibles

- `ADMINISTRACION`
- `JEFE`
- `DIGITALIZADOR`

### Códigos de error comunes

| Código | Significado |
| --- | --- |
| `400` | Datos inválidos o validación fallida (devuelve `{"status":"error","mensaje":"..."}`) |
| `401` | Token inválido/ausente o credenciales incorrectas |
| `403` | Usuario autenticado sin permiso para el recurso |
| `404` | Recurso no encontrado |
| `409` | Conflicto de estado (ej. asistencia ya registrada) |
| `500` | Error interno del servidor (`{"status":"error","mensaje":"Error interno del servidor"}`) |

Formato de error no autenticado (401):

```json
{"status":401,"error":"No autenticado","mensaje":"Token inválido o ausente"}
```

---

## Índice de endpoints

### Auth (`/api/auth`)

| Método | Ruta | Descripción | Acceso |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | Iniciar sesión y obtener token JWT | Público |
| GET | `/api/auth/perfil` | Obtener datos del usuario autenticado | Autenticado |

### Usuarios (`/api/usuarios`)

| Método | Ruta | Descripción | Acceso |
| --- | --- | --- | --- |
| GET | `/api/usuarios` | Listar todos los usuarios | ADMINISTRACION, JEFE |
| GET | `/api/usuarios/{id}` | Obtener un usuario por ID | ADMINISTRACION, JEFE |
| POST | `/api/usuarios` | Crear un usuario | ADMINISTRACION |
| PUT | `/api/usuarios/{id}` | Actualizar un usuario | ADMINISTRACION |
| PATCH | `/api/usuarios/{id}/estado` | Activar/desactivar un usuario | ADMINISTRACION |
| DELETE | `/api/usuarios/{id}` | Eliminar un usuario | ADMINISTRACION |

### Asistencias (`/api/asistencias`)

| Método | Ruta | Descripción | Acceso |
| --- | --- | --- | --- |
| POST | `/api/asistencias/entrada` | Registrar hora de entrada (hoy) | Autenticado |
| POST | `/api/asistencias/salida` | Registrar hora de salida (hoy) | Autenticado |
| POST | `/api/asistencias/almuerzo/salida` | Registrar salida a almuerzo (hoy) | Autenticado |
| POST | `/api/asistencias/almuerzo/retorno` | Registrar retorno de almuerzo (hoy) | Autenticado |
| GET | `/api/asistencias/mis` | Listar asistencias propias (hoy primero, filtro por estado) | Autenticado |
| GET | `/api/asistencias/reporte` | Reporte resumido por usuario | ADMINISTRACION, JEFE |
| GET | `/api/asistencias` | Listar asistencias (hoy primero, filtro por estado) | ADMINISTRACION, JEFE |
| GET | `/api/asistencias/{id}` | Obtener una asistencia por ID | ADMINISTRACION, JEFE |
| POST | `/api/asistencias` | Crear registro manual | ADMINISTRACION |
| PUT | `/api/asistencias/{id}` | Actualizar asistencia | ADMINISTRACION |
| DELETE | `/api/asistencias/{id}` | Eliminar asistencia | ADMINISTRACION |

### Estados de asistencia (`EstadoAsistencia`)

`PRESENTE`, `TARDE`, `AUSENTE`, `JUSTIFICADO`, `PERMISO`, `VACACIONES`

---

## Auth

### POST `/api/auth/login`

Inicia sesión y devuelve el token JWT.

**Request body**

```json
{
  "username": "admin",
  "password": "123456"
}
```

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `username` | string | Sí | Nombre de usuario |
| `password` | string | Sí | Contraseña |

**Response 200 OK**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "usuario": {
    "id": 1,
    "username": "admin",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "dni": "12345678",
    "email": "admin@ioarr.com",
    "rol": "ADMINISTRACION",
    "activo": true
  },
  "rol": "ADMINISTRACION"
}
```

**Response 401** — Credenciales inválidas

```json
{"mensaje": "Credenciales inválidas"}
```

---

### GET `/api/auth/perfil`

Devuelve los datos del usuario autenticado.

**Response 200 OK**

```json
{
  "id": 1,
  "username": "admin",
  "nombre": "Administrador",
  "apellido": "Sistema",
  "dni": "12345678",
  "email": "admin@ioarr.com",
  "rol": "ADMINISTRACION",
  "activo": true
}
```

---

## Usuarios

### GET `/api/usuarios`

Lista todos los usuarios.

**Acceso:** `ADMINISTRACION`, `JEFE`

**Response 200 OK**

```json
[
  {
    "id": 1,
    "username": "admin",
    "nombre": "Administrador",
    "apellido": "Sistema",
    "dni": "12345678",
    "email": "admin@ioarr.com",
    "rol": "ADMINISTRACION",
    "activo": true
  }
]
```

---

### GET `/api/usuarios/{id}`

Obtiene un usuario por su ID.

**Acceso:** `ADMINISTRACION`, `JEFE`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID del usuario |

**Response 200 OK**

```json
{
  "id": 2,
  "username": "jefe",
  "nombre": "Jefe",
  "apellido": "Area",
  "dni": "87654321",
  "email": "jefe@ioarr.com",
  "rol": "JEFE",
  "activo": true
}
```

**Response 404** — No encontrado

---

### POST `/api/usuarios`

Crea un nuevo usuario.

**Acceso:** `ADMINISTRACION`

**Request body**

```json
{
  "username": "nuevo",
  "password": "123456",
  "nombre": "Juan",
  "apellido": "Perez",
  "dni": "11122233",
  "email": "juan@ioarr.com",
  "rol": "DIGITALIZADOR"
}
```

| Campo | Tipo | Obligatorio | Validación |
| --- | --- | --- | --- |
| `username` | string | Sí | 3-50 caracteres, único |
| `password` | string | Sí | Mínimo 6 caracteres |
| `nombre` | string | Sí | Máximo 80 caracteres |
| `apellido` | string | Sí | Máximo 80 caracteres |
| `dni` | string | No | Máximo 20 caracteres, único |
| `email` | string | No | Máximo 100 caracteres, único |
| `rol` | Rol | Sí | Uno de los roles válidos |

**Response 201 Created**

```json
{
  "id": 5,
  "username": "nuevo",
  "nombre": "Juan",
  "apellido": "Perez",
  "dni": "11122233",
  "email": "juan@ioarr.com",
  "rol": "DIGITALIZADOR",
  "activo": true
}
```

**Response 400** — Duplicados o validación fallida

```json
{"mensaje": "El nombre de usuario ya existe"}
{"mensaje": "El DNI ya está registrado"}
{"mensaje": "El email ya está registrado"}
```

---

### PUT `/api/usuarios/{id}`

Actualiza los campos enviados (todos opcionales). Se actualizan solo los campos no nulos.

**Acceso:** `ADMINISTRACION`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID del usuario |

**Request body** (ejemplo)

```json
{
  "nombre": "Juan Carlos",
  "rol": "JEFE"
}
```

| Campo | Tipo | Obligatorio | Validación |
| --- | --- | --- | --- |
| `username` | string | No | 3-50 caracteres, único |
| `password` | string | No | Mínimo 6 caracteres |
| `nombre` | string | No | Máximo 80 caracteres |
| `apellido` | string | No | Máximo 80 caracteres |
| `dni` | string | No | Máximo 20 caracteres, único |
| `email` | string | No | Máximo 100 caracteres, único |
| `rol` | Rol | No | Uno de los roles válidos |

**Response 200 OK**

```json
{
  "id": 5,
  "username": "nuevo",
  "nombre": "Juan Carlos",
  "apellido": "Perez",
  "dni": "11122233",
  "email": "juan@ioarr.com",
  "rol": "JEFE",
  "activo": true
}
```

**Response 400** — Duplicados

```json
{"mensaje": "El nombre de usuario ya existe"}
```

**Response 404** — No encontrado

---

### PATCH `/api/usuarios/{id}/estado`

Activa o desactiva un usuario.

**Acceso:** `ADMINISTRACION`

**Request body**

```json
{
  "activo": false
}
```

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `activo` | boolean | Sí | `true` activa, `false` desactiva |

**Response 200 OK**

```json
{
  "id": 5,
  "username": "nuevo",
  "nombre": "Juan Carlos",
  "apellido": "Perez",
  "dni": "11122233",
  "email": "juan@ioarr.com",
  "rol": "JEFE",
  "activo": false
}
```

**Response 400** — Intentar desactivar la propia cuenta

```json
{"mensaje": "No puede desactivar su propia cuenta"}
```

**Response 404** — No encontrado

---

### DELETE `/api/usuarios/{id}`

Elimina un usuario.

**Acceso:** `ADMINISTRACION`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID del usuario |

**Response 204 No Content**

**Response 400** — Intentar eliminar la propia cuenta

```json
{"mensaje": "No puede eliminar su propia cuenta"}
```

**Response 404** — No encontrado

---

## Asistencias

### POST `/api/asistencias/entrada`

Registra la entrada del usuario autenticado con la fecha de hoy. Si la hora de entrada es posterior a las **08:00**, el estado se marca como `TARDE`; si no, `PRESENTE`.

**Acceso:** Autenticado

**Request** — Sin body

**Response 201 Created**

```json
{
  "id": 10,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-17",
  "horaEntrada": "08:30:00",
  "horaSalida": null,
  "horaSalidaAlmuerzo": null,
  "horaEntradaAlmuerzo": null,
  "estado": "TARDE",
  "observacion": null
}
```

**Response 409** — Ya registró asistencia hoy

```json
{"status": "error", "mensaje": "El usuario ya registró asistencia hoy"}
```

---

### POST `/api/asistencias/salida`

Registra la salida del usuario autenticado con la fecha de hoy. Requiere una entrada registrada previamente.

**Acceso:** Autenticado

**Request** — Sin body

**Response 200 OK**

```json
{
  "id": 10,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-17",
  "horaEntrada": "08:30:00",
  "horaSalida": "17:15:00",
    "horaSalidaAlmuerzo": null,
    "horaEntradaAlmuerzo": null,
  "estado": "TARDE",
  "observacion": null
}
```

**Response 409**

```json
{"status": "error", "mensaje": "No hay entrada registrada hoy"}
{"status": "error", "mensaje": "El usuario ya registró salida hoy"}
```

---

### POST `/api/asistencias/almuerzo/salida`

Registra la salida a almuerzo del usuario autenticado con la fecha de hoy. Requiere una entrada registrada previamente.

**Acceso:** Autenticado

**Request** — Sin body

**Response 200 OK**

```json
{
  "id": 10,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-17",
  "horaEntrada": "08:30:00",
  "horaSalida": "17:15:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": null,
  "estado": "TARDE",
  "observacion": null
}
```

**Response 409**

```json
{"status": "error", "mensaje": "No hay entrada registrada hoy"}
{"status": "error", "mensaje": "El usuario ya registró salida a almuerzo hoy"}
```

---

### POST `/api/asistencias/almuerzo/retorno`

Registra el retorno de almuerzo del usuario autenticado con la fecha de hoy. Requiere una salida a almuerzo registrada previamente.

**Acceso:** Autenticado

**Request** — Sin body

**Response 200 OK**

```json
{
  "id": 10,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-17",
  "horaEntrada": "08:30:00",
  "horaSalida": "17:15:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": "14:00:00",
  "estado": "TARDE",
  "observacion": null
}
```

**Response 409**

```json
{"status": "error", "mensaje": "No hay entrada registrada hoy"}
{"status": "error", "mensaje": "No hay salida a almuerzo registrada hoy"}
{"status": "error", "mensaje": "El usuario ya registró retorno de almuerzo hoy"}
```

---

### GET `/api/asistencias/mis`

Lista las asistencias del usuario autenticado ordenadas por fecha descendente, mostrando primero las de la fecha actual. Solo admite filtro por estado. La edición de las horas de entrada y salida solo puede realizarla `ADMINISTRACION` (ver `PUT /api/asistencias/{id}`).

**Acceso:** Autenticado

**Parámetros de consulta (opcionales)**

| Param | Tipo | Formato | Descripción |
| --- | --- | --- | --- |
| `estado` | EstadoAsistencia | — | Filtrar por estado |

**Ejemplo:**

```
GET /api/asistencias/mis?estado=TARDE
```

**Response 200 OK**

```json
[
  {
    "id": 10,
    "usuarioId": 3,
    "usuario": "Juan Perez",
    "fecha": "2026-08-17",
    "horaEntrada": "08:30:00",
    "horaSalida": "17:15:00",
    "horaSalidaAlmuerzo": null,
    "horaEntradaAlmuerzo": null,
    "estado": "TARDE",
    "observacion": null
  }
]
```

---

### GET `/api/asistencias/reporte`

Genera un reporte agrupado por usuario con la cantidad de registros por estado y el tiempo acumulado de tardanza en el rango de fechas.

**Acceso:** `ADMINISTRACION`, `JEFE`

**Parámetros de consulta (opcionales)**

| Param | Tipo | Formato | Descripción |
| --- | --- | --- | --- |
| `usuarioId` | long | — | Filtrar por usuario. Si se envía, el reporte devuelve únicamente el resumen de ese usuario (incluida una fila en ceros si no tiene registros en el rango). Si el usuario no existe, devuelve `400`. |
| `desde` | date | `yyyy-MM-dd` | Fecha inicial |
| `hasta` | date | `yyyy-MM-dd` | Fecha final |

**Ejemplo:**

```
GET /api/asistencias/reporte?usuarioId=3&desde=2026-08-01&hasta=2026-08-31
```

**Response 200 OK**

```json
[
  {
    "usuarioId": 3,
    "usuario": "Juan Perez",
    "totalRegistros": 12,
    "presentes": 9,
    "tardes": 2,
    "ausentes": 1,
    "diasFaltados": 1,
    "tiempoAcumuladoTarde": "0:45"
  }
]
```

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `diasFaltados` | long | Total de días faltados (registros con estado `AUSENTE`). |
| `tiempoAcumuladoTarde` | string | Tiempo acumulado por llegar tarde en formato `HH:MM`. Se calcula con `horaEntrada - 08:00` en los registros con estado `TARDE`. |

**Response 400** — Si `usuarioId` no existe

```json
{"status": "error", "mensaje": "Usuario no encontrado: 99"}
```

---

### GET `/api/asistencias`

Lista todas las asistencias ordenadas por fecha descendente, mostrando primero las de la fecha actual. Solo admite filtro por estado.

**Acceso:** `ADMINISTRACION`, `JEFE`

**Parámetros de consulta (opcionales)**

| Param | Tipo | Formato | Descripción |
| --- | --- | --- | --- |
| `estado` | EstadoAsistencia | — | Filtrar por estado |

**Ejemplo:**

```
GET /api/asistencias?estado=TARDE
```

**Response 200 OK**

```json
[
  {
    "id": 10,
    "usuarioId": 3,
    "usuario": "Juan Perez",
    "fecha": "2026-08-17",
    "horaEntrada": "08:30:00",
    "horaSalida": "17:15:00",
    "horaSalidaAlmuerzo": null,
    "horaEntradaAlmuerzo": null,
    "estado": "TARDE",
    "observacion": null
  }
]
```

---

### GET `/api/asistencias/{id}`

Obtiene una asistencia por su ID.

**Acceso:** `ADMINISTRACION`, `JEFE`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID de la asistencia |

**Response 200 OK**

```json
{
  "id": 10,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-17",
  "horaEntrada": "08:30:00",
  "horaSalida": "17:15:00",
    "horaSalidaAlmuerzo": null,
    "horaEntradaAlmuerzo": null,
  "estado": "TARDE",
  "observacion": "Llegó con retraso"
}
```

**Response 400** — No encontrada

```json
{"status": "error", "mensaje": "Asistencia no encontrada: 999"}
```

---

### POST `/api/asistencias`

Crea un registro de asistencia manual para un usuario y fecha.

**Acceso:** `ADMINISTRACION`

**Request body**

```json
{
  "usuarioId": 3,
  "fecha": "2026-08-10",
  "horaEntrada": "08:00:00",
  "horaSalida": "17:00:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": "14:00:00",
  "estado": "JUSTIFICADO",
  "observacion": "Trámite médico"
}
```

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `usuarioId` | long | Sí | ID del usuario |
| `fecha` | date | Sí | Fecha del registro |
| `horaEntrada` | time | No | Hora de entrada |
| `horaSalida` | time | No | Hora de salida |
| `horaSalidaAlmuerzo` | time | No | Hora de salida a almuerzo |
| `horaEntradaAlmuerzo` | time | No | Hora de retorno de almuerzo |
| `estado` | EstadoAsistencia | Sí | Estado del registro |
| `observacion` | string | No | Observación |

**Response 201 Created**

```json
{
  "id": 15,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-10",
  "horaEntrada": "08:00:00",
  "horaSalida": "17:00:00",
  "horaSalidaAlmuerzo": null,
  "horaEntradaAlmuerzo": null,
  "estado": "JUSTIFICADO",
  "observacion": "Trámite médico"
}
```

**Response 400** — Usuario no encontrado

```json
{"status": "error", "mensaje": "Usuario no encontrado: 99"}
```

**Response 409** — Registro duplicado para el usuario/fecha

```json
{"status": "error", "mensaje": "El usuario ya tiene un registro para esa fecha"}
```

---

### PUT `/api/asistencias/{id}`

Actualiza los campos enviados (todos opcionales). Se actualizan solo los campos no nulos.

**Acceso:** `ADMINISTRACION`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID de la asistencia |

**Request body** (ejemplo)

```json
{
  "estado": "PERMISO",
  "observacion": "Permiso aprobado"
}
```

| Campo | Tipo | Obligatorio | Descripción |
| --- | --- | --- | --- |
| `fecha` | date | No | Nueva fecha |
| `horaEntrada` | time | No | Hora de entrada |
| `horaSalida` | time | No | Hora de salida |
| `horaSalidaAlmuerzo` | time | No | Hora de salida a almuerzo |
| `horaEntradaAlmuerzo` | time | No | Hora de retorno de almuerzo |
| `estado` | EstadoAsistencia | No | Estado |
| `observacion` | string | No | Observación |

**Response 200 OK**

```json
{
  "id": 15,
  "usuarioId": 3,
  "usuario": "Juan Perez",
  "fecha": "2026-08-10",
  "horaEntrada": "08:00:00",
  "horaSalida": "17:00:00",
  "horaSalidaAlmuerzo": null,
  "horaEntradaAlmuerzo": null,
  "estado": "PERMISO",
  "observacion": "Permiso aprobado"
}
```

**Response 409** — Si la nueva fecha ya existe para el mismo usuario

```json
{"status": "error", "mensaje": "El usuario ya tiene un registro para esa fecha"}
```

---

### DELETE `/api/asistencias/{id}`

Elimina un registro de asistencia.

**Acceso:** `ADMINISTRACION`

**Parámetros de ruta**

| Param | Tipo | Descripción |
| --- | --- | --- |
| `id` | long | ID de la asistencia |

**Response 204 No Content**

**Response 400** — No encontrada

```json
{"status": "error", "mensaje": "Asistencia no encontrada: 999"}
```

---

## Horarios de trabajo

### GET `/api/horarios`

Obtiene el horario de trabajo configurado. Si aún no existe, se crea automáticamente con los valores por defecto (entrada `08:00`, salida a almuerzo `13:00`, entrada de almuerzo `14:30`, salida `17:30`, tolerancia `10`, latitud `-14.118589819492668`, longitud `-72.24576119540694`).

**Acceso:** cualquier usuario autenticado

**Response 200 OK**

```json
{
  "id": 1,
  "horaEntrada": "08:00:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": "14:30:00",
  "horaSalida": "17:30:00",
  "toleranciaMinutos": 10,
  "latitud": -14.118589819492668,
  "longitud": -72.24576119540694
}
```

### PUT `/api/horarios`

Actualiza el horario de trabajo.

**Acceso:** solo `ADMINISTRACION`

**Request body**

```json
{
  "horaEntrada": "08:00:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": "14:30:00",
  "horaSalida": "17:30:00",
  "toleranciaMinutos": 10,
  "latitud": -14.118589819492668,
  "longitud": -72.24576119540694
}
```

**Response 200 OK**

```json
{
  "id": 1,
  "horaEntrada": "08:00:00",
  "horaSalidaAlmuerzo": "13:00:00",
  "horaEntradaAlmuerzo": "14:30:00",
  "horaSalida": "17:30:00",
  "toleranciaMinutos": 10,
  "latitud": -14.118589819492668,
  "longitud": -72.24576119540694
}
```

**Response 400** — Datos inválidos (campos `@NotNull`, tolerancia ≥ 0, latitud entre -90 y 90, longitud entre -180 y 180)

---

## Nota sobre el registro de entrada/salida

- La hora límite de entrada es `08:00` (`AsistenciaService.HORA_LIMITE_ENTRADA`). Una entrada después de esa hora marca el estado como `TARDE`.
- Solo se permite una entrada, una salida, una salida a almuerzo y un retorno de almuerzo por usuario por día.
- El retorno de almuerzo (`almuerzo/retorno`) solo puede registrarse si previamente se registró la salida a almuerzo (`almuerzo/salida`).