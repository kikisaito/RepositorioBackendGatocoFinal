package com.example.patients.infrastructure.services

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import java.io.File
import java.nio.file.Files

/**
 * CloudinaryService
 * Servicio para manejar la subida y eliminación de imágenes en Cloudinary
 */
class CloudinaryService(
    cloudName: String,
    apiKey: String,
    apiSecret: String
) {
    private val cloudinary: Cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        )
    )
    
    /**
     * Sube una imagen a Cloudinary
     * @param bytes Los bytes de la imagen
     * @param publicId El ID público para la imagen (opcional, se generará automáticamente si no se proporciona)
     * @return La URL de la imagen subida
     */
    suspend fun uploadImage(bytes: ByteArray, publicId: String? = null): String {
        var tempFile: File? = null
        return try {
            // Validar que los bytes no estén vacíos
            if (bytes.isEmpty()) {
                throw IllegalArgumentException("Los bytes de la imagen están vacíos")
            }
            
            // Cloudinary Java SDK requiere un File para esta versión
            // Crear un archivo temporal
            tempFile = File.createTempFile("cloudinary_upload_", ".tmp")
            tempFile.deleteOnExit()
            
            // Escribir los bytes al archivo temporal
            Files.write(tempFile.toPath(), bytes)
            
            // Validar que el archivo se escribió correctamente
            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw RuntimeException("Error al escribir el archivo temporal")
            }
            
            // Subir la imagen
            val uploadResult = if (publicId != null) {
                cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", "gatoco/pets",
                        "resource_type", "image"
                    )
                )
            } else {
                cloudinary.uploader().upload(
                    tempFile,
                    ObjectUtils.asMap(
                        "folder", "gatoco/pets",
                        "resource_type", "image"
                    )
                )
            }
            
            uploadResult["secure_url"] as String
        } catch (e: Exception) {
            throw RuntimeException("Error al subir la imagen a Cloudinary: ${e.message}", e)
        } finally {
            // Eliminar el archivo temporal
            tempFile?.delete()
        }
    }
    
    /**
     * Elimina una imagen de Cloudinary
     * @param imageUrl La URL de la imagen a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    suspend fun deleteImage(imageUrl: String): Boolean {
        return try {
            // Extraer el public_id de la URL
            val publicId = extractPublicIdFromUrl(imageUrl)
            if (publicId == null) {
                return false
            }
            
            val deleteResult = cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "image")
            )
            
            deleteResult["result"] == "ok"
        } catch (e: Exception) {
            println("Error al eliminar la imagen de Cloudinary: ${e.message}")
            false
        }
    }
    
    /**
     * Extrae el public_id de una URL de Cloudinary
     * Ejemplo: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/gatoco/pets/abc123.jpg
     * Retorna: gatoco/pets/abc123
     */
    private fun extractPublicIdFromUrl(url: String): String? {
        return try {
            val regex = Regex("""/(?:v\d+/)?([^/]+/[^/]+/[^.]+)""")
            val match = regex.find(url)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}

