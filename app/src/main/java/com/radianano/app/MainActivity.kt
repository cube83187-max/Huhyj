package com.radianano.app

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private lateinit var usbManager: UsbManager
    private var port: UsbSerialPort? = null
    private var serialConnection: android.hardware.usb.UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null

    private val executor = Executors.newSingleThreadExecutor()
    private val buffer = StringBuilder()

    private lateinit var statusText: TextView
    private lateinit var cpmText: TextView
    private lateinit var pulseText: TextView

    private var currentCpm = 0
    private var totalPulses = 0L

    companion object {
        private const val ACTION_USB_PERMISSION =
            "com.radianano.app.USB_PERMISSION"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_USB_PERMISSION) {
                if (intent.getBooleanExtra(
                        UsbManager.EXTRA_PERMISSION_GRANTED, false
                    )
                ) {
                    connectUsb()
                } else {
                    statusText.text = "USB permission denied"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)

        createInterface()
    }

    private fun createInterface() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 28, 28, 28)
        }

        val title = TextView(this).apply {
            text = "RadiaNano"
            textSize = 30f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "Disconnected"
            textSize = 18f
            gravity = Gravity.CENTER
        }

        cpmText = TextView(this).apply {
            text = "CPM: 0"
            textSize = 32f
            gravity = Gravity.CENTER
        }

        pulseText = TextView(this).apply {
            text = "Total pulses: 0"
            textSize = 20f
            gravity = Gravity.CENTER
        }

        val connectButton = Button(this).apply {
            text = "Connect Arduino USB"
            setOnClickListener { requestUsbPermission() }
        }

        val disconnectButton = Button(this).apply {
            text = "Disconnect"
            setOnClickListener { disconnectUsb() }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(cpmText)
        layout.addView(pulseText)
        layout.addView(connectButton)
        layout.addView(disconnectButton)

        setContentView(layout)
    }

    private fun requestUsbPermission() {
        val drivers = UsbSerialProber.getDefaultProber()
            .findAllDrivers(usbManager)

        if (drivers.isEmpty()) {
            statusText.text = "No USB serial device found"
            return
        }

        val device = drivers[0].device

        if (usbManager.hasPermission(device)) {
            connectUsb()
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                this,
                0,
                Intent(ACTION_USB_PERMISSION).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_MUTABLE
            )

            usbManager.requestPermission(device, permissionIntent)
        }
    }

    private fun connectUsb() {
        try {
            val drivers = UsbSerialProber.getDefaultProber()
                .findAllDrivers(usbManager)

            if (drivers.isEmpty()) {
                statusText.text = "No Arduino found"
                return
            }

            val driver = drivers[0]
            val connection = usbManager.openDevice(driver.device)

            if (connection == null) {
                statusText.text = "Cannot open USB device"
                return
            }

            serialConnection = connection
            port = driver.ports[0]

            port?.open(connection)
            port?.setParameters(
                9600,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            val listener =
                object : SerialInputOutputManager.Listener {

                    override fun onNewData(data: ByteArray) {
                        val received = String(data, Charsets.UTF_8)

                        runOnUiThread {
                            processData(received)
                        }
                    }

                    override fun onRunError(e: Exception) {
                        runOnUiThread {
                            statusText.text =
                                "Serial error: ${e.message}"
                        }
                    }
                }

            ioManager = SerialInputOutputManager(port, listener)
            executor.submit(ioManager)

            statusText.text = "Connected - 9600 baud"

        } catch (e: Exception) {
            statusText.text = "Connection error: ${e.message}"
            disconnectUsb()
        }
    }

    private fun processData(data: String) {
        buffer.append(data)

        while (buffer.contains("\n")) {
            val index = buffer.indexOf("\n")
            val line = buffer.substring(0, index).trim()
            buffer.delete(0, index + 1)

            if (line.isEmpty()) continue

            when {
                line.startsWith("CPM:", true) -> {
                    currentCpm =
                        line.substringAfter(":").trim().toIntOrNull()
                            ?: currentCpm
                }

                line.startsWith("PULSE:", true) -> {
                    totalPulses =
                        line.substringAfter(":").trim().toLongOrNull()
                            ?: totalPulses
                }

                else -> {
                    line.toIntOrNull()?.let {
                        currentCpm = it
                    }
                }
            }

            updateDisplay()
        }
    }

    private fun updateDisplay() {
        cpmText.text = "CPM: $currentCpm"
        pulseText.text = "Total pulses: $totalPulses"
    }

    private fun disconnectUsb() {
        try {
            ioManager?.stop()
            ioManager = null
            port?.close()
            port = null
            serialConnection?.close()
            serialConnection = null
            statusText.text = "Disconnected"
        } catch (e: Exception) {
            statusText.text = "Disconnected"
        }
    }

    override fun onDestroy() {
        disconnectUsb()
        try {
            unregisterReceiver(usbReceiver)
        } catch (_: Exception) {
        }
        executor.shutdownNow()
        super.onDestroy()
    }
}
