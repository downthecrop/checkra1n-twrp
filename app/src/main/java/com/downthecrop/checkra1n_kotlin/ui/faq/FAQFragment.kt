package com.downthecrop.checkra1n_kotlin.ui.faq

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.downthecrop.checkra1n_kotlin.R

class FAQFragment : Fragment() {

    private lateinit var faqViewModel: FAQViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        faqViewModel = ViewModelProviders.of(this).get(FAQViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_faq, container, false)
        val textView: TextView = root.findViewById(R.id.text_faq)
        faqViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = (Html.fromHtml("" +
                    "<br><h1>FAQ</h1>" +
                    "<li> This application allows for automated startup of a bundled .zip to execute checkra1n in TWRP to jailbreak supported iOS devices. More info and source code available here: <a href='https://github.com/downthecrop/checkra1n-twrp'>https://github.com/downthecrop/checkra1n-twrp</a></li>\n" +
                    "<li> Requirements: Root, TWRP Custom Recovery, USB-C to USB-A Adapter, arm64 based Android device, Supported iOS device </li>\n" +
                    "<li> Why run in recovery? TWRP provides an environment with fewer services fighting over the USB controller. checkra1n relies on a highly controlled data flow over USB.</li>\n" +
                    "<li> Will this work with all Android phones? No. If you could already do this manually in TWRP, yes definitely. My Nexus 5X (2015) and Mi Mix 3 (2018) both work. </li>\n" +
                    "<h1>DFU Mode Instructions</h1>" +
                    "You will be required to manually enter DFU mode without on screen instructions. Please familiarize yourself with the specific button combinations and timings for your device." +
                    "<br>" +
                    "<h1>iPad, iPhone 6s and below, iPhone SE and iPod touch</h1>\n" +
                    "<ol>\n" +
                    "<li> Hold down both the Home button and Lock button.</li>\n" +
                    "<li> After 8 seconds, release the Lock button while continuing to hold down the Home button.</li>\n" +
                    "<li> If the Apple logo appears, the Lock button was held down for too long.</li>\n" +
                    "<li> Nothing will be displayed on the screen when the device is in DFU mode.</li>\n" +
                    "</ol>\n" +
                    "\n" +
                    "<h1>iPhone 7 and iPhone 7 Plus</h1>\n" +
                    "<ol>\n" +
                    "<li> Hold down both the Side button and Volume Down button.</li>\n" +
                    "<li> After 8 seconds, release the Side button while continuing to hold down the Volume Down button.</li>\n" +
                    "<li> If the Apple logo appears, the Side button was held down for too long.</li>\n" +
                    "<li> Nothing will be displayed on the screen when the device is in DFU mode.</li>\n" +
                    "</ol>\n" +
                    "\n" +
                    "<h1>iPhone 8, iPhone 8 Plus and iPhone X</h1>\n" +
                    "<ol>\n" +
                    "<li> Quick-press the Volume Up button</li>\n" +
                    "<li> Quick-press the Volume Down button</li>\n" +
                    "<li> Hold down the Side button until the screen goes black, then hold down both the Side button and Volume Down button.</li>\n" +
                    "<li> After 5 seconds, release the Side button while continuing to hold down the Volume Down button.</li>\n" +
                    "<li> If the Apple logo appears, the Side button was held down for too long.</li>\n" +
                    "<li> Nothing will be displayed on the screen when the device is in DFU mode.</li>\n" +
                    "</ol>\n" +
                    "\n" +
                    "<h1>Exiting DFU Mode</h1>\n" +
                    "If this Android device is unable to execute checkra1n you can manually exit DFU to return to iOS\n" +
                    "<ul>\n" +
                    "<li> For iPad, iPhone 6s and below, iPhone SE and iPod touch: hold the Home button and the Lock button until the device reboots.</li>\n" +
                    "<li> For iPhone 7 and iPhone 7 Plus: hold down the Side button and Volume Down button until the device reboots.</li>\n" +
                    "<li> For iPhone 8, iPhone 8 Plus, and iPhone X: quick-press the Volume Up button, then quick-press the Volume Down button, then hold down the Side button until the device reboots.</li>\n" +
                    "</ul>"
            ))
        })
        return root
    }
}