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


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        val myButton: Button = root.findViewById(R.id.reboot_button)
        val cacheDir = requireContext().cacheDir
        var errorCode = 0

        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this.context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)

        // set message of alert dialog
        dialogBuilder.setMessage("Do you want to reboot into TWRP to run checkra1n?")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                    dialog, id -> Runtime.getRuntime().exec("su -c reboot recovery")
            })
            // negative button text and action
            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                    dialog, id -> textView.append(Html.fromHtml("<em><font color=#b58900>ERR: Declined Reboot</font><br>"))

            })

        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.setMovementMethod(ScrollingMovementMethod())
            textView.text = (Html.fromHtml(""+
                    "<em>Info: checkra1n Android<br>" +
                    "<em>Bundled binary version: 0.10.2 arm64 Linux<br>"))
        })
        homeViewModel.button.observe(viewLifecycleOwner, Observer {

            try{
                Runtime.getRuntime().exec("su")
                val checkRoot = shell_exec("su -c echo su")

                if (checkRoot == "su"){
                    myButton.text = "Run checkra1n (Reboot Recovery)"
                    textView.append(Html.fromHtml("<em>Log: Root verified<br>"))
                }
                else{
                    myButton.text = "Running in user mode. Enable Root."
                }
            }
            catch (e: IOException) {
                myButton.text = "Device Not Rooted"
                textView.append(Html.fromHtml("<em>Log: This Android device isn't rooted. Root is required for operation<br>"))
                errorCode = 1

            }

            myButton.setOnClickListener(){
                try {

                    val checkRoot = shell_exec("su -c echo su")

                    if (checkRoot == "su"){

                        myButton.text = "Run checkra1n (Reboot Recovery)"

                        copyToCache(R.raw.checkra1n, "checkra1n.zip");
                        copyToCache(R.raw.bootcommand, "command");

                        textView.append(Html.fromHtml("<em>Log: Copied checkra1n.zip to $cacheDir<br>"))
                        textView.append(Html.fromHtml("<em>Log: Copied bootcommand to $cacheDir<br>"))

                        Runtime.getRuntime().exec("su -c mkdir /data/checkra1n")
                        Runtime.getRuntime().exec("su -c cp $cacheDir/checkra1n.zip /data/checkra1n/")
                        Runtime.getRuntime().exec("su -c cp $cacheDir/command /cache/recovery/command")

                        //Verify that the copied files from the app cache are in their intended places
                        var verifyCheckra1nZIP = shell_exec("su -c [ -f '/data/checkra1n/checkra1n.zip' ] && echo true || echo false")
                        var verifyBootCommand = shell_exec("su -c [ -f '/cache/recovery/command' ] && echo true || echo false")

                        if (verifyCheckra1nZIP == "true"){
                            textView.append(Html.fromHtml("<em>Log: checkra1n.zip located at /data/checkra1n/<br>"))
                        } else{
                            textView.append(Html.fromHtml("<em><font color=#b58900>Error: checkra1n.zip NOT FOUND /data/checkra1n/</font><br>"))
                        }
                        if (verifyBootCommand == "true"){
                            textView.append(Html.fromHtml("<em>Log: boot command located at /cache/recovery/command<br>"))
                        } else{
                            textView.append(Html.fromHtml("<em><font color=#b58900>ERR: boot command NOT FOUND /cache/recovery/command</font><br>"))
                        }


                        if(verifyBootCommand == "true" && verifyCheckra1nZIP == "true"){
                            textView.append(Html.fromHtml("<em><font color=#657b83>SUCCESS: Ready to boot recovery.</font><br>"))
                            val alert = dialogBuilder.create()
                            alert.setTitle("You are about to reboot")
                            alert.show()
                        }
                    }
                    else if(errorCode < 1){
                        myButton.text = "Running in user mode. Enable Root."
                    }

                } catch (e: IOException) {
                }
            }
        })


        return root
    }

    fun shell_exec(cmd: String?): String? {
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

    private fun copyToCache(
        resourceId: Int,
        resourceName: String
    ) {
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

}