# GUÍA DE DESPLIEGUE AUTOMATIZADO (AWS + GitHub Actions)

¡Hola! Aquí tienes los pasos exactos para configurar tu despliegue. Sigue esta guía al pie de la letra.

## 1. Preparar tu Servidor AWS (EC2)

Necesitas conectarte a tu servidor una vez para prepararlo.

1.  **Conéctate por SSH** a tu instancia (usando Putty o Terminal).
2.  **Sube el script de configuración**:
    Copia el contenido del archivo `deploy/setup-server.sh` de este repositorio.
    En tu servidor, crea el archivo y pega el contenido:
    ```bash
    nano setup-server.sh
    # Pega el contenido, guarda (Ctrl+O) y sal (Ctrl+X)
    chmod +x setup-server.sh
    ./setup-server.sh
    ```
    *Este script creará el usuario `deploy`, instalará Java y configurará el puerto 80.*

## 2. Configurar Secretos en GitHub

Para que GitHub Actions pueda entrar a tu servidor, necesita las llaves.

1.  Ve a tu repositorio en GitHub -> **Settings** -> **Secrets and variables** -> **Actions**.
2.  Haz clic en **New repository secret** y agrega estos 3 secretos:

    *   **`AWS_HOST`**: La IP pública o DNS de tu instancia EC2 (ej. `54.123.45.67`).
    *   **`AWS_USERNAME`**: El usuario por defecto de tu instancia (usualmente `ubuntu` en AWS).
    *   **`SSH_PRIVATE_KEY`**: Tu llave privada `.pem` completa.
        *   *Nota*: Copia todo el contenido, desde `-----BEGIN RSA PRIVATE KEY-----` hasta `-----END RSA PRIVATE KEY-----`.

## 3. Desplegar

¡Eso es todo! Ahora, cada vez que hagas un **Push** a la rama `main`, GitHub Actions:
1.  Compilará tu código.
2.  Lo enviará a tu servidor.
3.  Reiniciará el servicio automáticamente.

---

### Sobre tus llaves de AWS
Me compartiste unas llaves (`aws_access_key_id`, etc.). Esas llaves son para usar la API de AWS (como crear servidores por código). **No las necesitamos para este método de despliegue**, ya que usamos SSH directo, que es más simple y seguro para lo que necesitas.
**IMPORTANTE**: Como publicaste esas llaves en el chat, te recomiendo desactivarlas o rotarlas en tu consola de AWS por seguridad, ya que son temporales pero sensibles.
