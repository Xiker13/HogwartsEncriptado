#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
SCRIPTUM - Cifrado y Descifrado con el algoritmo de Vigenère
============================================================

Alumno/a:
Unidad Didáctica: UD1 - Python

Descripción general
-------------------
Implementación del algoritmo clásico de cifrado y descifrado Vigenère.

Características principales:
- Permite cifrar y descifrar texto y archivos .txt
- No modifica el archivo original (crea una copia cifrada/descifrada)
- Valida la clave (no vacía, sin caracteres invisibles, longitud mínima)
- Muestra mensajes claros de error o advertencia
- No almacena claves en disco
- Puede ser invocado desde la app Java (modo consola)
@author salca
"""

# ========================= LIBRERÍAS =========================
import os
import sys
import string
import logging
import json
from typing import List, Tuple

# ========================== CONFIGURACIÓN LOGGING JSON ==========================



# ========================== CONFIGURACIÓN ==========================

# Ruta base donde está el script
BASE_DIR = os.path.dirname(__file__)

# Carpeta donde se guardan los archivos de prueba
DATA_DIR = os.path.join(BASE_DIR, "..", "data")

# Archivos de demostración
FILE_ORIGINAL = os.path.join(DATA_DIR, "mensaje.txt")
FILE_CIFRADO = os.path.join(DATA_DIR, "mensaje_cifrado.txt")
FILE_DESCIFRADO = os.path.join(DATA_DIR, "mensaje_descifrado.txt")

# Texto y clave de ejemplo
TEXTO_DEMO = "ATAQUE AL AMANECER"
CLAVE_DEMO = "CLAVE"

# Parámetros de validación de clave
MINIMO_CLAVE = 3
CARACTERES_INVISIBLES = [
    '\u200B', '\u200C', '\u200D', '\u2060', '\uFEFF'
]  # espacios invisibles / zero width


# ====================== FUNCIONES AUXILIARES =======================

def limpiar_texto(texto: str) -> str:
    """
    Convierte el texto a mayúsculas y elimina todos los caracteres
    que no sean letras A-Z (alfabeto inglés).

    Esto deja el texto en formato apropiado para aplicar el Vigenère clásico.

    Parámetros
    ----------
    texto : str
        Texto original.

    Retorna
    -------
    str
        Texto solo con letras mayúsculas A-Z.
    """
    texto = texto.upper()
    return "".join(c for c in texto if c in string.ascii_uppercase)


def detectar_invisibles(cadena: str) -> List[str]:
    """
    Busca caracteres invisibles (zero width, BOM, etc.) dentro de una cadena.

    Parámetros
    ----------
    cadena : str
        Cadena a revisar.

    Retorna
    -------
    List[str]
        Lista con los caracteres invisibles encontrados (en repr()).
        Si no hay, lista vacía.
    """
    encontrados: List[str] = []
    for c in cadena:
        if c in CARACTERES_INVISIBLES:
            encontrados.append(repr(c))
    return encontrados


def normalizar_clave(clave: str) -> str:
    """
    Normaliza la clave eliminando todos los caracteres que no sean letras A-Z.

    El cifrado de Vigenère clásico opera únicamente sobre letras del alfabeto
    inglés. Este método convierte la clave a mayúsculas y descarta cualquier
    carácter no alfabético (números, tildes, signos, espacios...).

    Parámetros
    ----------
    clave : str
        Clave introducida por el usuario.

    Retorna
    -------
    str
        Clave limpia compuesta solo por letras mayúsculas A-Z.
    """
    return "".join(c for c in clave.upper() if c in string.ascii_uppercase)


def validar_datos(texto: str, clave: str) -> Tuple[bool, str]:
    """
    Valida tanto el texto como la clave antes de aplicar el cifrado/descifrado.

    Comprueba:
      - Que la clave no esté vacía.
      - Que no contenga caracteres invisibles (zero-width, BOM...).
      - Que, tras limpiarla, tenga una longitud mínima definida por MINIMO_CLAVE.
      - Que la clave original no contenga caracteres NO válidos (números, guiones, etc.).
      - Que el texto no esté vacío.
      - Que el texto contenga al menos una letra válida A-Z.

    Retorna
    -------
    Tuple[bool, str]
        - bool: True si los datos son válidos, False si hay algún problema.
        - str: Mensaje de error o advertencia (vacío si todo está correcto).
    """

    # 1. Verificar que la clave no esté vacía o hecha solo de espacios
    if not clave or not clave.strip():
        return False, "La clave no puede estar vacía."

    # 2. Buscar caracteres invisibles en la clave
    invisibles = detectar_invisibles(clave)
    if invisibles:
        codigos = ", ".join(f"U+{ord(c):04X}" for c in invisibles)
        return False, (
            f"⚠️ La clave contiene caracteres invisibles o ilegales "
            f"({codigos}). Revísala y vuelve a introducirla."
        )

    # 3. Normalizar la clave (solo letras A-Z)
    clave_normalizada = normalizar_clave(clave)

    # Si no queda nada tras limpiar, no es válida
    if not clave_normalizada:
        return False, "La clave debe contener al menos una letra A-Z."

    # Comprobar longitud mínima
    if len(clave_normalizada) < MINIMO_CLAVE:
        return False, f"La clave debe tener al menos {MINIMO_CLAVE} letras."

    # 4. Si la clave original tenía caracteres no válidos (números, guiones, acentos, etc.),
    #    lo consideramos un error y se informa al usuario.
    if clave_normalizada != clave.upper():
        return False, (
            "⚠️ La clave contiene caracteres no válidos (solo se permiten letras A-Z).\n"
            f"Clave introducida: {clave}\n"
            f"Parte válida de la clave sería: {clave_normalizada}"
        )

    # 5. Validar el texto
    if not texto or not texto.strip():
        return False, "❌ El texto no puede estar vacío."

    texto_limpio = limpiar_texto(texto)
    if not texto_limpio:
        return False, "El texto debe contener al menos una letra A-Z."

    return True, ""



def leer_fichero(path: str) -> str:
    """
    Intenta leer un archivo de texto en UTF-8. Si falla, intenta Latin-1.

    Parámetros
    ----------
    path : str
        Ruta del archivo a leer.

    Retorna
    -------
    str
        Contenido del archivo o "" si hay error.
    """
    try:
        with open(path, "r", encoding="utf-8") as f:
            return f.read()
    except UnicodeDecodeError:
        logging.warning(f"{path} no está en UTF-8, probando Latin-1...")
        try:
            with open(path, "r", encoding="latin-1") as f:
                return f.read()
        except Exception as e:
            logging.error(f"No se pudo leer {path}: {e}")
            return ""
    except FileNotFoundError:
        logging.error(f"El fichero {path} no existe.")
        return ""
    except Exception as e:
        logging.error(f"No se pudo leer {path}: {e}")
        return ""


def escribir_fichero(path: str, contenido: str) -> None:
    """
    Escribe un texto en un archivo en UTF-8.

    Parámetros
    ----------
    path : str
        Ruta del archivo de salida.
    contenido : str
        Texto a escribir.

    Retorna
    -------
    None
    """
    try:
        with open(path, "w", encoding="utf-8") as f:
            f.write(contenido)
        logging.info(f"Fichero escrito en {path}")
    except Exception as e:
        logging.error(f"No se pudo escribir en {path}: {e}")


# =================== ALGORITMO VIGENÈRE PURO =======================

def ajustar_clave(texto: str, clave: str) -> str:
    """
    Ajusta la longitud de la clave repitiéndola hasta igualar el tamaño del texto.

    Ejemplo:
        texto = "HOLA", clave = "A" → clave ajustada = "AAAA"

    Parámetros
    ----------
    texto : str
        Texto limpio sobre el que se va a cifrar.
    clave : str
        Clave original (se normaliza dentro).

    Retorna
    -------
    str
        Clave repetida y cortada a la longitud exacta del texto.
    """
    clave = normalizar_clave(clave)
    if not clave:
        raise ValueError("La clave no puede estar vacía.")
    repeticiones = (len(texto) // len(clave)) + 1
    return (clave * repeticiones)[:len(texto)]


def cifrar_vigenere(texto: str, clave: str) -> str:
    """
    Aplica el cifrado clásico de Vigenère.

    A cada letra del texto limpio (A-Z) se le suma el desplazamiento
    de la letra correspondiente de la clave.

    Parámetros
    ----------
    texto : str
        Texto original a cifrar.
    clave : str
        Clave de cifrado.

    Retorna
    -------
    str
        Texto cifrado (solo letras A-Z).

    Lanza
    -----
    ValueError
        Si los datos no son válidos.
    """
    valido, mensaje = validar_datos(texto, clave)
    if not valido:
        raise ValueError(mensaje)

    texto_limpio = limpiar_texto(texto)
    clave_ajustada = ajustar_clave(texto_limpio, clave)

    resultado = []
    for t, k in zip(texto_limpio, clave_ajustada):
        nt = ord(t) - ord('A')   # posición de la letra del texto
        nk = ord(k) - ord('A')   # posición de la letra de la clave
        c = (nt + nk) % 26       # desplazamiento modular
        resultado.append(chr(c + ord('A')))
    return "".join(resultado)


def descifrar_vigenere(texto_cifrado: str, clave: str) -> str:
    """
    Realiza el descifrado del algoritmo de Vigenère.

    A cada letra cifrada se le resta el desplazamiento de la clave.

    Parámetros
    ----------
    texto_cifrado : str
        Texto cifrado (solo letras A-Z).
    clave : str
        Clave utilizada para cifrar.

    Retorna
    -------
    str
        Texto descifrado (solo letras A-Z).

    Lanza
    -----
    ValueError
        Si los datos no son válidos.
    """
    valido, mensaje = validar_datos(texto_cifrado, clave)
    if not valido:
        raise ValueError(mensaje)

    texto_limpio = limpiar_texto(texto_cifrado)
    clave_ajustada = ajustar_clave(texto_limpio, clave)

    resultado = []
    for c, k in zip(texto_limpio, clave_ajustada):
        nc = ord(c) - ord('A')   # posición de la letra cifrada
        nk = ord(k) - ord('A')   # posición de la letra de la clave
        p = (nc - nk) % 26       # desplazamiento inverso
        resultado.append(chr(p + ord('A')))

    texto_descifrado = "".join(resultado)

    # Aviso simple si el resultado parece raro
    if not any(ch in string.ascii_uppercase for ch in texto_descifrado):
        logging.warning("Posible clave incorrecta: el resultado no contiene letras legibles.")

    return texto_descifrado


# ================== FUNCIONES PARA ARCHIVOS ========================

def cifrar_archivo(ruta_entrada: str, ruta_salida: str, clave: str) -> None:
    """
    Cifra un archivo .txt usando Vigenère y guarda el resultado en otro archivo.

    No sobreescribe el original.

    Parámetros
    ----------
    ruta_entrada : str
        Ruta del archivo de texto original.
    ruta_salida : str
        Ruta del archivo donde se escribirá el texto cifrado.
    clave : str
        Clave de cifrado.

    Retorna
    -------
    None
    """
    if not os.path.exists(ruta_entrada):
        logging.error("El archivo de entrada no existe.")
        return

    contenido = leer_fichero(ruta_entrada)
    if not contenido:
        logging.error("No se pudo leer el archivo de entrada.")
        return

    try:
        cifrado = cifrar_vigenere(contenido, clave)
        escribir_fichero(ruta_salida, cifrado)
    except ValueError as e:
        logging.error(str(e))


def descifrar_archivo(ruta_entrada: str, ruta_salida: str, clave: str) -> None:
    """
    Descifra un archivo .txt cifrado con Vigenère y guarda el resultado
    en un archivo nuevo.

    Parámetros
    ----------
    ruta_entrada : str
        Ruta del archivo cifrado.
    ruta_salida : str
        Ruta del archivo donde se escribirá el texto descifrado.
    clave : str
        Clave de descifrado.

    Retorna
    -------
    None
    """
    if not os.path.exists(ruta_entrada):
        logging.error("El archivo cifrado no existe.")
        return

    contenido = leer_fichero(ruta_entrada)
    if not contenido:
        logging.error("No se pudo leer el archivo cifrado.")
        return

    try:
        descifrado = descifrar_vigenere(contenido, clave)
        escribir_fichero(ruta_salida, descifrado)
    except ValueError as e:
        logging.error(str(e))


# ========================== DEMO ============================

def demo() -> None:
    """
    Ejecuta una demostración completa del cifrado Vigenère:

    - Cifra y descifra un texto fijo.
    - Genera archivos de prueba en la carpeta /data.
    """
    # Crear carpeta de datos si no existe
    os.makedirs(DATA_DIR, exist_ok=True)

    logging.info("=== DEMO VIGENÈRE ===")
    logging.info(f"Texto demo : {TEXTO_DEMO}")
    logging.info(f"Clave demo : {CLAVE_DEMO}")

    try:
        cifrado = cifrar_vigenere(TEXTO_DEMO, CLAVE_DEMO)
        logging.info(f"Cifrado     : {cifrado}")

        descifrado = descifrar_vigenere(cifrado, CLAVE_DEMO)
        logging.info(f"Descifrado  : {descifrado}")
    except ValueError as e:
        logging.error(str(e))

    # Crear archivo base si no existe
    if not os.path.exists(FILE_ORIGINAL):
        escribir_fichero(
            FILE_ORIGINAL,
            "Este es un mensaje secreto para probar el cifrado Vigenere."
        )

    logging.info("\n=== CIFRANDO ARCHIVO ===")
    cifrar_archivo(FILE_ORIGINAL, FILE_CIFRADO, CLAVE_DEMO)

    logging.info("\n=== DESCIFRANDO ARCHIVO ===")
    descifrar_archivo(FILE_CIFRADO, FILE_DESCIFRADO, CLAVE_DEMO)


# ====================== MODO CONSOLA / JAVA ========================

if __name__ == "__main__":
    """
    Permite usar el script directamente o desde la app Java.

    Modos admitidos:
        python vigenere.py
            → ejecuta la demo por defecto

        python vigenere.py cifrar "TEXTO" "CLAVE"
            → imprime por stdout el texto cifrado

        python vigenere.py descifrar "TEXTO" "CLAVE"
            → imprime por stdout el texto descifrado

        python vigenere.py cifrar-archivo entrada.txt salida.txt CLAVE
            → crea el archivo cifrado

        python vigenere.py descifrar-archivo entrada.txt salida.txt CLAVE
            → crea el archivo descifrado
    """
    args = sys.argv
    if len(args) == 1:
        # Sin argumentos → modo demo
        demo()
    else:
        modo = args[1].lower()
        try:
            if modo == "cifrar" and len(args) >= 4:
                # Cifrar texto directamente
                print(cifrar_vigenere(args[2], args[3]))
            elif modo == "descifrar" and len(args) >= 4:
                # Descifrar texto directamente
                print(descifrar_vigenere(args[2], args[3]))
            elif modo == "cifrar-archivo" and len(args) >= 5:
                # Cifrar archivo .txt
                cifrar_archivo(args[2], args[3], args[4])
            elif modo == "descifrar-archivo" and len(args) >= 5:
                # Descifrar archivo .txt
                descifrar_archivo(args[2], args[3], args[4])
            else:
                # Uso incorrecto
                logging.error("Uso incorrecto. Uso:\n"
                              '  python vigenere.py cifrar "TEXTO" "CLAVE"\n'
                              '  python vigenere.py descifrar "TEXTO" "CLAVE"\n'
                              '  python vigenere.py cifrar-archivo entrada.txt salida.txt CLAVE\n'
                              '  python vigenere.py descifrar-archivo entrada.txt salida.txt CLAVE')
                sys.exit(1)
        except ValueError as e:
            logging.error(str(e))
            sys.exit(1)
