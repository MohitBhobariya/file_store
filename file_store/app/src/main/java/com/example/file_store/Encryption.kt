package com.example.file_store

import android.net.Uri
import android.util.Base64.NO_WRAP
import android.util.Base64.encodeToString
import android.util.Log
import androidx.core.net.toFile
import java.io.*
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryption{

    @Throws(Exception::class)
    fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        //generate a key with secure random
        keyGenerator?.init(128, secureRandom)
        return keyGenerator?.generateKey()
    }
//    fun saveSecretKey(secretKey: SecretKey): String {
//        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
//        return encodedKey
//    }

    @Throws(Exception::class)
    fun readFile(uri:Uri): ByteArray {
        val file = File(uri.path!!)
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )

        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents

    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, path: String) {
        val file = File(path)
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

    @Throws(Exception::class)
    fun encrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = yourKey.getEncoded()
        val skeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.getBlockSize())))
        return cipher.doFinal(fileData)
    }
}



