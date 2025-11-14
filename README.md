# 🪄 HogwartsEncriptado

## 📖 Descripción del repositorio
HogwartsEncriptado es una aplicación desarrollada en **JavaFX**, que permite **cifrar y descifrar mensajes o archivos** utilizando dos algoritmos:

- **AES (Java)** — cifrado moderno y seguro
- **Vigenère (Python)** — cifrado clásico ejecutado mediante script externo

El proyecto integra **Java 23**, **JavaFX 13** y **Python 3.13**, combinando ambos lenguajes para ofrecer una experiencia  educativa y funcional.

---

## 📂 Estructura del Proyecto

### 📌 1. Aplicación (JavaFX)
- `App.java` – Clase principal
- `Lanzador.java` – Punto de entrada
- `MainController.java` – Controlador principal
- `module-info.java` – Configuración del módulo

### 📌 2. Lógica de Cifrado (Java – AES)
- `AESCipher.java` – Implementa cifrado y descifrado AES.
- `AESImageCipher` – Implementa cifrado y descifrado AES.

### 📌 3. Integración con Python
- `PythonVigenereService.java` – Ejecuta el script Python y recibe resultados.

### 📌 4. Lógica de Cifrado (Python – Vigenère)
- `Vigenere.py` – Implementa cifrado/descifrado y logging.
- `vigenere.log` – Registro de eventos.

### 📌 5. Datos de Ejemplo
- `mensaje.txt`
- `mensaje_cifrado.txt`
- `mensaje_descifrado.txt`

### 📌 6. Recursos (Interfaz)
- CSS → `estilo/estilo.css`
- FXML → `fxml/MainView.fxml`
- Imágenes → `imagenes/`
- Traduccion → `i18n/`

---

## ⚙️ Requisitos

### Software
- Java 23
- JavaFX 13
- Python 3.13
- Navegador

---

## 🚀 Instalación y Ejecución

### 1️⃣ Clonar el repositorio
```
https://github.com/Xiker13/HogwartsEncriptado.git
```

### 2️⃣ Verificar Python
```
python --version
```

### 3️⃣ Ejecutar la aplicación
Ejecutar en el IDE:
```
Lanzador.java
```

### 4️⃣ Ejemplos de uso

#### 🔐 Cifrado AES textos (Java)
1. Introducir texto
2. Introducir clave
3. Seleccionar AES
4. Cifrar

#### 🔐 Cifrado AES imagenes (Java)
1. Seleccionar AES 
2. Introducir clave 
3. Pinchar en cifrar 
4. Seleccionar imagen 
5. Seleccionar lugar donde guardarlo


#### 🔡 Cifrado Vigenère (Python)
1. Seleccionar Vigenère
2. Introducir clave
3. Ejecutar

#### 🔓 Descifrado
Usar la misma clave para restaurar el mensaje original.

---

## ✨ Características
- Interfaz Hogwarts
- AES y Vigenère integrados
- Comunicación Java ↔ Python
- Logs Python
- Estilo CSS

---

## 🧙 Autores
- Xiker — UI y AES
- Salca — Python y servicios
