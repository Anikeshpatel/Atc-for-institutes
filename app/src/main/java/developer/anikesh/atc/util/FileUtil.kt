package developer.anikesh.atc.util

import android.os.Environment
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object FileUtil {
    private var HOME_DIR = File(Environment.getExternalStorageDirectory().absolutePath +File.separator+ "Atc")
    fun init() {
        if (!HOME_DIR.exists()) {
            HOME_DIR.mkdir()
        }
    }

    fun saveDocument(rows: List<String>) {
        init()
        val writer = BufferedWriter(FileWriter(File(HOME_DIR.absolutePath +File.separator+ getDateString(System.currentTimeMillis()) + ".csv")))
        for (row in rows) {
            writer.write(row)
            writer.newLine()
        }
        writer.close()
    }

    private fun getDateString(timeStamp: Long): String {
        val formate = SimpleDateFormat("dd-MM-yyyy")
        val date = Date(timeStamp)
        return formate.format(date)
    }
}