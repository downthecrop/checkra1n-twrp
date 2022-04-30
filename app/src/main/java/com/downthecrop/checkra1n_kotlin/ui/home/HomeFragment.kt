package com.downthecrop.checkra1n_kotlin.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html.fromHtml
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders.*
import com.downthecrop.checkra1n_kotlin.BuildConfig
import com.downthecrop.checkra1n_kotlin.R
import java.io.*


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        homeViewModel = of(this).get(HomeViewModel::class.java)

        val colorSuccess = "<font color=#657b83>"
        val colorErr = "<font color=#b58900>"

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val messageLog: TextView = root.findViewById(R.id.text_home)
        val startButton: Button = root.findViewById(R.id.reboot_button)
        val cacheDir = requireContext().cacheDir
        val localZipDir = "/data/checkra1n"
        val localRecoveryDir = "/cache/recovery"
        val archArray = arrayOf("aarch64","armv4","armv4t","armv5t","armv5te","armv5tej","armv6","armv7","armv7l")
        var archIndex = -1

        fun shellExec(cmd: String?): String? {
            var o: String? = ""
            try {
                val p = Runtime.getRuntime().exec(cmd)
                val b = BufferedReader(InputStreamReader(p.inputStream))
                var line: String?
                while (b.readLine().also { line = it } != null) o += line
            } catch (e: Exception) {
                o = "error"
            }
            return o
        }

        fun copyToCache(resourceId: Int, resourceName: String) {

            val cacheFileURI: String = requireContext().cacheDir.toString() +"/"+ resourceName

            try {
                val `in` = resources.openRawResource(resourceId)
                val out: FileOutputStream?
                out = FileOutputStream(cacheFileURI)
                val buff = ByteArray(1024)
                var read: Int
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

        fun addToLog(message: String, color: Int = -1){
            when (color){
                -1 -> messageLog.append(fromHtml("<em>$message</em><br>"))
                0 -> messageLog.append(fromHtml("<em>$colorErr$message</font></em><br>"))
                1 -> messageLog.append(fromHtml("<em>$colorSuccess$message</font></em><br>"))
            }
        }

        fun arch(): Boolean {
            archIndex = archArray.indexOf(System.getProperty("os.arch")) //Set index for printing
            return archArray.contains(System.getProperty("os.arch")) //True or false return for support arch
        }

        fun archName(): String? {
            arch()
            return System.getProperty("os.arch")
        }

        fun binaryPlatform(): String {
            return if (archIndex > 0)
                "Using checkra1n-arm (32bit)"
            else
                "Using checkra1n-arm64 (64bit)"
        }

        fun setButtonText(message: String) {
            startButton.text = message
        }

        messageLog.movementMethod = ScrollingMovementMethod()
        val dialogBuilder = AlertDialog.Builder(this.context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)

        val versionName = BuildConfig.VERSION_NAME
        addToLog("Info: checkra1n Android<br>" +
                "binary version: $versionName arm64 & arm32<br>"+
                "os.arch: "+archName()+"<br>"+
                "binary: "+binaryPlatform())
        if (!arch()){
            addToLog("Unsupported architecture. Only ARM and ARM64 CPU's supported",0)
        }
        var checkRoot = shellExec("su -c echo su")

        if (checkRoot == "su" && arch()){
            setButtonText("Run checkra1n (Reboot Recovery)")
            addToLog("Log: Root verified",1)
        } else{
            setButtonText("Running in user mode. Enable Root.")
            addToLog("ERR: Root is required for operation",0)
        }

        dialogBuilder.setMessage("Do you want to reboot into TWRP to run checkra1n?")
            .setCancelable(false)
            .setPositiveButton("Proceed") { _, _ ->
                shellExec("su -c reboot recovery")
            }
            .setNegativeButton("Cancel") { _, _ ->
                addToLog("ERR: Declined Reboot", 0)
            }

        startButton.setOnClickListener {

            try {

                checkRoot = shellExec("su -c echo su")

                if (checkRoot == "su" && arch()){

                    setButtonText("Run checkra1n (Reboot Recovery)")
                    val rawZipFile = if (archIndex > 0) R.raw.checkra1n_arm64 else R.raw.checkra1n_arm

                    copyToCache(rawZipFile, "checkra1n.zip")
                    addToLog("Log: Copying checkra1n.zip to $cacheDir")

                    copyToCache(R.raw.bootcommand, "command")
                    addToLog("Log: Copying bootcommand to $cacheDir")

                    val cacheZip = File("$cacheDir/checkra1n.zip")
                    val cacheRecovery = File("$cacheDir/command")

                    if (cacheZip.exists())
                        addToLog("Log: Validated checkra1n.zip in $cacheDir",1)
                    else
                        addToLog("ERR: Failed to copy checkra1n.zip to $cacheDir",0)
                    if (cacheRecovery.exists())
                        addToLog("Log: Validated bootcommand in $cacheDir",1)
                    else
                        addToLog("ERR: Failed to copy bootcommand to $cacheDir",0)

                    addToLog("Log: Copying from cache to local..")
                    shellExec("su -c mkdir $localZipDir")
                    shellExec("su -c cp $cacheDir/checkra1n.zip $localZipDir")
                    shellExec("su -c cp $cacheDir/command $localRecoveryDir/command")

                    //Verify that the copied files from the app cache are in their intended places
                    val localZip = shellExec("su -c [ -f '/data/checkra1n/checkra1n.zip' ] && echo True || echo False")?.toBoolean()
                    val localRecovery = shellExec("su -c [ -f '/cache/recovery/command' ] && echo True || echo False")?.toBoolean()

                    if (localZip!!)
                        addToLog("Log: checkra1n.zip located at $localZipDir")
                    else
                        addToLog("ERR: checkra1n.zip NOT FOUND $localZipDir",0)

                    if (localRecovery!!)
                        addToLog("Log: boot command located at $localRecoveryDir")
                    else
                        addToLog("ERR: boot command NOT FOUND $localRecoveryDir",0)

                    if(localZip && localRecovery){
                        addToLog("SUCCESS: Ready to boot recovery.",1)
                        val alert = dialogBuilder.create()
                        alert.setTitle("You are about to reboot")
                        alert.show()
                    } else{
                        addToLog("ERR: Failed at final step. Please try again.",0)
                    }
                } else{
                    setButtonText("Running in user mode. Enable Root.")
                    addToLog("ERR: Unable to execute",0)
                }
            } catch (e: IOException) {
                addToLog("ERR: IOException in main task. Unable to execute",0)
            }
        }
        return root
    }
}