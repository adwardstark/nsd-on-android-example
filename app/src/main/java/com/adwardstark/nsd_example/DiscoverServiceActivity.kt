package com.adwardstark.nsd_example

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.adwardstark.nsd_example.databinding.ActivityDiscoverServiceBinding
import java.net.InetAddress

class DiscoverServiceActivity : AppCompatActivity() {

    companion object {
        private val TAG = DiscoverServiceActivity::class.java.simpleName
        // 10 seconds time-out
        private const val DISCOVERY_TIME_OUT: Long = 10000
    }

    private lateinit var viewBinder: ActivityDiscoverServiceBinding

    private lateinit var nsdManager: NsdManager

    private var isDiscoveryInProgress: Boolean = false

    private var mServiceName = "NsdService"
    private var mServiceType = "_nsd_service._tcp."

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "->onDiscoveryStarted()")
            printToConsole("Discovery started for: $mServiceType")
            isDiscoveryInProgress = true
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "->onServiceFound() Service: $service")
            when {
                service.serviceType != mServiceType -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: $mServiceName")
            }
            if(service.serviceName.contains(mServiceName)) {
                printToConsole("Found service: $mServiceName")
                nsdManager.resolveService(service, object : NsdManager.ResolveListener {

                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        // Called when the resolve fails. Use the error code to debug.
                        Log.e(TAG, "->onResolveFailed() error-code: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        Log.e(TAG, "->onServiceResolved() $serviceInfo")

                        if (serviceInfo.serviceName == mServiceName) {
                            Log.d(TAG, "Same IP.")
                        }
                        val port: Int = serviceInfo.port
                        val host: InetAddress = serviceInfo.host

                        Log.d(TAG, "Found IP: ${host.hostAddress}, port: $port")
                        printToConsole("with IP: ${host.hostAddress}, port: $port")
                    }
                })
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "->onServiceLost() $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "->onDiscoveryStopped() $serviceType")
            printToConsole("Discovery stopped.")
            isDiscoveryInProgress = false
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "->onStartDiscoveryFailed() error-code:$errorCode")
            if(isDiscoveryInProgress){
                nsdManager.stopServiceDiscovery(this)
                isDiscoveryInProgress = false
            } else {
                Toast.makeText(this@DiscoverServiceActivity,
                    "Failed with code: $errorCode", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "->onStopDiscoveryFailed() error-code:$errorCode")
            if(isDiscoveryInProgress){
                nsdManager.stopServiceDiscovery(this)
                isDiscoveryInProgress = false
            } else {
                Toast.makeText(this@DiscoverServiceActivity,
                    "Failed with code: $errorCode", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinder = ActivityDiscoverServiceBinding.inflate(layoutInflater)
        setContentView(viewBinder.root)

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager)

        viewBinder.discoveryButton.setOnClickListener {
            if(!isDiscoveryInProgress) {
                startAndStopDiscovery()
            } else {
                Toast.makeText(this, "Discovery already in progress", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun startAndStopDiscovery() {
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

        mServiceName = serviceText.text.toString().trim()
        mServiceType = serviceType.text.toString().trim()

        nsdManager.apply {
            discoverServices(mServiceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if(isDiscoveryInProgress) {
                printToConsole("Discovery time-out.")
                Log.d(TAG, "Discovery time-out.")
                nsdManager.stopServiceDiscovery(discoveryListener)
            }
        }, DISCOVERY_TIME_OUT)
    }

    private fun printToConsole(text: String) {
        runOnUiThread {
            var console = viewBinder.consoleText.text.toString()
            console += "\n> $text"
            viewBinder.consoleText.text = console
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nsdManager.apply {
            if(isDiscoveryInProgress) {
                stopServiceDiscovery(discoveryListener)
            }
        }
    }
}