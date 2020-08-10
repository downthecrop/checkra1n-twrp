package com.downthecrop.checkra1n_kotlin.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.downthecrop.checkra1n_kotlin.R
import java.io.*
import java.security.MessageDigest
import kotlin.experimental.and


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val messageLog: TextView = root.findViewById(R.id.text_home)
        val startButton: Button = root.findViewById(R.id.reboot_button)
        val cacheDir = requireContext().cacheDir
        val bootHash = "3B9CB161FE0C765397FA1E1E809819CE"
        val zipHash = "21FC5E50F47D9BC9882C5F0386BBD5EC"
        var errorCode = 0

        fun addToLog(message: String){
            messageLog.append(Html.fromHtml("<em>$message</em><br>"))
        }

        fun addToLog(message: String, color: Int){

            val colorSuccess = "<font color=#657b83>"
            val colorErr = "<font color=#b58900>"

            if (color == 0)
                messageLog.append(Html.fromHtml("<em>$colorErr$message</font></em><br>"))
            else if (color == 1)
                messageLog.append(Html.fromHtml("<em>$colorSuccess$message</font></em><br>"))
        }

        fun setButtonText(message: String) {
            startButton.text = "$message"
        }

        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this.context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)

        // set message of alert dialog
        dialogBuilder.setMessage("Do you want to reboot into TWRP to run checkra1n?")
            .setCancelable(false)
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> shellExec("su -c reboot recovery")
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> addToLog("ERR: Declined Reboot",0)
            })

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            messageLog.setMovementMethod(ScrollingMovementMethod())
            addToLog(""+
                    "Info: checkra1n Android<br>" +
                    "Bundled binary version: 0.10.2 arm64 Linux")
        })

        homeViewModel.button.observe(viewLifecycleOwner, Observer {
            try{

                val checkRoot = shellExec("su -c echo su")

                if (checkRoot == "su"){
                    setButtonText("Run checkra1n (Reboot Recovery)")
                    addToLog("Log: Root verified",1)
                } else{
                    setButtonText("Running in user mode. Enable Root.")
                }
            }
            catch (e: IOException) {
                setButtonText("Device Not Rooted")
                addToLog("Log: This Android device isn't rooted. Root is required for operation")
                errorCode = 1
            }

            startButton.setOnClickListener(){
                try {

                    val checkRoot = shellExec("su -c echo su")

                    if (checkRoot == "su"){

                        setButtonText("Run checkra1n (Reboot Recovery)")

                        copyToCache(R.raw.checkra1n, "checkra1n.zip");
                        copyToCache(R.raw.bootcommand, "command");

                        val cacheZIP = File("$cacheDir/checkra1n.zip")
                        val cacheBoot = File("$cacheDir/command")

                        val cacheZipHash = cacheZIP.calcHash().toHexString().toUpperCase()
                        val cacheBootHash = cacheBoot.calcHash().toHexString().toUpperCase()

                        if (cacheZipHash == zipHash)
                            addToLog("Log: Copied checkra1n.zip to $cacheDir")
                        if (cacheBootHash == bootHash)
                            addToLog("Log: Copied bootcommand to $cacheDir")


                        shellExec("su -c mkdir /data/checkra1n")
                        shellExec("su -c cp $cacheDir/checkra1n.zip /data/checkra1n/")
                        shellExec("su -c cp $cacheDir/command /cache/recovery/command")

                        //Verify that the copied files from the app cache are in their intended places
                        var verifyCheckra1nZIP = shellExec("su -c [ -f '/data/checkra1n/checkra1n.zip' ] && echo true || echo false")
                        var verifyBootCommand = shellExec("su -c [ -f '/cache/recovery/command' ] && echo true || echo false")

                        if (verifyCheckra1nZIP == "true"){
                            addToLog("Log: checkra1n.zip located at /data/checkra1n/")
                        } else{
                            addToLog("ERR: checkra1n.zip NOT FOUND /data/checkra1n/",0)
                        }
                        if (verifyBootCommand == "true"){
                            addToLog("Log: boot command located at /cache/recovery/command")
                        } else{
                            addToLog("ERR: boot command NOT FOUND /cache/recovery/command",0)
                        }


                        if(verifyBootCommand == "true" && verifyCheckra1nZIP == "true"){
                            addToLog("SUCCESS: Ready to boot recovery.",1)
                            val alert = dialogBuilder.create()
                            alert.setTitle("You are about to reboot")
                            alert.show()
                        }
                    } else if(errorCode < 1){
                        setButtonText("Running in user mode. Enable Root.")
                    }
                } catch (e: IOException) {
                }
            }
        })
        return root
    }

    private fun shellExec(cmd: String?): String? {
        var o: String? = ""
        try {
            val p = Runtime.getRuntime().exec(cmd)
            val b = BufferedReader(InputStreamReader(p.inputStream))
            var line: String? = ""
            while (b.readLine().also({ line = it }) != null) o += line
        } catch (e: Exception) {
            o = "error"
        }
        return o
    }

    private fun copyToCache(resourceId: Int, resourceName: String) {

        val cacheFileURI: String = requireContext().cacheDir.toString() +"/"+ resourceName

        try {
            val `in` = resources.openRawResource(resourceId)
            var out: FileOutputStream? = null
            out = FileOutputStream(cacheFileURI)
            val buff = ByteArray(1024)
            var read = 0
            try {
                while (`in`.read(buff).also { read = it } > 0) {
                    out.write(buff, 0, read)
                }
            } finally {
                `in`.close()
                out.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun File.calcHash(algorithm: String = "MD5", bufferSize: Int = 1024): ByteArray {
        this.inputStream().use { input ->
            val buffer = ByteArray(bufferSize)
            val digest = MessageDigest.getInstance(algorithm)

            read@ while (true) {
                when (val bytesRead = input.read(buffer)) {
                    -1 -> break@read
                    else -> digest.update(buffer, 0, bytesRead)
                }
            }
            return digest.digest()
        }
    }

    fun ByteArray.toHexString(): String {
        return this.fold(StringBuilder()) { result, b -> result.append(String.format("%02X", b)) }.toString()
    }

}