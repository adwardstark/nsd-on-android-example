package com.adwardstark.nsd_example

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.adwardstark.nsd_example.databinding.ActivityRegisterServiceBinding

class RegisterServiceActivity : AppCompatActivity() {

    companion object {
        private val TAG = RegisterServiceActivity::class.java.simpleName
    }

    private lateinit var viewBinder: ActivityRegisterServiceBinding

    private lateinit var nsdManager: NsdManager

    private var isServiceRegistered: Boolean = false

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            Log.d(TAG,"->onServiceRegistered() serviceName: ${NsdServiceInfo.serviceName}")
            printToConsole("Service registered with name: ${NsdServiceInfo.serviceName}")
            isServiceRegistered = true
            updateRegisterButtonText(true)
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            Log.d(TAG, "->onRegistrationFailed()")
            printToConsole("Failed to register service, error-code: $errorCode")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG, "->onServiceUnregistered()")
            printToConsole("Service unregistered.")
            isServiceRegistered = false
            updateRegisterButtonText(false)
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            Log.d(TAG, "->onUnregistrationFailed()")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = ActivityRegisterServiceBinding.inflate(layoutInflater)
        setContentView(viewBinder.root)

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager)

        viewBinder.registerButton.setOnClickListener {
            if(isServiceRegistered) {
                nsdManager.unregisterService(registrationListener)
            } else {
                registerService()
            }
        }

    }

    private fun printToConsole(text: String) {
        runOnUiThread {
            var console = viewBinder.consoleText.text.toString()
            console += "\n> $text"
            viewBinder.consoleText.text = console
        }
    }

    private fun updateRegisterButtonText(isRegistered: Boolean) {
        runOnUiThread {
            if(isRegistered) {
                viewBinder.registerButton.text = getString(R.string.de_register_service)
            } else {
                viewBinder.registerButton.text = getString(R.string.register_service)
            }
        }
    }

    private fun registerService() {
        val serviceText = viewBinder.serviceNameText
        if(serviceText.text.isBlank()) {
            serviceText.error = "*Required"
            return
        }
        val serviceType = viewBinder.serviceTypeText
        if(serviceType.text.isBlank()) {
            serviceType.error = "*Required"
            return
        }

        val servicePort = viewBinder.servicePortText
        if(servicePort.text.isBlank()) {
            servicePort.error = "*Required"
            return
        }

        val mServiceName = serviceText.text.toString().trim()
        val mServiceType = serviceType.text.toString().trim()
        val mServicePort = servicePort.text.toString().trim().toInt()

        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            this.serviceName = mServiceName
            this.serviceType = mServiceType
            port = mServicePort
        }

        Log.d(TAG, "->registerService() $serviceInfo")

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isServiceRegistered) {
            nsdManager.unregisterService(registrationListener)
        }
    }

}