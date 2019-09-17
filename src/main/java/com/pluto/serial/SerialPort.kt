package com.pluto.serial

import android.util.Log
import java.io.*

class SerialPort @Throws(SecurityException::class, IOException::class)
constructor(device: File, baudrate: Int) {

    /*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
    private var mFd: FileDescriptor? = null
    private val mFileInputStream: FileInputStream
    private val mFileOutputStream: FileOutputStream

    // Getters and setters
    val inputStream: InputStream
        get() = mFileInputStream

    val outputStream: OutputStream
        get() = mFileOutputStream

    init {

        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                val su = Runtime.getRuntime().exec("/system/xbin/su")
                val cmd = ("chmod 666 " + device.absolutePath + "\n"
                        + "exit\n")
                su.outputStream.write(cmd.toByteArray())
                if (su.waitFor() != 0 || !device.canRead()
                    || !device.canWrite()
                ) {
                    throw SecurityException()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw SecurityException()
            }

        }

        mFd = open(device.absolutePath, baudrate, 0)
        if (mFd == null) {
            Log.e("SerialPort", "native open returns null")
            throw IOException()
        }
        mFileInputStream = FileInputStream(mFd)
        mFileOutputStream = FileOutputStream(mFd)
    }
    // JNI
    external fun close()
    private external fun open(path: String, baudrate: Int, flags: Int): FileDescriptor

    init {
        System.loadLibrary("serial_port")
    }
}
